package com.cxjt.xxd.constants;

public class FormConstant {
    public static final String APP_ID= "ukwo_xxd";
    //贷款申请订单实体名称
    public static final String LOAN_APPLY_ORDER_ENTITY_NAME = "ukwo_xxd_loan_apply_bill";

    //贷款申请备注单
    public static final String APPLY_NOTE_BILL_ENTITY_NAME = "ukwo_xxd_apply_note_bill";

    //贷款申请订单流程编码或标识(注意:流程设计器中不能随意更改)
    public static final String LOAN_APPLY_ORDER_WORK_FLOW_IDENTIFY = "Proc_ukwo_xxd_loan_apply_bill_audit_3";

    //贷款申请订单-单据体附件字段标识
    public static final String LOAN_APPLY_ORDER_ENTITY_ATTACHMENT_FIELD_NAME = "ukwo_gua_attachment";

    //贷款申请订单-反担保附件面板标识
    public static final String LOAN_APPLY_ORDER_GUA_ATTACHMENT_PANEL_NAME = "ukwo_ap_guarantee";

    //贷款申请订单-申请者附件面板标识
    public static final String LOAN_APPLY_ORDER_APPLICANT_ATTACHMENT_PANEL_NAME = "ukwo_ap_applicant";

    //贷款担保费确认单实体名称
    public static final String LOAN_GUA_CONFIRM_BILL_ENTITY_NAME = "ukwo_xxd_gua_confirm_bill";

    //贷款申请组织机构映射单
    public static final String LOAN_APPLY_ORG_MAPPING_ENTITY_NAME = "ukwo_xxd_org_mapping_bill";

    //申请单附件定时任务
    public static final String LOAN_APPLY_BILL_ATT_TASK_NAME = "ukwo_apply_bill_att_task";

    //申请单附件任务明细
    public static final String LOAN_APPLY_BILL_ATT_TASK_ITEM_NAME = "ukwo_bill_att_task_item";

    //业务单元
    public static final String BOS_ORG = "bos_org";

    //短信发送记录单
    public static final String SEND_SMS_ENTITY_NAME = "ukwo_xxd_send_sms_bill";

    //验证码短信
    public static final String SEND_SMS_VALIDATE_CODE_TYPE = "1";

    //确认订单归属项目经理短信
    public static final String SEND_AUDIT_PROJECT_MRG_TYPE = "2";

    //确认项目经理后,给客户经理的手机发送短信
    public static final String SEND_AUDIT_CUST_MRG_TYPE = "3";

    //是
    public static final String Y = "Y";

    //否
    public static final String N = "N";

    //启用
    public static final String ENABLE = "enable";

    //禁用
    public static final String DISABLE = "disable";

    //审批流提交节点
    public static final String TASK_SUBMIT = "UserTask3";

    //审批流转派节点编码
    public static final String TASK_TRANSFER = "AuditTask5";

    //审批流项目经理录入节点编码
    public static final String TASK_ENTER = "AuditTask7";

    //审批决策项
    public static final String APPROVE = "approve";

    //在流程实例终止时事件标识
    public static final String WORKFLOW_INSTANCE_TERMINATE = "terminate";

    //流程实例完成时事件标识
    public static final String WORKFLOW_INSTANCE_END = "end";

    //时间格式
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    //第三方扩展库路由key
    public static final String SECD_ROUTE_KEY = "secd";
    //系统库
    public static final String SYS_ROUTE_KEY = "sys";


}
