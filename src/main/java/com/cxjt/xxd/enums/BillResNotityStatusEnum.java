package com.cxjt.xxd.enums;

public enum BillResNotityStatusEnum {
    SUCCESS("SUCCESS", "执行成功"),
    FAILED("FAILED", "执行失败"),
    NOT_YET("NOT_YET", "未执行"),
    RUNNING("RUNNING","执行中");

    private String code;
    private String description;

    BillResNotityStatusEnum(String code, String description) {
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
