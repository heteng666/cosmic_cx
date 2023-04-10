package com.cxjt.xxd.config;

import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

/**
 * 从配置文件自动注入,配置路径:mc管理中心 >>基础数据维护 >>环境公共配置项
 */
public class XXDConfig {

    private final static Log logger = LogFactory.getLog(XXDConfig.class);

    //短信模板[验证反担保人的手机号时发送]
    //[常德金融超市]${verificationCode},请在${minutes}分钟内完成验证.
    private static String SMS_VERIFICATION_CODE_TEMPLDATE = System.getProperty("XXD_SMS_VERIFICATION_CODE_TEMPLDATE");

    //短信模板[确认订单归属项目经理后发送]
    //[常德金融超市]尊敬的客户,您申请的"鑫湘e贷"预审已通过审核,稍后会有项目经理(${content})联系您进行线下复核,请保持电话畅通。
    private static String SMS_ORDER_CONFIRM_PRO_MGR_TEMPLDATE = System.getProperty("XXD_SMS_ORDER_CONFIRM_PRO_MGR_TEMPLDATE");

    //短信模板[当转派并确认项目经理后，给客户经理的手机发送短信]
    //【常德金融超市】【${companyName}】申请鑫湘e贷产品已通过预授信，担保方项目经理为${proName}（电话${proPhone}）
    private static String SMS_CONFIRM_PRO_CUST_MGR_TEMPLDATE = System.getProperty("XXD_SMS_CONFIRM_PRO_CUST_MGR_TEMPLDATE");

    //短信验证码有效期,单位:秒,初始值:300秒
    private static String SMS_VERIFY_CODE_VALID_SECONDS = System.getProperty("XXD_SMS_VERIFY_CODE_VALID_SECONDS");

    //风险部组织机构ID
    private static final String RISK_DEPT_ID = System.getProperty("XXD_RISK_DEPT_ID");

    //财科担保组织机构ID
    private static final String CKDB_ORG_ID = System.getProperty("XXD_CKDB_ORG_ID");

    public static String getSmsVerificationCodeTempldate() {
        if (StringUtils.isBlank(SMS_VERIFICATION_CODE_TEMPLDATE)) {
            throw new RuntimeException("暂未获取到短信模板,请联系管理员检查mc环境公共项配置");
        }
        return SMS_VERIFICATION_CODE_TEMPLDATE;
    }

    public static String getSmsOrderConfirmProMgrTempldate() {
        if (StringUtils.isBlank(SMS_ORDER_CONFIRM_PRO_MGR_TEMPLDATE)) {
            throw new RuntimeException("暂未获取到短信模板,请联系管理员检查mc环境公共项配置");
        }

        return SMS_ORDER_CONFIRM_PRO_MGR_TEMPLDATE;
    }

    public static String getSmsConfirmProCustMgrTempldate(){
        if (StringUtils.isBlank(SMS_CONFIRM_PRO_CUST_MGR_TEMPLDATE)) {
            throw new RuntimeException("暂未获取到客户经理短信模板,请联系管理员检查mc环境公共项配置");
        }

        return SMS_CONFIRM_PRO_CUST_MGR_TEMPLDATE;
    }

    /**
     * 获取智慧财鑫【风险部】组织机构ID
     *
     * @return
     */
    public static long getRiskDeptId() {
        if (StringUtils.isBlank(RISK_DEPT_ID)) {
            throw new RuntimeException("暂未获取到机构配置,请联系管理员检查mc环境公共项配置");
        }

        return Long.parseLong(RISK_DEPT_ID);
    }

    /**
     * 获取智慧财鑫【财科担保】组织机构ID
     *
     * @return
     */
    public static long getCkdbOrgId() {
        if (StringUtils.isBlank(CKDB_ORG_ID)) {
            throw new RuntimeException("暂未获取到机构配置,请联系管理员检查mc环境公共项配置");
        }

        return Long.parseLong(CKDB_ORG_ID);
    }

    public static int getSmsVerifyCodeValidSeconds() {
        if (StringUtils.isBlank(SMS_VERIFY_CODE_VALID_SECONDS)) {
            throw new RuntimeException("暂未获取到短信验证码有效期配置,请联系管理员检查mc环境公共项配置");
        }
        return Integer.parseInt(SMS_VERIFY_CODE_VALID_SECONDS);
    }

    public static int getSmsVerifyCodeValidMinutes() {
        // 将验证码由秒换算成分钟
        Integer seconds = getSmsVerifyCodeValidSeconds();
        return seconds / 60;
    }
}
