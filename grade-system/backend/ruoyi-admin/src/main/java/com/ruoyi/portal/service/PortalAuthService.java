package com.ruoyi.portal.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.framework.web.service.SysLoginService;
import com.ruoyi.portal.mapper.PortalAuthMapper;

/**
 * 门户身份服务。
 *
 * 学生/教师身份存放在 edu_student / edu_teacher 业务表中，
 * 登录凭证复用 RuoYi 的 sys_user（账号 = 学号/工号）。
 */
@Service
public class PortalAuthService
{
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_TEACHER = "TEACHER";

    @Autowired
    private PortalAuthMapper authMapper;

    @Autowired
    private SysLoginService loginService;

    /**
     * 门户登录：先确认是学生/教师账号，再走 RuoYi 的用户名密码认证并签发 JWT。
     */
    public Map<String, Object> login(String username, String password)
    {
        Map<String, Object> user = loadIdentity(username);
        String token = loginService.login(username, password, null, null);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    /**
     * 当前登录者的门户身份（从 JWT 中取用户名）。
     */
    public Map<String, Object> currentUser()
    {
        return loadIdentity(SecurityUtils.getUsername());
    }

    /**
     * 按登录账号解析学生或教师身份。
     */
    public Map<String, Object> loadIdentity(String loginId)
    {
        Map<String, Object> student = authMapper.selectStudent(loginId);
        if (student != null && !student.isEmpty())
        {
            Map<String, Object> user = new LinkedHashMap<String, Object>();
            user.put("loginId", loginId);
            user.put("role", ROLE_STUDENT);
            user.put("name", student.get("name"));
            user.put("classId", student.get("classId"));
            user.put("className", student.get("className"));
            user.put("manageClassIds", Collections.emptyList());
            return user;
        }

        Map<String, Object> teacher = authMapper.selectTeacher(loginId);
        if (teacher != null && !teacher.isEmpty())
        {
            List<String> classIds = authMapper.selectManagedClassIds(loginId);
            Map<String, Object> user = new LinkedHashMap<String, Object>();
            user.put("loginId", loginId);
            user.put("role", ROLE_TEACHER);
            user.put("name", teacher.get("name"));
            user.put("classId", "");
            user.put("className", "");
            user.put("manageClassIds", classIds == null ? Collections.emptyList() : classIds);
            return user;
        }

        throw new ServiceException("非学生/教师账号，无法登录用户端");
    }

    public boolean isTeacher(Map<String, Object> user)
    {
        return ROLE_TEACHER.equals(user.get("role"));
    }

    @SuppressWarnings("unchecked")
    public List<String> managedClassIds(Map<String, Object> user)
    {
        Object value = user.get("manageClassIds");
        return value == null ? Collections.<String>emptyList() : (List<String>) value;
    }
}
