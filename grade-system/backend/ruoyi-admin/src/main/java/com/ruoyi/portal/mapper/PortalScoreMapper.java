package com.ruoyi.portal.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 门户成绩查询与维护。
 */
public interface PortalScoreMapper
{
    List<Map<String, Object>> selectScoreViews(Map<String, Object> params);

    Map<String, Object> selectScoreViewById(@Param("scoreId") String scoreId);

    Map<String, Object> selectScoreByKey(@Param("studentId") String studentId,
            @Param("courseId") String courseId, @Param("term") String term);

    Map<String, Object> selectScoreByKeyForUpdate(@Param("studentId") String studentId,
            @Param("courseId") String courseId, @Param("term") String term);

    Map<String, Object> selectScoreByStudentCourse(@Param("studentId") String studentId,
            @Param("courseId") String courseId);

    String selectStudentClass(@Param("studentId") String studentId);

    List<Map<String, Object>> selectRoster(Map<String, Object> params);

    List<Map<String, Object>> selectLogs(@Param("scoreId") String scoreId);

    int insertScore(Map<String, Object> params);

    int updateScore(Map<String, Object> params);

    int updateTotal(Map<String, Object> params);

    int insertLog(Map<String, Object> params);

    int countPendingAppeal(Map<String, Object> params);
}
