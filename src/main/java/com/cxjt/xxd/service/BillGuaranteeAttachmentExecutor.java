package com.cxjt.xxd.service;

import com.cxjt.xxd.dao.XXDLoanApplyBillAttTaskItemDao;
import kd.bos.dataentity.entity.DynamicObjectCollection;

/**
 * 贷款申请单-【反担保附件】
 */
public class BillGuaranteeAttachmentExecutor extends AbstractBillAttachmentExecutor {
    @Override
    public DynamicObjectCollection query(String applyId) {
        DynamicObjectCollection guaranteeList = XXDLoanApplyBillAttTaskItemDao.queryGuarantee(applyId);
        return guaranteeList;
    }

    @Override
    public String getDesc() {
        return "贷款申请单-反担保附件执行器";
    }
}
