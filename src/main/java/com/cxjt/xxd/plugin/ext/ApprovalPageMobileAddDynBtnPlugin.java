package com.cxjt.xxd.plugin.ext;

import java.util.*;

import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.dao.XXDOrgMappingDao;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.form.CloseCallBack;
import kd.bos.form.control.Button;
import kd.bos.form.control.events.TabSelectEvent;
import kd.bos.form.events.CustomEventArgs;
import kd.bos.form.events.OnGetControlArgs;
import kd.bos.metadata.form.mcontrol.MBarItemAp;
import kd.bos.metadata.form.mcontrol.MToolbarAp;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.workflow.component.ApprovalRecord;
import kd.bos.workflow.component.approvalrecord.IApprovalRecordGroup;
import kd.bos.workflow.component.approvalrecord.IApprovalRecordItem;
import kd.bos.workflow.engine.WFMultiLangConstants;
import kd.bos.workflow.engine.WfConfigurationUtil;
import kd.bos.workflow.engine.WfUtils;
import kd.bos.workflow.engine.impl.cmd.task.TaskHandleContext;
import kd.bos.workflow.engine.impl.cmd.task.WorkflowTaskCenterTypes;
import kd.bos.workflow.management.plugin.ApprovalPageTpl;
import kd.bos.workflow.taskcenter.plugin.ApprovalPageMobilePluginNew;
import kd.bos.workflow.taskcenter.plugin.util.ApprovalPluginUtil;
import kd.bos.workflow.taskcenter.plugin.validate.AddPersonFiltersCustomEvent;

/**
 * 旧版移动审批界面
 */
public class ApprovalPageMobileAddDynBtnPlugin extends ApprovalPageMobilePluginNew {

    //页面控件
    private static final String MTOOLBARAPBUTTNOS = "mtoolbarapbuttnos";//审批页面下方工具栏
    //放入缓存key
    private static final String BTNKEYS = "btnkeys";
//    上一步
    protected static final String PREVSTEP = "prevstep";
//    下一步
    protected static final String NEXTSTEP = "nextstep";
    //    补录
    protected static final String OPENADDINFO = "openaddinfo";
    //    受理
    protected static final String ACCEPT = "accept";

    @Override
    public void customEvent(CustomEventArgs e) {
        if(null==e){
            return;
        }
        if(!AddPersonFiltersCustomEvent.KEY_ADDPERSONFILTERSCUSTOMEVENT.equals(e.getKey()) || !(e instanceof AddPersonFiltersCustomEvent)){
            return;
        }
        //自定义过滤条件
        AddPersonFiltersCustomEvent bac = (AddPersonFiltersCustomEvent)e;
        List<QFilter> qFilters = bac.getQFilters();
        if(qFilters == null){
            return;
        }
        // TODO: 2023/4/7
//        获取办事处负责人id集合作为过滤条件
        List<Long> orgIds = XXDOrgMappingDao.queryAllOrgIds();
        if(orgIds!=null){
            List<Long> managers = new ArrayList(10);
            for (long orgId:orgIds) {
                List<Long> managersOfOrg = UserServiceHelper.getManagersOfOrg(orgId);
                managers.addAll(managersOfOrg);
            }

            QFilter qFilter = new QFilter("id", QCP.in, managers);
            qFilters.add(qFilter);
        }

    }

