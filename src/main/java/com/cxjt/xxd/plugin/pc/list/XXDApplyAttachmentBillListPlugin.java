package com.cxjt.xxd.plugin.pc.list;

import com.cxjt.xxd.component.AttachmentServiceComponent;
import com.cxjt.xxd.component.LoanApplyAttComponent;
import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.dao.XXDLoanApplyBillAttTaskDao;
import com.cxjt.xxd.dao.XXDLoanApplyBillAttTaskItemDao;
import com.cxjt.xxd.enums.AttachmentEnum;
import com.cxjt.xxd.model.bo.AttachmentBO;
import com.cxjt.xxd.model.res.order.FileInfoRes;
import com.cxjt.xxd.service.BillAttachmentExecutor;
import com.cxjt.xxd.util.FileUtil;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.OrmLocaleValue;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.exception.KDBizException;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * 列表插件,请求金融超市[上送文件接口]
 */
public class XXDApplyAttachmentBillListPlugin extends AbstractBillPlugIn {

    private final Log logger = LogFactory.getLog(this.getClass());

    //按钮操作编码
    private static final String OPERATE_KEY_SEND_APPLY_FILE = "ukwo_tb_send_apply_attach";

    //[单据附件]列表工具栏[上送附件接口]逻辑
    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {

        String operateKey = args.getOperateKey();

        if (!OPERATE_KEY_SEND_APPLY_FILE.equals(operateKey)) {
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
        //申请单附件任务单ID
        long pkId = (long) pkIds.get(0);

        DynamicObject taskBill = XXDLoanApplyBillAttTaskDao.queryByPK(pkId);

        BillAttachmentExecutor executor = new BillAttachmentExecutor();
        executor.execute(taskBill);

    }

}
