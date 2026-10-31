package com.grade.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：业务异常转成带业务码的 AjaxResult，其它异常统一 500。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<AjaxResult> handleService(ServiceException e) {
        return ResponseEntity.ok(AjaxResult.error(e.getBusinessCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AjaxResult> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .orElse("参数校验失败");
        return ResponseEntity.ok(AjaxResult.error("B0001", msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AjaxResult> handleOther(Exception e) {
        log.error("接口异常", e);
        return ResponseEntity.status(HttpStatus.OK)
                .body(AjaxResult.error(500, "系统繁忙：" + e.getMessage()));
    }
}