    @Override
    public void click(EventObject evt) {
        Button btn = (Button)evt.getSource();
        String keyNumber = btn.getKey();
        String entityNumber = getPageCache().get("entitynumber");
        if(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME.equals(entityNumber)){
            //这里放开所有按钮需要重新自己添加
//        同意-驳回-拒绝   test,groupdiscuss,btncirculation,btnviewflowchart,mbaritem_more,consent,reject,
            if("consent".equals(keyNumber) || "reject".equals(keyNumber) || "stop".equals(keyNumber)){
                super.showApprovalBtn(keyNumber, (TaskHandleContext)null);
            }
//        查看流程图
            else if("btnviewflowchart".equals(keyNumber)){
                //查看流程图
                super.showViewFlowChartFunc();
            }

            //        补录
            else if("openaddinfo".equals(keyNumber)){
                super.showExtenBtns("openaddinfo");

            }
            //        受理
            else if("accept".equals(keyNumber)){
                //受理，缓存0，下一步缓存1.2.3.4
                this.getPageCache().put("flagAdd","0");
                this.getView().setVisible(true,"prevstep","nextstep");
                this.getView().setVisible(false,"openaddinfo","accept","consent","reject","stop","prevstep","notecontent");
                super.showExtenBtns("accept");

            }
            //        上一步
            else if("prevstep".equals(keyNumber)){
                String flagAdd = this.getPageCache().get("flagAdd");
//            第二步
                if(flagAdd.length()==2){
                    this.getView().setVisible(true,"nextstep");
                    this.getView().setVisible(false,"prevstep");
                }else{
                    this.getView().setVisible(true,"prevstep","nextstep");
                }
                this.getPageCache().put("flagAdd",flagAdd.substring(1));
                this.getView().setVisible(false,"consent","save","reject","stop");


                super.showExtenBtns("prevstep");

            }
            //        暂存
            else if("save".equals(keyNumber)){
                super.showExtenBtns("save");

            }
            //        下一步
            else if("nextstep".equals(keyNumber)){
                String flagAdd = this.getPageCache().get("flagAdd");
//            第二步
                if(flagAdd.length()==1){
                    this.getView().setVisible(true,"nextstep","prevstep");
//                最后一步
                }else if(flagAdd.length()==3){
                    this.getView().setVisible(true,"prevstep","save","consent");
                    this.getView().setVisible(false,"nextstep","reject","stop");
                }else{
                    this.getView().setVisible(true,"prevstep","nextstep");
                }
                this.getPageCache().put("flagAdd",flagAdd+"0");
                super.showExtenBtns("nextstep");

            }
            // 备注进度
            else if("notecontent".equals(keyNumber)){
                super.showExtenBtns("notecontent");

            }
            // 转交
            else if("btntransfer".equals(keyNumber)){
                CloseCallBack callBack = new CloseCallBack("kd.bos.workflow.taskcenter.plugin.ApprovalPageMobilePluginNew", "showUserF7PageFromMain");
                super.showUserF7PageByType("auditType_transfer", WfConfigurationUtil.getTransferName(), "showUserF7PageFromMain", (List)null, callBack);
            }

        }else if(FormConstant.LOAN_GUA_CONFIRM_BILL_ENTITY_NAME.equals(entityNumber)){
            //        同意-驳回-拒绝   test,groupdiscuss,btncirculation,btnviewflowchart,mbaritem_more,consent,reject,
            if("consent".equals(keyNumber) || "reject".equals(keyNumber) || "stop".equals(keyNumber)){
                super.showApprovalBtn(keyNumber, (TaskHandleContext)null);
            }
//        查看流程图
            else if("btnviewflowchart".equals(keyNumber)){
                //查看流程图
                super.showViewFlowChartFunc();
            }
        }else{
            super.click(evt);
        }
    }

    @Override
    public void tabSelected(TabSelectEvent evt) {
        String entityNumber = getPageCache().get("entitynumber");
        if(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME.equals(entityNumber)||FormConstant.LOAN_GUA_CONFIRM_BILL_ENTITY_NAME.equals(entityNumber)){
            String key = evt.getTabKey();
            switch (key) {
                case "tab_approvalrecord":
                    this.getView().setVisible(false,MTOOLBARAPBUTTNOS);
                    break;

                case "tab_billinfo":
                    this.getView().setVisible(true,MTOOLBARAPBUTTNOS);
                    break;
            }
        }
    }
//注册新加按钮的监听
    @Override
    public void onGetControl(OnGetControlArgs e) {
            String btnkeys = this.getPageCache().get("btnkeys");
            if (null != btnkeys && btnkeys.contains(e.getKey())) {
                Button btn = new Button();
                btn.setKey(e.getKey());
                btn.addClickListener(this);
                btn.addItemClickListener(this);
                e.setControl(btn);
            }

        super.onGetControl(e);
    }

