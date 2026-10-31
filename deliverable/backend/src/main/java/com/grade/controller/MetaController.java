package com.grade.controller;

import com.grade.auth.AuthInterceptor;
import com.grade.auth.TokenService;
import com.grade.common.AjaxResult;
import com.grade.service.MetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 基础数据接口：场次、科目、班级、学生档案。
 * 对应《概要设计说明书》§6.2 的 5~8 号接口。
 */
@Tag(name = "02 基础数据", description = "场次 / 科目 / 班级 / 学生档案")
@RestController
@RequestMapping("/business")
public class MetaController {

    private final MetaService meta;

    public MetaController(MetaService meta) {
        this.meta = meta;
    }

    @Operation(summary = "月考场次列表", description = "scoreStatus=PUBLISHED 表示已出成绩；可选按状态过滤")
    @GetMapping("/exam/list")
    public AjaxResult examList(@RequestParam(required = false) String scoreStatus) {
        List<Map<String, Object>> list = meta.exams();
        if (scoreStatus != null && !scoreStatus.isBlank()) {
            list = list.stream().filter(e -> scoreStatus.equals(e.get("scoreStatus"))).toList();
        }
        return AjaxResult.success(list);
    }

    @Operation(summary = "科目字典", description = "9 门科目及满分、类别（必考 / 首选 / 再选）")
    @GetMapping("/subject/list")
    public AjaxResult subjectList(@RequestParam(required = false) String category) {
        List<Map<String, Object>> list = meta.subjects();
        if (category != null && !category.isBlank()) {
            list = list.stream().filter(s -> category.equals(s.get("category"))).toList();
        }
        return AjaxResult.success(list);
    }

    @Operation(summary = "班级列表")
    @GetMapping("/class/list")
    public AjaxResult classList() {
        return AjaxResult.success(meta.classes());
    }

    @Operation(summary = "学生档案", description = "含选科组合与该生实际参加的 6 门科目；学生只能查自己")
    @GetMapping("/student/profile/{studentId}")
    public AjaxResult profile(@PathVariable String studentId, HttpServletRequest request) {
        TokenService.Session s = (TokenService.Session) request.getAttribute(AuthInterceptor.ATTR_SESSION);
        if ("STUDENT".equals(s.role()) && !s.loginId().equals(studentId)) {
            return AjaxResult.error("B2001", "只能查看本人档案");
        }
        Map<String, Object> data = meta.studentProfile(studentId);
        if (data == null) {
            return AjaxResult.error("B2001", "学生不存在或无数据权限");
        }
        return AjaxResult.success(data);
    }
}
