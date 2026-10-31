package com.ruoyi.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 成绩计算工具（公共模块）。
 *
 * 规则与实验一前端 utils/score.js 保持一致：
 *   总评 = round(平时 * 0.4 + 期末 * 0.6)，任一项为空则总评为空；
 *   绩点 = 总评 < 60 ? 0 : (总评 - 50) / 10，保留 1 位小数。
 */
public final class GradeCalc
{
    private static final BigDecimal USUAL_WEIGHT = new BigDecimal("0.4");
    private static final BigDecimal FINAL_WEIGHT = new BigDecimal("0.6");

    private GradeCalc()
    {
    }

    public static Integer total(Integer usualScore, Integer finalScore)
    {
        if (usualScore == null || finalScore == null)
        {
            return null;
        }
        BigDecimal value = BigDecimal.valueOf(usualScore).multiply(USUAL_WEIGHT)
                .add(BigDecimal.valueOf(finalScore).multiply(FINAL_WEIGHT));
        return value.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    public static BigDecimal gpa(Integer totalScore)
    {
        if (totalScore == null)
        {
            return null;
        }
        if (totalScore < 60)
        {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(totalScore - 50)
                .divide(BigDecimal.TEN, 1, RoundingMode.HALF_UP);
    }
}
