package com.cxjt.xxd.component;

import com.alibaba.fastjson.JSONObject;
import com.cxjt.xxd.config.RemoteConfig;
import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.enums.JrcsErrorCodeEnum;
import com.cxjt.xxd.enums.SendStatusEnum;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.util.JsonUtil;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SendSmsComponent {
    private static final Log LOGGER = LogFactory.getLog(SendSmsComponent.class);

    private static final String JRCS_PROTOCOL = RemoteConfig.getJrcsProtocol();
    private static final String HOST = RemoteConfig.getJrcsHost();
    private static final int PORT = RemoteConfig.getJrcsPort();
    private static final String PATH = RemoteConfig.getJrcsSendCxSmsPath();
    private static final String URL = JRCS_PROTOCOL + "://" + HOST + ":" + PORT + PATH;

    private static final String ENTITY_NAME = FormConstant.SEND_SMS_ENTITY_NAME;

    public static void send(DynamicObject message) {
        String sendStatus = message.getString("ukwo_send_status"); //发送状态
        if (SendStatusEnum.SUCCESS.getCode().equals(sendStatus)) {
            return;
        }

        String mobilePhone = message.getString("ukwo_mobile_phone");   //手机号码
        String smsContent = message.getString("ukwo_sms_content"); //短信内容
        Integer retryCount = message.getInt("ukwo_retry_count"); //重试次数

        JSONObject respData = call(mobilePhone, smsContent);
        String code = respData.getString("code");
        String msg = respData.getString("msg");
        message.set("ukwo_retry_count", SendStatusEnum.FAILED.getCode().equals(sendStatus) ? retryCount + 1 : retryCount);
        message.set("ukwo_send_time", new Date());
        if (JrcsErrorCodeEnum.SUCCESS.getCode().equals(code)) {
            message.set("ukwo_send_status", SendStatusEnum.SUCCESS.getCode());
            message.set("ukwo_error_msg", null);
        } else {
            message.set("ukwo_send_status", SendStatusEnum.FAILED.getCode());
            message.set("ukwo_error_msg", msg);
        }
        SaveServiceHelper.update(new DynamicObject[]{message});

    }

    /**
     * 调用金融超市发送短信接口
     *
     * @param mobilePhone 手机号码
     * @param smsContent  短信内容
     * @return
     */
    public static JSONObject call(String mobilePhone, String smsContent) {
        JSONObject result = null;
        try {
            Map<String, Object> reqParam = new HashMap<>();
            Map<String, String> body = new HashMap<>();
            body.put("mobiles", mobilePhone);
            body.put("content", smsContent);
            reqParam.put("body", body);
            String responseStr = HttpService.getService().doPostByHttpClient(URL, JsonUtil.format(reqParam));
            result = JSONObject.parseObject(responseStr);
        } catch (Exception e) {
            throw new RuntimeException("调用金融超市发送短信接口出现异常", e);
        }
        return result;
    }

    /**
     * 获取指定时间段发送成功的短信记录
     *
     * @param applyId     贷款申请编码
     * @param mobilePhone 手机号码
     * @param smsType     短信类型
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @return
     */
    public static DynamicObject[] getSuccSmsByTimeScope(String applyId, String mobilePhone, String smsType, Date startTime, Date endTime) {
        String selectProperties = "id,ukwo_apply_id,ukwo_mobile_phone,ukwo_sms_type,ukwo_sms_content,ukwo_send_status,ukwo_error_msg,ukwo_retry_count,ukwo_send_time";
        String orderBy = "ukwo_send_time DESC";
        int top = 5;
        QFilter[] filters = new QFilter[]{new QFilter("ukwo_send_time", QCP.large_equals, startTime), new QFilter("ukwo_send_time", QCP.less_equals, endTime), new QFilter("ukwo_sms_type", QCP.equals, smsType), new QFilter("ukwo_send_status", QCP.equals, SendStatusEnum.SUCCESS.getCode()), new QFilter("ukwo_mobile_phone", QCP.equals, mobilePhone), new QFilter("ukwo_apply_id", QCP.equals, applyId)};
        return BusinessDataServiceHelper.load(ENTITY_NAME, selectProperties, filters, orderBy, top);
    }

}
