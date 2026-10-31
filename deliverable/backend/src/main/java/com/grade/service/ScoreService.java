package com.grade.service;

import com.grade.common.ServiceException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 成绩服务：成绩单矩阵、分页查询、录入名单、批量录入、修改留痕。
 * 对应《概要设计说明书》§6.2 的 9~16 号接口，业务规则见 §4.3（R-01、R-02、R-03、R-09、R-10、R-11）。
 */
@Service
public class ScoreService {

    private final JdbcTemplate jdbc;
    private final MetaService meta;

    public ScoreService(JdbcTemplate jdbc, MetaService meta) {
        this.jdbc = jdbc;
        this.meta = meta;
    }

    // ---------------------------------------------------------------- 矩阵
    /**
     * 学生成绩单矩阵：行 = 该生 6 门科目，列 = 已出成绩的 6 次月考，另附每次月考的总分与排名。
     */
    public Map<String, Object> matrix(String studentId, String examId, String subjectId) {
        Map<String, Object> student = meta.studentProfile(studentId);
        if (student == null) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("rows", List.of());
            empty.put("totals", List.of());
            empty.put("sessions", List.of());
            empty.put("fullScore", 0);
            return empty;
        }

        List<Map<String, Object>> allSessions = meta.publishedExams();
        List<Map<String, Object>> sessions = allSessions.stream()
                .filter(s -> examId == null || examId.isBlank() || examId.equals(s.get("examId")))
                .toList();

        List<String> subjectIds = meta.subjectIdsOfStudent(studentId);
        if (subjectId != null && !subjectId.isBlank()) {
            subjectIds = List.of(subjectId);
        }

