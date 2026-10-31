package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.ClassroomMapper;
import com.ruoyi.system.domain.Classroom;
import com.ruoyi.system.service.IClassroomService;

/**
 * 班级管理Service业务层处理
 * 
 * @author grade-team
 * @date 2026-10-31
 */
@Service
public class ClassroomServiceImpl implements IClassroomService 
{
    @Autowired
    private ClassroomMapper classroomMapper;

    /**
     * 查询班级管理
     * 
     * @param classId 班级管理主键
     * @return 班级管理
     */
    @Override
    public Classroom selectClassroomByClassId(String classId)
    {
        return classroomMapper.selectClassroomByClassId(classId);
    }

    /**
     * 查询班级管理列表
     * 
     * @param classroom 班级管理
     * @return 班级管理
     */
    @Override
    public List<Classroom> selectClassroomList(Classroom classroom)
    {
        return classroomMapper.selectClassroomList(classroom);
    }

    /**
     * 新增班级管理
     * 
     * @param classroom 班级管理
     * @return 结果
     */
    @Override
    public int insertClassroom(Classroom classroom)
    {
        classroom.setCreateTime(DateUtils.getNowDate());
        return classroomMapper.insertClassroom(classroom);
    }

    /**
     * 修改班级管理
     * 
     * @param classroom 班级管理
     * @return 结果
     */
    @Override
    public int updateClassroom(Classroom classroom)
    {
        classroom.setUpdateTime(DateUtils.getNowDate());
        return classroomMapper.updateClassroom(classroom);
    }

    /**
     * 批量删除班级管理
     * 
     * @param classIds 需要删除的班级管理主键
     * @return 结果
     */
    @Override
    public int deleteClassroomByClassIds(String[] classIds)
    {
        return classroomMapper.deleteClassroomByClassIds(classIds);
    }

    /**
     * 删除班级管理信息
     * 
     * @param classId 班级管理主键
     * @return 结果
     */
    @Override
    public int deleteClassroomByClassId(String classId)
    {
        return classroomMapper.deleteClassroomByClassId(classId);
    }
}
