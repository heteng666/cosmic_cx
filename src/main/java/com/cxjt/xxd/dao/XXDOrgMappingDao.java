package com.cxjt.xxd.dao;

import com.cxjt.xxd.constants.FormConstant;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 组织机构映射单Helper
 */
public class XXDOrgMappingDao {

    private final Log logger = LogFactory.getLog(this.getClass());

    /**
     * 根据金融超市侧区域编码获取组织结构映射对象
     *
     * @param jrcsRegionCode 金融超市侧区域编码
     * @return
     */
    public static DynamicObject queryOne(long jrcsRegionCode) {
        String entityName = FormConstant.LOAN_APPLY_ORG_MAPPING_ENTITY_NAME;

        QFilter[] filters = new QFilter[]{new QFilter("ukwo_jrcs_org_id", QCP.equals, jrcsRegionCode)};

        DynamicObject orgMappingBill = BusinessDataServiceHelper.loadSingle(entityName, filters);

        if (orgMappingBill == null) {
            throw new RuntimeException("未查询到组织机构,请联系系统管理员");
        }

        return orgMappingBill;
    }

    /**
     * 根据金融超市侧区域编码获取[智慧财鑫]组织结构Id
     *
     * @param jrcsRegionCode 金融超市侧区域编码
     * @return
     */
    public static long getUkwoBosOrgId(long jrcsRegionCode) {
        DynamicObject orgMappingBill = queryOne(jrcsRegionCode);

        DynamicObject org = (DynamicObject) orgMappingBill.get("ukwo_bos_org_id");

        long orgId = (long) org.get("id");

        return orgId;
    }

    /**
     * 返回智慧财鑫侧[财科担保]各办事处组织机构Id
     * @return
     */
    public static List<Long> queryAllOrgIds() {
        String entityName = FormConstant.LOAN_APPLY_ORG_MAPPING_ENTITY_NAME;
        String selectProperties = "ukwo_bos_org_id,ukwo_jrcs_org_id,ukwo_jrcs_org_name";

        DynamicObject[] orgMappingList = BusinessDataServiceHelper.load(entityName, selectProperties, null);

        List<Long> orgList = new ArrayList<>(10);

        for (int i = 0; i < orgMappingList.length; i++) {
            DynamicObject mappingItem = orgMappingList[i];
            DynamicObject bosOrg = (DynamicObject) mappingItem.get("ukwo_bos_org_id");
            long bosOrgId = (long)bosOrg.get("id");
            orgList.add(bosOrgId);
        }

        return orgList;
    }
}
