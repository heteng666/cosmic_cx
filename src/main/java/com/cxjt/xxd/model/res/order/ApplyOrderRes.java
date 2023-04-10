package com.cxjt.xxd.model.res.order;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 尽调结果通知
 */
public class ApplyOrderRes implements Serializable {

    //业务编号
    private String applyId;

    //尽调结果
    private String toneDownResult;

    //审批额度,单位:元,审批通过必填
    private String approveAmount;

    //担保期限,单位:月,审批通过必填
    private String guaranteeTerm;

    //项目经理
    private String projectManager;

    //项目经理手机号
    private String phoneNumber;

    //审批时间,格式:YYYY-MM-DD HH:MM:SS
    private String approveDate;

    //审批意见
    private String approveDesc;


    //反担保信息
    private List<GuaranteeInfoRes> backGuaranteeInfo = Collections.EMPTY_LIST;


    public String getApplyId() {
        return applyId;
    }

    public void setApplyId(String applyId) {
        this.applyId = applyId;
    }

    public String getToneDownResult() {
        return toneDownResult;
    }

    public void setToneDownResult(String toneDownResult) {
        this.toneDownResult = toneDownResult;
    }

    public String getApproveAmount() {
        return approveAmount;
    }

    public void setApproveAmount(String approveAmount) {
        this.approveAmount = approveAmount;
    }

    public String getGuaranteeTerm() {
        return guaranteeTerm;
    }

    public void setGuaranteeTerm(String guaranteeTerm) {
        this.guaranteeTerm = guaranteeTerm;
    }

    public String getProjectManager() {
        return projectManager;
    }

    public void setProjectManager(String projectManager) {
        this.projectManager = projectManager;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getApproveDate() {
        return approveDate;
    }

    public void setApproveDate(String approveDate) {
        this.approveDate = approveDate;
    }

    public String getApproveDesc() {
        return approveDesc;
    }

    public void setApproveDesc(String approveDesc) {
        this.approveDesc = approveDesc;
    }

    public List<GuaranteeInfoRes> getBackGuaranteeInfo() {
        return backGuaranteeInfo;
    }

    public void setBackGuaranteeInfo(List<GuaranteeInfoRes> backGuaranteeInfo) {
        this.backGuaranteeInfo = backGuaranteeInfo;
    }
}
