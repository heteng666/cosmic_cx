package com.cxjt.xxd.plugin.mobile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cxjt.xxd.component.HttpService;
import com.cxjt.xxd.config.RemoteConfig;
import com.cxjt.xxd.enums.JrcsErrorCodeEnum;
import com.cxjt.xxd.util.FileUtil;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.exception.KDBizException;
import kd.bos.form.control.AttachmentPanel;
import kd.bos.form.control.events.*;
import kd.bos.form.events.ClientCallBackEvent;
import kd.bos.form.plugin.AbstractMobFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.util.JsonUtil;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;


public class XXDBizLicensePlugin extends AbstractMobFormPlugin implements UploadListener {
    private final Log LOGGER = LogFactory.getLog(this.getClass());

    private static final String JRCS_PROTOCOL = RemoteConfig.getJrcsProtocol();
    private static final String HOST = RemoteConfig.getJrcsHost();
    private static final int PORT = RemoteConfig.getJrcsPort();
    private static final String URL = JRCS_PROTOCOL + "://" + HOST + ":" + PORT + "/fins-precloud/tencentCloud/BizLicenseOCR.do";

    private static final String ATTACHMENT_PANEL_KEY = "ukwo_biz_license";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        // 侦听附件面板控件的文件上传事件
        AttachmentPanel attachmentPanel = this.getView().getControl(ATTACHMENT_PANEL_KEY);
        attachmentPanel.addUploadListener(this);
        AttachmentPanel attachmentPanel1 = this.getView().getControl("ukwo_resolution");
        attachmentPanel1.addUploadListener(this);

    }

    @Override
    public void upload(UploadEvent evt) {

        AttachmentPanel attachmentPanel = (AttachmentPanel) evt.getSource();
        String key = attachmentPanel.getKey();  //当前控件标识
        this.getView().setVisible(false, key + "ap");
        UploadListener.super.upload(evt);
    }

    @Override
    public void afterUpload(UploadEvent evt) {
        AttachmentPanel attachmentPanel = (AttachmentPanel) evt.getSource();
        String key = attachmentPanel.getKey();  //当前控件标识
        if (!"ukwo_biz_license".equals(key)) {
            return;
        }
        if (evt.getUrls().length > 1) {
            throw new KDBizException("营业执照至多上传一个");
        }
        Map<String, Object> attachInfo = (Map<String, Object>) evt.getUrls()[0];    //营业执照附件信息
        String fileUrl = attachInfo.get("url").toString();  //文件下载地址
        LOGGER.info("文件地址：" + fileUrl);
        String base64Str = null;
        try {
            byte[] bytes = FileUtil.download(fileUrl);
            base64Str = FileUtil.byte2Base64(bytes);
        } catch (Exception e) {
            LOGGER.error("营业执照上传异常：{}", e.getMessage());
        }
        if (StringUtils.isBlank(base64Str)) {
            this.getView().showErrorNotification("营业执照上传异常，请重新上传");
            return;
        }
        Map<String, Object> reqParam = new HashMap<>();
        Map<String, String> body = new HashMap<>();
        body.put("ImageBase64", base64Str);
        reqParam.put("body", body);
        this.getPageCache().put("reqParam", JSON.toJSONString(reqParam));
        this.getView().showLoading(new LocaleString("OCR识别中，请稍等"), 0, false);
        this.getView().addClientCallBack("afterUploadLicense", 1);
    }

    /**
     * addClientCallBack到期回调方法
     */
    @Override
    public void clientCallBack(ClientCallBackEvent e) {
        if ("afterUploadLicense".equals(e.getName())) {
            this.getPageCache().put("OCR-company", "false");
            try {
                String jsonData = this.getPageCache().get("reqParam");
                Map<String, Object> reqParam = JSONObject.parseObject(jsonData, Map.class);
                String responseStr = HttpService.getService().doPostByHttpClient(URL, JsonUtil.format(reqParam));
                LOGGER.info("OCR识别完毕：{}", responseStr);
                JSONObject resultObj = JSONObject.parseObject(responseStr);
                String code = resultObj.getString("code");
                String msg = resultObj.getString("msg");
                if (JrcsErrorCodeEnum.SUCCESS.getCode().equals(code)) {
                    // 字段回显  Period 2021年02月25日至长期  SetDate 2021年02月25日
                    JSONObject data = resultObj.getJSONObject("body");
                    String enterpriseName = data.getString("Name");
                    String socialCode = data.getString("RegNum");
                    String period = data.getString("Period");
                    String[] dateArr = period.split("至");
                    if (!dateArr[1].contains("长期")) {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
                        Date startDate = format.parse(dateArr[0].trim());
                        Date endTime = format.parse(dateArr[1].trim());
                        Date nowDate = new Date();
                        if (nowDate.getTime() < startDate.getTime() || nowDate.getTime() > endTime.getTime()) {
                            this.getView().showErrorNotification("营业执照已过期");
                            this.getView().hideLoading(false);
                            return;
                        }
                    }

                    Object rowNum = this.getModel().getValue("ukwo_row_num");
                    LOGGER.info("OCR识别完毕-rowNum：{}", rowNum);
                    if (rowNum != null && StringUtils.isNotBlank((String) rowNum)) {
                        // 修改，信息做匹配

                        if (!enterpriseName.equals(this.getModel().getValue("ukwo_company_name_ent"))
                                || !socialCode.equals(this.getModel().getValue("ukwo_u_soc_cre_code_ent"))

                        ) {
                            this.getView().showErrorNotification("信息不符，请重新上传");
                            this.getView().hideLoading(false);
                            return;
                        }
                        this.getModel().setValue("ukwo_busi_lic_address", data.getString("Address"));
                    } else {
                        // 新增
                        this.getModel().setValue("ukwo_company_name_ent", enterpriseName);
                        this.getModel().setValue("ukwo_u_soc_cre_code_ent", socialCode);
                        this.getModel().setValue("ukwo_busi_lic_address", data.getString("Address"));
                    }
                    this.getPageCache().put("OCR-company", "true");
                } else {
                    LOGGER.error("OCR识别失败：{}", msg);
                    this.getView().showTipNotification(msg);
                }
            } catch (Exception e1) {
                LOGGER.error("OCR识别失败：{}", e1.getMessage());
                this.getView().showErrorNotification("OCR识别失败，请重新上传");
            }
        }
        this.getView().hideLoading(false);
    }

    @Override
    public void afterRemove(UploadEvent evt) {
        AttachmentPanel attachmentPanel = (AttachmentPanel) evt.getSource();
        String key = attachmentPanel.getKey();  //当前控件标识
        this.getView().setVisible(true, key + "ap");
        UploadListener.super.afterRemove(evt);
        // 删除文件后清空回显字段
        /* this.getModel().setValue("ukwo_company_name_ent", null);
        this.getModel().setValue("ukwo_u_soc_cre_code_ent", null);
        this.getModel().setValue("ukwo_busi_lic_address", null); */
    }

}
