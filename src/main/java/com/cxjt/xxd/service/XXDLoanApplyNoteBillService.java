package com.cxjt.xxd.service;

import com.cxjt.xxd.component.UserServiceComponent;
import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.dao.XXDBosOrgDao;
import com.cxjt.xxd.dao.XXDLoanApplyBillDao;
import com.cxjt.xxd.dao.XXDLoanApplyNoteBillDao;

import com.cxjt.xxd.enums.BillStatusEnum;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.tx.TX;
import kd.bos.db.tx.TXHandle;

import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import java.util.Date;

/**
 * 贷款申请订单备注进度服务接口
 */
public class XXDLoanApplyNoteBillService {

    private static Log logger = LogFactory.getLog(XXDLoanApplyNoteBillService.class);

    private static final String APPLY_NOTE_BILL_ENTITY_NAME = FormConstant.APPLY_NOTE_BILL_ENTITY_NAME;

    private static final XXDLoanApplyNoteBillDao loanApplyNoteBillDao = new XXDLoanApplyNoteBillDao();

    //private static final XXDLoanApplyBillDao loanApplyBillDao = new XXDLoanApplyBillDao();

    /**
     * @param applyId     贷款申请单号
     * @param noteContent 备注信息
     * @throws Exception
     */
    public static void save(String applyId, String noteContent) throws Exception {

        if (StringUtils.isBlank(noteContent)) {
            throw new RuntimeException("备注信息不能为空");
        }
        noteContent = noteContent.trim();
        noteContent = noteContent.length() > 500 ? noteContent.substring(0, 500) : noteContent;
        //备注时间
        String noteTime = DateFormatUtils.format(new Date(), FormConstant.DATETIME_FORMAT);
        //创建人等于当前登录人
        long userId = UserServiceComponent.getCurrentUserId();

        long mainOrgId = UserServiceComponent.getUserMainOrgId(userId);

        //创建时间
        Date createTime = new Date();

        DynamicObject loanNoteBill = BusinessDataServiceHelper.newDynamicObject(APPLY_NOTE_BILL_ENTITY_NAME);
        loanNoteBill.set("ukwo_apply_id", applyId);
        loanNoteBill.set("ukwo_note_content", noteContent);
        loanNoteBill.set("ukwo_note_time", noteTime);
        loanNoteBill.set("org", XXDBosOrgDao.queryById(mainOrgId));
         loanNoteBill.set("creator", userId);
        loanNoteBill.set("ukwo_create_time", createTime);
        //只有暂存的数据才可以提交
        loanNoteBill.set("billstatus", BillStatusEnum.A.getCode());

        //事务一致性处理
        try (TXHandle h = TX.required("ukwo_xxd_apply_note_bill_submit")) {
            try {
                //更新贷款申请单
                XXDLoanApplyBillDao.updateNote(applyId, noteContent, noteTime);
                //提交贷款申请备注单
                loanApplyNoteBillDao.save(APPLY_NOTE_BILL_ENTITY_NAME, loanNoteBill);

            } catch (Throwable e) {
                h.markRollback();
                logger.error("备注订单服务出现异常,参数如下:applyId={},异常信息如下:{}", applyId, e);
                throw e;
            }
        }

    }

}
