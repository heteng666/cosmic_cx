package com.cxjt.xxd.plugin.wf.loan.fee;

import com.alibaba.fastjson.JSONObject;
import com.cxjt.xxd.component.GuaFeeConfirmComponent;

import com.cxjt.xxd.component.WorkflowComponent;
import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.dao.XXDGuaConfirmBillDao;
import com.cxjt.xxd.enums.BillResNotityStatusEnum;
import com.cxjt.xxd.enums.JrcsErrorCodeEnum;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.workflow.WorkflowServiceHelper;
import kd.bos.workflow.api.AgentExecution;
import kd.bos.workflow.component.approvalrecord.IApprovalRecordItem;
import kd.bos.workflow.engine.extitf.IWorkflowPlugin;

import java.util.Date;

/**
 * 担保费确认结果通知,在项目经理走完担保费流程后,将审批结果同步给金融超市
 */
public class XXDLoanGuaFeeConfirmNotifier implements IWorkflowPlugin {

    private final Log logger = LogFactory.getLog(this.getClass());

    /**
     * 在流程实例终止时(terminate)或流程实例完成时执行(end),通过agentExecution获取审批意见[注意:此时流程暂未结束]
     *
     *
     * @param execution
     */
    @Override
    public void notify(AgentExecution execution) {
        //获取执行时机
        String eventName = execution.getEventName();

        boolean condition1 = FormConstant.WORKFLOW_INSTANCE_TERMINATE.equals(eventName);
        boolean condition2 = FormConstant.WORKFLOW_INSTANCE_END.equals(eventName);
        boolean condition = condition1 || condition2;
        if (!condition) {
            logger.warn("在流程实例终止时或流程实例完成时才能担保费确认结果通知逻辑,eventName={},businessKey={}",eventName,execution.getBusinessKey());
            return;
        }


        //单据业务ID
        String businessKey = execution.getBusinessKey();
        //获取当前审批的单据标识
        String entityNumber = execution.getEntityNumber();

        //DynamicObject guaConfirmBill = BusinessDataServiceHelper.loadSingle(businessKey, entityNumber);
        DynamicObject guaConfirmBill = XXDGuaConfirmBillDao.loadSingle(execution);
        //业务编号
        String ukwoApplyId = (String) guaConfirmBill.get("ukwo_apply_id");
        //汇款记录编号
        String ukwoRecordId = (String) guaConfirmBill.get("ukwo_record_id");

        logger.info("开始执行将保费确认结果同步给金融超市逻辑,单据业务ID={},ukwoApplyId={},ukwoRecordId={}", businessKey,ukwoApplyId,ukwoRecordId);

        try {
            IApprovalRecordItem approvalRecordItem = WorkflowComponent.getApprovalRecordItem(businessKey);
            //将审批结果同步给金融超市
            String responseStr = GuaFeeConfirmComponent.call(ukwoApplyId, ukwoRecordId, approvalRecordItem);

            JSONObject jsonResult = JSONObject.parseObject(responseStr);
            String code = (String) jsonResult.get("code");
            String msg = (String) jsonResult.get("msg");

            guaConfirmBill.set("ukwo_pay_notice_code", code);
            guaConfirmBill.set("ukwo_pay_notice_msg", msg);
            guaConfirmBill.set("modifytime",new Date());

            if (JrcsErrorCodeEnum.SUCCESS.getCode().equals(code)) {
                guaConfirmBill.set("ukwo_pay_notity_status", BillResNotityStatusEnum.SUCCESS.getCode());
            } else {
                guaConfirmBill.set("ukwo_pay_notity_status", BillResNotityStatusEnum.FAILED.getCode());
            }

            //更新担保费确认申请单通知状态
            //UPDATE tk_ukwo_xxd_gua_con_bill SET fk_ukwo_pay_notity_status=?,fk_ukwo_pay_notice_code=?,fk_ukwo_pay_notice_msg=?,fmodifytime=?,fmodifierid=? WHERE FId=?
            SaveServiceHelper.update(new DynamicObject[]{guaConfirmBill});
        } catch (Exception e) {
            //更新担保费确认申请单,通知状态为失败,然后重试
            logger.error("担保费确认结果通知异常,信息如下:{},{},异常信息如下:{}", ukwoApplyId, ukwoRecordId, e);
            //更新担保费确认申请单通知状态
            guaConfirmBill.set("ukwo_pay_notity_status", BillResNotityStatusEnum.FAILED.getCode());
            SaveServiceHelper.update(new DynamicObject[]{guaConfirmBill});
        }

    }
}
