package com.cxjt.xxd.enums;

/**
 * 反担保附件类型枚举定义
 */
public enum AttachmentEnum {
    A("1", "身份证人像面"),
    B("2", "身份证国徽面"),
    C("3", "营业执照"),
    D("4", "决议"),
    E("5", "<<委托担保合同>>"),
    F("6", "<<担保及放款通知书>>"),
    G("7", "<<最高额反担保保证合同>>"),
    H("8", "<<法律文书送达地址确认书>>"),
    //单据-申请者附件
    I("10", "申请者附件"),
    //单据-审批附件
    J("11", "审批附件"),
    //单据-反担保附件
    K("12", "反担保附件"),
    L("13", "廉洁风险告知及反馈函");

    private String code;
    private String description;

    AttachmentEnum(String code, String description) {
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
