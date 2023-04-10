package com.cxjt.xxd.model.req.guarant;

import kd.bos.openapi.common.custom.annotation.ApiModel;
import kd.bos.openapi.common.custom.annotation.ApiParam;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 汇款信息
 */
@ApiModel
public class TransferInfoReq implements Serializable {

    @ApiParam(value = "汇款记录编号",required = true)
    private String ukwoRecordId;

    @ApiParam(value = "户名",required = true,example = "常德财鑫数字科技有限公司")
    private String ukwoAccountName;

    @ApiParam(value = "银行卡号",required = true,example = "3436456456565")
    private String ukwoBankCardNo;

    @ApiParam(value = "汇款金额(单位:万元)",required = true,example = "1.50")
    private BigDecimal ukwoFeeAmount;

    @ApiParam(value = "汇款时间,格式:yyyy-MM-dd",required = true,example = "2023-01-09")
    private String ukwoTranferDate;

    public String getUkwoRecordId() {
        return ukwoRecordId;
    }

    public void setUkwoRecordId(String ukwoRecordId) {
        this.ukwoRecordId = ukwoRecordId;
    }

    public String getUkwoAccountName() {
        return ukwoAccountName;
    }

    public void setUkwoAccountName(String ukwoAccountName) {
        this.ukwoAccountName = ukwoAccountName;
    }

    public String getUkwoBankCardNo() {
        return ukwoBankCardNo;
    }

    public void setUkwoBankCardNo(String ukwoBankCardNo) {
        this.ukwoBankCardNo = ukwoBankCardNo;
    }

    public BigDecimal getUkwoFeeAmount() {
        return ukwoFeeAmount;
    }

    public void setUkwoFeeAmount(BigDecimal ukwoFeeAmount) {
        this.ukwoFeeAmount = ukwoFeeAmount;
    }

    public String getUkwoTranferDate() {
        return ukwoTranferDate;
    }

    public void setUkwoTranferDate(String ukwoTranferDate) {
        this.ukwoTranferDate = ukwoTranferDate;
    }
}

