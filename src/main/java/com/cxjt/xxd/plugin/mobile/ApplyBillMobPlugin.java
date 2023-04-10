package com.cxjt.xxd.plugin.mobile;

import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.service.XXDLoanApplicationService;
import com.cxjt.xxd.util.XXDUtils;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.events.ChangeData;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.*;
import kd.bos.form.cardentry.CardEntry;
import kd.bos.form.container.Tab;
import kd.bos.form.control.AttachmentPanel;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.TabSelectEvent;
import kd.bos.form.control.events.TabSelectListener;
import kd.bos.form.control.events.UploadEvent;
import kd.bos.form.control.events.UploadListener;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.ClientCallBackEvent;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.field.AttachmentEdit;
import kd.bos.form.plugin.AbstractMobFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.AttachmentServiceHelper;
import kd.bos.servicehelper.attachment.AttachmentFieldServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.util.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import java.math.BigDecimal;
import java.util.*;
/**
 * 贷款申请订单移动表单插件
 * */
public class ApplyBillMobPlugin extends AbstractMobFormPlugin implements TabSelectListener,UploadListener {
    private final static Log logger = LogFactory.getLog(ApplyBillMobPlugin.class);
    @Override
    public void afterDoOperation(AfterDoOperationEventArgs e) {
        String operateKey = e.getOperateKey();
        //反担保信息新增
        if ("openaddpage".equals(operateKey)) {
            //新增时移除缓存的分录行数据
            this.getPageCache().remove("row");
            MobileFormShowParameter showParameter = new MobileFormShowParameter();
            showParameter.setFormId("ukwo_guarantoradd");
            showParameter.setCaption("新增");
            showParameter.setStatus(OperationStatus.EDIT);
            showParameter.setCloseCallBack(new CloseCallBack(this, "addPage"));
            showParameter.getOpenStyle().setShowType(ShowType.Floating);
            this.getView().showForm(showParameter);
        //补录信息
        }else if ("openaddinfo".equals(operateKey)) {
            this.getView().setEnable(false,"ukwo_ap_applicant","ukwo_ap_guarantee");
            this.getView().setVisible(true,"ukwo_cardentryfixrowap1");
            setEntryVisible(true);

            Tab tab = this.getView().getControl("ukwo_tabap");
            tab.activeTab("ukwo_tabpageap2");
            this.getPageCache().put("openaddinfo","");
        //上一步
        }else if ("prevstep".equals(operateKey)) {
            Tab tab = this.getView().getControl("ukwo_tabap");
            String currentTab = tab.getCurrentTab();
            if("ukwo_tabpageap0".equals(currentTab)){
                this.getView().setVisible(true,"ukwo_bar_accept");
            }
            else if("ukwo_tabpageap1".equals(currentTab)){
                tab.activeTab("ukwo_tabpageap0");
            }else if("ukwo_tabpageap2".equals(currentTab)){
                tab.activeTab("ukwo_tabpageap0");
            }else if("ukwo_tabpageap3".equals(currentTab)){
                tab.activeTab("ukwo_tabpageap2");
            }else if("ukwo_tabpageap4".equals(currentTab)){
                tab.activeTab("ukwo_tabpageap3");
            }

        //下一步
        }else if ("nextstep".equals(operateKey)) {
            Tab tab = this.getView().getControl("ukwo_tabap");
            String currentTab = tab.getCurrentTab();
            if("ukwo_tabpageap0".equals(currentTab)){
                tab.activeTab("ukwo_tabpageap2");
            }else if("ukwo_tabpageap1".equals(currentTab)){
                tab.activeTab("ukwo_tabpageap2");
            }else if("ukwo_tabpageap2".equals(currentTab)){
                tab.activeTab("ukwo_tabpageap3");
            }else if("ukwo_tabpageap3".equals(currentTab)){
                tab.activeTab("ukwo_tabpageap4");
            }else if("ukwo_tabpageap4".equals(currentTab)){

            }

            //修改担保人信息
        }else if ("modifyentry".equals(operateKey)) {
            EntryGrid grid = this.getView().getControl("entryentity");
            int[] selectRows = grid.getSelectRows();
            if(selectRows.length>0){
                //修改行行号，回调时写入被修改行
                this.getView().getPageCache().put("row",""+selectRows[0]);
                HashMap<String, Object> paramMap = new HashMap<>();
                paramMap.put("type",this.getModel().getValue("ukwo_gua_signer_type", selectRows[0]));
                paramMap.put("rownum",""+selectRows[0]);
                //行号，控制本人和担保人逻辑
                paramMap.put("ukwo_row_num",this.getModel().getValue("ukwo_row_num", selectRows[0]));
                paramMap.put("ukwo_gua_attachment",this.getModel().getValue("ukwo_gua_attachment", selectRows[0]));
                paramMap.put("ukwo_relation_type",this.getModel().getValue("ukwo_relation_type", selectRows[0]));
                paramMap.put("ukwo_company_name_ent",this.getModel().getValue("ukwo_company_name_ent", selectRows[0]));
                paramMap.put("ukwo_u_soc_cre_code_ent",this.getModel().getValue("ukwo_u_soc_cre_code_ent", selectRows[0]));
                paramMap.put("ukwo_busi_lic_address",this.getModel().getValue("ukwo_busi_lic_address", selectRows[0]));
                paramMap.put("ukwo_signer_name",this.getModel().getValue("ukwo_signer_name", selectRows[0]));
                paramMap.put("ukwo_signer_idcard",this.getModel().getValue("ukwo_signer_idcard", selectRows[0]));
                paramMap.put("ukwo_signer_phone",this.getModel().getValue("ukwo_signer_phone", selectRows[0]));
                paramMap.put("ukwo_signer_address",this.getModel().getValue("ukwo_signer_address", selectRows[0]));

                MobileFormShowParameter showParameter = new MobileFormShowParameter();

                showParameter.setFormId("ukwo_guarantoradd");

                showParameter.setCaption("新增");

                showParameter.setStatus(OperationStatus.EDIT);

                showParameter.setCustomParam("params",paramMap);

                showParameter.setCloseCallBack(new CloseCallBack(this, "addPage"));

                showParameter.getOpenStyle().setShowType(ShowType.Floating);

                this.getView().showForm(showParameter);
            }else{
                this.getView().showTipNotification("请选择一行数据再操作！");
            }
//受理
        }else if("accept".equals(operateKey)){
            Integer ukwoApproveTerm = (Integer) this.getModel().getValue("ukwo_approve_term");
            BigDecimal ukwoApproveAmount = (BigDecimal) this.getModel().getValue("ukwo_approve_amount");
            if(ukwoApproveTerm==null||ukwoApproveTerm==0){
                //申请期限赋值
                this.getModel().setValue("ukwo_approve_term",this.getModel().getValue("ukwo_application_period"));
            }
            if(ukwoApproveAmount==null||BigDecimal.ZERO.compareTo(ukwoApproveAmount)==0){
                //审批金额赋初始值
                this.getModel().setValue("ukwo_approve_amount",this.getModel().getValue("ukwo_application_amount"));
            }

            this.getView().setEnable(true,"ukwo_ap_applicant","ukwo_ap_guarantee");
            this.getView().setVisible(true,"ukwo_cardentryfixrowap1");
            setEntryVisible(true);

            Tab tab = this.getView().getControl("ukwo_tabap");
            tab.activeTab("ukwo_tabpageap0");
            this.getView().setVisible(false,"ukwo_bar_accept","ukwo_bar_addinfo","ukwo_tabpageap1");
            this.getView().setVisible(true,"ukwo_bar_nextstep","ukwo_tabpageap4","bar_save","ukwo_flexpanelap11");
            //进入受理状态
            this.getView().getPageCache().put("tabs","");
            // 设置tab属性-外边距
            Map<String, Object> map = new HashMap<>();
            Map<String, Object> s = new HashMap<>();
            Map<String, Object> m = new HashMap<>();
            m.put("t", "0px");
            s.put("m", m);
            map.put("s", s);
            this.getView().updateControlMetadata("ukwo_flexpanelap11",map);
            // 设置tab属性-外边距
            map = new HashMap<>();
            s = new HashMap<>();
            m = new HashMap<>();
            m.put("t", "-42px");
            s.put("m", m);
            map.put("s", s);
            this.getView().updateControlMetadata("ukwo_tabap",map);
            // 设置覆盖标签颜色
            setColor(1);
        }else if("deleteentry".equals(operateKey)){
            setEntryTabs();
            SaveServiceHelper.saveOperate(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME,new DynamicObject[]{this.getModel().getDataEntity(true)});
//        获取360
        }else if("report360".equals(operateKey)){
            List<Map<String, Object>> atts = AttachmentServiceHelper.
                    getAttachments(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME, this.getModel().getDataEntity().getPkValue(), "attachmentpanel");
            if(atts.size()<=0){
                this.getView().showTipNotification("没有找到关联的360报告");
                return;
            }

            //在360报告页签打开页面，页面上发送预览附件前端请求
            MobileFormShowParameter formShowParameter = new MobileFormShowParameter();
            formShowParameter.setFormId("ukwo_opentaskpage");
            formShowParameter.getOpenStyle().setShowType(ShowType.InContainer);
//            formShowParameter.getOpenStyle().setShowType(ShowType.Floating);

            //formShowParameter.setHasRight(true);
            HashMap<String, Object> paramMap = new HashMap<>();
            paramMap.put("att",atts.get(0));
            formShowParameter.setCustomParam("params",paramMap);
            //打开的应用appId
            //formShowParameter.setAppId("ukwo_xxd");
            //设置页签容器的key，这里为应用中的页签容器的标识
            formShowParameter.getOpenStyle().setTargetKey("ukwo_flexpanelap13");
            this.getView().showForm(formShowParameter);
//            备注进度
        }else if ("notecontent".equals(operateKey)){
            MobileFormShowParameter showParameter = new MobileFormShowParameter();
            showParameter.setFormId("ukwo_progress_note");
//            showParameter.setCaption("新增");
            showParameter.setStatus(OperationStatus.EDIT);
            HashMap<String, Object> paramMap = new HashMap<>();
            paramMap.put("content" ,this.getModel().getValue("ukwo_note_content"));
            paramMap.put("applyId" ,this.getModel().getValue("ukwo_apply_id"));
            showParameter.setCustomParam("params",paramMap);
            showParameter.setCloseCallBack(new CloseCallBack(this, "note"));
            showParameter.getOpenStyle().setShowType(ShowType.Floating);
            this.getView().showForm(showParameter);
        }
    }

