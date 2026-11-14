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
import com.ruoyi.system.domain.Score;
import com.ruoyi.system.service.IScoreService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 成绩管理Controller
 * 
 * @author grade-team
 * @date 2026-10-31
 */
@RestController
@RequestMapping("/system/score")
public class ScoreController extends BaseController
{
    @Autowired
    private IScoreService scoreService;

    /**
     * 查询成绩管理列表
     */
    @PreAuthorize("@ss.hasPermi('system:score:list')")
    @GetMapping("/list")
    public TableDataInfo list(Score score)
    {
        startPage();
        List<Score> list = scoreService.selectScoreList(score);
        return getDataTable(list);
    }

    /**
     * 导出成绩管理列表
     */
    @PreAuthorize("@ss.hasPermi('system:score:export')")
    @Log(title = "成绩管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Score score)
    {
        List<Score> list = scoreService.selectScoreList(score);
        ExcelUtil<Score> util = new ExcelUtil<Score>(Score.class);
        util.exportExcel(response, list, "成绩管理数据");
    }

    /**
     * 获取成绩管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:score:query')")
    @GetMapping(value = "/{scoreId}")
    public AjaxResult getInfo(@PathVariable("scoreId") String scoreId)
    {
        return success(scoreService.selectScoreByScoreId(scoreId));
    }

    /**
     * 新增成绩管理
     */
    @PreAuthorize("@ss.hasPermi('system:score:add')")
    @Log(title = "成绩管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Score score)
    {
        return toAjax(scoreService.insertScore(score));
    }

    /**
     * 修改成绩管理
     */
    @PreAuthorize("@ss.hasPermi('system:score:edit')")
    @Log(title = "成绩管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Score score)
    {
        return toAjax(scoreService.updateScore(score));
    }

    /**
     * 删除成绩管理
     */
    @PreAuthorize("@ss.hasPermi('system:score:remove')")
    @Log(title = "成绩管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{scoreIds}")
    public AjaxResult remove(@PathVariable String[] scoreIds)
    {
        return toAjax(scoreService.deleteScoreByScoreIds(scoreIds));
    }
}
