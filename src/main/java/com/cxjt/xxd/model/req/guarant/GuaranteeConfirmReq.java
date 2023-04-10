package com.cxjt.xxd.model.req.guarant;

import kd.bos.openapi.common.custom.annotation.ApiModel;
import kd.bos.openapi.common.custom.annotation.ApiParam;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 担保费确认请求信息
 */
@ApiModel
public class GuaranteeConfirmReq implements Serializable {

    @ApiParam(value = "担保费信息",required = true)
    private GuaranteeFeeReq guaranteeFee;

    @ApiParam(value = "汇款信息",required = true)
    private TransferInfoReq transferInfo;

    @ApiParam(value = "相关协议信息",required = true)
    private List<GuaranteeAgreementReq> agreementList;

    public GuaranteeFeeReq getGuaranteeFee() {
        return guaranteeFee;
    }

    public void setGuaranteeFee(GuaranteeFeeReq guaranteeFee) {
        this.guaranteeFee = guaranteeFee;
    }

    public TransferInfoReq getTransferInfo() {
        return transferInfo;
    }

    public void setTransferInfo(TransferInfoReq transferInfo) {
        this.transferInfo = transferInfo;
    }

    public List<GuaranteeAgreementReq> getAgreementList() {
        return agreementList;
    }

    public void setAgreementList(List<GuaranteeAgreementReq> agreementList) {
        this.agreementList = agreementList;
    }
}