    @Override
    public void beforeBindData(EventObject e) {
        this.getView().setEnable(false,"ukwo_ap_applicant","ukwo_ap_guarantee");
        //隐藏页签遮罩,补录按钮
        this.getView().setVisible(false,"ukwo_flexpanelap11","ukwo_cardentryfixrowap1");
        setEntryVisible(false);
        //360附件赋值
        String url360 = (String) this.getModel().getValue("ukwo_360_report_url");
        Long pkValue = (Long) this.getModel().getDataEntity().getPkValue();
        FormShowParameter showParameter = this.getView().getFormShowParameter();
        OperationStatus status = showParameter.getStatus();
//新增状态
        if(status==null || OperationStatus.ADDNEW.equals(status)){
            return;
        }
        try {
            Map<String, Object> loanStatus = XXDLoanApplicationService.getLoanStatus(pkValue);

            Object ukwoOrderStatus = loanStatus.get("ukwo_order_status");
            Object ukwoOrderStatusCode = loanStatus.get("ukwo_order_status_code");
            if(ukwoOrderStatus!=null){
                this.getModel().setValue("ukwo_order_status",ukwoOrderStatus);
            }
            if(ukwoOrderStatusCode!=null){
                this.getModel().setValue("ukwo_order_status_code",ukwoOrderStatusCode);
            }
        } catch (Exception ex) {
            logger.error("获取订单状态异常：{}",ex.getMessage());
            throw new RuntimeException(ex);
        }
        if(StringUtils.isNotEmpty(url360)){
            List<Map<String, Object>> atts = AttachmentServiceHelper.getAttachments(this.getView().getEntityId(), pkValue, "attachmentpanel");
            //仅首次需赋值附件面板
            if(atts==null || atts.size() <=0){
                XXDUtils.buildAttachmentDataFromPanel(url360,"360报告.pdf","pdf",pkValue);
            }
        }
        SaveServiceHelper.saveOperate(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME,new DynamicObject[]{this.getModel().getDataEntity(true)});
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        String name = e.getProperty().getName();
        if("ukwo_approve_amount".equals(name)){
            ChangeData changeData = e.getChangeSet()[0];
            //申报金额
            BigDecimal ukwoApplicationAmount = (BigDecimal) this.getModel().getValue("ukwo_application_amount");
            if(ukwoApplicationAmount==null || ukwoApplicationAmount.compareTo(BigDecimal.ZERO) <=0){
                this.getView().showErrorNotification("申保金额数值异常，请核实后再操作！");
                return;
            }
            BigDecimal newValue = (BigDecimal) changeData.getNewValue();
            if(newValue.compareTo(ukwoApplicationAmount) > 0){
                this.getView().showTipNotification("审批金额不能大于申保金额");
                //this.getModel().setValue("ukwo_approve_amount",changeData.getOldValue());
            }
        }
    }

//    补录页面回调，信息写入单据
    @Override
    public void closedCallBack(ClosedCallBackEvent e) {

        String actionId = e.getActionId();
        //补录担保信息
        if("addPage".equals(actionId)) {
            //子页面数据回调
            HashMap<String, Object> returnData = (HashMap<String, Object>) e.getReturnData();
            String key = (String) returnData.get("key");
            //获取修改的行数，更新数据
            String row = this.getView().getPageCache().get("row");
            this.getView().getPageCache().remove("row");
            int rowint=0;
            if(row!=null&&!"".equals(row)){
                rowint = Integer.valueOf(row);
            }else{
                int[] ints = this.getModel().batchCreateNewEntryRow("entryentity", 1);
                rowint = ints[0];
            }
            DynamicObject value = (DynamicObject) returnData.get("value");
            if("savegr".equals(key)){
                //先清空当前行附件字段
//                DynamicObjectCollection ukwoGuaAttachments = (DynamicObjectCollection) this.getModel().getValue("ukwo_gua_attachment", rowint);
//                if(ukwoGuaAttachments!=null&&ukwoGuaAttachments.size()>0){
//                    ArrayList<Object> attPks = new ArrayList<>();
//                    for (DynamicObject attobj:ukwoGuaAttachments) {
//                        DynamicObject fbasedataid = attobj.getDynamicObject("fbasedataid");
//                        Object pkValue = fbasedataid.getPkValue();
//                        if(pkValue!=null){
//                            attPks.add(pkValue);
//                        }
//                    }
//                    if(attPks.size()>0){
//                        XXDUtils.deleteAtts(attPks.toArray());
//                        //AttachmentFieldServiceHelper.removeAttachmentsByAttPkIds(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME,"tk_ukwo_gua_attachment",attPks.toArray());
//                    }
//                }


                this.getModel().setValue("ukwo_gua_signer_type","1",rowint);
                this.getModel().setValue("ukwo_relation_type",value.get("ukwo_relation_type_gr"),rowint);

                AttachmentEdit attEdit = this.getView().getControl("ukwo_gua_attachment");
                List<Map<String, Object>> attachmentData1 = (List<Map<String, Object>>) returnData.get("atts1");
                List<Map<String, Object>> attachmentData2 = (List<Map<String, Object>>) returnData.get("atts2");
                attachmentData1.forEach(attach -> {
                    //修改附件数据的uid、关联单据实体编码、关联单据实体id
                    String attName = "身份证人像面."+attach.get("type");
                    String pathCode = AttachmentFieldServiceHelper.saveTempToFileService((String) attach.get("url"), attach.get("uid"), attName);
//                    pathCode = UrlService.getAttachmentFullUrl(pathCode);
                    attach.put("url",pathCode);

                    attach.put("uid", XXDUtils.getUid().toString());
                    attach.put("entityNum", getView().getEntityId());
                    attach.put("billPkId", String.valueOf(getModel().getValue("id")));
                    attach.put("name", attName);
                });
                attachmentData2.forEach(attach -> {
                    //修改附件数据的uid、关联单据实体编码、关联单据实体id
                    String attName = "身份证国徽面."+attach.get("type");
                    String pathCode = AttachmentFieldServiceHelper.saveTempToFileService((String) attach.get("url"), attach.get("uid"), attName);
//                    pathCode = UrlService.getAttachmentFullUrl(pathCode);
                    attach.put("url",pathCode);

                    attach.put("uid", XXDUtils.getUid().toString());
                    attach.put("entityNum", getView().getEntityId());
                    attach.put("billPkId", String.valueOf(getModel().getValue("id")));
                    attach.put("name", attName);
                });
                attachmentData1.addAll(attachmentData2);
                List<DynamicObject> saveAttachments = attEdit.getAttachmentModel().saveAttachments(attEdit.getModel(), this.getView().getPageId(), this.getModel().getDataEntityType().getName(), attachmentData1);
//                List<DynamicObject> saveAttachments2 = attEdit.getAttachmentModel().saveAttachments(attEdit.getModel(), this.getView().getPageId(), this.getModel().getDataEntityType().getName(), attachmentData2);
                List<Long> idSet = new ArrayList<>();
                idSet.add(0L);
                idSet.add(0L);
                saveAttachments.forEach(att -> {
                    String name = att.getString("name");
                    if(name.contains("身份证人像面")){
                        idSet.set(0,att.getLong("id"));
                    }else if(name.contains("身份证国徽面")){
                        idSet.set(1,att.getLong("id"));
                    }
                });
                //给目标附件字段赋值
                this.getModel().setValue("ukwo_gua_attachment",idSet.toArray(),rowint);

//                this.getModel().setValue("ukwo_u_soc_cre_code_ent",value.get(""),ints[0]);
                this.getModel().setValue("ukwo_signer_name",value.get("ukwo_signer_name_gr"),rowint);
                this.getModel().setValue("ukwo_signer_idcard",value.get("ukwo_signer_idcard_gr"),rowint);
                this.getModel().setValue("ukwo_signer_phone",value.get("ukwo_signer_phone_gr"),rowint);
                this.getModel().setValue("ukwo_signer_address",value.get("ukwo_signer_address"),rowint);
            }else if("saveqy".equals(key)){
                //先清空当前行附件字段
//                DynamicObjectCollection ukwoGuaAttachments = (DynamicObjectCollection) this.getModel().getValue("ukwo_gua_attachment", rowint);
//                if(ukwoGuaAttachments!=null&&ukwoGuaAttachments.size()>0){
//                    ArrayList<Object> attPks = new ArrayList<>();
//                    for (DynamicObject attobj:ukwoGuaAttachments) {
//                        DynamicObject fbasedataid = attobj.getDynamicObject("fbasedataid");
//                        Object pkValue = fbasedataid.getPkValue();
//                        if(pkValue!=null){
//                            attPks.add(pkValue);
//                        }
//                    }
//                    if(attPks.size()>0){
//                        XXDUtils.deleteAtts(attPks.toArray());
////                        AttachmentFieldServiceHelper.removeAttachmentsByAttPkIds(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME,"tk_ukwo_gua_attachment",attPks.toArray());
//                    }
//                }


                AttachmentEdit attEdit = this.getView().getControl("ukwo_gua_attachment");
                List<Map<String, Object>> attachmentData3 = (List<Map<String, Object>>) returnData.get("atts3");
                List<Map<String, Object>> attachmentData4 = (List<Map<String, Object>>) returnData.get("atts4");
                List<Map<String, Object>> attachmentData5 = (List<Map<String, Object>>) returnData.get("atts5");
                List<Map<String, Object>> attachmentData6 = (List<Map<String, Object>>) returnData.get("atts6");
                attachmentData3.forEach(attach -> {
                    //修改附件数据的uid、关联单据实体编码、关联单据实体id
                    String attName = "身份证人像面." + attach.get("type");
                    String pathCode = AttachmentFieldServiceHelper.saveTempToFileService((String) attach.get("url"), attach.get("uid"), attName);
//                    pathCode = UrlService.getAttachmentFullUrl(pathCode);
                    attach.put("url",pathCode);

                    attach.put("uid", XXDUtils.getUid().toString());
                    attach.put("entityNum", getView().getEntityId());
                    attach.put("billPkId", String.valueOf(getModel().getValue("id")));
                    attach.put("name", attName);
                    logger.info("企业补录：身份证人像面");
                });
                attachmentData4.forEach(attach -> {
                    //修改附件数据的uid、关联单据实体编码、关联单据实体id
                    String attName = "身份证国徽面."+attach.get("type");
                    String pathCode = AttachmentFieldServiceHelper.saveTempToFileService((String) attach.get("url"), attach.get("uid"), attName);
//                    pathCode = UrlService.getAttachmentFullUrl(pathCode);
                    attach.put("url",pathCode);

                    attach.put("uid", XXDUtils.getUid().toString());
                    attach.put("entityNum", getView().getEntityId());
                    attach.put("billPkId", String.valueOf(getModel().getValue("id")));
                    attach.put("name", attName);
                    logger.info("企业补录：身份证国徽面");
                });
                attachmentData5.forEach(attach -> {
                    //修改附件数据的uid、关联单据实体编码、关联单据实体id
                    String attName = "营业执照."+attach.get("type");
                    String pathCode = AttachmentFieldServiceHelper.saveTempToFileService((String) attach.get("url"), attach.get("uid"), attName);
//                    pathCode = UrlService.getAttachmentFullUrl(pathCode);
                    attach.put("url",pathCode);

                    attach.put("uid", XXDUtils.getUid().toString());
                    attach.put("entityNum", getView().getEntityId());
                    attach.put("billPkId", String.valueOf(getModel().getValue("id")));
                    attach.put("name", attName);
                    logger.info("企业补录：营业执照");
                });
                attachmentData6.forEach(attach -> {
                    //修改附件数据的uid、关联单据实体编码、关联单据实体id
                    String attName = "决议."+attach.get("type");
                    String pathCode = AttachmentFieldServiceHelper.saveTempToFileService((String) attach.get("url"), attach.get("uid"), attName);
//                    pathCode = UrlService.getAttachmentFullUrl(pathCode);
                    attach.put("url",pathCode);

                    attach.put("uid", XXDUtils.getUid().toString());
                    attach.put("entityNum", getView().getEntityId());
                    attach.put("billPkId", String.valueOf(getModel().getValue("id")));
                    attach.put("name", attName);
                    logger.info("企业补录：决议");
                });
                attachmentData3.addAll(attachmentData4);
                attachmentData3.addAll(attachmentData5);
                attachmentData3.addAll(attachmentData6);
                List<DynamicObject> saveAttachments = attEdit.getAttachmentModel().saveAttachments(attEdit.getModel(), this.getView().getPageId(), this.getModel().getDataEntityType().getName(), attachmentData3);
//                List<DynamicObject> saveAttachments4 = attEdit.getAttachmentModel().saveAttachments(attEdit.getModel(), this.getView().getPageId(), this.getModel().getDataEntityType().getName(), attachmentData4);
//                List<DynamicObject> saveAttachments5 = attEdit.getAttachmentModel().saveAttachments(attEdit.getModel(), this.getView().getPageId(), this.getModel().getDataEntityType().getName(), attachmentData5);
//                List<DynamicObject> saveAttachments6 = attEdit.getAttachmentModel().saveAttachments(attEdit.getModel(), this.getView().getPageId(), this.getModel().getDataEntityType().getName(), attachmentData6);
                List<Long> idSet = new ArrayList<>();
                idSet.add(0L);
                idSet.add(0L);
                idSet.add(0L);
                idSet.add(0L);
                saveAttachments.forEach(att -> {
                    String name = att.getString("name");
                    if(name.contains("身份证人像面")){
                        idSet.set(0,att.getLong("id"));
                    }else if(name.contains("身份证国徽面")){
                        idSet.set(1,att.getLong("id"));
                    }else if(name.contains("营业执照")){
                        idSet.set(2,att.getLong("id"));
                    }else if(name.contains("决议")){
                        idSet.set(3,att.getLong("id"));
                    }
                    logger.info("企业补录：赋值附件:"+att);
                });
                //给目标附件字段赋值
                logger.info("企业补录：赋值附件id:"+idSet.toArray().length);
                this.getModel().setValue("ukwo_gua_attachment",idSet.toArray(),rowint);
                this.getModel().setValue("ukwo_gua_signer_type","2",rowint);
                this.getModel().setValue("ukwo_relation_type",value.get("ukwo_relation_type_qy"),rowint);
                this.getModel().setValue("ukwo_company_name_ent",value.get("ukwo_company_name_ent"),rowint);
                this.getModel().setValue("ukwo_u_soc_cre_code_ent",value.get("ukwo_u_soc_cre_code_ent"),rowint);
                this.getModel().setValue("ukwo_signer_name",value.get("ukwo_signer_name_qy"),rowint);
                this.getModel().setValue("ukwo_signer_idcard",value.get("ukwo_signer_idcard_qy"),rowint);
                this.getModel().setValue("ukwo_signer_phone",value.get("ukwo_signer_phone_qy"),rowint);
                this.getModel().setValue("ukwo_busi_lic_address",value.get("ukwo_busi_lic_address"),rowint);
            }
            SaveServiceHelper.saveOperate(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME,new DynamicObject[]{this.getModel().getDataEntity(true)});
            setEntryTabs();
        }
        if("note".equals(actionId)){
            HashMap<String, Object> returnData = (HashMap<String, Object>) e.getReturnData();
            String content = (String) returnData.get("content");
            this.getModel().setValue("ukwo_note_content",content);
            String noteTime = DateFormatUtils.format(new Date(), FormConstant.DATETIME_FORMAT);
            this.getModel().setValue("ukwo_note_time",noteTime);
        }
    }


