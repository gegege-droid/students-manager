package com.grade.service;

import com.grade.common.ServiceException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 查分申请与受理服务。
 * 对应《概要设计说明书》§6.2 的 17~22 号接口，业务规则见 §4.3（R-06、R-07、R-08、R-09、R-10）。
 * 状态机：PENDING → ACCEPTED(KEEP/CORRECT) 或 REJECTED，处理流程与《详细设计说明书》§7 一致。
 */
@Service
public class AppealService {

    private final JdbcTemplate jdbc;
    private final MetaService meta;
    private final ScoreService scoreService;

    public AppealService(JdbcTemplate jdbc, MetaService meta, ScoreService scoreService) {
        this.jdbc = jdbc;
        this.meta = meta;
        this.scoreService = scoreService;
    }

    private static final String BASE_SELECT = """
            SELECT a.appeal_id AS appealId, a.score_id AS scoreId, a.student_id AS studentId,
                   st.student_name AS studentName, st.class_id AS className, st.combination,
                   a.exam_id AS examId, e.exam_name AS examName,
                   DATE_FORMAT(e.exam_date, '%Y-%m-%d') AS examDate,
                   a.subject_id AS subjectId, sub.subject_name AS subjectName,
                   sub.full_score AS fullScore, a.original_score AS originalScore,
                   a.reason, DATE_FORMAT(a.apply_time, '%Y-%m-%d %H:%i') AS applyTime,
                   a.status, a.handle_result AS handleResult, a.corrected_score AS correctedScore,
                   a.opinion, a.handler_id AS handlerId,
                   DATE_FORMAT(a.handle_time, '%Y-%m-%d %H:%i') AS handleTime
            FROM biz_appeal a
            LEFT JOIN biz_student st ON st.student_id = a.student_id
            LEFT JOIN biz_exam e ON e.exam_id = a.exam_id
            LEFT JOIN sys_subject sub ON sub.subject_id = a.subject_id
            WHERE a.del_flag = '0'
            """;

