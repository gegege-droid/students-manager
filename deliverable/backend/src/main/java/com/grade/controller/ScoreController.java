package com.grade.controller;

import com.grade.auth.AuthInterceptor;
import com.grade.auth.TokenService;
import com.grade.common.AjaxResult;
import com.grade.common.ServiceException;
import com.grade.service.ScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 成绩接口：矩阵、分页列表、录入名单、批量录入、修改、修改记录、总分排名。
 * 对应《概要设计说明书》§6.2 的 9~16 号接口。
 */
@Tag(name = "03 成绩", description = "成绩单矩阵 / 录入 / 修改 / 留痕 / 排名")
@RestController
@RequestMapping("/business/score")
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @Operation(summary = "学生成绩单矩阵", description = "行 = 该生 6 门科目，列 = 已出成绩的场次；含总分、班级排名、年级排名")
    @GetMapping("/matrix")
    public AjaxResult matrix(@RequestParam String studentId,
                             @RequestParam(required = false) String examId,
                             @RequestParam(required = false) String subjectId,
                             HttpServletRequest request) {
        TokenService.Session session = current(request);
        if ("STUDENT".equals(session.role()) && !session.loginId().equals(studentId)) {
            throw new ServiceException("B2001", "只能查看本人成绩");
        }
        return AjaxResult.success(scoreService.matrix(studentId, examId, subjectId));
    }

    @Operation(summary = "成绩分页列表", description = "支持关键字（学号/姓名）、场次、科目、班级过滤")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) String keyword,
                           @RequestParam(required = false) String examId,
                           @RequestParam(required = false) String subjectId,
                           @RequestParam(required = false) String classId) {
        return AjaxResult.success(scoreService.list(keyword, examId, subjectId, classId));
    }

    @Operation(summary = "录入名单", description = "只包含选考该科目的学生，已录入的回填分数")
    @GetMapping("/roster")
    public AjaxResult roster(@RequestParam String classId,
                            @RequestParam String examId,
                            @RequestParam String subjectId) {
        return AjaxResult.success(scoreService.roster(classId, examId, subjectId));
    }

    @Operation(summary = "批量录入成绩", description = "按唯一键 upsert，任一行越界则整批回滚（B3001）")
    @PostMapping("/batch")
    public AjaxResult batch(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        TokenService.Session session = requireTeacher(request);
        String examId = String.valueOf(body.get("examId"));
        String subjectId = String.valueOf(body.get("subjectId"));
        Object rawRows = body.get("rows");
        if (!(rawRows instanceof List<?> list) || list.isEmpty()) {
            throw new ServiceException("B0006", "rows 不能为空");
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof Map<?, ?> m) {
                @SuppressWarnings("unchecked")
                Map<String, Object> row = (Map<String, Object>) m;
                rows.add(row);
            }
        }
        return AjaxResult.success("录入成功",
                scoreService.batch(examId, subjectId, rows, session.loginId()));
    }

    @Operation(summary = "修改成绩", description = "查分中的成绩锁定（B3004）；修改后写一条 MANUAL 修改记录")
    @PutMapping
    public AjaxResult update(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        TokenService.Session session = requireTeacher(request);
        String studentId = String.valueOf(body.get("studentId"));
        String examId = String.valueOf(body.get("examId"));
        String subjectId = String.valueOf(body.get("subjectId"));
        Object raw = body.get("newScore");
        if (raw == null) {
            throw new ServiceException("B0007", "newScore 不能为空");
        }
        int newScore = ((Number) raw).intValue();
        String reason = body.get("reason") == null ? "" : String.valueOf(body.get("reason"));
        return AjaxResult.success("修改成功",
                scoreService.update(studentId, examId, subjectId, newScore, reason, session.loginId()));
    }

    @Operation(summary = "修改记录查询", description = "按学号 + 场次 + 科目查询历史修改记录，时间倒序")
    @GetMapping("/log/list")
    public AjaxResult logs(@RequestParam String studentId,
                           @RequestParam(required = false) String examId,
                           @RequestParam(required = false) String subjectId) {
        return AjaxResult.success(scoreService.logs(studentId, examId, subjectId));
    }

    @Operation(summary = "月考总分与排名", description = "读取 biz_score_total 的核算结果")
    @GetMapping("/total/list")
    public AjaxResult totals(@RequestParam(required = false) String studentId,
                             @RequestParam(required = false) String examId) {
        return AjaxResult.success(scoreService.totals(studentId, examId));
    }

    private TokenService.Session current(HttpServletRequest request) {
        return (TokenService.Session) request.getAttribute(AuthInterceptor.ATTR_SESSION);
    }

    private TokenService.Session requireTeacher(HttpServletRequest request) {
        TokenService.Session s = current(request);
        if (!"TEACHER".equals(s.role())) {
            throw new ServiceException("B2002", "只有班主任可以执行该操作");
        }
        return s;
    }
}
