package com.ruoyi.portal.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.GradeCalc;
import com.ruoyi.portal.mapper.PortalAppealMapper;
import com.ruoyi.portal.mapper.PortalScoreMapper;

/**
 * 门户查分申请服务。
 *
 * 状态机：PENDING -> ACCEPTED(KEEP/CORRECT) / REJECTED；
 * CORRECT 时在同一事务内更新申请、成绩与修改记录。
 */
@Service
public class PortalAppealService
{
    private static final AtomicInteger LOG_SEQ = new AtomicInteger();

    @Autowired
    private PortalAuthService authService;

    @Autowired
    private PortalAppealMapper appealMapper;

    @Autowired
    private PortalScoreMapper scoreMapper;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submit(Map<String, Object> body)
    {
        Map<String, Object> user = authService.currentUser();
        assertStudent(user);
        String studentId = String.valueOf(user.get("loginId"));
        String courseId = str(body.get("courseId"));
        String reason = str(body.get("reason"));
        if (isEmpty(courseId))
        {
            throw new ServiceException("请选择申请查分的课程");
        }
        if (reason.length() < 10 || reason.length() > 200)
        {
            throw new ServiceException("请填写 10~200 字的申请理由");
        }

        Map<String, Object> score = scoreMapper.selectScoreByStudentCourse(studentId, courseId);
        if (score == null)
        {
            throw new ServiceException("该课程暂无成绩，无法申请查分");
        }
        String term = str(score.get("term"));
        if (appealMapper.countPending(studentId, courseId, term) > 0)
        {
            throw new ServiceException("该课程已有待受理的查分申请，请勿重复提交");
        }

        String appealId = nextAppealId();
        Map<String, Object> insert = new HashMap<String, Object>();
        insert.put("appealId", appealId);
        insert.put("studentId", studentId);
        insert.put("courseId", courseId);
        insert.put("term", term);
        insert.put("originalScore", toInt(score.get("totalScore")));
        insert.put("reason", reason);
        appealMapper.insertAppeal(insert);
        return appealMapper.selectAppealViewById(appealId);
    }

    public List<Map<String, Object>> mine()
    {
        Map<String, Object> user = authService.currentUser();
        assertStudent(user);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("studentId", String.valueOf(user.get("loginId")));
        return appealMapper.selectAppealViews(params);
    }

    public List<Map<String, Object>> list(String status, String keyword, String courseId)
    {
        Map<String, Object> user = authService.currentUser();
        assertTeacher(user);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("classIds", authService.managedClassIds(user));
        params.put("status", status);
        params.put("keyword", keyword);
        params.put("courseId", courseId);
        return appealMapper.selectAppealViews(params);
    }

