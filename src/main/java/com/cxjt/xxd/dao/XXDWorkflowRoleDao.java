package com.cxjt.xxd.dao;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作流角色
 */
public class XXDWorkflowRoleDao {

    private static final Log logger = LogFactory.getLog(XXDWorkflowRoleDao.class);

    /**
     * 工作流角色实体标识
     */
    private static final String WORK_FLOW_ROLE_ENTIRY_NAME = "wf_role";

    /**
     * 查询工作流角色实体缺省属性
     */
    private static final String DEFAULT_WORK_FLOW_ROLE_SELECT_PROPERTIES = "id,number,name,roleentry,roleentry.user";

    /**
     * 查询单据头信息 + 单据体信息,不支持*查询，需要什么属性就查什么属性,单据体的属性获取方式为“entryentity.标识”
     * 如:"id,number,name,roleentry,roleentry.user"
     *
     * @param entityName
     * @param selectProperties
     * @param filters
     * @return
     */
    public static DynamicObject loadSingle(String entityName, String selectProperties, QFilter[] filters) {
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle(entityName, selectProperties, filters);
        return dynamicObject;
    }


    public static DynamicObject queryWorkFlowRole(QFilter[] filters) {
        DynamicObject dynamicObject = loadSingle(WORK_FLOW_ROLE_ENTIRY_NAME, DEFAULT_WORK_FLOW_ROLE_SELECT_PROPERTIES, filters);
        return dynamicObject;
    }

    /**
     * 获取角色-人员明细单据体集合
     *
     * @param workFlowRole
     * @return
     */
    public static DynamicObjectCollection getWorkFlowRoleEntrys(DynamicObject workFlowRole) {
        DynamicObjectCollection entrys = workFlowRole.getDynamicObjectCollection("roleentry");
        return entrys;
    }

    /**
     * 根据角色编码获取角色人员,角色编码需唯一
     *
     * @param roleCode
     * @return
     */
    public static List<Long> getRoleUserIds(String roleCode) {
        try {
            QFilter[] filters = new QFilter[]{new QFilter("number", QCP.equals, roleCode)};

            List<Long> usersByRoleNum = new ArrayList<>();

            DynamicObject workFlowRole = queryWorkFlowRole(filters);
            if(workFlowRole == null){
                logger.warn("[{}]角色编码不存在",roleCode);
                return usersByRoleNum;
            }

            //获取角色-人员明细单据体集合
            DynamicObjectCollection entrys = getWorkFlowRoleEntrys(workFlowRole);

            if(entrys == null || entrys.size() <= 0){
                logger.warn("[{}]角色编码下未找到参与人",roleCode);
                return usersByRoleNum;
            }

            //遍历角色人员明细
            for (DynamicObject roleEntry : entrys) {
                //获取审批人员对象
                DynamicObject user = (DynamicObject) roleEntry.get("user");
                Long enable = Long.valueOf((String) user.get("enable"));
                //若人员状态为可用
                if (1 == enable) {
                    //获取人员id
                    Long userId = (Long) user.get("id");
                    //将人员ID存入集合
                    usersByRoleNum.add(userId);
                }

            }

            //根据角色获取参与人
            return usersByRoleNum;

        } catch (Exception e) {
            logger.error("根据角色编码[{}]获取角色人员出现异常,异常信息如下:{}",roleCode, e);
            throw new RuntimeException(e);
        }

    }

}
