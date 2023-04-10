package com.cxjt.xxd.plugin.wf.loan.order;

import com.cxjt.xxd.config.XXDConfig;
import com.cxjt.xxd.dao.XXDLoanApplyBillDao;
import com.cxjt.xxd.dao.XXDOrgMappingDao;
import com.cxjt.xxd.dao.XXDWorkflowRoleDao;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.tree.TreeNode;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.org.model.OrgTreeParam;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.util.CollectionUtils;
import kd.bos.workflow.api.AgentExecution;
import kd.bos.workflow.engine.extitf.WorkflowPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 鑫湘贷[贷款申请单尽调流程]流程插件,生成[财科担保]负责人待办任务
 * 针对申请订单为“津市”的，财科担保负责人审核员为(掲耀:津鑫公司负责人),其他申请订单都为（李敏：财科担保负责人）;
 * 财科担保负责人审核环节支持多个用户审核，支持可配置；
 */
public class XXDLoanAppCkdbMgrParticipator extends WorkflowPlugin {

    private final Log logger = LogFactory.getLog(this.getClass());

    //鑫湘贷财科担保(津鑫公司)审核角色
    private static final String XXD_CKDB_SH_JXGS_ROLE = "XXD_CKDB_SH_JXGS_ROLE";

    //鑫湘贷财科担保审核角色
    private static final String XXD_CKDB_SH_ROLE = "XXD_CKDB_SH_ROLE";

    //JRSC侧津市市组织机构编码
    private static final String UKWO_JRCS_JS_ORG_CODE = "430781";

    /**
     * 返回[财科担保审批]参与人Id列表
     *
     * @param execution
     * @return
     */
    @Override
    public List<Long> calcUserIds(AgentExecution execution) {
        //单据业务ID
        String businessKey = execution.getBusinessKey();
        //获取当前审批的单据标识
        String entityNumber = execution.getEntityNumber();
        String taskName = execution.getCurrentTask().getName().getLocaleValue();

        DynamicObject loanApplicationBill = XXDLoanApplyBillDao.loadSingle(execution);
        String ukwoApplyId = (String)loanApplicationBill.get("ukwo_apply_id");

        logger.info("开始执行获取[业务部负责人审核]参与人插件,taskName={},ukwoApplyId={}",taskName,ukwoApplyId);
        // String ukwoJrcsOrgCode = (String) loanApplicationBill.get("ukwo_jrcs_org_code");

        //获取[完成尽调项目经理]的主职部门ID
        long proManagerMainOrgId = getProManagerMainOrgId(loanApplicationBill);


        //boolean condition = UKWO_JRCS_JS_ORG_CODE.equals(ukwoJrcsOrgCode.trim());
        boolean condition = isFromJs(proManagerMainOrgId);

        List<Long> userIds = null;
        if (condition) {
            //来源于津市市的贷款申请单
            userIds = XXDWorkflowRoleDao.getRoleUserIds(XXD_CKDB_SH_JXGS_ROLE);
            logger.info("来源于津市市的贷款申请单[{}]",ukwoApplyId);
        } else {
            //其他办事处的贷款申请单
            userIds = XXDWorkflowRoleDao.getRoleUserIds(XXD_CKDB_SH_ROLE);
        }

        return userIds;
    }

    /**
     * 获取贷款申请单[完成尽调项目经理]主职部门ID
     *
     * @param loanApplicationBill
     * @return 若从贷款申请单中查询不到[项目经理]则返回-1
     */
    private long getProManagerMainOrgId(DynamicObject loanApplicationBill) {
        long proManagerMainOrgId = -1L;
        //获取贷款申请单完成尽调项目经理
        DynamicObject custManager = (DynamicObject) loanApplicationBill.get("ukwo_project_manager");
        if (custManager != null) {
            long masterid = (long) custManager.get("masterid");
            //获取项目经理主职部门ID
            proManagerMainOrgId = UserServiceHelper.getUserMainOrgId(masterid);
        }

        return proManagerMainOrgId;
    }

    /**
     * 判断贷款申请单是否来自于津市
     *
     * @param proManagerMainOrgId [完成尽调项目经理]主职部门ID
     * @return true:是,false:否
     */
    private boolean isFromJs(long proManagerMainOrgId) {
        boolean condition = false;

        //津鑫公司组织机构ID
        long jxgsOrgId = XXDOrgMappingDao.getUkwoBosOrgId(Long.valueOf(UKWO_JRCS_JS_ORG_CODE));

        if(proManagerMainOrgId == jxgsOrgId){
            return true;
        }

        OrgTreeParam param = new OrgTreeParam();
        param.setId(jxgsOrgId);
        List<TreeNode> orgList = OrgUnitServiceHelper.getTreeChildren(param);

        for (TreeNode orgItem : orgList) {
            String id = orgItem.getId();
            if (id.equals(String.valueOf(proManagerMainOrgId))) {
                condition = true;
                break;
            }
        }

        return condition;
    }

    @Deprecated
    private List<Long> getCkdbManagers() {
        long ckdbOrgId = XXDConfig.getCkdbOrgId();
        List<Long> managerIds = UserServiceHelper.getManagersOfOrg(ckdbOrgId);

        if (CollectionUtils.isEmpty(managerIds)) {
            logger.error("未查询到财科担保负责人,请联系系统管理员");
            throw new KDBizException("未查询到财科担保负责人,请联系系统管理员");
        }

        return managerIds;
    }
}

