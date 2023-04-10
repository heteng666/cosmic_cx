package com.cxjt.xxd.controller;

import com.cxjt.xxd.dao.XXDLoanApplyBillDao;
import com.cxjt.xxd.enums.XXDErrorCodeEnum;
import com.cxjt.xxd.model.res.XXDCustomApiResult;
import com.cxjt.xxd.model.res.order.ApplyNoteRes;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.custom.annotation.*;
import kd.bos.openapi.common.result.CustomApiResult;
import kd.bos.openapi.common.util.JsonUtil;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiController(value = "xxd", desc = "鑫湘贷贷款申请备注进度服务")
@ApiMapping(value = "/xxd/loan/note")
public class XXDLoanNoteController implements Serializable {

    private final Log logger = LogFactory.getLog(this.getClass());

    @Validated
    @ApiGetMapping(value = "queryCommentByApplyId", desc = "获取贷款申请备注进度")
    @ApiErrorCodes({
            @ApiErrorCode(code = "0000", desc = "成功"),
            @ApiErrorCode(code = "0002", desc = "数据不存在"),
            @ApiErrorCode(code = "403", desc = "没有此接口访问权限"),
            @ApiErrorCode(code = "不为0000", desc = "0000之外的值均为失败"),

    })
    public CustomApiResult<@ApiResponseBody("返回参数") ApplyNoteRes> queryCommentByApplyId(@NotBlank @ApiParam(required = true,value = "申请编号",position = 0) String applyId) {

        logger.info("获取贷款申请备注进度接口接收到请求,信息如下:{}", applyId);

        try {
            DynamicObject orderBill = XXDLoanApplyBillDao.queryOne(applyId);

            if (orderBill == null) {
                logger.warn("业务申请单号[{}]不存在", applyId);
                return XXDCustomApiResult.fail(XXDErrorCodeEnum.DOES_NOT_EXIST.getCode(), XXDErrorCodeEnum.DOES_NOT_EXIST.getDescription());
            }

            String noteContent = (String) orderBill.get("ukwo_note_content");
            String noteTime = (String) orderBill.get("ukwo_note_time");

            ApplyNoteRes applyNoteRes = new ApplyNoteRes(applyId,noteContent,noteTime);

            return XXDCustomApiResult.success(XXDErrorCodeEnum.SUCCESS.getCode(),XXDErrorCodeEnum.SUCCESS.getDescription(),applyNoteRes);

        } catch (Exception e) {
            return XXDCustomApiResult.fail(XXDErrorCodeEnum.FAILED.getCode(), e.getMessage());
        }


    }
}
