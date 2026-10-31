package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.Course;

/**
 * 课程管理Mapper接口
 * 
 * @author grade-team
 * @date 2026-10-31
 */
public interface CourseMapper 
{
    /**
     * 查询课程管理
     * 
     * @param courseId 课程管理主键
     * @return 课程管理
     */
    public Course selectCourseByCourseId(String courseId);

    /**
     * 查询课程管理列表
     * 
     * @param course 课程管理
     * @return 课程管理集合
     */
    public List<Course> selectCourseList(Course course);

    /**
     * 新增课程管理
     * 
     * @param course 课程管理
     * @return 结果
     */
    public int insertCourse(Course course);

    /**
     * 修改课程管理
     * 
     * @param course 课程管理
     * @return 结果
     */
    public int updateCourse(Course course);

    /**
     * 删除课程管理
     * 
     * @param courseId 课程管理主键
     * @return 结果
     */
    public int deleteCourseByCourseId(String courseId);

    /**
     * 批量删除课程管理
     * 
     * @param courseIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCourseByCourseIds(String[] courseIds);
}
