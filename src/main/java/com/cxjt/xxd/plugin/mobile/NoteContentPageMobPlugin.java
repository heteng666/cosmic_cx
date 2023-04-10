package com.cxjt.xxd.plugin.mobile;

import com.alibaba.fastjson.JSONObject;
import com.cxjt.xxd.service.XXDLoanApplyNoteBillService;
import kd.bos.form.FormShowParameter;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.plugin.AbstractMobFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.util.StringUtils;

import java.util.EventObject;
import java.util.HashMap;

public class NoteContentPageMobPlugin extends AbstractMobFormPlugin {
    private final static Log logger = LogFactory.getLog(NoteContentPageMobPlugin.class);
    @Override
    public void afterCreateNewData(EventObject e) {
        FormShowParameter showParameter = this.getView().getFormShowParameter();
        //获取单个参数
        JSONObject params = showParameter.getCustomParam("params");
        if(params!=null){
            String content = params.getString("content");
            String applyId = params.getString("applyId");
            this.getView().getPageCache().put("applyId",applyId);
            this.getModel().setValue("ukwo_note_content",content);
        }
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs e) {
        String operateKey = e.getOperateKey();
        if("submitnote".equals(operateKey)){
            String content = (String) this.getModel().getValue("ukwo_note_content");
            String applyId = this.getView().getPageCache().get("applyId");
            if(StringUtils.isNotEmpty(content) && StringUtils.isNotEmpty(applyId)){
                try {
                    XXDLoanApplyNoteBillService.save(applyId,content);
                } catch (Exception ex) {
                    logger.error("保存备注异常：{}",ex.getMessage());
                    throw new RuntimeException(ex);
                }
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("content", content);
                this.getView().returnDataToParent(hashMap);
                this.getView().showTipNotification("提交成功");
                this.getView().close();
            }else{
                this.getView().showErrorNotification("备注信息不能为空！");
            }

        }
    }
}