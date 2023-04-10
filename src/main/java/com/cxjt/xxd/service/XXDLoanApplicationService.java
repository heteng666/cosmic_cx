package com.cxjt.xxd.service;

import com.cxjt.xxd.component.LoanApplyStatusComponent;
import com.cxjt.xxd.component.OrgUnitServiceComponent;
import com.cxjt.xxd.component.UserServiceComponent;
import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.dao.XXDBosOrgDao;
import com.cxjt.xxd.dao.XXDLoanApplyBillDao;
import com.cxjt.xxd.dao.XXDOrgMappingDao;
import com.cxjt.xxd.enums.ApplyStatusEnum;
import com.cxjt.xxd.enums.XXDErrorCodeEnum;
import com.cxjt.xxd.model.req.order.ApplyOrderReq;
import com.cxjt.xxd.model.req.order.CustInfoReq;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.servicehelper.workflow.WorkflowServiceHelper;

import java.util.*;

public class XXDLoanApplicationService {

    private final static Log logger = LogFactory.getLog(XXDLoanApplicationService.class);

    private static final XXDLoanApplyBillDao loanApplyBillDao = new XXDLoanApplyBillDao();

    //银行初审通过之后,才走担保流程,[担保审核拒绝][授信过期][银行复核拒绝][已还清]等处于终态的单据,可不必同步状态[已与产品经理确认终态]
    //考虑到历史数据等原因220010,以及220010细化之后的330002,330004,330006,330008等状态都必须是终态
    public static final List<Integer> finalStatus = Arrays.asList(new Integer[]{
            ApplyStatusEnum.XXD_220010.getCode(), ApplyStatusEnum.XXD_330002.getCode(),
            ApplyStatusEnum.XXD_330004.getCode(), ApplyStatusEnum.XXD_330006.getCode(), ApplyStatusEnum.XXD_330008.getCode(),
            ApplyStatusEnum.XXD_220012.getCode(), ApplyStatusEnum.XXD_220014.getCode(), ApplyStatusEnum.XXD_220020.getCode()
    });


    /**
     * 根据贷款申请单主键查询订单状态
     *
     * @param pkId
     * @return
     * @throws Exception
     */
    public static Map<String, Object> getLoanStatus(long pkId) throws Exception {
        //获取贷款申请单,将单据体附件字段信息一并查出
        DynamicObject orderBill = LoanApplyStatusComponent.queryOrderByPK(pkId);
        Object ukwOrderStatusCodeObj = orderBill.get("ukwo_order_status_code");

        if (ukwOrderStatusCodeObj != null) {
            Integer ukwOrderStatusCode = (Integer) ukwOrderStatusCodeObj;
            //若数据库中数据状态不为空且未达到终态
            if (finalStatus.indexOf(ukwOrderStatusCode) == -1) {
                //获取最新订单状态、绑定附件并更新数据库
                LoanApplyStatusComponent.process(orderBill);
                //获取更新之后的最新状态
                orderBill = XXDLoanApplyBillDao.loadSingle(pkId);
            }

        } else {
            //获取最新订单状态、绑定附件并更新数据库
            LoanApplyStatusComponent.process(orderBill);
            //获取更新之后的最新状态
            orderBill = XXDLoanApplyBillDao.loadSingle(pkId);
        }

        Map<String, Object> resultMap = new HashMap<>();
        String description = (String) orderBill.get("ukwo_order_status");
        resultMap.put("ukwo_order_status", description);
        resultMap.put("ukwo_order_status_code", ApplyStatusEnum.getCodeByDesc(description));

        return resultMap;
    }


    public void preProcess(ApplyOrderReq applyOrderReq, DynamicObject loanOrderBill) {

        CustInfoReq custInfo = applyOrderReq.getBusiInfo().getCustInfo();
        //业务编号
        String ukwoApplyId = applyOrderReq.getBusiInfo().getChildBusinessInfoReq().getUkwoApplyId();

        //统一社会信用代码
        String ukwoUniSociCreditCode = custInfo.getUkwoUniSociCreditCode();

        //根据统一社会信用代码查询最新一条贷款申请
        DynamicObject latest = queryLatestOneByCreditCode(ukwoUniSociCreditCode);

        if (latest != null) {
            //String id = latest.get("id").toString();
            //String latestApplyId = latest.get("ukwo_apply_id").toString();
            //boolean inProcess = WorkflowServiceHelper.inProcess(id);
            //若该贷款申请订单处于流转状态
            /*
            if (inProcess) {
                logger.error("贷款申请提交失败[reqUkwoApplyId={}],企业存在未结束的订单[latestApplyId={}],ukwoUniSociCreditCode={}", ukwoApplyId, latestApplyId, ukwoUniSociCreditCode);
                throw new KDBizException(new ErrorCode(XXDErrorCodeEnum.FAILED.getCode(), "企业存在未结束的订单"));
            }*/

            //获取项目经理
            DynamicObject proManager = (DynamicObject) latest.get("ukwo_project_manager");
            //获取项目经理ID
            long proMasterid = (long) proManager.get("masterid");
            //根据ID获取项目经理主要职务部门
            long proMainOrgId = UserServiceComponent.getUserMainOrgId(proMasterid);

            //组织机构映射单中所有组织
            List<Long> allMappingOrgIdList = XXDOrgMappingDao.queryAllOrgIds();

            //项目经理主要职务部门是否在[组织机构映射表中](从目前生产数据来看,除津市津鑫以外的各个办事处人员直接挂在办事处下,办事处相当于部门)
            if (allMappingOrgIdList.contains(proMainOrgId)) {
                DynamicObject orgObj = XXDBosOrgDao.queryById(proMainOrgId);
                //申请订单所属区域
                loanOrderBill.set("ukwo_region_code", orgObj);
                loanOrderBill.set("org", orgObj);
            } else {
                //若不满足以上条件,那么项目经理很大可能来自于津市津鑫,津市津鑫是公司,人员挂在部门下,部门上面是公司
                //获取直接上级组织
                Map<Long, Long> orglist = OrgUnitServiceComponent.getDirectSuperiorOrgForAdmin(Collections.singletonList(proMainOrgId));
                long proCompanyOrgId = orglist.get(proMainOrgId);
                boolean condition = allMappingOrgIdList.contains(proCompanyOrgId);
                if (condition) {
                    DynamicObject companyOrgObj = XXDBosOrgDao.queryById(proCompanyOrgId);
                    //申请订单所属区域
                    loanOrderBill.set("ukwo_region_code", companyOrgObj);
                    loanOrderBill.set("org", companyOrgObj);
                } else {
                    //若还不满足,执行循环兜底查询策略
                    for(Long mappingOrgId: allMappingOrgIdList){
                        boolean belongToFlag = UserServiceComponent.isUserBelongTo(proMasterid,mappingOrgId,true);
                        if(belongToFlag){
                            DynamicObject companyOrgObj = XXDBosOrgDao.queryById(mappingOrgId);
                            //申请订单所属区域
                            loanOrderBill.set("ukwo_region_code", companyOrgObj);
                            loanOrderBill.set("org", companyOrgObj);
                            break;
                        }
                    }

                }

            }
        }

    }


    /**
     * 根据统一社会信用代码查询最新一条贷款申请
     *
     * @param ukwoUniSociCreditCode 统一社会信用代码
     * @return
     */
    public DynamicObject queryLatestOneByCreditCode(String ukwoUniSociCreditCode) {
        DynamicObject latest = loanApplyBillDao.queryLatestOneByCreditCode(ukwoUniSociCreditCode);
        return latest;
    }

}