    /*
     * 页面第一次渲染，调用插件，进行数据的读取
     */
    @Override
    public void afterCreateNewData(EventObject evt) {
        String tasktype = getPageCache().get("tasktype");
        String entityNumber = getPageCache().get("entitynumber");
        String curNode = getPageCache().get("CURNODE");
        if(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME.equals(entityNumber)){
            //这里放开所有按钮需要重新自己添加
            this.addDynamicButtons(entityNumber, tasktype, curNode);
            this.getView().setVisible(false,"prevstep","save","nextstep");
        }else if(FormConstant.LOAN_GUA_CONFIRM_BILL_ENTITY_NAME.equals(entityNumber)){
            //这里放开所有按钮需要重新自己添加
            this.addDynamicButtons(entityNumber, tasktype, curNode);
        }
    }



    /**
     * 动态添加下方的审批按钮
     * @param type
     * @param curNode
     */
    private void addDynamicButtons(String entityNumber,String type, String curNode) {
        //设置显示的背景色、前景色
        String backColor = getPageCache().get(ApprovalPageTpl.BACKCOLORFORDYNBTN);
        String foreColor = getPageCache().get(ApprovalPageTpl.FORECOLORFORDYNBTN);// 之前是#3F3F3F
        String btnKeys = this.getPageCache().get(BTNKEYS);
        //下方工具栏
        MToolbarAp mToolbarAp = new MToolbarAp();
        mToolbarAp.setKey(MTOOLBARAPBUTTNOS);
        //下方工具栏项
        MBarItemAp itemAp = null;
        //存放button的key
        StringBuilder btnkeys = new StringBuilder();
        if(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME.equals(entityNumber)){
            //        待办
            if(WorkflowTaskCenterTypes.TOHANDLE.equals(type)){
//            转派
                if(FormConstant.TASK_TRANSFER.equals(curNode)){
                    //添加按钮
                    itemAp = setMBarItem("转交其他办事处负责人","btntransfer",getPageCache().get(ApprovalPageTpl.BACKCOLORFORAPPROVAL),getPageCache().get(ApprovalPageTpl.FORECOLORFORAPPROVAL));
                    mToolbarAp.getItems().add(itemAp);
                    btnkeys.append("btntransfer").append(',');
                    itemAp = setMBarItem("转派项目经理","consent",getPageCache().get(ApprovalPageTpl.BACKCOLORFORAPPROVAL),getPageCache().get(ApprovalPageTpl.FORECOLORFORAPPROVAL));
                    mToolbarAp.getItems().add(itemAp);
                    btnkeys.append("consent").append(',');
//                录入
                }else if(FormConstant.TASK_ENTER.equals(curNode)){
                    //添加按钮
                    itemAp = setMBarItem("补录担保人信息","openaddinfo",getPageCache().get(ApprovalPageTpl.BACKCOLORFORAPPROVAL),getPageCache().get(ApprovalPageTpl.FORECOLORFORAPPROVAL));
                    mToolbarAp.getItems().add(itemAp);
                    btnkeys.append("openaddinfo").append(',');
                    itemAp = setMBarItem("受理","accept",getPageCache().get(ApprovalPageTpl.BACKCOLORFORAPPROVAL),getPageCache().get(ApprovalPageTpl.FORECOLORFORAPPROVAL));
                    mToolbarAp.getItems().add(itemAp);
                    btnkeys.append("accept").append(',');
                    itemAp = setMBarItem("上一步","prevstep",getPageCache().get(ApprovalPageTpl.BACKCOLORFORAPPROVAL),getPageCache().get(ApprovalPageTpl.FORECOLORFORAPPROVAL));
                    mToolbarAp.getItems().add(itemAp);
                    btnkeys.append("prevstep").append(',');
                    itemAp = setMBarItem("保存","save",getPageCache().get(ApprovalPageTpl.BACKCOLORFORAPPROVAL),getPageCache().get(ApprovalPageTpl.FORECOLORFORAPPROVAL));
                    mToolbarAp.getItems().add(itemAp);
                    btnkeys.append("save").append(',');
                    itemAp = setMBarItem("下一步","nextstep",getPageCache().get(ApprovalPageTpl.BACKCOLORFORAPPROVAL),getPageCache().get(ApprovalPageTpl.FORECOLORFORAPPROVAL));
                    mToolbarAp.getItems().add(itemAp);
                    btnkeys.append("nextstep").append(',');


                    itemAp = setMBarItem("提交","consent",getPageCache().get(ApprovalPageTpl.BACKCOLORFORAPPROVAL),getPageCache().get(ApprovalPageTpl.FORECOLORFORAPPROVAL));
                    mToolbarAp.getItems().add(itemAp);
                    btnkeys.append("consent").append(',');
                    itemAp = setMBarItem("拒绝","stop",getPageCache().get(ApprovalPageTpl.BACKCOLORFORAPPROVAL),getPageCache().get(ApprovalPageTpl.FORECOLORFORAPPROVAL));
                    mToolbarAp.getItems().add(itemAp);
                    btnkeys.append("stop").append(',');
                    itemAp = setMBarItem("退回","reject",getPageCache().get(ApprovalPageTpl.BACKCOLORFORAPPROVAL),getPageCache().get(ApprovalPageTpl.FORECOLORFORAPPROVAL));
                    mToolbarAp.getItems().add(itemAp);
                    btnkeys.append("reject").append(',');
                    itemAp = setMBarItem("备注进度","notecontent",getPageCache().get(ApprovalPageTpl.BACKCOLORFORAPPROVAL),getPageCache().get(ApprovalPageTpl.FORECOLORFORAPPROVAL));
                    mToolbarAp.getItems().add(itemAp);
                    btnkeys.append("notecontent").append(',');
                    this.getView().setVisible(false,"consent");
                }else {
                    //添加按钮
                    itemAp = setMBarItem("同意","consent",getPageCache().get(ApprovalPageTpl.BACKCOLORFORAPPROVAL),getPageCache().get(ApprovalPageTpl.FORECOLORFORAPPROVAL));
                    mToolbarAp.getItems().add(itemAp);
                    btnkeys.append("consent").append(',');
                    itemAp = setMBarItem("拒绝","stop",getPageCache().get(ApprovalPageTpl.BACKCOLORFORAPPROVAL),getPageCache().get(ApprovalPageTpl.FORECOLORFORAPPROVAL));
                    mToolbarAp.getItems().add(itemAp);
                    btnkeys.append("stop").append(',');
                    itemAp = setMBarItem("退回","reject",getPageCache().get(ApprovalPageTpl.BACKCOLORFORAPPROVAL),getPageCache().get(ApprovalPageTpl.FORECOLORFORAPPROVAL));
                    mToolbarAp.getItems().add(itemAp);
                    btnkeys.append("reject").append(',');
                }
            }



        }else if(FormConstant.LOAN_GUA_CONFIRM_BILL_ENTITY_NAME.equals(entityNumber)){
            if(WorkflowTaskCenterTypes.TOHANDLE.equals(type)){
                //这里放开所有按钮需要重新自己添加
                itemAp = setMBarItem("确认","consent",getPageCache().get(ApprovalPageTpl.BACKCOLORFORAPPROVAL),getPageCache().get(ApprovalPageTpl.FORECOLORFORAPPROVAL));
                mToolbarAp.getItems().add(itemAp);
                btnkeys.append("consent").append(',');
                itemAp = setMBarItem("退回","stop",getPageCache().get(ApprovalPageTpl.BACKCOLORFORAPPROVAL),getPageCache().get(ApprovalPageTpl.FORECOLORFORAPPROVAL));
                mToolbarAp.getItems().add(itemAp);
                btnkeys.append("stop").append(',');
            }
        }
//        已办
        if(WorkflowTaskCenterTypes.HANDLED.equals(type)){
            //查看流程图
            itemAp = setMBarItem(WFMultiLangConstants.getFlowChartName(),BTNVIEWFLOWCHART,getPageCache().get(ApprovalPageTpl.BACKCOLORFORAPPROVAL),getPageCache().get(ApprovalPageTpl.FORECOLORFORAPPROVAL));
            mToolbarAp.getItems().add(itemAp);
            btnkeys.append(BTNVIEWFLOWCHART).append(',');
        }
        this.getView().updateControlMetadata(MTOOLBARAPBUTTNOS, mToolbarAp.createControl());
        this.getPageCache().put(BTNKEYS, btnkeys.toString());
    }

