package com.cxjt.xxd.model.req.order;

import kd.bos.openapi.common.custom.annotation.ApiModel;
import kd.bos.openapi.common.custom.annotation.ApiParam;

import javax.validation.constraints.NotBlank;

/**
 * 360报告
 */
@ApiModel
public class Report360Req {
    /**
     * 常德金融超市生成的360报告PDF文件URL地址
     */
    @ApiParam(value = "常德金融超市生成的360报告PDF文件URL地址",required = true,example = "http://36.7.144.246:28080/group1/M00/20/E4/ooYBAGLeXnuAcwtaAA_8esQBq1g645.pdf")
    @NotBlank(message = "360报告文件地址不能为空")
    private String ukwo360ReportUrl;

    public String getUkwo360ReportUrl() {
        return ukwo360ReportUrl;
    }

    public void setUkwo360ReportUrl(String ukwo360ReportUrl) {
        this.ukwo360ReportUrl = ukwo360ReportUrl;
    }
}