        Map<String, Integer> scoreMap = new HashMap<>();      // "examId|subjectId" -> score
        Map<String, String> statusMap = new HashMap<>();       // "examId|subjectId" -> status
        Map<String, Integer> fullScoreMap = new HashMap<>();
        for (Map<String, Object> s : meta.subjects()) {
            fullScoreMap.put(String.valueOf(s.get("subjectId")), ((Number) s.get("fullScore")).intValue());
        }
        for (Map<String, Object> row : jdbc.queryForList("""
                SELECT exam_id, subject_id, score FROM biz_score
                WHERE student_id = ? AND del_flag = '0'
                """, studentId)) {
            scoreMap.put(row.get("exam_id") + "|" + row.get("subject_id"),
                    ((Number) row.get("score")).intValue());
        }
        Set<String> pending = new HashSet<>();
        for (Map<String, Object> row : jdbc.queryForList("""
                SELECT exam_id, subject_id FROM biz_appeal
                WHERE student_id = ? AND status = 'PENDING' AND del_flag = '0'
                """, studentId)) {
            pending.add(row.get("exam_id") + "|" + row.get("subject_id"));
        }
        Set<String> modified = new HashSet<>();
        for (Map<String, Object> row : jdbc.queryForList("""
                SELECT DISTINCT exam_id, subject_id FROM biz_score_log WHERE student_id = ?
                """, studentId)) {
            modified.add(row.get("exam_id") + "|" + row.get("subject_id"));
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (String sid : subjectIds) {
            Map<String, Object> subject = meta.subjectById(sid);
            int full = fullScoreMap.getOrDefault(sid, 100);
            List<Map<String, Object>> cells = new ArrayList<>();
            List<Integer> valid = new ArrayList<>();
            double rateSum = 0;
            for (Map<String, Object> session : allSessions) {
                String key = session.get("examId") + "|" + sid;
                Integer score = scoreMap.get(key);
                Map<String, Object> cell = new LinkedHashMap<>();
                cell.put("examId", session.get("examId"));
                cell.put("score", score);
                cell.put("rate", score == null ? null : round(score / (double) full, 4));
                cell.put("status", statusOf(key, pending, modified));
                cells.add(cell);
                if (score != null) {
                    valid.add(score);
                    rateSum += score / (double) full;
                }
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("subjectId", sid);
            row.put("name", subject.get("name"));
            row.put("fullScore", full);
            row.put("category", subject.get("category"));
            row.put("cells", cells);
            row.put("latest", valid.isEmpty() ? null : valid.get(valid.size() - 1));
            row.put("averageRate", valid.isEmpty() ? null : round(rateSum / valid.size(), 4));
            row.put("delta", valid.size() >= 2 ? valid.get(valid.size() - 1) - valid.get(valid.size() - 2) : 0);
            row.put("firstScore", valid.isEmpty() ? null : valid.get(0));
            row.put("improved", valid.size() >= 2 ? valid.get(valid.size() - 1) - valid.get(0) : 0);
            rows.add(row);
        }

        Map<String, Map<String, Object>> totalMap = new HashMap<>();
        for (Map<String, Object> t : jdbc.queryForList("""
                SELECT exam_id, total_score, score_rate, class_rank, grade_rank
                FROM biz_score_total WHERE student_id = ?
                """, studentId)) {
            totalMap.put(String.valueOf(t.get("exam_id")), t);
        }
        List<Map<String, Object>> totals = new ArrayList<>();
        for (Map<String, Object> session : sessions) {
            String eid = String.valueOf(session.get("examId"));
            Map<String, Object> t = totalMap.get(eid);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("examId", eid);
            row.put("name", session.get("name"));
            row.put("total", t == null ? null : t.get("total_score"));
            row.put("rate", t == null ? null : t.get("score_rate"));
            row.put("classRank", t == null ? null : t.get("class_rank"));
            row.put("gradeRank", t == null ? null : t.get("grade_rank"));
            totals.add(row);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("rows", rows);
        data.put("totals", totals);
        data.put("sessions", sessions);
        data.put("fullScore", subjectId != null && !subjectId.isBlank()
                ? fullScoreMap.getOrDefault(subjectId, 100) : 750);
        data.put("student", student);
        return data;
    }

    private String statusOf(String key, Set<String> pending, Set<String> modified) {
        if (pending.contains(key)) {
            return "APPEALING";
        }
        return modified.contains(key) ? "MODIFIED" : "NORMAL";
    }

    // ---------------------------------------------------------------- 分页查询
    public Map<String, Object> list(String keyword, String examId, String subjectId, String classId) {
        StringBuilder sql = new StringBuilder("""
                SELECT sc.score_id AS scoreId, sc.student_id AS studentId, st.student_name AS studentName,
                       st.class_id AS classId, st.combination,
                       sc.exam_id AS examId, e.exam_name AS examName,
                       DATE_FORMAT(e.exam_date, '%Y-%m-%d') AS examDate,
                       sc.subject_id AS subjectId, sub.subject_name AS subjectName,
                       sub.full_score AS fullScore, sc.score, sc.score_rate AS scoreRate,
                       sc.level, sc.status, sc.recorder_id AS recorderId,
                       DATE_FORMAT(sc.record_time, '%Y-%m-%d %H:%i') AS recordTime
                FROM biz_score sc
                JOIN biz_student st ON st.student_id = sc.student_id
                JOIN sys_subject sub ON sub.subject_id = sc.subject_id
                JOIN biz_exam e ON e.exam_id = sc.exam_id
                WHERE sc.del_flag = '0'
                """);
        List<Object> args = new ArrayList<>();
        if (examId != null && !examId.isBlank()) {
            sql.append(" AND sc.exam_id = ?");
            args.add(examId);
        }
        if (subjectId != null && !subjectId.isBlank()) {
            sql.append(" AND sc.subject_id = ?");
            args.add(subjectId);
        }
        if (classId != null && !classId.isBlank()) {
            sql.append(" AND st.class_id = ?");
            args.add(classId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (sc.student_id LIKE ? OR st.student_name LIKE ?)");
            args.add("%" + keyword.trim() + "%");
            args.add("%" + keyword.trim() + "%");
        }
        sql.append(" ORDER BY sc.exam_id DESC, sc.student_id ASC");
        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), args.toArray());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", rows.size());
        data.put("rows", rows);
        return data;
    }

    // ---------------------------------------------------------------- 录入名单
    public List<Map<String, Object>> roster(String classId, String examId, String subjectId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : jdbc.queryForList("""
                SELECT student_id AS studentId, student_name AS name, class_id AS classId,
                       combination
                FROM biz_student WHERE del_flag = '0' AND (? = '' OR class_id = ?)
                ORDER BY student_id
                """, classId == null ? "" : classId, classId == null ? "" : classId)) {
            String sid = String.valueOf(row.get("studentId"));
            if (!meta.subjectIdsOfStudent(sid).contains(subjectId)) {
                continue;
            }
            Integer score = null;
            try {
                score = jdbc.queryForObject("""
                        SELECT score FROM biz_score
                        WHERE student_id = ? AND exam_id = ? AND subject_id = ? AND del_flag = '0'
                        """, Integer.class, sid, examId, subjectId);
            } catch (Exception ignore) {
                score = null;
            }
            Map<String, Object> r = new LinkedHashMap<>(row);
            r.put("score", score);
            r.put("recorded", score != null);
            result.add(r);
        }
        return result;
    }

