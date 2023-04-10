package com.cxjt.xxd.component;

import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.dao.XXDLoanApplyBillAttTaskDao;
import com.cxjt.xxd.dao.XXDLoanApplyBillDao;
import com.cxjt.xxd.enums.AttachmentEnum;
import com.cxjt.xxd.enums.BillResNotityStatusEnum;
import com.cxjt.xxd.enums.XXDErrorCodeEnum;
import com.cxjt.xxd.model.bo.AttachmentBO;
import com.cxjt.xxd.model.res.order.FileInfoRes;
import com.cxjt.xxd.util.FileUtil;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.OrmLocaleValue;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDBizException;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LoanApplyAttComponent {

    /**
     * 将申请单信息写入[附件定时任务]表
     *
     * @param loanApplicationBill
     */
    public static void initTask(DynamicObject loanApplicationBill) {

        if (loanApplicationBill == null) {
            throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.FAILED.getCode(), "贷款申请单不能为空"));
        }

        long billId = (long) loanApplicationBill.get("id");
        String ukwoApplyBillId = String.valueOf(billId);

        //一张申请单对应一个附件任务,若不为空，说明之前已经写入,提前返回
        DynamicObject loanTaskBill = XXDLoanApplyBillAttTaskDao.queryByApplyBillId(ukwoApplyBillId);
        if(loanTaskBill != null){
            return;
        }

        String ukwoApplyId = (String) loanApplicationBill.get("ukwo_apply_id");
        String ukwoCompanyName = (String) loanApplicationBill.get("ukwo_company_name");
        String ukwoLegalPersonName = (String) loanApplicationBill.get("ukwo_legal_person_name");
        DynamicObject ukwoRegionCodeobj = (DynamicObject) loanApplicationBill.get("ukwo_region_code");
        String entityName = FormConstant.LOAN_APPLY_BILL_ATT_TASK_NAME;

        DynamicObject loanApplyTaskBill = BusinessDataServiceHelper.newDynamicObject(entityName);

        loanApplyTaskBill.set("ukwo_apply_bill_id", ukwoApplyBillId);
        //业务编号
        loanApplyTaskBill.set("ukwo_apply_id", ukwoApplyId);
        //企业名称
        loanApplyTaskBill.set("ukwo_company_name", ukwoCompanyName);
        //法人姓名
        loanApplyTaskBill.set("ukwo_legal_person_name", ukwoLegalPersonName);
        //所属区域
        loanApplyTaskBill.set("ukwo_region_code", ukwoRegionCodeobj);
        //附件总数默认为0
        loanApplyTaskBill.set("ukwo_att_count", 0);

        //执行状态,默认未执行
        loanApplyTaskBill.set("ukwo_exec_status", BillResNotityStatusEnum.NOT_YET.getCode());
        //启用状态,默认启用
        loanApplyTaskBill.set("ukwo_enable_status", FormConstant.ENABLE);
        //创建时间
        loanApplyTaskBill.set("ukwo_create_time", new Date());

        //在表单-保存操作-已对 [单据编号] + [启用状态] 做唯一性校验
        OperationResult result = SaveServiceHelper.saveOperate(entityName, new DynamicObject[]{loanApplyTaskBill}, OperateOption.create());
        boolean isSuccess = result.isSuccess();
        if (!isSuccess) {
            throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.FAILED.getCode(), result.toString()));
        }
    }


    public static void initTask(String applyId) {
        DynamicObject loanApplyBill = XXDLoanApplyBillDao.queryOne(applyId);
        initTask(loanApplyBill);
    }

    public static List<AttachmentBO> getBillTaskItem(long applyBillId) {

        long billId =applyBillId;

        String formId = FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME;
        //单据-申请者附件
        String attachKey = FormConstant.LOAN_APPLY_ORDER_APPLICANT_ATTACHMENT_PANEL_NAME;
        List<AttachmentBO> applicantAttachmentList = AttachmentServiceComponent.getAttachmentInfoList(formId, billId, attachKey, AttachmentEnum.I);

        //单据-反担保附件
        attachKey = FormConstant.LOAN_APPLY_ORDER_GUA_ATTACHMENT_PANEL_NAME;
        List<AttachmentBO> guaranteeAttachmentList = AttachmentServiceComponent.getAttachmentInfoList(formId, billId, attachKey, AttachmentEnum.K);


        //DynamicObject loanApplicationBill = BusinessDataServiceHelper.loadSingle(formId, XXDLoanApplicationBillListPlugin.SELECT_PROPERTIES, filters);
        DynamicObject loanApplicationBill = XXDLoanApplyBillDao.queryWithAttFieldByPK(billId);

        //单据体行附件集合
        List<AttachmentBO> entityAttList = new ArrayList<>(16);

        //遍历单据体行
        DynamicObjectCollection guaranteeEntityList = loanApplicationBill.getDynamicObjectCollection("entryentity");
        for (DynamicObject guaranteeEntity : guaranteeEntityList) {
            //单据体行ID
            long entityId = (long) guaranteeEntity.get("id");
            //单据体行附件字段
            DynamicObjectCollection entityAttCollection = (DynamicObjectCollection) guaranteeEntity.get(FormConstant.LOAN_APPLY_ORDER_ENTITY_ATTACHMENT_FIELD_NAME);

            for (DynamicObject dynamicItem : entityAttCollection) {
                DynamicObject basedataObj = (DynamicObject) dynamicItem.getDynamicObject("fbasedataid");
                OrmLocaleValue ormLocalValue = (OrmLocaleValue) basedataObj.get("name");
                //带有扩展名的文件
                String fullItemFileName = (String) ormLocalValue.get("zh_CN");
                //不带有扩展名的文件
                //String itemFileName = FileUtil.getNameWithOutExt(fullItemFileName);
                String url = (String) basedataObj.get("url");
                String attachmentFullUrl = AttachmentServiceComponent.getFullUrlByPathCode(url);
                //String base64Str = FileUtil.byte2Base64(attachmentFullUrl);
                String fileType = "";

                boolean condition1 = fullItemFileName.contains("身份证人像面");
                boolean condition2 = fullItemFileName.contains("身份证国徽面");
                boolean condition3 = fullItemFileName.contains("营业执照");
                boolean condition4 = fullItemFileName.contains("决议");
                boolean condition = condition1 || condition2 || condition3 || condition4;

                if (!condition) {
                    continue;
                }
                if (condition1) {
                    fileType = AttachmentEnum.A.getCode();
                }
                if (condition2) {
                    fileType = AttachmentEnum.B.getCode();
                }
                if (condition3) {
                    fileType = AttachmentEnum.C.getCode();
                }
                if (condition4) {
                    fileType = AttachmentEnum.D.getCode();
                }
                AttachmentBO attachmentItem = new AttachmentBO(fileType, fullItemFileName, attachmentFullUrl, String.valueOf(entityId));
                entityAttList.add(attachmentItem);
            }
        }

        int count = applicantAttachmentList.size() + guaranteeAttachmentList.size() + entityAttList.size();
        List<AttachmentBO> allAttachList = new ArrayList<>(count);

        allAttachList.addAll(applicantAttachmentList);
        allAttachList.addAll(guaranteeAttachmentList);
        allAttachList.addAll(entityAttList);

        return allAttachList;
    }
}
