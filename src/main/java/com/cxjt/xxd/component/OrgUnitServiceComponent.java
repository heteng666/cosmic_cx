package com.cxjt.xxd.component;

import kd.bos.entity.tree.TreeNode;
import kd.bos.org.api.IOrgService;
import kd.bos.org.model.OrgTreeParam;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;
import kd.bos.servicehelper.org.OrgViewType;

import java.util.List;
import java.util.Map;

/**
 * 组织单元组件
 */
public class OrgUnitServiceComponent {

    /**
     * 获取直接上级组织
     *
     * @param viewSchemaNumber 组织视图方案编码
     * @param orgIds           组织ID列表
     * @return
     */
    public static Map<Long, Long> getDirectSuperiorOrg(String viewSchemaNumber, java.util.List<Long> orgIds) {
        Map<Long, Long> orglist = OrgUnitServiceHelper.getDirectSuperiorOrg(viewSchemaNumber, orgIds);

        return orglist;
    }

    public static Map<Long, Long> getDirectSuperiorOrgForAdmin(java.util.List<Long> orgIds) {
        Map<Long, Long> orglist = getDirectSuperiorOrg(OrgViewType.Admin, orgIds);

        return orglist;
    }

    /**
     * 根据组织ID获取组织视图树所有下级节点列表（包括孙子节点）
     *
     * @param param 组织树参数对象
     * @return
     */
    public static List<TreeNode> getTreeChildren(OrgTreeParam param) {
        return OrgUnitServiceHelper.getTreeChildren(param);
    }

    /**
     * 根据组织ID获取组织视图树所有下级节点列表（包括孙子节点）
     *
     * @param param 组织树参数对象
     * @return
     */
    public static List<Map<String, Object>> getTreeChildrenMap(OrgTreeParam param) {
        return OrgUnitServiceHelper.getTreeChildrenMap(param);
    }

    /**
     * 判断组织是否为上下级关系
     *
     * @param viewNumber 视图方案编码
     * @param parentId   上级组织ID
     * @param orgId      组织ID
     * @return
     */
    public static boolean isParentOrg(String viewNumber, long parentId, long orgId) {
        return OrgUnitServiceHelper.isParentOrg(viewNumber,parentId,orgId);
    }

    /**
     * 获取组织接口服务
     *
     * @return
     */
    public static IOrgService getOrgService() {
        return OrgUnitServiceHelper.getOrgService();
    }

}
