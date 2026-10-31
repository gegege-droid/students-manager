package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.ScoreChangeLogMapper;
import com.ruoyi.system.domain.ScoreChangeLog;
import com.ruoyi.system.service.IScoreChangeLogService;

/**
 * 成绩修改记录Service业务层处理
 * 
 * @author grade-team
 * @date 2026-10-31
 */
@Service
public class ScoreChangeLogServiceImpl implements IScoreChangeLogService 
{
    @Autowired
    private ScoreChangeLogMapper scoreChangeLogMapper;

    /**
     * 查询成绩修改记录
     * 
     * @param logId 成绩修改记录主键
     * @return 成绩修改记录
     */
    @Override
    public ScoreChangeLog selectScoreChangeLogByLogId(String logId)
    {
        return scoreChangeLogMapper.selectScoreChangeLogByLogId(logId);
    }

    /**
     * 查询成绩修改记录列表
     * 
     * @param scoreChangeLog 成绩修改记录
     * @return 成绩修改记录
     */
    @Override
    public List<ScoreChangeLog> selectScoreChangeLogList(ScoreChangeLog scoreChangeLog)
    {
        return scoreChangeLogMapper.selectScoreChangeLogList(scoreChangeLog);
    }

    /**
     * 新增成绩修改记录
     * 
     * @param scoreChangeLog 成绩修改记录
     * @return 结果
     */
    @Override
    public int insertScoreChangeLog(ScoreChangeLog scoreChangeLog)
    {
        scoreChangeLog.setCreateTime(DateUtils.getNowDate());
        return scoreChangeLogMapper.insertScoreChangeLog(scoreChangeLog);
    }

    /**
     * 修改成绩修改记录
     * 
     * @param scoreChangeLog 成绩修改记录
     * @return 结果
     */
    @Override
    public int updateScoreChangeLog(ScoreChangeLog scoreChangeLog)
    {
        scoreChangeLog.setUpdateTime(DateUtils.getNowDate());
        return scoreChangeLogMapper.updateScoreChangeLog(scoreChangeLog);
    }

    /**
     * 批量删除成绩修改记录
     * 
     * @param logIds 需要删除的成绩修改记录主键
     * @return 结果
     */
    @Override
    public int deleteScoreChangeLogByLogIds(String[] logIds)
    {
        return scoreChangeLogMapper.deleteScoreChangeLogByLogIds(logIds);
    }

    /**
     * 删除成绩修改记录信息
     * 
     * @param logId 成绩修改记录主键
     * @return 结果
     */
    @Override
    public int deleteScoreChangeLogByLogId(String logId)
    {
        return scoreChangeLogMapper.deleteScoreChangeLogByLogId(logId);
    }
}
