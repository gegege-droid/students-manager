package com.grade.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 高考成绩预测服务：「加权移动平均 + 趋势修正」。
 * 算法与前端 utils/score.js 的 predictSubject 完全一致（见《详细设计说明书》§9.7），
 * 参数通过 application.yml 的 grade.predict.* 配置。
 */
@Service
public class PredictService {

    private final JdbcTemplate jdbc;
    private final MetaService meta;

    @Value("${grade.predict.gap:2}")
    private int gap;
    @Value("${grade.predict.damping:0.5}")
    private double damping;
    @Value("${grade.predict.recent-count:3}")
    private int recentCount;
    @Value("${grade.predict.min-rate:0.3}")
    private double minRate;
    @Value("${grade.predict.max-rate:1.0}")
    private double maxRate;

    public PredictService(JdbcTemplate jdbc, MetaService meta) {
        this.jdbc = jdbc;
        this.meta = meta;
    }

    /** 单科预测结果；无有效成绩返回 null */
    public Map<String, Object> predictSubject(List<Integer> scoresInOrder, int fullScore) {
        List<Integer> valid = new ArrayList<>();
        for (Integer s : scoresInOrder) {
            if (s != null) {
                valid.add(s);
            }
        }
        if (valid.isEmpty() || fullScore <= 0) {
            return null;
        }
        int n = valid.size();
        double[] rates = new double[n];
        for (int i = 0; i < n; i++) {
            rates[i] = valid.get(i) / (double) fullScore;
        }

        // ② 加权移动平均：权重 1,2,3…
        double weightSum = 0;
        double weighted = 0;
        for (int i = 0; i < n; i++) {
            double w = i + 1;
            weightSum += w;
            weighted += rates[i] * w;
        }
        double baseRate = weighted / weightSum;

        // ③ 趋势修正：最近 recentCount 次的最小二乘斜率，外推 gap 次后乘阻尼
        int from = Math.max(0, n - recentCount);
        double slope = linearSlope(rates, from, n);
        double predictedRate = clamp(baseRate + slope * gap * damping, minRate, maxRate);

        // ④ 预测分与区间
        double mean = 0;
        for (double r : rates) {
            mean += r;
        }
        mean /= n;
        double var = 0;
        for (double r : rates) {
            var += (r - mean) * (r - mean);
        }
        double sd = Math.sqrt(var / n);
        int predicted = (int) Math.round(predictedRate * fullScore);
        int margin = Math.max((int) Math.round(1.5 * sd * fullScore),
                (int) Math.round(fullScore * 0.02));
        int low = Math.max(0, Math.min(fullScore, predicted - margin));
        int high = Math.max(0, Math.min(fullScore, predicted + margin));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fullScore", fullScore);
        result.put("count", n);
        result.put("latest", valid.get(n - 1));
        result.put("baseRate", ScoreService.round(baseRate, 4));
        result.put("predictedRate", ScoreService.round(predictedRate, 4));
        result.put("slope", ScoreService.round(slope, 6));
        result.put("sd", ScoreService.round(sd, 6));
        result.put("predicted", predicted);
        result.put("low", low);
        result.put("high", high);
        result.put("deltaPerExam", ScoreService.round(slope * fullScore, 1));
        result.put("confidence", confidenceOf(n, sd));
        return result;
    }

    /** 最小二乘斜率：x 取序号 0..n-1 */
    public double linearSlope(double[] values, int from, int to) {
        int n = to - from;
        if (n < 2) {
            return 0;
        }
        double meanX = (n - 1) / 2.0;
        double meanY = 0;
        for (int i = from; i < to; i++) {
            meanY += values[i];
        }
        meanY /= n;
        double num = 0;
        double den = 0;
        for (int i = 0; i < n; i++) {
            num += (i - meanX) * (values[from + i] - meanY);
            den += (i - meanX) * (i - meanX);
        }
        return den == 0 ? 0 : num / den;
    }

    /** 置信度：场次越多、波动越小越可信 */
    public String confidenceOf(int count, double sd) {
        if (count >= 5 && sd <= 0.05) {
            return "HIGH";
        }
        if (count >= 3 && sd <= 0.09) {
            return "MEDIUM";
        }
        return "LOW";
    }

    /**
     * 学生整体预测：逐科预测后汇总总分与区间，整体置信度取各科最低档。
     */
    public Map<String, Object> predictStudent(String studentId) {
        List<Map<String, Object>> sessions = meta.publishedExams();
        List<Map<String, Object>> subjects = new ArrayList<>();
        int predictedTotal = 0;
        int lowTotal = 0;
        int highTotal = 0;
        int fullTotal = 0;
        boolean low = false;
        boolean medium = false;

        for (String sid : meta.subjectIdsOfStudent(studentId)) {
            Map<String, Object> subject = meta.subjectById(sid);
            int full = meta.fullScoreOf(sid);
            List<Map<String, Object>> records = new ArrayList<>();
            List<Integer> scores = new ArrayList<>();
            for (Map<String, Object> session : sessions) {
                String examId = String.valueOf(session.get("examId"));
                Integer score = null;
                try {
                    score = jdbc.queryForObject("""
                            SELECT score FROM biz_score
                            WHERE student_id = ? AND exam_id = ? AND subject_id = ? AND del_flag = '0'
                            """, Integer.class, studentId, examId, sid);
                } catch (Exception ignore) {
                    score = null;
                }
                Map<String, Object> rec = new LinkedHashMap<>();
                rec.put("examId", examId);
                rec.put("examName", session.get("name"));
                rec.put("score", score);
                records.add(rec);
                scores.add(score);
            }
            Map<String, Object> prediction = predictSubject(scores, full);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("subjectId", sid);
            row.put("name", subject.get("name"));
            row.put("category", subject.get("category"));
            row.put("records", records);
            if (prediction == null) {
                row.put("count", 0);
                row.put("latest", null);
                row.put("predicted", null);
                row.put("low", null);
                row.put("high", null);
                row.put("baseRate", null);
                row.put("predictedRate", null);
                row.put("deltaPerExam", 0);
                row.put("confidence", "LOW");
                low = true;
            } else {
                row.putAll(prediction);
                predictedTotal += (int) prediction.get("predicted");
                lowTotal += (int) prediction.get("low");
                highTotal += (int) prediction.get("high");
                fullTotal += (int) prediction.get("fullScore");
                String conf = String.valueOf(prediction.get("confidence"));
                if ("LOW".equals(conf)) {
                    low = true;
                } else if ("MEDIUM".equals(conf)) {
                    medium = true;
                }
            }
            subjects.add(row);
        }

        Map<String, Object> total = new LinkedHashMap<>();
        total.put("predicted", predictedTotal);
        total.put("low", lowTotal);
        total.put("high", highTotal);
        total.put("fullScore", fullTotal == 0 ? 750 : fullTotal);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("studentId", studentId);
        data.put("student", meta.studentProfile(studentId));
        data.put("subjects", subjects);
        data.put("total", total);
        data.put("sessionCount", sessions.size());
        data.put("confidence", low ? "LOW" : (medium ? "MEDIUM" : "HIGH"));
        data.put("config", Map.of("gap", gap, "damping", damping, "recentCount", recentCount,
                "minRate", minRate, "maxRate", maxRate));
        return data;
    }

    private double clamp(double v, double min, double max) {
        return Math.min(max, Math.max(min, v));
    }
}
