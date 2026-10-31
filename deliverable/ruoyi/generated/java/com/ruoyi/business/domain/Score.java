package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 月考成绩对象 biz_score
 * 
 * @author ruoyi
 * @date 2026-10-31
 */
public class Score extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 成绩ID（主键） */
    private String scoreId;

    /** 学号 */
    @Excel(name = "学号")
    private String studentId;

    /** 月考场次编号 */
    @Excel(name = "月考场次编号")
    private String examId;

    /** 科目编号 */
    @Excel(name = "科目编号")
    private String subjectId;

    /** 得分（0 ~ 该科满分） */
    @Excel(name = "得分", readConverterExp = "0=,~=,该=科满分")
    private Long score;

    /** 得分率 = score / full_score（冗余，便于排序统计） */
    @Excel(name = "得分率 = score / full_score", readConverterExp = "冗=余，便于排序统计")
    private BigDecimal scoreRate;

    /** 等级（A 优秀 / B 良好 / C 及格 / D 待提高） */
    @Excel(name = "等级", readConverterExp = "A=,优=秀,/=,B=,良=好,/=,C=,及=格,/=,D=,待=提高")
    private String level;

    /** 状态（NORMAL 正常 / APPEALING 查分中 / MODIFIED 已更正） */
    @Excel(name = "状态", readConverterExp = "N=ORMAL,正=常,/=,A=PPEALING,查=分中,/=,M=ODIFIED,已=更正")
    private String status;

    /** 录入人（工号） */
    @Excel(name = "录入人", readConverterExp = "工=号")
    private String recorderId;

    /** 录入时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "录入时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date recordTime;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

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

    public void setExamId(String examId) 
    {
        this.examId = examId;
    }

    public String getExamId() 
    {
        return examId;
    }

    public void setSubjectId(String subjectId) 
    {
        this.subjectId = subjectId;
    }

    public String getSubjectId() 
    {
        return subjectId;
    }

    public void setScore(Long score) 
    {
        this.score = score;
    }

    public Long getScore() 
    {
        return score;
    }

    public void setScoreRate(BigDecimal scoreRate) 
    {
        this.scoreRate = scoreRate;
    }

    public BigDecimal getScoreRate() 
    {
        return scoreRate;
    }

    public void setLevel(String level) 
    {
        this.level = level;
    }

    public String getLevel() 
    {
        return level;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
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

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("scoreId", getScoreId())
            .append("studentId", getStudentId())
            .append("examId", getExamId())
            .append("subjectId", getSubjectId())
            .append("score", getScore())
            .append("scoreRate", getScoreRate())
            .append("level", getLevel())
            .append("status", getStatus())
            .append("recorderId", getRecorderId())
            .append("recordTime", getRecordTime())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
