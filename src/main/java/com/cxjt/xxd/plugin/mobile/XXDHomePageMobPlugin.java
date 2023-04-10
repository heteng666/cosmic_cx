package com.cxjt.xxd.plugin.mobile;

import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.util.XXDUtils;
import kd.bos.bill.MobileBillShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.CloseCallBack;
import kd.bos.form.MobileFormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.cardentry.CardEntry;
import kd.bos.form.container.Tab;
import kd.bos.form.control.Control;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.*;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.HyperLinkClickListener;
import kd.bos.form.plugin.AbstractMobFormPlugin;
import kd.bos.list.MobileListShowParameter;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.workflow.WorkflowServiceHelper;
import kd.bos.util.StringUtils;

import java.math.BigDecimal;
import java.util.EventObject;
import java.util.HashMap;

/**
 * APP首页
 * */
public class XXDHomePageMobPlugin extends AbstractMobFormPlugin implements CellClickListener, TabSelectListener,PullRefreshListener, HyperLinkClickListener {
    private final static Log logger = LogFactory.getLog(XXDHomePageMobPlugin.class);
    private int pagecount = 10;

//    初始化加载
    @Override
    public void beforeBindData(EventObject e) {
        long currUserId = RequestContext.get().getCurrUserId();
        //待办任务	查询待办任务(toHandle)、已办任务(handled)，在办申请(toApply)，已办申请(applyed)
        Long toHandle = WorkflowServiceHelper.getTaskCountByType(String.valueOf(currUserId), "toHandle");
        pagecount = toHandle.intValue();
        HashMap<String, Object> todoFilters = new HashMap<>();
//        获取当前登录用户待办任务
        setTodo(this.getModel(),0, pagecount, currUserId, todoFilters);
        //已办任务初始化不加载，切换页签加载
//        Long handled = WorkflowServiceHelper.getTaskCountByType(String.valueOf(currUserId), "handled");
//        pagecount = handled.intValue();
//        HashMap<String, Object> doneFilters = new HashMap<>();
//        setDone(this.getModel(),0, pagecount, currUserId, doneFilters);
    }
//查询过滤
    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        String operateKey = args.getOperateKey();
        if("searchtodo".equals(operateKey)){
            String ukwoSearchkey = (String) this.getModel().getValue("ukwo_searchkeytodo");
            if(StringUtils.isNotEmpty(ukwoSearchkey)){
                this.getModel().deleteEntryData("ukwo_todoentrys");
                long currUserId = RequestContext.get().getCurrUserId();
                //待办任务	查询待办任务(toHandle)、已办任务(handled)，在办申请(toApply)，已办申请(applyed)
                Long toHandle = WorkflowServiceHelper.getTaskCountByType(String.valueOf(currUserId), "toHandle");
                pagecount = toHandle.intValue();
                HashMap<String, Object> todoFilters = new HashMap<>();
                todoFilters.put("companyName",ukwoSearchkey);
//        获取当前登录用户待办任务
                setTodo(this.getModel(),0, pagecount, currUserId, todoFilters);
            }else{
                this.getModel().deleteEntryData("ukwo_todoentrys");
                long currUserId = RequestContext.get().getCurrUserId();
                //待办任务	查询待办任务(toHandle)、已办任务(handled)，在办申请(toApply)，已办申请(applyed)
                Long toHandle = WorkflowServiceHelper.getTaskCountByType(String.valueOf(currUserId), "toHandle");
                pagecount = toHandle.intValue();
                setTodo(this.getModel(),0, pagecount, currUserId, null);
            }
            this.getView().updateView("ukwo_todoentrys");
        }else if("searchdone".equals(operateKey)){
            String ukwoSearchkey = (String) this.getModel().getValue("ukwo_searchkeydone");
            if(StringUtils.isNotEmpty(ukwoSearchkey)){
                this.getModel().deleteEntryData("ukwo_doneentrys");
                long currUserId = RequestContext.get().getCurrUserId();
                //待办任务	查询待办任务(toHandle)、已办任务(handled)，在办申请(toApply)，已办申请(applyed)
                Long handled = WorkflowServiceHelper.getTaskCountByType(String.valueOf(currUserId), "handled");
                //已办任务
                pagecount = handled.intValue();
                HashMap<String, Object> doneFilters = new HashMap<>();
                doneFilters.put("companyName",ukwoSearchkey);
                setDone(this.getModel(),0, pagecount, currUserId, doneFilters);
            }else{
                this.getModel().deleteEntryData("ukwo_doneentrys");
                long currUserId = RequestContext.get().getCurrUserId();
                //待办任务	查询待办任务(toHandle)、已办任务(handled)，在办申请(toApply)，已办申请(applyed)
                Long handled = WorkflowServiceHelper.getTaskCountByType(String.valueOf(currUserId), "handled");
                //已办任务
                pagecount = handled.intValue();
                setDone(this.getModel(),0, pagecount, currUserId, null);
            }
            this.getView().updateView("ukwo_doneentrys");
        }
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        EntryGrid gridtodo = this.getView().getControl("ukwo_todoentrys");
        EntryGrid griddone = this.getView().getControl("ukwo_doneentrys");

