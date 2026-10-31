package com.ruoyi.portal.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 门户下拉选项：学期、班级、课程。
 */
public interface PortalOptionMapper
{
    List<String> selectTerms();

    List<Map<String, Object>> selectClasses(@Param("classIds") List<String> classIds);

    List<Map<String, Object>> selectCourses();
}
