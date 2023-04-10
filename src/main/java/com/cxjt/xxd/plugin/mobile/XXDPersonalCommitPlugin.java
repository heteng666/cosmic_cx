package com.cxjt.xxd.plugin.mobile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cxjt.xxd.component.IdentityAuthenticationComponent;
import com.cxjt.xxd.component.VerifyCodeComponent;
import com.cxjt.xxd.enums.JrcsErrorCodeEnum;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.control.AttachmentPanel;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.AbstractMobFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.util.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

public class XXDPersonalCommitPlugin extends AbstractMobFormPlugin {
    private final Log LOGGER = LogFactory.getLog(this.getClass());

    private static final String OPERATE_KEY = "savegr";

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        FormOperate formOperate = (FormOperate) args.getSource();
        String currentKey = formOperate.getOperateKey();   //当前操作控件key

        if (OPERATE_KEY.equals(currentKey)) {
            // 检验附件
            AttachmentPanel attachment1 = this.getControl("ukwo_idcard_front_gr");
            List<Map<String, Object>> attachmentData1 = attachment1.getAttachmentData();
            if (CollectionUtils.isEmpty(attachmentData1)) {
                this.getView().showTipNotification("身份证人像面附件不能为空！");
                args.setCancel(Boolean.TRUE);
                return;
            }
            AttachmentPanel attachment2 = this.getControl("ukwo_idcard_back_gr");
            List<Map<String, Object>> attachmentData2 = attachment2.getAttachmentData();
            if (CollectionUtils.isEmpty(attachmentData2)) {
                this.getView().showTipNotification("身份证国徽面附件不能为空！");
                args.setCancel(Boolean.TRUE);
                return;
            }

            String idCard = (String) this.getModel().getValue("ukwo_signer_idcard_gr");   //法人身份证号码
            String name = (String) this.getModel().getValue("ukwo_signer_name_gr");   //法人姓名
            if (StringUtils.isEmpty(idCard) || StringUtils.isEmpty(name)) {
                this.getView().showTipNotification("请上传身份证完成身份证OCR识别");
                args.setCancel(true);
                return;
            }
            // 检验是否通过信息匹配
            String idcardOcrFront =this.getPageCache().get("ukwo_idcard_front_gr");
            String idcardOcrBack =this.getPageCache().get("ukwo_idcard_back_gr");

            if( "false".equals(idcardOcrFront) || "false".equals(idcardOcrBack) ){
                String idname = "false".equals(idcardOcrFront)?"身份证人像面":"身份证国徽面";
                this.getView().showTipNotification(idname+"信息不符，请重新上传！");
                args.setCancel(Boolean.TRUE);
                return;
            }
            // 关系不能为空
            String relationType = (String) this.getModel().getValue("ukwo_relation_type_gr");
            if(StringUtils.isEmpty(relationType)){
                this.getView().showTipNotification("关系不能为空！");
                args.setCancel(Boolean.TRUE);
                return;
            }
            //行号，控制本人不做手机号校验，1本人，其他非本人
            String ukwoRowNum = (String) this.getModel().getValue("ukwo_row_num");
            if(!"1".equals(ukwoRowNum)){
                String mobilePhone = (String) this.getModel().getValue("ukwo_signer_phone_gr");   //手机号码
                if (StringUtils.isEmpty(mobilePhone)) {
                    this.getView().showTipNotification("请输入签署人手机号");
                    args.setCancel(true);
                    return;
                }
                String verifyCode = (String) this.getModel().getValue("ukwo_yzm_gr");   //验证码
                if (StringUtils.isEmpty(verifyCode)) {
                    this.getView().showTipNotification("请填写验证码进行手机验证");
                    args.setCancel(true);
                    return;
                }
                IFormView parentView = this.getView().getParentView();
                String applyId = parentView.getModel().getValue("ukwo_apply_id").toString();    //申请编号
                //获得缓存里的验证码
                String cacheVerifyCode = VerifyCodeComponent.getCacheVerifyCode("getverifycodegr", applyId, mobilePhone, verifyCode);
                LOGGER.info("缓存中的验证码为：{}", cacheVerifyCode);
                if (StringUtils.isEmpty(cacheVerifyCode)) {
                    this.getView().showTipNotification("验证码已过期，请重新获取验证码");
                    args.setCancel(true);
                    return;
                }
                if (!verifyCode.equals(cacheVerifyCode)) {
                    this.getView().showTipNotification("验证码错误，请重试");
                    args.setCancel(true);
                    return;
                }
            }
            // 检验身份证唯一性
            DynamicObjectCollection entity= this.getView().getParentView().getModel().getEntryEntity("entryentity");
//            获取修改行行号，不校验修改行
            FormShowParameter showParameter = this.getView().getFormShowParameter();
            JSONObject params = showParameter.getCustomParam("params");
            int rowint = -1;
            if (params != null) {
                String rownum = params.getString("rownum");
                rowint = Integer.valueOf(rownum);
            }
            for (int i = 0; i < entity.size(); i++) {
                DynamicObject object = entity.get(i);
                if(object.get("ukwo_gua_signer_type")!=null){
                    String type = object.getString("ukwo_gua_signer_type");
                    if("1".equals(type) && idCard.equals(object.getString("ukwo_signer_idcard"))&&rowint!=i){
                        this.getView().showTipNotification("个人信息已存在！");
                        args.setCancel(true);
                        return;
                    }
                }
            }

            // 身份信息认证
            try {
                String responseStr = IdentityAuthenticationComponent.execute(idCard, name);
                JSONObject respData = JSON.parseObject(responseStr);
                if (!JrcsErrorCodeEnum.SUCCESS.getCode().equals(respData.getString("code"))) {
                    this.getView().showTipNotification(respData.getString("msg"));
                    args.setCancel(true);
                }
                JSONObject body = JSON.parseObject(respData.getString("body"));
                if(!"0".equals(body.getString("Result"))){
                    this.getView().showTipNotification(body.getString("Description"));
                    args.setCancel(true);
                }
            } catch (Exception e) {
                LOGGER.error("身份信息校验异常：" + e.getMessage());
                this.getView().showTipNotification("上传信息有误，请重新上传");
//                this.getView().showTipNotification(e.getMessage());
                args.setCancel(true);
            }
        }
    }

}