        gridtodo.addCellClickListener(this);
        griddone.addCellClickListener(this);

        // 页签添加监听事件
        Tab tab = this.getView().getControl("ukwo_tabap");
        tab.addTabSelectListener(this);
        CardEntry todocardEntry = (CardEntry)this.getView().getControl("ukwo_todoentrys");
        todocardEntry.addPullRefreshlisteners(this);
        todocardEntry.addHyperClickListener(this);
        CardEntry donecardEntry = (CardEntry)this.getView().getControl("ukwo_doneentrys");
        donecardEntry.addPullRefreshlisteners(this);

    }
//打开详情
    public void cellClick(CellClickEvent evt) {
        Control source = (Control) evt.getSource();
        String key = source.getKey();
        String clickValue = "ukwo_urldone";
        if("ukwo_todoentrys".equals(key)){
            clickValue="ukwo_urltodo";
        }
        int row = evt.getRow();
        String ukwoUrltodo = (String) this.getModel().getValue(clickValue, row);
        ukwoUrltodo = ukwoUrltodo + "&customUrl=true&device=mob";
        this.getView().openUrl(ukwoUrltodo);
//        HashMap<String, Object> paramMap = new HashMap<>();
//        paramMap.put("auditUrl",ukwoUrltodo);
//        MobileFormShowParameter showParameter = new MobileFormShowParameter();
//        showParameter.setCustomParam("params",paramMap);
//        showParameter.setFormId("ukwo_opentaskpage");
//        //showParameter.setCaption("申请单");
//        showParameter.setCloseCallBack(new CloseCallBack(this, "taskshow"));
//        showParameter.getOpenStyle().setShowType(ShowType.Floating);
//        this.getView().showForm(showParameter);
//        ukwoUrltodo="http://www.baidu.com";
    }

    @Override
    public void cellDoubleClick(CellClickEvent cellClickEvent) {
        Object source = cellClickEvent.getSource();
    }

    /**
     * 页签切换，打开列表
     * */
    @Override
    public void tabSelected(TabSelectEvent event) {
        // 点击页签的key
        String subTabKey = event.getTabKey();
        //待办
        if("ukwo_tab_to_do_list".equals(subTabKey)){
            String ukwoSearchkey = (String) this.getModel().getValue("ukwo_searchkeytodo");
            this.getModel().deleteEntryData("ukwo_todoentrys");
            long currUserId = RequestContext.get().getCurrUserId();
            //待办任务	查询待办任务(toHandle)、已办任务(handled)，在办申请(toApply)，已办申请(applyed)
            Long toHandle = WorkflowServiceHelper.getTaskCountByType(String.valueOf(currUserId), "toHandle");
            pagecount = toHandle.intValue();
            HashMap<String, Object> todoFilters = new HashMap<>();
            todoFilters.put("companyName",ukwoSearchkey);
//        获取当前登录用户待办任务
            setTodo(this.getModel(),0, pagecount, currUserId, todoFilters);
            this.getView().updateView("ukwo_todoentrys");
        //已办
        }else if("ukwo_tab_done_list".equals(subTabKey)){
            String ukwoSearchkey = (String) this.getModel().getValue("ukwo_searchkeydone");
            this.getModel().deleteEntryData("ukwo_doneentrys");
            long currUserId = RequestContext.get().getCurrUserId();
            //待办任务	查询待办任务(toHandle)、已办任务(handled)，在办申请(toApply)，已办申请(applyed)
            Long handled = WorkflowServiceHelper.getTaskCountByType(String.valueOf(currUserId), "handled");
            //已办任务
            pagecount = handled.intValue();
            HashMap<String, Object> doneFilters = new HashMap<>();
            doneFilters.put("companyName",ukwoSearchkey);
            setDone(this.getModel(),0, pagecount, currUserId, doneFilters);
            this.getView().updateView("ukwo_doneentrys");
        }else if("ukwo_tabpageap6".equals(subTabKey)){
            //打开贷款申请单列表页面
            MobileListShowParameter listShowParameter = new MobileListShowParameter();
            listShowParameter.setFormId("bos_moblist");
            listShowParameter.setBillFormId("ukwo_xxd_loan_apply_bill");
            listShowParameter.getOpenStyle().setShowType(ShowType.InContainer);
            listShowParameter.setHasRight(true);
            //打开的应用appId
            listShowParameter.setAppId("ukwo_xxd");
            //设置页签容器的key，这里为应用中的页签容器的标识
            listShowParameter.getOpenStyle().setTargetKey("ukwo_tabpageap");
            this.getView().showForm(listShowParameter);
        }
    }
    /**
     * 待办列表赋值
     * @param model 单据模型
     * @param start 查询起点
     * @param pagecount 查询数量
     * @param userid 用户id
     * @param filters 过滤条件
     * */
    public static void setTodo(IDataModel model,int start,int pagecount,Object userid,HashMap filters){
        String entityName = XXDUtils.getEntityNameKey(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME);
//        担保费
        String entityNameDB = XXDUtils.getEntityNameKey(FormConstant.LOAN_GUA_CONFIRM_BILL_ENTITY_NAME);
//        获取当前登录用户待办任务
        DynamicObjectCollection todocoll = WorkflowServiceHelper.getToHandleTasksByUserId(start, pagecount, String.valueOf(userid));
        if(todocoll!=null&&todocoll.size()>0){
            for (int i=0;i<todocoll.size();i++){
                DynamicObject todoTemp = todocoll.get(i);
                String entityname = (String) todoTemp.get("ENTITYNAME");
                //贷款申请单待办
                if(entityName.equals(entityname)){
                    boolean isExist = QueryServiceHelper.exists(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME, todoTemp.get("BUSINESSKEY"));
                    if(!isExist){
                        continue;
                    }
                    //ukwo_company_name 企业名称；ukwo_application_amount 申请金额；ukwo_application_period 申请期限；ukwo_application_date 申请时间
                    String selectFields = "billno,ukwo_company_name,ukwo_application_amount,ukwo_application_period,ukwo_application_date,ukwo_approve_amount,ukwo_approve_term";
                    DynamicObject bill = BusinessDataServiceHelper.loadSingle(todoTemp.get("BUSINESSKEY"), FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME, selectFields);
//                    过滤
                    if(filters!=null){
                        String companyName = (String) filters.get("companyName");
                        if(companyName!=null&&!"".equals(companyName)){
                            String ukwoCompanyName = bill.getString("ukwo_company_name");
                            if(ukwoCompanyName==null || !ukwoCompanyName.contains(companyName)){
                                continue;
                            }
                        }
                    }
                    int[] ints = model.batchCreateNewEntryRow("ukwo_todoentrys", 1);
                    StringBuffer contentStr = new StringBuffer();
//                    contentStr.append("单据类型：");
//                    contentStr.append(filterName);
//                    contentStr.append("\n");
//                    contentStr.append("申请单编号：");
//                    contentStr.append(bill.getString("billno"));
//                    contentStr.append("\n");
                    contentStr.append("企业名称：");
                    contentStr.append(bill.getString("ukwo_company_name"));
                    contentStr.append("\n");
                    contentStr.append("申请金额：");
                    contentStr.append(XXDUtils.getFormatString(bill.getBigDecimal("ukwo_application_amount").toString(),2) + "万元");
                    contentStr.append("\n");
                    contentStr.append("申请期限：");
                    contentStr.append(XXDUtils.getFormatString(bill.getBigDecimal("ukwo_application_period").toString(),-1) + "个月");
                    contentStr.append("\n");
                    contentStr.append("申请时间：");
                    contentStr.append(XXDUtils.getFormatDate(bill.getDate("ukwo_application_date")));
                    contentStr.append("\n");
                    //added by liuwei 2023-03-09
                    contentStr.append("审批金额：");
                    String ukwoApproveAmount = bill.getBigDecimal("ukwo_approve_amount").compareTo(new BigDecimal(0)) == 0
                            ?"-":XXDUtils.getFormatString(bill.getBigDecimal("ukwo_approve_amount").toString(),2) + "万元";
                    contentStr.append(ukwoApproveAmount);
                    contentStr.append("\n");

                    contentStr.append("审批期限：");
                    String ukwoApproveTerm = bill.getBigDecimal("ukwo_approve_term").compareTo(new BigDecimal(0)) == 0
                            ?"-":XXDUtils.getFormatString(bill.getBigDecimal("ukwo_approve_term").toString(),-1) + "个月";
                    contentStr.append(ukwoApproveTerm);
                    //added by liuwei 2023-03-09
                    String nodeName = todoTemp.getString("NAME");
                    model.setValue("ukwo_titletodo","鑫湘e贷-"+nodeName,ints[0]);
                    model.setValue("ukwo_contenttodo",contentStr.toString(),ints[0]);
                    model.setValue("ukwo_buttontodo",nodeName,ints[0]);
                    model.setValue("ukwo_urltodo",todoTemp.getString("URL"),ints[0]);
                    model.setValue("ukwo_billkeytodo",todoTemp.getString("BUSINESSKEY"),ints[0]);
                    //担保费确认单待办
                }else if(entityNameDB.equals(entityname)){
                    boolean isExist = QueryServiceHelper.exists(FormConstant.LOAN_GUA_CONFIRM_BILL_ENTITY_NAME, todoTemp.get("BUSINESSKEY"));
                    if(!isExist){
                        continue;
                    }
                    //ukwo_company_name 企业名称；ukwo_guarant_amount 担保金额(万元)；ukwo_guarantee_fee_amount 担保费(万元)；ukwo_payment_method 缴纳方式；ukwo_transfer_date 汇款时间
                    String selectFields = "billno,ukwo_company_name,ukwo_guarant_amount,ukwo_guarantee_fee_amount,ukwo_payment_method,ukwo_transfer_date";
                    DynamicObject bill = BusinessDataServiceHelper.loadSingle(todoTemp.get("BUSINESSKEY"), FormConstant.LOAN_GUA_CONFIRM_BILL_ENTITY_NAME, selectFields);
                    //                    过滤
                    if(filters!=null){
                        String companyName = (String) filters.get("companyName");
                        if(companyName!=null&&!"".equals(companyName)){
                            String ukwoCompanyName = bill.getString("ukwo_company_name");
                            if(ukwoCompanyName==null || !ukwoCompanyName.contains(companyName)){
                                continue;
                            }
                        }
                    }

                    int[] ints = model.batchCreateNewEntryRow("ukwo_todoentrys", 1);
                    StringBuffer contentStr = new StringBuffer();
//                    contentStr.append("单据类型：");
//                    contentStr.append(filterNameDB);
//                    contentStr.append("\n");
//                    contentStr.append("单据编号：");
//                    contentStr.append(bill.getString("billno"));
//                    contentStr.append("\n");
                    contentStr.append("企业名称：");
                    contentStr.append(bill.getString("ukwo_company_name"));
                    contentStr.append("\n");
                    contentStr.append("担保金额：");
                    contentStr.append(XXDUtils.getFormatString(bill.getBigDecimal("ukwo_guarant_amount").toString(),2) + "万元");
                    contentStr.append("\n");
                    contentStr.append("担保费：");
                    contentStr.append(XXDUtils.getFormatString(bill.getBigDecimal("ukwo_guarantee_fee_amount").toString(),2) + "万元");
                    contentStr.append("\n");
                    contentStr.append("缴纳方式：");
                    contentStr.append(bill.getString("ukwo_payment_method"));
                    contentStr.append("\n");
                    contentStr.append("缴纳时间：");
                    contentStr.append(XXDUtils.getFormatDate(bill.getDate("ukwo_transfer_date")));
                    String nodeName = todoTemp.getString("NAME");
                    model.setValue("ukwo_titletodo","鑫湘e贷-"+nodeName,ints[0]);
                    model.setValue("ukwo_contenttodo",contentStr.toString(),ints[0]);
                    model.setValue("ukwo_buttontodo",nodeName,ints[0]);
                    model.setValue("ukwo_urltodo",todoTemp.getString("URL"),ints[0]);
                    model.setValue("ukwo_billkeytodo",todoTemp.getString("BUSINESSKEY"),ints[0]);
                }
            }
        }
    }

    /**
     * 已办列表赋值
     * @param model 单据模型
     * @param start 查询起点
     * @param pagecount 查询数量
     * @param userid 用户id
     * @param filters 过滤条件
     * */
    public static void setDone(IDataModel model,int start,int pagecount,Object userid,HashMap filters){
        String entityName = XXDUtils.getEntityNameKey(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME);
//        担保费
        String entityNameDB = XXDUtils.getEntityNameKey(FormConstant.LOAN_GUA_CONFIRM_BILL_ENTITY_NAME);
        //已办任务
        DynamicObjectCollection donecoll = WorkflowServiceHelper.getHandledTasksByUserId(start, pagecount, String.valueOf(userid),new HashMap<>());
        if(donecoll!=null&&donecoll.size()>0){
            for (int i=0;i<donecoll.size();i++) {
                DynamicObject doneTemp = donecoll.get(i);
                String entityname = (String) doneTemp.get("ENTITYNAME");
                if (entityName.equals(entityname)) {
                    boolean isExist = QueryServiceHelper.exists(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME, doneTemp.get("BUSINESSKEY"));
                    if (!isExist) {
                        continue;
                    }
                    //ukwo_company_name 企业名称；ukwo_application_amount 申请金额；ukwo_application_period 申请期限；ukwo_application_date 申请时间
                    String selectFields = "billno,ukwo_company_name,ukwo_application_amount,ukwo_application_period,ukwo_application_date,ukwo_approve_amount,ukwo_approve_term";
                    DynamicObject bill = BusinessDataServiceHelper.loadSingle(doneTemp.get("BUSINESSKEY"), FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME, selectFields);
                    //                    过滤
                    if(filters!=null){
                        String companyName = (String) filters.get("companyName");
                        if(companyName!=null&&!"".equals(companyName)){
                            String ukwoCompanyName = bill.getString("ukwo_company_name");
                            if(ukwoCompanyName==null || !ukwoCompanyName.contains(companyName)){
                                continue;
                            }
                        }
                    }
                    int[] ints = model.batchCreateNewEntryRow("ukwo_doneentrys", 1);
                    StringBuffer contentStr = new StringBuffer();
//                    contentStr.append("单据类型：");
//                    contentStr.append(filterName);
//                    contentStr.append("\n");
//                    contentStr.append("申请单编号：");
//                    contentStr.append(bill.getString("billno"));
//                    contentStr.append("\n");
                    contentStr.append("企业名称：");
                    contentStr.append(bill.getString("ukwo_company_name"));
                    contentStr.append("\n");
                    contentStr.append("申请金额：");
                    contentStr.append(XXDUtils.getFormatString(bill.getBigDecimal("ukwo_application_amount").toString(), 2) + "万元");
                    contentStr.append("\n");
                    contentStr.append("申请期限：");
                    contentStr.append(XXDUtils.getFormatString(bill.getBigDecimal("ukwo_application_period").toString(), -1) + "个月");
                    contentStr.append("\n");
                    contentStr.append("申请时间：");
                    contentStr.append(XXDUtils.getFormatDate(bill.getDate("ukwo_application_date")));

                    //added by liuwei 2023-03-09
                    contentStr.append("\n");
                    contentStr.append("审批金额：");
                    String ukwoApproveAmount = bill.getBigDecimal("ukwo_approve_amount").compareTo(new BigDecimal(0)) == 0
                            ?"-":XXDUtils.getFormatString(bill.getBigDecimal("ukwo_approve_amount").toString(),2) + "万元";
                    contentStr.append(ukwoApproveAmount);
                    contentStr.append("\n");

                    contentStr.append("审批期限：");
                    String ukwoApproveTerm = bill.getBigDecimal("ukwo_approve_term").compareTo(new BigDecimal(0)) == 0
                            ?"-":XXDUtils.getFormatString(bill.getBigDecimal("ukwo_approve_term").toString(),-1) + "个月";
                    contentStr.append(ukwoApproveTerm);
                    //added by liuwei 2023-03-09

                    String nodeName = doneTemp.getString("NAME");
                    model.setValue("ukwo_titledone", "鑫湘e贷-" + nodeName, ints[0]);
                    model.setValue("ukwo_contentdone", contentStr.toString(), ints[0]);
                    model.setValue("ukwo_buttondone", nodeName, ints[0]);
                    model.setValue("ukwo_urldone", doneTemp.getString("URL"), ints[0]);
                    model.setValue("ukwo_billkeydone", doneTemp.getString("BUSINESSKEY"), ints[0]);
                } else if (entityNameDB.equals(entityname)) {
                    boolean isExist = QueryServiceHelper.exists(FormConstant.LOAN_GUA_CONFIRM_BILL_ENTITY_NAME, doneTemp.get("BUSINESSKEY"));
                    if (!isExist) {
                        continue;
                    }
                    //ukwo_company_name 企业名称；ukwo_guarant_amount 担保金额(万元)；ukwo_guarantee_fee_amount 担保费(万元)；ukwo_payment_method 缴纳方式；ukwo_transfer_date 汇款时间
                    String selectFields = "billno,ukwo_company_name,ukwo_guarant_amount,ukwo_guarantee_fee_amount,ukwo_payment_method,ukwo_transfer_date";
                    DynamicObject bill = BusinessDataServiceHelper.loadSingle(doneTemp.get("BUSINESSKEY"), FormConstant.LOAN_GUA_CONFIRM_BILL_ENTITY_NAME, selectFields);
                    //                    过滤
                    if(filters!=null){
                        String companyName = (String) filters.get("companyName");
                        if(companyName!=null&&!"".equals(companyName)){
                            String ukwoCompanyName = bill.getString("ukwo_company_name");
                            if(ukwoCompanyName==null || !ukwoCompanyName.contains(companyName)){
                                continue;
                            }
                        }
                    }
                    int[] ints = model.batchCreateNewEntryRow("ukwo_doneentrys", 1);
                    StringBuffer contentStr = new StringBuffer();
//                    contentStr.append("单据类型：");
//                    contentStr.append(filterNameDB);
//                    contentStr.append("\n");
//                    contentStr.append("单据编号：");
//                    contentStr.append(bill.getString("billno"));
//                    contentStr.append("\n");
                    contentStr.append("企业名称：");
                    contentStr.append(bill.getString("ukwo_company_name"));
                    contentStr.append("\n");
                    contentStr.append("担保金额：");
                    contentStr.append(XXDUtils.getFormatString(bill.getBigDecimal("ukwo_guarant_amount").toString(), 2) + "万元");
                    contentStr.append("\n");
                    contentStr.append("担保费：");
                    contentStr.append(XXDUtils.getFormatString(bill.getBigDecimal("ukwo_guarantee_fee_amount").toString(), 2) + "万元");
                    contentStr.append("\n");
                    contentStr.append("缴纳方式：");
                    contentStr.append(bill.getString("ukwo_payment_method"));
                    contentStr.append("\n");
                    contentStr.append("缴纳时间：");
                    contentStr.append(XXDUtils.getFormatDate(bill.getDate("ukwo_transfer_date")));
                    String nodeName = doneTemp.getString("NAME");
                    model.setValue("ukwo_titledone", "鑫湘e贷-" + nodeName, ints[0]);
                    model.setValue("ukwo_contentdone", contentStr.toString(), ints[0]);
                    model.setValue("ukwo_buttondone", nodeName, ints[0]);
                    model.setValue("ukwo_urldone", doneTemp.getString("URL"), ints[0]);
                    model.setValue("ukwo_billkeydone", doneTemp.getString("BUSINESSKEY"), ints[0]);
                }
            }
        }
    }

