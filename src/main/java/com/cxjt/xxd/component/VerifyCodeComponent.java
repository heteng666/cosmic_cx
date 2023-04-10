package com.cxjt.xxd.component;

import com.cxjt.xxd.config.XXDConfig;
import com.cxjt.xxd.constants.FormConstant;
import kd.bos.cache.CacheFactory;
import kd.bos.cache.DistributeSessionlessCache;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Calendar;
import java.util.Date;

public class VerifyCodeComponent {
    private static final Log LOGGER = LogFactory.getLog(VerifyCodeComponent.class);

    private static final Integer VERIFY_CODE_LENGTH = 6;    //验证码长度

    private static final int VERIFY_CODE_VALID_SECONDS = XXDConfig.getSmsVerifyCodeValidSeconds();

    public static String generateVerifyCode() {
        return RandomStringUtils.randomNumeric(VERIFY_CODE_LENGTH);
    }

    /**
     * 获取缓存中的验证码（先从redis中取，如果连接不上redis或者redis中没有拿到就从数据库里取）
     *
     * @param operateCode 操作编码
     * @param applyId     贷款申请编码
     * @param mobilePhone 手机号码
     * @param verifyCode  用户输入的验证码
     * @return
     */
    public static String getCacheVerifyCode(String operateCode, String applyId, String mobilePhone, String verifyCode) {
        String cacheVerifyCode = null;
        DistributeSessionlessCache cache = null;
        try {
            cache = CacheFactory.getCommonCacheFactory().getDistributeSessionlessCache("customRegion");
        } catch (Exception e) {
            LOGGER.error("Redis服务连接异常：" + e.getMessage());
        }
        if (cache != null) {
            String cacheKey = applyId + ":" + operateCode + ":" + mobilePhone; //缓存key
            cacheVerifyCode = cache.get(cacheKey);
            if (StringUtils.isNotEmpty(cacheVerifyCode)) {
                return cacheVerifyCode;
            }
        }
        // 如果未连接上redis或者没有在redis中拿到缓存的验证码，从数据库读取指定时间段范围内的验证码发送记录
        Calendar calendar = Calendar.getInstance();
        Date endTime = calendar.getTime();  //结束时间
        calendar.add(Calendar.SECOND, -VERIFY_CODE_VALID_SECONDS);
        Date startTime = calendar.getTime();    //开始时间
        DynamicObject[] messages = SendSmsComponent.getSuccSmsByTimeScope(applyId, mobilePhone, FormConstant.SEND_SMS_VALIDATE_CODE_TYPE, startTime, endTime);
        if (messages != null && messages.length > 0) {
            String smsContent = messages[0].getString("ukwo_sms_content");
            cacheVerifyCode = smsContent.contains(verifyCode) ? verifyCode : null;
        }
        return cacheVerifyCode;
    }


}
