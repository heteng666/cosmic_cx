package com.cxjt.xxd.enums;

public enum GuarantRelationEnum {

    //法定代表人、法定代表人配偶、法定代表人子女、实际控制人、实际控制人配偶、实际控制人子女、自然人股东、自然人股东配偶、实际控制人控股企业
    A("1", "申请者"),
    B("2", "法定代表人配偶"),
    C("9", "法定代表人子女"),
    D("5", "实际控制人"),
    E("6", "实际控制人配偶"),
    F("7", "实际控制人子女"),
    G("3", "自然人股东"),
    H("4", "自然人股东配偶"),
    I("8", "实际控制人控股企业"),
    J("10", "法定代表人");

    private String code;
    private String description;

    private GuarantRelationEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }


    public String getDescription() {
        return description;
    }

}
