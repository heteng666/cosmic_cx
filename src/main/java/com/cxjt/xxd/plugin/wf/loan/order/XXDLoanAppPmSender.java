package com.cxjt.xxd.plugin.wf.loan.order;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxjt.xxd.config.XXDConfig;
import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.enums.SendStatusEnum;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.workflow.api.AgentExecution;
import kd.bos.workflow.engine.extitf.IWorkflowPlugin;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.Map;

/**
 * 确认订单归属项目经理后发送短信,注册在【办事处负责人转派】节点,离开节点时执行
 */
public class XXDLoanAppPmSender implements IWorkflowPlugin {

    private final Log logger = LogFactory.getLog(this.getClass());

    @Override
    public void notify(AgentExecution execution) {

        //获取执行时机
        String eventName = execution.getEventName();
        String ukwoApplyId = "";

        //项目经理电话
        String phone = "";
        //项目经理姓名
        String name = "";
        //企业名称
        String companyName = "";
        //客户经理电话
        String custPhone = "";

        try {
            //单据业务ID
            String businessKey = execution.getBusinessKey();
            //获取当前审批的单据标识
            String entityNumber = execution.getEntityNumber();

            DynamicObject loanApplicationBill = BusinessDataServiceHelper.loadSingle(businessKey, entityNumber);


            //业务编号
            ukwoApplyId = (String) loanApplicationBill.get("ukwo_apply_id");
            //法人手机号
            String ukwoLegalPersonPhone = (String) loanApplicationBill.get("ukwo_legal_person_phone");
            //短信类型
            String ukwoSmsType = FormConstant.SEND_AUDIT_PROJECT_MRG_TYPE;
            //发送状态,默认为未发送
            String ukwo_send_status = SendStatusEnum.UNSENT.getCode();

            //获取指定的下一步参数人ID(办事处负责人转派的项目经理ID)
            String dynParticipantJsonStr = (String) execution.getCurrentTask().getVariable("dynParticipant");
            JSONObject dynParticipant = JSONObject.parseObject(dynParticipantJsonStr);
            JSONObject tt = (JSONObject) dynParticipant.get(FormConstant.LOAN_APPLY_ORDER_WORK_FLOW_IDENTIFY + "_AuditTask5");
            JSONArray array = (JSONArray) tt.get(FormConstant.LOAN_APPLY_ORDER_WORK_FLOW_IDENTIFY + "_AuditTask7");
            long nextAssigneeId = Long.valueOf(String.valueOf(array.get(0)));

            //将尽调项目经理姓名以及手机号,通过短信发送给法人
            //给申请者发送短信内容调整如下：【常德金融超市】尊敬的客户，您的【企业名称】申请的“鑫湘e贷”稍后会有项目经理（张三13812341234）联系您进行线下复核，请保持电话畅通。
            //【常德金融超市】尊敬的客户，您的【${companyName}】申请的“鑫湘e贷”稍后会有项目经理（${content}）联系您进行线下复核，请保持电话畅通。
            Map<String, Object> assigneeMap = UserServiceHelper.getUserInfoByID(nextAssigneeId);
            phone = (String) assigneeMap.get("phone");
            name = (String) assigneeMap.get("name");
            companyName = (String) loanApplicationBill.get("ukwo_company_name");
            custPhone = (String) loanApplicationBill.get("ukwo_cust_phone");

            boolean conditon = StringUtils.isBlank(name) || StringUtils.isBlank(phone) || StringUtils.isBlank(ukwoLegalPersonPhone);
            if(conditon){
                throw new RuntimeException("法人手机号、项目经理名称、项目经理电话均不能为空");
            }

            //短信模板
            String ukwoSmsContentTemplate = XXDConfig.getSmsOrderConfirmProMgrTempldate();
            String ukwoSmsContent = ukwoSmsContentTemplate.replace("${companyName}", companyName).replace("${content}", name + phone);

            DynamicObject sendSmsBill = BusinessDataServiceHelper.newDynamicObject(FormConstant.SEND_SMS_ENTITY_NAME);
            sendSmsBill.set("ukwo_apply_id", ukwoApplyId);
            //发送给申请人即企业法人
            sendSmsBill.set("ukwo_mobile_phone", ukwoLegalPersonPhone);
            sendSmsBill.set("ukwo_sms_type", ukwoSmsType);
            sendSmsBill.set("ukwo_sms_content", ukwoSmsContent);
            sendSmsBill.set("ukwo_send_status", ukwo_send_status);
            sendSmsBill.set("ukwo_create_time", new Date());

            SaveServiceHelper.save(new DynamicObject[]{sendSmsBill});

        } catch (Exception e) {
            //不抛出异常,短信发送失败,不能影响主流程
            logger.error("确认订单归属项目经理后,短信信息入库失败,业务编号:{},异常信息如下:{}", ukwoApplyId, e);
        }

        try {
            boolean conditon = StringUtils.isBlank(name) || StringUtils.isBlank(phone) || StringUtils.isBlank(custPhone);
            if(conditon){
                throw new RuntimeException("客户经理手机号、项目经理名称、项目经理电话均不能为空");
            }
            DynamicObject custSmsBill = buildCustMgrSms(ukwoApplyId,companyName,name,phone,custPhone);
            SaveServiceHelper.save(new DynamicObject[]{custSmsBill});
        } catch (Exception e) {
            logger.error("当转派并确认项目经理后,给客户经理的手机发送短信,短信信息入库失败,业务编号:{},异常信息如下:{}", ukwoApplyId, e);
        }

    }


    DynamicObject buildCustMgrSms(String applyId, String companyName, String proMgrName, String proMgrPhone, String custMgrPhone) {
        //当转派并确认项目经理后，给客户经理的手机发送短信，短信内容为【常德金融超市】【企业名称】申请鑫湘e贷产品已通过预授信，担保方项目经理为张三（电话13812345678）。
        // (说明：企业名称读取该笔订单的企业名称，项目经理中的姓名及电话读取项目经理的信息)

        DynamicObject custSmsBill = BusinessDataServiceHelper.newDynamicObject(FormConstant.SEND_SMS_ENTITY_NAME);

        //【常德金融超市】【${companyName}】申请鑫湘e贷产品已通过预授信，担保方项目经理为${proName}（电话${proPhone}）
        String smsCustContentTemplate = XXDConfig.getSmsConfirmProCustMgrTempldate();
        String ukwoSmsContent = smsCustContentTemplate.replace("${companyName}", companyName).replace("${proName}", proMgrName).replace("${proPhone}", proMgrPhone);

        custSmsBill.set("ukwo_apply_id", applyId);
        custSmsBill.set("ukwo_mobile_phone", custMgrPhone);
        custSmsBill.set("ukwo_sms_type", FormConstant.SEND_AUDIT_CUST_MRG_TYPE);
        custSmsBill.set("ukwo_sms_content", ukwoSmsContent);
        custSmsBill.set("ukwo_send_status", SendStatusEnum.UNSENT.getCode());
        custSmsBill.set("ukwo_create_time", new Date());

        return custSmsBill;
    }

}
