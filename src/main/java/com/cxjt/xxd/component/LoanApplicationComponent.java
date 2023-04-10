package com.cxjt.xxd.component;

import com.alibaba.fastjson.JSONObject;
import com.cxjt.xxd.config.RemoteConfig;
import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.enums.BillResNotityStatusEnum;
import com.cxjt.xxd.enums.GuarantSignerTypeEnum;
import com.cxjt.xxd.enums.JrcsErrorCodeEnum;
import com.cxjt.xxd.model.res.order.ApplyOrderRes;
import com.cxjt.xxd.model.res.order.GuaranteeInfoRes;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.util.JsonUtil;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.workflow.component.approvalrecord.IApprovalRecordItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 尽调结果通知
 */
public class LoanApplicationComponent {

    private final static Log logger = LogFactory.getLog(LoanApplicationComponent.class);

    private static final String JRCS_PROTOCOL = RemoteConfig.getJrcsProtocol();
    private static final String HOST = RemoteConfig.getJrcsHost();
    private static final int PORT = RemoteConfig.getJrcsPort();

    private static final String PATH = RemoteConfig.getJrcsGuaranteeNoticePath();
    private static final String URL = JRCS_PROTOCOL + "://" + HOST + ":" + PORT + PATH;

    public static String execute(ApplyOrderRes applyOrderRes) throws Exception {
        String applyId = applyOrderRes.getApplyId();
        Map<String, Object> paraMap = new HashMap<>();
        paraMap.put("body", applyOrderRes);

        String jsonData = JsonUtil.format(paraMap);
        logger.info("开始请求JRCS尽调结果通知接口,参数如下:applyId={},jsonData={}", applyId, jsonData);
        String responseStr = HttpService.getService().doPostByHttpClient(URL, jsonData);
        logger.info("JRCS尽调结果通知接口响应信息:applyId={},responseStr={}", applyId, responseStr);
        return responseStr;
    }


    public static void auditResultNotify(DynamicObject loanApplicationBill) throws Exception {
        String ukwoLoanNotityStatus = (String) loanApplicationBill.get("ukwo_loan_notity_status");
        String ukwoApplyId = (String) loanApplicationBill.get("ukwo_apply_id");

        //跳过已经通知成功的数据
        if (BillResNotityStatusEnum.SUCCESS.getCode().equals(ukwoLoanNotityStatus)) {
            logger.info("跳过已成功通知JRCS尽调结果的单据applyId={}", ukwoApplyId);
            return;
        }

        String businessKey = String.valueOf(loanApplicationBill.get("id"));
        IApprovalRecordItem approvalRecordItem = WorkflowComponent.getApprovalRecordItem(businessKey);
        ApplyOrderRes applyOrderRes = convert(loanApplicationBill, approvalRecordItem);


        //审批通过的申请单,将表单信息写入[附件定时任务]
        //[此处不做事务控制]若将申请单信息写入[附件定时任务]表出现异常,可手动同步一次做补偿[贷款申请订单列表>>同步审核结果]

        //2为金融超市侧成功标识
        if ("2".equals(applyOrderRes.getToneDownResult())) {
            LoanApplyAttComponent.initTask(loanApplicationBill);
        }


        String responseStr = LoanApplicationComponent.execute(applyOrderRes);

        JSONObject jsonResult = JSONObject.parseObject(responseStr);
        String code = (String) jsonResult.get("code");
        String msg = (String) jsonResult.get("msg");

        loanApplicationBill.set("ukwo_loan_notity_code", code);
        loanApplicationBill.set("ukwo_loan_notice_msg", msg);


        if (JrcsErrorCodeEnum.SUCCESS.getCode().equals(code)) {
            loanApplicationBill.set("ukwo_loan_notity_status", BillResNotityStatusEnum.SUCCESS.getCode());
        } else {
            loanApplicationBill.set("ukwo_loan_notity_status", BillResNotityStatusEnum.FAILED.getCode());
        }
        //更新贷款尽调申请单通知状态
        SaveServiceHelper.update(new DynamicObject[]{loanApplicationBill});

    }

