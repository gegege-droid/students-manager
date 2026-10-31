package com.grade.controller;

import com.grade.auth.AuthInterceptor;
import com.grade.auth.TokenService;
import com.grade.common.AjaxResult;
import com.grade.common.ServiceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：登录、当前用户、验证码、退出。
 * 对应《概要设计说明书》§6.2 的 1~4 号接口。
 */
@Tag(name = "01 认证", description = "登录 / 当前用户 / 验证码 / 退出")
@RestController
public class AuthController {

    private final JdbcTemplate jdbc;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /** 演示用验证码：uuid → code，真实环境应存 Redis 并渲染图片 */
    private final Map<String, String> captchaStore = new ConcurrentHashMap<>();

    public AuthController(JdbcTemplate jdbc, TokenService tokenService) {
        this.jdbc = jdbc;
        this.tokenService = tokenService;
    }

    @Operation(summary = "获取图形验证码", description = "演示环境返回固定为 4 位字符的验证码（明文返回便于测试）")
    @GetMapping("/captchaImage")
    public AjaxResult captcha() {
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 4; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        captchaStore.put(uuid, code.toString());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("uuid", uuid);
        data.put("code", code.toString());
        data.put("expireIn", 120);
        return AjaxResult.success(data);
    }

    @Operation(summary = "登录", description = "校验账号密码，返回令牌；演示环境不强制校验验证码")
    @PostMapping("/login")
    public AjaxResult login(@RequestBody Map<String, Object> body) {
        String loginId = str(body.get("loginId"));
        String password = str(body.get("password"));

        Map<String, Object> user;
        try {
            user = jdbc.queryForMap("""
                    SELECT user_id, login_id, user_name, password, user_type, class_id, status
                    FROM sys_user WHERE login_id = ? AND del_flag = '0'
                    """, loginId);
        } catch (Exception e) {
            throw new ServiceException("B1001", "学号/工号或密码错误");
        }
        if (!"0".equals(str(user.get("status")))) {
            throw new ServiceException("B1003", "账号已被停用，请联系班主任");
        }
        String stored = str(user.get("password"));
        if (!matches(password, stored)) {
            throw new ServiceException("B1001", "学号/工号或密码错误");
        }

        String classId = str(user.get("class_id"));
        String className = "";
        if (!classId.isEmpty()) {
            try {
                className = str(jdbc.queryForObject(
                        "SELECT class_name FROM sys_class WHERE class_id = ?", String.class, classId));
            } catch (Exception ignore) {
                className = "";
            }
        }
        String token = tokenService.create(loginId, str(user.get("user_type")),
                str(user.get("user_name")), classId, className);
        jdbc.update("UPDATE sys_user SET login_date = NOW() WHERE login_id = ?", loginId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("loginId", loginId);
        data.put("role", str(user.get("user_type")));
        data.put("name", str(user.get("user_name")));
        data.put("classId", classId);
        data.put("className", className);
        return AjaxResult.success("登录成功", data);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/getInfo")
    public AjaxResult getInfo(HttpServletRequest request) {
        TokenService.Session s = (TokenService.Session) request.getAttribute(AuthInterceptor.ATTR_SESSION);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("loginId", s.loginId());
        data.put("role", s.role());
        data.put("name", s.name());
        data.put("classId", s.classId());
        data.put("className", s.className());
        return AjaxResult.success(data);
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public AjaxResult logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            tokenService.remove(header.substring(7).trim());
        }
        return AjaxResult.success("已退出登录", null);
    }

    /**
     * 密码比对：兼容 BCrypt 密文与明文（演示库中密码为 BCrypt 密文）。
     */
    private boolean matches(String raw, String stored) {
        if (raw == null) {
            return false;
        }
        if (stored != null && stored.startsWith("$2")) {
            return encoder.matches(raw, stored);
        }
        return raw.equals(stored);
    }

    private String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}
