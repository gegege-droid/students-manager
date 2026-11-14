package com.ruoyi.web.controller.portal;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.portal.service.PortalAiService;

/**
 * 用户端 AI 咨询接口（DeepSeek 代理）。
 */
@RestController
@RequestMapping("/portal/ai")
public class PortalAiController
{
    @Autowired
    private PortalAiService aiService;

    /**
     * 当前是否已配置 AI 服务
     */
    @GetMapping("/status")
    public AjaxResult status()
    {
        return AjaxResult.success(aiService.status());
    }

    /**
     * 对话。请求体：{ "messages": [ { "role": "user", "content": "..." } ] }
     */
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody Map<String, Object> body)
    {
        return AjaxResult.success(aiService.chat(body));
    }
}
