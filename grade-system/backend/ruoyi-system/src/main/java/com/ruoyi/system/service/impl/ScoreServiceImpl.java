package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.GradeCalc;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.ScoreMapper;
import com.ruoyi.system.domain.Score;
import com.ruoyi.system.service.IScoreService;

/**
 * 成绩管理Service业务层处理
 * 
 * @author grade-team
 * @date 2026-10-31
 */
@Service
public class ScoreServiceImpl implements IScoreService 
{
    @Autowired
    private ScoreMapper scoreMapper;

    /**
     * 查询成绩管理
     * 
     * @param scoreId 成绩管理主键
     * @return 成绩管理
     */
    @Override
    public Score selectScoreByScoreId(String scoreId)
    {
        return scoreMapper.selectScoreByScoreId(scoreId);
    }

    /**
     * 查询成绩管理列表
     * 
     * @param score 成绩管理
     * @return 成绩管理
     */
    @Override
    public List<Score> selectScoreList(Score score)
    {
        return scoreMapper.selectScoreList(score);
    }

    /**
     * 新增成绩管理
     * 
     * @param score 成绩管理
     * @return 结果
     */
    @Override
    public int insertScore(Score score)
    {
        // 代码生成后的二次开发：主键、总评/绩点、录入人由后端统一生成
        if (StringUtils.isEmpty(score.getScoreId()))
        {
            score.setScoreId("SC" + System.currentTimeMillis());
        }
        Integer total = GradeCalc.total(score.getUsualScore(), score.getFinalScore());
        score.setTotalScore(total);
        score.setGpa(GradeCalc.gpa(total));
        score.setManualTotal("0");
        score.setRecorderId(SecurityUtils.getUsername());
        score.setRecordTime(DateUtils.getNowDate());
        score.setCreateTime(DateUtils.getNowDate());
        return scoreMapper.insertScore(score);
    }

    /**
     * 修改成绩管理
     *
     * @param score 成绩管理
     * @return 结果
     */
    @Override
    public int updateScore(Score score)
    {
        // 二次开发：总评与绩点始终按公式重算，避免管理员手工填写不一致
        Integer total = GradeCalc.total(score.getUsualScore(), score.getFinalScore());
        score.setTotalScore(total);
        score.setGpa(GradeCalc.gpa(total));
        score.setManualTotal("0");
        score.setUpdateTime(DateUtils.getNowDate());
        return scoreMapper.updateScore(score);
    }

    /**
     * 批量删除成绩管理
     * 
     * @param scoreIds 需要删除的成绩管理主键
     * @return 结果
     */
    @Override
    public int deleteScoreByScoreIds(String[] scoreIds)
    {
        return scoreMapper.deleteScoreByScoreIds(scoreIds);
    }

    /**
     * 删除成绩管理信息
     * 
     * @param scoreId 成绩管理主键
     * @return 结果
     */
    @Override
    public int deleteScoreByScoreId(String scoreId)
    {
        return scoreMapper.deleteScoreByScoreId(scoreId);
    }
}
