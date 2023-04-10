package com.cxjt.xxd.component;

import com.cxjt.xxd.config.RemoteConfig;
import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.enums.GuarantRelationEnum;
import com.cxjt.xxd.enums.GuarantSignerTypeEnum;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.util.JsonUtil;
import kd.bos.servicehelper.workflow.WorkflowServiceHelper;
import kd.bos.workflow.component.approvalrecord.IApprovalRecordGroup;
import kd.bos.workflow.component.approvalrecord.IApprovalRecordItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuaFeeConfirmComponent {

    private static final Log logger = LogFactory.getLog(GuaFeeConfirmComponent.class);

    private static final String JRCS_PROTOCOL = RemoteConfig.getJrcsProtocol();
    private static final String HOST = RemoteConfig.getJrcsHost();
    private static final int PORT = RemoteConfig.getJrcsPort();
    private static final String PATH = RemoteConfig.getJrcsPaymentNoticePath();
    private static final String URL = JRCS_PROTOCOL + "://" + HOST + ":" + PORT + PATH;


    /**
     * 请求[金融超市][担保费确认结果通知接口]
     *
     * @param applyId      业务编号
     * @param recordId     汇款记录编号
     * @param decisionType approve为同意,reject为驳回,terminate为不同意并终止
     * @param auditDesc    审核意见
     * @param auditTime    审核时间
     * @return
     */
    public static String execute(String applyId, String recordId, String decisionType, String auditDesc, String auditTime) throws Exception {
        Map<String, Object> paraMap = new HashMap<>();
        Map<String, String> data = new HashMap<>();

        //审核结果 1未确认 2通过 3未通过(JRCS侧本接口枚举值)
        String creditValidity = "";

        if (FormConstant.APPROVE.equals(decisionType)) {
            creditValidity = "2";
        } else {
            creditValidity = "3";
        }

        data.put("applyId", applyId);
        data.put("recordId", recordId);
        data.put("creditValidity", creditValidity);
        data.put("auditDesc", auditDesc);
        data.put("updateTime", auditTime);
        paraMap.put("body", data);

        String jsonData = JsonUtil.format(paraMap);
        logger.info("开始请求JRCS担保费确认结果通知接口,参数如下:applyId={},jsonData={}", applyId, jsonData);
        String responseStr = HttpService.getService().doPostByHttpClient(URL, jsonData);
        logger.info("JRCS担保费确认结果通知接口响应信息:applyId={},responseStr={}", applyId, responseStr);
        return responseStr;
    }

    public static String call(String applyId, String recordId, IApprovalRecordItem approvalRecordItem) throws Exception {
        String decisionType = approvalRecordItem.getDecisionType();
        String approvalResult = approvalRecordItem.getResult();
        String message = approvalRecordItem.getMessage();
        //审核时间,格式yyyy-MM-dd HH:mm:ss
        String auditTime = approvalRecordItem.getTime();
        String activityId = approvalRecordItem.getActivityId();
        String auditDesc = message;

        String result = execute(applyId, recordId, decisionType, auditDesc, auditTime);

        return result;
    }

    /**
     * 是否为申请者
     *
     * @param ukwoGuaSignerType 签订类型
     * @param ukwoRelationType  关系
     * @return
     */
    public static boolean isApplicant(String ukwoGuaSignerType, String ukwoRelationType) {
        boolean condition = GuarantSignerTypeEnum.ENTERPRISE.getCode().equals(ukwoGuaSignerType) && GuarantRelationEnum.A.getCode().equals(ukwoRelationType);
        return condition;
    }

}
