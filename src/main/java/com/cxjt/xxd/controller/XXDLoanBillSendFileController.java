package com.cxjt.xxd.controller;

import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.dao.XXDLoanApplyBillAttTaskDao;
import com.cxjt.xxd.dao.XXDLoanApplyBillAttTaskItemDao;
import com.cxjt.xxd.enums.BillResNotityStatusEnum;
import com.cxjt.xxd.enums.XXDErrorCodeEnum;
import com.cxjt.xxd.helper.PageHelper;
import com.cxjt.xxd.model.res.XXDCustomApiResult;
import com.cxjt.xxd.service.BillAttachmentExecutor;
import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.db.ResultSetHandler;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.custom.annotation.*;
import kd.bos.openapi.common.result.CustomApiResult;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.ResultSet;

/**
 * 内部使用,未暴露给外围
 */
@ApiController(value = "xxd", desc = "鑫湘贷贷款申请单附件服务")
@ApiMapping(value = "/xxd/loanAttachment")
public class XXDLoanBillSendFileController implements Serializable {

    private final Log logger = LogFactory.getLog(this.getClass());


    @Validated
    @ApiPostMapping(value = "sendFile", desc = "上送贷款申请订单附件")
    public CustomApiResult<@ApiResponseBody("返回参数") String> sendFile(@NotNull @Valid @ApiParam(value = "业务单号", required = true) String applyId) {

        logger.info("鑫湘贷贷款申请单附件服务接收到请求,参数如下:applyId={}", applyId);

        //一笔申请单一个任务,获取申请单任务总数
        int taskCount = XXDLoanApplyBillAttTaskDao.getTaskCount();

        if (taskCount <= 0) {
            return XXDCustomApiResult.success(XXDErrorCodeEnum.SUCCESS.getCode(), "暂未获取到任务数", "");
        }

        int pageSize = PageHelper.DEFAULT_PAGE_SIZE;
        //获取页数
        int pageCount = PageHelper.getPageCount(taskCount, pageSize);

        //循环处理每一页
        for (int i = 1; i <= pageCount; i++) {
            try {
                DynamicObjectCollection taskList = XXDLoanApplyBillAttTaskDao.queryTaskByPage(pageSize);
                //String update_task_item_sql = "update tk_ukwo_bil_att_task_item set fk_ukwo_exec_status = ? where id = ? and fk_ukwo_enable_status = ? and fk_ukwo_exec_status in(?,?)";

                //循环遍历附件任务单
                for (DynamicObject taskBill : taskList) {
                    BillAttachmentExecutor executor = new BillAttachmentExecutor();
                    executor.execute(taskBill);

                }

            } catch (Exception e) {
                logger.error("处理订单列表出现异常,请联系系统管理员,异常信息如下:{}", e);
                return XXDCustomApiResult.success(XXDErrorCodeEnum.FAILED.getCode(), "附件上送失败", e.toString());
            }
        }

        return XXDCustomApiResult.success(XXDErrorCodeEnum.SUCCESS.getCode(), "附件上送成功", "");

    }

}
