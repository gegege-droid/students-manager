package com.ruoyi.portal.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 门户身份查询：用登录账号（学号/工号）关联学生、教师与所带班级。
 */
public interface PortalAuthMapper
{
    Map<String, Object> selectStudent(@Param("loginId") String loginId);

    Map<String, Object> selectTeacher(@Param("loginId") String loginId);

    List<String> selectManagedClassIds(@Param("teacherId") String teacherId);
}
