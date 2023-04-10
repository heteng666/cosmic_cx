package com.cxjt.xxd.helper;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;

public class XXDLoanApplicationHelper {

    /**
     * 获取单据体
     *
     * @param dynamicObject
     * @return
     */
    public static DynamicObjectCollection getDynamicObjectCollection(DynamicObject dynamicObject) {
        DynamicObjectCollection collection = dynamicObject.getDynamicObjectCollection("entryentity");
        return collection;
    }

    /**
     * 获取单据体数据类型
     *
     * @param dynamicObject
     * @return
     */
    public static DynamicObjectType getDynamicObjectType(DynamicObject dynamicObject) {
        DynamicObjectCollection entryentity = getDynamicObjectCollection(dynamicObject);
        DynamicObjectType dynamicObjectType = entryentity.getDynamicObjectType();
        return dynamicObjectType;
    }

}
