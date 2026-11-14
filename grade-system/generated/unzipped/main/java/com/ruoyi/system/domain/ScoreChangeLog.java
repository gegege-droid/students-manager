package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 成绩修改记录对象 edu_score_change_log
 * 
 * @author grade-team
 * @date 2026-10-31
 */
public class ScoreChangeLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 日志编号 */
    @Excel(name = "日志编号")
    private String logId;

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

    /** 原平时成绩 */
    @Excel(name = "原平时成绩")
    private Integer oldUsual;

    /** 原期末成绩 */
    @Excel(name = "原期末成绩")
    private Integer oldFinal;

    /** 原总评成绩 */
    @Excel(name = "原总评成绩")
    private Integer oldTotal;

    /** 新平时成绩 */
    @Excel(name = "新平时成绩")
    private Integer newUsual;

    /** 新期末成绩 */
    @Excel(name = "新期末成绩")
    private Integer newFinal;

    /** 新总评成绩 */
    @Excel(name = "新总评成绩")
    private Integer newTotal;

    /** 修改原因（5-100字） */
    @Excel(name = "修改原因", readConverterExp = "5=-100字")
    private String changeReason;

    /** 操作人工号 */
    @Excel(name = "操作人工号")
    private String operatorId;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date operateTime;

    /** 来源（MANUAL手工 APPEAL查分受理） */
    @Excel(name = "来源", readConverterExp = "M=ANUAL手工,A=PPEAL查分受理")
    private String source;

    public void setLogId(String logId) 
    {
        this.logId = logId;
    }

    public String getLogId() 
    {
        return logId;
    }

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

    public void setOldUsual(Integer oldUsual) 
    {
        this.oldUsual = oldUsual;
    }

    public Integer getOldUsual() 
    {
        return oldUsual;
    }

    public void setOldFinal(Integer oldFinal) 
    {
        this.oldFinal = oldFinal;
    }

    public Integer getOldFinal() 
    {
        return oldFinal;
    }

    public void setOldTotal(Integer oldTotal) 
    {
        this.oldTotal = oldTotal;
    }

    public Integer getOldTotal() 
    {
        return oldTotal;
    }

    public void setNewUsual(Integer newUsual) 
    {
        this.newUsual = newUsual;
    }

    public Integer getNewUsual() 
    {
        return newUsual;
    }

    public void setNewFinal(Integer newFinal) 
    {
        this.newFinal = newFinal;
    }

    public Integer getNewFinal() 
    {
        return newFinal;
    }

    public void setNewTotal(Integer newTotal) 
    {
        this.newTotal = newTotal;
    }

    public Integer getNewTotal() 
    {
        return newTotal;
    }

    public void setChangeReason(String changeReason) 
    {
        this.changeReason = changeReason;
    }

    public String getChangeReason() 
    {
        return changeReason;
    }

    public void setOperatorId(String operatorId) 
    {
        this.operatorId = operatorId;
    }

    public String getOperatorId() 
    {
        return operatorId;
    }

    public void setOperateTime(Date operateTime) 
    {
        this.operateTime = operateTime;
    }

    public Date getOperateTime() 
    {
        return operateTime;
    }

    public void setSource(String source) 
    {
        this.source = source;
    }

    public String getSource() 
    {
        return source;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("logId", getLogId())
            .append("scoreId", getScoreId())
            .append("studentId", getStudentId())
            .append("courseId", getCourseId())
            .append("term", getTerm())
            .append("oldUsual", getOldUsual())
            .append("oldFinal", getOldFinal())
            .append("oldTotal", getOldTotal())
            .append("newUsual", getNewUsual())
            .append("newFinal", getNewFinal())
            .append("newTotal", getNewTotal())
            .append("changeReason", getChangeReason())
            .append("operatorId", getOperatorId())
            .append("operateTime", getOperateTime())
            .append("source", getSource())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
