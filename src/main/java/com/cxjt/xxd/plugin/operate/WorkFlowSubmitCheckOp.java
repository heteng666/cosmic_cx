package com.cxjt.xxd.plugin.operate;

import com.cxjt.xxd.constants.FormConstant;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.AddValidatorsEventArgs;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.BeginOperationTransactionArgs;
import kd.bos.servicehelper.operation.SaveServiceHelper;

/*
* 工作流尽调校验操作插件
* 审批信息赋值
* */
public class WorkFlowSubmitCheckOp extends AbstractOperationServicePlugIn {
    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        super.onPreparePropertys(e);
    }

    @Override
    public void onAddValidators(AddValidatorsEventArgs e) {
        super.onAddValidators(e);
        e.addValidator(new SubmitValidator());
    }

    @Override
    public void beginOperationTransaction(BeginOperationTransactionArgs e) {
        DynamicObject[] dataEntities = e.getDataEntities();
        DynamicObject dataEntity = dataEntities[0];
        //尽调结束后赋值审批期限
        dataEntity.set("ukwo_amountmob",dataEntity.get("ukwo_approve_amount"));
        dataEntity.set("ukwo_termmob",dataEntity.get("ukwo_approve_term"));

        dataEntity.set("ukwo_last_year_revemob",dataEntity.get("ukwo_last_year_revenue"));
        dataEntity.set("ukwo_last_year_total_amob",dataEntity.get("ukwo_last_year_total_asse"));
        dataEntity.set("ukwo_last_year_pay_tamob",dataEntity.get("ukwo_last_year_pay_taxes"));
        dataEntity.set("ukwo_number_of_employmob",dataEntity.get("ukwo_number_of_employees"));
        dataEntity.set("ukwo_is_existing_businmob",dataEntity.get("ukwo_is_existing_business"));
        dataEntity.set("ukwo_if_first_bank_mob",dataEntity.get("ukwo_if_first_bank_loan"));
        dataEntity.set("ukwo_high_tech_mob",dataEntity.get("ukwo_high_tech_industry"));
        dataEntity.set("ukwo_if_three_farmob",dataEntity.get("ukwo_if_three_farmers"));
        dataEntity.set("ukwo_core_fund_using_mob",dataEntity.get("ukwo_core_fund_using_ente"));
        dataEntity.set("ukwo_incr_emp_mob",dataEntity.get("ukwo_incr_emp_count"));
        dataEntity.set("ukwo_incr_income_mob",dataEntity.get("ukwo_incr_income_amout"));

        dataEntity.set("ukwo_economic_mob",dataEntity.get("ukwo_economic_compos"));
        dataEntity.set("ukwo_policy_support_mob",dataEntity.get("ukwo_policy_support_area"));
        dataEntity.set("ukwo_strategy_em_mob",dataEntity.get("ukwo_strategy_em_industry"));
        dataEntity.set("ukwo_ent_register_mob",dataEntity.get("ukwo_ent_register_area"));

        SaveServiceHelper.saveOperate(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME,dataEntities);
    }
}
