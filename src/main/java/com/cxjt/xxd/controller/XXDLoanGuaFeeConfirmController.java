package com.cxjt.xxd.controller;

import com.cxjt.xxd.dao.XXDLoanApplyBillDao;
import com.cxjt.xxd.model.res.XXDCustomApiResult;
import com.cxjt.xxd.component.AttachmentFieldServiceComponent;
import com.cxjt.xxd.component.LoanApplyStatusComponent;
import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.enums.BillResNotityStatusEnum;
import com.cxjt.xxd.enums.BillStatusEnum;
import com.cxjt.xxd.enums.XXDErrorCodeEnum;
import com.cxjt.xxd.dao.XXDOrgMappingDao;
import com.cxjt.xxd.model.req.guarant.GuaranteeAgreementReq;
import com.cxjt.xxd.model.req.guarant.GuaranteeConfirmReq;
import com.cxjt.xxd.model.req.guarant.GuaranteeFeeReq;
import com.cxjt.xxd.model.req.guarant.TransferInfoReq;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.tx.TX;
import kd.bos.db.tx.TXHandle;
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
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.workflow.WorkflowServiceHelper;
import kd.bos.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@ApiController(value = "xxd", desc = "鑫湘贷贷款担保费确认服务")
@ApiMapping(value = "/xxd/guarant/fee")
public class XXDLoanGuaFeeConfirmController implements Serializable {

    private Log logger = LogFactory.getLog(this.getClass());

    private final String ENTITY_NAME = FormConstant.LOAN_GUA_CONFIRM_BILL_ENTITY_NAME;

