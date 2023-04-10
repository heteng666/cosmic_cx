package com.cxjt.xxd.dao;

import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.enums.BillResNotityStatusEnum;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.db.ResultSetHandler;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 申请单附件定时任务
 */
public class XXDLoanApplyBillAttTaskDao {

    private static final String DEFAULT_SELECT_PROPERTIES = "id,ukwo_apply_bill_id,ukwo_apply_id,ukwo_company_name,ukwo_legal_person_name,ukwo_region_code,ukwo_att_count,ukwo_exec_status,ukwo_enable_status,ukwo_start_time,ukwo_end_time,ukwo_create_time";

    private static final String ENTITY_NAME = FormConstant.LOAN_APPLY_BILL_ATT_TASK_NAME;

    /**
     * 根据主键查询
     *
     * @param pkId
     * @return
     */
    public static DynamicObject queryByPK(long pkId) {
        QFilter[] filters = new QFilter[]{new QFilter("id", QCP.equals, pkId)};

        DynamicObject loanTaskBill = BusinessDataServiceHelper.loadSingle(ENTITY_NAME, DEFAULT_SELECT_PROPERTIES, filters);

        return loanTaskBill;
    }

    public static DynamicObject queryByApplyBillId(String applyBillId) {
        QFilter[] filters = new QFilter[]{new QFilter("ukwo_apply_bill_id", QCP.equals, applyBillId)};

        DynamicObject loanTaskBill = BusinessDataServiceHelper.loadSingle(ENTITY_NAME, DEFAULT_SELECT_PROPERTIES, filters);

        return loanTaskBill;
    }

    public static boolean updateTaskForRunning(long taskId) {
        DBRoute dbRoute = DBRoute.of(FormConstant.SECD_ROUTE_KEY);
        //TODO case when ukwo_start_time,为了统计整个任务耗时,应只有首次才update,重试update会导致耗时不准
        String sql = "update tk_ukwo_apply_bi_att_task set fk_ukwo_exec_status = ?,fk_ukwo_start_time = ? where fid = ? and fk_ukwo_enable_status = ? and fk_ukwo_exec_status in(?,?)";
        Object[] params = new Object[]{BillResNotityStatusEnum.RUNNING.getCode(), new Date(), taskId, FormConstant.ENABLE, BillResNotityStatusEnum.NOT_YET.getCode(), BillResNotityStatusEnum.FAILED.getCode()};
        int rows = DB.update(dbRoute, sql, params);

        return rows > 0;
    }

    public static boolean updateTaskForSuccess(long taskId,Integer billTotalAttachCount) {
        DBRoute dbRoute = DBRoute.of(FormConstant.SECD_ROUTE_KEY);
        String sql = "update tk_ukwo_apply_bi_att_task set fk_ukwo_exec_status = ?,fk_ukwo_att_count = ?,fk_ukwo_end_time = ? where fid = ?  and fk_ukwo_exec_status not in(?)";
        Object[] params = new Object[]{BillResNotityStatusEnum.SUCCESS.getCode(),billTotalAttachCount, new Date(), taskId, BillResNotityStatusEnum.SUCCESS.getCode()};
        int rows = DB.update(dbRoute, sql, params);

        return rows > 0;
    }


    public static boolean updateTaskForFailed(long taskId,Integer billTotalAttachCount) {
        DBRoute dbRoute = DBRoute.of(FormConstant.SECD_ROUTE_KEY);
        String sql = "update tk_ukwo_apply_bi_att_task set fk_ukwo_exec_status = ?,fk_ukwo_att_count = ?,fk_ukwo_end_time = ? where fid = ?  and fk_ukwo_exec_status not in(?)";
        Object[] params = new Object[]{BillResNotityStatusEnum.FAILED.getCode(),billTotalAttachCount, new Date(), taskId, BillResNotityStatusEnum.SUCCESS.getCode()};
        int rows = DB.update(dbRoute, sql, params);

        return rows > 0;
    }

    /**
     * 分页获取[申请单附件定时任务]表中状态为[启用]且执行状态为[未执行]以及[执行失败]的业务单据数
     *
     * @param pageSize
     * @return
     */
    public static DynamicObjectCollection queryTaskByPage(int pageSize) {
        //启用
        QFilter enable = new QFilter("ukwo_enable_status", QCP.equals, FormConstant.ENABLE);

        //[未执行]以及[执行失败]
        List<String> execStatusList = Arrays.asList(BillResNotityStatusEnum.NOT_YET.getCode(), BillResNotityStatusEnum.FAILED.getCode());
        QFilter exec = new QFilter("ukwo_exec_status", QCP.in, execStatusList);

        QFilter filter = (enable.and(exec));

        QFilter[] filters = new QFilter[]{filter};
        String selectFields = DEFAULT_SELECT_PROPERTIES;

        //新增创建时间字段,按时间降序排列
        String orderBy = "ukwo_create_time ASC";

        DynamicObjectCollection taskList = QueryServiceHelper.query(ENTITY_NAME, selectFields, filters, orderBy, pageSize);

        return taskList;

    }

    /**
     * 获取[申请单附件定时任务]表中状态为[启用]且执行状态为[未执行]以及[执行失败]的业务单据数
     *
     * @return
     */
    public static int getTaskCount() {
        //String algoKey = XXDLoanApplyBillAttTaskDao.class.getName();
        DBRoute dbRoute = DBRoute.of(FormConstant.SECD_ROUTE_KEY);
        String sql = "select count(1) dataCount from tk_ukwo_apply_bi_att_task where fk_ukwo_enable_status = ? and fk_ukwo_exec_status in(?,?)";
        Object[] params = new Object[]{FormConstant.ENABLE, BillResNotityStatusEnum.NOT_YET.getCode(), BillResNotityStatusEnum.FAILED.getCode()};

        //DataSet dataSet = DB.queryDataSet(algoKey, dbRoute, sql, params);
        //int count = dataSet.count("ukwo_apply_id",true);
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

}
