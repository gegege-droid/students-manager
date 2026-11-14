package com.ruoyi.portal.service;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import com.ruoyi.portal.mapper.PortalScoreMapper;

/**
 * 门户成绩服务。
 *
 * 负责成绩查询、批量录入、单条修改与修改留痕。
 * 所有写操作都校验教师所带班级，并在事务内检查 PENDING 查分申请。
 */
@Service
public class PortalScoreService
{
    private static final AtomicInteger ID_SEQ = new AtomicInteger();

    @Autowired
    private PortalAuthService authService;

    @Autowired
    private PortalScoreMapper scoreMapper;

    public List<Map<String, Object>> list(String term, String keyword, String classId, String courseId)
    {
        Map<String, Object> user = authService.currentUser();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("term", term);
        params.put("keyword", keyword);
        params.put("courseId", courseId);

        if (PortalAuthService.ROLE_STUDENT.equals(user.get("role")))
        {
            params.put("studentId", user.get("loginId"));
        }
        else
        {
            assertTeacher(user);
            params.put("classIds", authService.managedClassIds(user));
            if (!isEmpty(classId))
            {
                assertManageClass(user, classId);
                params.put("classId", classId);
            }
        }
        return scoreMapper.selectScoreViews(params);
    }

    public List<Map<String, Object>> roster(String classId, String courseId, String term)
    {
        Map<String, Object> user = authService.currentUser();
        assertTeacher(user);
        if (isEmpty(classId) || isEmpty(courseId) || isEmpty(term))
        {
            throw new ServiceException("请先选择班级、课程与学期");
        }
        assertManageClass(user, classId);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("classId", classId);
        params.put("courseId", courseId);
        params.put("term", term);
        return scoreMapper.selectRoster(params);
    }

    public List<Map<String, Object>> logs(String scoreId)
    {
        Map<String, Object> user = authService.currentUser();
        Map<String, Object> score = scoreMapper.selectScoreViewById(scoreId);
        if (score == null)
        {
            throw new ServiceException("成绩记录不存在");
        }
        if (PortalAuthService.ROLE_STUDENT.equals(user.get("role")))
        {
            if (!String.valueOf(user.get("loginId")).equals(String.valueOf(score.get("studentId"))))
            {
                throw new ServiceException("无权查看他人成绩");
            }
        }
        else
        {
            assertTeacher(user);
            assertManageClass(user, String.valueOf(score.get("classId")));
        }
        return scoreMapper.selectLogs(scoreId);
    }

