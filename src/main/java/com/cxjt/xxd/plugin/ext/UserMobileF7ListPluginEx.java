package com.cxjt.xxd.plugin.ext;

import kd.bos.filter.FilterContainer;
import kd.bos.form.events.FilterContainerInitArgs;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.form.field.events.BeforeFilterF7SelectEvent;
import kd.bos.form.field.events.BeforeFilterF7SelectListener;
import kd.bos.list.ListShowParameter;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.org.utils.Consts;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;

import java.util.EventObject;

public class UserMobileF7ListPluginEx extends AbstractListPlugin implements BeforeFilterF7SelectListener {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);

        // 侦听过滤面板上，过滤字段F7点击事件：设置自定义组织过滤字段，F7打开的基础资料列表
        FilterContainer filterContainer = this.getView().getControl("mobfilterpanel");
        filterContainer.addBeforeF7SelectListener(this);
    }

    /**
     * 过滤面板，基础资料字段，点击F7时触发此事件
     * @remark
     * 1. 如果点击的是自定义的组织过滤字段，则自行构建组织F7列表显示参数
     */
    @Override
    public void beforeF7Select(BeforeFilterF7SelectEvent arg0) {



    }

    @Override
    public void setFilter(SetFilterEvent e) {
        /*if(this.getView().getParentView()!=null){
            String entityId = this.getView().getParentView().getEntityId();
            if("wf_approvaldealpagemobile".equals(entityId)){
                // 未分配部门或者主职部门
                QFilter dptFilter = QFilter.isNull(Consts.ENTRY_ENTITY);
                dptFilter = dptFilter.or(new QFilter("entryentity.ispartjob", QFilter.equals, Boolean.FALSE));
                e.getQFilters().add(dptFilter);
            }

        }*/
    }
    @Override
    public void filterColumnSetFilter(SetFilterEvent args) {
        System.err.println("filterColumnSetFilter");
        if ("dpt.name".equals(args.getFieldName())){
            args.addCustomQFilter(new QFilter("name", QFilter.equals, "安乡办事处"));
        }
        super.filterColumnSetFilter(args);
    }


    @Override
    public void filterContainerInit(FilterContainerInitArgs args) {
        System.err.println("filterColumnSetFilter");
        System.err.println("filterColumnSetFilter");
    }
}
