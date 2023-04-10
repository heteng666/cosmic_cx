package com.cxjt.xxd.enums;

/**
 * 单据状态
 */
public enum BillStatusEnum {

    A("A", "暂存"),
    B("B", "已提交"),
    C("C", "已审核");

    private String code;
    private String description;

    BillStatusEnum(String code, String description) {
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
