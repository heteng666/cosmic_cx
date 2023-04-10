package com.cxjt.xxd.model.req.guarant;

import kd.bos.openapi.common.custom.annotation.ApiModel;
import kd.bos.openapi.common.custom.annotation.ApiParam;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 担保费信息
 */
@ApiModel
public class GuaranteeFeeReq implements Serializable {

    @ApiParam(value = "业务编号",required = true,example = "230112430700000009")
    private String ukwoApplyId;

    @ApiParam(value = "企业名称",required = true,example = "常德财鑫数字科技有限公司")
    private String ukwoCompanyName;

    @ApiParam(value = "担保金额(单位:万元)",required = true,example = "300.00")
    private BigDecimal ukwoGuarantAmount;

    @ApiParam(value = "担保费(单位:万元)",required = true,example = "1.50")
    private BigDecimal ukwoGuaranteeFeeAmount;

    @ApiParam(value = "所属区域,企业注册地所在区域,金融超市侧提供各环境枚举值",required = true,example = "430702")
    private long ukwoRegionCode;

    public String getUkwoApplyId() {
        return ukwoApplyId;
    }

    public void setUkwoApplyId(String ukwoApplyId) {
        this.ukwoApplyId = ukwoApplyId;
    }

    public String getUkwoCompanyName() {
        return ukwoCompanyName;
    }

    public void setUkwoCompanyName(String ukwoCompanyName) {
        this.ukwoCompanyName = ukwoCompanyName;
    }

    public BigDecimal getUkwoGuarantAmount() {
        return ukwoGuarantAmount;
    }

    public void setUkwoGuarantAmount(BigDecimal ukwoGuarantAmount) {
        this.ukwoGuarantAmount = ukwoGuarantAmount;
    }

    public BigDecimal getUkwoGuaranteeFeeAmount() {
        return ukwoGuaranteeFeeAmount;
    }

    public void setUkwoGuaranteeFeeAmount(BigDecimal ukwoGuaranteeFeeAmount) {
        this.ukwoGuaranteeFeeAmount = ukwoGuaranteeFeeAmount;
    }


    public long getUkwoRegionCode() {
        return ukwoRegionCode;
    }

    public void setUkwoRegionCode(Long ukwoRegionCode) {
        this.ukwoRegionCode = ukwoRegionCode;
    }
}
