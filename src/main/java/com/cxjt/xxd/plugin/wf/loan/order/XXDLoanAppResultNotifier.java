package com.cxjt.xxd.plugin.wf.loan.order;

import com.cxjt.xxd.component.LoanApplicationComponent;
import com.cxjt.xxd.component.LoanApplyAttComponent;
import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.dao.XXDLoanApplyBillDao;
import com.cxjt.xxd.enums.BillResNotityStatusEnum;
import com.cxjt.xxd.enums.XXDErrorCodeEnum;
import com.cxjt.xxd.plugin.pc.list.XXDLoanApplicationBillListPlugin;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.workflow.api.AgentExecution;
import kd.bos.workflow.engine.extitf.IWorkflowPlugin;


/**
 * 贷款申请尽调结果通知,将尽调审批结果同步给金融超市
 */
public class XXDLoanAppResultNotifier implements IWorkflowPlugin {

    private final Log logger = LogFactory.getLog(this.getClass());


    /**
     * 在流程实例终止时(terminate)或流程实例完成时执行(end)
     *
     * @param execution
     */
    @Override
    public void notify(AgentExecution execution) {

        //获取执行时机
        String eventName = execution.getEventName();

        //业务编号
        String ukwoApplyId = "";
        //单据业务ID
        long businessKey = Long.valueOf(execution.getBusinessKey());

        logger.info("开始执行贷款尽调通知,单据业务ID={}......",businessKey);

        DynamicObject loanApplicationBill = null;

        try {
            //获取当前审批的单据标识
            //String entityNumber = execution.getEntityNumber();
            //QFilter[] filters = new QFilter[]{new QFilter("id", QCP.equals, businessKey)};
           //loanApplicationBill = BusinessDataServiceHelper.loadSingle(entityNumber, XXDLoanApplicationBillListPlugin.SELECT_PROPERTIES, filters);
            loanApplicationBill = XXDLoanApplyBillDao.queryByPK(businessKey,XXDLoanApplicationBillListPlugin.SELECT_PROPERTIES);
            ukwoApplyId = (String) loanApplicationBill.get("ukwo_apply_id");
        } catch (Exception e) {
            logger.error("将审批结果同步给金融超市[查询申请单]出现异常,单据业务ID={},ukwoApplyId={},eventName={},异常信息={}", businessKey,ukwoApplyId, eventName, e);
            throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.FAILED.getCode(), "将审批结果同步给金融超市[查询申请单]出现异常,请联系系统管理员"));
        }


        boolean condition1 = FormConstant.WORKFLOW_INSTANCE_TERMINATE.equals(eventName);
        boolean condition2 = FormConstant.WORKFLOW_INSTANCE_END.equals(eventName);
        boolean condition = condition1 || condition2;


        if (!condition) {
            logger.warn("在流程实例终止时或流程实例完成时才能执行尽调结果通知逻辑,ukwoApplyId={},eventName={},单据业务ID={}", ukwoApplyId, eventName,businessKey);
            return;
        }

        try {
            logger.info("开始执行将审批结果同步给金融超市逻辑,业务编号={},单据业务ID={},eventName={}", ukwoApplyId, businessKey, eventName);
            LoanApplicationComponent.auditResultNotify(loanApplicationBill);
        } catch (Exception e) {
            logger.error("将审批结果同步给金融超市出现异常,ukwoApplyId={},eventName={},异常信息={}", ukwoApplyId, eventName, e);
            throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.FAILED.getCode(), "将审批结果同步给金融超市出现异常,请联系系统管理员"));
        }

    }

}
