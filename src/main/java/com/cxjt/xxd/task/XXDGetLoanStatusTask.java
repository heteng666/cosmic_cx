package com.cxjt.xxd.task;


import com.cxjt.xxd.component.LoanApplyStatusComponent;
import com.cxjt.xxd.constants.FormConstant;


import com.cxjt.xxd.dao.XXDLoanApplyBillDao;
import com.cxjt.xxd.helper.PageHelper;
import com.cxjt.xxd.plugin.pc.list.XXDGetLoanApplyStatusBillListPlugin;
import com.cxjt.xxd.service.XXDLoanApplicationService;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;

import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;

import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;

import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import org.apache.commons.lang.StringUtils;


import java.util.List;
import java.util.Map;

/**
 * 获取订单状态调度任务以及<<担保及放款通知书>>
 * AcquireTimerJobsTask
 */
public class XXDGetLoanStatusTask extends AbstractTask {

    private final Log logger = LogFactory.getLog(this.getClass());

    //银行初审通过之后,才走担保流程,[担保审核拒绝][银行复核拒绝][已还清]等处于终态的单据,可不必同步状态[已与产品经理确认终态]
    private static final List<Integer> finalStatus = XXDLoanApplicationService.finalStatus;

    //每页取20条
    private static final int PAGE_SIZE = 20;


    /**
     * 分页获取贷款申请订单,请求金融超市接口,获取订单状态并更新数据库
     *
     * @param requestContext
     * @param map
     * @throws KDException
     */
    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        logger.info("====================开始执行获取订单状态任务====================");

        String entityName = FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME;
        String selectProperties = LoanApplyStatusComponent.SELECT_PROPERTIES;
        //QFilter[] filters = new QFilter[]{new QFilter("ukwo_apply_id", QCP.equals, "230116430700000010")};

        /*
        int dataCount = 0;
        DataSet dataSet = null;
        try {
            String countSql = "select count(1) data_count from tk_ukwo_xxd_loan_apply_bi where fk_ukwo_order_status_code is null or fk_ukwo_order_status_code not in(" + StringUtils.join(finalStatus, ",") + ")";
            dataSet = DB.queryDataSet(XXDGetLoanStatusTask.class.getName(), DBRoute.of(FormConstant.SECD_ROUTE_KEY), countSql);
            if (dataSet.hasNext()) {
                Row row = dataSet.next();
                dataCount = ((Long) row.get("data_count")).intValue();
            }
        } catch (Exception e) {
            logger.error("====================[订单状态任务]获取订单总数出现异常,信息如下:{}====================", e);
        } finally {
            if (dataSet != null) {
                dataSet.close();
            }
        }*/
        int dataCount = 0;
        try{
            dataCount = XXDLoanApplyBillDao.queryLoanStatusTaskItemCount(finalStatus);
            logger.info("====================获取订单状态任务获取到待执行数据数:{}条====================",dataCount);
        }catch (Exception e) {
            logger.error("====================[订单状态任务]获取订单总数出现异常,信息如下:{}====================", e);
        }


        if (dataCount <= 0) {
            return;
        }

        QFilter isNull = QFilter.isNull("ukwo_order_status_code");
        QFilter notIn = new QFilter("ukwo_order_status_code", QCP.not_in, finalStatus);
        QFilter filter = (isNull.or(notIn));
        QFilter[] filters = new QFilter[]{filter};

        //新增创建时间字段,按时间降序排列
        String orderBy = "ukwo_create_time DESC";
        //int top = PAGE_SIZE;

        //DynamicObjectCollection orderList = null;
        int pageCount = PageHelper.getPageCount(dataCount, PAGE_SIZE);

        int successCount = 0;
        int failedCount = 0;

        for (int i = 0; i < pageCount; i++) {
            try {
                //订单状态达到终态时不同步
                //DynamicObjectCollection orderList = QueryServiceHelper.query(entityName, selectProperties, filters, orderBy, top);
                DynamicObject[] orderList = XXDLoanApplyBillDao.queryLoanStatusTaskItemByPage(entityName, selectProperties, filters, orderBy, i,PAGE_SIZE);

                //循环遍历贷款申请单
                for(int j = 0; j < orderList.length; j++){
                    DynamicObject orderBill = orderList[j];
                    String applyId = (String) orderBill.get("ukwo_apply_id");
                    try {
                        logger.info("开始获取第[{}]页第[{}]条单据状态[{}].....", i+1, j+1, applyId);
                        boolean result = LoanApplyStatusComponent.process(orderBill);
                        if(result){
                            successCount++;
                        }else{
                            failedCount++;
                        }
                        logger.info("成功获取第[{}]页第[{}]条单据状态[{}].....", i+1, j+1, applyId);
                    } catch (Exception e) {
                        logger.error("调用获取订单状态接口出现异常,请联系系统管理员,请求参数:applyId={},异常信息如下:{}", applyId, e);
                    }
                }

            } catch (Exception e) {
                logger.error("获取贷款订单列表出现异常,请联系系统管理员,异常信息如下:{}", e);
            }

        }

        logger.info("====================获取订单状态任务执行完毕,共成功获取[{}]条申请单状态,JRCS失败返回[{}]条====================",successCount,failedCount);
    }

}
