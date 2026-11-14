package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.Appeal;

/**
 * 查分申请管理Service接口
 * 
 * @author grade-team
 * @date 2026-10-31
 */
public interface IAppealService 
{
    /**
     * 查询查分申请管理
     * 
     * @param appealId 查分申请管理主键
     * @return 查分申请管理
     */
    public Appeal selectAppealByAppealId(String appealId);

    /**
     * 查询查分申请管理列表
     * 
     * @param appeal 查分申请管理
     * @return 查分申请管理集合
     */
    public List<Appeal> selectAppealList(Appeal appeal);

    /**
     * 新增查分申请管理
     * 
     * @param appeal 查分申请管理
     * @return 结果
     */
    public int insertAppeal(Appeal appeal);

    /**
     * 修改查分申请管理
     * 
     * @param appeal 查分申请管理
     * @return 结果
     */
    public int updateAppeal(Appeal appeal);

    /**
     * 批量删除查分申请管理
     * 
     * @param appealIds 需要删除的查分申请管理主键集合
     * @return 结果
     */
    public int deleteAppealByAppealIds(String[] appealIds);

    /**
     * 删除查分申请管理信息
     * 
     * @param appealId 查分申请管理主键
     * @return 结果
     */
    public int deleteAppealByAppealId(String appealId);
}
