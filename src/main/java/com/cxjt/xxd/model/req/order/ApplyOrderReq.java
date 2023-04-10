package com.cxjt.xxd.model.req.order;

import kd.bos.openapi.common.custom.annotation.ApiModel;
import kd.bos.openapi.common.custom.annotation.ApiParam;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 贷款订单申请接口
 */
@ApiModel
public class ApplyOrderReq implements Serializable {

    /**
     * 金融超市侧提供服务接口,智慧财鑫侧每天获取一次,无需上送
     */
    //@ApiParam("订单状态,金融超市侧提供服务接口,智慧财鑫侧每天获取一次,无需上送")
    //private String ukwoOrderStatus;

    /**
     * 业务信息
     */
    @ApiParam(value = "业务信息", required = true)
    @NotNull
    @Valid
    private BusinessInfoReq busiInfo;

    /**
     * 360报告
     */
    @ApiParam(value = "360报告", required = true)
    private Report360Req report360;

    //反担保信息
    @ApiParam(value = "反担保信息", required = true)
    private List<GuaranteeInfoReq> guaranteeInfoList;


    public BusinessInfoReq getBusiInfo() {
        return busiInfo;
    }

    public void setBusiInfo(BusinessInfoReq busiInfo) {
        this.busiInfo = busiInfo;
    }

    public Report360Req getReport360() {
        return report360;
    }

    public void setReport360(Report360Req report360) {
        this.report360 = report360;
    }

    public List<GuaranteeInfoReq> getGuaranteeInfoList() {
        return guaranteeInfoList;
    }

    public void setGuaranteeInfoList(List<GuaranteeInfoReq> guaranteeInfoList) {
        this.guaranteeInfoList = guaranteeInfoList;
    }
}
