package com.cxjt.xxd.service;

import com.cxjt.xxd.dao.XXDLoanApplyBillAttTaskItemDao;
import kd.bos.dataentity.entity.DynamicObjectCollection;

/**
 * 贷款申请单-【身份证国徽面】附件
 */
public class BillIdCardBackAttachmentExecutor extends AbstractBillAttachmentExecutor {
    @Override
    public DynamicObjectCollection query(String applyId) {
        DynamicObjectCollection idCardBackList = XXDLoanApplyBillAttTaskItemDao.queryIdCardBack(applyId);
        return idCardBackList;
    }

    @Override
    public String getDesc() {
        return "贷款申请单-身份证国徽面附件执行器";
    }
}
