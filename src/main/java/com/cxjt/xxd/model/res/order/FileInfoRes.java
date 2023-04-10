package com.cxjt.xxd.model.res.order;

import java.io.Serializable;

public class FileInfoRes implements Serializable {
    //文件类型
    private String fileType;

    //文件名
    private String fileName;

    //文件URL
    private String fileUrl;

    //文件内容(Base64字符串)
    private String fileContent;

    //单据体行唯一标识
    private String uniCode;

    public FileInfoRes() {

    }

    public FileInfoRes(String fileType, String fileName, String fileContent, String uniCode) {
        this.fileType = fileType;
        this.fileName = fileName;
        this.fileContent = fileContent;
        this.uniCode = uniCode;
    }



    public FileInfoRes(String fileName, String fileUrl, String fileContent) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileContent = fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileContent() {
        return fileContent;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public String getUniCode() {
        return uniCode;
    }

    public void setUniCode(String uniCode) {
        this.uniCode = uniCode;
    }
}
