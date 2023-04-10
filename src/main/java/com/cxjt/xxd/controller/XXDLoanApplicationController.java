package com.cxjt.xxd.controller;

import com.cxjt.xxd.component.GuaFeeConfirmComponent;
import com.cxjt.xxd.config.XXDConfig;
import com.cxjt.xxd.dao.XXDLoanApplyBillDao;
import com.cxjt.xxd.model.res.XXDCustomApiResult;
import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.enums.BillResNotityStatusEnum;
import com.cxjt.xxd.enums.BillStatusEnum;
import com.cxjt.xxd.enums.XXDErrorCodeEnum;
import com.cxjt.xxd.dao.XXDOrgMappingDao;
import com.cxjt.xxd.model.req.order.*;
import com.cxjt.xxd.service.XXDLoanApplicationService;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.custom.annotation.*;
import kd.bos.openapi.common.result.CustomApiResult;
import kd.bos.openapi.common.util.JsonUtil;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.annotation.Validated;


import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 所有数据通过业务编号applyId关联,一个applyId只能提交一次
 */
@ApiController(value = "xxd", desc = "鑫湘贷贷款申请服务")
@ApiMapping(value = "/xxd/loanApplication")
public class XXDLoanApplicationController implements Serializable {

    private final static Log logger = LogFactory.getLog(XXDLoanApplicationController.class);

    private final static String ENTITY_NAME = FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME;

    private final XXDLoanApplicationService loanApplicationService = new XXDLoanApplicationService();

    @Validated
    @ApiPostMapping(value = "submit", desc = "上送贷款申请订单")
    @ApiErrorCodes({
            @ApiErrorCode(code = "0000", desc = "成功"),
            @ApiErrorCode(code = "0001", desc = "数据已存在"),
            @ApiErrorCode(code = "400", desc = "数据校验错误"),
            @ApiErrorCode(code = "403", desc = "没有此接口访问权限"),
            @ApiErrorCode(code = "604", desc = "重复请求"),
            @ApiErrorCode(code = "不为0000", desc = "0000之外的值均为失败"),

    })
    public CustomApiResult<@ApiResponseBody("返回参数") String> submit(@NotNull @Valid @ApiRequestBody(value = "入参") ApplyOrderReq applyOrderReq) {
        logger.info("接收到上送贷款申请订单请求,请求报文待加密,请求信息如下:{}", JsonUtil.format(applyOrderReq));
        String ukwoApplyId = "";
        try {
            XXDLoanApplicationValidator.validate(applyOrderReq);
            DynamicObject loanOrderBill = BillBuilder.build(applyOrderReq);

            loanApplicationService.preProcess(applyOrderReq,loanOrderBill);

            //业务编号
            ukwoApplyId = (String) loanOrderBill.get("ukwo_apply_id");

            //初始化反担保信息
            List<GuaranteeInfoReq> guaranteeInfoList = applyOrderReq.getGuaranteeInfoList();

            //将【申请人信息】放在第一行
            List<GuaranteeInfoReq> applicants = guaranteeInfoList.stream().filter(item -> GuaFeeConfirmComponent.isApplicant(item.getUkwoGuaSignerType(), item.getUkwoRelationType())).collect(Collectors.toList());
            if (applicants.size() != 1) {
                throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.FAILED.getCode(), "业务单据有且只能有一位申请者"));
            }
            //【申请人信息】
            GuaranteeInfoReq applicant = applicants.get(0);
            //【申请人信息】放首位的List
            List<GuaranteeInfoReq> orderGuaInfoList = new ArrayList(guaranteeInfoList.size());
            orderGuaInfoList.add(applicant);

            for (GuaranteeInfoReq guaInfoReq : guaranteeInfoList) {
                boolean flag = GuaFeeConfirmComponent.isApplicant(guaInfoReq.getUkwoGuaSignerType(), guaInfoReq.getUkwoRelationType());
                if (!flag) {
                    orderGuaInfoList.add(guaInfoReq);
                }
            }

