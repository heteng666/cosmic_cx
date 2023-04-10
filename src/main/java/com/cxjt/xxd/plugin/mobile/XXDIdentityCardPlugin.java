package com.cxjt.xxd.plugin.mobile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cxjt.xxd.component.HttpService;
import com.cxjt.xxd.config.RemoteConfig;
import com.cxjt.xxd.enums.CardSideEnum;
import com.cxjt.xxd.enums.JrcsErrorCodeEnum;
import com.cxjt.xxd.util.FileUtil;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.exception.KDBizException;
import kd.bos.form.control.AttachmentPanel;
import kd.bos.form.control.events.UploadEvent;
import kd.bos.form.control.events.UploadListener;
import kd.bos.form.events.ClientCallBackEvent;
import kd.bos.form.plugin.AbstractMobFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.util.JsonUtil;
import org.apache.commons.lang.StringUtils;
import java.text.SimpleDateFormat;
import java.util.*;

public class XXDIdentityCardPlugin extends AbstractMobFormPlugin implements UploadListener {
    private final Log LOGGER = LogFactory.getLog(this.getClass());

    private static final String JRCS_PROTOCOL = RemoteConfig.getJrcsProtocol();
    private static final String HOST = RemoteConfig.getJrcsHost();
    private static final int PORT = RemoteConfig.getJrcsPort();
    private static final String URL = JRCS_PROTOCOL + "://" + HOST + ":" + PORT + "/fins-precloud/tencentCloud/IDCardOCR.do";

