package com.cxjt.xxd.component;

import kd.bos.servicehelper.workflow.WorkflowServiceHelper;
import kd.bos.workflow.component.approvalrecord.IApprovalRecordGroup;
import kd.bos.workflow.component.approvalrecord.IApprovalRecordItem;

import java.util.List;

public class WorkflowComponent {

    public static IApprovalRecordItem getApprovalRecordItem(String businessKey){
        List<IApprovalRecordGroup> approvalRecordList = WorkflowServiceHelper.getAllApprovalRecord(businessKey);
        int approvalRecordIndex = approvalRecordList.size() - 1;
        IApprovalRecordGroup approvalRecord  =  approvalRecordList.get(approvalRecordIndex);

        List<IApprovalRecordItem> approvalRecordItemList = approvalRecord.getChildren();
        int approvalRecordItemIndex = approvalRecordItemList.size() - 1;
        IApprovalRecordItem approvalRecordItem =approvalRecordItemList.get(approvalRecordItemIndex);
        return approvalRecordItem;
    }
}
