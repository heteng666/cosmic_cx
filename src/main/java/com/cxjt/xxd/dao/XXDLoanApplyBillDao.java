package com.cxjt.xxd.dao;

import com.cxjt.xxd.constants.FormConstant;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.db.ResultSetHandler;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.workflow.api.AgentExecution;


import java.sql.ResultSet;
import java.util.Date;
import java.util.List;

public class XXDLoanApplyBillDao {

    public static final String SELECT_PROPERTIES_WITH_PANEL_FIELD = "id,billno,ukwo_apply_id,ukwo_approve_amount,ukwo_approve_term,ukwo_project_manager,ukwo_company_name,ukwo_legal_person_name,ukwo_region_code,ukwo_loan_notity_status,ukwo_loan_notity_code,ukwo_loan_notice_msg,entryentity.ukwo_signer_name,entryentity.ukwo_gua_signer_type,entryentity.ukwo_relation_type,entryentity.ukwo_signer_idcard,entryentity.ukwo_signer_phone,entryentity.ukwo_busi_lic_address,entryentity.ukwo_signer_address,entryentity.ukwo_company_name_ent,entryentity.ukwo_u_soc_cre_code_ent,entryentity.ukwo_gua_attachment,entryentity.ukwo_gua_attachment.url";

    /**
     * 根据主键和实体名称查询
     *
     * @param pk
     * @param entityName
     * @return
     */
    public static DynamicObject loadSingle(Object pk, String entityName) {
        DynamicObject loanApplicationBill = BusinessDataServiceHelper.loadSingle(pk, entityName);
        return loanApplicationBill;
    }

    public static DynamicObject loadSingle(Object pk) {
        DynamicObject loanApplicationBill = loadSingle(pk, FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME);
        return loanApplicationBill;
    }

    public static DynamicObject loadSingle(AgentExecution execution) {
        //单据业务ID
        String businessKey = execution.getBusinessKey();
        //获取当前审批的单据标识
        String entityNumber = execution.getEntityNumber();

        DynamicObject loanApplicationBill = loadSingle(businessKey, entityNumber);

        return loanApplicationBill;
    }

    /**
     * 根据业务编号查询贷款申请订单
     *
     * @param ukwoApplyId
     * @return
     */
    public static DynamicObject queryOne(String ukwoApplyId) {
        String entityName = FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME;

        QFilter[] filters = new QFilter[]{new QFilter("ukwo_apply_id", QCP.equals, ukwoApplyId)};

        DynamicObject loanOrderBill = BusinessDataServiceHelper.loadSingle(entityName, filters);

        return loanOrderBill;
    }

    /**
     * 根据业务编号判断贷款申请订单是否存在
     *
     * @param ukwoApplyId
     * @return
     */
    public static boolean exists(String ukwoApplyId) {
        //业务编号ukwo_apply_id
        QFilter[] filters = new QFilter[]{new QFilter("ukwo_apply_id", QCP.equals, ukwoApplyId)};

        boolean isExist = QueryServiceHelper.exists(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME, filters);

        return isExist;

    }

    /**
     * 根据主键查询
     *
     * @param pkId
     * @return
     */
    public static DynamicObject queryByPK(long pkId, String selectProperties) {
        String entityName = FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME;

        QFilter[] filters = new QFilter[]{new QFilter("id", QCP.equals, pkId)};

        DynamicObject loanOrderBill = BusinessDataServiceHelper.loadSingle(entityName, selectProperties, filters);

        return loanOrderBill;
    }

    /**
     * 将申请单、单据体以及单据体附件字段一并查出
     *
     * @param pkId
     * @return
     */
    public static DynamicObject queryWithAttFieldByPK(Object pkId) {
        String entityName = FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME;

        QFilter[] filters = new QFilter[]{new QFilter("id", QCP.equals, pkId)};

        DynamicObject loanOrderBill = BusinessDataServiceHelper.loadSingle(entityName, SELECT_PROPERTIES_WITH_PANEL_FIELD, filters);

        return loanOrderBill;
    }

    /**
     * 获取数据总数
     *
     * @param dbRoute
     * @param sb
     * @return
     */
    public static int getDataCount(DBRoute dbRoute, String sql, Object[] params) {
        int dataCount = DB.query(dbRoute, sql, params, new ResultSetHandler<Integer>() {
            @Override
            public Integer handle(ResultSet resultSet) throws Exception {
                int dataCount = 0;
                if (resultSet.next()) {
                    dataCount = resultSet.getInt(1);
                }
                return dataCount;
            }
        });


        return dataCount;

    }


    public static int queryLoanStatusTaskItemCount(List<Integer> finalStatus) {
        String countSql = "select count(1) data_count from tk_ukwo_xxd_loan_apply_bi where fk_ukwo_order_status_code is null or fk_ukwo_order_status_code not in(?,?,?,?)";
        Object[] params = finalStatus.toArray();
        int dataCount = getDataCount(DBRoute.of(FormConstant.SECD_ROUTE_KEY), countSql, params);
        return dataCount;
    }


    public static DynamicObject[] queryLoanStatusTaskItemByPage(String entityName, String selectProperties, QFilter[] filters, String orderBy, int pageIndex, int pagesize) {
        DynamicObject[] orderList = BusinessDataServiceHelper.load(entityName, selectProperties, filters, orderBy, pageIndex, pagesize);

        if (orderList == null) {
            orderList = new DynamicObject[]{};
        }
        return orderList;
    }

    /**
     * 更新备注信息
     *
     * @param applyId
     * @param noteContent
     * @param noteTime
     * @return
     */
    public static void updateNote(String applyId, String noteContent, String noteTime) {
        DBRoute dbRoute = DBRoute.of(FormConstant.SECD_ROUTE_KEY);
        //TODO 业务单号添加索引
        String sql = "update tk_ukwo_xxd_loan_apply_bi set fk_ukwo_note_content = ?,fk_ukwo_note_time = ? where fk_ukwo_apply_id = ?"; //modifytime=?
        Date modifytime = new Date();

        Object[] params = new Object[]{noteContent, noteTime,applyId};//modifytime

        int rows = DB.update(dbRoute, sql, params);

        if (rows <= 0) {
            throw new RuntimeException("更新备注信息失败");
        }
    }

    /**
     * 根据统一社会信用代码查询最新一条贷款申请
     *
     * @param ukwoUniSociCreditCode 统一社会信用代码
     * @return
     */
    public DynamicObject queryLatestOneByCreditCode(String ukwoUniSociCreditCode) {
        String entityName = FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME;
        String selectProperties = "id,billno,ukwo_apply_id,ukwo_uni_soci_credit_code,ukwo_region_code,ukwo_project_name,ukwo_project_manager,ukwo_create_time";
        QFilter[] filters = new QFilter[]{new QFilter("ukwo_uni_soci_credit_code", QCP.equals, ukwoUniSociCreditCode)};;
        //获取查询订单列表时,按创建时间降序排列
        String orderBy = "ukwo_create_time DESC";
        int top = 1;

        DynamicObject latest = null;
        DynamicObject[] orderBillList = BusinessDataServiceHelper.load(entityName,selectProperties,filters,orderBy,top);
        if(orderBillList != null && orderBillList.length > 0){
            latest = orderBillList[0];
        }

        return latest;

    }
}
