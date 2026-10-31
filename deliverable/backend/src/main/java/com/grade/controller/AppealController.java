package com.grade.controller;

import com.grade.auth.AuthInterceptor;
import com.grade.auth.TokenService;
import com.grade.common.AjaxResult;
import com.grade.common.ServiceException;
import com.grade.service.AppealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 查分接口：列表、我的申请、详情、提交、受理、驳回。
 * 对应《概要设计说明书》§6.2 的 17~22 号接口。
 */
@Tag(name = "04 查分", description = "申请查分 / 受理 / 驳回")
@RestController
@RequestMapping("/business/appeal")
public class AppealController {

    private final AppealService appealService;

    public AppealController(AppealService appealService) {
        this.appealService = appealService;
    }

    @Operation(summary = "申请分页列表", description = "status=PENDING 待受理 / HANDLED 已处理；支持关键字、场次、科目、天数过滤")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) String status,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(required = false) String examId,
                           @RequestParam(required = false) String subjectId,
                           @RequestParam(required = false) Integer days,
                           HttpServletRequest request) {
        requireTeacher(request);
        return AjaxResult.success(appealService.list(status, keyword, examId, subjectId, days));
    }

    @Operation(summary = "我的申请", description = "学生查看自己提交的查分申请与处理结果")
    @GetMapping("/mine")
    public AjaxResult mine(HttpServletRequest request) {
        return AjaxResult.success(appealService.mine(current(request).loginId()));
    }

    @Operation(summary = "申请详情")
    @GetMapping("/{appealId}")
    public AjaxResult detail(@PathVariable String appealId) {
        return AjaxResult.success(appealService.detail(appealId));
    }

    @Operation(summary = "提交查分申请", description = "理由 10～200 字；同场次同科目只允许一条待受理申请（B4002）")
    @PostMapping
    public AjaxResult create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        TokenService.Session session = current(request);
        String examId = String.valueOf(body.get("examId"));
        String subjectId = String.valueOf(body.get("subjectId"));
        String reason = body.get("reason") == null ? "" : String.valueOf(body.get("reason"));
        return AjaxResult.success("提交成功",
                appealService.create(session.loginId(), examId, subjectId, reason));
    }

    @Operation(summary = "受理查分申请", description = "result=KEEP 维持 / CORRECT 更正；更正时同步改分并写 APPEAL 记录")
    @PutMapping("/handle")
    public AjaxResult handle(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        TokenService.Session session = requireTeacher(request);
        String appealId = String.valueOf(body.get("appealId"));
        String result = body.get("result") == null ? "KEEP" : String.valueOf(body.get("result"));
        Object raw = body.get("correctedScore");
        Integer correctedScore = raw == null ? null : ((Number) raw).intValue();
        String opinion = body.get("opinion") == null ? "" : String.valueOf(body.get("opinion"));
        return AjaxResult.success("受理完成",
                appealService.handle(appealId, result, correctedScore, opinion, session.loginId()));
    }

    @Operation(summary = "驳回查分申请", description = "理由 5～200 字；不修改成绩")
    @PutMapping("/reject")
    public AjaxResult reject(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        TokenService.Session session = requireTeacher(request);
        String appealId = String.valueOf(body.get("appealId"));
        String opinion = body.get("opinion") == null ? "" : String.valueOf(body.get("opinion"));
        return AjaxResult.success("已驳回", appealService.reject(appealId, opinion, session.loginId()));
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
