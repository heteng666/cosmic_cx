package com.cxjt.xxd.model.res.order;

import java.io.Serializable;

/**
 * 反担保信息
 */
public class GuaranteeInfoRes implements Serializable {
    //反担保人姓名
    private String personName;

    //签订类型:1个人  2企业
    private String contractType;

    //反担保人类型:1:法人 2法人配偶 3 股东 4 股东配偶 5实控人
    private String personType;

    //身份证号码
    private String idCard;

    //签署人联系方式
    private String phoneNumber;

    //企业名称,签订类型为企业时,必填
    private String enterpriseName;

    //统一社会信用代码,签订类型为企业时,必填
    private String socialCode;

    //反担保唯一标识
    private String uniCode;

    //个人时：身份证地址 企业时：营业执照住所
    private String address;

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public String getPersonType() {
        return personType;
    }

    public void setPersonType(String personType) {
        this.personType = personType;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getSocialCode() {
        return socialCode;
    }

    public void setSocialCode(String socialCode) {
        this.socialCode = socialCode;
    }

    public String getUniCode() {
        return uniCode;
    }

    public void setUniCode(String uniCode) {
        this.uniCode = uniCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
