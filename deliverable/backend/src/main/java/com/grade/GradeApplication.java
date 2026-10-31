package com.grade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 高中成绩管理系统后端启动类。
 *
 * <p>对应《概要设计说明书》§3.2 的后端三层结构：Controller（表现层）→ Service（业务层）
 * → JdbcTemplate（持久层，替代 MyBatis-Plus 以保持零 XML 依赖）。
 * 接口契约、错误码与《详细设计说明书》§6、§11 一致。</p>
 */
@SpringBootApplication
public class GradeApplication {

    public static void main(String[] args) {
        SpringApplication.run(GradeApplication.class, args);
    }
}