//    下拉刷新，重新加载
    @Override
    public void pullRefesh(PullRefreshEvent e) {
        Tab tab = (Tab)this.getView().getControl("ukwo_tabap");
        String tabId = tab.getCurrentTab();
        if ("ukwo_tab_to_do_list".equals(tabId)) {
            String ukwoSearchkey = (String) this.getModel().getValue("ukwo_searchkeytodo");
            this.getModel().deleteEntryData("ukwo_todoentrys");
            long currUserId = RequestContext.get().getCurrUserId();
            //待办任务	查询待办任务(toHandle)、已办任务(handled)，在办申请(toApply)，已办申请(applyed)
            Long toHandle = WorkflowServiceHelper.getTaskCountByType(String.valueOf(currUserId), "toHandle");
            pagecount = toHandle.intValue();
            HashMap<String, Object> todoFilters = new HashMap<>();
            todoFilters.put("companyName",ukwoSearchkey);
//        获取当前登录用户待办任务
            setTodo(this.getModel(),0, pagecount, currUserId, todoFilters);
            this.getView().updateView("ukwo_todoentrys");
        } else if ("ukwo_tab_done_list".equals(tabId)) {
            String ukwoSearchkey = (String) this.getModel().getValue("ukwo_searchkeydone");
            this.getModel().deleteEntryData("ukwo_doneentrys");
            long currUserId = RequestContext.get().getCurrUserId();
            //待办任务	查询待办任务(toHandle)、已办任务(handled)，在办申请(toApply)，已办申请(applyed)
            Long handled = WorkflowServiceHelper.getTaskCountByType(String.valueOf(currUserId), "handled");
            //已办任务
            pagecount = handled.intValue();
            HashMap<String, Object> doneFilters = new HashMap<>();
            doneFilters.put("companyName",ukwoSearchkey);
            setDone(this.getModel(),0, pagecount, currUserId, doneFilters);
            this.getView().updateView("ukwo_doneentrys");
        }
    }

    @Override
    public void hyperLinkClick(HyperLinkClickEvent e) {
        Object source = e.getSource();
        Object ukwoBillkeydone = this.getModel().getValue("ukwo_billkeytodo", e.getRowIndex());
        MobileBillShowParameter showParameter = new MobileBillShowParameter();

        showParameter.setFormId("ukwo_xxd_loan_apply_bill_mob");

        showParameter.setCaption("新增");

        showParameter.setStatus(OperationStatus.EDIT);
        showParameter.setPkId(ukwoBillkeydone);


        showParameter.getOpenStyle().setShowType(ShowType.Floating);

        this.getView().showForm(showParameter);
    }
}
