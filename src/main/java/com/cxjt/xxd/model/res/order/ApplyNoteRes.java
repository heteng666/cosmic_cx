package com.cxjt.xxd.model.res.order;

import kd.bos.openapi.common.custom.annotation.ApiModel;
import kd.bos.openapi.common.custom.annotation.ApiParam;

import java.io.Serializable;

@ApiModel
public class ApplyNoteRes implements Serializable {
    
    @ApiParam(value = "业务编号", required = true)
    private String applyId;

    @ApiParam(value = "备注内容", required = true)
    private String noteContent;

    @ApiParam(value = "备注时间", required = true)
    private String noteTime;

    public ApplyNoteRes() {

    }

    public ApplyNoteRes(String applyId, String noteContent, String noteTime) {
        this.applyId = applyId;
        this.noteContent = noteContent;
        this.noteTime = noteTime;
    }

    public String getApplyId() {
        return applyId;
    }

    public void setApplyId(String applyId) {
        this.applyId = applyId;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }

    public String getNoteTime() {
        return noteTime;
    }

    public void setNoteTime(String noteTime) {
        this.noteTime = noteTime;
    }
}
