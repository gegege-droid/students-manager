package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 班级管理对象 edu_classroom
 * 
 * @author grade-team
 * @date 2026-10-31
 */
public class Classroom extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 班级编号 */
    @Excel(name = "班级编号")
    private String classId;

    /** 班级名称 */
    @Excel(name = "班级名称")
    private String className;

    /** 年级 */
    @Excel(name = "年级")
    private String grade;

    /** 班主任工号 */
    @Excel(name = "班主任工号")
    private String headTeacherId;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public void setClassId(String classId) 
    {
        this.classId = classId;
    }

    public String getClassId() 
    {
        return classId;
    }

    public void setClassName(String className) 
    {
        this.className = className;
    }

    public String getClassName() 
    {
        return className;
    }

    public void setGrade(String grade) 
    {
        this.grade = grade;
    }

    public String getGrade() 
    {
        return grade;
    }

    public void setHeadTeacherId(String headTeacherId) 
    {
        this.headTeacherId = headTeacherId;
    }

    public String getHeadTeacherId() 
    {
        return headTeacherId;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("classId", getClassId())
            .append("className", getClassName())
            .append("grade", getGrade())
            .append("headTeacherId", getHeadTeacherId())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
