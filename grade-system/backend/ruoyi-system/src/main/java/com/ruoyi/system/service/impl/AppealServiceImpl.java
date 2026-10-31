package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.AppealMapper;
import com.ruoyi.system.domain.Appeal;
import com.ruoyi.system.service.IAppealService;

/**
 * 查分申请管理Service业务层处理
 * 
 * @author grade-team
 * @date 2026-10-31
 */
@Service
public class AppealServiceImpl implements IAppealService 
{
    @Autowired
    private AppealMapper appealMapper;

    /**
     * 查询查分申请管理
     * 
     * @param appealId 查分申请管理主键
     * @return 查分申请管理
     */
    @Override
    public Appeal selectAppealByAppealId(String appealId)
    {
        return appealMapper.selectAppealByAppealId(appealId);
    }

    /**
     * 查询查分申请管理列表
     * 
     * @param appeal 查分申请管理
     * @return 查分申请管理
     */
    @Override
    public List<Appeal> selectAppealList(Appeal appeal)
    {
        return appealMapper.selectAppealList(appeal);
    }

    /**
     * 新增查分申请管理
     * 
     * @param appeal 查分申请管理
     * @return 结果
     */
    @Override
    public int insertAppeal(Appeal appeal)
    {
        appeal.setCreateTime(DateUtils.getNowDate());
        return appealMapper.insertAppeal(appeal);
    }

    /**
     * 修改查分申请管理
     * 
     * @param appeal 查分申请管理
     * @return 结果
     */
    @Override
    public int updateAppeal(Appeal appeal)
    {
        appeal.setUpdateTime(DateUtils.getNowDate());
        return appealMapper.updateAppeal(appeal);
    }

    /**
     * 批量删除查分申请管理
     * 
     * @param appealIds 需要删除的查分申请管理主键
     * @return 结果
     */
    @Override
    public int deleteAppealByAppealIds(String[] appealIds)
    {
        return appealMapper.deleteAppealByAppealIds(appealIds);
    }

    /**
     * 删除查分申请管理信息
     * 
     * @param appealId 查分申请管理主键
     * @return 结果
     */
    @Override
    public int deleteAppealByAppealId(String appealId)
    {
        return appealMapper.deleteAppealByAppealId(appealId);
    }
}
