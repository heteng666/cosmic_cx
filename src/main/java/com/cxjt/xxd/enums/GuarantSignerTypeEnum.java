package com.cxjt.xxd.enums;

/**
 * 签订类型
 */
public enum GuarantSignerTypeEnum {
    INDIVIDUAL("1", "个人"),
    ENTERPRISE("2", "企业");


    private String code;
    private String description;

    private GuarantSignerTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
