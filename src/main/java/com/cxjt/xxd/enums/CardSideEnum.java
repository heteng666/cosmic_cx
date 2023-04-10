package com.cxjt.xxd.enums;

public enum CardSideEnum {
    FRONT("FRONT", "人像面"),

    BACK("BACK", "国徽面");

    CardSideEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    private String code;
    private String description;

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
