package com.cxjt.xxd.plugin.ext;

import com.cxjt.xxd.constants.FormConstant;
import kd.bos.form.IPageCache;
import kd.bos.workflow.taskcenter.plugin.ApprovalDealPageMobilePluginNew;

import java.util.EventObject;
/**
 * 审批下一步处理人页面扩展
 *
 * */
public class ApprovalDealPageMobilePluginEX extends ApprovalDealPageMobilePluginNew {
    @Override
    public void afterCreateNewData(EventObject evt) {
        IPageCache pageCache = this.getPageCache();
        String entitynumber = pageCache.get("entitynumber");
        String taskdefinitionkey = pageCache.get("taskdefinitionkey");
        if(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME.equals(entitynumber) && taskdefinitionkey!=null && taskdefinitionkey.endsWith(FormConstant.TASK_TRANSFER)){
//            this.getPageCache().get("isShowRecall");
            this.getView().setVisible(false,"labelap2","btn_recallmodify");
            this.getModel().setValue("approval_username","选择项目经理");
        }
    }
}
