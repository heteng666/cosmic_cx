package com.cxjt.xxd.service;

import com.cxjt.xxd.dao.XXDLoanApplyBillAttTaskItemDao;
import kd.bos.dataentity.entity.DynamicObjectCollection;

/**
 * 贷款申请单-【申请者附件】
 */
public class BillApplicantAttachmentExecutor extends AbstractBillAttachmentExecutor {

    @Override
    public DynamicObjectCollection query(String applyId) {
        DynamicObjectCollection applicantList = XXDLoanApplyBillAttTaskItemDao.queryApplicant(applyId);
        return applicantList;
    }

    @Override
    public String getDesc() {
        return "贷款申请单-申请者附件执行器";
    }
}
