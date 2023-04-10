package com.cxjt.xxd.plugin.pc;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.container.Tab;
import kd.bos.form.control.IFrame;
import kd.bos.form.control.events.*;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import java.util.EventObject;

public class XXDReport360Plugin extends AbstractBillPlugIn implements TabSelectListener {
    private final Log LOGGER = LogFactory.getLog(this.getClass());

    private final static String TAB_CONTROL_KEY = "ukwo_tabap"; //页签控件标识
    private final static String IFRAME_CONTROL_KEY = "ukwo_360_iframe"; //IFrame控件标识

    private final static String TAB_KEY = "ukwo_tab_360_report"; //子页签标识

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        Tab tab = this.getView().getControl(TAB_CONTROL_KEY);
        tab.addTabSelectListener(this);
    }

    @Override
    public void tabSelected(TabSelectEvent event) {
        String tabKey = event.getTabKey();  //当前选中子页签的key
        if (TAB_KEY.equals(tabKey)) {
            IFrame iframe = this.getView().getControl(IFRAME_CONTROL_KEY);// 获得IFrame控件对象
            IDataModel dataModel = this.getModel();
            String reportUrl = dataModel.getValue("ukwo_360_report_url").toString();    //360报告地址
            if (StringUtils.isNotEmpty(reportUrl)) {
                iframe.setSrc(reportUrl);
            }
        }
    }
}
