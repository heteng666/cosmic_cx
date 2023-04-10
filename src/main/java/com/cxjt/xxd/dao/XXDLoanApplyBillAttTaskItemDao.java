package com.cxjt.xxd.dao;

import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.enums.AttachmentEnum;
import com.cxjt.xxd.enums.BillResNotityStatusEnum;
import com.cxjt.xxd.model.bo.AttachmentBO;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.db.ResultSetHandler;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 申请单附件任务明细
 */
public class XXDLoanApplyBillAttTaskItemDao {

    private static final String DEFAULT_SELECT_PROPERTIES = "id,ukwo_apply_id,ukwo_gua_uni_code_ent,ukwo_attachment_name,ukwo_attachment_url,ukwo_attachment_type,ukwo_exec_status,ukwo_enable_status,ukwo_start_time,ukwo_end_time,ukwo_create_time";

    private static final String ENTITY_NAME = FormConstant.LOAN_APPLY_BILL_ATT_TASK_ITEM_NAME;

    /**
     * @param ukwoApplyId   业务单号
     * @param allAttachList 需要同步给金融超市的单据附件
     *                      <p>1:身份证正面<p/>
     *                      <p>2:身份证反面<p/>
     *                      <p>3:营业执照<p/>
     *                      <p>4:决议<p/>
     *                      <p>10:申请者附件<p/>
     *                      <p>12:反担保附件<p/>
     */
    public static void batchSave(String ukwoApplyId, List<AttachmentBO> allAttachList) {

        if (CollectionUtils.isEmpty(allAttachList)) {
            return;
        }

        DynamicObject[] dataEntities = new DynamicObject[allAttachList.size()];

        for (int i = 0; i < allAttachList.size(); i++) {
            AttachmentBO item = allAttachList.get(i);

            DynamicObject taskItemBill = BusinessDataServiceHelper.newDynamicObject(FormConstant.LOAN_APPLY_BILL_ATT_TASK_ITEM_NAME);
            taskItemBill.set("ukwo_apply_id", ukwoApplyId);
            taskItemBill.set("ukwo_gua_uni_code_ent", "");
            taskItemBill.set("ukwo_attachment_name", item.getUkwoAttachmentName());
            taskItemBill.set("ukwo_attachment_url", item.getUkwoAttachmentUrl());
            taskItemBill.set("ukwo_attachment_type", item.getUkwoAttachmentType());
            taskItemBill.set("ukwo_exec_status", BillResNotityStatusEnum.NOT_YET.getCode());
            taskItemBill.set("ukwo_enable_status", FormConstant.ENABLE);
            taskItemBill.set("ukwo_create_time", new Date());

            String ukwoGuaUniCodeEnt = item.getUkwoGuaUniCodeEnt();
            //只有单据体附件字段才有此值,[申请者附件]以及[反担保附件]无
            if (StringUtils.isNotBlank(ukwoGuaUniCodeEnt)) {
                taskItemBill.set("ukwo_gua_uni_code_ent", ukwoGuaUniCodeEnt);
            }

            dataEntities[i] = taskItemBill;
        }


        SaveServiceHelper.saveOperate(ENTITY_NAME, dataEntities, OperateOption.create());

    }

    static QFilter[] getCommonFilters(String applyId, String attachmentType) {
        QFilter applyIdFilter = new QFilter("ukwo_apply_id", QCP.equals, applyId);
        QFilter attachmentTypeFilter = new QFilter("ukwo_attachment_type", QCP.equals, attachmentType);
        //启用
        QFilter enable = new QFilter("ukwo_enable_status", QCP.equals, FormConstant.ENABLE);

        //[未执行]以及[执行失败]
        List<String> execStatusList = Arrays.asList(BillResNotityStatusEnum.NOT_YET.getCode(), BillResNotityStatusEnum.FAILED.getCode());
        QFilter exec = new QFilter("ukwo_exec_status", QCP.in, execStatusList);

        QFilter filter = (applyIdFilter.and(attachmentTypeFilter).and(enable).and(exec));

        QFilter[] filters = new QFilter[]{filter};

        return filters;
    }

    /**
     * 获取业务单据-附件
     *
     * @param applyId        业务编号
     * @param attachmentType 附件类型
     * @return
     */
    public static DynamicObjectCollection query(String applyId, String attachmentType) {
        QFilter[] filters = getCommonFilters(applyId, attachmentType);
        DynamicObjectCollection attachmentList = QueryServiceHelper.query(ENTITY_NAME, DEFAULT_SELECT_PROPERTIES, filters);

        return attachmentList;
    }

    /**
     * 获取业务单据-附件面板-申请者附件
     *
     * @param applyId
     * @return
     */
    public static DynamicObjectCollection queryApplicant(String applyId) {
        return query(applyId, AttachmentEnum.I.getCode());
    }

    /**
     * 获取业务单据-附件面板-反担保附件
     *
     * @param applyId
     * @return
     */
    public static DynamicObjectCollection queryGuarantee(String applyId) {
        return query(applyId, AttachmentEnum.K.getCode());
    }

