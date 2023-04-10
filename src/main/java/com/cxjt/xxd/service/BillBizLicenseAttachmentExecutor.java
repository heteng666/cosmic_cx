package com.cxjt.xxd.service;

import com.cxjt.xxd.dao.XXDLoanApplyBillAttTaskItemDao;
import kd.bos.dataentity.entity.DynamicObjectCollection;

/**
 * 贷款申请单-【营业执照】附件
 */
public class BillBizLicenseAttachmentExecutor extends AbstractBillAttachmentExecutor {
    @Override
    public DynamicObjectCollection query(String applyId) {
        //获取单据【营业执照】附件
        DynamicObjectCollection bizLicenseList = XXDLoanApplyBillAttTaskItemDao.queryBizLicense(applyId);
        return bizLicenseList;
    }

    @Override
    public String getDesc() {
        return "贷款申请单-营业执照附件执行器";
    }
}
