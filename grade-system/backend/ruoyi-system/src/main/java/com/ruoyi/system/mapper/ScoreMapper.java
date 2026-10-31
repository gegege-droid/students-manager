package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.Score;

/**
 * 成绩管理Mapper接口
 * 
 * @author grade-team
 * @date 2026-10-31
 */
public interface ScoreMapper 
{
    /**
     * 查询成绩管理
     * 
     * @param scoreId 成绩管理主键
     * @return 成绩管理
     */
    public Score selectScoreByScoreId(String scoreId);

    /**
     * 查询成绩管理列表
     * 
     * @param score 成绩管理
     * @return 成绩管理集合
     */
    public List<Score> selectScoreList(Score score);

    /**
     * 新增成绩管理
     * 
     * @param score 成绩管理
     * @return 结果
     */
    public int insertScore(Score score);

    /**
     * 修改成绩管理
     * 
     * @param score 成绩管理
     * @return 结果
     */
    public int updateScore(Score score);

    /**
     * 删除成绩管理
     * 
     * @param scoreId 成绩管理主键
     * @return 结果
     */
    public int deleteScoreByScoreId(String scoreId);

    /**
     * 批量删除成绩管理
     * 
     * @param scoreIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteScoreByScoreIds(String[] scoreIds);
}
