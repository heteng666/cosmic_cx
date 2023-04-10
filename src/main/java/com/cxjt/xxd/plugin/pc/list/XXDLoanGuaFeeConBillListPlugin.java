package com.cxjt.xxd.plugin.pc.list;

import com.alibaba.fastjson.JSONObject;
import com.cxjt.xxd.component.GuaFeeConfirmComponent;
import com.cxjt.xxd.component.WorkflowComponent;
import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.enums.BillResNotityStatusEnum;
import com.cxjt.xxd.enums.JrcsErrorCodeEnum;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.exception.KDBizException;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.workflow.WorkflowServiceHelper;
import kd.bos.workflow.component.approvalrecord.IApprovalRecordItem;
import org.apache.commons.collections.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * 担保费确认单列表插件
 */
public class XXDLoanGuaFeeConBillListPlugin extends AbstractBillPlugIn {

    private final Log logger = LogFactory.getLog(this.getClass());

    //按钮操作编码
    private static final String OPERATE_KEY_NOTITY_AUDIT_RES = "do_notify_audit_res";

    /**
     * 担保费确认单列表工具栏[同步确认结果]逻辑
     * @param args
     */
    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        String operateKey = args.getOperateKey();
        if (OPERATE_KEY_NOTITY_AUDIT_RES.equals(operateKey)) {
            OperationResult opResult = args.getOperationResult();
            if (opResult == null || !opResult.isSuccess()) {
                return;
            }

            List<Object> pkIds = opResult.getSuccessPkIds();
            if (CollectionUtils.isEmpty(pkIds)) {
                throw new KDBizException("暂未获取到选中的担保费确认单列表数据,请联系系统管理员");
            }
            if (pkIds.size() > 5) {
                throw new KDBizException("最多支持选中5条数据");
            }

            //操作失败的记录数
            int failedCount = 0;

            String entityName = FormConstant.LOAN_GUA_CONFIRM_BILL_ENTITY_NAME;
            String selectProperties = "id,billno,ukwo_apply_id,ukwo_record_id,ukwo_pay_notity_status,ukwo_pay_notice_code,ukwo_pay_notice_msg,modifytime";
            QFilter[] filters = new QFilter[]{new QFilter("id", QCP.in, pkIds)};
            DynamicObject[] datas = BusinessDataServiceHelper.load(entityName, selectProperties, filters);

            for (DynamicObject guaConfirmBill : datas) {
                String ukwoPayNotityStatus = (String) guaConfirmBill.get("ukwo_pay_notity_status");
                //跳过已经通知成功的数据
                if (BillResNotityStatusEnum.SUCCESS.getCode().equals(ukwoPayNotityStatus)) {
                    continue;
                }

                //业务编号
                String ukwoApplyId = (String) guaConfirmBill.get("ukwo_apply_id");
                //汇款记录编号
                String ukwoRecordId = (String) guaConfirmBill.get("ukwo_record_id");
                String businessKey = String.valueOf(guaConfirmBill.get("id"));
                boolean inProcess = WorkflowServiceHelper.inProcess(businessKey);

                if (inProcess) {
                    logger.warn("【{}】业务单据,担保费确认流程暂未结束,中断通知JRCS......",ukwoApplyId);
                    continue;
                }


                try {
                    IApprovalRecordItem approvalRecordItem = WorkflowComponent.getApprovalRecordItem(businessKey);

                    //请求金融超市
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
                    SaveServiceHelper.update(new DynamicObject[]{guaConfirmBill});
                } catch (Exception e) {
                    logger.error("[手动重试]担保费确认结果通知异常,信息如下:{},{},异常信息如下:{}", ukwoApplyId, ukwoRecordId, e);
                    failedCount++;
                }

                try {
                    Thread.sleep(2000L);
                } catch (Exception e) {

                }

            }

            if (failedCount != 0) {
                String errorMsg = String.format("担保费确认结果通知%d条数据请求失败,请联系系统管理员", failedCount);
                this.getView().showErrorNotification(errorMsg);
            }

            if((datas.length - failedCount) > 0 ){
                this.getView().invokeOperation("refresh");
            }
        }
    }
}
