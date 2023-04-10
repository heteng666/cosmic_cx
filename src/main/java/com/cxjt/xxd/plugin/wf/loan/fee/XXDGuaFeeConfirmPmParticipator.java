package com.cxjt.xxd.plugin.wf.loan.fee;

import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.dao.XXDGuaConfirmBillDao;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.workflow.api.AgentExecution;
import kd.bos.workflow.engine.extitf.WorkflowPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * 鑫湘贷担保费确认流程插件,提交担保费确认接口数据后，生成[完成贷款申请单尽调流程]的项目经理的待办任务
 */
public class XXDGuaFeeConfirmPmParticipator extends WorkflowPlugin {

    private final Log logger = LogFactory.getLog(this.getClass());

    /**
     * 返回[项目经理确认]参与人Id列表
     *
     * @param execution
     * @return
     */
    @Override
    public List<Long> calcUserIds(AgentExecution execution) {
        String businessKey = execution.getBusinessKey();
        //获取当前审批的单据标识
        String entityNumber = execution.getEntityNumber();//ukwo_xxd_gua_confirm_bill
        //DynamicObject guaConfirmBill = BusinessDataServiceHelper.loadSingle(businessKey, entityNumber, "Id,billno,ukwo_apply_id,ukwo_company_name,ukwo_guarant_amount,ukwo_guarantee_fee_amount,ukwo_region_code");
        DynamicObject guaConfirmBill = XXDGuaConfirmBillDao.loadSingle(execution);
        String ukwoApplyId = (String) guaConfirmBill.get("ukwo_apply_id");

        //业务编号ukwo_apply_id
        QFilter[] filters = new QFilter[]{new QFilter("ukwo_apply_id", QCP.equals, ukwoApplyId)};

        //根据业务编号,从贷款申请订单中获取完成尽调审批的项目经理
        String entityName = FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME;
        DynamicObject loanApplyBill = BusinessDataServiceHelper.loadSingle(entityName, "ukwo_project_name,ukwo_project_manager", filters);

        if (loanApplyBill == null) {
            logger.error("贷款申请订单[{}]不存在,请联系系统管理员", ukwoApplyId);
            throw new KDBizException("贷款申请订单不存在,请联系系统管理员");

        }
        DynamicObject custManager = (DynamicObject) loanApplyBill.get("ukwo_project_manager");
        long masterid = (long) custManager.get("masterid");

        //生成完成尽调审批的项目经理待办任务
        List<Long> pmList = new ArrayList<>();


        pmList.add(masterid);

        return pmList;
    }
}
