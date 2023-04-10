package com.cxjt.xxd.enums;

/**
 * 智慧财鑫侧错误码定义
 */
public enum XXDErrorCodeEnum {
    SUCCESS("0000", "成功"),
    HAVE_EXIST("0001", "数据已存在"),
    DOES_NOT_EXIST("0002", "数据不存在"),
    IN_PROCESS("0003", "订单处于流程运转中"),
    AMOUNT_GREATER_THAN_ZERO("0005", "金额应大于0"),
    FAILED("9999", "失败");

    private String code;

    private String description;

    private XXDErrorCodeEnum(String code, String description) {
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
