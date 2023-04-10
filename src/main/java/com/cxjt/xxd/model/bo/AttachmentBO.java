package com.cxjt.xxd.model.bo;

import java.io.Serializable;

public class AttachmentBO implements Serializable {

    //文件类型
    private String ukwoAttachmentType;

    //文件名
    private String ukwoAttachmentName;

    //文件URL
    private String ukwoAttachmentUrl;

    //单据体行唯一标识
    private String ukwoGuaUniCodeEnt;


    public AttachmentBO(String ukwoAttachmentType, String ukwoAttachmentName, String ukwoAttachmentUrl, String ukwoGuaUniCodeEnt) {
        this.ukwoAttachmentType = ukwoAttachmentType;
        this.ukwoAttachmentName = ukwoAttachmentName;
        this.ukwoAttachmentUrl = ukwoAttachmentUrl;
        this.ukwoGuaUniCodeEnt = ukwoGuaUniCodeEnt;
    }

    public AttachmentBO(String ukwoAttachmentType, String ukwoAttachmentName, String ukwoAttachmentUrl) {
        this.ukwoAttachmentType = ukwoAttachmentType;
        this.ukwoAttachmentName = ukwoAttachmentName;
        this.ukwoAttachmentUrl = ukwoAttachmentUrl;
    }

    public AttachmentBO() {

    }

    public String getUkwoAttachmentType() {
        return ukwoAttachmentType;
    }

    public void setUkwoAttachmentType(String ukwoAttachmentType) {
        this.ukwoAttachmentType = ukwoAttachmentType;
    }

    public String getUkwoAttachmentName() {
        return ukwoAttachmentName;
    }

    public void setUkwoAttachmentName(String ukwoAttachmentName) {
        this.ukwoAttachmentName = ukwoAttachmentName;
    }

    public String getUkwoAttachmentUrl() {
        return ukwoAttachmentUrl;
    }

    public void setUkwoAttachmentUrl(String ukwoAttachmentUrl) {
        this.ukwoAttachmentUrl = ukwoAttachmentUrl;
    }

    public String getUkwoGuaUniCodeEnt() {
        return ukwoGuaUniCodeEnt;
    }

    public void setUkwoGuaUniCodeEnt(String ukwoGuaUniCodeEnt) {
        this.ukwoGuaUniCodeEnt = ukwoGuaUniCodeEnt;
    }
}
