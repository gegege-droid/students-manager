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
import com.ruoyi.system.domain.Appeal;
import com.ruoyi.system.service.IAppealService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 查分申请管理Controller
 * 
 * @author grade-team
 * @date 2026-10-31
 */
@RestController
@RequestMapping("/system/appeal")
public class AppealController extends BaseController
{
    @Autowired
    private IAppealService appealService;

    /**
     * 查询查分申请管理列表
     */
    @PreAuthorize("@ss.hasPermi('system:appeal:list')")
    @GetMapping("/list")
    public TableDataInfo list(Appeal appeal)
    {
        startPage();
        List<Appeal> list = appealService.selectAppealList(appeal);
        return getDataTable(list);
    }

    /**
     * 导出查分申请管理列表
     */
    @PreAuthorize("@ss.hasPermi('system:appeal:export')")
    @Log(title = "查分申请管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Appeal appeal)
    {
        List<Appeal> list = appealService.selectAppealList(appeal);
        ExcelUtil<Appeal> util = new ExcelUtil<Appeal>(Appeal.class);
        util.exportExcel(response, list, "查分申请管理数据");
    }

    /**
     * 获取查分申请管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:appeal:query')")
    @GetMapping(value = "/{appealId}")
    public AjaxResult getInfo(@PathVariable("appealId") String appealId)
    {
        return success(appealService.selectAppealByAppealId(appealId));
    }

    /**
     * 新增查分申请管理
     */
    @PreAuthorize("@ss.hasPermi('system:appeal:add')")
    @Log(title = "查分申请管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Appeal appeal)
    {
        return toAjax(appealService.insertAppeal(appeal));
    }

    /**
     * 修改查分申请管理
     */
    @PreAuthorize("@ss.hasPermi('system:appeal:edit')")
    @Log(title = "查分申请管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Appeal appeal)
    {
        return toAjax(appealService.updateAppeal(appeal));
    }

    /**
     * 删除查分申请管理
     */
    @PreAuthorize("@ss.hasPermi('system:appeal:remove')")
    @Log(title = "查分申请管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{appealIds}")
    public AjaxResult remove(@PathVariable String[] appealIds)
    {
        return toAjax(appealService.deleteAppealByAppealIds(appealIds));
    }
}
