package com.grade.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 基础数据服务：科目字典、月考场次、班级、学生档案。
 * 对应《概要设计说明书》§6.2 的 5~8 号接口。
 */
@Service
public class MetaService {

    private final JdbcTemplate jdbc;

    public MetaService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 科目字典（9 门），按 sort_no 排序 */
    public List<Map<String, Object>> subjects() {
        return jdbc.queryForList("""
                SELECT subject_id AS subjectId, subject_name AS name, full_score AS fullScore,
                       subject_category AS category, sort_no AS sortNo
                FROM sys_subject WHERE del_flag = '0' AND status = '0'
                ORDER BY sort_no
                """);
    }

    /** 月考场次（7 次），含成绩状态 */
    public List<Map<String, Object>> exams() {
        return jdbc.queryForList("""
                SELECT exam_id AS examId, exam_name AS name,
                       DATE_FORMAT(exam_date, '%Y-%m-%d') AS date,
                       stage, score_status AS scoreStatus, sort_no AS sortNo
                FROM biz_exam WHERE del_flag = '0' ORDER BY sort_no
                """);
    }

    /** 已出成绩的场次 */
    public List<Map<String, Object>> publishedExams() {
        return jdbc.queryForList("""
                SELECT exam_id AS examId, exam_name AS name,
                       DATE_FORMAT(exam_date, '%Y-%m-%d') AS date, sort_no AS sortNo
                FROM biz_exam WHERE del_flag = '0' AND score_status = 'PUBLISHED'
                ORDER BY sort_no
                """);
    }

    public List<Map<String, Object>> classes() {
        return jdbc.queryForList("""
                SELECT class_id AS classId, class_name AS className, grade_name AS gradeName,
                       grade_student_count AS gradeStudentCount, head_teacher_id AS headTeacherId
                FROM sys_class WHERE del_flag = '0' ORDER BY sort_no
                """);
    }

    /** 学生档案：含班级名、选科组合与该生实际参加的 6 门科目 */
    public Map<String, Object> studentProfile(String studentId) {
        Map<String, Object> student;
        try {
            student = jdbc.queryForMap("""
                    SELECT s.student_id AS studentId, s.student_name AS name, s.gender,
                           s.class_id AS classId, c.class_name AS className, s.combination,
                           s.primary_subject AS primarySubject, s.secondary_subjects AS secondarySubjects,
                           c.grade_student_count AS gradeStudentCount
                    FROM biz_student s
                    LEFT JOIN sys_class c ON c.class_id = s.class_id
                    WHERE s.student_id = ? AND s.del_flag = '0'
                    """, studentId);
        } catch (Exception e) {
            return null;
        }
        List<Map<String, Object>> subjects = new ArrayList<>();
        for (String id : subjectIdsOfStudent(studentId)) {
            subjects.add(subjectById(id));
        }
        student.put("subjects", subjects);
        student.put("subjectIds", subjectIdsOfStudent(studentId));
        return student;
    }

    /** 某学生实际参加的 6 门科目：3 门必考 + 1 门首选 + 2 门再选 */
    public List<String> subjectIdsOfStudent(String studentId) {
        List<String> ids = new ArrayList<>();
        for (Map<String, Object> row : jdbc.queryForList("""
                SELECT subject_id FROM sys_subject
                WHERE del_flag = '0' AND subject_category = 'REQUIRED' ORDER BY sort_no
                """)) {
            ids.add(String.valueOf(row.get("subject_id")));
        }
        Map<String, Object> s;
        try {
            s = jdbc.queryForMap("""
                    SELECT primary_subject, secondary_subjects FROM biz_student
                    WHERE student_id = ? AND del_flag = '0'
                    """, studentId);
        } catch (Exception e) {
            return ids;
        }
        String primary = s.get("primary_subject") == null ? "" : String.valueOf(s.get("primary_subject"));
        if (!primary.isEmpty()) {
            ids.add(primary);
        }
        String secondary = s.get("secondary_subjects") == null ? "" : String.valueOf(s.get("secondary_subjects"));
        for (String one : secondary.split(",")) {
            String id = one.trim();
            if (!id.isEmpty()) {
                ids.add(id);
            }
        }
        return ids;
    }

    public Map<String, Object> subjectById(String subjectId) {
        List<Map<String, Object>> list = jdbc.queryForList("""
                SELECT subject_id AS subjectId, subject_name AS name, full_score AS fullScore,
                       subject_category AS category
                FROM sys_subject WHERE subject_id = ?
                """, subjectId);
        return list.isEmpty() ? new LinkedHashMap<>() : list.get(0);
    }

    public int fullScoreOf(String subjectId) {
        List<Integer> list = jdbc.queryForList(
                "SELECT full_score FROM sys_subject WHERE subject_id = ?", Integer.class, subjectId);
        return list.isEmpty() ? 100 : list.get(0);
    }

    public String examName(String examId) {
        List<String> list = jdbc.queryForList(
                "SELECT exam_name FROM biz_exam WHERE exam_id = ?", String.class, examId);
        return list.isEmpty() ? examId : list.get(0);
    }

    /** 等级：按得分率四档，与前端 gradeLevel 一致 */
    public static String levelOf(double rate) {
        if (rate >= 0.85) {
            return "A";
        }
        if (rate >= 0.70) {
            return "B";
        }
        if (rate >= 0.60) {
            return "C";
        }
        return "D";
    }
}
