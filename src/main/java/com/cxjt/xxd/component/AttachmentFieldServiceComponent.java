package com.cxjt.xxd.component;

import com.cxjt.xxd.constants.FormConstant;
import com.cxjt.xxd.util.XXDUtils;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;

/**
 * 单据附件字段工具类
 */
public class AttachmentFieldServiceComponent {

    private static Log logger = LogFactory.getLog(AttachmentFieldServiceComponent.class);

    /**
     * 上传附件到附件字段
     *
     * @param url
     * @param name
     * @return
     */
    public static Long buildAttachmentDataFromEdit(String url, String name) {
        int extIndex = name.lastIndexOf(".");
        String type = name.substring(extIndex);

        Long attachmentId = XXDUtils.buildAttachmentDataFromEdit(url, name, type);

        return attachmentId;
    }

    /**
     * 将上传到【附件字段】的附件,绑定到单据体行上,绑定后调用SaveServiceHelper.save将表单对象入库
     *
     * @param attachmentId 【上传附件到附件字段】后返回的唯一标识
     * @param entityRow    【附件】所属单据体行
     * @param panelKey     【流程设计器】-【单据体】-【附件字段】标识
     */
    public static void bind(long attachmentId, DynamicObject entityRow, String panelKey) {
        DynamicObjectCollection ukwoAttachmentfield = (DynamicObjectCollection) entityRow.get(panelKey);
        DynamicObject dynamicObject = ukwoAttachmentfield.addNew();
        dynamicObject.set("fbasedataid_id", attachmentId);
    }

    /**
     * 判断【贷款申请单-单据体行】是否已包含入参文件
     * @param guaranteeEntity 贷款申请单-单据体行
     * @param ukwoAttachmentName 将要绑定到【贷款申请单-单据体行】的文件名
     * @return true:已包含,false:未包含
     */
    public static boolean contain(DynamicObject guaranteeEntity, String ukwoAttachmentName) {
        DynamicObjectCollection attachmentEditList = (DynamicObjectCollection) guaranteeEntity.get(FormConstant.LOAN_APPLY_ORDER_ENTITY_ATTACHMENT_FIELD_NAME);
        boolean result = false;

        for (DynamicObject attachmentEdit : attachmentEditList) {
            //logger.info("开始执行[{}]在单据体附件字段存在性判断,单据体行=[{}]",ukwoAttachmentName,attachmentEdit.toString());
            DynamicObject attachment = (DynamicObject) attachmentEdit.get("fbasedataid");
            if(attachment == null){
                continue;
            }
            //String name = attachment.getString("name");
            String name = attachment.getLocaleString("name").toString();
            //String url = attachment.getString("url");
            if (name.contains(ukwoAttachmentName)) {
                result = true;
                break;
            }
        }

        return result;
    }
}
