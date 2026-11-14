package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.Classroom;

/**
 * 班级管理Service接口
 * 
 * @author grade-team
 * @date 2026-10-31
 */
public interface IClassroomService 
{
    /**
     * 查询班级管理
     * 
     * @param classId 班级管理主键
     * @return 班级管理
     */
    public Classroom selectClassroomByClassId(String classId);

    /**
     * 查询班级管理列表
     * 
     * @param classroom 班级管理
     * @return 班级管理集合
     */
    public List<Classroom> selectClassroomList(Classroom classroom);

    /**
     * 新增班级管理
     * 
     * @param classroom 班级管理
     * @return 结果
     */
    public int insertClassroom(Classroom classroom);

    /**
     * 修改班级管理
     * 
     * @param classroom 班级管理
     * @return 结果
     */
    public int updateClassroom(Classroom classroom);

    /**
     * 批量删除班级管理
     * 
     * @param classIds 需要删除的班级管理主键集合
     * @return 结果
     */
    public int deleteClassroomByClassIds(String[] classIds);

    /**
     * 删除班级管理信息
     * 
     * @param classId 班级管理主键
     * @return 结果
     */
    public int deleteClassroomByClassId(String classId);
}
