package com.ruoyi.business.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.ScoreMapper;
import com.ruoyi.business.domain.Score;
import com.ruoyi.business.service.IScoreService;

/**
 * 月考成绩Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-10-31
 */
@Service
public class ScoreServiceImpl implements IScoreService 
{
    @Autowired
    private ScoreMapper scoreMapper;

    /**
     * 查询月考成绩
     * 
     * @param scoreId 月考成绩主键
     * @return 月考成绩
     */
    @Override
    public Score selectScoreByScoreId(String scoreId)
    {
        return scoreMapper.selectScoreByScoreId(scoreId);
    }

    /**
     * 查询月考成绩列表
     * 
     * @param score 月考成绩
     * @return 月考成绩
     */
    @Override
    public List<Score> selectScoreList(Score score)
    {
        return scoreMapper.selectScoreList(score);
    }

    /**
     * 新增月考成绩
     * 
     * @param score 月考成绩
     * @return 结果
     */
    @Override
    public int insertScore(Score score)
    {
        score.setCreateTime(DateUtils.getNowDate());
        return scoreMapper.insertScore(score);
    }

    /**
     * 修改月考成绩
     * 
     * @param score 月考成绩
     * @return 结果
     */
    @Override
    public int updateScore(Score score)
    {
        score.setUpdateTime(DateUtils.getNowDate());
        return scoreMapper.updateScore(score);
    }

    /**
     * 批量删除月考成绩
     * 
     * @param scoreIds 需要删除的月考成绩主键
     * @return 结果
     */
    @Override
    public int deleteScoreByScoreIds(String[] scoreIds)
    {
        return scoreMapper.deleteScoreByScoreIds(scoreIds);
    }

    /**
     * 删除月考成绩信息
     * 
     * @param scoreId 月考成绩主键
     * @return 结果
     */
    @Override
    public int deleteScoreByScoreId(String scoreId)
    {
        return scoreMapper.deleteScoreByScoreId(scoreId);
    }
}