    // ---------------------------------------------------------------- 批量录入
    /**
     * 批量录入：按唯一键 (student_id, exam_id, subject_id) 做 upsert，
     * 与建表脚本里的 ON DUPLICATE KEY UPDATE 等价（R-02）。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batch(String examId, String subjectId, List<Map<String, Object>> rows,
                                     String recorderId) {
        int full = meta.fullScoreOf(subjectId);
        int saved = 0;
        for (Map<String, Object> row : rows) {
            String studentId = String.valueOf(row.get("studentId"));
            Object raw = row.get("score");
            if (raw == null || String.valueOf(raw).isBlank()) {
                continue;
            }
            int score = ((Number) raw).intValue();
            if (score < 0 || score > full) {
                throw new ServiceException("B3001",
                        "学号 " + studentId + " 的分数 " + score + " 超出 0～" + full + " 的范围");
            }
            double rate = round(score / (double) full, 4);
            String level = MetaService.levelOf(rate);
            String scoreId = "SC" + studentId + examId + subjectId;
            int updated = jdbc.update("""
                    UPDATE biz_score SET score = ?, score_rate = ?, level = ?,
                           recorder_id = ?, record_time = NOW(), update_by = ?, update_time = NOW()
                    WHERE student_id = ? AND exam_id = ? AND subject_id = ? AND del_flag = '0'
                    """, score, rate, level, recorderId, recorderId, studentId, examId, subjectId);
            if (updated == 0) {
                jdbc.update("""
                        INSERT INTO biz_score (score_id, student_id, exam_id, subject_id, score,
                                score_rate, level, status, recorder_id, record_time, create_by, create_time)
                        VALUES (?, ?, ?, ?, ?, ?, ?, 'NORMAL', ?, NOW(), ?, NOW())
                        """, scoreId, studentId, examId, subjectId, score, rate, level, recorderId, recorderId);
            }
            saved++;
        }
        recalcTotal(examId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("saved", saved);
        data.put("failed", List.of());
        return data;
    }

    // ---------------------------------------------------------------- 修改成绩
    /**
     * 修改单条成绩并写入修改记录（R-10、R-11）：查分中的成绩不允许修改。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> update(String studentId, String examId, String subjectId,
                                      int newScore, String reason, String operatorId) {
        List<Map<String, Object>> list = jdbc.queryForList("""
                SELECT score_id, score, status FROM biz_score
                WHERE student_id = ? AND exam_id = ? AND subject_id = ? AND del_flag = '0'
                """, studentId, examId, subjectId);
        if (list.isEmpty()) {
            throw new ServiceException("B3002", "成绩记录不存在");
        }
        Map<String, Object> score = list.get(0);
        if ("APPEALING".equals(score.get("status"))) {
            throw new ServiceException("B3004", "该成绩处于查分中，请先完成受理");
        }
        int full = meta.fullScoreOf(subjectId);
        if (newScore < 0 || newScore > full) {
            throw new ServiceException("B3001", "分数必须是 0～" + full + " 的整数");
        }
        if (reason == null || reason.trim().length() < 5 || reason.trim().length() > 100) {
            throw new ServiceException("B0002", "修改原因需 5～100 字");
        }
        int old = ((Number) score.get("score")).intValue();
        if (old == newScore) {
            throw new ServiceException("B0003", "新分数与原分数相同，无需修改");
        }
        double rate = round(newScore / (double) full, 4);
        jdbc.update("""
                UPDATE biz_score SET score = ?, score_rate = ?, level = ?, status = 'MODIFIED',
                       recorder_id = ?, update_by = ?, update_time = NOW()
                WHERE score_id = ?
                """, newScore, rate, MetaService.levelOf(rate), operatorId, operatorId, score.get("score_id"));
        String logId = "LOG" + DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(LocalDateTime.now());
        jdbc.update("""
                INSERT INTO biz_score_log (log_id, score_id, student_id, exam_id, subject_id,
                        old_score, new_score, change_reason, operator_id, operate_time, source)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), 'MANUAL')
                """, logId, score.get("score_id"), studentId, examId, subjectId,
                old, newScore, reason.trim(), operatorId);
        recalcTotal(examId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("scoreId", score.get("score_id"));
        data.put("oldScore", old);
        data.put("newScore", newScore);
        data.put("logId", logId);
        return data;
    }

    // ---------------------------------------------------------------- 修改记录
    public List<Map<String, Object>> logs(String studentId, String examId, String subjectId) {
        return jdbc.queryForList("""
                SELECT l.log_id AS logId, l.score_id AS scoreId, l.student_id AS studentId,
                       st.student_name AS studentName, l.exam_id AS examId, e.exam_name AS examName,
                       l.subject_id AS subjectId, sub.subject_name AS subjectName,
                       l.old_score AS oldScore, l.new_score AS newScore,
                       l.change_reason AS changeReason, l.operator_id AS operatorId,
                       DATE_FORMAT(l.operate_time, '%Y-%m-%d %H:%i') AS operateTime, l.source
                FROM biz_score_log l
                LEFT JOIN biz_student st ON st.student_id = l.student_id
                LEFT JOIN biz_exam e ON e.exam_id = l.exam_id
                LEFT JOIN sys_subject sub ON sub.subject_id = l.subject_id
                WHERE l.student_id = ? AND (? = '' OR l.exam_id = ?) AND (? = '' OR l.subject_id = ?)
                ORDER BY l.operate_time DESC
                """, studentId, nvl(examId), nvl(examId), nvl(subjectId), nvl(subjectId));
    }

    // ---------------------------------------------------------------- 总分与排名
    public List<Map<String, Object>> totals(String studentId, String examId) {
        return jdbc.queryForList("""
                SELECT t.exam_id AS examId, e.exam_name AS examName, t.student_id AS studentId,
                       st.student_name AS studentName, t.total_score AS totalScore,
                       t.score_rate AS scoreRate, t.class_rank AS classRank, t.grade_rank AS gradeRank,
                       DATE_FORMAT(t.calc_time, '%Y-%m-%d %H:%i') AS calcTime
                FROM biz_score_total t
                LEFT JOIN biz_exam e ON e.exam_id = t.exam_id
                LEFT JOIN biz_student st ON st.student_id = t.student_id
                WHERE (? = '' OR t.student_id = ?) AND (? = '' OR t.exam_id = ?)
                ORDER BY t.exam_id DESC, t.class_rank ASC
                """, nvl(studentId), nvl(studentId), nvl(examId), nvl(examId));
    }

    /**
     * 重算某场次的总分与班级排名（对应《概要设计说明书》§6.8 的核算 SQL）。
     */
    public void recalcTotal(String examId) {
        jdbc.update("""
                INSERT INTO biz_score_total (student_id, exam_id, total_score, score_rate, calc_time)
                SELECT student_id, exam_id, SUM(score), ROUND(SUM(score) / 750, 4), NOW()
                FROM biz_score WHERE exam_id = ? AND del_flag = '0'
                GROUP BY student_id, exam_id
                ON DUPLICATE KEY UPDATE total_score = VALUES(total_score),
                        score_rate = VALUES(score_rate), calc_time = NOW()
                """, examId);
        jdbc.update("""
                UPDATE biz_score_total t
                JOIN (SELECT student_id, RANK() OVER (ORDER BY total_score DESC) AS rk
                      FROM biz_score_total WHERE exam_id = ?) r
                  ON r.student_id = t.student_id
                SET t.class_rank = r.rk
                WHERE t.exam_id = ?
                """, examId, examId);
    }

    static double round(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
