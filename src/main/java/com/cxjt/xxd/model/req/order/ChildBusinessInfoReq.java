package com.cxjt.xxd.model.req.order;

import kd.bos.openapi.common.custom.annotation.ApiModel;
import kd.bos.openapi.common.custom.annotation.ApiParam;

import java.math.BigDecimal;

/**
 * 子业务信息
 */
@ApiModel
public class ChildBusinessInfoReq {

    @ApiParam(value = "业务编号,常德金融超市推送过来的数据", required = true,example = "230112430700000009")
    private String ukwoApplyId;


    @ApiParam(value = "常德金融超市推送过来的申请企业名称", required = true,example = "常德市财鑫数字科技有限公司")
    private String ukwoProjectName;


    @ApiParam(value = "申保日期,订单推送过来的日期,yyyy-MM-dd HH:mm:ss", required = true,example = "2023-01-09 22:18:56")
    private String ukwoApplicationDate;


    @ApiParam(value = "申保金额(单位:万元),常德金融超市推送过来的预授信金额", required = true,example = "300.00")
    private BigDecimal ukwoApplicationAmount;


    @ApiParam(value = "申保期限(月),常德金融超市推送过来的预授信期限", required = true,example = "12")
    private int ukwoApplicationPeriod;


    @ApiParam(value = "担保费率,常德金融超市推送过来的担保费率,直接送去掉%之后的数据，如费率为:0.6%,那么直接送0.6", required = true,example = "0.6")
    private BigDecimal ukwoGuarantedRates;


    @ApiParam(value = "360信用分,常德金融超市推送过来的360信用分", required = true,example = "620")
    private int ukwo360CreditScore;


    @ApiParam(value = "贷款(融资)用途,常德金融超市推送过来的贷款（融资）用途值", required = true,example = "流动资金贷款")
    private String ukwoLoanUsage;


    @ApiParam(value = "上一年度营收(万元),常德金融超市推送过来的税务数据,审批过程中项目经理可修改", required = true,example = "2300.00")
    private BigDecimal ukwoLastYearRevenue;


    @ApiParam(value = "上年度资产总额(万元),常德金融超市推送过来的税务数据,审批过程中项目经理可修改", required = true,example = "2000.00")
    private BigDecimal ukwoLastYearTotalAsse;


    @ApiParam(value = " 上一年度缴纳税收(万元）,常德金融超市推送过来的税务数据,审批过程中项目经理可修改", required = true,example = "100.00")
    private BigDecimal ukwoLastYearPayTaxes;


    @ApiParam(value = "经办网点,常德金融超市推送过来的经办网点信息", required = true,example = "武陵区支行")
    private String ukwoHandlingOutlet;


    @ApiParam(value = "客户经理,来源常德金融超市推送过来的客户经理信息", required = true,example = "赵六")
    private String ukwoCustManager;

    @ApiParam(value = "客户经理手机号,来源常德金融超市推送过来的客户经理手机号", required = true,example = "15876531765")
    private String ukwoCustPhone;

    @ApiParam(value = "提示类信息,来源常德金融超市推送过来的提示类信息,多条记录用“；”隔开", required = true)
    private String ukwoTips;

    public String getUkwoApplyId() {
        return ukwoApplyId;
    }

    public void setUkwoApplyId(String ukwoApplyId) {
        this.ukwoApplyId = ukwoApplyId;
    }

    public String getUkwoProjectName() {
        return ukwoProjectName;
    }

    public void setUkwoProjectName(String ukwoProjectName) {
        this.ukwoProjectName = ukwoProjectName;
    }

    public String getUkwoApplicationDate() {
        return ukwoApplicationDate;
    }

    public void setUkwoApplicationDate(String ukwoApplicationDate) {
        this.ukwoApplicationDate = ukwoApplicationDate;
    }

    public BigDecimal getUkwoApplicationAmount() {
        return ukwoApplicationAmount;
    }

    public void setUkwoApplicationAmount(BigDecimal ukwoApplicationAmount) {
        this.ukwoApplicationAmount = ukwoApplicationAmount;
    }

    public int getUkwoApplicationPeriod() {
        return ukwoApplicationPeriod;
    }

    public void setUkwoApplicationPeriod(int ukwoApplicationPeriod) {
        this.ukwoApplicationPeriod = ukwoApplicationPeriod;
    }

    public BigDecimal getUkwoGuarantedRates() {
        return ukwoGuarantedRates;
    }

    public void setUkwoGuarantedRates(BigDecimal ukwoGuarantedRates) {
        this.ukwoGuarantedRates = ukwoGuarantedRates;
    }

    public int getUkwo360CreditScore() {
        return ukwo360CreditScore;
    }

    public void setUkwo360CreditScore(int ukwo360CreditScore) {
        this.ukwo360CreditScore = ukwo360CreditScore;
    }

    public String getUkwoLoanUsage() {
        return ukwoLoanUsage;
    }

    public void setUkwoLoanUsage(String ukwoLoanUsage) {
        this.ukwoLoanUsage = ukwoLoanUsage;
    }

    public BigDecimal getUkwoLastYearRevenue() {
        return ukwoLastYearRevenue;
    }

    public void setUkwoLastYearRevenue(BigDecimal ukwoLastYearRevenue) {
        this.ukwoLastYearRevenue = ukwoLastYearRevenue;
    }

    public BigDecimal getUkwoLastYearTotalAsse() {
        return ukwoLastYearTotalAsse;
    }

    public void setUkwoLastYearTotalAsse(BigDecimal ukwoLastYearTotalAsse) {
        this.ukwoLastYearTotalAsse = ukwoLastYearTotalAsse;
    }

    public BigDecimal getUkwoLastYearPayTaxes() {
        return ukwoLastYearPayTaxes;
    }

    public void setUkwoLastYearPayTaxes(BigDecimal ukwoLastYearPayTaxes) {
        this.ukwoLastYearPayTaxes = ukwoLastYearPayTaxes;
    }

    public String getUkwoHandlingOutlet() {
        return ukwoHandlingOutlet;
    }

    public void setUkwoHandlingOutlet(String ukwoHandlingOutlet) {
        this.ukwoHandlingOutlet = ukwoHandlingOutlet;
    }

    public String getUkwoCustManager() {
        return ukwoCustManager;
    }

    public void setUkwoCustManager(String ukwoCustManager) {
        this.ukwoCustManager = ukwoCustManager;
    }

    public String getUkwoCustPhone() {
        return ukwoCustPhone;
    }

    public void setUkwoCustPhone(String ukwoCustPhone) {
        this.ukwoCustPhone = ukwoCustPhone;
    }

    public String getUkwoTips() {
        return ukwoTips;
    }

    public void setUkwoTips(String ukwoTips) {
        this.ukwoTips = ukwoTips;
    }
}
