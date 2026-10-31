package com.grade.controller;

import com.grade.common.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仅演示环境使用：把演示账号的密码统一重置为 123456 的 BCrypt 密文。
 *
 * <p>生产环境必须删除本控制器（或加权限限制），否则任何人都能重置密码。</p>
 */
@Tag(name = "99 演示工具（仅演示环境）", description = "重置演示账号密码")
@RestController
public class DemoController {

    private final JdbcTemplate jdbc;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DemoController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Operation(summary = "重置演示账号密码", description = "把所有 sys_user 的密码重置为 123456 的 BCrypt 密文，返回更新条数")
    @PostMapping("/demo/resetPassword")
    public AjaxResult resetPassword() {
        String hash = encoder.encode("123456");
        int updated = jdbc.update("UPDATE sys_user SET password = ?", hash);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("updated", updated);
        data.put("password", "123456");
        return AjaxResult.success("已重置为 123456", data);
    }
}