    public void registerListener (EventObject e){
    // 页签添加监听事件
        Tab tab = this.getView().getControl("ukwo_tabap");
        tab.addTabSelectListener(this);
    //附件面板-附件增删监听
        AttachmentPanel attachmentPanel = this.getControl("ukwo_ap_applicant");
        attachmentPanel.addUploadListener(this);
        AttachmentPanel attachmentPanel1 = this.getControl("ukwo_ap_guarantee");
        attachmentPanel1.addUploadListener(this);
    }

  // 重写tabSelected方法
    public void tabSelected(TabSelectEvent event){
        String subTabKey = event.getTabKey();
        logger.info("setColor:"+subTabKey);
        if ("ukwo_tabpageap1".equals(subTabKey)) {
            List<Map<String, Object>> atts = AttachmentServiceHelper.
                    getAttachments(FormConstant.LOAN_APPLY_ORDER_ENTITY_NAME, this.getModel().getDataEntity().getPkValue(), "attachmentpanel");
            if(atts.size()<=0){
                this.getView().showTipNotification("没有找到关联的360报告");
                return;
            }

            //在360报告页签打开页面，页面上发送预览附件前端请求
            MobileFormShowParameter formShowParameter = new MobileFormShowParameter();
            formShowParameter.setFormId("ukwo_opentaskpage");
            formShowParameter.getOpenStyle().setShowType(ShowType.InContainer);
//            formShowParameter.getOpenStyle().setShowType(ShowType.Modal);
            //formShowParameter.setHasRight(true);
            HashMap<String, Object> paramMap = new HashMap<>();
            paramMap.put("att",atts.get(0));
            formShowParameter.setCustomParam("params",paramMap);
            //打开的应用appId
            //formShowParameter.setAppId("ukwo_xxd");
            //设置页签容器的key，这里为应用中的页签容器的标识
            formShowParameter.getOpenStyle().setTargetKey("ukwo_flexpanelap13");
            this.getView().showForm(formShowParameter);


//            IFrame iframe = this.getView().getControl("ukwo_iframeap");// 获得IFrame控件对象
//            IDataModel dataModel = this.getModel();
//            String reportUrl = dataModel.getValue("ukwo_360_report_url").toString();    //360报告地址
//            if (StringUtils.isNotEmpty(reportUrl)) {
////                String url = "http://223.151.52.25:8099/ierp/attachment/preview.do?path=/0e176cad922d4c2f84228294fae0d2d7&appId=ukwo_demo_ht&fId=ukwo_purreq_ht&pageId=root9e28da376b894876b8ed3f45ca849a83_ukwo_purreq_ht_1612253207424169984&kd_cs_ticket=WGMl8FsqjhgCr4yvhS39GxRfiMyll62C";
////                iframe.setSrc(url);
//                iframe.setSrc(reportUrl);
//            }
        }
//        判断行数,界面规则配置
        if ("ukwo_tabpageap2".equals(subTabKey)) {
            //this.getView().setVisible(true,"");
        }
        //获取单据在流程中所处的节点
//        List<IApprovalRecordGroup> allApprovalRecord = WorkflowServiceHelper.getAllApprovalRecord(this.getModel().getDataEntity().getPkValue().toString());
//        logger.info("setColor:"+allApprovalRecord.size()+"-Record.size");
       // if(allApprovalRecord==null || allApprovalRecord.size()==0 || allApprovalRecord.size()==3){
            // 点击页签的key
        String tabs = this.getView().getPageCache().get("tabs");
        String openaddinfo = this.getPageCache().get("openaddinfo");
        if("ukwo_tabpageap0".equals(subTabKey)){
            if(tabs!=null){
                this.getView().setVisible(true,"ukwo_bar_nextstep");
            }
            this.getView().setVisible(true,"tbmain");
            this.getView().setVisible(false,"ukwo_bar_prevstep");
            // 设置覆盖标签颜色
            setColor(1);
        }
        if("ukwo_tabpageap1".equals(subTabKey)){
            this.getView().setVisible(true,"tbmain");
            if(tabs!=null){
                this.getView().setVisible(true,"ukwo_bar_prevstep","ukwo_bar_nextstep");
            }

        }
        if("ukwo_tabpageap2".equals(subTabKey)){
            if(openaddinfo!=null){
                this.getView().setVisible(false,"tbmain");
                this.getPageCache().remove("openaddinfo");
            }
            if(tabs!=null){
                this.getView().setVisible(true,"ukwo_bar_prevstep","ukwo_bar_nextstep");
            }
            // 设置覆盖标签颜色
            setColor(2);
        }
        if("ukwo_tabpageap3".equals(subTabKey)){
            this.getView().setVisible(true,"tbmain");
            if(tabs!=null){
                this.getView().setVisible(true,"ukwo_bar_prevstep","ukwo_bar_nextstep");
            }
            // 设置覆盖标签颜色
            setColor(3);
        }
        if("ukwo_tabpageap4".equals(subTabKey)){
            this.getView().setVisible(true,"tbmain");
            if(tabs!=null){
                this.getView().setVisible(true,"ukwo_bar_prevstep");

            }
            this.getView().setVisible(false,"ukwo_bar_nextstep");
            // 设置覆盖标签颜色
            setColor(4);
        }
        //}
    }

