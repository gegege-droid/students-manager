package com.grade.config;

import com.grade.auth.AuthInterceptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置：跨域、登录拦截、Swagger 文档信息。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/captchaImage",
                        "/demo/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/webjars/**",
                        "/error");
    }

    @Bean
    public OpenAPI gradeOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("高中成绩管理系统 接口文档")
                .version("1.0.0")
                .description("""
                        新高考「3+1+2」高中成绩管理系统后端接口。
                        与《概要设计说明书》第 6 章接口清单一一对应；返回体统一为 { code, msg, data }。
                        调用顺序：先 POST /login 拿 token，再在右上角 Authorize 里填 token（不用加 Bearer 前缀之外的引号）。
                        演示账号：学生 20230101 / 123456，班主任 T001 / 123456。
                        """));
    }
}