    @Validated
    @ApiPostMapping(value = "confirm", desc = "担保费确认")
    @ApiErrorCodes({
            @ApiErrorCode(code = "0000", desc = "成功"),
            @ApiErrorCode(code = "0002", desc = "数据不存在"),
            @ApiErrorCode(code = "0003", desc = "订单处于流程运转中"),
            @ApiErrorCode(code = "403", desc = "没有此接口访问权限"),
            @ApiErrorCode(code = "604", desc = "重复请求"),
            @ApiErrorCode(code = "不为0000", desc = "0000之外的值均为失败"),

    })
    public CustomApiResult<@ApiResponseBody("返回参数") String> confirm(@NotNull @Valid @ApiRequestBody(value = "入参") GuaranteeConfirmReq guaConfirm) {
        logger.info("接收到担保费确认单请求,请求报文待加密,请求信息如下:{}", JsonUtil.format(guaConfirm));
        //初始化担保费信息
        GuaranteeFeeReq guaranteeFee = guaConfirm.getGuaranteeFee();
        //业务编号
        String ukwoApplyId = guaranteeFee.getUkwoApplyId();

        //查询该贷款申请订单是否存在
        DynamicObject loanOrderBill = XXDLoanApplyBillDao.queryOne(ukwoApplyId);
        boolean isExist = loanOrderBill != null;
        if (!isExist) {
            logger.error("贷款申请订单[{}]不存在,请联系系统管理员", ukwoApplyId);
            throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.DOES_NOT_EXIST.getCode(), XXDErrorCodeEnum.DOES_NOT_EXIST.getDescription()));
        }

        //查询是否已经走完线下尽调流程(单据是否在流程中)
        String loanOrderId = loanOrderBill.get("id").toString();
        boolean inProcess = WorkflowServiceHelper.inProcess(loanOrderId);
        //若该贷款申请订单处于流转状态(线下尽调申请流程结束之后,才会发起借款流程,才会缴纳保费)
        if (inProcess) {
            logger.error("贷款申请订单处于流程流转状态,暂未结束,ukwoApplyId={},loanOrderId={}", ukwoApplyId, loanOrderId);
            throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.IN_PROCESS.getCode(), "贷款申请订单处于流程运转中"));
        }


        DynamicObject feeConfirmBill = BusinessDataServiceHelper.newDynamicObject(ENTITY_NAME);
        //初始化基础信息
        feeConfirmBill.set("billstatus", BillStatusEnum.A.getCode());
        //担保费确认结果通知,默认为未执行,待项目经理审核后,请求金融超市,并更新状态
        feeConfirmBill.set("ukwo_pay_notity_status", BillResNotityStatusEnum.NOT_YET.getCode());


        //配置工具配置编码规则后,通过SaveServiceHelper.saveOperate API可触发
        //feeConfirmBill.set("billno", ID.genLongId());


        //企业名称
        String ukwoCompanyName = guaranteeFee.getUkwoCompanyName();
        //担保金额(单位:万元)
        BigDecimal ukwoGuarantAmount = guaranteeFee.getUkwoGuarantAmount();
        if (ukwoGuarantAmount.compareTo(new BigDecimal(0)) != 1) {
            throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.AMOUNT_GREATER_THAN_ZERO.getCode(), XXDErrorCodeEnum.AMOUNT_GREATER_THAN_ZERO.getDescription()));
        }

        //担保费(单位:万元)
        BigDecimal ukwoGuaranteeFeeAmount = guaranteeFee.getUkwoGuaranteeFeeAmount();
        if (ukwoGuaranteeFeeAmount.compareTo(new BigDecimal(0)) != 1) {
            throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.AMOUNT_GREATER_THAN_ZERO.getCode(), XXDErrorCodeEnum.AMOUNT_GREATER_THAN_ZERO.getDescription()));
        }

        //所属区域(金融超市侧区域编码)
        long jrcsRegionCode = guaranteeFee.getUkwoRegionCode();
        long ukwoRegionCode = 0L;

        try {
            ukwoRegionCode = XXDOrgMappingDao.getUkwoBosOrgId(jrcsRegionCode);
        } catch (Exception e) {
            logger.error("[{}]提交担保费确认单查询组织机构映射异常,信息如下{}:", ukwoApplyId, e);
            return XXDCustomApiResult.fail(XXDErrorCodeEnum.FAILED.getCode(), e.getMessage());
        }

        feeConfirmBill.set("ukwo_apply_id", ukwoApplyId);
        feeConfirmBill.set("ukwo_company_name", ukwoCompanyName);
        feeConfirmBill.set("ukwo_guarant_amount", ukwoGuarantAmount);
        feeConfirmBill.set("ukwo_guarantee_fee_amount", ukwoGuaranteeFeeAmount);
        feeConfirmBill.set("ukwo_region_code", ukwoRegionCode);
        //默认为一次性缴纳
        feeConfirmBill.set("ukwo_payment_method", "一次性缴纳");

        //初始化汇款信息
        TransferInfoReq transferInfo = guaConfirm.getTransferInfo();
        //汇款记录编号
        String ukwoRecordId = transferInfo.getUkwoRecordId();
        //户名
        String ukwoAccountName = transferInfo.getUkwoAccountName();
        //银行卡号
        String ukwoBankCardNo = transferInfo.getUkwoBankCardNo();
        //汇款金额(单位:万元)
        BigDecimal ukwoFeeAmount = transferInfo.getUkwoFeeAmount();
        if (ukwoFeeAmount.compareTo(new BigDecimal(0)) != 1) {
            throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.AMOUNT_GREATER_THAN_ZERO.getCode(), XXDErrorCodeEnum.AMOUNT_GREATER_THAN_ZERO.getDescription()));
        }

        //汇款时间,格式:yyyy-MM-dd HH:mm:ss(24小时制)
        String ukwoTranferDate = transferInfo.getUkwoTranferDate();

        feeConfirmBill.set("ukwo_record_id", ukwoRecordId);
        feeConfirmBill.set("ukwo_account_name", ukwoAccountName);
        feeConfirmBill.set("ukwo_bank_card_no", ukwoBankCardNo);
        feeConfirmBill.set("ukwo_transfer_fee_amount", ukwoFeeAmount);
        feeConfirmBill.set("ukwo_transfer_date", ukwoTranferDate);

        //相关协议信息(贷款订单申请-单据体附件)
        List<GuaranteeAgreementReq> agreementList = guaConfirm.getAgreementList();
        if (CollectionUtils.isEmpty(agreementList)) {
            throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.FAILED.getCode(), "相关协议信息不能为空"));
        }

        //将单据、单据体以及单据体附件一并查出
        DynamicObject fullOrderBill = LoanApplyStatusComponent.queryOrderByPK(Long.parseLong(loanOrderId));
        //反担保信息
        DynamicObjectCollection guaranteeEntityList = fullOrderBill.getDynamicObjectCollection("entryentity");

        for (GuaranteeAgreementReq guaranteeAgreement : agreementList) {
            try {
                String ukwoGuaUniCodeEnt = guaranteeAgreement.getUkwoGuaUniCodeEnt();
                String ukwoSignerIdcard = guaranteeAgreement.getUkwoSignerIdcard();
                String ukwoAttachmentType = guaranteeAgreement.getUkwoAttachmentType();
                //金融超市传送过来,无扩展名
                String ukwoAttachmentName = guaranteeAgreement.getUkwoAttachmentName();
                String ukwoAttachmentUrl = guaranteeAgreement.getUkwoAttachmentUrl();
                int index = ukwoAttachmentUrl.lastIndexOf(".");
                //扩展名
                String type = ukwoAttachmentUrl.substring(index);
                //文件名去掉前后的空格
                ukwoAttachmentName = ukwoAttachmentName.trim();
                ukwoAttachmentName = ukwoAttachmentName + type;

                //绑定申请者签署的<<委托担保合同>>、<<法律文书送达地址确认书>> <<廉洁风险告知及反馈函>>
                //绑定反担保人签署的<<最高额反担保保证合同>>、<<法律文书送达地址确认书>>

                for (DynamicObject guaranteeEntity : guaranteeEntityList) {
                    //DynamicObject entityRow = guaranteeEntity;
                    long guaEntityId = (long) guaranteeEntity.get("id");
                    boolean condition = (guaEntityId == Long.parseLong(ukwoGuaUniCodeEnt));
                    if (condition) {
                        boolean containsFlag = AttachmentFieldServiceComponent.contain(guaranteeEntity, ukwoAttachmentName);
                        //boolean containsFlag = false;
                        //若未包含,则绑定
                        if (!containsFlag) {
                            long attachmentId = AttachmentFieldServiceComponent.buildAttachmentDataFromEdit(ukwoAttachmentUrl, ukwoAttachmentName);
                            AttachmentFieldServiceComponent.bind(attachmentId, guaranteeEntity, FormConstant.LOAN_APPLY_ORDER_ENTITY_ATTACHMENT_FIELD_NAME);
                        }

                    }

                }
            } catch (Exception e) {
                logger.error("[{}]提交担保费确认单,处理相关协议信息异常,信息如下{}:", ukwoApplyId, e);
                throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.FAILED.getCode(), "相关协议信息处理异常"));
            }


        }

        //事务一致性处理
        try (TXHandle h = TX.required("ukwo_xxd_gua_confirm_bill_submit")) {
            try {
                SaveServiceHelper.save(new DynamicObject[]{fullOrderBill});
                OperationResult result = OperationServiceHelper.executeOperate("submit", ENTITY_NAME, new DynamicObject[]{feeConfirmBill}, OperateOption.create());

                boolean isSuccess = result.isSuccess();
                if (!isSuccess) {
                    //logger.error("[{}]提交担保费确认单失败,信息如下{}:", ukwoApplyId, result.toString());
                    throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.FAILED.getCode(), result.toString()));
                }

                return XXDCustomApiResult.success(XXDErrorCodeEnum.SUCCESS.getCode(), "成功提交保费确认单", "");
            } catch (Throwable e) {
                h.markRollback();
                logger.error("[{}]提交担保费确认单异常,信息如下{}:", ukwoApplyId, e);
                throw e;
            }
        }


    }

}
