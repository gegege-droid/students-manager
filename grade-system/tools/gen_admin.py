# -*- coding: utf-8 -*-
"""
调用 RuoYi ruoyi-generator 完成：
  1) 登录 admin
  2) 导入 edu_* 表（已导入则跳过）
  3) 编辑生成配置：菜单父级 2000、列的 list/query/edit/insert 标记
  4) 下载生成代码 zip 并解压

用法：
  python tools/gen_admin.py
默认环境：http://localhost:8080，admin/admin123，父菜单 2000
"""
import argparse
import io
import json
import os
import sys
import urllib.parse
import urllib.request
import zipfile

TABLES = [
    "edu_score",
    "edu_appeal",
    "edu_course",
    "edu_student",
    "edu_teacher",
    "edu_classroom",
    "edu_score_change_log",
]

FUNCTION_NAMES = {
    "edu_score": "成绩管理",
    "edu_appeal": "查分申请管理",
    "edu_course": "课程管理",
    "edu_student": "学生管理",
    "edu_teacher": "教师管理",
    "edu_classroom": "班级管理",
    "edu_score_change_log": "成绩修改记录",
}

AUDIT = {"create_by", "create_time", "update_by", "update_time", "remark"}

NO_EDIT = {
    "edu_score": {"total_score", "gpa", "manual_total", "recorder_id", "record_time"},
    "edu_appeal": {"status", "result", "corrected_score", "opinion", "handler_id", "handle_time"},
    "edu_score_change_log": {"change_reason", "operator_id", "operate_time", "source"},
}

NO_INSERT = {
    "edu_score": {"total_score", "gpa", "manual_total", "recorder_id", "record_time"},
    "edu_appeal": {"status", "result", "corrected_score", "opinion", "handler_id", "handle_time"},
}

QUERY = {
    "edu_score": {"student_id", "course_id", "term"},
    "edu_appeal": {"student_id", "course_id", "term", "status"},
    "edu_course": {"course_name", "term", "teacher_id"},
    "edu_student": {"student_id", "student_name", "class_id"},
    "edu_teacher": {"teacher_id", "teacher_name"},
    "edu_classroom": {"class_id", "class_name"},
    "edu_score_change_log": {"student_id", "course_id", "term"},
}

REQUIRED = {
    "edu_student": {"student_id", "student_name", "class_id"},
    "edu_teacher": {"teacher_id", "teacher_name"},
    "edu_classroom": {"class_id", "class_name"},
    "edu_course": {"course_id", "course_name", "credit", "term", "teacher_id"},
    "edu_score": {"score_id", "student_id", "course_id", "term"},
    "edu_appeal": {"appeal_id", "student_id", "course_id", "term", "reason", "apply_time", "status"},
    "edu_score_change_log": {
        "log_id", "score_id", "student_id", "course_id", "term",
        "change_reason", "operator_id", "operate_time", "source",
    },
}

TEXTAREA = {"reason", "opinion", "change_reason", "remark"}
DATETIME = {"apply_time", "handle_time", "record_time", "operate_time", "create_time", "update_time"}


class Client:
    def __init__(self, base):
        self.base = base.rstrip("/")
        self.token = None

    def request(self, path, method="GET", data=None, raw=False):
        headers = {"User-Agent": "grade-system-generator"}
        body = None
        if data is not None:
            body = json.dumps(data, ensure_ascii=False).encode("utf-8")
            headers["Content-Type"] = "application/json; charset=utf-8"
        if self.token:
            headers["Authorization"] = "Bearer " + self.token
        req = urllib.request.Request(self.base + path, data=body, headers=headers, method=method)
        with urllib.request.urlopen(req, timeout=120) as resp:
            content = resp.read()
        if raw:
            return content
        return json.loads(content.decode("utf-8"))

    def login(self, username, password):
        r = self.request("/login", "POST", {"username": username, "password": password})
        if r.get("code") != 200 or not r.get("token"):
            raise RuntimeError("登录失败: %s" % r)
        self.token = r["token"]


def get_table_list(client):
    r = client.request("/tool/gen/list?pageNum=1&pageSize=100")
    if r.get("code") != 200:
        raise RuntimeError("获取生成表失败: %s" % r)
    return {row["tableName"]: row for row in r.get("rows", [])}