    /**
     * 设置工具栏项
     * @param title
     * @param key
     * @param backColor
     * @param foreColor
     * @return
     */
    private MBarItemAp setMBarItem(String title, String key, String backColor, String foreColor) {
        MBarItemAp itemAp = new MBarItemAp();
        itemAp.setName(new LocaleString(title));
        itemAp.setId(key);
        itemAp.setKey(key);
        itemAp.setHeight(new LocaleString("100%"));
        itemAp.setButtonStyle(1);
        itemAp.setRadius("0");
        itemAp.setBackColor(backColor);
        itemAp.setForeColor(foreColor);
        return itemAp;
    }

    /**
     * 格式化审批记录
     * */
    protected void showApprovalRecordNew(Boolean isPCShow, Boolean approvalIsNew, Boolean hideChat, Boolean isNewApprovalRecord) {
        boolean isTaskExist = Boolean.parseBoolean(super.getPageCache().get("isTaskExist"));
        if (isTaskExist) {
            Long processInstanceId = WfUtils.normalizeId(super.getPageCache().get("processInstanceId"));
            String businesskey = super.getPageCache().get("businesskey");
            String curTaskId = this.getPageCache().get("taskid");
            List<IApprovalRecordGroup> approvalRecordItems = ApprovalPluginUtil.getApprovalRecordItems(super.getTaskService(), processInstanceId, businesskey, curTaskId, isNewApprovalRecord);
            //处理审批记录，隐藏节点
            List<IApprovalRecordGroup> newItems = new ArrayList<>();
            for (IApprovalRecordGroup record : approvalRecordItems) {
                List<IApprovalRecordItem> children = record.getChildren();
                if(children!=null && children.size()>0){
                    IApprovalRecordItem iApprovalRecordItem = children.get(0);
                    String activityId = iApprovalRecordItem.getActivityId();
                    if(activityId!=null&&(activityId.endsWith(FormConstant.TASK_SUBMIT)||activityId.endsWith(FormConstant.TASK_TRANSFER))){
                        //删除两个节点
                    }else{
                        newItems.add(record);
                    }
                }
            }
            ApprovalRecord approvalRecord = (ApprovalRecord)this.getControl("approvalrecordap");
            approvalRecord.setYzjParameter(businesskey, String.valueOf(processInstanceId), "wf_taskCenter");
            Map<String, Object> parameters = new HashMap();
            parameters.put("hideMoreChat", isPCShow && !hideChat ? "" : "true");
            parameters.put("hideChat", hideChat ? "true" : "");
            parameters.put("isPC", isPCShow.toString());
            parameters.put("approvalIsNew", approvalIsNew);
            String billPageId = this.getPageCache().get("billPageId");
            parameters.put("pageId", billPageId);
            boolean isDDOrQYWX = this.isNotFromYZJ();
            parameters.put("isDDOrQYWX", isDDOrQYWX);
            parameters.put("procInstId", processInstanceId);
            approvalRecord.setParameters(parameters);
            approvalRecord.setArData(newItems);
        }
    }


}

