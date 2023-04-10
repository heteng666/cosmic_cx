package com.cxjt.xxd.controller;

import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.enums.XXDErrorCodeEnum;
import com.cxjt.xxd.model.req.order.Report360Req;
import com.cxjt.xxd.model.req.report.XXD360ReportReq;
import com.cxjt.xxd.model.res.XXDCustomApiResult;
import com.cxjt.xxd.util.XXDUtils;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.custom.annotation.*;
import kd.bos.openapi.common.result.CustomApiResult;
import kd.bos.openapi.common.util.JsonUtil;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.AttachmentServiceHelper;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@ApiController(value = "send360Report",desc = "上传360报告")
@ApiMapping(value = "/xxd")
public class XXD360ReportUpdateController implements Serializable {
    private final Log logger = LogFactory.getLog(this.getClass());
    @Validated
    @ApiPostMapping(value = "/send360Report", desc = "上传360报告")
    @ApiErrorCodes({
            @ApiErrorCode(code = "0000", desc = "成功"),
            @ApiErrorCode(code = "0001", desc = "数据已存在"),
            @ApiErrorCode(code = "403", desc = "没有此接口访问权限"),
            @ApiErrorCode(code = "604", desc = "重复请求"),
            @ApiErrorCode(code = "不为0000", desc = "0000之外的值均为失败"),

    })
    public CustomApiResult<@ApiResponseBody("返回参数") String> send360Report(@NotNull @Valid @ApiRequestBody(value = "入参") XXD360ReportReq report) {
        logger.info("接收到上传360报告附件请求,请求报文待加密,请求信息如下:{}", JsonUtil.format(report));
        String ukwoApplyId = report.getUkwoApplyId();
        Report360Req report360 = report.getReport360();
        if(StringUtils.isEmpty(ukwoApplyId)||report360==null){
            return XXDCustomApiResult.fail(XXDErrorCodeEnum.FAILED.getCode(),"参数不能为空");
        }
        String ukwo360ReportUrl = report360.getUkwo360ReportUrl();
        if(StringUtils.isEmpty(ukwo360ReportUrl)){
            return XXDCustomApiResult.fail(XXDErrorCodeEnum.FAILED.getCode(),"参数不能为空");
        }
        QFilter qFilter = new QFilter("ukwo_apply_id", QCP.equals, ukwoApplyId);
        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME, "ukwo_apply_id,ukwo_360_report_url", new QFilter[]{qFilter});
        if(dynamicObject==null){
            return XXDCustomApiResult.fail(XXDErrorCodeEnum.DOES_NOT_EXIST.getCode(),"业务编号没有查找到对应单据");
        }
        String oldUrl = dynamicObject.getString("ukwo_360_report_url");
        if(ukwo360ReportUrl.equals(oldUrl)){
            return XXDCustomApiResult.fail(XXDErrorCodeEnum.HAVE_EXIST.getCode(),"数据已存在");
        }

        try{
            dynamicObject.set("ukwo_360_report_url",ukwo360ReportUrl);
            List<Map<String, Object>> atts = AttachmentServiceHelper.getAttachments(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME, dynamicObject.getPkValue(), "attachmentpanel");
            XXDUtils.buildAttachmentDataFromPanel(ukwo360ReportUrl,"360报告.pdf","pdf",dynamicObject.getPkValue());
            if(atts!=null && atts.size()>0){
                for (Map<String, Object> att:atts) {
                    Object attPkId = att.get("attPkId");
                    DynamicObject attObj = BusinessDataServiceHelper.loadSingle(attPkId, "bos_attachment", "FInterID,fnumber");
                    Object fInterID = attObj.get("FInterID");
                    Object fnumber = attObj.get("fnumber");
                    AttachmentServiceHelper.remove(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME,fInterID,fnumber);
                }
            }
            SaveServiceHelper.saveOperate(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME,new DynamicObject[]{dynamicObject});
        }catch (Exception e){
            logger.error("上传360报告附件出现异常,业务单据编号:{},异常信息如下:{}",ukwoApplyId,e);
            throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.FAILED.getCode(), "360报告附件上传失败"));
        }
        return XXDCustomApiResult.success(XXDErrorCodeEnum.SUCCESS.getCode(), "成功提交", "");
    }
}
