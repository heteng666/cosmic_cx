package com.cxjt.xxd.service;

import com.cxjt.xxd.dao.XXDLoanApplyBillAttTaskItemDao;
import kd.bos.dataentity.entity.DynamicObjectCollection;

/**
 * 贷款申请单-【身份证人像面】附件
 */
public class BillIdCardFrontAttachmentExecutor extends AbstractBillAttachmentExecutor {
    @Override
    public DynamicObjectCollection query(String applyId) {
        DynamicObjectCollection idCardFrontList = XXDLoanApplyBillAttTaskItemDao.queryIdCardFront(applyId);
        return idCardFrontList;
    }

    @Override
    public String getDesc() {
        return "贷款申请单-身份证人像面附件执行器";
    }
}