    /**
     * 受理查分申请：维持原成绩（KEEP）或更正成绩（CORRECT）。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> handle(String appealId, Map<String, Object> body)
    {
        Map<String, Object> user = authService.currentUser();
        assertTeacher(user);
        String handlerId = String.valueOf(user.get("loginId"));
        Map<String, Object> appeal = loadPendingAppeal(appealId, user);

        String result = str(body.get("result"));
        if (!"KEEP".equals(result) && !"CORRECT".equals(result))
        {
            throw new ServiceException("处理结果只能是 KEEP 或 CORRECT");
        }
        String opinion = str(body.get("opinion"));
        if (opinion.length() < 5 || opinion.length() > 200)
        {
            throw new ServiceException("请填写 5~200 字的处理意见");
        }

        Map<String, Object> handle = new HashMap<String, Object>();
        handle.put("appealId", appealId);
        handle.put("result", result);
        handle.put("opinion", opinion);
        handle.put("handlerId", handlerId);

        if ("CORRECT".equals(result))
        {
            Integer corrected = toInt(body.get("correctedScore"));
            if (corrected == null || corrected < 0 || corrected > 100)
            {
                throw new ServiceException("更正后的成绩必须是 0~100 的整数");
            }
            String studentId = String.valueOf(appeal.get("studentId"));
            String courseId = String.valueOf(appeal.get("courseId"));
            String term = String.valueOf(appeal.get("term"));
            Map<String, Object> score = scoreMapper.selectScoreByKeyForUpdate(studentId, courseId, term);
            if (score == null)
            {
                throw new ServiceException("成绩记录不存在，无法更正");
            }
            scoreMapper.updateTotal(buildTotalUpdate(score, corrected, handlerId));
            scoreMapper.insertLog(buildAppealLog(score, corrected, opinion, handlerId));
            handle.put("correctedScore", corrected);
        }
        else
        {
            handle.put("correctedScore", null);
        }
        appealMapper.updateHandle(handle);
        return appealMapper.selectAppealViewById(appealId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> reject(String appealId, Map<String, Object> body)
    {
        Map<String, Object> user = authService.currentUser();
        assertTeacher(user);
        String handlerId = String.valueOf(user.get("loginId"));
        loadPendingAppeal(appealId, user);
        String opinion = str(body.get("opinion"));
        if (opinion.length() < 5 || opinion.length() > 200)
        {
            throw new ServiceException("请填写 5~200 字的驳回理由");
        }
        Map<String, Object> update = new HashMap<String, Object>();
        update.put("appealId", appealId);
        update.put("opinion", opinion);
        update.put("handlerId", handlerId);
        appealMapper.updateReject(update);
        return appealMapper.selectAppealViewById(appealId);
    }

    private Map<String, Object> loadPendingAppeal(String appealId, Map<String, Object> user)
    {
        Map<String, Object> appeal = appealMapper.selectAppealForUpdate(appealId);
        if (appeal == null)
        {
            throw new ServiceException("查分申请不存在");
        }
        if (!"PENDING".equals(appeal.get("status")))
        {
            throw new ServiceException("该申请已处理，不能重复受理");
        }
        assertManageClass(user, String.valueOf(appeal.get("classId")));
        return appeal;
    }

    private Map<String, Object> buildTotalUpdate(Map<String, Object> score, Integer corrected, String handlerId)
    {
        Map<String, Object> update = new HashMap<String, Object>();
        update.put("scoreId", score.get("scoreId"));
        update.put("totalScore", corrected);
        update.put("gpa", GradeCalc.gpa(corrected));
        update.put("operatorId", handlerId);
        return update;
    }

    private Map<String, Object> buildAppealLog(Map<String, Object> score, Integer corrected,
            String opinion, String handlerId)
    {
        String reason = "查分受理更正：" + opinion;
        if (reason.length() > 100)
        {
            reason = reason.substring(0, 100);
        }
        Map<String, Object> log = new HashMap<String, Object>();
        log.put("logId", "LOG" + System.currentTimeMillis()
                + String.format("%03d", LOG_SEQ.getAndIncrement() % 1000));
        log.put("scoreId", score.get("scoreId"));
        log.put("studentId", score.get("studentId"));
        log.put("courseId", score.get("courseId"));
        log.put("term", score.get("term"));
        log.put("oldUsual", toInt(score.get("usualScore")));
        log.put("oldFinal", toInt(score.get("finalScore")));
        log.put("oldTotal", toInt(score.get("totalScore")));
        log.put("newUsual", toInt(score.get("usualScore")));
        log.put("newFinal", toInt(score.get("finalScore")));
        log.put("newTotal", corrected);
        log.put("changeReason", reason);
        log.put("operatorId", handlerId);
        log.put("source", "APPEAL");
        return log;
    }

    private String nextAppealId()
    {
        String prefix = "AP" + new SimpleDateFormat("yyyyMMdd").format(new Date());
        String max = appealMapper.selectMaxAppealId(prefix);
        int seq = 1;
        if (max != null && max.length() > prefix.length())
        {
            try
            {
                seq = Integer.parseInt(max.substring(prefix.length())) + 1;
            }
            catch (NumberFormatException ignored)
            {
                seq = 1;
            }
        }
        return prefix + String.format("%03d", seq);
    }

    private void assertStudent(Map<String, Object> user)
    {
        if (!PortalAuthService.ROLE_STUDENT.equals(user.get("role")))
        {
            throw new ServiceException("仅学生可执行该操作");
        }
    }

    private void assertTeacher(Map<String, Object> user)
    {
        if (!PortalAuthService.ROLE_TEACHER.equals(user.get("role")))
        {
            throw new ServiceException("仅教师可执行该操作");
        }
    }

    private void assertManageClass(Map<String, Object> user, String classId)
    {
        List<String> classIds = authService.managedClassIds(user);
        if (classId == null || !classIds.contains(classId))
        {
            throw new ServiceException("无权操作该班级");
        }
    }

    private Integer toInt(Object value)
    {
        if (value == null || "".equals(String.valueOf(value).trim()))
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        try
        {
            return Integer.valueOf(String.valueOf(value).trim());
        }
        catch (NumberFormatException ex)
        {
            throw new ServiceException("成绩必须是 0~100 的整数");
        }
    }

    private String str(Object value)
    {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private boolean isEmpty(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}
