package com.ruoyi.web.controller.portal;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.portal.service.PortalAuthService;

/**
 * 用户端登录与身份接口。
 */
@RestController
@RequestMapping("/portal/auth")
public class PortalAuthController
{
    @Autowired
    private PortalAuthService authService;

    @Anonymous
    @PostMapping("/login")
    public AjaxResult login(@RequestBody Map<String, Object> body)
    {
        String username = body.get("username") == null ? "" : String.valueOf(body.get("username"));
        String password = body.get("password") == null ? "" : String.valueOf(body.get("password"));
        return AjaxResult.success(authService.login(username, password));
    }

    @GetMapping("/profile")
    public AjaxResult profile()
    {
        return AjaxResult.success(authService.currentUser());
    }
}
