package com.cxjt.xxd.plugin.wf.loan.order;

import com.cxjt.xxd.config.XXDConfig;
import com.cxjt.xxd.dao.XXDWorkflowRoleDao;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.util.CollectionUtils;
import kd.bos.workflow.api.AgentExecution;
import kd.bos.workflow.engine.extitf.WorkflowPlugin;

import java.util.List;

/**
 * 鑫湘贷[贷款申请单尽调流程]流程插件,生成风险部审核参与人待办任务
 */
public class XXDLoanAppRiskDeptMgrParticipator extends WorkflowPlugin {

    private final Log logger = LogFactory.getLog(this.getClass());

    //鑫湘贷风险部审核角色
    private final String XXD_RISK_SH_ROLE = "XXD_RISK_SH_ROLE";

    /**
     * 返回[风险部审批]参与人Id列表
     *
     * @param execution
     * @return
     */
    @Override
    public List<Long> calcUserIds(AgentExecution execution) {

        List<Long> userIds = XXDWorkflowRoleDao.getRoleUserIds(XXD_RISK_SH_ROLE);

        if (org.apache.commons.collections.CollectionUtils.isEmpty(userIds)) {
            throw new RuntimeException("暂未获取到风险部审核参与人");
        }

        return userIds;

    }


    @Deprecated
    private List<Long> getRiskManagers() {
        long riskDeptId = XXDConfig.getRiskDeptId();

        //获取该部门负责人
        List<Long> managersOfOrg = UserServiceHelper.getManagersOfOrg(riskDeptId);

        if (CollectionUtils.isEmpty(managersOfOrg)) {
            logger.error("未查询到风险部负责人,请联系系统管理员");
            throw new KDBizException("未查询到风险部负责人,请联系系统管理员");
        }

        return managersOfOrg;
    }

}
