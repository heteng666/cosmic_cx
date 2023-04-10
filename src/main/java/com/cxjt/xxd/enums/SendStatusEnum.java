package com.cxjt.xxd.enums;

public enum SendStatusEnum {
    UNSENT("0", "未发送"),
    SUCCESS("1", "成功"),
    FAILED("2", "失败");

    SendStatusEnum(String code, String description) {
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
