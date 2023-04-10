package com.cxjt.xxd.model.req.order;

import kd.bos.openapi.common.custom.annotation.ApiModel;
import kd.bos.openapi.common.custom.annotation.ApiParam;

import java.io.Serializable;
import java.util.List;

/**
 * 反担保信息
 */
@ApiModel
public class GuaranteeInfoReq implements Serializable {

    @ApiParam(value = "签署对象,常德金融超市推送过来的数据,1:个人,2:企业",required = true,example = "1")
    private String ukwoGuaSignerType;

    @ApiParam(value = "关系,常德金融超市推送过来的数据:法定代表人:1,法定代表人配偶:2,自然人股东:3,自然人股东配偶:4,实际控制人:5,实际控制人配偶:6,实际控制人子女:7,实际控制人控股企业:8,法定代表人子女:9",required = true,example = "1")
    private String ukwoRelationType;

    @ApiParam(value = "企业名称,常德金融超市推送过来的数据,签订类型为企业时上送",example = "常德市财鑫数字科技有限公司")
    private String ukwoCompanyNameEnt;

    @ApiParam(value = "统一社会信用代码,常德金融超市推送过来的数据,签订类型为企业时上送",example = "3432534534")
    private String ukwoUSocCreCodeEnt;

    @ApiParam(value = "签署人姓名,常德金融超市推送过来的数据",required = true,example = "李四")
    private String ukwoSignerName;

    @ApiParam(value = "签署人身份证号,常德金融超市推送过来的数据",required = true,example = "24464543434")
    private String ukwoSignerIdcard;

    @ApiParam(value = "签署人手机号,常德金融超市推送过来的数据",required = true,example = "13912345678")
    private String ukwoSignerPhone;

    //@ApiParam(value = "反担保行数据唯一标识,智慧财鑫自动生成,无需上送",example = "139123456789998888")
    //private Long ukwoGuaUniCodeEnt;

    //@ApiParam(value = "附件信息",required = true)
    //private List<GuaranteeAttachmentReq> attachmentList;

    public String getUkwoGuaSignerType() {
        return ukwoGuaSignerType;
    }

    public void setUkwoGuaSignerType(String ukwoGuaSignerType) {
        this.ukwoGuaSignerType = ukwoGuaSignerType;
    }

    public String getUkwoRelationType() {
        return ukwoRelationType;
    }

    public void setUkwoRelationType(String ukwoRelationType) {
        this.ukwoRelationType = ukwoRelationType;
    }

    public String getUkwoCompanyNameEnt() {
        return ukwoCompanyNameEnt;
    }

    public void setUkwoCompanyNameEnt(String ukwoCompanyNameEnt) {
        this.ukwoCompanyNameEnt = ukwoCompanyNameEnt;
    }

    public String getUkwoUSocCreCodeEnt() {
        return ukwoUSocCreCodeEnt;
    }

    public void setUkwoUSocCreCodeEnt(String ukwoUSocCreCodeEnt) {
        this.ukwoUSocCreCodeEnt = ukwoUSocCreCodeEnt;
    }

    public String getUkwoSignerName() {
        return ukwoSignerName;
    }

    public void setUkwoSignerName(String ukwoSignerName) {
        this.ukwoSignerName = ukwoSignerName;
    }

    public String getUkwoSignerIdcard() {
        return ukwoSignerIdcard;
    }

    public void setUkwoSignerIdcard(String ukwoSignerIdcard) {
        this.ukwoSignerIdcard = ukwoSignerIdcard;
    }

    public String getUkwoSignerPhone() {
        return ukwoSignerPhone;
    }

    public void setUkwoSignerPhone(String ukwoSignerPhone) {
        this.ukwoSignerPhone = ukwoSignerPhone;
    }



}
