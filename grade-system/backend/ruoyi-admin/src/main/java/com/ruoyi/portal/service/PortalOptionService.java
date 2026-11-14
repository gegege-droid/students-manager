package com.ruoyi.portal.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.portal.mapper.PortalOptionMapper;

/**
 * 门户下拉选项服务。
 */
@Service
public class PortalOptionService
{
    @Autowired
    private PortalAuthService authService;

    @Autowired
    private PortalOptionMapper optionMapper;

    public Map<String, Object> options()
    {
        Map<String, Object> user = authService.currentUser();
        List<String> classIds = new ArrayList<String>();
        if (PortalAuthService.ROLE_STUDENT.equals(user.get("role")))
        {
            Object classId = user.get("classId");
            if (classId != null && !"".equals(classId))
            {
                classIds.add(String.valueOf(classId));
            }
        }
        else
        {
            classIds.addAll(authService.managedClassIds(user));
        }

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("terms", optionMapper.selectTerms());
        result.put("classes", optionMapper.selectClasses(classIds));
        result.put("courses", optionMapper.selectCourses());
        return result;
    }
}