    /**
     * 教师批量录入成绩。已存在的成绩会被更新；存在待受理申请的行直接拒绝。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> saveBatch(Map<String, Object> body)
    {
        Map<String, Object> user = authService.currentUser();
        assertTeacher(user);
        String classId = str(body.get("classId"));
        String courseId = str(body.get("courseId"));
        String term = str(body.get("term"));
        if (isEmpty(classId) || isEmpty(courseId) || isEmpty(term))
        {
            throw new ServiceException("班级、课程、学期不能为空");
        }
        assertManageClass(user, classId);

        Object rawRows = body.get("rows");
        if (!(rawRows instanceof List) || ((List<?>) rawRows).isEmpty())
        {
            throw new ServiceException("请至少录入一条成绩");
        }
        String operatorId = String.valueOf(user.get("loginId"));
        int saved = 0;
        int index = 0;
        for (Object item : (List<?>) rawRows)
        {
            index++;
            if (!(item instanceof Map))
            {
                throw new ServiceException("第" + index + "行数据格式不正确");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> row = (Map<String, Object>) item;
            String studentId = str(row.get("studentId"));
            Integer usual = toInt(row.get("usualScore"));
            Integer finalScore = toInt(row.get("finalScore"));
            if (isEmpty(studentId))
            {
                throw new ServiceException("第" + index + "行缺少学号");
            }
            if (usual == null || finalScore == null)
            {
                throw new ServiceException("第" + index + "行需同时填写平时成绩与期末成绩");
            }
            validateScore(usual, "平时成绩");
            validateScore(finalScore, "期末成绩");

            String studentClass = scoreMapper.selectStudentClass(studentId);
            if (!classId.equals(studentClass))
            {
                throw new ServiceException("学生 " + studentId + " 不属于班级 " + classId);
            }

            Map<String, Object> existing = scoreMapper.selectScoreByKeyForUpdate(studentId, courseId, term);
            if (existing != null && countPending(studentId, courseId, term) > 0)
            {
                throw new ServiceException("学生 " + studentId + " 的该课程正在查分受理中，禁止修改");
            }

            Integer total = GradeCalc.total(usual, finalScore);
            BigDecimal gpa = GradeCalc.gpa(total);
            if (existing != null)
            {
                Map<String, Object> update = new HashMap<String, Object>();
                update.put("scoreId", existing.get("scoreId"));
                update.put("usualScore", usual);
                update.put("finalScore", finalScore);
                update.put("totalScore", total);
                update.put("gpa", gpa);
                update.put("operatorId", operatorId);
                scoreMapper.updateScore(update);
            }
            else
            {
                Map<String, Object> insert = new HashMap<String, Object>();
                insert.put("scoreId", nextScoreId());
                insert.put("studentId", studentId);
                insert.put("courseId", courseId);
                insert.put("term", term);
                insert.put("usualScore", usual);
                insert.put("finalScore", finalScore);
                insert.put("totalScore", total);
                insert.put("gpa", gpa);
                insert.put("recorderId", operatorId);
                scoreMapper.insertScore(insert);
            }
            saved++;
        }

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("savedCount", saved);
        return result;
    }

    /**
     * 教师修改单条成绩：先锁定成绩行，检查查分状态，再更新并写修改记录。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateScore(String scoreId, Map<String, Object> body)
    {
        Map<String, Object> user = authService.currentUser();
        assertTeacher(user);
        Map<String, Object> score = scoreMapper.selectScoreViewById(scoreId);
        if (score == null)
        {
            throw new ServiceException("成绩记录不存在");
        }
        assertManageClass(user, String.valueOf(score.get("classId")));

        String studentId = String.valueOf(score.get("studentId"));
        String courseId = String.valueOf(score.get("courseId"));
        String term = String.valueOf(score.get("term"));
        if (countPending(studentId, courseId, term) > 0)
        {
            throw new ServiceException("该成绩查分中，禁止直接修改，请先完成查分受理");
        }

        Integer usual = toInt(body.get("usualScore"));
        Integer finalScore = toInt(body.get("finalScore"));
        if (usual == null || finalScore == null)
        {
            throw new ServiceException("需同时填写平时成绩与期末成绩");
        }
        validateScore(usual, "平时成绩");
        validateScore(finalScore, "期末成绩");
        String reason = str(body.get("reason"));
        if (reason.length() < 5 || reason.length() > 100)
        {
            throw new ServiceException("请填写 5~100 字的修改原因");
        }

        Integer total = GradeCalc.total(usual, finalScore);
        BigDecimal gpa = GradeCalc.gpa(total);
        String operatorId = String.valueOf(user.get("loginId"));

        Map<String, Object> update = new HashMap<String, Object>();
        update.put("scoreId", scoreId);
        update.put("usualScore", usual);
        update.put("finalScore", finalScore);
        update.put("totalScore", total);
        update.put("gpa", gpa);
        update.put("operatorId", operatorId);
        scoreMapper.updateScore(update);

        Map<String, Object> log = new HashMap<String, Object>();
        log.put("logId", nextLogId());
        log.put("scoreId", scoreId);
        log.put("studentId", studentId);
        log.put("courseId", courseId);
        log.put("term", term);
        log.put("oldUsual", toInt(score.get("usualScore")));
        log.put("oldFinal", toInt(score.get("finalScore")));
        log.put("oldTotal", toInt(score.get("totalScore")));
        log.put("newUsual", usual);
        log.put("newFinal", finalScore);
        log.put("newTotal", total);
        log.put("changeReason", reason);
        log.put("operatorId", operatorId);
        log.put("source", "MANUAL");
        scoreMapper.insertLog(log);

        return scoreMapper.selectScoreViewById(scoreId);
    }

    private int countPending(String studentId, String courseId, String term)
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("studentId", studentId);
        params.put("courseId", courseId);
        params.put("term", term);
        return scoreMapper.countPendingAppeal(params);
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

    private void validateScore(Integer value, String label)
    {
        if (value == null || value < 0 || value > 100)
        {
            throw new ServiceException(label + "必须是 0~100 的整数");
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

    private String nextScoreId()
    {
        return "SC" + System.currentTimeMillis() + String.format("%03d", ID_SEQ.getAndIncrement() % 1000);
    }

    private String nextLogId()
    {
        return "LOG" + System.currentTimeMillis() + String.format("%03d", ID_SEQ.getAndIncrement() % 1000);
    }
}
