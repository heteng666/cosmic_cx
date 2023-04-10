package com.cxjt.xxd.plugin.pc.list;

import com.cxjt.xxd.component.LoanApplicationComponent;
import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.enums.XXDErrorCodeEnum;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDBizException;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.workflow.WorkflowServiceHelper;
import kd.bos.workflow.api.BizProcessStatus;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * 贷款尽调申请列表插件,手动触发【尽调结果通知】
 */
public class XXDLoanApplicationBillListPlugin extends AbstractBillPlugIn {

    private final Log logger = LogFactory.getLog(this.getClass());

    //按钮操作编码
    private static final String OPERATE_KEY_NOTITY_AUD_RESULT = "ukwo_tb_notify_aud_resust";

    public static final String SELECT_PROPERTIES = "id,billno,ukwo_apply_id,ukwo_approve_amount,ukwo_approve_term,ukwo_project_manager,ukwo_company_name,ukwo_legal_person_name,ukwo_region_code,ukwo_loan_notity_status,ukwo_loan_notity_code,ukwo_loan_notice_msg,entryentity.ukwo_signer_name,entryentity.ukwo_gua_signer_type,entryentity.ukwo_relation_type,entryentity.ukwo_signer_idcard,entryentity.ukwo_signer_phone,entryentity.ukwo_busi_lic_address,entryentity.ukwo_signer_address,entryentity.ukwo_company_name_ent,entryentity.ukwo_u_soc_cre_code_ent,entryentity.ukwo_gua_attachment,entryentity.ukwo_gua_attachment.url";

    /**
     * 贷款尽调申请单列表工具栏[同步审核结果]逻辑，
     * 此功能逻辑较重,最好一条一条来,毕竟只是兜底,正常情况下,需要重发的数据很少
     *
     * @param args
     */
    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        String operateKey = args.getOperateKey();

        if (!OPERATE_KEY_NOTITY_AUD_RESULT.equals(operateKey)) {
            return;
        }

        OperationResult opResult = args.getOperationResult();
        if (opResult == null || !opResult.isSuccess()) {
            return;
        }

        List<Object> pkIds = opResult.getSuccessPkIds();
        if (CollectionUtils.isEmpty(pkIds)) {
            throw new KDBizException("暂未选中数据");
        }
        if (pkIds.size() > 1) {
            throw new KDBizException("只支持选中单条数据");
        }

        logger.info("[手动]开始执行将审批结果同步给金融超市逻辑");
        //获取当前审批的单据标识
        String entityName = FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME;
        //String selectProperties = "id,billno,ukwo_apply_id,ukwo_approve_amount,ukwo_approve_term,ukwo_project_manager,ukwo_loan_notity_status,ukwo_loan_notity_code,ukwo_loan_notice_msg,entryentity.ukwo_signer_name,entryentity.ukwo_gua_signer_type,entryentity.ukwo_relation_type,entryentity.ukwo_signer_idcard,entryentity.ukwo_signer_phone,entryentity.ukwo_busi_lic_address,entryentity.ukwo_signer_address,entryentity.ukwo_company_name_ent,entryentity.ukwo_u_soc_cre_code_ent";
        QFilter[] filters = new QFilter[]{new QFilter("id", QCP.equals, pkIds.get(0))};

        DynamicObject loanApplicationBill = BusinessDataServiceHelper.loadSingle(entityName, SELECT_PROPERTIES, filters);

        String ukwoApplyId = (String) loanApplicationBill.get("ukwo_apply_id");
        try {
            //手动发起通知时,尽调流程已经结束
            String businessKey = String.valueOf(loanApplicationBill.get("id"));

            //true:当前单据如果处于流程当中,false:当前单据已经结束
            boolean inProcess = WorkflowServiceHelper.inProcess(businessKey);
            if (inProcess) {
                logger.warn("[手动重试]流程实例结束后才执行尽调结果通知逻辑:businessKey = {},ukwoApplyId={},inProcess={}", businessKey, ukwoApplyId, inProcess);
                throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.FAILED.getCode(), "流程实例结束后才能同步尽调审批结果通知,请联系系统管理员"));
            }


            LoanApplicationComponent.auditResultNotify(loanApplicationBill);
            //[刷新列表数据]操作刷新,refresh为操作编码
            this.getView().invokeOperation("refresh");
        } catch (Exception e) {
            logger.error("[手动重试]同步尽调审批结果异常,业务单据编号:{},异常信息如下:{}", ukwoApplyId, e);
            if (e instanceof KDBizException) {
                throw new KDBizException(e.getMessage());
            }
            throw new KDBizException("同步尽调审批结果失败,请联系系统管理员");
        }

    }

}
