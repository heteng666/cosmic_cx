package com.cxjt.xxd.model.req.order;

import kd.bos.openapi.common.custom.annotation.ApiModel;
import kd.bos.openapi.common.custom.annotation.ApiParam;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


/**
 * 业务信息
 */
@ApiModel
public class BusinessInfoReq implements Serializable {
    /**
     * 业务属性
     */
   // @ApiParam("业务属性")
    //private BusinessAttributeVO busiAttribute;

    /**
     * 客户信息
     */
    @ApiParam("客户信息")
    @NotNull
    @Valid
    private CustInfoReq custInfo;


    @ApiParam("业务信息")
    private ChildBusinessInfoReq childBusinessInfoReq;



    public CustInfoReq getCustInfo() {
        return custInfo;
    }

    public void setCustInfo(CustInfoReq custInfo) {
        this.custInfo = custInfo;
    }

    public ChildBusinessInfoReq getChildBusinessInfoReq() {
        return childBusinessInfoReq;
    }

    public void setChildBusinessInfoReq(ChildBusinessInfoReq childBusinessInfoReq) {
        this.childBusinessInfoReq = childBusinessInfoReq;
    }
}
