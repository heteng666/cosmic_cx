package com.cxjt.xxd.model.req.report;

import com.cxjt.xxd.model.req.order.Report360Req;
import kd.bos.openapi.common.custom.annotation.ApiModel;
import kd.bos.openapi.common.custom.annotation.ApiParam;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
@ApiModel
public class XXD360ReportReq implements Serializable {
    @ApiParam(value = "业务编号",required = true,example = "230112430700000009")
    @NotBlank(message = "业务编号不能为空")
    private String ukwoApplyId;

    @ApiParam(value = "360报告", required = true)
    @NotNull
    @Valid
    private Report360Req report360;

    public String getUkwoApplyId() {
        return ukwoApplyId;
    }

    public void setUkwoApplyId(String ukwoApplyId) {
        this.ukwoApplyId = ukwoApplyId;
    }

    public Report360Req getReport360() {
        return report360;
    }

    public void setReport360(Report360Req report360) {
        this.report360 = report360;
    }

}
