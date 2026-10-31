package com.ruoyi.system.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.ScoreChangeLog;
import com.ruoyi.system.service.IScoreChangeLogService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 成绩修改记录Controller
 * 
 * @author grade-team
 * @date 2026-10-31
 */
@RestController
@RequestMapping("/system/log")
public class ScoreChangeLogController extends BaseController
{
    @Autowired
    private IScoreChangeLogService scoreChangeLogService;

    /**
     * 查询成绩修改记录列表
     */
    @PreAuthorize("@ss.hasPermi('system:log:list')")
    @GetMapping("/list")
    public TableDataInfo list(ScoreChangeLog scoreChangeLog)
    {
        startPage();
        List<ScoreChangeLog> list = scoreChangeLogService.selectScoreChangeLogList(scoreChangeLog);
        return getDataTable(list);
    }

    /**
     * 导出成绩修改记录列表
     */
    @PreAuthorize("@ss.hasPermi('system:log:export')")
    @Log(title = "成绩修改记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ScoreChangeLog scoreChangeLog)
    {
        List<ScoreChangeLog> list = scoreChangeLogService.selectScoreChangeLogList(scoreChangeLog);
        ExcelUtil<ScoreChangeLog> util = new ExcelUtil<ScoreChangeLog>(ScoreChangeLog.class);
        util.exportExcel(response, list, "成绩修改记录数据");
    }

    /**
     * 获取成绩修改记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:log:query')")
    @GetMapping(value = "/{logId}")
    public AjaxResult getInfo(@PathVariable("logId") String logId)
    {
        return success(scoreChangeLogService.selectScoreChangeLogByLogId(logId));
    }

    /**
     * 新增成绩修改记录
     */
    @PreAuthorize("@ss.hasPermi('system:log:add')")
    @Log(title = "成绩修改记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ScoreChangeLog scoreChangeLog)
    {
        return toAjax(scoreChangeLogService.insertScoreChangeLog(scoreChangeLog));
    }

    /**
     * 修改成绩修改记录
     */
    @PreAuthorize("@ss.hasPermi('system:log:edit')")
    @Log(title = "成绩修改记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ScoreChangeLog scoreChangeLog)
    {
        return toAjax(scoreChangeLogService.updateScoreChangeLog(scoreChangeLog));
    }

    /**
     * 删除成绩修改记录
     */
    @PreAuthorize("@ss.hasPermi('system:log:remove')")
    @Log(title = "成绩修改记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{logIds}")
    public AjaxResult remove(@PathVariable String[] logIds)
    {
        return toAjax(scoreChangeLogService.deleteScoreChangeLogByLogIds(logIds));
    }
}
