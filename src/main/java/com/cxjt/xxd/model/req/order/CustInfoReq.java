package com.cxjt.xxd.model.req.order;

import kd.bos.openapi.common.custom.annotation.ApiModel;
import kd.bos.openapi.common.custom.annotation.ApiParam;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 客户信息
 */
@ApiModel
public class CustInfoReq implements Serializable {

    /**
     * 企业名称
     */
    @NotBlank
    @ApiParam(value = "常德金融超市推送过来的企业名称",required = true,example = "常德市财鑫数字科技有限公司")
    private String ukwoCompanyName;

    /**
     * 统一社会信用代码
     */
    @NotBlank
    @ApiParam(value = "常德金融超市推送过来的统一社会信用代码",required = true,example = "342344534543")
    private String ukwoUniSociCreditCode;

    /**
     * 法人姓名
     */
    @NotBlank
    @ApiParam(value = "常德金融超市推送过来的法人姓名",required = true,example = "张三")
    private String ukwoLegalPersonName;

    /**
     * 法人身份证号
     */
    @NotBlank
    @ApiParam(value = "常德金融超市推送过来的法人身份证号",required = true,example = "430702199909091111")
    private String ukwoLegalPersonIdcard;

    /**
     * 法人手机号
     */
    @NotBlank
    @ApiParam(value = "常德金融超市推送过来的法人手机号",required = true,example = "13812345678")
    private String ukwoLegalPersonPhone;

    /**
     * 联系人姓名
     */
    @NotBlank
    @ApiParam(value = "常德金融超市推送过来的法人姓名",required = true,example = "张三")
    private String ukwoContactName;

    /**
     * 联系人身份证号
     */
    @NotBlank
    @ApiParam(value = "常德金融超市推送过来的法人身份证号",required = true,example = "430702199909091111")
    private String ukwoContactIdcard;

    /**
     * 联系人手机号
     */
    @NotBlank
    @ApiParam(value = "常德金融超市推送过来的法人手机号",required = true,example = "13812345678")
    private String ukwoContactPhone;

    /**
     * 所属区域
     */
    @NotNull
    @ApiParam(value ="常德金融超市推送过来的区域编码,金融超市侧提供枚举值",required = true,example = "430702")
    private long ukwoRegionCode;

    public String getUkwoCompanyName() {
        return ukwoCompanyName;
    }

    public void setUkwoCompanyName(String ukwoCompanyName) {
        this.ukwoCompanyName = ukwoCompanyName;
    }

    public String getUkwoUniSociCreditCode() {
        return ukwoUniSociCreditCode;
    }

    public void setUkwoUniSociCreditCode(String ukwoUniSociCreditCode) {
        this.ukwoUniSociCreditCode = ukwoUniSociCreditCode;
    }

    public String getUkwoLegalPersonName() {
        return ukwoLegalPersonName;
    }

    public void setUkwoLegalPersonName(String ukwoLegalPersonName) {
        this.ukwoLegalPersonName = ukwoLegalPersonName;
    }

    public String getUkwoLegalPersonIdcard() {
        return ukwoLegalPersonIdcard;
    }

    public void setUkwoLegalPersonIdcard(String ukwoLegalPersonIdcard) {
        this.ukwoLegalPersonIdcard = ukwoLegalPersonIdcard;
    }

    public String getUkwoLegalPersonPhone() {
        return ukwoLegalPersonPhone;
    }

    public void setUkwoLegalPersonPhone(String ukwoLegalPersonPhone) {
        this.ukwoLegalPersonPhone = ukwoLegalPersonPhone;
    }

    public String getUkwoContactName() {
        return ukwoContactName;
    }

    public void setUkwoContactName(String ukwoContactName) {
        this.ukwoContactName = ukwoContactName;
    }

    public String getUkwoContactIdcard() {
        return ukwoContactIdcard;
    }

    public void setUkwoContactIdcard(String ukwoContactIdcard) {
        this.ukwoContactIdcard = ukwoContactIdcard;
    }

    public String getUkwoContactPhone() {
        return ukwoContactPhone;
    }

    public void setUkwoContactPhone(String ukwoContactPhone) {
        this.ukwoContactPhone = ukwoContactPhone;
    }

    public long getUkwoRegionCode() {
        return ukwoRegionCode;
    }

    public void setUkwoRegionCode(long ukwoRegionCode) {
        this.ukwoRegionCode = ukwoRegionCode;
    }
}
