package com.cxjt.xxd.plugin.pc.list;

import com.alibaba.fastjson.JSONObject;
import com.cxjt.xxd.component.SendSmsComponent;
import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.enums.JrcsErrorCodeEnum;
import com.cxjt.xxd.enums.SendStatusEnum;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.list.IListView;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import org.apache.commons.collections.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * 手动发送短信,作为定时任务发送短信的补偿机制,不受重试次数的限制
 */
public class XXDSendSmsListPlugin extends AbstractBillPlugIn {
    private final Log LOGGER = LogFactory.getLog(this.getClass());

    private static final String OPERATE_KEY_SEND_SMS = "sendsms";

    //因未添加分布式锁,为了降低与[定时任务]的并发访问,此处控制手动发送最多发送两条
    private static final Integer MAX_SEND_SIZE = 2;

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {

        if (!OPERATE_KEY_SEND_SMS.equals(args.getOperateKey())) {
            return;
        }

        OperationResult opResult = args.getOperationResult();
        if (!(opResult != null && opResult.isSuccess())) {
            return;
        }


        List<Object> pkIds = opResult.getSuccessPkIds();

        if (CollectionUtils.isEmpty(pkIds)) {
            this.getView().showErrorNotification("暂未获取到选中的短信信息");
            return;
        }

        if (pkIds.size() > MAX_SEND_SIZE) {
            LOGGER.error("暂未获取到符合要求的短信信息");
            this.getView().showErrorNotification("手动发送最多只支持发送【" + MAX_SEND_SIZE + "】条");
            return;
        }

        String entityName = FormConstant.SEND_SMS_ENTITY_NAME;
        String selectProperties = "id,ukwo_apply_id,ukwo_mobile_phone,ukwo_sms_type,ukwo_sms_content,ukwo_send_status,ukwo_error_msg,ukwo_retry_count,ukwo_send_time";
        QFilter[] filters = new QFilter[]{new QFilter("id", QCP.in, pkIds)};
        DynamicObject[] messages = null;
        try {
            messages = BusinessDataServiceHelper.load(entityName, selectProperties, filters);
        } catch (Exception e) {
            LOGGER.error("获取短信信息列表出现异常,请联系系统管理员,异常信息如下:{}", e);
            this.getView().showErrorNotification("获取短信信息列表出现异常,请联系系统管理员");
            return;
        }


        int failedCount = 0;
        for (DynamicObject message : messages) {
            String applyId = message.getString("ukwo_apply_id");   //手机号码
            String mobilePhone = message.getString("ukwo_mobile_phone");   //手机号码
            try {
                SendSmsComponent.send(message);
            } catch (Exception e) {
                failedCount++;
                LOGGER.error("短信发送异常,参数如下:申请编号:[{}],手机号:[{}],异常信息:{}", applyId, mobilePhone, e);
            }
        }
        //刷新列表
        IListView listView = (IListView) this.getView();
        listView.refresh();
        if (failedCount > 0) {
            this.getView().showErrorNotification(String.format("手动发送短信共[%d]条失败,请联系系统管理员", failedCount));
        }


    }
}
