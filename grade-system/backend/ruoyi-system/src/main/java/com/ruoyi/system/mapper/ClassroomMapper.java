package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.Classroom;

/**
 * 班级管理Mapper接口
 * 
 * @author grade-team
 * @date 2026-10-31
 */
public interface ClassroomMapper 
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
     * 删除班级管理
     * 
     * @param classId 班级管理主键
     * @return 结果
     */
    public int deleteClassroomByClassId(String classId);

    /**
     * 批量删除班级管理
     * 
     * @param classIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteClassroomByClassIds(String[] classIds);
}
