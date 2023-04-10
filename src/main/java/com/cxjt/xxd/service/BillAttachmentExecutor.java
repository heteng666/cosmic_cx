package com.cxjt.xxd.service;

import com.cxjt.xxd.component.LoanApplyAttComponent;
import com.cxjt.xxd.dao.XXDLoanApplyBillAttTaskDao;
import com.cxjt.xxd.dao.XXDLoanApplyBillAttTaskItemDao;
import com.cxjt.xxd.enums.BillResNotityStatusEnum;
import com.cxjt.xxd.model.bo.AttachmentBO;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BillAttachmentExecutor extends AbstractBillAttachmentExecutor {

    private final Log logger = LogFactory.getLog(this.getClass());

    private static List<AbstractBillAttachmentExecutor> billAttachmentExecutorList = new ArrayList<>(6);

    static {
        billAttachmentExecutorList.add(new BillApplicantAttachmentExecutor());
        billAttachmentExecutorList.add(new BillGuaranteeAttachmentExecutor());
        billAttachmentExecutorList.add(new BillIdCardFrontAttachmentExecutor());
        billAttachmentExecutorList.add(new BillIdCardBackAttachmentExecutor());
        billAttachmentExecutorList.add(new BillBizLicenseAttachmentExecutor());
        billAttachmentExecutorList.add(new BillResolutionAttachmentExecutor());
    }

    @Override
    public void execute(DynamicObject taskBill) {
        //从[附件任务订单]获取到的申请单ID,[附件任务订单]中的业务编号为String
        String billIdStr = (String) taskBill.get("ukwo_apply_bill_id");
        // [贷款申请订单]中的主键为Long
        long applyBillId = Long.parseLong(billIdStr);
        //[贷款申请订单]业务编号
        String applyId = (String) taskBill.get("ukwo_apply_id");

        long taskId = (long) taskBill.get("id");
        boolean flag = XXDLoanApplyBillAttTaskDao.updateTaskForRunning(taskId);
        //更新附件任务单失败
        if (!flag) {
            return;
        }

        Integer billTotalAttachCount = -1;
        try {
            //获取[订单附件]
            List<AttachmentBO> allAttachList = LoanApplyAttComponent.getBillTaskItem(applyBillId);
            //入库前先查询,若没有,则将[订单附件]信息入库
            int taskItemCount = XXDLoanApplyBillAttTaskItemDao.getTaskItemCount(applyId);
            if (taskItemCount == 0) {
                XXDLoanApplyBillAttTaskItemDao.batchSave(applyId, allAttachList);
                taskItemCount = getBillTotalAttachCount(applyId);
            }


            //循环处理【单据】【附件分类】
            for (AbstractBillAttachmentExecutor executor : billAttachmentExecutorList) {
                try {
                    executor.execute(taskBill);
                } catch (Exception e) {
                    logger.error("[{}]执行[{}]任务单出现异常,异常信息如下:{}", executor.getDesc(), taskId, e);
                }
            }

            //获取启用状态【贷款申请单】附件总数
            //TODO 将applyId调整为ukwo_apply_bill_id
            billTotalAttachCount = taskItemCount;
            //获取启用状态【贷款申请单】执行成功附件总数
            Integer billSuccessTotalAttachCount = getBillSuccessTotalAttachCount(applyId);

            taskBill.set("ukwo_att_count", billTotalAttachCount);
            taskBill.set("ukwo_end_time", new Date());
            taskBill.set("ukwo_exec_status", BillResNotityStatusEnum.FAILED.getCode());

            if (billTotalAttachCount.equals(billSuccessTotalAttachCount)) {
                //更新任务状态
                //taskBill.set("ukwo_exec_status", BillResNotityStatusEnum.SUCCESS.getCode());
                XXDLoanApplyBillAttTaskDao.updateTaskForSuccess(taskId, billTotalAttachCount);
            } else {
                XXDLoanApplyBillAttTaskDao.updateTaskForFailed(taskId, billTotalAttachCount);
            }
            //更新任务状态
            //SaveServiceHelper.update(new DynamicObject[]{taskBill});
        } catch (Exception e) {
            //taskBill.set("ukwo_exec_status", BillResNotityStatusEnum.FAILED.getCode());
            //SaveServiceHelper.update(new DynamicObject[]{taskBill});
            XXDLoanApplyBillAttTaskDao.updateTaskForFailed(taskId, billTotalAttachCount);
        } finally {
            super.removeBillTotalAttachCount(applyId);
        }

    }

    @Override
    public DynamicObjectCollection query(String applyId) {
        return null;
    }

    @Override
    public String getDesc() {
        return "贷款申请单附件执行器";
    }
}
