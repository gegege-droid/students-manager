package com.grade.auth;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 令牌与会话管理。
 *
 * <p>设计文档中令牌存 Redis、后端无状态；本实现为演示环境，改为进程内 Map 存储，
 * 语义保持一致（30 分钟滑动过期、退出即失效）。生产环境替换本类即可。</p>
 */
@Component
public class TokenService {

    /** 会话信息：登录账号、角色、姓名、所属班级 */
    public record Session(String loginId, String role, String name, String classId,
                          String className, LocalDateTime expireAt) {
    }

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final long expireMinutes;

    public TokenService(@Value("${grade.token.expire-minutes:30}") long expireMinutes) {
        this.expireMinutes = expireMinutes;
    }

    public String create(String loginId, String role, String name, String classId, String className) {
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, new Session(loginId, role, name, classId, className,
                LocalDateTime.now().plusMinutes(expireMinutes)));
        return token;
    }

    /** 取会话并顺延过期时间；过期则清除并返回 null */
    public Session get(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        Session s = sessions.get(token);
        if (s == null) {
            return null;
        }
        if (s.expireAt().isBefore(LocalDateTime.now())) {
            sessions.remove(token);
            return null;
        }
        Session renewed = new Session(s.loginId(), s.role(), s.name(), s.classId(), s.className(),
                LocalDateTime.now().plusMinutes(expireMinutes));
        sessions.put(token, renewed);
        return renewed;
    }

    public void remove(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }
}
