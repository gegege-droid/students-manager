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
import com.ruoyi.portal.service.PortalScoreService;

/**
 * 用户端成绩接口。
 */
@RestController
@RequestMapping("/portal/scores")
public class PortalScoreController
{
    @Autowired
    private PortalScoreService scoreService;

    @GetMapping
    public AjaxResult list(@RequestParam(value = "term", required = false) String term,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "classId", required = false) String classId,
            @RequestParam(value = "courseId", required = false) String courseId)
    {
        return AjaxResult.success(scoreService.list(term, keyword, classId, courseId));
    }

    @GetMapping("/roster")
    public AjaxResult roster(@RequestParam("classId") String classId,
            @RequestParam("courseId") String courseId,
            @RequestParam("term") String term)
    {
        return AjaxResult.success(scoreService.roster(classId, courseId, term));
    }

    @GetMapping("/{scoreId}/logs")
    public AjaxResult logs(@PathVariable("scoreId") String scoreId)
    {
        return AjaxResult.success(scoreService.logs(scoreId));
    }

    @PostMapping("/batch")
    public AjaxResult saveBatch(@RequestBody Map<String, Object> body)
    {
        return AjaxResult.success(scoreService.saveBatch(body));
    }

    @PutMapping("/{scoreId}")
    public AjaxResult update(@PathVariable("scoreId") String scoreId,
            @RequestBody Map<String, Object> body)
    {
        return AjaxResult.success(scoreService.updateScore(scoreId, body));
    }
}
