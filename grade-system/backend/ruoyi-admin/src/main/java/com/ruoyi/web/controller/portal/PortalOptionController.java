package com.ruoyi.web.controller.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.portal.service.PortalOptionService;

/**
 * 用户端下拉选项接口。
 */
@RestController
@RequestMapping("/portal")
public class PortalOptionController
{
    @Autowired
    private PortalOptionService optionService;

    @GetMapping("/options")
    public AjaxResult options()
    {
        return AjaxResult.success(optionService.options());
    }
}
