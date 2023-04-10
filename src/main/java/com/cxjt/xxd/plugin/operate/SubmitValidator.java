package com.cxjt.xxd.plugin.operate;

import com.cxjt.xxd.util.XXDUtils;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.validate.AbstractValidator;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.util.StringUtils;
import java.math.BigDecimal;
/*
* 工作流校验器
* */
public class SubmitValidator extends AbstractValidator {
    private final static Log logger = LogFactory.getLog(SubmitValidator.class);
    @Override
    public void validate() {
        ExtendedDataEntity[] dataEntities = this.getDataEntities();
        if(dataEntities!=null&&dataEntities.length>0){
            StringBuffer errString = new StringBuffer("请录入");
            ExtendedDataEntity dataEntity = dataEntities[0];
            DynamicObject bill = dataEntity.getDataEntity();
            //上一年度营收(万元)
            BigDecimal ukwo_last_year_revenue = (BigDecimal) bill.get("ukwo_last_year_revenue");
            if(BigDecimal.ZERO.compareTo(ukwo_last_year_revenue)==0){
                errString.append("'上一年度营收(万元)',");
            }
            //上年度资产总额(万元)
            BigDecimal ukwo_last_year_total_asse = (BigDecimal) bill.get("ukwo_last_year_total_asse");
            if(BigDecimal.ZERO.compareTo(ukwo_last_year_total_asse)==0){
                errString.append("'上年度资产总额(万元)',");
            }
            //上一年度缴纳税收(万元)
            BigDecimal ukwo_last_year_pay_taxes = (BigDecimal) bill.get("ukwo_last_year_pay_taxes");
            if(BigDecimal.ZERO.compareTo(ukwo_last_year_pay_taxes)==0){
                errString.append("'上一年度缴纳税收(万元)',");
            }
            //从业人数
            Integer ukwo_number_of_employees = (Integer) bill.get("ukwo_number_of_employees");
            if(ukwo_number_of_employees==null||ukwo_number_of_employees==0){
                errString.append("'从业人数',");
            }
            //是否存量业务
            String ukwo_is_existing_business = (String) bill.get("ukwo_is_existing_business");
            if(StringUtils.isEmpty(ukwo_is_existing_business)){
                errString.append("'是否存量业务',");
            }
            //是否首次银行贷款
            String ukwo_if_first_bank_loan = (String) bill.get("ukwo_if_first_bank_loan");
            if(StringUtils.isEmpty(ukwo_if_first_bank_loan)){
                errString.append("'是否首次银行贷款',");
            }
            //是否高新技术产业
            String ukwo_high_tech_industry = (String) bill.get("ukwo_high_tech_industry");
            if(StringUtils.isEmpty(ukwo_high_tech_industry)){
                errString.append("'是否高新技术产业',");
            }
            //是否三农
            String ukwo_if_three_farmers = (String) bill.get("ukwo_if_three_farmers");
            if(StringUtils.isEmpty(ukwo_if_three_farmers)){
                errString.append("'是否三农',");
            }
            //核心用款企业
            String ukwo_core_fund_using_ente = (String) bill.get("ukwo_core_fund_using_ente");
            if(StringUtils.isEmpty(ukwo_core_fund_using_ente)){
                errString.append("'核心用款企业',");
            }
            //带动就业(人)
//            Integer ukwo_incr_emp_count = (Integer) bill.get("ukwo_incr_emp_count");
//            if(ukwo_incr_emp_count==null||ukwo_incr_emp_count==0){
//                errString.append("'带动就业(人)',");
//            }
            //带动增收(万元)
//            BigDecimal ukwo_incr_income_amout = (BigDecimal) bill.get("ukwo_incr_income_amout");
//            if(BigDecimal.ZERO.compareTo(ukwo_incr_income_amout)==0){
//                errString.append("'带动增收(万元)',");
//            }
            //申报金额
            BigDecimal ukwoApplicationAmount = (BigDecimal) bill.get("ukwo_application_amount");
            //审批金额(万元)
            BigDecimal ukwo_approve_amount = (BigDecimal) bill.get("ukwo_approve_amount");
            if(BigDecimal.ZERO.compareTo(ukwo_approve_amount)>=0||ukwo_approve_amount.compareTo(ukwoApplicationAmount) > 0){
                errString.append("'审批金额(万元)',");
            }
            //审批期限(月)
//            Integer ukwo_approve_term = (Integer) bill.get("ukwo_approve_term");
//            if(ukwo_approve_term==null||ukwo_approve_term==0){
//                errString.append("'审批期限(月)',");
//            }

            //债务人/债务人经营主体经济成分
            String ukwo_economic_compos = (String) bill.get("ukwo_economic_compos");
            if(StringUtils.isEmpty(ukwo_economic_compos)){
                errString.append("'债务人/债务人经营主体经济成分',");
            }
            //政策扶持领域
            String ukwo_policy_support_area = (String) bill.get("ukwo_policy_support_area");
            if(StringUtils.isEmpty(ukwo_policy_support_area)){
                errString.append("'政策扶持领域',");
            }
            //战略新兴产业分类
            String ukwo_strategy_em_industry = (String) bill.get("ukwo_strategy_em_industry");
            if(StringUtils.isEmpty(ukwo_strategy_em_industry)){
                errString.append("'战略新兴产业分类',");
            }
            //企业登记所在区县
            String ukwo_ent_register_area = (String) bill.get("ukwo_ent_register_area");
            if(StringUtils.isEmpty(ukwo_ent_register_area)){
                errString.append("'企业登记所在区县',");
            }




            DynamicObjectCollection entrys = bill.getDynamicObjectCollection("entryentity");
            if(entrys!=null && entrys.size()>0){
                logger.info("进入反担保附件校验逻辑,单据号："+bill.getString("billno"));
                for (int i = 0; i < entrys.size(); i++) {
                    DynamicObject entry = entrys.get(i);
                    int seq = entry.getInt("seq");
                    String type = entry.getString("ukwo_gua_signer_type");
                    DynamicObjectCollection atts = entry.getDynamicObjectCollection("ukwo_gua_attachment");
                    logger.info("单据号："+bill.getString("billno")+";i:"+i+";seq:"+seq+";size:"+atts.size()+";type:"+type);
                    if(atts==null||atts.size()<2){
                        if(seq==1){
                            errString.append("'反担保信息-申请者附件',");
                        }else{
                            errString.append("'反担保信息-第"+ seq +"行-反担保附件',");

                        }
                    }else{
                        //个人
                        int size = atts.size();
                        if("1".equals(type)){
                            if(size!=2){
                                if(seq==1){
                                    errString.append("'反担保信息-申请者附件',");
                                }else{
                                    errString.append("'反担保信息-第"+ seq +"行-反担保附件',");

                                }
                            }
                            //企业
                        }else if("2".equals(type)){
                            if(size!=4){
                                if(seq==1){
                                    errString.append("'反担保信息-申请者附件',");
                                }else{
                                    errString.append("'反担保信息-第"+ seq +"行-反担保附件',");

                                }
                            }
                        }
                    }
                }
                logger.info("结束反担保附件校验逻辑,单据号："+bill.getString("billno"));
            }else{
                errString.append("'反担保信息',");
            }
//            反担保附件
//            List<Map<String, Object>> attachments = AttachmentServiceHelper.getAttachments(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME, bill.getPkValue(), FormConstant.LOAN_APPLY_ORDER_GUA_ATTACHMENT_PANEL_NAME);
//            if(attachments==null||attachments.size()<1){
//                errString.append("'反担保附件',");
//            }
//            申请者附件
//            List<Map<String, Object>> attachments1 = AttachmentServiceHelper.getAttachments(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME, bill.getPkValue(), FormConstant.LOAN_APPLY_ORDER_APPLICANT_ATTACHMENT_PANEL_NAME);
//            if(attachments1==null||attachments1.size()<1){
//                errString.append("'申请者附件',");
//            }
            if(errString.length()>5){
                String substring = errString.substring(0, errString.length() - 1);
                this.addMessage(dataEntity,substring);
            }
        }
    }
}
