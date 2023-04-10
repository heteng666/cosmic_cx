package com.cxjt.xxd.plugin.mobile;

import com.alibaba.fastjson.JSONObject;
import com.cxjt.xxd.constants.FormConstant;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IClientViewProxy;
import kd.bos.form.control.IFrame;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.plugin.AbstractMobFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.workflow.WorkflowServiceHelper;
import kd.bos.util.StringUtils;
import kd.bos.workflow.component.approvalrecord.IApprovalRecordGroup;

import java.math.BigDecimal;
import java.util.*;

/**
 * 360报告预览，发送前端指令调用云之家附件预览
 * */
public class TaskShowPageMobPlugin extends AbstractMobFormPlugin {

    @Override
    public void afterCreateNewData(EventObject e) {

        FormShowParameter showParameter = this.getView().getFormShowParameter();
        //获取单个参数
        JSONObject params = showParameter.getCustomParam("params");
        if(params!=null){
            JSONObject att = params.getJSONObject("att");
            IClientViewProxy clientViewProxy = this.getView().getService(IClientViewProxy.class);
            // 封装一个数据结构，转成json之后是这个格式：
            // [{"downloadUrl":'',"fileDownloadUrl":'',"fileExt":'jpg',"fileId":"",'fileName':""}]
            //{"p":[{"downloadUrl":'',"fileDownloadUrl":'',"fileExt":'jpg',"fileId":"",'fileName':""}],"a":"previewAttachment"}
            HashMap resutJson = new HashMap<String,Object>();
            HashMap tempJson = new HashMap<String,Object>();
            ArrayList<Object> ayys = new ArrayList<>();
            tempJson.put("downloadUrl",att.get("previewurl"));
            tempJson.put("fileDownloadUrl",att.get("previewurl"));
            tempJson.put("previewUrl",att.get("previewurl"));
            tempJson.put("fileExt",att.get("type"));
            tempJson.put("fileId",att.get("uid"));
            tempJson.put("fileName",att.get("name"));
            ayys.add(tempJson);
            resutJson.put("p",ayys);
            resutJson.put("a","previewAttachment");
            //给前端发previewAttachment指令
            clientViewProxy.addAction("previewAttachment", ayys);
        }
    }

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        /*
        直接iframe打开附件会异常，改为发送前端指令
        FormShowParameter showParameter = this.getView().getFormShowParameter();
        //获取单个参数
        JSONObject params = showParameter.getCustomParam("params1");
        if(params!=null){
//          在iframe控件打开，页面跳转可正常
            String auditUrl = params.getString("auditUrl");
//            this.getView().openUrl(auditUrl);
            IFrame iframe = this.getView().getControl("ukwo_iframeap");// 获得IFrame控件对象
//            String url = "http://223.151.52.25:8099/ierp/attachment/preview.do?path=/0e176cad922d4c2f84228294fae0d2d7&appId=ukwo_demo_ht&fId=ukwo_purreq_ht&pageId=root9e28da376b894876b8ed3f45ca849a83_ukwo_purreq_ht_1612253207424169984&kd_cs_ticket=WGMl8FsqjhgCr4yvhS39GxRfiMyll62C";
            String url = "http://36.7.144.246:28080/group1/M00/22/B8/oYYBAGPc1O2Ac5PgAAJVdmhqEow101.pdf";
            iframe.setSrc(url);
            if (StringUtils.isNotEmpty(auditUrl)) {
                iframe.setSrc(auditUrl);
            }
        }
         */
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs e) {
        String operateKey = e.getOperateKey();
        if("initdata".equals(operateKey)){
            ArrayList<DynamicObject> dynamicObjects = new ArrayList<>();
            String msg = "";
            String prop = "billno,ukwo_amountmob,ukwo_approve_amount,ukwo_termmob,ukwo_approve_term,ukwo_last_year_revemob," +
                    "ukwo_last_year_revenue,ukwo_last_year_total_amob,ukwo_last_year_total_asse," +
                    "ukwo_last_year_pay_tamob,ukwo_last_year_pay_taxes,ukwo_number_of_employmob,ukwo_number_of_employees," +
                    "ukwo_is_existing_businmob,ukwo_is_existing_business,ukwo_if_first_bank_mob,ukwo_if_first_bank_loan," +
                    "ukwo_high_tech_mob,ukwo_high_tech_industry,ukwo_if_three_farmob,ukwo_if_three_farmers," +
                    "ukwo_core_fund_using_mob,ukwo_core_fund_using_ente,ukwo_incr_emp_mob,ukwo_incr_emp_count," +
                    "ukwo_incr_income_mob,ukwo_incr_income_amout";
            QFilter qFilter = new QFilter("billstatus", QCP.not_equals, "A");
            DynamicObject[] bills = BusinessDataServiceHelper.load(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME, prop, new QFilter[]{qFilter});
            //1.已审核；2.已提交已过尽调节点
            for (DynamicObject bill:bills) {
                //上一年度营收(万元)
                BigDecimal ukwo_last_year_revenue = (BigDecimal) bill.get("ukwo_last_year_revenue");
                if(BigDecimal.ZERO.compareTo(ukwo_last_year_revenue)==0){
                    continue;
                }
                //上年度资产总额(万元)
                BigDecimal ukwo_last_year_total_asse = (BigDecimal) bill.get("ukwo_last_year_total_asse");
                if(BigDecimal.ZERO.compareTo(ukwo_last_year_total_asse)==0){
                    continue;
                }
                //上一年度缴纳税收(万元)
                BigDecimal ukwo_last_year_pay_taxes = (BigDecimal) bill.get("ukwo_last_year_pay_taxes");
                if(BigDecimal.ZERO.compareTo(ukwo_last_year_pay_taxes)==0){
                    continue;
                }
                //从业人数
                Integer ukwo_number_of_employees = (Integer) bill.get("ukwo_number_of_employees");
                if(ukwo_number_of_employees==null||ukwo_number_of_employees==0){
                    continue;
                }
                //是否存量业务
                String ukwo_is_existing_business = (String) bill.get("ukwo_is_existing_business");
                if(kd.bos.util.StringUtils.isEmpty(ukwo_is_existing_business)){
                    continue;
                }
                //是否首次银行贷款
                String ukwo_if_first_bank_loan = (String) bill.get("ukwo_if_first_bank_loan");
                if(kd.bos.util.StringUtils.isEmpty(ukwo_if_first_bank_loan)){
                    continue;
                }
                //是否高新技术产业
                String ukwo_high_tech_industry = (String) bill.get("ukwo_high_tech_industry");
                if(kd.bos.util.StringUtils.isEmpty(ukwo_high_tech_industry)){
                    continue;
                }
                //是否三农
                String ukwo_if_three_farmers = (String) bill.get("ukwo_if_three_farmers");
                if(kd.bos.util.StringUtils.isEmpty(ukwo_if_three_farmers)){
                    continue;
                }
                //核心用款企业
                String ukwo_core_fund_using_ente = (String) bill.get("ukwo_core_fund_using_ente");
                if(StringUtils.isEmpty(ukwo_core_fund_using_ente)){
                    continue;
                }
                //审批金额(万元)
                BigDecimal ukwo_approve_amount = (BigDecimal) bill.get("ukwo_approve_amount");
                if(BigDecimal.ZERO.compareTo(ukwo_approve_amount)>=0){
                    continue;
                }
                //审批期限(月)
                Integer ukwo_approve_term = (Integer) bill.get("ukwo_approve_term");
                if(ukwo_approve_term==null||ukwo_approve_term==0){
                    continue;
                }

                List<IApprovalRecordGroup> allApprovalRecord = WorkflowServiceHelper.getAllApprovalRecord(bill.getPkValue().toString());
                if(allApprovalRecord!=null && allApprovalRecord.size()>3){
                    bill.set("ukwo_amountmob",bill.get("ukwo_approve_amount"));
                    bill.set("ukwo_termmob",bill.get("ukwo_approve_term"));

                    bill.set("ukwo_last_year_revemob",bill.get("ukwo_last_year_revenue"));
                    bill.set("ukwo_last_year_total_amob",bill.get("ukwo_last_year_total_asse"));
                    bill.set("ukwo_last_year_pay_tamob",bill.get("ukwo_last_year_pay_taxes"));
                    bill.set("ukwo_number_of_employmob",bill.get("ukwo_number_of_employees"));
                    bill.set("ukwo_is_existing_businmob",bill.get("ukwo_is_existing_business"));
                    bill.set("ukwo_if_first_bank_mob",bill.get("ukwo_if_first_bank_loan"));
                    bill.set("ukwo_high_tech_mob",bill.get("ukwo_high_tech_industry"));
                    bill.set("ukwo_if_three_farmob",bill.get("ukwo_if_three_farmers"));
                    bill.set("ukwo_core_fund_using_mob",bill.get("ukwo_core_fund_using_ente"));
                    bill.set("ukwo_incr_emp_mob",bill.get("ukwo_incr_emp_count"));
                    bill.set("ukwo_incr_income_mob",bill.get("ukwo_incr_income_amout"));
                    dynamicObjects.add(bill);
                    msg=msg+bill.getString("billno")+";";
                }
            }
//            this.getView().showTipNotification(msg+";;;"+dynamicObjects.size());
            if(dynamicObjects.size()>0){
                DynamicObject[] allbills = dynamicObjects.toArray(new DynamicObject[dynamicObjects.size()]);
                SaveServiceHelper.save(allbills);
            }
        }
    }
}