            //获取[反担保信息]单据体数据集
            DynamicObjectCollection entryentity = loanOrderBill.getDynamicObjectCollection("entryentity");
            //遍历【申请人信息】放首位的List
            for (int i = 0; i < orderGuaInfoList.size(); i++) {
                GuaranteeInfoReq guaranteeInfo = orderGuaInfoList.get(i);
                //获取单据体数据类型
                DynamicObject entry = new DynamicObject(entryentity.getDynamicObjectType());
                entry.set("ukwo_gua_signer_type", guaranteeInfo.getUkwoGuaSignerType());
                entry.set("ukwo_relation_type", guaranteeInfo.getUkwoRelationType());
                entry.set("ukwo_company_name_ent", guaranteeInfo.getUkwoCompanyNameEnt());
                entry.set("ukwo_u_soc_cre_code_ent", guaranteeInfo.getUkwoUSocCreCodeEnt());
                entry.set("ukwo_signer_name", guaranteeInfo.getUkwoSignerName());
                entry.set("ukwo_signer_idcard", guaranteeInfo.getUkwoSignerIdcard());
                entry.set("ukwo_signer_phone", guaranteeInfo.getUkwoSignerPhone());
                //设置[单据体]行号,区别申请者(本人信息)与反担保信息,前端展示时,需要将第一行申请者(本人信息)放到【申请者(本人信息)】面板
                entry.set("ukwo_row_num", String.valueOf((i + 1)));

                entryentity.add(entry);
            }


