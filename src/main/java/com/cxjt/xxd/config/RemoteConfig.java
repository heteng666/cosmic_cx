package com.cxjt.xxd.config;

import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

/**
 * 从配置文件自动注入,配置路径:mc管理中心 >>基础数据维护 >>环境公共配置项
 */
public class RemoteConfig {

    private final static Log logger = LogFactory.getLog(RemoteConfig.class);

    //public static final String JRCS_HOST  = "192.168.251.7";
    //public static final int JRCS_PORT  = 8085;

    private static String JRCS_HOST = System.getProperty("JRCS_HOST");
    private static String JRCS_PORT = System.getProperty("JRCS_PORT");
    //担保费确认结果通知地址
    private static String JRCS_PAYMENT_NOTICE_PATH = System.getProperty("JRCS_PAYMENT_NOTICE_PATH");
    //尽调结果通知地址
    private static String JRCS_GUARANTEE_NOTICE_PATH = System.getProperty("JRCS_GUARANTEE_NOTICE_PATH");
    //短信发送地址
    private static String JRCS_SEND_CX_SMS_PATH = System.getProperty("JRCS_SEND_CX_SMS_PATH");

    //获取订单状态地址
    private static String JRCS_GET_LOAN_PROCESS_PATH = System.getProperty("JRCS_GET_LOAN_PROCESS_PATH");

    //请求协议
    private static String JRCS_PROTOCOL = System.getProperty("JRCS_PROTOCOL");

    static {
        logger.info("========================金融超市服务器IP地址为:{}", JRCS_HOST);
        logger.info("========================金融超市服务器端口号为:{}", JRCS_PORT);
        logger.info("========================金融超市担保费确认结果通知地址为:{}", JRCS_PAYMENT_NOTICE_PATH);
        logger.info("========================金融超市尽调结果通知地址为:{}", JRCS_GUARANTEE_NOTICE_PATH);
        logger.info("========================金融超市短信发送地址为:{}", JRCS_SEND_CX_SMS_PATH);
        logger.info("========================金融超市获取订单状态地址为:{}", JRCS_GET_LOAN_PROCESS_PATH);
        logger.info("========================金融超市请求协议为:{}", JRCS_PROTOCOL);
    }

    public static String getJrcsHost() {
        if (StringUtils.isBlank(JRCS_HOST)) {
            throw new RuntimeException("暂未获取到IP地址,请联系管理员检查mc环境公共项配置");
        }

        return JRCS_HOST;
    }

    public static int getJrcsPort() {
        if (StringUtils.isBlank(JRCS_PORT)) {
            throw new RuntimeException("暂未获取到端口号,请联系管理员检查mc环境公共项配置");
        }
        return Integer.parseInt(JRCS_PORT);
    }

    public static String getJrcsProtocol() {
        if (StringUtils.isBlank(JRCS_PROTOCOL)) {
            throw new RuntimeException("暂未获取到请求协议,请联系管理员检查mc环境公共项配置");
        }

        return JRCS_PROTOCOL;
    }

    public static String getJrcsPaymentNoticePath() {
        if (StringUtils.isBlank(JRCS_PAYMENT_NOTICE_PATH)) {
            throw new RuntimeException("暂未获取到担保费确认结果通知地址,请联系管理员检查mc环境公共项配置");
        }
        return JRCS_PAYMENT_NOTICE_PATH;
    }

    public static String getJrcsGuaranteeNoticePath() {
        if (StringUtils.isBlank(JRCS_GUARANTEE_NOTICE_PATH)) {
            throw new RuntimeException("暂未获取到尽调结果通知地址,请联系管理员检查mc环境公共项配置");
        }
        return JRCS_GUARANTEE_NOTICE_PATH;
    }

    public static String getJrcsSendCxSmsPath() {
        if (StringUtils.isBlank(JRCS_SEND_CX_SMS_PATH)) {
            throw new RuntimeException("暂未获取到短信发送地址,请联系管理员检查mc环境公共项配置");
        }
        return JRCS_SEND_CX_SMS_PATH;
    }

    public static String getJrcsGetLoanProcessPath() {
        if (StringUtils.isBlank(JRCS_GET_LOAN_PROCESS_PATH)) {
            throw new RuntimeException("暂未获取到获取订单状态地址,请联系管理员检查mc环境公共项配置");
        }
        return JRCS_GET_LOAN_PROCESS_PATH;
    }


}
