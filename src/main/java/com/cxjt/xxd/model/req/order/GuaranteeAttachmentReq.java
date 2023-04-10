package com.cxjt.xxd.model.req.order;

import kd.bos.openapi.common.custom.annotation.ApiModel;
import kd.bos.openapi.common.custom.annotation.ApiParam;

import java.io.Serializable;

/**
 * 反担保附件信息
 */
@ApiModel
public class GuaranteeAttachmentReq implements Serializable {

    @ApiParam(value = "附件名称",required = true,example = "<<委托担保合同>>.pdf")
    private String ukwoAttachmentName;

    @ApiParam(value = "附件地址",required = true,example = "xxxxxx")
    private String ukwoAttachmentUrl;

    @ApiParam(value = "附件类型(1:身份证正面,2:身份证反面,3:营业执照,4:决议,5:<<委托担保合同>>,6:<<担保及放款通知书>>,7:<<最高额反担保保证合同>>,8:<<法律文书送达地址确认书>>)",required = true,example = "1")
    private String ukwoAttachmentType;

    @ApiParam(value = "签署人身份证号",required = true,example = "430702199005123031")
    private String ukwoSignerIdcard;

}
