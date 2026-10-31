package com.ruoyi.portal.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 门户查分申请。
 */
public interface PortalAppealMapper
{
    List<Map<String, Object>> selectAppealViews(Map<String, Object> params);

    Map<String, Object> selectAppealViewById(@Param("appealId") String appealId);

    Map<String, Object> selectAppealForUpdate(@Param("appealId") String appealId);

    String selectMaxAppealId(@Param("prefix") String prefix);

    int countPending(@Param("studentId") String studentId,
            @Param("courseId") String courseId, @Param("term") String term);

    int insertAppeal(Map<String, Object> params);

    int updateHandle(Map<String, Object> params);

    int updateReject(Map<String, Object> params);
}
