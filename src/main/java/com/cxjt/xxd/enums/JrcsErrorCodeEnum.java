package com.cxjt.xxd.enums;

/**
 * 金融超市侧错误码定义
 */
public enum JrcsErrorCodeEnum {
    SUCCESS("0000", "成功"),
    EXCEPTION("9999", "异常");

    private String code;
    private String description;

    JrcsErrorCodeEnum(String code, String description) {
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