    /** 班主任侧列表：PENDING 为待受理，其余（ACCEPTED/REJECTED）为已处理 */
    public Map<String, Object> list(String status, String keyword, String examId, String subjectId, Integer days) {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        List<Object> args = new ArrayList<>();
        if ("PENDING".equals(status)) {
            sql.append(" AND a.status = 'PENDING'");
        } else if ("HANDLED".equals(status)) {
            sql.append(" AND a.status <> 'PENDING'");
        } else if (status != null && !status.isBlank()) {
            sql.append(" AND a.status = ?");
            args.add(status);
        }
        if (examId != null && !examId.isBlank()) {
            sql.append(" AND a.exam_id = ?");
            args.add(examId);
        }
        if (subjectId != null && !subjectId.isBlank()) {
            sql.append(" AND a.subject_id = ?");
            args.add(subjectId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (a.appeal_id LIKE ? OR a.student_id LIKE ? OR st.student_name LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        if (days != null && days > 0) {
            sql.append(" AND a.apply_time >= DATE_SUB(NOW(), INTERVAL ? DAY)");
            args.add(days);
        }
        sql.append(" ORDER BY a.apply_time DESC");
        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), args.toArray());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", rows.size());
        data.put("rows", rows);
        data.put("pendingCount", rows.stream().filter(r -> "PENDING".equals(r.get("status"))).count());
        return data;
    }

    public List<Map<String, Object>> mine(String studentId) {
        return jdbc.queryForList(BASE_SELECT + " AND a.student_id = ? ORDER BY a.apply_time DESC", studentId);
    }

    public Map<String, Object> detail(String appealId) {
        List<Map<String, Object>> list = jdbc.queryForList(BASE_SELECT + " AND a.appeal_id = ?", appealId);
        if (list.isEmpty()) {
            throw new ServiceException("B4003", "申请不存在");
        }
        return list.get(0);
    }

    /**
     * 提交查分申请：校验理由长度、拦截重复申请、生成编号、快照原分、把成绩置为查分中。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> create(String studentId, String examId, String subjectId, String reason) {
        String text = reason == null ? "" : reason.trim();
        if (text.length() < 10 || text.length() > 200) {
            throw new ServiceException("B4001", "请填写 10～200 字的申请理由");
        }
        Integer pending = jdbc.queryForObject("""
                SELECT COUNT(*) FROM biz_appeal
                WHERE student_id = ? AND exam_id = ? AND subject_id = ?
                  AND status = 'PENDING' AND del_flag = '0'
                """, Integer.class, studentId, examId, subjectId);
        if (pending != null && pending > 0) {
            throw new ServiceException("B4002", "该场次该科目已有待受理的查分申请，请勿重复提交");
        }
        List<Map<String, Object>> scores = jdbc.queryForList("""
                SELECT score_id, score FROM biz_score
                WHERE student_id = ? AND exam_id = ? AND subject_id = ? AND del_flag = '0'
                """, studentId, examId, subjectId);
        if (scores.isEmpty()) {
            throw new ServiceException("B3002", "该场次该科目还没有成绩，无法申请查分");
        }
        Map<String, Object> score = scores.get(0);
        String appealId = nextAppealId();
        jdbc.update("""
                INSERT INTO biz_appeal (appeal_id, score_id, student_id, exam_id, subject_id,
                        original_score, reason, apply_time, status, create_by, create_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), 'PENDING', ?, NOW())
                """, appealId, score.get("score_id"), studentId, examId, subjectId,
                score.get("score"), text, studentId);
        jdbc.update("UPDATE biz_score SET status = 'APPEALING' WHERE score_id = ?", score.get("score_id"));
        return detail(appealId);
    }

    /**
     * 受理：以 status='PENDING' 作为并发条件更新（乐观控制），
     * 更正时同步改分并写入 source=APPEAL 的修改记录。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> handle(String appealId, String result, Integer correctedScore,
                                      String opinion, String handlerId) {
        String text = opinion == null ? "" : opinion.trim();
        if (text.length() < 5 || text.length() > 200) {
            throw new ServiceException("B4004", "请填写 5～200 字的处理意见");
        }
        if (!"KEEP".equals(result) && !"CORRECT".equals(result)) {
            throw new ServiceException("B0004", "处理结果只能是 KEEP（维持）或 CORRECT（更正）");
        }
        List<Map<String, Object>> appeals = jdbc.queryForList("""
                SELECT appeal_id, student_id, exam_id, subject_id, score_id, status
                FROM biz_appeal WHERE appeal_id = ? AND del_flag = '0'
                """, appealId);
        if (appeals.isEmpty()) {
            throw new ServiceException("B4003", "申请不存在");
        }
        Map<String, Object> appeal = appeals.get(0);
        String subjectId = String.valueOf(appeal.get("subject_id"));
        int full = meta.fullScoreOf(subjectId);
        if ("CORRECT".equals(result)) {
            if (correctedScore == null) {
                throw new ServiceException("B4005", "更正分数不能为空");
            }
            if (correctedScore < 0 || correctedScore > full) {
                throw new ServiceException("B4005", "更正分数必须是 0～" + full + " 的整数");
            }
        }
        int updated = jdbc.update("""
                UPDATE biz_appeal SET status = 'ACCEPTED', handle_result = ?, corrected_score = ?,
                       opinion = ?, handler_id = ?, handle_time = NOW(), update_by = ?, update_time = NOW()
                WHERE appeal_id = ? AND status = 'PENDING'
                """, result, "CORRECT".equals(result) ? correctedScore : null,
                text, handlerId, handlerId, appealId);
        if (updated == 0) {
            throw new ServiceException("B4003", "该申请已被处理，请刷新后重试");
        }
        Object scoreId = appeal.get("score_id");
        if ("CORRECT".equals(result) && scoreId != null) {
            Integer old = jdbc.queryForObject(
                    "SELECT score FROM biz_score WHERE score_id = ?", Integer.class, scoreId);
            double rate = ScoreService.round(correctedScore / (double) full, 4);
            jdbc.update("""
                    UPDATE biz_score SET score = ?, score_rate = ?, level = ?, status = 'MODIFIED',
                           update_by = ?, update_time = NOW() WHERE score_id = ?
                    """, correctedScore, rate, MetaService.levelOf(rate), handlerId, scoreId);
            jdbc.update("""
                    INSERT INTO biz_score_log (log_id, score_id, student_id, exam_id, subject_id,
                            old_score, new_score, change_reason, operator_id, operate_time, source, appeal_id)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), 'APPEAL', ?)
                    """, "LOG" + System.currentTimeMillis(), scoreId, appeal.get("student_id"),
                    appeal.get("exam_id"), appeal.get("subject_id"), old, correctedScore,
                    "查分受理更正：" + text, handlerId, appealId);
            recalcTotalSafely(String.valueOf(appeal.get("exam_id")));
        } else if (scoreId != null) {
            restoreScoreStatus(String.valueOf(scoreId), String.valueOf(appeal.get("student_id")),
                    String.valueOf(appeal.get("exam_id")), subjectId);
        }
        return detail(appealId);
    }

    /** 驳回：申请置为 REJECTED，不修改成绩 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> reject(String appealId, String opinion, String handlerId) {
        String text = opinion == null ? "" : opinion.trim();
        if (text.length() < 5 || text.length() > 200) {
            throw new ServiceException("B4004", "请填写 5～200 字的驳回理由");
        }
        List<Map<String, Object>> appeals = jdbc.queryForList("""
                SELECT appeal_id, score_id, student_id, exam_id, subject_id
                FROM biz_appeal WHERE appeal_id = ? AND del_flag = '0'
                """, appealId);
        if (appeals.isEmpty()) {
            throw new ServiceException("B4003", "申请不存在");
        }
        int updated = jdbc.update("""
                UPDATE biz_appeal SET status = 'REJECTED', handle_result = NULL, opinion = ?,
                       handler_id = ?, handle_time = NOW(), update_by = ?, update_time = NOW()
                WHERE appeal_id = ? AND status = 'PENDING'
                """, text, handlerId, handlerId, appealId);
        if (updated == 0) {
            throw new ServiceException("B4003", "该申请已被处理，请刷新后重试");
        }
        Map<String, Object> appeal = appeals.get(0);
        if (appeal.get("score_id") != null) {
            restoreScoreStatus(String.valueOf(appeal.get("score_id")),
                    String.valueOf(appeal.get("student_id")),
                    String.valueOf(appeal.get("exam_id")), String.valueOf(appeal.get("subject_id")));
        }
        return detail(appealId);
    }

    /** 申请结束后恢复成绩状态：有修改记录为 MODIFIED，否则 NORMAL */
    private void restoreScoreStatus(String scoreId, String studentId, String examId, String subjectId) {
        Integer logs = jdbc.queryForObject("""
                SELECT COUNT(*) FROM biz_score_log
                WHERE student_id = ? AND exam_id = ? AND subject_id = ?
                """, Integer.class, studentId, examId, subjectId);
        String status = (logs != null && logs > 0) ? "MODIFIED" : "NORMAL";
        jdbc.update("""
                UPDATE biz_score SET status = ?, update_time = NOW()
                WHERE score_id = ? AND status = 'APPEALING'
                """, status, scoreId);
    }

    private void recalcTotalSafely(String examId) {
        try {
            scoreService.recalcTotal(examId);
        } catch (Exception ignore) {
            // 核算失败不影响受理结果，可后续重算
        }
    }

    /** 申请编号：AP + yyyyMMdd + 3 位当日流水（R-07） */
    public String nextAppealId() {
        String base = "AP" + DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now());
        for (int i = 1; i < 1000; i++) {
            String candidate = base + String.format("%03d", i);
            Integer exists = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM biz_appeal WHERE appeal_id = ?", Integer.class, candidate);
            if (exists == null || exists == 0) {
                return candidate;
            }
        }
        throw new ServiceException("B0005", "当日申请编号已用尽");
    }
}
