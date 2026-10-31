package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 成绩管理对象 edu_score
 * 
 * @author grade-team
 * @date 2026-10-31
 */
public class Score extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 成绩编号 */
    @Excel(name = "成绩编号")
    private String scoreId;

    /** 学号 */
    @Excel(name = "学号")
    private String studentId;

    /** 课程编号 */
    @Excel(name = "课程编号")
    private String courseId;

    /** 学期 */
    @Excel(name = "学期")
    private String term;

    /** 平时成绩（0-100） */
    @Excel(name = "平时成绩", readConverterExp = "0=-100")
    private Integer usualScore;

    /** 期末成绩（0-100） */
    @Excel(name = "期末成绩", readConverterExp = "0=-100")
    private Integer finalScore;

    /** 总评成绩 */
    @Excel(name = "总评成绩")
    private Integer totalScore;

    /** 绩点 */
    @Excel(name = "绩点")
    private BigDecimal gpa;

    /** 总评是否人工更正（0否 1是） */
    @Excel(name = "总评是否人工更正", readConverterExp = "0=否,1=是")
    private String manualTotal;

    /** 录入人工号 */
    @Excel(name = "录入人工号")
    private String recorderId;

    /** 录入时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "录入时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date recordTime;

    public void setScoreId(String scoreId) 
    {
        this.scoreId = scoreId;
    }

    public String getScoreId() 
    {
        return scoreId;
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

    public void setUsualScore(Integer usualScore) 
    {
        this.usualScore = usualScore;
    }

    public Integer getUsualScore() 
    {
        return usualScore;
    }

    public void setFinalScore(Integer finalScore) 
    {
        this.finalScore = finalScore;
    }

    public Integer getFinalScore() 
    {
        return finalScore;
    }

    public void setTotalScore(Integer totalScore) 
    {
        this.totalScore = totalScore;
    }

    public Integer getTotalScore() 
    {
        return totalScore;
    }

    public void setGpa(BigDecimal gpa) 
    {
        this.gpa = gpa;
    }

    public BigDecimal getGpa() 
    {
        return gpa;
    }

    public void setManualTotal(String manualTotal) 
    {
        this.manualTotal = manualTotal;
    }

    public String getManualTotal() 
    {
        return manualTotal;
    }

    public void setRecorderId(String recorderId) 
    {
        this.recorderId = recorderId;
    }

    public String getRecorderId() 
    {
        return recorderId;
    }

    public void setRecordTime(Date recordTime) 
    {
        this.recordTime = recordTime;
    }

    public Date getRecordTime() 
    {
        return recordTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("scoreId", getScoreId())
            .append("studentId", getStudentId())
            .append("courseId", getCourseId())
            .append("term", getTerm())
            .append("usualScore", getUsualScore())
            .append("finalScore", getFinalScore())
            .append("totalScore", getTotalScore())
            .append("gpa", getGpa())
            .append("manualTotal", getManualTotal())
            .append("recorderId", getRecorderId())
            .append("recordTime", getRecordTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
