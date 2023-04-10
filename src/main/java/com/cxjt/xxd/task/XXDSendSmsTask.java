package com.cxjt.xxd.task;

import com.alibaba.fastjson.JSONObject;
import com.cxjt.xxd.component.SendSmsComponent;
import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.enums.JrcsErrorCodeEnum;
import com.cxjt.xxd.enums.SendStatusEnum;
import com.cxjt.xxd.helper.PageHelper;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class XXDSendSmsTask extends AbstractTask {
    private final Log LOGGER = LogFactory.getLog(this.getClass());

    private static final Integer RETRY_COUNT = 2;   //短信发送重试次数

    //待发送短信类型
    private static final List<String> smsTypeList = Arrays.asList(new String[]{FormConstant.SEND_AUDIT_PROJECT_MRG_TYPE,FormConstant.SEND_AUDIT_CUST_MRG_TYPE});

    /**
     * 定时发送 发送失败的短信
     *
     * @param requestContext
     * @param map
     * @throws KDException
     */
    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        LOGGER.info("====================开始执行短信发送任务====================");
        String entityName = FormConstant.SEND_SMS_ENTITY_NAME;
        int totalCount = 0;
        DataSet dataSet = null;

        try {
            //查询目前数据库中一共有多少条未发送或发送失败的数据
            String countSql = "SELECT COUNT(1) total_count FROM tk_ukwo_xxd_send_sms" + " WHERE fk_ukwo_sms_type in ('" +FormConstant.SEND_AUDIT_PROJECT_MRG_TYPE+"','" +FormConstant.SEND_AUDIT_CUST_MRG_TYPE   +"') AND fk_ukwo_retry_count <= " + RETRY_COUNT + " AND fk_ukwo_send_status IN ('" + SendStatusEnum.UNSENT.getCode() + "', '" + SendStatusEnum.FAILED.getCode() + "')";
            dataSet = DB.queryDataSet(XXDSendSmsTask.class.getName(), DBRoute.of("secd"), countSql);
            if (dataSet.hasNext()) {
                Row row = dataSet.next();
                totalCount = ((Long) row.get("total_count")).intValue();
            }
        } catch (Exception e) {
            LOGGER.error("[短信发送任务]获取短信总数出现异常,信息如下:{}", e);
        } finally {
            if (dataSet != null) {
                dataSet.close();
                //dataSet = null;
            }
        }

        if (totalCount <= 0) {
            return;
        }
        int pageSize = 10;
        int pageCount = PageHelper.getPageCount(totalCount, pageSize);   //总页数

        String selectProperties = "id,ukwo_apply_id,ukwo_mobile_phone,ukwo_sms_type,ukwo_sms_content,ukwo_send_status,ukwo_error_msg,ukwo_retry_count,ukwo_send_time,ukwo_create_time";
        String orderBy = "ukwo_create_time ASC";
        QFilter[] filters = new QFilter[]{new QFilter("ukwo_sms_type", QCP.in,smsTypeList), new QFilter("ukwo_send_status", QCP.in, new String[]{SendStatusEnum.UNSENT.getCode(), SendStatusEnum.FAILED.getCode()}), new QFilter("ukwo_retry_count", QCP.less_equals, RETRY_COUNT)};

        int successCount = 0;
        for (int i = 0; i < pageCount; i++) {
            try {
                DynamicObject[] messages = BusinessDataServiceHelper.load(entityName, selectProperties, filters, orderBy, pageSize);
                // 查询数据库中发送状态为失败的短信
                if (ArrayUtils.isEmpty(messages)) {
                    LOGGER.info("暂未获取到符合要求的短信信息");
                    continue;
                }
                for (DynamicObject message : messages) {
                    String applyId = message.getString("ukwo_apply_id");   //业务编号
                    String mobilePhone = message.getString("ukwo_mobile_phone");   //手机号码
                    try {
                        SendSmsComponent.send(message);
                        successCount++;
                    } catch (Exception e) {
                        LOGGER.error("短信发送异常,参数如下:申请编号:[{}],手机号:[{}],异常信息:{}", applyId, mobilePhone, e);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("获取短信信息列表出现异常,请联系系统管理员,异常信息如下:{}", e);
                throw new KDException("获取短信信息列表出现异常,请联系系统管理员");
            }
        }

        LOGGER.info("====================短信发送任务执行完毕,共成功发送短信[{}]条====================",successCount);
    }
}
