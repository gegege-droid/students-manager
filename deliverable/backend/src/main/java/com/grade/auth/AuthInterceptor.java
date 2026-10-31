package com.grade.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：校验 Authorization: Bearer &lt;token&gt;，
 * 通过后把会话放入 request attribute，供 Controller 取当前用户。
 * 等价于若依的 JwtAuthenticationFilter + @PreAuthorize 的角色校验。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_SESSION = "session";

    private final TokenService tokenService;

    public AuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String header = request.getHeader("Authorization");
        String token = header != null && header.startsWith("Bearer ") ? header.substring(7).trim() : null;
        TokenService.Session session = tokenService.get(token);
        if (session == null) {
            writeUnauthorized(response);
            return false;
        }
        request.setAttribute(ATTR_SESSION, session);
        return true;
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getOutputStream().write(
                "{\"code\":401,\"msg\":\"未登录或令牌已过期，请重新登录\",\"data\":null}"
                        .getBytes(StandardCharsets.UTF_8));
    }
}
