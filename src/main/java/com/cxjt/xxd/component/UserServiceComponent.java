package com.cxjt.xxd.component;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.servicehelper.user.UserServiceHelper;

import java.util.Map;

/**
 * 用户组件
 */
public class UserServiceComponent {

    public static DynamicObject getUserInfoByID(long userID,String selector){
       return UserServiceHelper.getUserInfoByID(userID,selector);
    }
    /**
     * 获取当前人员ID
     *
     * @return
     */
    public static long getCurrentUserId() {
        return kd.bos.servicehelper.user.UserServiceHelper.getCurrentUserId();
    }

    /**
     * 获取用户的主要职务部门
     *
     * @param userID 人员ID 人员内码
     * @return 主职部门内码
     */
    public static long getUserMainOrgId(long userID) {
        return kd.bos.servicehelper.user.UserServiceHelper.getUserMainOrgId(userID);
    }

    /**
     * 根据用户id获取用户信息
     *
     * @param userID 用户id
     * @return 包含人员的userid、uid、number、name、phone、email、type的map
     */
    public static Map<String, Object> getUserInfoByID(long userID) {
        return kd.bos.servicehelper.user.UserServiceHelper.getUserInfoByID(userID);
    }

    /**
     * 判断某个人员是否属于某个组织
     *
     * @param userId 人员ID
     * @param orgId  组织ID
     * @return true: 属于; false: 不属于
     */
    public static boolean isUserBelongTo(Long userId, Long orgId) {
        return kd.bos.servicehelper.user.UserServiceHelper.isUserBelongTo(userId, orgId);
    }

    /**
     * 判断某个人员是否属于某个组织
     *
     * @param userId              人员ID
     * @param orgId               组织ID
     * @param checkBelongToSubOrg 是否校验下级组织
     * @return  true: 属于; false: 不属于
     */
    public static boolean isUserBelongTo(Long userId, Long orgId, boolean checkBelongToSubOrg) {
        return kd.bos.servicehelper.user.UserServiceHelper.isUserBelongTo(userId, orgId, checkBelongToSubOrg);
    }


}
