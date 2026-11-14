package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 查分申请管理对象 edu_appeal
 * 
 * @author grade-team
 * @date 2026-10-31
 */
public class Appeal extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 申请编号 */
    @Excel(name = "申请编号")
    private String appealId;

    /** 学号 */
    @Excel(name = "学号")
    private String studentId;

    /** 课程编号 */
    @Excel(name = "课程编号")
    private String courseId;

    /** 学期 */
    @Excel(name = "学期")
    private String term;

    /** 申请时原总评成绩 */
    @Excel(name = "申请时原总评成绩")
    private Integer originalScore;

    /** 申请理由（10-200字） */
    @Excel(name = "申请理由", readConverterExp = "1=0-200字")
    private String reason;

    /** 申请时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "申请时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date applyTime;

    /** 状态（PENDING待受理 ACCEPTED已受理 REJECTED已驳回） */
    @Excel(name = "状态", readConverterExp = "P=ENDING待受理,A=CCEPTED已受理,R=EJECTED已驳回")
    private String status;

    /** 处理结果（KEEP维持 CORRECT更正） */
    @Excel(name = "处理结果", readConverterExp = "K=EEP维持,C=ORRECT更正")
    private String result;

    /** 更正后成绩 */
    @Excel(name = "更正后成绩")
    private Integer correctedScore;

    /** 处理意见（5-200字） */
    @Excel(name = "处理意见", readConverterExp = "5=-200字")
    private String opinion;

    /** 受理人工号 */
    @Excel(name = "受理人工号")
    private String handlerId;

    /** 受理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "受理时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date handleTime;

    public void setAppealId(String appealId) 
    {
        this.appealId = appealId;
    }

    public String getAppealId() 
    {
        return appealId;
    }

    public void setStudentId(String studentId) 
    {
        this.studentId = studentId;
    }

    public String getStudentId() 
    {
        return studentId;
    }

    public void setCourseId(String courseId) 
    {
        this.courseId = courseId;
    }

    public String getCourseId() 
    {
        return courseId;
    }

    public void setTerm(String term) 
    {
        this.term = term;
    }

    public String getTerm() 
    {
        return term;
    }

    public void setOriginalScore(Integer originalScore) 
    {
        this.originalScore = originalScore;
    }

    public Integer getOriginalScore() 
    {
        return originalScore;
    }

    public void setReason(String reason) 
    {
        this.reason = reason;
    }

    public String getReason() 
    {
        return reason;
    }

    public void setApplyTime(Date applyTime) 
    {
        this.applyTime = applyTime;
    }

    public Date getApplyTime() 
    {
        return applyTime;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setResult(String result) 
    {
        this.result = result;
    }

    public String getResult() 
    {
        return result;
    }

    public void setCorrectedScore(Integer correctedScore) 
    {
        this.correctedScore = correctedScore;
    }

    public Integer getCorrectedScore() 
    {
        return correctedScore;
    }

    public void setOpinion(String opinion) 
    {
        this.opinion = opinion;
    }

    public String getOpinion() 
    {
        return opinion;
    }

    public void setHandlerId(String handlerId) 
    {
        this.handlerId = handlerId;
    }

    public String getHandlerId() 
    {
        return handlerId;
    }

    public void setHandleTime(Date handleTime) 
    {
        this.handleTime = handleTime;
    }

    public Date getHandleTime() 
    {
        return handleTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("appealId", getAppealId())
            .append("studentId", getStudentId())
            .append("courseId", getCourseId())
            .append("term", getTerm())
            .append("originalScore", getOriginalScore())
            .append("reason", getReason())
            .append("applyTime", getApplyTime())
            .append("status", getStatus())
            .append("result", getResult())
            .append("correctedScore", getCorrectedScore())
            .append("opinion", getOpinion())
            .append("handlerId", getHandlerId())
            .append("handleTime", getHandleTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
