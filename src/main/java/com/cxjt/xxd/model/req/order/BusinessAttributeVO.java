package com.cxjt.xxd.model.req.order;

import kd.bos.openapi.common.custom.annotation.ApiModel;
import kd.bos.openapi.common.custom.annotation.ApiParam;

import java.io.Serializable;

/**
 * 业务属性
 */
@ApiModel
public class BusinessAttributeVO implements Serializable {


    @ApiParam("客户性质,默认为“企业”,无需上送")
    private String ukwoCustNature = "企业";


    @ApiParam("产品名称,默认为“鑫湘e贷”,无需上送")
    private String ukwoProdName = "鑫湘e贷";


    @ApiParam("业务类型,默认为“300万及以下业务”,无需上送")
    private String ukwoBusinessType = "300万及以下业务";

    public String getUkwoCustNature() {
        return ukwoCustNature;
    }

    public void setUkwoCustNature(String ukwoCustNature) {
        this.ukwoCustNature = ukwoCustNature;
    }

    public String getUkwoProdName() {
        return ukwoProdName;
    }

    public void setUkwoProdName(String ukwoProdName) {
        this.ukwoProdName = ukwoProdName;
    }

    public String getUkwoBusinessType() {
        return ukwoBusinessType;
    }

    public void setUkwoBusinessType(String ukwoBusinessType) {
        this.ukwoBusinessType = ukwoBusinessType;
    }
}
