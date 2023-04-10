package com.cxjt.xxd.enums;

import org.apache.commons.lang.StringUtils;

/**
 * 订单贷款状态枚举值,金融超市侧提供
 */
public enum ApplyStatusEnum {
    XXD_220001(220001, "平台准入中"),
    XXD_220002(220002, "平台准入拒绝"),
    XXD_220003(220003, "税务授权中"),
    XXD_220004(220004, "税务查询失败"),
    XXD_220005(220005, "平台授信中"),

    XXD_220006(220006, "平台授信拒绝"),
    XXD_220007(220007, "银行初审中"),

    XXD_220008(220008, "银行初审拒绝"),
    XXD_220009(220009, "担保审核中"),

    XXD_220010(220010, "担保审核拒绝"),
    XXD_220011(220011, "银行复核中"),

    XXD_220012(220012, "银行复核拒绝"),
    XXD_220013(220013, "授信成功"),
    XXD_220014(220014, "授信过期"),

    XXD_220015(220015, "签约中"),
    XXD_220016(220016, "缴费中"),

    XXD_220017(220017, "缴费待确认"),
    XXD_220018(220018, "缴费成功"),
    XXD_220019(220019, "已放款"),
    XXD_220020(220020, "已还清"),
    XXD_220021(220021, "税务待授权"),

    //智慧财鑫内部状态,进一步对JRCS侧的【担保审核中】【担保审核拒绝】状态进行了细分

    XXD_330000(330000, "办事处负责人转派中"),
    XXD_330001(330001, "项目经理受理中"),
    XXD_330002(330002, "项目经理受理拒绝"),
    XXD_330003(330003, "办事处负责人审核中"),
    XXD_330004(330004, "办事处负责人审核拒绝"),
    XXD_330005(330005, "风险部负责人审核中"),
    XXD_330006(330006, "风险部负责人审核拒绝"),
    XXD_330007(330007, "业务部负责人审核中"),
    XXD_330008(330008, "业务部负责人审核拒绝");


    private Integer code;
    private String description;

    ApplyStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }


    public static Integer getCodeByDesc(String description) {
        ApplyStatusEnum applyStatusEnum =  find(description);
        return applyStatusEnum.getCode();
    }

    public static ApplyStatusEnum find(String description) {
        if (StringUtils.isBlank(description)) {
            // TODO: 2023/4/7
//            throw new RuntimeException("订单贷款状态不能为空");
            return ApplyStatusEnum.XXD_220013;
                    
        }

        ApplyStatusEnum result = null;
        for(ApplyStatusEnum applyStatusEnum:ApplyStatusEnum.values()){
            if(description.equals(applyStatusEnum.getDescription())){
                result = applyStatusEnum;
                break;
            }
        }

        return result;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
