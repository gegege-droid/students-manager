package com.grade.controller;

import com.grade.auth.AuthInterceptor;
import com.grade.auth.TokenService;
import com.grade.common.AjaxResult;
import com.grade.common.ServiceException;
import com.grade.service.PredictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预测接口：按学生给出逐科预测、总分与区间、置信度。
 * 对应《概要设计说明书》§6.2 的 23 号接口，算法见《详细设计说明书》§9.7。
 */
@Tag(name = "05 预测", description = "高考成绩预测")
@RestController
@RequestMapping("/business/predict")
public class PredictController {

    private final PredictService predictService;

    public PredictController(PredictService predictService) {
        this.predictService = predictService;
    }

    @Operation(summary = "学生高考成绩预测", description = "加权移动平均 + 趋势修正；参数 gap=2、damping=0.5、recentCount=3")
    @GetMapping("/student/{studentId}")
    public AjaxResult predict(@PathVariable String studentId, HttpServletRequest request) {
        TokenService.Session session = (TokenService.Session) request.getAttribute(AuthInterceptor.ATTR_SESSION);
        if ("STUDENT".equals(session.role()) && !session.loginId().equals(studentId)) {
            throw new ServiceException("B2001", "只能查看本人预测");
        }
        return AjaxResult.success(predictService.predictStudent(studentId));
    }
}