def import_tables(client, tables):
    exist = get_table_list(client)
    missing = [t for t in tables if t not in exist]
    if missing:
        q = urllib.parse.quote(",".join(missing))
        r = client.request("/tool/gen/importTable?tables=" + q, "POST")
        if r.get("code") != 200:
            raise RuntimeError("导入表失败: %s" % r)
    return get_table_list(client)


def configure_columns(table_name, columns):
    no_edit = NO_EDIT.get(table_name, set())
    no_insert = NO_INSERT.get(table_name, set())
    query = QUERY.get(table_name, set())
    required = REQUIRED.get(table_name, set())
    for col in columns:
        name = col["columnName"]
        pk = str(col.get("isPk")) == "1" or col.get("pk")
        audit = name in AUDIT or col.get("superColumn")

        col["isList"] = "0" if audit else "1"
        col["list"] = col["isList"] == "1"
        col["isQuery"] = "1" if (name in query and not audit) else "0"
        col["query"] = col["isQuery"] == "1"
        col["isEdit"] = "0" if (pk or audit or name in no_edit) else "1"
        col["edit"] = col["isEdit"] == "1"
        col["isInsert"] = "0" if (pk or audit or name in no_insert) else "1"
        col["insert"] = col["isInsert"] == "1"
        col["isRequired"] = "1" if name in required else "0"
        col["required"] = col["isRequired"] == "1"
        if name in TEXTAREA:
            col["htmlType"] = "textarea"
        elif name in DATETIME:
            col["htmlType"] = "datetime"
        elif col.get("htmlType") in (None, ""):
            col["htmlType"] = "input"
        if not col.get("queryType"):
            col["queryType"] = "EQ"
    return columns


def configure_table(client, table_row):
    table_name = table_row["tableName"]
    detail = client.request("/tool/gen/%s" % table_row["tableId"])
    data = detail["data"]
    info = data["info"]
    columns = data["rows"]

    info["functionName"] = FUNCTION_NAMES.get(table_name, info.get("functionName") or table_name)
    info["parentMenuId"] = 2000
    info["params"] = {
        "parentMenuId": "2000",
        "treeCode": "",
        "treeName": "",
        "treeParentCode": "",
    }
    info["options"] = json.dumps(info["params"], ensure_ascii=False)
    info["columns"] = configure_columns(table_name, columns)

    r = client.request("/tool/gen", "PUT", info)
    if r.get("code") != 200:
        raise RuntimeError("更新生成配置失败 %s: %s" % (table_name, r))
    print("[config] %-24s %s" % (table_name, info["functionName"]))


def download(client, tables, out_dir):
    os.makedirs(out_dir, exist_ok=True)
    q = urllib.parse.quote(",".join(tables))
    content = client.request("/tool/gen/batchGenCode?tables=" + q, raw=True)
    zip_path = os.path.join(out_dir, "ruoyi.zip")
    with open(zip_path, "wb") as f:
        f.write(content)
    unzip_dir = os.path.join(out_dir, "unzipped")
    os.makedirs(unzip_dir, exist_ok=True)
    with zipfile.ZipFile(io.BytesIO(content)) as zf:
        zf.extractall(unzip_dir)
        names = zf.namelist()
    print("[download] %s (%d files)" % (zip_path, len(names)))
    for name in sorted(names):
        print("  ", name)
    return zip_path, unzip_dir


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base", default="http://localhost:8080")
    parser.add_argument("--user", default="admin")
    parser.add_argument("--password", default="admin123")
    parser.add_argument("--out", default=os.path.join(os.path.dirname(os.path.dirname(__file__)), "generated"))
    args = parser.parse_args()

    client = Client(args.base)
    client.login(args.user, args.password)
    tables = import_tables(client, TABLES)
    print("[import] tables:", ", ".join(sorted(tables)))
    for name in TABLES:
        if name not in tables:
            raise RuntimeError("缺少生成表: " + name)
        configure_table(client, tables[name])
    download(client, TABLES, args.out)


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print("[ERROR]", exc, file=sys.stderr)
        sys.exit(1)
