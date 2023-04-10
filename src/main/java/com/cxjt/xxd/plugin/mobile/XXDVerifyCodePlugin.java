package com.cxjt.xxd.plugin.mobile;

import com.alibaba.fastjson.JSONObject;
import com.cxjt.xxd.component.SendSmsComponent;
import com.cxjt.xxd.component.VerifyCodeComponent;
import com.cxjt.xxd.config.XXDConfig;
import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.constants.RegexConstant;
import com.cxjt.xxd.enums.JrcsErrorCodeEnum;
import com.cxjt.xxd.enums.SendStatusEnum;
import kd.bos.cache.CacheFactory;
import kd.bos.cache.DistributeSessionlessCache;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.KDBizException;
import kd.bos.form.IFormView;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.AbstractMobFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

public class XXDVerifyCodePlugin extends AbstractMobFormPlugin {
    private final Log LOGGER = LogFactory.getLog(this.getClass());

    private static final String[] OPERATE_KEYS = new String[]{"getverifycodegr:ukwo_signer_phone_gr", "getverifycodeqy:ukwo_signer_phone_qy"};

    private static final int VERIFY_CODE_VALID_SECONDS = XXDConfig.getSmsVerifyCodeValidSeconds();

    private static final int VERIFY_CODE_VALID_MINUTES = XXDConfig.getSmsVerifyCodeValidMinutes();

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        FormOperate formOperate = (FormOperate) args.getSource();
        String currentKey = formOperate.getOperateKey();   //当前操作控件key
        boolean flag = false;
        String controlKey = null;
        for (String key : OPERATE_KEYS) {
            if (currentKey.equals(key.split(":")[0])) {
                flag = true;
                controlKey = key.split(":")[1];
                break;
            }
        }
        if (flag) {
            String verifyCode = VerifyCodeComponent.generateVerifyCode();   //短信验证码
            String phone = this.getModel().getValue(controlKey).toString(); //手机号码
            if (StringUtils.isEmpty(phone)) {
                this.getView().showTipNotification("请输入签署人手机号");
                args.setCancel(true);
                return;
                //throw new KDBizException("请输入签署人手机号");
            }
            if (!phone.matches(RegexConstant.MOBILE_PHONE)) {
                this.getView().showTipNotification("手机号格式不正确");
                args.setCancel(true);
                return;
            }
            IFormView parentView = this.getView().getParentView();    //当前页面的父页面
            Object object = null;
            try {
                object = parentView.getModel().getValue("ukwo_apply_id");
            } catch (Exception e) {
                LOGGER.error("申请编码获取异常：{}", e.getMessage());
            }
            String applyId = null;
            if (object != null) {
                applyId = object.toString();
                LOGGER.info("获取到业务编号【" + applyId + "】");
            } else {
                throw new KDBizException("未获取到申请编号,请联系管理员");
            }
            String cacheKey = applyId + ":" + currentKey + ":" + phone;   //缓存key
            String smsContent = XXDConfig.getSmsVerificationCodeTempldate();
            String sendStatus = null;
            String errorMsg = null;
            try {
                LOGGER.info("单据号【{}】手机号【{}】获取到验证码【{}】",applyId,phone,verifyCode);
                // 发送短信
                smsContent = smsContent.replace("${verificationCode}", verifyCode).replace("${minutes}", VERIFY_CODE_VALID_MINUTES + "");
                JSONObject respData = SendSmsComponent.call(phone, smsContent);
                String code = respData.getString("code");
                String msg = respData.getString("msg");
                if (JrcsErrorCodeEnum.SUCCESS.getCode().equals(code)) {
                    sendStatus = SendStatusEnum.SUCCESS.getCode();
                    try{
                        //将验证码写入redis缓存中
                        DistributeSessionlessCache cache = CacheFactory.getCommonCacheFactory().getDistributeSessionlessCache("customRegion");
                        cache.put(cacheKey, verifyCode, VERIFY_CODE_VALID_SECONDS);
                    }catch (Exception e){
                        LOGGER.error("将短信验证码写入Redis缓存出现异常:applyId={},phone={},异常信息如下={}",applyId,phone,e);
                    }

                } else {
                    errorMsg = msg;
                    LOGGER.error("验证码发送失败:applyId={},phone={},msg={}",applyId,phone,msg);
                    this.getView().showErrorNotification("验证码发送失败,请联系系统管理员");
                    args.setCancel(true);
                    return;
                    //throw new KDBizException("验证码发送异常：" + msg);
                }
            } catch (Exception e) {
                sendStatus = SendStatusEnum.FAILED.getCode();
                errorMsg = e.getMessage().length() >200 ?e.getMessage().substring(0,200):e.getMessage();
                LOGGER.error("验证码发送异常:applyId={},phone={},异常信息如下:{}",applyId,phone,e);
                throw new KDBizException("验证码发送异常,请联系系统管理员");
            } finally {
                this.saveMessage(applyId, phone, smsContent, sendStatus, errorMsg);
            }
        }
    }

    private void saveMessage(String applyId, String mobilePhone, String smsContent, String sendStatus, String errorMsg) {
        //IFormView parentView = this.getView().getParentView();    //当前页面的父页面
        //todo 需要判断父页面是哪个，再执行逻辑
        //if (!FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME.equals(parentView.getEntityId())) {
            //return;
        //}
        String entityName = FormConstant.SEND_SMS_ENTITY_NAME;
        DynamicObject message = BusinessDataServiceHelper.newDynamicObject(entityName);
        Date sysTime = new Date();  //系统时间
        message.set("ukwo_apply_id", applyId);
        message.set("ukwo_mobile_phone", mobilePhone);
        message.set("ukwo_sms_type", FormConstant.SEND_SMS_VALIDATE_CODE_TYPE);
        message.set("ukwo_sms_content", smsContent);
        message.set("ukwo_retry_count", 0);
        message.set("ukwo_send_status", sendStatus);
        message.set("ukwo_error_msg", errorMsg);
        message.set("ukwo_send_time", sysTime);
        message.set("ukwo_create_time", sysTime);
        //保存
        SaveServiceHelper.saveOperate(entityName, new DynamicObject[]{message});
    }
}
