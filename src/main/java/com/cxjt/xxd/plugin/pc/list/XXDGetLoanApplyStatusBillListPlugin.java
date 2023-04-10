package com.cxjt.xxd.plugin.pc.list;

import com.cxjt.xxd.component.LoanApplyStatusComponent;
import com.cxjt.xxd.enums.ApplyStatusEnum;
import com.cxjt.xxd.service.XXDLoanApplicationService;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.exception.KDBizException;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import org.apache.commons.collections.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 贷款尽调申请列表插件,手动触发【获取订单状态】
 */
public class XXDGetLoanApplyStatusBillListPlugin extends AbstractBillPlugIn {

    private final Log logger = LogFactory.getLog(this.getClass());

    //按钮操作编码
    private static final String OPERATE_KEY_GET_APPLY_STATUS = "ukwo_tb_get_apply_status";


    //银行初审通过之后,才走担保流程,[担保审核拒绝][授信过期][银行复核拒绝][已还清]等处于终态的单据,可不必同步状态[已与产品经理确认终态]
    public static final List<Integer> finalStatus = XXDLoanApplicationService.finalStatus;


    /**
     * 贷款尽调申请单列表工具栏[获取订单状态]逻辑，
     *
     * @param args
     */
    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        String operateKey = args.getOperateKey();

        if (!OPERATE_KEY_GET_APPLY_STATUS.equals(operateKey)) {
            return;
        }

        OperationResult opResult = args.getOperationResult();
        if (opResult == null || !opResult.isSuccess()) {
            throw new KDBizException("操作失败,请联系系统管理员");
        }

        List<Object> pkIds = opResult.getSuccessPkIds();

        if (CollectionUtils.isEmpty(pkIds)) {
            this.getView().showErrorNotification("暂未选中数据");
            return;
        }

        if (pkIds.size() > 1) {
            this.getView().showMessage("只支持选中单条数据");
            return;
        }

        long pkId = (long) pkIds.get(0);
        DynamicObject orderBill = LoanApplyStatusComponent.queryOrderByPK(pkId);

        Object ukwOrderStatusCodeObj = orderBill.get("ukwo_order_status_code");

        if (ukwOrderStatusCodeObj != null) {
            Integer ukwOrderStatusCode = (Integer) ukwOrderStatusCodeObj;
            if (finalStatus.indexOf(ukwOrderStatusCode) != -1) {
                this.getView().showMessage("订单已处于终态无需获取");
                return;
            }
        }

        boolean result = LoanApplyStatusComponent.process(orderBill);

        if (result) {
            //[刷新列表数据]操作刷新,refresh为操作编码
            this.getView().invokeOperation("refresh");
        } else {
            //throw new KDBizException("获取状态失败");
            this.getView().showErrorNotification("获取状态失败");
        }

    }


}
