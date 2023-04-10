package com.cxjt.xxd.component;

import com.cxjt.xxd.config.RemoteConfig;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;

public class IdentityAuthenticationComponent {
    private static final Log LOGGER = LogFactory.getLog(IdentityAuthenticationComponent.class);

    private static final String JRCS_PROTOCOL = RemoteConfig.getJrcsProtocol();

    private static final String HOST = RemoteConfig.getJrcsHost();

    private static final int PORT = RemoteConfig.getJrcsPort();

    private static final String URL = JRCS_PROTOCOL + "://" + HOST + ":" + PORT + "/fins-precloud/tencentCloud/IdCardVerification.do";

    /**
     * 请求[金融超市][身份信息认证]
     *
     * @param idCard         法人身份证号码
     * @param name           法人姓名
     * @return
     */
    public static String execute(String idCard, String name) throws Exception {
        Map<String, Object> reqParam = new HashMap<String, Object>();
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("IdCard", idCard);
        body.put("Name", name);
        reqParam.put("body", body);
        return HttpService.getService().doPostByHttpClient(URL, JsonUtil.format(reqParam));
    }
}
