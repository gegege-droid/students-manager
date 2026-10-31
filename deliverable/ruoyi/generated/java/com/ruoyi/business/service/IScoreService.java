package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.Score;

/**
 * 月考成绩Service接口
 * 
 * @author ruoyi
 * @date 2026-10-31
 */
public interface IScoreService 
{
    /**
     * 查询月考成绩
     * 
     * @param scoreId 月考成绩主键
     * @return 月考成绩
     */
    public Score selectScoreByScoreId(String scoreId);

    /**
     * 查询月考成绩列表
     * 
     * @param score 月考成绩
     * @return 月考成绩集合
     */
    public List<Score> selectScoreList(Score score);

    /**
     * 新增月考成绩
     * 
     * @param score 月考成绩
     * @return 结果
     */
    public int insertScore(Score score);

    /**
     * 修改月考成绩
     * 
     * @param score 月考成绩
     * @return 结果
     */
    public int updateScore(Score score);

    /**
     * 批量删除月考成绩
     * 
     * @param scoreIds 需要删除的月考成绩主键集合
     * @return 结果
     */
    public int deleteScoreByScoreIds(String[] scoreIds);

    /**
     * 删除月考成绩信息
     * 
     * @param scoreId 月考成绩主键
     * @return 结果
     */
    public int deleteScoreByScoreId(String scoreId);
}
