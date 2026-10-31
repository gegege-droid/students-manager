package com.ruoyi.web.controller.portal;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.portal.service.PortalAppealService;

/**
 * 用户端查分申请接口。
 */
@RestController
@RequestMapping("/portal/appeals")
public class PortalAppealController
{
    @Autowired
    private PortalAppealService appealService;

    @PostMapping
    public AjaxResult submit(@RequestBody Map<String, Object> body)
    {
        return AjaxResult.success(appealService.submit(body));
    }

    @GetMapping("/mine")
    public AjaxResult mine()
    {
        return AjaxResult.success(appealService.mine());
    }

    @GetMapping
    public AjaxResult list(@RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "courseId", required = false) String courseId)
    {
        return AjaxResult.success(appealService.list(status, keyword, courseId));
    }

    @PutMapping("/{appealId}/handle")
    public AjaxResult handle(@PathVariable("appealId") String appealId,
            @RequestBody Map<String, Object> body)
    {
        return AjaxResult.success(appealService.handle(appealId, body));
    }

    @PutMapping("/{appealId}/reject")
    public AjaxResult reject(@PathVariable("appealId") String appealId,
            @RequestBody Map<String, Object> body)
    {
        return AjaxResult.success(appealService.reject(appealId, body));
    }
}