            boolean isExist = XXDLoanApplyBillDao.exists(ukwoApplyId);
            if (isExist) {
                throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.HAVE_EXIST.getCode(), XXDErrorCodeEnum.HAVE_EXIST.getDescription()));
            }

            //OperationResult result = SaveServiceHelper.saveOperate(ENTITY_NAME, new DynamicObject[]{loanOrderBill}, OperateOption.create());
            //操作接口会根据用户的组织和功能数据权限对业务进行权限管控
            OperationResult result = OperationServiceHelper.executeOperate("submit", ENTITY_NAME, new DynamicObject[]{loanOrderBill}, OperateOption.create());
            boolean isSuccess = result.isSuccess();
            if (!isSuccess) {
                throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.FAILED.getCode(), result.toString()));
            }

            logger.info("[{}]申请单提交成功", ukwoApplyId);

            return XXDCustomApiResult.success(XXDErrorCodeEnum.SUCCESS.getCode(), "成功提交贷款申请单", "");

        } catch (Exception e) {
            logger.error("提交贷款申请单[{}]异常,信息如下{}:", ukwoApplyId, e);
            if (e instanceof KDBizException) {
                KDBizException bizExc = (KDBizException) e;
                ErrorCode errorCode = bizExc.getErrorCode();

                return XXDCustomApiResult.fail(errorCode.getCode(), errorCode.getMessage());
            }
            return XXDCustomApiResult.fail(XXDErrorCodeEnum.FAILED.getCode(), e.getMessage());
        }


    }

    static class BillBuilder {
        static DynamicObject build(ApplyOrderReq applyOrderReq) {
            DynamicObject loanOrderBill = BusinessDataServiceHelper.newDynamicObject(ENTITY_NAME);

            //初始化基础信息
            loanOrderBill.set("billstatus", BillStatusEnum.A.getCode());
            //初始化尽调结果通知状态,默认未执行
            loanOrderBill.set("ukwo_loan_notity_status", BillResNotityStatusEnum.NOT_YET.getCode());

            //获取查询订单列表时,按创建时间降序排列
            loanOrderBill.set("ukwo_create_time", new Date());

            //配置工具配置编码规则后,通过SaveServiceHelper.saveOperate API可触发
            //loanOrderBill.set("billno", ID.genLongId());

            //初始化[业务属性]
            //默认值处理
            loanOrderBill.set("ukwo_cust_nature", "企业");
            loanOrderBill.set("ukwo_prod_name", "鑫湘e贷");
            loanOrderBill.set("ukwo_business_type", "300万及以下业务");

            //初始化[客户信息]
            CustInfoReq custInfo = applyOrderReq.getBusiInfo().getCustInfo();
            String ukwoCompanyName = custInfo.getUkwoCompanyName();
            String ukwoUniSociCreditCode = custInfo.getUkwoUniSociCreditCode();
            String ukwoLegalPersonName = custInfo.getUkwoLegalPersonName();
            String ukwoLegalPersonIdcard = custInfo.getUkwoLegalPersonIdcard();
            String ukwoLegalPersonPhone = custInfo.getUkwoLegalPersonPhone();
            String ukwoContactName = custInfo.getUkwoContactName();
            String ukwoContactIdcard = custInfo.getUkwoContactIdcard();
            String ukwoContactPhone = custInfo.getUkwoContactPhone();
            //所属区域(金融超市侧区域编码)
            long jrcsRegionCode = custInfo.getUkwoRegionCode();
            DynamicObject  xxdOrgMappingObj = XXDOrgMappingDao.queryOne(jrcsRegionCode);

            //所属地区,取【金融超市组织机构】映射中的【金融超市组织机构名称】
            String ukwoJrcsOrgName = (String)xxdOrgMappingObj.get("ukwo_jrcs_org_name");
            loanOrderBill.set("ukwo_jrcs_org_name", ukwoJrcsOrgName);
            //所属区域(金融超市侧区域编码)(依据此字段,生成财科担保负责人审核代办任务)
            loanOrderBill.set("ukwo_jrcs_org_code", jrcsRegionCode+"");

            long ukwoRegionCode = XXDOrgMappingDao.getUkwoBosOrgId(jrcsRegionCode);
            DynamicObject orgObj = BusinessDataServiceHelper.loadSingle(ukwoRegionCode, FormConstant.BOS_ORG);

            //企业名称
            loanOrderBill.set("ukwo_company_name", ukwoCompanyName);
            //统一社会信用代码
            loanOrderBill.set("ukwo_uni_soci_credit_code", ukwoUniSociCreditCode);
            //法人姓名
            loanOrderBill.set("ukwo_legal_person_name", ukwoLegalPersonName);
            //法人身份证号
            loanOrderBill.set("ukwo_legal_person_idcard", ukwoLegalPersonIdcard);
            //法人手机号
            loanOrderBill.set("ukwo_legal_person_phone", ukwoLegalPersonPhone);
            //联系人姓名
            loanOrderBill.set("ukwo_contact_name", ukwoContactName);
            //联系人身份证号
            loanOrderBill.set("ukwo_contact_idcard", ukwoContactIdcard);
            //联系人手机号
            loanOrderBill.set("ukwo_contact_phone", ukwoContactPhone);
            //申请订单所属区域(依据此字段,生成相应办事处负责人转派任务)
            loanOrderBill.set("ukwo_region_code", orgObj);
            loanOrderBill.set("org", orgObj);

            //初始化业务信息
            ChildBusinessInfoReq cBusinessInfoReq = applyOrderReq.getBusiInfo().getChildBusinessInfoReq();
            //业务编号
            String ukwoApplyId = cBusinessInfoReq.getUkwoApplyId();
            //项目名称
            String ukwoProjectName = cBusinessInfoReq.getUkwoProjectName();
            //申保日期
            String ukwoApplicationDate = cBusinessInfoReq.getUkwoApplicationDate();
            //申保金额
            BigDecimal ukwoApplicationAmount = cBusinessInfoReq.getUkwoApplicationAmount();
            //申保期限（月）
            int ukwoApplicationPeriod = cBusinessInfoReq.getUkwoApplicationPeriod();
            //担保费率（%）
            BigDecimal ukwoGuarantedRates = cBusinessInfoReq.getUkwoGuarantedRates();
            //360信用分
            int ukwo360CreditScore = cBusinessInfoReq.getUkwo360CreditScore();
            //贷款(融资)用途
            String ukwoLoanUsage = cBusinessInfoReq.getUkwoLoanUsage();
            //上一年度营收
            BigDecimal ukwoLastYearRevenue = cBusinessInfoReq.getUkwoLastYearRevenue();
            //上年度资产总额
            BigDecimal ukwoLastYearTotalAsse = cBusinessInfoReq.getUkwoLastYearTotalAsse();
            //上一年度缴纳税收
            BigDecimal ukwoLastYearPayTaxes = cBusinessInfoReq.getUkwoLastYearPayTaxes();
            //经办网点
            String ukwoHandlingOutlet = cBusinessInfoReq.getUkwoHandlingOutlet();
            //客户经理
            String ukwoCustManager = cBusinessInfoReq.getUkwoCustManager();
            //客户经理手机号
            String ukwoCustPhone = cBusinessInfoReq.getUkwoCustPhone();

            //提示类信息
            String ukwoTips = cBusinessInfoReq.getUkwoTips();
            //提示类信息:支持多行显示;最多显示500字
            ukwoTips = StringUtils.isBlank(ukwoTips) ? "" : ukwoTips.length() > 500?ukwoTips.substring(0,500):ukwoTips;

            loanOrderBill.set("ukwo_apply_id", ukwoApplyId);
            loanOrderBill.set("ukwo_project_name", ukwoProjectName);
            loanOrderBill.set("ukwo_application_date", ukwoApplicationDate);
            loanOrderBill.set("ukwo_application_amount", ukwoApplicationAmount);
            loanOrderBill.set("ukwo_application_period", ukwoApplicationPeriod);
            loanOrderBill.set("ukwo_guaranted_rates", ukwoGuarantedRates);
            loanOrderBill.set("ukwo_360_credit_score", ukwo360CreditScore);
            loanOrderBill.set("ukwo_loan_usage", ukwoLoanUsage);
            loanOrderBill.set("ukwo_last_year_revenue", ukwoLastYearRevenue);
            loanOrderBill.set("ukwo_last_year_total_asse", ukwoLastYearTotalAsse);
            loanOrderBill.set("ukwo_last_year_pay_taxes", ukwoLastYearPayTaxes);
            loanOrderBill.set("ukwo_handling_outlet", ukwoHandlingOutlet);
            loanOrderBill.set("ukwo_cust_manager", ukwoCustManager);
            loanOrderBill.set("ukwo_cust_phone", ukwoCustPhone);
            loanOrderBill.set("ukwo_tips", ukwoTips);
            loanOrderBill.set("ukwo_guarantee_type", "贷款类担保");
            loanOrderBill.set("ukwo_guarantee_subject", "财科担保");
            loanOrderBill.set("ukwo_apportionment_mode", "二八分摊");
            loanOrderBill.set("ukwo_fina_insti_type", "银行金融机构");
            loanOrderBill.set("ukwo_gua_fee_ch_method", "一次性缴纳");
            loanOrderBill.set("ukwo_anti_guarantee_mode", "保证担保");
            loanOrderBill.set("ukwo_project_source", "前置申请");
            loanOrderBill.set("ukwo_loan_bank", "湖南银行");
            loanOrderBill.set("ukwo_loan_bank_3rd", "常德分行");
            loanOrderBill.set("ukwo_due_dil_mode", "自主尽调");
            //是否现场尽调,默认为“是”
            loanOrderBill.set("ukwo_is_locale_due_dil", FormConstant.Y);
            //是否为平台关联企业,默认为“否”
            loanOrderBill.set("ukwo_is_p_ass_enterprise", FormConstant.N);
            //是否由平台提供反担保,默认为“否”
            loanOrderBill.set("ukwo_is_pro_gua_by_plat", FormConstant.N);


            //初始化申保状态
            //关联总额度(万元),默认为“0”
            loanOrderBill.set("ukwo_total_ass_amount", new BigDecimal(0));

            //测算额度(万元),等于申保金额
            loanOrderBill.set("ukwo_calc_amount", ukwoApplicationAmount);

            //初始化360报告
            Report360Req report360 = applyOrderReq.getReport360();
            String ukwo360ReportUrl = report360.getUkwo360ReportUrl();
            //金融超市生成的360报告PDF文件URL地址
            loanOrderBill.set("ukwo_360_report_url", ukwo360ReportUrl);

            //风险审核部门(初始化鑫湘贷风险部审核角色使用)
            loanOrderBill.set("ukwo_org_risk", XXDConfig.getRiskDeptId());
            //财科融资担保审核(初始化鑫湘贷财科担保审核角色使用)
            loanOrderBill.set("ukwo_org_ckdb", XXDConfig.getCkdbOrgId());



            return loanOrderBill;
        }
    }

    static class XXDLoanApplicationValidator {

        static void validate(ApplyOrderReq applyOrderReq) {

            CustInfoReq custInfo = applyOrderReq.getBusiInfo().getCustInfo();
            String ukwoLegalPersonName = custInfo.getUkwoLegalPersonName();
            String ukwoLegalPersonIdcard = custInfo.getUkwoLegalPersonIdcard();
            String ukwoLegalPersonPhone = custInfo.getUkwoLegalPersonPhone();
            String ukwoContactName = custInfo.getUkwoContactName();
            String ukwoContactIdcard = custInfo.getUkwoContactIdcard();
            String ukwoContactPhone = custInfo.getUkwoContactPhone();

            boolean conditionName = StringUtils.isNotBlank(ukwoLegalPersonName) && StringUtils.isNotBlank(ukwoContactName) && StringUtils.equals(ukwoLegalPersonName, ukwoContactName);

            boolean conditionIdcard = StringUtils.isNotBlank(ukwoLegalPersonIdcard) && StringUtils.isNotBlank(ukwoContactIdcard) && StringUtils.equals(ukwoLegalPersonIdcard, ukwoContactIdcard);

            boolean conditionPhone = StringUtils.isNotBlank(ukwoLegalPersonPhone) && StringUtils.isNotBlank(ukwoContactPhone) && StringUtils.equals(ukwoLegalPersonPhone, ukwoContactPhone);

            boolean condition = conditionName && conditionIdcard && conditionPhone;

            if (!condition) {
                throw new RuntimeException("法人信息与联系人信息不一致");
            }

            //反担保信息
            List<GuaranteeInfoReq> guaranteeInfoList = applyOrderReq.getGuaranteeInfoList();
            if (CollectionUtils.isEmpty(guaranteeInfoList)) {
                throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.FAILED.getCode(), "反担保信息不能为空"));
            }
        }
    }
}
