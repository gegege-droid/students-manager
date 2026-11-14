# -*- coding: utf-8 -*-
"""
成绩管理系统用户侧 API 冒烟测试。

前置：后端 8080 已启动；数据库已执行 db/00_grade_system_all.sql。
运行：python tools/smoke_test.py
注意：本脚本会写入测试数据；运行结束后可执行 db/02_seed_grade.sql 恢复。
"""
import json
import sys
import urllib.error
import urllib.request

BASE = "http://localhost:8080"
passed = 0
failed = 0


def call(path, method="GET", body=None, token=None):
    headers = {"User-Agent": "smoke-test"}
    data = None
    if body is not None:
        data = json.dumps(body, ensure_ascii=False).encode("utf-8")
        headers["Content-Type"] = "application/json; charset=utf-8"
    if token:
        headers["Authorization"] = "Bearer " + token
    req = urllib.request.Request(BASE + path, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            return resp.status, json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as exc:
        try:
            return exc.code, json.loads(exc.read().decode("utf-8", "replace"))
        except Exception:
            return exc.code, {"code": exc.code, "msg": "HTTP error"}


def login(username, password, portal=False):
    path = "/portal/auth/login" if portal else "/login"
    _, r = call(path, "POST", {"username": username, "password": password})
    assert r.get("code") == 200, r
    return r["data"]["token"] if portal else r["token"]


def check(name, condition, detail=""):
    global passed, failed
    if condition:
        passed += 1
        print("[PASS] %s" % name)
    else:
        failed += 1
        print("[FAIL] %s %s" % (name, detail))


def data_of(response):
    return response[1].get("data")


def main():
    # 管理端 + 生成器
    admin = login("admin", "admin123")
    _, r = call("/tool/gen/list?pageNum=1&pageSize=100", token=admin)
    names = [row["tableName"] for row in r.get("rows", [])]
    check("代码生成器已导入 edu_score", "edu_score" in names, names)

    # 学生登录
    student = login("2021001", "123456", portal=True)
    _, r = call("/portal/auth/profile", token=student)
    user = r.get("data") or {}
    check("学生身份", user.get("role") == "STUDENT" and user.get("name") == "张三", user)

    _, r = call("/portal/scores", token=student)
    rows = r.get("data") or []
    check("学生成绩条数=9", len(rows) == 9, len(rows))
    status = {row["courseId"]: row["status"] for row in rows}
    check("HIS101 查分中", status.get("HIS101") == "APPEALING", status.get("HIS101"))
    check("CHN101 已修改", status.get("CHN101") == "MODIFIED", status.get("CHN101"))

    _, r = call("/portal/appeals", "POST",
                {"courseId": "HIS101", "reason": "平时分已按要求提交全部作业，请老师复核登分情况"}, student)
    check("重复申请被拒绝", r.get("code") != 200, r.get("msg"))

    _, r = call("/portal/appeals", "POST",
                {"courseId": "MAT101", "reason": "期末卷面估分与公布成绩差距较大，请复核"}, student)
    new_appeal = r.get("data") or {}
    check("提交 MAT101 申请成功", r.get("code") == 200 and new_appeal.get("status") == "PENDING", r)

    # 教师
    teacher = login("T001", "123456", portal=True)
    _, r = call("/portal/appeals?status=PENDING", token=teacher)
    pending = r.get("data") or []
    check("教师待办 >= 4", len(pending) >= 4, len(pending))

    _, r = call("/portal/appeals/AP20250115001/handle", "PUT",
                {"result": "CORRECT", "correctedScore": 56,
                 "opinion": "已复核，平时分漏计一次作业，更正为56分。"}, teacher)
    handled = r.get("data") or {}
    check("受理更正成功", r.get("code") == 200 and handled.get("status") == "ACCEPTED", r.get("msg"))

    _, r = call("/portal/scores?courseId=HIS101", token=teacher)
    his = [x for x in (r.get("data") or []) if x["studentId"] == "2021001"]
    check("HIS101 总评=56", his and his[0]["totalScore"] == 56, his)
    check("HIS101 绩点=0.0", his and float(his[0]["gpa"]) == 0.0, his and his[0]["gpa"])

    _, r = call("/portal/scores/SC20250101008/logs", token=teacher)
    check("生成 APPEAL 修改记录", len(r.get("data") or []) >= 1, r.get("data"))

    _, r = call("/portal/appeals/AP20250115002/reject", "PUT",
                {"opinion": "已复核答卷，计分无误，驳回申请。"}, teacher)
    check("驳回成功", r.get("code") == 200 and (r.get("data") or {}).get("status") == "REJECTED", r)

    _, r = call("/portal/scores/batch", "POST", {
        "classId": "G2101", "courseId": "CHN101", "term": "2024-2025-1",
        "rows": [
            {"studentId": "2021002", "usualScore": 80, "finalScore": 85},
            {"studentId": "2021004", "usualScore": 70, "finalScore": 75},
        ]
    }, teacher)
    check("批量录入 2 条", r.get("code") == 200 and (r.get("data") or {}).get("savedCount") == 2, r)

    _, r = call("/portal/scores/roster?classId=G2101&courseId=CHN101&term=2024-2025-1", token=teacher)
    roster = r.get("data") or []
    check("名单已录入 4 人", sum(1 for x in roster if x.get("recorded")) == 4,
          [(x["studentId"], x["recorded"]) for x in roster])

    _, r = call("/portal/scores?courseId=PHY101", token=teacher)
    phy = [x for x in (r.get("data") or []) if x["studentId"] == "2021005"]
    check("2021005 物理查分中", phy and phy[0]["status"] == "APPEALING", phy)
    if phy:
        _, r = call("/portal/scores/" + phy[0]["scoreId"], "PUT",
                    {"usualScore": 60, "finalScore": 66, "reason": "测试查分锁定"}, teacher)
        check("查分中禁止修改", r.get("code") != 200, r.get("msg"))

    print("\n结果：%d passed, %d failed" % (passed, failed))
    return 1 if failed else 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except Exception as exc:
        print("[ERROR]", repr(exc))
        sys.exit(2)
