package com.cxjt.xxd.model.req.guarant;

import kd.bos.openapi.common.custom.annotation.ApiModel;
import kd.bos.openapi.common.custom.annotation.ApiParam;

import java.io.Serializable;

/**
 * 相关协议信息
 */
@ApiModel
public class GuaranteeAgreementReq  implements Serializable {

    @ApiParam(value = "反担保行数据唯一标识,智慧财鑫尽调通知接口返回,金融超市回传",required = true,example = "xxxxxx")
    private String ukwoGuaUniCodeEnt;

    @ApiParam(value = "附件名称",required = true,example = "<<委托担保合同>>.pdf")
    private String ukwoAttachmentName;

    @ApiParam(value = "附件地址",required = true,example = "xxxxxx")
    private String ukwoAttachmentUrl;

    @ApiParam(value = "附件类型(1:身份证正面,2:身份证反面,3:营业执照,4:决议,5:<<委托担保合同>>,6:<<担保及放款通知书>>,7:<<最高额反担保保证合同>>,8:<<法律文书送达地址确认书>>,13:<<廉洁风险告知及反馈函>>)",required = true,example = "1")
    private String ukwoAttachmentType;

    @ApiParam(value = "签署人身份证号",required = true,example = "430702199005123031")
    private String ukwoSignerIdcard;

    public String getUkwoGuaUniCodeEnt() {
        return ukwoGuaUniCodeEnt;
    }

    public void setUkwoGuaUniCodeEnt(String ukwoGuaUniCodeEnt) {
        this.ukwoGuaUniCodeEnt = ukwoGuaUniCodeEnt;
    }

    public String getUkwoAttachmentName() {
        return ukwoAttachmentName;
    }

    public void setUkwoAttachmentName(String ukwoAttachmentName) {
        this.ukwoAttachmentName = ukwoAttachmentName;
    }

    public String getUkwoAttachmentUrl() {
        return ukwoAttachmentUrl;
    }

    public void setUkwoAttachmentUrl(String ukwoAttachmentUrl) {
        this.ukwoAttachmentUrl = ukwoAttachmentUrl;
    }

    public String getUkwoAttachmentType() {
        return ukwoAttachmentType;
    }

    public void setUkwoAttachmentType(String ukwoAttachmentType) {
        this.ukwoAttachmentType = ukwoAttachmentType;
    }

    public String getUkwoSignerIdcard() {
        return ukwoSignerIdcard;
    }

    public void setUkwoSignerIdcard(String ukwoSignerIdcard) {
        this.ukwoSignerIdcard = ukwoSignerIdcard;
    }
}
