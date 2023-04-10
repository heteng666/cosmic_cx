package com.cxjt.xxd.service;

import com.cxjt.xxd.dao.XXDLoanApplyBillAttTaskItemDao;
import kd.bos.dataentity.entity.DynamicObjectCollection;

/**
 * 贷款申请单-【决议】附件
 */
public class BillResolutionAttachmentExecutor extends AbstractBillAttachmentExecutor {
    @Override
    public DynamicObjectCollection query(String applyId) {
        DynamicObjectCollection resolutionList = XXDLoanApplyBillAttTaskItemDao.queryResolution(applyId);
        return resolutionList;
    }

    @Override
    public String getDesc() {
        return "贷款申请单-决议附件执行器";
    }
}
