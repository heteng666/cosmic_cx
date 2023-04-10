package com.cxjt.xxd.plugin.wf.loan.order;

import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.dao.XXDOrgMappingDao;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.workflow.api.AgentExecution;
import kd.bos.workflow.engine.extitf.IWorkflowPlugin;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 鑫湘贷[贷款申请单尽调流程]流程[指定参与人]插件,生成[录入尽调结果][项目经理]待办任务
 */
public class XXDLoanAppPmParticipator implements IWorkflowPlugin {
    private final static Log logger = LogFactory.getLog(XXDLoanAppPmParticipator.class);

    /**
     * 返回[录入尽调结果]参与人Id列表
     *
     * @param execution
     * @return
     */
    @Override
    public List<Long> calcUserIds(AgentExecution execution) {
        List<Long> orgList = XXDOrgMappingDao.queryAllOrgIds();
        logger.info("[录入尽调结果][指定参与人][项目经理]插件,获取到组织结构如下:{}", orgList.toString());
        //TODO 获取机构下所有人员时,若该机构下人员在其他部们兼职,则人员会出现多次
        //jobType:1：只获取主职人员；2：只获取兼职部门人员
        Set<Long> userIds = UserServiceHelper.getAllUsersOfOrg(1, orgList, true, false);

        List<Long> userIdList = new ArrayList<>(userIds);

        return userIdList;

    }

    private List<Long> getAllUsersOfOrg(List<Long> orgList) {
        String orgStr = StringUtils.join(orgList, ",");
        String querySql = "SELECT DISTINCT A.FId id FROM t_SEC_User A INNER JOIN t_SEC_UserPosition B ON B.FId=A.FId AND B.fdptid in (" + orgStr + ")  AND B.fispartjob = '0' AND A.fenable = '1'";
        logger.info("查询[录入尽调结果][指定参与人]SQL============================{}", querySql);
        DataSet dataSet = DB.queryDataSet(XXDLoanAppPmParticipator.class.getName(), DBRoute.of(FormConstant.SYS_ROUTE_KEY), querySql);

        List<Long> userIds = new ArrayList<>();
        while (dataSet.hasNext()) {
            Row row = dataSet.next();
            long userId = (Long) row.get("id");
            userIds.add(userId);
        }

        return userIds;
    }

}