    private String[] attachmentPanelKeys = {"ukwo_idcard_front_gr", "ukwo_idcard_back_gr", "ukwo_idcard_front_qy", "ukwo_idcard_back_qy"};

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        for (String attachmentPanelKey : attachmentPanelKeys) {
            // 侦听附件面板控件的文件上传事件
            AttachmentPanel attachmentPanel = this.getView().getControl(attachmentPanelKey);
            attachmentPanel.addUploadListener(this);
        }
    }

    @Override
    public void upload(UploadEvent evt) {
        AttachmentPanel attachmentPanel = (AttachmentPanel) evt.getSource();
        String key = attachmentPanel.getKey();  //当前控件标识
        this.getView().setVisible(false,key+"ap");
        UploadListener.super.upload(evt);
    }


    /**
     addClientCallBack到期回调方法
     */
    @Override
    public void clientCallBack(ClientCallBackEvent e) {
        if ("afterUploadCard".equals(e.getName())) {
            String keyCode ="";
            try {
                String jsonData = this.getPageCache().get("reqParam");
                String key = this.getPageCache().get("akey");
                // 是否识别成功标识
                this.getPageCache().put(key, "false");
                Map<String, Object>  reqParam = JSONObject.parseObject(jsonData,Map.class);
                String responseStr = HttpService.getService().doPostByHttpClient(URL, JsonUtil.format(reqParam));
                JSONObject resultObj = JSONObject.parseObject(responseStr);
                LOGGER.info("OCR识别完毕：{}", responseStr);
                String code = resultObj.getString("code");
                String msg = resultObj.getString("msg");

                if (JrcsErrorCodeEnum.SUCCESS.getCode().equals(code)) {
                    // 页面回显数据
                    JSONObject data = resultObj.getJSONObject("body");
                    // 您的身份证信息已过期
                    if (key.contains("idcard_front")) {
                        Object rowNum =this.getModel().getValue("ukwo_row_num");
                        LOGGER.info("OCR识别完毕-rowNum：{}", rowNum);
                        if(rowNum!=null && StringUtils.isNotBlank((String)rowNum)){
                            // 修改，信息做匹配
                            if (key.endsWith("gr")) {
                                if(!data.getString("Name").equals( this.getModel().getValue("ukwo_signer_name_gr"))
                                        || !data.getString("IdNum").equals( this.getModel().getValue("ukwo_signer_idcard_gr"))
                                ){
                                    this.getView().showErrorNotification("信息不符，请重新上传");
                                    this.getView().hideLoading(false);
                                    return;
                                }
                                this.getModel().setValue("ukwo_signer_address", data.getString("Address"));
                            }else if (key.endsWith("qy")) {
                                if(!data.getString("Name").equals( this.getModel().getValue("ukwo_signer_name_qy"))
                                        || !data.getString("IdNum").equals( this.getModel().getValue("ukwo_signer_idcard_qy"))
                                ){
                                    this.getView().showErrorNotification("信息不符，请重新上传");
                                    this.getView().hideLoading(false);
                                    return;
                                }
                            }

                        }else{
                            // 新增
                            if (key.endsWith("gr")) {
                                this.getModel().setValue("ukwo_signer_name_gr", data.getString("Name"));
                                this.getModel().setValue("ukwo_signer_idcard_gr", data.getString("IdNum"));
                                this.getModel().setValue("ukwo_signer_address", data.getString("Address"));
                            } else if (key.endsWith("qy")) {
                                this.getModel().setValue("ukwo_signer_name_qy", data.getString("Name"));
                                this.getModel().setValue("ukwo_signer_idcard_qy", data.getString("IdNum"));
                            }
                        }
                    }else{
                        // 国徽面 2017.02.08-2037.02.08
                        //2020.01.15-长期
                       String  validDate = data.getString("ValidDate");
                        if(StringUtils.isBlank(validDate)){
                            this.getView().showErrorNotification("OCR识别失败");
                            this.getView().hideLoading(false);
                            return;
                        }
                        if(!validDate.contains("长期")){
                            String[] dateArr = validDate.split("-");
                            SimpleDateFormat format= new SimpleDateFormat("yyyy.MM.dd");
                            Date startDate = format.parse(dateArr[0]);
                            Date endTime = format.parse(dateArr[1]);
                            Date nowDate = new Date ();
                            if(nowDate.getTime()<startDate.getTime() || nowDate.getTime()>endTime.getTime()){
                                this.getView().showErrorNotification("您的身份证信息已过期");
                                this.getView().hideLoading(false);
                                return;
                            }
                        }

                    }
                    this.getPageCache().put(key, "true");

                } else {
                    LOGGER.error("OCR识别失败：{}", msg);
                    this.getView().showErrorNotification(msg);
                }
            } catch (Exception e1) {
                LOGGER.info("OCR识别异常：{}", e1.getMessage());
                this.getView().showErrorNotification("OCR识别失败，请联系管理员");
            }
            this.getView().hideLoading(false);
        }

    }
    @Override
    public void afterUpload(UploadEvent evt) {
        //this.getView().showTipNotification("OCR识别中，请稍等");

        AttachmentPanel attachmentPanel = (AttachmentPanel) evt.getSource();
        String key = attachmentPanel.getKey();  //当前控件标识
        if (evt.getUrls().length > 1) {
            throw new KDBizException("身份证正面及反面至多上传一个");
        }
        Map<String, Object> attachInfo = (Map<String, Object>) evt.getUrls()[0];    //身份证附件信息
        String fileUrl = attachInfo.get("url").toString();  //文件下载地址
        LOGGER.info("文件地址：" + fileUrl);
        String base64Str = null;
        try {
            byte[] bytes = FileUtil.download(fileUrl);
            base64Str = FileUtil.byte2Base64(bytes);
        } catch (Exception e) {
            LOGGER.error("身份证正面上传异常：{}", e.getMessage());
        }
        if (StringUtils.isBlank(base64Str)) {
            this.getView().showErrorNotification("身份证正面上传异常，请联系管理员");
            return;
        }
        Map<String, Object> reqParam = new HashMap<String, Object>();
        Map<String, String> body = new HashMap<String, String>();
        body.put("ImageBase64", base64Str);
        body.put("CardSide", key.contains("idcard_front") ? CardSideEnum.FRONT.getCode() : CardSideEnum.BACK.getCode());
        reqParam.put("body", body);
        this.getPageCache().put("reqParam", JSON.toJSONString(reqParam));
        this.getPageCache().put("akey", key);
        this.getView().showLoading(new LocaleString("OCR识别中，请稍等"), 0, false);
        this.getView().addClientCallBack("afterUploadCard", 0);

    }


    @Override
    public void afterRemove(UploadEvent evt) {
        AttachmentPanel attachmentPanel = (AttachmentPanel) evt.getSource();
        String key = attachmentPanel.getKey();  //当前控件标识
        this.getView().setVisible(true,key+"ap");
        UploadListener.super.afterRemove(evt);
        /* AttachmentPanel attachmentPanel = (AttachmentPanel) evt.getSource();
        String key = attachmentPanel.getKey();  //当前控件标识
        // 删除文件后清空回显字段
        if (key.contains("idcard_front")) {
            if (key.endsWith("gr")) {
                this.getModel().setValue("ukwo_signer_name_gr", null);
                this.getModel().setValue("ukwo_signer_idcard_gr", null);
                this.getModel().setValue("ukwo_signer_address", null);
            } else if (key.endsWith("qy")) {
                this.getModel().setValue("ukwo_signer_name_qy", null);
                this.getModel().setValue("ukwo_signer_idcard_qy", null);
            }
        } */
    }


}