    public static ApplyOrderRes convert(DynamicObject loanApplicationBill, String decisionType, String auditTime, String approveDesc) {
        //业务编号
        String ukwoApplyId = (String) loanApplicationBill.get("ukwo_apply_id");

        DynamicObject ukwoProjectManager = (DynamicObject) loanApplicationBill.get("ukwo_project_manager");

        long userId = (long) ukwoProjectManager.get("id");
        Map<String, Object> userMap = UserServiceHelper.getUserInfoByID(userId);

        //项目经理
        String projectManager = String.valueOf(userMap.get("name"));
        //项目经理手机号
        String phoneNumber = String.valueOf(userMap.get("phone"));

        String businessKey = String.valueOf(loanApplicationBill.get("id"));
        //String formId = FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME;
        //Object pkId = businessKey;

        //申请者附件清单
        ApplyOrderRes applyOrderRes = new ApplyOrderRes();

        //反担保附件清单

        applyOrderRes.setApplyId(ukwoApplyId);

        applyOrderRes.setProjectManager(projectManager);
        applyOrderRes.setPhoneNumber(phoneNumber);

        logger.info("贷款尽调结果通知获取到的审批决策:ukwoApplyId={},decisionType={}", ukwoApplyId, decisionType);

        //尽调结果:2通过,3未通过
        String toneDownResult = FormConstant.APPROVE.equals(decisionType) ? "2" : "3";

        //若审批通过
        if (FormConstant.APPROVE.equals(decisionType)) {
            //审批金额(万元)【审批通过才返回】
            BigDecimal ukwoApproveAmount = (BigDecimal) loanApplicationBill.get("ukwo_approve_amount");
            //将审批金额转换为元[返回给JRCS需转换为元]
            BigDecimal approveAmount = ukwoApproveAmount.multiply(new BigDecimal(10000));
            //审批期限(月)【审批通过才返回】
            int ukwoApproveTerm = (int) loanApplicationBill.get("ukwo_approve_term");

            applyOrderRes.setApproveAmount(approveAmount.toPlainString());
            applyOrderRes.setGuaranteeTerm(String.valueOf(ukwoApproveTerm));

            //反担保信息(尽调结果通过必填)
            DynamicObjectCollection guaranteeEntityList = loanApplicationBill.getDynamicObjectCollection("entryentity");

            //反担保信息返回对象
            List<GuaranteeInfoRes> backGuaranteeInfoList = new ArrayList<>(guaranteeEntityList.size());

            for (DynamicObject guaranteeEntity : guaranteeEntityList) {
                GuaranteeInfoRes guaranteeInfo = LoanApplicationComponent.convert(guaranteeEntity);
                backGuaranteeInfoList.add(guaranteeInfo);
            }

            applyOrderRes.setBackGuaranteeInfo(backGuaranteeInfoList);
        }


        applyOrderRes.setToneDownResult(toneDownResult);
        applyOrderRes.setApproveDate(auditTime);
        applyOrderRes.setApproveDesc(approveDesc);


        return applyOrderRes;
    }

    public static ApplyOrderRes convert(DynamicObject loanApplicationBill, IApprovalRecordItem approvalRecordItem) {
        //获取决策项类型
        String decisionType = approvalRecordItem.getDecisionType();
        //审批时间
        String auditTime = approvalRecordItem.getTime();
        //审批意见
        String approveDesc = approvalRecordItem.getMessage();

        return convert(loanApplicationBill, decisionType, auditTime, approveDesc);
    }

    public static GuaranteeInfoRes convert(DynamicObject guaranteeEntity) {
        //反担保人姓名
        String ukwoSignerName = (String) guaranteeEntity.get("ukwo_signer_name");
        //签订类型:1个人,2企业
        String ukwoGuaSignerType = (String) guaranteeEntity.get("ukwo_gua_signer_type");
        //反担保人类型
        String ukwoRelationType = (String) guaranteeEntity.get("ukwo_relation_type");
        //身份证号码
        String ukwoSignerIdcard = (String) guaranteeEntity.get("ukwo_signer_idcard");
        //签署人联系方式
        String ukwoSignerPhone = (String) guaranteeEntity.get("ukwo_signer_phone");
        //住所
        String ukwoBusiLicAddress = (String) guaranteeEntity.get("ukwo_busi_lic_address");
        //住址
        String ukwoSignerAddress = (String) guaranteeEntity.get("ukwo_signer_address");

        //单据体行附件
        // DynamicObjectCollection entityAttCollection = (DynamicObjectCollection) guaranteeEntity.get(FormConstant.LOAN_APPLY_ORDER_ENTITY_ATTACHMENT_FIELD_NAME);
        GuaranteeInfoRes guaranteeInfoRes = new GuaranteeInfoRes();

        //个人时:身份证地址,企业时:营业执照住所
        String address = ukwoSignerAddress;
        //签订类型为企业时
        if (GuarantSignerTypeEnum.ENTERPRISE.getCode().equals(ukwoGuaSignerType)) {
            address = ukwoBusiLicAddress;
            //企业名称
            String ukwoCompanyNameEnt = (String) guaranteeEntity.get("ukwo_company_name_ent");
            //统一社会信用代码
            String ukwoUSocCreCodeEnt = (String) guaranteeEntity.get("ukwo_u_soc_cre_code_ent");

            guaranteeInfoRes.setEnterpriseName(ukwoCompanyNameEnt);
            guaranteeInfoRes.setSocialCode(ukwoUSocCreCodeEnt);

        }


        guaranteeInfoRes.setPersonName(ukwoSignerName);
        guaranteeInfoRes.setContractType(ukwoGuaSignerType);
        guaranteeInfoRes.setPersonType(ukwoRelationType);
        guaranteeInfoRes.setIdCard(ukwoSignerIdcard);
        guaranteeInfoRes.setPhoneNumber(ukwoSignerPhone);


        //单据体行ID
        String entryId = String.valueOf(guaranteeEntity.get("id"));
        guaranteeInfoRes.setAddress(address);
        guaranteeInfoRes.setUniCode(entryId);

        return guaranteeInfoRes;
    }

}
