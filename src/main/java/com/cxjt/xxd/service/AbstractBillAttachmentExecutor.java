package com.cxjt.xxd.service;

import com.alibaba.fastjson.JSONObject;
import com.cxjt.xxd.component.HttpService;
import com.cxjt.xxd.config.RemoteConfig;
import com.cxjt.xxd.dao.XXDLoanApplyBillAttTaskItemDao;
import com.cxjt.xxd.enums.JrcsErrorCodeEnum;
import com.cxjt.xxd.model.res.order.FileInfoRes;
import com.cxjt.xxd.util.FileUtil;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.util.JsonUtil;
import kd.bos.session.EncreptSessionUtils;
import kd.bos.url.UrlService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractBillAttachmentExecutor {

    private final Log logger = LogFactory.getLog(this.getClass());

    private static final String JRCS_PROTOCOL = RemoteConfig.getJrcsProtocol();
    private static final String HOST = RemoteConfig.getJrcsHost();
    private static final int PORT = RemoteConfig.getJrcsPort();
    private static final String PATH = "/fins-appsply/public/xxdNotice/sendFiles.do";
    private static final String URL = JRCS_PROTOCOL + "://" + HOST + ":" + PORT + PATH;

    //单据和单据附件总数映射
    private static Map<String, Integer> billMap = new ConcurrentHashMap<>();


    public void execute(DynamicObject taskBill) {
        String applyId = (String) taskBill.get("ukwo_apply_id");
        //获取启用状态【贷款申请单】附件总数
        Integer billTotalAttachCount = getBillTotalAttachCount(applyId);

        //获取执行器可处理【贷款申请单】附件
        DynamicObjectCollection attachmentList = query(applyId);

        boolean condition = attachmentList != null && attachmentList.size() > 0;
        if (!condition) {
            return;
        }

        //循环处理单据附件分类明细项
        for (DynamicObject attachment : attachmentList) {
            long id = (long) attachment.get("id");

            try {
                //将任务明细更新为“Running”
                boolean updateTaskItemFlag = XXDLoanApplyBillAttTaskItemDao.updateTaskItemForRunning(id);
                if (!updateTaskItemFlag) {
                    continue;
                }

                //若任务明细状态更新成功,则请求JRCS
                String responseStr = requestRemote(attachment, billTotalAttachCount);
                JSONObject jsonResult = JSONObject.parseObject(responseStr);
                String code = (String) jsonResult.get("code");
                String msg = (String) jsonResult.get("msg");
                //TODO code以及msg入库
                attachment.set("ukwo_end_time", new Date());
                if (JrcsErrorCodeEnum.SUCCESS.getCode().equals(code)) {
                    //attachment.set("ukwo_exec_status", BillResNotityStatusEnum.SUCCESS.getCode());
                    //更新任务明细状态
                    //SaveServiceHelper.update(new DynamicObject[]{attachment});
                    XXDLoanApplyBillAttTaskItemDao.updateTaskItemForSuccess(id);
                } else {
                    XXDLoanApplyBillAttTaskItemDao.updateTaskItemForFailed(id);
                }
            } catch (Exception e) {
                //attachment.set("ukwo_exec_status",  BillResNotityStatusEnum.FAILED.getCode());
                //更新任务明细状态
                //SaveServiceHelper.update(new DynamicObject[]{attachment});
                XXDLoanApplyBillAttTaskItemDao.updateTaskItemForFailed(id);
            }
        }

    }

    public String requestRemote(DynamicObject attachment, Integer billTotalAttachCount) throws Exception {
        String applyId = (String) attachment.get("ukwo_apply_id");
        String ukwoAttachmentUrl = (String) attachment.get("ukwo_attachment_url");


        Map<String, Object> reqMap = new HashMap<>();
        Map<String, Object> paraMap = new HashMap<>();
        paraMap.put("applyId", applyId);
        paraMap.put("totalNum", billTotalAttachCount + "");

        String ukwoGuaUniCodeEnt = (String) attachment.get("ukwo_gua_uni_code_ent");
        String ukwoAttachmentName = (String) attachment.get("ukwo_attachment_name");
        String ukwoAttachmentType = (String) attachment.get("ukwo_attachment_type");
        //ukwoAttachmentUrl没有Session信息的话,会需要登录之后,才能拿到Session信息
        //若ukwoAttachmentUrl不包含kdedcba,则需要拼接Session信息,如包含则说明已含有Session信息
        String encryptAttachmentUrl = getUrl(ukwoAttachmentUrl);
        String fileContent = generateBase64(encryptAttachmentUrl);

        //InputStream base64Is = XXDUtils.base2InputStream(fileContent);
        //String localFilePath = "D:\\test\\" + ukwoAttachmentName;
        //XXDUtils.downloadFile(base64Is, localFilePath);

        FileInfoRes fileInfo = new FileInfoRes(ukwoAttachmentType, ukwoAttachmentName, fileContent, ukwoGuaUniCodeEnt);
        List<FileInfoRes> fileList = new ArrayList<>(1);
        fileList.add(fileInfo);

        paraMap.put("fileList", fileList);

        reqMap.put("body", paraMap);


        String jsonData = JsonUtil.format(reqMap);
        //logger.info("开始请求JRCS文件上传接口,地址如下:URL={}", URL);
        logger.info("开始请求JRCS文件上传接口,参数如下:ukwoGuaUniCodeEnt={},ukwoAttachmentType={},ukwoAttachmentName={}", ukwoGuaUniCodeEnt,ukwoAttachmentType,ukwoAttachmentName);
        String responseStr = HttpService.getService().doPostByHttpClient(URL, jsonData);
        logger.info("JRCS尽文件上传接口响应信息:applyId={},responseStr={}", applyId, responseStr);

        return responseStr;
    }

    private String getUrl(String url) {
        if (!url.contains("/download.do")) {
            url = UrlService.getAttachmentFullUrl(url);
        }

        url = url.contains("&kdedcba") ? url :
                EncreptSessionUtils.encryptSession(url);

        return url;
    }

    /**
     * 获取启用状态【贷款申请单】附件总数
     *
     * @param applyId
     * @returnWW
     */
    public final Integer getBillTotalAttachCount(String applyId) {

        Integer billTotalAttachCount = billMap.get(applyId);
        if (billTotalAttachCount == null) {
            //获取【贷款申请单】附件总数
            billTotalAttachCount = XXDLoanApplyBillAttTaskItemDao.getTaskItemCount(applyId);
            billMap.put(applyId, billTotalAttachCount);
        }

        return billTotalAttachCount;
    }

    /**
     * 删除贷款申请单】附件总数
     *
     * @param applyId
     */
    public final void removeBillTotalAttachCount(String applyId) {
        billMap.remove(applyId);
    }

    /**
     * 获取启用状态【贷款申请单】执行成功附件总数
     *
     * @param applyId
     * @returnWW
     */
    public final Integer getBillSuccessTotalAttachCount(String applyId) {

        Integer billSuccessTotalAttachCount = XXDLoanApplyBillAttTaskItemDao.getTaskItemSuccessCount(applyId);


        return billSuccessTotalAttachCount;
    }

    /**
     * 根据文件url获取base64字符串
     *
     * @param attachmentFileUrl
     * @return
     */
    public String generateBase64(String attachmentFileUrl) {
        try {
            String base64Str = FileUtil.byte2Base64(attachmentFileUrl);
            //InputStream inputStream = XXDUtils.getInputStreamFromUrl(attachmentFileUrl);
            //base64Str = XXDUtils.inputStream2Base64(inputStream);
            return base64Str;
        } catch (Exception e) {
            throw new RuntimeException("根据文件url获取base64字符串出现异常", e);
        }

    }

    /**
     * 获取【贷款申请单】附件数据
     *
     * @param applyId
     * @return
     */
    public abstract DynamicObjectCollection query(String applyId);

    /**
     * 执行器描述信息
     *
     * @return
     */
    public abstract String getDesc();
}
