package com.cxjt.xxd.plugin.wf.loan.order;

import com.cxjt.xxd.dao.XXDLoanApplyBillDao;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.workflow.api.AgentExecution;
import kd.bos.workflow.engine.extitf.WorkflowPlugin;

import java.util.ArrayList;
import java.util.List;


/**
 * 返回[办事处审核]参与人Id列表
 */
public class XXDLoanAppBscShParticipator extends WorkflowPlugin {

    private final Log logger = LogFactory.getLog(this.getClass());

    /**
     * 取完成尽调项目经理所在办事处的负责人
     *
     * @param execution
     * @return
     */
    @Override
    public List<Long> calcUserIds(AgentExecution execution) {
        //查询贷款申请单
        DynamicObject loanApplicationBill = XXDLoanApplyBillDao.loadSingle(execution);

        String ukwoApplyId = (String)loanApplicationBill.get("ukwo_apply_id");
        String taskName = execution.getCurrentTask().getName().getLocaleValue();

        logger.info("开始执行获取[办事处负责人审核]参与人插件,taskName={},ukwoApplyId={}",taskName,ukwoApplyId);

        List<Long> userIds = new ArrayList<>();

        //获取贷款申请单完成尽调项目经理
        DynamicObject custManager = (DynamicObject) loanApplicationBill.get("ukwo_project_manager");

        //TODO 此插件绑在【办事处负责人审核结点】,但在进入【录入尽调结果】和进入【办事处负责人审核结点】是都会进入,因此要判空,离开【录入尽调结果】结点后，才写入项目经理数据
        //与何老师确认
        //在做流程参与人预测时,也会进入,所以也要判空
        if(custManager != null){
            long masterid = (long) custManager.get("masterid");

            //获取项目经理主职部门ID
            long userMainOrgId = UserServiceHelper.getUserMainOrgId(masterid);

            //获取项目经理主职部门负责人ID
            userIds = UserServiceHelper.getManagersOfOrg(userMainOrgId);
        }

        return userIds;
    }
}
