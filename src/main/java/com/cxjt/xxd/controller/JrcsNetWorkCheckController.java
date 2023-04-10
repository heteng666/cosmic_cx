package com.cxjt.xxd.controller;

import com.cxjt.xxd.component.HttpService;
import com.cxjt.xxd.config.RemoteConfig;
import com.cxjt.xxd.enums.XXDErrorCodeEnum;
import com.cxjt.xxd.model.res.XXDCustomApiResult;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.custom.annotation.*;
import kd.bos.openapi.common.result.CustomApiResult;
import kd.bos.openapi.common.util.JsonUtil;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@ApiController(value = "xxd", desc = "鑫湘贷网络检查服务")
@ApiMapping(value = "/xxd/jrcs/network/check")
public class JrcsNetWorkCheckController implements Serializable {

    private final Log logger = LogFactory.getLog(this.getClass());

    private static final String JRCS_PROTOCOL = RemoteConfig.getJrcsProtocol();
    private static final String HOST = RemoteConfig.getJrcsHost();
    private static final int PORT = RemoteConfig.getJrcsPort();

    private static final String PATH = "/fins-precloud/entreGua/getPersonInfo.do?idCard=430703199307291518";

    private static final String URL = JRCS_PROTOCOL + "://" + HOST + ":" + PORT + PATH;

    @Validated
    @ApiPostMapping(value = "check", desc = "网络检查")
    public CustomApiResult<@ApiResponseBody("返回参数") String> check() {
        try {
            Map<String, Object> paraMap = new HashMap<>();
            String jsonData = JsonUtil.format(paraMap);
            String responseStr = HttpService.getService().doPostByHttpClient(URL, jsonData);
            return XXDCustomApiResult.success(XXDErrorCodeEnum.SUCCESS.getCode(), "验证成功", responseStr);
        } catch (Exception e) {
            return XXDCustomApiResult.fail(XXDErrorCodeEnum.FAILED.getCode(), e.getMessage());
        }

    }
}