    /**
     * 获取业务单据-附件字段-身份证人像面
     *
     * @param applyId
     * @return
     */
    public static DynamicObjectCollection queryIdCardFront(String applyId) {
        return query(applyId, AttachmentEnum.A.getCode());
    }

    /**
     * 获取业务单据-附件字段-身份证国徽面
     *
     * @param applyId
     * @return
     */
    public static DynamicObjectCollection queryIdCardBack(String applyId) {
        return query(applyId, AttachmentEnum.B.getCode());
    }

    /**
     * 获取业务单据-附件字段-营业执照
     *
     * @param applyId
     * @return
     */
    public static DynamicObjectCollection queryBizLicense(String applyId) {
        return query(applyId, AttachmentEnum.C.getCode());
    }

    /**
     * 获取业务单据-附件字段-决议
     *
     * @param applyId
     * @return
     */
    public static DynamicObjectCollection queryResolution(String applyId) {
        return query(applyId, AttachmentEnum.D.getCode());
    }

    /**
     * 获取单据[启用]状态,任务总数
     *
     * @param applyId
     * @return
     */
    public static int getTaskItemCount(String applyId) {
        DBRoute dbRoute = DBRoute.of(FormConstant.SECD_ROUTE_KEY);
        String sql = "select count(1) dataCount from tk_ukwo_bil_att_task_item where fk_ukwo_apply_id = ? and fk_ukwo_enable_status = ?";//230209430700000007
        Object[] params = new Object[]{applyId, FormConstant.ENABLE};//BillResNotityStatusEnum.NOT_YET.getCode(), BillResNotityStatusEnum.FAILED.getCode()

        int count = DB.query(dbRoute, sql, params, new ResultSetHandler<Integer>() {
            @Override
            public Integer handle(ResultSet rs) throws Exception {
                int count = 0;
                if (rs.next()) {
                    count = rs.getInt("dataCount");
                }
                return count;
            }
        });

        return count;

    }

    /**
     * 获取单据[启用]状态,执行成功任务总数
     *
     * @param applyId
     * @return
     */
    public static int getTaskItemSuccessCount(String applyId) {
        DBRoute dbRoute = DBRoute.of(FormConstant.SECD_ROUTE_KEY);
        String sql = "select count(1) dataCount from tk_ukwo_bil_att_task_item where fk_ukwo_apply_id = ? and fk_ukwo_exec_status =? and fk_ukwo_enable_status = ?";
        Object[] params = new Object[]{applyId,BillResNotityStatusEnum.SUCCESS.getCode(), FormConstant.ENABLE};

        int count = DB.query(dbRoute, sql, params, new ResultSetHandler<Integer>() {
            @Override
            public Integer handle(ResultSet rs) throws Exception {
                int count = 0;
                if (rs.next()) {
                    count = rs.getInt("dataCount");
                }
                return count;
            }
        });

        return count;

    }

    public static boolean updateTaskItemForRunning(long taskItemId) {
        DBRoute dbRoute = DBRoute.of(FormConstant.SECD_ROUTE_KEY);

        //ukwo_start_time 不用case when,每次都更新,统计任务明细耗时会丢失重试的耗时
        String sql = "update tk_ukwo_bil_att_task_item set fk_ukwo_exec_status = ?,fk_ukwo_start_time = ?  where fid = ? and fk_ukwo_enable_status = ? and fk_ukwo_exec_status in(?,?)";

        Object[] params = new Object[]{BillResNotityStatusEnum.RUNNING.getCode(), new Date(),taskItemId, FormConstant.ENABLE, BillResNotityStatusEnum.NOT_YET.getCode(), BillResNotityStatusEnum.FAILED.getCode()};
        int rows = DB.update(dbRoute, sql, params);

        return rows > 0;

    }

    public static boolean updateTaskItemForFailed(long taskItemId) {
        DBRoute dbRoute = DBRoute.of(FormConstant.SECD_ROUTE_KEY);
        String sql = "update tk_ukwo_bil_att_task_item set fk_ukwo_exec_status = ?,fk_ukwo_end_time = ?  where fid = ?  and  fk_ukwo_exec_status not in(?)";

        Object[] params = new Object[]{BillResNotityStatusEnum.FAILED.getCode(), new Date(),taskItemId, BillResNotityStatusEnum.SUCCESS.getCode()};
        int rows = DB.update(dbRoute, sql, params);

        return rows > 0;

    }


    public static boolean updateTaskItemForSuccess(long taskItemId) {
        DBRoute dbRoute = DBRoute.of(FormConstant.SECD_ROUTE_KEY);
        String sql = "update tk_ukwo_bil_att_task_item set fk_ukwo_exec_status = ?,fk_ukwo_end_time = ?  where fid = ?  and  fk_ukwo_exec_status not in(?)";

        Object[] params = new Object[]{BillResNotityStatusEnum.SUCCESS.getCode(), new Date(),taskItemId, BillResNotityStatusEnum.SUCCESS.getCode()};
        int rows = DB.update(dbRoute, sql, params);

        return rows > 0;

    }
}
