package com.cxjt.xxd.component;

import com.cxjt.xxd.config.RemoteConfig;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;

public class EnterpriseVerificationComponent {
    private static final Log LOGGER = LogFactory.getLog(EnterpriseVerificationComponent.class);

    private static final String JRCS_PROTOCOL = RemoteConfig.getJrcsProtocol();

    private static final String HOST = RemoteConfig.getJrcsHost();

    private static final int PORT = RemoteConfig.getJrcsPort();

    private static final String URL = JRCS_PROTOCOL + "://" + HOST + ":" + PORT + "/fins-precloud/tencentCloud/VerifyEnterpriseFourFactors.do";

    /**
     * 请求[金融超市][企业四要素校验接口]
     *
     * @param idCard         法人身份证号码
     * @param name           法人姓名
     * @param socialCode     统一社会信用代码
     * @param enterpriseName 企业名称
     * @return
     */
    public static String execute(String idCard, String name, String socialCode, String enterpriseName) throws Exception {
        Map<String, Object> reqParam = new HashMap<String, Object>();
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("RealName", name);
        body.put("IdCard", idCard);
        body.put("EnterpriseName", enterpriseName);
        body.put("EnterpriseMark", socialCode);
        reqParam.put("body", body);
        return HttpService.getService().doPostByHttpClient(URL, JsonUtil.format(reqParam));
    }
}
