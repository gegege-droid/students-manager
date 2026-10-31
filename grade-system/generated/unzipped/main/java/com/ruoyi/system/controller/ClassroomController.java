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
import com.ruoyi.system.domain.Classroom;
import com.ruoyi.system.service.IClassroomService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 班级管理Controller
 * 
 * @author grade-team
 * @date 2026-10-31
 */
@RestController
@RequestMapping("/system/classroom")
public class ClassroomController extends BaseController
{
    @Autowired
    private IClassroomService classroomService;

    /**
     * 查询班级管理列表
     */
    @PreAuthorize("@ss.hasPermi('system:classroom:list')")
    @GetMapping("/list")
    public TableDataInfo list(Classroom classroom)
    {
        startPage();
        List<Classroom> list = classroomService.selectClassroomList(classroom);
        return getDataTable(list);
    }

    /**
     * 导出班级管理列表
     */
    @PreAuthorize("@ss.hasPermi('system:classroom:export')")
    @Log(title = "班级管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Classroom classroom)
    {
        List<Classroom> list = classroomService.selectClassroomList(classroom);
        ExcelUtil<Classroom> util = new ExcelUtil<Classroom>(Classroom.class);
        util.exportExcel(response, list, "班级管理数据");
    }

    /**
     * 获取班级管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:classroom:query')")
    @GetMapping(value = "/{classId}")
    public AjaxResult getInfo(@PathVariable("classId") String classId)
    {
        return success(classroomService.selectClassroomByClassId(classId));
    }

    /**
     * 新增班级管理
     */
    @PreAuthorize("@ss.hasPermi('system:classroom:add')")
    @Log(title = "班级管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Classroom classroom)
    {
        return toAjax(classroomService.insertClassroom(classroom));
    }

    /**
     * 修改班级管理
     */
    @PreAuthorize("@ss.hasPermi('system:classroom:edit')")
    @Log(title = "班级管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Classroom classroom)
    {
        return toAjax(classroomService.updateClassroom(classroom));
    }

    /**
     * 删除班级管理
     */
    @PreAuthorize("@ss.hasPermi('system:classroom:remove')")
    @Log(title = "班级管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{classIds}")
    public AjaxResult remove(@PathVariable String[] classIds)
    {
        return toAjax(classroomService.deleteClassroomByClassIds(classIds));
    }
}
