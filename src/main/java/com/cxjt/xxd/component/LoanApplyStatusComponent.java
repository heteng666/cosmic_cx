package com.cxjt.xxd.component;

import com.alibaba.fastjson.JSONObject;
import com.cxjt.xxd.config.RemoteConfig;
import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.dao.XXDLoanApplyBillDao;
import com.cxjt.xxd.enums.*;
import com.cxjt.xxd.helper.XXDLoanApplicationHelper;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.util.JsonUtil;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.workflow.component.approvalrecord.IApprovalRecordItem;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LoanApplyStatusComponent {

    private static final Log logger = LogFactory.getLog(LoanApplyStatusComponent.class);

    public static final String SELECT_PROPERTIES = "id,billno,ukwo_apply_id,ukwo_project_name,ukwo_order_status,ukwo_order_status_code,ukwo_industry_sub_desc,ukwo_loan_date,ukwo_expire_date,ukwo_loan_rate,ukwo_gua_contract_no,ukwo_entrust_contract_no,ukwo_loan_contract_no,modifytime,entryentity,entryentity.ukwo_gua_signer_type,entryentity.ukwo_relation_type,entryentity.ukwo_gua_attachment";//,entryentity.ukwo_gua_attachment.fbasedataid.name

    private static final String JRCS_PROTOCOL = RemoteConfig.getJrcsProtocol();
    private static final String HOST = RemoteConfig.getJrcsHost();
    private static final int PORT = RemoteConfig.getJrcsPort();
    private static final String PATH = RemoteConfig.getJrcsGetLoanProcessPath();
    private static final String URL = JRCS_PROTOCOL + "://" + HOST + ":" + PORT + PATH;

    /**
     * 根据主键获取贷款申请单
     *
     * @param orderPkId 贷款申请单主键
     * @return
     */
    public static DynamicObject queryOrderByPK(long orderPkId) {
        DynamicObject orderBill = XXDLoanApplyBillDao.queryByPK(orderPkId, SELECT_PROPERTIES);
        return orderBill;
    }

    /**
     * 请求金融超市获取订单状态
     *
     * @param applyId 业务编号
     * @return
     */
    public static String call(String applyId) {
        Map<String, Object> paraMap = new HashMap<>();
        Map<String, String> data = new HashMap<>();
        data.put("applyId", applyId);
        paraMap.put("body", data);

        String jsonData = JsonUtil.format(paraMap);

        String responseStr = "";

        try {
            logger.info("====================开始请求获取订单状态接口,applyId={},请求地址={}", applyId, URL);
            responseStr = HttpService.getService().doPostByHttpClient(URL, jsonData);
            logger.info("====================获取订单状态接口返回结果,applyId={},data={}", applyId, responseStr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return responseStr;
    }

    /**
     * 处理获取订单状态逻辑
     *
     * @param orderBill
     * @return
     */
    public static boolean process(DynamicObject orderBill) {
        String applyId = (String) orderBill.get("ukwo_apply_id");
        String responseStr = LoanApplyStatusComponent.call(applyId);

        JSONObject jsonResult = JSONObject.parseObject(responseStr);
        String code = (String) jsonResult.get("code");
        String msg = (String) jsonResult.get("msg");

        //成功时才更新
        boolean successCondition = JrcsErrorCodeEnum.SUCCESS.getCode().equals(code);
        if (!successCondition) {
            //logger.error("[获取订单状态]JRCS失败返回,信息如下:applyId={},code={},msg={}", applyId, code, msg);
            return false;
        }


        String body = (String) jsonResult.get("body");
        JSONObject bodyJson = JSONObject.parseObject(body);
        Integer applyStatus = (Integer) bodyJson.get("applyStatus");
        String applyStatusDesc = (String) bodyJson.get("applyStatusDesc");
        String resApplyId = (String) bodyJson.get("applyId");
        boolean applyIdCondition = applyId.equals(resApplyId);

        if (!applyIdCondition) {
            logger.error("[获取订单状态接口],请求业务编号与响应业务编号不一致,reqApplyId ={},resApplyId={}", applyId, resApplyId);
            throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.FAILED.getCode(), "[获取订单状态接口],请求业务编号与响应业务编号不一致"));
        }
        // 2023-03-30 JRCS获取订单状态接口-新增7个返回属性【行业小类描述】【借款日期】【到期日期】【实际借款利率】【保证合同号】【委保合同号】【借款合同编号】
        //考虑到历史数据以及数据的及时性,已与产品达成一致,获取订单状态时,终态不查,订单状态没有变化查
        /*
        Object ukwoOrderStatusCodeObj = orderBill.get("ukwo_order_status_code");

        if (ukwoOrderStatusCodeObj != null) {
            Integer ukwoOrderStatusCode = (Integer) ukwoOrderStatusCodeObj;
            boolean statusCondition = ukwoOrderStatusCode.equals(applyStatus);
            if (statusCondition) {
                throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.FAILED.getCode(), "[获取订单状态接口],订单状态没有发现变化,无需更新"));
            }
        }*/

        if (bodyJson.get("payNotice") != null) {
            JSONObject payNoticeJson = (JSONObject) bodyJson.get("payNotice");
            Object fileUrlObj = payNoticeJson.get("fileUrl");
            Object fileNameObj = payNoticeJson.get("fileName");
            Object fileTypeObj = payNoticeJson.get("fileType");

            boolean condition = (fileNameObj != null) && (fileUrlObj != null) && (fileTypeObj != null);
            if (!condition) {
                logger.error("[获取订单状态接口]返回信息有误,文件名、文件URL或文件内容为空");
                throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.FAILED.getCode(), "[获取订单状态接口]返回信息有误,文件名、文件URL或文件类型为空"));
            }

            if (!AttachmentEnum.F.getCode().equals(fileTypeObj)) {
                throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.FAILED.getCode(), "[获取订单状态接口]返回信息有误,文件类型不正确"));

            }

            //String entryPkId = "";
            DynamicObjectCollection entry = orderBill.getDynamicObjectCollection("entryentity");
            //循环遍历单据体
            for (int i = 0; i < entry.size(); i++) {
                DynamicObject row = entry.get(i);
                String ukwoGuaSignerType = String.valueOf(row.get("ukwo_gua_signer_type"));
                String ukwoRelationType = String.valueOf(row.get("ukwo_relation_type"));
                if (GuaFeeConfirmComponent.isApplicant(ukwoGuaSignerType, ukwoRelationType)) {
                    String fileUrl = String.valueOf(fileUrlObj);
                    String fileName = String.valueOf(fileNameObj);
                    int index = fileUrl.lastIndexOf(".");
                    //扩展名
                    String type = fileUrl.substring(index);
                    //文件名去掉前后的空格
                    fileName = fileName.trim();
                    fileName = fileName + type;

                    boolean containsFlag = AttachmentFieldServiceComponent.contain(row, fileName);
                    //查询企业法人的《担保及放款通知书>是否已经同步过,若未同步过,则绑定
                    if (!containsFlag) {
                        //同步<<担保及放款通知书>>到对应的申请者(即:企业法人)
                        long attachmentId = AttachmentFieldServiceComponent.buildAttachmentDataFromEdit(fileUrl, fileName);
                        AttachmentFieldServiceComponent.bind(attachmentId, row, FormConstant.LOAN_APPLY_ORDER_ENTITY_ATTACHMENT_FIELD_NAME);
                    }


                }
            }

        }

        //行业小类描述
        String industryDesc = (String) bodyJson.get("industryDesc");
        //借款日期,格式:格式 yyyy-MM-dd
        String loanDate = (String) bodyJson.get("loanDate");
        //到期日期,格式:格式 yyyy-MM-dd
        String expireDate = (String) bodyJson.get("expireDate");
        //实际借款利率
        String loanRate = (String) bodyJson.get("loanRate");
        //保证合同号
        String guaranteeContractNo = (String) bodyJson.get("guaranteeContractNo");
        //委保合同号
        String entrustContractNo = (String) bodyJson.get("entrustContractNo");
        //借款合同编号
        String loanContractNo = (String) bodyJson.get("loanContractNo");

        //将合同信息入库
        orderBill.set("ukwo_industry_sub_desc", industryDesc == null ? "-" : industryDesc);
        orderBill.set("ukwo_loan_date", loanDate == null ? "-" : loanDate);
        orderBill.set("ukwo_expire_date", expireDate == null ? "-" : expireDate);
        orderBill.set("ukwo_loan_rate", loanRate == null ? "-" : loanRate);
        orderBill.set("ukwo_gua_contract_no", guaranteeContractNo == null ? "-" : guaranteeContractNo);
        orderBill.set("ukwo_entrust_contract_no", entrustContractNo == null ? "-" : entrustContractNo);
        orderBill.set("ukwo_loan_contract_no", loanContractNo == null ? "-" : loanContractNo);

        //页面直接显示订单状态描述
        orderBill.set("ukwo_order_status", applyStatusDesc);
        orderBill.set("ukwo_order_status_code", applyStatus);
        orderBill.set("modifytime", new Date());
        handleApplyStatus(orderBill,applyStatus);
        SaveServiceHelper.save(new DynamicObject[]{orderBill});

        boolean execResult = true;
        return execResult;
    }


    private static void handleApplyStatus(DynamicObject orderBill, Integer jrcsApplyStatus) {

        boolean condition220009 = ApplyStatusEnum.XXD_220009.getCode().equals(jrcsApplyStatus);
        boolean condition220010 = ApplyStatusEnum.XXD_220010.getCode().equals(jrcsApplyStatus);

        boolean condition = condition220009 || condition220010;

        if (!condition) {
            return;
        }

        String businessKey = String.valueOf(orderBill.get("id"));
        //String applyStatusDesc = (String) orderBill.get("ukwo_order_status");
        //需要对JRCS侧的【担保审核中】【担保审核拒绝】两种状态根据【单据流程状态】进行细化
        IApprovalRecordItem approvalRecordItem = WorkflowComponent.getApprovalRecordItem(businessKey);

        //获取决策项类型
        String decisionType = approvalRecordItem.getDecisionType();
        //获取工作流结点名称
        String activityName = approvalRecordItem.getActivityName().trim();

        //担保审核拒绝
        if (condition220010) {
            String description = activityName + "拒绝";
            Integer code = ApplyStatusEnum.getCodeByDesc(description);
            orderBill.set("ukwo_order_status", description);
            orderBill.set("ukwo_order_status_code", code);
        }

        //担保审核中
        if (condition220009) {
            //String currentActivityName = ApplyStatusHelper.get(activityName);
            String currentActivityName = activityName;
            String description = currentActivityName + "中";
            Integer code = ApplyStatusEnum.getCodeByDesc(description);

            orderBill.set("ukwo_order_status", description);
            orderBill.set("ukwo_order_status_code", code);
        }


    }

    static class ApplyStatusHelper {

        private static Map<String, String> wfNodeNameMap = new HashMap();

        static {
            wfNodeNameMap.put("贷款申请订单提交", "贷款申请订单提交");
            wfNodeNameMap.put("办事处负责人转派", "办事处负责人转派");
            wfNodeNameMap.put("项目经理受理", "项目经理受理");
            wfNodeNameMap.put("办事处负责人审核", "办事处负责人审核");
            wfNodeNameMap.put("风险部负责人审核", "风险部负责人审核");
            wfNodeNameMap.put("业务部负责人审核", "业务部负责人审核");
        }

        public static String get(String key) {
            String value = wfNodeNameMap.get(key);

            if (StringUtils.isBlank(value)) {
                logger.error("获取工作流结点信息失败,key={}",key);
                throw new RuntimeException("获取工作流结点信息失败");
            }

            return value;
        }

    }

}
