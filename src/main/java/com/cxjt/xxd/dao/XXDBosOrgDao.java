package com.cxjt.xxd.dao;

import com.cxjt.xxd.constants.FormConstant;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.servicehelper.BusinessDataServiceHelper;


public class XXDBosOrgDao {

    /**
     * 根据组织机构ID获取组织
     * @param pk
     * @return
     */
    public static DynamicObject queryById(Object pk) {
        DynamicObject orgObj = BusinessDataServiceHelper.loadSingle(pk, FormConstant.BOS_ORG);

        return orgObj;
    }

}
