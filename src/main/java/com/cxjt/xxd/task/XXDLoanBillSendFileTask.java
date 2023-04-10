package com.cxjt.xxd.task;

import com.cxjt.xxd.dao.XXDLoanApplyBillAttTaskDao;
import com.cxjt.xxd.enums.XXDErrorCodeEnum;
import com.cxjt.xxd.helper.PageHelper;
import com.cxjt.xxd.model.res.XXDCustomApiResult;
import com.cxjt.xxd.service.BillAttachmentExecutor;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.schedule.executor.AbstractTask;

import java.util.Map;

/**
 * 上送订单附件定时任务
 */
public class XXDLoanBillSendFileTask extends AbstractTask {

    private final Log logger = LogFactory.getLog(this.getClass());

    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        logger.info("====================开始执行[上送订单附件定时任务]......");
        //一笔申请单一个任务,获取申请单任务总数
        int taskCount = XXDLoanApplyBillAttTaskDao.getTaskCount();

        if (taskCount <= 0) {
            logger.info("暂未获取到任务数");
            return;
        }

        int pageSize = PageHelper.DEFAULT_PAGE_SIZE;
        //获取页数
        int pageCount = PageHelper.getPageCount(taskCount, pageSize);

        int successCount = 0;

        //循环处理每一页任务
        for (int i = 1; i <= pageCount; i++) {
            try {
                DynamicObjectCollection taskList = XXDLoanApplyBillAttTaskDao.queryTaskByPage(pageSize);
                //循环遍历附件任务单
                for (int j = 0; j < taskList.size(); j++) {
                    DynamicObject taskBill = taskList.get(j);
                    long taskId = (long) taskBill.get("id");
                    logger.info("开始执行第[{}]页第[{}]条任务单[{}].....", i, j, taskId);
                    BillAttachmentExecutor executor = new BillAttachmentExecutor();
                    executor.execute(taskBill);
                    successCount++;
                    logger.info("第[{}]页第[{}]条任务单[{}]执行结束.....", i, j, taskId);
                }
            } catch (Exception e) {
                logger.error("附件上送失败,请联系系统管理员,异常信息如下:{}", e);
            }

        }

        logger.info("====================[上送订单附件定时任务]执行完毕,共处理任务[{}]条......",successCount);
    }
}
