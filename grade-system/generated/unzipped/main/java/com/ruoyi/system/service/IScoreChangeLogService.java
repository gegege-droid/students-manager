package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.ScoreChangeLog;

/**
 * 成绩修改记录Service接口
 * 
 * @author grade-team
 * @date 2026-10-31
 */
public interface IScoreChangeLogService 
{
    /**
     * 查询成绩修改记录
     * 
     * @param logId 成绩修改记录主键
     * @return 成绩修改记录
     */
    public ScoreChangeLog selectScoreChangeLogByLogId(String logId);

    /**
     * 查询成绩修改记录列表
     * 
     * @param scoreChangeLog 成绩修改记录
     * @return 成绩修改记录集合
     */
    public List<ScoreChangeLog> selectScoreChangeLogList(ScoreChangeLog scoreChangeLog);

    /**
     * 新增成绩修改记录
     * 
     * @param scoreChangeLog 成绩修改记录
     * @return 结果
     */
    public int insertScoreChangeLog(ScoreChangeLog scoreChangeLog);

    /**
     * 修改成绩修改记录
     * 
     * @param scoreChangeLog 成绩修改记录
     * @return 结果
     */
    public int updateScoreChangeLog(ScoreChangeLog scoreChangeLog);

    /**
     * 批量删除成绩修改记录
     * 
     * @param logIds 需要删除的成绩修改记录主键集合
     * @return 结果
     */
    public int deleteScoreChangeLogByLogIds(String[] logIds);

    /**
     * 删除成绩修改记录信息
     * 
     * @param logId 成绩修改记录主键
     * @return 结果
     */
    public int deleteScoreChangeLogByLogId(String logId);
}