    /**
     * 设置标签颜色
     * @param index
     */
    private void setColor(int index ){
        logger.info("setColor:"+index+"-index");
        // 蓝色
        Map<String, Object> color1 = new HashMap<String, Object>();
        color1.put(ClientProperties.ForeColor, "#276ff5");
        // 黑色
        Map<String, Object> color2 = new HashMap<String, Object>();
        color2.put(ClientProperties.ForeColor, "#404040");
        //业务信息
        this.getView().updateControlMetadata("ukwo_labelap21",index==1?color1:color2);
        //反担保信息
        this.getView().updateControlMetadata("ukwo_labelap23",index==2?color1:color2);
        //附件信息
        this.getView().updateControlMetadata("ukwo_labelap24",index==3?color1:color2);
        //审批信息
        this.getView().updateControlMetadata("ukwo_labelap2",index==4?color1:color2);
    }

    @Override
    public void afterUpload(UploadEvent evt) {
        UploadListener.super.afterUpload(evt);
        //        附件上传异步回调
        this.getView().addClientCallBack("upload",1);
    }

    @Override
    public void afterRemove(UploadEvent evt) {
        UploadListener.super.afterRemove(evt);
        //        附件删除异步回调
        this.getView().addClientCallBack("remove",1);
    }
//异步回调
    @Override
    public void clientCallBack(ClientCallBackEvent e) {
        if ("upload".equals(e.getName())||"remove".equals(e.getName())) {
            this.getView().invokeOperation("save");
        }
    }

    @Override
    public void afterBindData(EventObject e) {
        // 反担保信息标签只显示一个
        setEntryTabs();
        super.afterBindData(e);
    }

    /*
    * 卡片分录，本人、反担保区分标识
    * */
    public void setEntryTabs(){
        DynamicObjectCollection entryentity =this.getModel().getEntryEntity("entryentity");
        if(entryentity.size()>2){
            CardEntry entry = this.getControl("entryentity");
            entry.setChildVisible(Boolean.TRUE, 1,"ukwo_fandanbao");
            for(int i=2;i<entryentity.size();i++){
                entry.setChildVisible(Boolean.FALSE, i,"ukwo_fandanbao");
            }
        }
    }
    /*
    * 分录卡片显隐设置
    * */
    public void setEntryVisible(Boolean flag){
        DynamicObjectCollection entryentity =this.getModel().getEntryEntity("entryentity");
        if(entryentity.size()>0){
            CardEntry entry = this.getControl("entryentity");
            for(int i=0;i<entryentity.size();i++){
                entry.setChildVisible(flag, i,"ukwo_cardentryflexpanelap3");
            }
        }
    }
}
