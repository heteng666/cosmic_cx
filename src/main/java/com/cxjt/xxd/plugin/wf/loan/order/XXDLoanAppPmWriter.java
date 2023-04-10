package com.cxjt.xxd.plugin.wf.loan.order;

import com.cxjt.xxd.constants.FormConstant;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;
import kd.bos.servicehelper.org.OrgViewType;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.workflow.api.AgentExecution;
import kd.bos.workflow.api.AgentTask;
import kd.bos.workflow.engine.extitf.IWorkflowPlugin;
import org.apache.commons.lang.time.DateFormatUtils;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * 贷款申请订单尽调,离开【录入尽调结果】节点时,将处理该节点的项目经理写入[贷款尽调申请单]
 */
public class XXDLoanAppPmWriter implements IWorkflowPlugin {

    private final Log logger = LogFactory.getLog(this.getClass());


    /**
     * 在离开【录入尽调结果】节点时绑定工作流插件
     *
     * @param execution
     */
    @Override
    public void notify(AgentExecution execution) {
        logger.info("开始执行离开【录入尽调结果】节点时,将处理该节点的项目经理写入[贷款尽调申请单]逻辑");

        //获取执行时机
        String eventName = execution.getEventName();
        //离开结点时才执行
        if (!"end@end".equals(eventName)) {
            return;
        }

        //获取当前工作流节点
        AgentTask currentTask = execution.getCurrentTask();
        //获取当前节点实际审批人ID
        Long assigneeId = currentTask.getAssigneeId();


        //单据业务ID
        String businessKey = execution.getBusinessKey();
        //获取当前审批的单据标识
        String entityNumber = execution.getEntityNumber();

        DynamicObject loanApplicationBill = BusinessDataServiceHelper.loadSingle(businessKey, entityNumber);

        //项目经理
        loanApplicationBill.set("ukwo_project_manager", assigneeId);

        //经办人,读取项目经理名称(从审批流程信息获取)
        loanApplicationBill.set("ukwo_operator", assigneeId);

        //经办时间,读取项目经理录入尽调结果时间
        loanApplicationBill.set("ukwo_handling_time", DateFormatUtils.format(new Date(), FormConstant.DATETIME_FORMAT));

        //经办人部门,显示项目经理归属上级架构名称(比如“武陵区办事处”),
        long mainOrgId = UserServiceHelper.getUserMainOrgId(assigneeId);
        loanApplicationBill.set("ukwo_handling_dept", mainOrgId);

        //经办人公司,显示项目经理所属公司名称
        Map<Long,Long> orglist =  OrgUnitServiceHelper.getDirectSuperiorOrg(OrgViewType.Admin, Collections.singletonList(mainOrgId));
        long companyOrgId = orglist.get(mainOrgId);
        loanApplicationBill.set("ukwo_handling_company", companyOrgId);

        //业务经理,显示项目经理名称
        loanApplicationBill.set("ukwo_business_mgr", assigneeId);

        //风险部经理,显示空
       // loanApplicationBill.set("ukwo_risk_mgr", "");

        //更新数据库
        SaveServiceHelper.update(new DynamicObject[]{loanApplicationBill});
    }

}
