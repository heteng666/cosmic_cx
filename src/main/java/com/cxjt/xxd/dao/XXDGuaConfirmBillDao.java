package com.cxjt.xxd.dao;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.workflow.api.AgentExecution;

public class XXDGuaConfirmBillDao {


    public static DynamicObject loadSingle(Object pk, String entityName) {
        DynamicObject guaConfirmBill = BusinessDataServiceHelper.loadSingle(pk, entityName);

        return guaConfirmBill;
    }

    public static DynamicObject loadSingle(AgentExecution execution) {
        //单据业务ID
        String businessKey = execution.getBusinessKey();
        //获取当前审批的单据标识
        String entityNumber = execution.getEntityNumber();

        DynamicObject guaConfirmBill = loadSingle(businessKey, entityNumber);

        return guaConfirmBill;
    }
}
