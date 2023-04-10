package com.cxjt.xxd.dao;

import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.servicehelper.operation.OperationServiceHelper;


public class XXDLoanApplyNoteBillDao {


    public void save(String entityNumber, DynamicObject loanApplyNoteBill) {
        OperationResult result = OperationServiceHelper.executeOperate("submit", entityNumber, new DynamicObject[]{loanApplyNoteBill}, OperateOption.create());
        boolean isSuccess = result.isSuccess();
        if (!isSuccess) {
            throw new RuntimeException(result.toString());
        }
    }

}
