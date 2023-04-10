package com.cxjt.xxd.component;

import com.cxjt.xxd.util.XXDUtils;
import com.cxjt.xxd.enums.AttachmentEnum;
import com.cxjt.xxd.model.bo.AttachmentBO;
import com.cxjt.xxd.model.res.order.FileInfoRes;
import com.cxjt.xxd.util.FileUtil;
import kd.bos.servicehelper.AttachmentServiceHelper;
import kd.bos.url.UrlService;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AttachmentServiceComponent {

    /**
     * 根据【路径映射】获取完整URL,若【路径映射】为完整URL,则原样返回
     *
     * @param path 路径映射
     * @return
     */
    public static String getFullUrlByPathCode(String path) {
        if (path.contains("http:")) {
            return path;
        }

        String attachmentFullUrl = UrlService.getAttachmentFullUrl(path);
        return attachmentFullUrl;
    }

    /**
     * 获取表单附件
     *
     * @param formId
     * @param pkId
     * @param attachKey
     * @return
     */
    public static List<Map<String, Object>> getAttachments(String formId, Object pkId, String attachKey) {
        List<Map<String, Object>> attachments = AttachmentServiceHelper.getAttachments(formId, pkId, attachKey);
        return attachments;
    }


    /**
     * 获取表单附件
     *
     * @param formId
     * @param pkId
     * @param attachKey
     * @param attachmentEnum
     * @return
     */
    @Deprecated
    public static List<FileInfoRes> getFileInfoList(String formId, Object pkId, String attachKey, AttachmentEnum attachmentEnum) {

        List<Map<String, Object>> applicantAttachments = getAttachments(formId, pkId, attachKey);

        List<FileInfoRes> fileInfoList = Collections.EMPTY_LIST;

        if (CollectionUtils.isEmpty(applicantAttachments)) {
            return fileInfoList;
        }

        fileInfoList = new ArrayList<>(applicantAttachments.size());

        for (Map<String, Object> applicantAttachment : applicantAttachments) {
            String name = (String) applicantAttachment.get("name");
            String url = (String) applicantAttachment.get("url");
            String base64Str = FileUtil.byte2Base64(url);
            FileInfoRes fileInfo = new FileInfoRes(attachmentEnum.getCode(), name, url);
            fileInfoList.add(fileInfo);
        }

        return fileInfoList;
    }

    /**
     * 获取表单附件
     *
     * @param formId
     * @param pkId
     * @param attachKey
     * @param attachmentEnum
     * @return
     */
    public static List<AttachmentBO> getAttachmentInfoList(String formId, Object pkId, String attachKey, AttachmentEnum attachmentEnum) {

        List<Map<String, Object>> applicantAttachments = getAttachments(formId, pkId, attachKey);

        List<AttachmentBO> fileInfoList = Collections.EMPTY_LIST;

        if (CollectionUtils.isEmpty(applicantAttachments)) {
            return fileInfoList;
        }

        fileInfoList = new ArrayList<>(applicantAttachments.size());

        for (Map<String, Object> applicantAttachment : applicantAttachments) {
            String name = (String) applicantAttachment.get("name");
            String url = (String) applicantAttachment.get("url");
            AttachmentBO fileInfo = new AttachmentBO(attachmentEnum.getCode(), name, url);
            fileInfoList.add(fileInfo);
        }

        return fileInfoList;
    }

    /**
     * 根据url上传附件至服务器并绑定单据附件面板
     *
     * @param url    文件url
     * @param name   文件名称含扩展名
     * @param type   文件类型
     * @param billId 单据id
     */
    public static void buildAttachmentDataFromPanel(String url, String name, String type, Object billId) {
        XXDUtils.buildAttachmentDataFromPanel(url, name, type, billId);
    }
}
