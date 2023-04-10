package com.cxjt.xxd.plugin.mobile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxjt.xxd.util.XXDUtils;
import kd.bos.ext.form.control.CountDown;
import kd.bos.ext.form.control.events.CountDownEvent;
import kd.bos.ext.form.control.events.CountDownListener;
import kd.bos.form.FormShowParameter;
import kd.bos.form.container.Tab;
import kd.bos.form.control.AttachmentPanel;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.plugin.AbstractMobFormPlugin;

import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 担保人信息补录
 */
public class GuarantorAddBillMobPlugin extends AbstractMobFormPlugin implements CountDownListener {
    @Override
    public void afterDoOperation(AfterDoOperationEventArgs e) {
        String operateKey = e.getOperateKey();
        if ("getverifycodegr".equals(operateKey)) {
            CountDown countdown = this.getControl("ukwo_countdownap");
            // 设置倒计时时间为60秒
            countdown.setDuration(60);
            // 启动倒计时
            countdown.start();
            this.getView().setVisible(false,"ukwo_buttonap");
            this.getView().setVisible(true,"ukwo_countdownap");
        } else if ("getverifycodeqy".equals(operateKey)) {
            CountDown countdown = this.getControl("ukwo_countdownap1");
            // 设置倒计时时间为60秒
            countdown.setDuration(60);
            // 启动倒计时
            countdown.start();
            this.getView().setVisible(false,"ukwo_buttonap1");
            this.getView().setVisible(true,"ukwo_countdownap1");
        }
        if ("savegr".equals(operateKey) || "saveqy".equals(operateKey)) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("key", operateKey);
            hashMap.put("value", this.getModel().getDataEntity());
            // 获取动态表单附件面板
            AttachmentPanel panel1 = this.getControl("ukwo_idcard_front_gr");
            // 获取动态表单附件面板内容
            List<Map<String, Object>> atts1 = panel1.getAttachmentData();
            AttachmentPanel panel2 = this.getControl("ukwo_idcard_back_gr");
            List<Map<String, Object>> atts2 = panel2.getAttachmentData();
            AttachmentPanel panel3 = this.getControl("ukwo_idcard_front_qy");
            List<Map<String, Object>> atts3 = panel3.getAttachmentData();
            AttachmentPanel panel4 = this.getControl("ukwo_idcard_back_qy");
            List<Map<String, Object>> atts4 = panel4.getAttachmentData();
            AttachmentPanel panel5 = this.getControl("ukwo_biz_license");
            List<Map<String, Object>> atts5 = panel5.getAttachmentData();
            AttachmentPanel panel6 = this.getControl("ukwo_resolution");
            List<Map<String, Object>> atts6 = panel6.getAttachmentData();
            hashMap.put("atts1", atts1);
            hashMap.put("atts2", atts2);
            hashMap.put("atts3", atts3);
            hashMap.put("atts4", atts4);
            hashMap.put("atts5", atts5);
            hashMap.put("atts6", atts6);

            this.getView().returnDataToParent(hashMap);
            this.getView().close();
        }
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        FormShowParameter showParameter = this.getView().getFormShowParameter();
        //获取单个参数
        JSONObject params = showParameter.getCustomParam("params");
        if (params != null) {
            String type = params.getString("type");
            //行号赋值，触发界面规则
            this.getModel().setValue("ukwo_row_num", params.get("ukwo_row_num"));
            //个人
            if ("1".equals(type)) {
                this.getView().setVisible(false,"ukwo_tabpageap1");
                this.getModel().setValue("ukwo_signer_address", params.get("ukwo_signer_address"));
                this.getModel().setValue("ukwo_signer_name_gr", params.get("ukwo_signer_name"));
                this.getModel().setValue("ukwo_signer_idcard_gr", params.get("ukwo_signer_idcard"));
                this.getModel().setValue("ukwo_relation_type_gr", params.get("ukwo_relation_type"));
                this.getModel().setValue("ukwo_signer_phone_gr", params.get("ukwo_signer_phone"));
                JSONArray atts = (JSONArray) params.get("ukwo_gua_attachment");

                String entityId = getView().getEntityId();
                if (atts.size() > 0) {
                    List<Map<String, Object>> attachmentData1 = XXDUtils.buildAttachmentDataFromEdit(atts.getJSONObject(0), entityId, "");
                    //key:目标附件面板标识，value:目标附件面板附件数据
                    JSONObject jsonObject = atts.getJSONObject(0);
                    JSONObject attachObj = jsonObject.getJSONObject("fbasedataid");
                    String attName = attachObj.getJSONObject("name").getString("zh_CN");
                    String panelKey = "ukwo_idcard_front_gr";
                    if(attName.contains("国徽")){
                        panelKey = "ukwo_idcard_back_gr";
                    }
                    AttachmentPanel panel1 = this.getControl(panelKey);
                    this.getView().setVisible(false,panelKey+"ap");
                    panel1.upload(attachmentData1);
                }
                if (atts.size() > 1) {
                    List<Map<String, Object>> attachmentData2 = XXDUtils.buildAttachmentDataFromEdit(atts.getJSONObject(1), entityId, "");
                    //key:目标附件面板标识，value:目标附件面板附件数据
                    JSONObject jsonObject = atts.getJSONObject(1);
                    JSONObject attachObj = jsonObject.getJSONObject("fbasedataid");
                    String attName = attachObj.getJSONObject("name").getString("zh_CN");
                    String panelKey = "ukwo_idcard_back_gr";
                    if(attName.contains("人像")){
                        panelKey = "ukwo_idcard_front_gr";
                    }
                    AttachmentPanel panel2 = this.getControl(panelKey);
                    this.getView().setVisible(false,panelKey+"ap");
                    panel2.upload(attachmentData2);
                }
                //企业
            } else if ("2".equals(type)) {
                // ComboEdit comboEdit = this.getControl("ukwo_relation_type_gr");
                Tab tab = this.getView().getControl("ukwo_tabap");
                tab.activeTab("ukwo_tabpageap1");
                this.getView().setVisible(false,"ukwo_tabpageap");
                this.getModel().setValue("ukwo_busi_lic_address", params.get("ukwo_busi_lic_address"));
                this.getModel().setValue("ukwo_relation_type_qy", params.get("ukwo_relation_type"));
                this.getModel().setValue("ukwo_company_name_ent", params.get("ukwo_company_name_ent"));
                this.getModel().setValue("ukwo_u_soc_cre_code_ent", params.get("ukwo_u_soc_cre_code_ent"));
                this.getModel().setValue("ukwo_signer_name_qy", params.get("ukwo_signer_name"));
                this.getModel().setValue("ukwo_signer_idcard_qy", params.get("ukwo_signer_idcard"));
                this.getModel().setValue("ukwo_signer_phone_qy", params.get("ukwo_signer_phone"));
                if("1".equals(params.get("ukwo_row_num"))){
                    // 申请人，不能修改关系
                    this.getView().setEnable(false,"ukwo_relation_type_qy");
                }
                JSONArray atts = (JSONArray) params.get("ukwo_gua_attachment");
                String entityId = getView().getEntityId();
                if (atts.size() > 0) {
                    List<Map<String, Object>> attachmentData3 = XXDUtils.buildAttachmentDataFromEdit(atts.getJSONObject(0), entityId, "");
                    //key:目标附件面板标识，value:目标附件面板附件数据
                    JSONObject jsonObject = atts.getJSONObject(0);
                    JSONObject attachObj = jsonObject.getJSONObject("fbasedataid");
                    String attName = attachObj.getJSONObject("name").getString("zh_CN");
                    String panelKey = "ukwo_idcard_front_qy";
                    if(attName.contains("国徽")){
                        panelKey = "ukwo_idcard_back_qy";
                    }else if(attName.contains("营业")){
                        panelKey = "ukwo_biz_license";
                    }else if(attName.contains("决议")){
                        panelKey = "ukwo_resolution";
                    }
                    this.getView().setVisible(false,panelKey+"ap");
                    AttachmentPanel panel3 = this.getControl(panelKey);
                    panel3.upload(attachmentData3);
                }
                if (atts.size() > 1) {
                    List<Map<String, Object>> attachmentData4 = XXDUtils.buildAttachmentDataFromEdit(atts.getJSONObject(1), entityId, "");
                    //key:目标附件面板标识，value:目标附件面板附件数据
                    JSONObject jsonObject = atts.getJSONObject(1);
                    JSONObject attachObj = jsonObject.getJSONObject("fbasedataid");
                    String attName = attachObj.getJSONObject("name").getString("zh_CN");
                    String panelKey = "ukwo_idcard_front_qy";
                    if(attName.contains("国徽")){
                        panelKey = "ukwo_idcard_back_qy";
                    }else if(attName.contains("营业")){
                        panelKey = "ukwo_biz_license";
                    }else if(attName.contains("决议")){
                        panelKey = "ukwo_resolution";
                    }
                    AttachmentPanel panel4 = this.getControl(panelKey);
                    this.getView().setVisible(false,panelKey+"ap");
                    panel4.upload(attachmentData4);
                }
                if (atts.size() > 2) {
                    List<Map<String, Object>> attachmentData5 = XXDUtils.buildAttachmentDataFromEdit(atts.getJSONObject(2), entityId, "");
                    //key:目标附件面板标识，value:目标附件面板附件数据
                    JSONObject jsonObject = atts.getJSONObject(2);
                    JSONObject attachObj = jsonObject.getJSONObject("fbasedataid");
                    String attName = attachObj.getJSONObject("name").getString("zh_CN");
                    String panelKey = "ukwo_idcard_front_qy";
                    if(attName.contains("国徽")){
                        panelKey = "ukwo_idcard_back_qy";
                    }else if(attName.contains("营业")){
                        panelKey = "ukwo_biz_license";
                    }else if(attName.contains("决议")){
                        panelKey = "ukwo_resolution";
                    }
                    AttachmentPanel panel5 = this.getControl(panelKey);
                    this.getView().setVisible(false,panelKey+"ap");
                    panel5.upload(attachmentData5);
                }
                if (atts.size() > 3) {
                    List<Map<String, Object>> attachmentData6 = XXDUtils.buildAttachmentDataFromEdit(atts.getJSONObject(3), entityId, "");
                    //key:目标附件面板标识，value:目标附件面板附件数据
                    JSONObject jsonObject = atts.getJSONObject(3);
                    JSONObject attachObj = jsonObject.getJSONObject("fbasedataid");
                    String attName = attachObj.getJSONObject("name").getString("zh_CN");
                    String panelKey = "ukwo_idcard_front_qy";
                    if(attName.contains("国徽")){
                        panelKey = "ukwo_idcard_back_qy";
                    }else if(attName.contains("营业")){
                        panelKey = "ukwo_biz_license";
                    }else if(attName.contains("决议")){
                        panelKey = "ukwo_resolution";
                    }
                    AttachmentPanel panel6 = this.getControl(panelKey);
                    this.getView().setVisible(false,panelKey+"ap");
                    panel6.upload(attachmentData6);
                }
            }
        }


    }

    @Override
    public void registerListener(EventObject e) {
        CountDown countdown = this.getControl("ukwo_countdownap");
        countdown.addCountDownListener(this);
        CountDown countdown1 = this.getControl("ukwo_countdownap1");
        countdown1.addCountDownListener(this);
    }
/**短信发送倒计时
 * */
    @Override
    public void onCountDownEnd(CountDownEvent evt) {
        CountDown countDown = (CountDown) evt.getSource();
        if ("ukwo_countdownap".equals(countDown.getKey())) {
            this.getView().setVisible(false,"ukwo_countdownap");
            this.getView().setVisible(true,"ukwo_buttonap");
        }else if ("ukwo_countdownap1".equals(countDown.getKey())) {
            this.getView().setVisible(false,"ukwo_countdownap1");
            this.getView().setVisible(true,"ukwo_buttonap1");
        }
    }
}
