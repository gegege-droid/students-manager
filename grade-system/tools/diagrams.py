# -*- coding: utf-8 -*-
"""
生成详细设计报告所需的架构图、ER 图、状态机、时序图与流程图。
输出目录：report/assets
"""
import os
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.font_manager import FontProperties, fontManager
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch

OUT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "report", "assets")
os.makedirs(OUT, exist_ok=True)

FONT = None
for candidate in [r"C:\Windows\Fonts\msyh.ttc", r"C:\Windows\Fonts\simhei.ttf"]:
    if os.path.exists(candidate):
        fontManager.addfont(candidate)
        FONT = FontProperties(fname=candidate).get_name()
        break
if FONT:
    plt.rcParams["font.sans-serif"] = [FONT]
plt.rcParams["axes.unicode_minus"] = False

PRIMARY = "#2B6CB0"
LIGHT = "#EBF8FF"
GREEN = "#38A169"
GREEN_L = "#F0FFF4"
ORANGE = "#ED8936"
ORANGE_L = "#FFFAF0"
RED = "#E53E3E"
RED_L = "#FFF5F5"
GRAY = "#718096"
GRAY_L = "#F7FAFC"
TEXT = "#1A202C"


def canvas(w, h, xlim=100, ylim=100):
    fig, ax = plt.subplots(figsize=(w, h))
    ax.set_xlim(0, xlim)
    ax.set_ylim(0, ylim)
    ax.axis("off")
    fig.patch.set_facecolor("white")
    return fig, ax


def box(ax, x, y, w, h, text, fc=LIGHT, ec=PRIMARY, fs=10, bold=False, tc=TEXT):
    patch = FancyBboxPatch(
        (x, y), w, h,
        boxstyle="round,pad=0.35,rounding_size=1.2",
        linewidth=1.3, edgecolor=ec, facecolor=fc
    )
    ax.add_patch(patch)
    ax.text(
        x + w / 2, y + h / 2, text,
        ha="center", va="center", fontsize=fs, color=tc,
        fontweight="bold" if bold else "normal", linespacing=1.4
    )


def arrow(ax, p1, p2, text="", color=GRAY, rad=0.0, fs=9):
    patch = FancyArrowPatch(
        p1, p2, arrowstyle="-|>", mutation_scale=13,
        linewidth=1.2, color=color,
        connectionstyle="arc3,rad=%s" % rad
    )
    ax.add_patch(patch)
    if text:
        mx, my = (p1[0] + p2[0]) / 2, (p1[1] + p2[1]) / 2
        ax.text(
            mx, my + 1.0, text, ha="center", va="bottom", fontsize=fs, color=GRAY,
            bbox=dict(boxstyle="round,pad=0.15", fc="white", ec="none", alpha=0.9)
        )


def save(fig, name):
    path = os.path.join(OUT, name)
    fig.savefig(path, dpi=200, bbox_inches="tight", facecolor="white")
    plt.close(fig)
    print("saved", path)


def arch_overview():
    fig, ax = canvas(11, 6.2)
    box(ax, 6, 80, 36, 12, "浏览器（学生 / 教师）\nhttp://localhost:5173", LIGHT, PRIMARY, 11, True)
    box(ax, 58, 80, 36, 12, "浏览器（管理员）\nhttp://localhost:8081", LIGHT, PRIMARY, 11, True)
    box(ax, 15, 48, 70, 15,
        "RuoYi-Vue 后端 :8080\nPortal API ｜ Admin API ｜ ruoyi-generator ｜ Spring Security + JWT",
        GRAY_L, PRIMARY, 11, True)
    box(ax, 5, 14, 26, 13, "MySQL 5.7 :3306\nry-vue / edu_* 表", GREEN_L, GREEN, 10)
    box(ax, 37, 14, 26, 13, "Redis :6379\n登录 token / 配置缓存", ORANGE_L, ORANGE, 10)
    box(ax, 69, 14, 26, 13, "文件与日志\nuploadPath", GRAY_L, GRAY, 10)
    arrow(ax, (24, 80), (34, 63), "HTTP / JSON")
    arrow(ax, (76, 80), (66, 63), "HTTP / JSON")
    arrow(ax, (36, 48), (20, 27), "MyBatis")
    arrow(ax, (50, 48), (50, 27), "Lettuce")
    arrow(ax, (64, 48), (80, 27), "日志")
    save(fig, "arch_overview.png")


def backend_packages():
    fig, ax = canvas(10, 6.2)
    box(ax, 4, 80, 30, 12, "web.controller.portal\nPortalAuth / Option / Score / Appeal", LIGHT, PRIMARY, 9)
    box(ax, 38, 80, 28, 12, "portal.service\nAuth / Option / Score / Appeal", LIGHT, PRIMARY, 9)
    box(ax, 70, 80, 26, 12, "portal.mapper\nMyBatis 接口", LIGHT, PRIMARY, 9)
    box(ax, 70, 52, 26, 12, "mapper/portal/*.xml\n显式列别名", GRAY_L, GRAY, 9)
    box(ax, 38, 52, 28, 12, "system.*（生成代码）\nController / Service / Mapper / Domain", GREEN_L, GREEN, 9)
    box(ax, 4, 52, 30, 12, "common\nBaseController / AjaxResult\nSecurityUtils / GradeCalc", ORANGE_L, ORANGE, 8.5)
    box(ax, 4, 22, 30, 12, "ruoyi-framework\nSecurity / JWT / TokenService", GRAY_L, GRAY, 9)
    box(ax, 38, 22, 28, 12, "MySQL\nedu_* 业务表", GREEN_L, GREEN, 9)
    box(ax, 70, 22, 26, 12, "ruoyi-ui\nVue2 生成页面", RED_L, RED, 9)
    arrow(ax, (19, 80), (38, 74), "调用")
    arrow(ax, (52, 80), (70, 74), "调用")
    arrow(ax, (81, 80), (81, 64))
    arrow(ax, (70, 58), (56, 58), "SQL 结果")
    arrow(ax, (52, 52), (19, 52), "复用")
    arrow(ax, (52, 34), (52, 52))
    arrow(ax, (70, 28), (66, 52), "REST")
    save(fig, "backend_packages.png")


def er_grade():
    fig, ax = canvas(11, 7)
    box(ax, 4, 72, 24, 14, "edu_classroom\nclass_id (PK)\nclass_name\nhead_teacher_id", GREEN_L, GREEN, 9)
    box(ax, 72, 72, 24, 14, "edu_teacher\nteacher_id (PK)\nteacher_name\ntitle", ORANGE_L, ORANGE, 9)
    box(ax, 4, 42, 24, 14, "edu_student\nstudent_id (PK)\nstudent_name\nclass_id", GREEN_L, GREEN, 9)
    box(ax, 72, 42, 24, 14, "edu_course\ncourse_id (PK)\ncourse_name\ncredit / term", ORANGE_L, ORANGE, 9)
    box(ax, 38, 55, 24, 14, "edu_score\nscore_id (PK)\nstudent_id + course_id + term\nusual / final / total / gpa", LIGHT, PRIMARY, 8.5)
    box(ax, 38, 24, 24, 14, "edu_appeal\nappeal_id (PK)\nstudent_id + course_id\nstatus / result / opinion", RED_L, RED, 8.5)
    box(ax, 72, 8, 24, 12, "edu_score_change_log\nlog_id (PK)\nscore_id\nold/new values", GRAY_L, GRAY, 8.5)

    arrow(ax, (16, 72), (16, 56), "1 : n")
    arrow(ax, (72, 79), (28, 79), "head_teacher_id", rad=0.0)
    arrow(ax, (84, 72), (84, 56), "1 : n")
    arrow(ax, (28, 50), (38, 60), "1 : n")
    arrow(ax, (72, 50), (62, 60), "1 : n")
    arrow(ax, (16, 42), (38, 32), "1 : n")
    arrow(ax, (84, 42), (62, 32), "1 : n")
    arrow(ax, (62, 55), (72, 18), "1 : n", rad=-0.1)
    ax.text(50, 4, "说明：不建物理外键；唯一键 (student_id, course_id, term) 保证一人一课程一学期只有一条成绩。",
            ha="center", fontsize=9, color=GRAY)
    save(fig, "er_grade.png")


def appeal_state():
    fig, ax = canvas(10, 5.2)
    box(ax, 8, 40, 24, 14, "PENDING\n待受理", LIGHT, PRIMARY, 12, True)
    box(ax, 55, 62, 28, 14, "ACCEPTED\n已受理", GREEN_L, GREEN, 12, True)
    box(ax, 55, 16, 28, 14, "REJECTED\n已驳回", RED_L, RED, 12, True)
    arrow(ax, (32, 51), (55, 66), "受理 KEEP（维持原成绩）", GREEN)
    arrow(ax, (32, 45), (55, 29), "受理 CORRECT（更正成绩）", GREEN)
    arrow(ax, (32, 40), (55, 23), "驳回（填写理由）", RED)
    ax.text(50, 4, "ACCEPTED / REJECTED 为终态；CORRECT 在同一事务内更新成绩并写入修改记录。",
            ha="center", fontsize=9.5, color=GRAY)
    save(fig, "appeal_state.png")


def score_state():
    fig, ax = canvas(10, 3.8)
    box(ax, 6, 40, 22, 14, "NORMAL\n正常", LIGHT, PRIMARY, 11, True)
    box(ax, 39, 40, 22, 14, "APPEALING\n查分中", ORANGE_L, ORANGE, 11, True)
    box(ax, 72, 40, 22, 14, "MODIFIED\n已修改", GREEN_L, GREEN, 11, True)
    arrow(ax, (28, 50), (39, 50), "提交 PENDING 申请", ORANGE)
    arrow(ax, (61, 50), (72, 50), "受理更正 / 修改", GREEN)
    arrow(ax, (17, 40), (72, 40), "手工修改并留痕", GRAY, rad=0.28)
    ax.text(50, 8, "状态不落库：由 edu_appeal 的 PENDING 申请与 edu_score_change_log 推导。",
            ha="center", fontsize=9.5, color=GRAY)
    save(fig, "score_state.png")


def lifelines(ax, names, top=90, bottom=10, fs=9):
    xs = []
    for i, name in enumerate(names):
        x = 8 + i * (84 / (len(names) - 1))
        box(ax, x - 8, top, 16, 9, name, GRAY_L, GRAY, fs, True)
        ax.plot([x, x], [top - 1, bottom], linestyle="--", color="#CBD5E0", linewidth=1)
        xs.append(x)
    return xs


def seq_appeal_submit():
    fig, ax = canvas(11, 6.4)
    xs = lifelines(ax, ["学生 Vue", "PortalAppeal\nController", "PortalAppeal\nService", "Mapper + XML", "MySQL"])
    levels = [76, 66, 56, 46, 36, 26]
    msgs = [
        "POST /portal/appeals {courseId,reason}",
        "submit(body)",
        "currentUser()：JWT → 学号",
        "selectScoreByStudentCourse",
        "返回成绩（term/totalScore）",
        "countPending = 0 ?",
    ]
    for y, msg in zip(levels, msgs):
        arrow(ax, (xs[0], y), (xs[4], y), msg)
    ax.text(50, 16, "insert edu_appeal(status=PENDING) → 返回申请 VO",
            ha="center", fontsize=9.5, color=PRIMARY)
    save(fig, "seq_appeal_submit.png")


def seq_appeal_handle():
    fig, ax = canvas(11, 7)
    xs = lifelines(ax, ["教师 Vue", "PortalAppeal\nController", "PortalAppeal\nService", "Score/Appeal\nMapper", "MySQL"])
    levels = [78, 69, 60, 51, 42, 33, 24]
    msgs = [
        "PUT /portal/appeals/{id}/handle",
        "handle(id, body)",
        "selectAppealForUpdate（FOR UPDATE）",
        "校验 PENDING + 所带班级 + 意见",
        "updateHandle / selectScoreByKeyForUpdate",
        "insert edu_score_change_log",
        "update edu_score + edu_appeal，提交事务",
    ]
    for y, msg in zip(levels, msgs):
        arrow(ax, (xs[0], y), (xs[4], y), msg)
    ax.text(50, 13, "任一校验失败抛 ServiceException，@Transactional 回滚三表更新。",
            ha="center", fontsize=9.5, color=RED)
    save(fig, "seq_appeal_handle.png")


def flow_score_query():
    fig, ax = canvas(8, 8)
    steps = [
        (72, "请求 GET /portal/scores\n（term / keyword / classId / courseId）"),
        (58, "SecurityUtils 取当前用户\nPortalAuthService 解析角色"),
        (44, "学生：studentId = 学号\n教师：classIds = 所带班级"),
        (30, "动态 SQL：edu_score JOIN 学生 / 班级 / 课程"),
        (16, "CASE 推导 status：APPEALING > MODIFIED > NORMAL"),
    ]
    for y, text in steps:
        box(ax, 14, y, 72, 10, text, LIGHT, PRIMARY, 9.5)
    for (y1, _), (y2, _) in zip(steps, steps[1:]):
        arrow(ax, (50, y1), (50, y2 + 10))
    box(ax, 14, 3, 72, 9, "返回成绩 VO 列表 → Vue 缓存 → 筛选 / 统计 / 分页", GREEN_L, GREEN, 9.5)
    arrow(ax, (50, 16), (50, 12))
    save(fig, "flow_score_query.png")


def flow_batch_save():
    fig, ax = canvas(8.6, 9)
    box(ax, 10, 78, 48, 8, "选择班级 / 课程 / 学期", LIGHT, PRIMARY, 9.5)
    box(ax, 10, 66, 48, 8, "查询班级名单（LEFT JOIN 已录成绩）", LIGHT, PRIMARY, 9.5)
    box(ax, 10, 54, 48, 8, "逐行校验：学号属于班级、成绩 0~100", LIGHT, PRIMARY, 9.5)
    box(ax, 10, 42, 48, 8, "已存在成绩记录？", ORANGE_L, ORANGE, 9.5)
    box(ax, 10, 30, 48, 8, "存在 PENDING 查分申请？", RED_L, RED, 9.5)
    box(ax, 66, 42, 26, 8, "插入新成绩\n计算总评/绩点", GREEN_L, GREEN, 8.5)
    box(ax, 66, 30, 26, 8, "更新成绩\n重算总评/绩点", GREEN_L, GREEN, 8.5)
    box(ax, 66, 16, 26, 8, "拒绝该行\n抛业务异常", RED_L, RED, 8.5)
    box(ax, 10, 16, 48, 8, "返回 savedCount，事务提交", GRAY_L, GRAY, 9.5)
    arrow(ax, (34, 78), (34, 74))
    arrow(ax, (34, 66), (34, 62))
    arrow(ax, (34, 54), (34, 50))
    arrow(ax, (34, 42), (34, 38))
    arrow(ax, (34, 42), (66, 46), "否", GREEN)
    arrow(ax, (34, 30), (66, 34), "否", GREEN)
    arrow(ax, (34, 30), (66, 20), "是", RED, rad=-0.15)
    arrow(ax, (34, 30), (34, 24), "是 → 回滚", RED)
    arrow(ax, (79, 42), (79, 38))
    save(fig, "flow_batch_save.png")


def flow_generator():
    fig, ax = canvas(10, 6)
    box(ax, 4, 78, 26, 11, "MySQL\nedu_* 业务表", GREEN_L, GREEN, 9.5)
    box(ax, 37, 78, 26, 11, "导入 gen_table\n/ gen_table_column", LIGHT, PRIMARY, 9.5)
    box(ax, 70, 78, 26, 11, "编辑元数据\n类名/字段/菜单父级", LIGHT, PRIMARY, 9.5)
    box(ax, 70, 52, 26, 11, "预览 / 下载\nruoyi.zip", ORANGE_L, ORANGE, 9.5)
    box(ax, 37, 52, 26, 11, "复制 Java/XML\nVue2 页面", LIGHT, PRIMARY, 9.5)
    box(ax, 4, 52, 26, 11, "执行菜单 SQL\nparent_id=2000", LIGHT, PRIMARY, 9.5)
    box(ax, 4, 26, 26, 11, "重启后端\n刷新管理端", GREEN_L, GREEN, 9.5)
    box(ax, 37, 26, 26, 11, "管理端 CRUD\n/system/<business>", GREEN_L, GREEN, 9.5)
    box(ax, 70, 26, 26, 11, "详细设计文档\n文件结构/伪代码", GRAY_L, GRAY, 9.5)
    arrow(ax, (30, 83), (37, 83))
    arrow(ax, (63, 83), (70, 83))
    arrow(ax, (83, 78), (83, 63))
    arrow(ax, (70, 57), (63, 57))
    arrow(ax, (37, 57), (30, 57))
    arrow(ax, (17, 52), (17, 37))
    arrow(ax, (30, 31), (37, 31))
    arrow(ax, (63, 31), (70, 31))
    save(fig, "flow_generator.png")


def auth_sequence():
    fig, ax = canvas(11, 5.6)
    xs = lifelines(ax, ["用户端 Vue", "PortalAuth\nController", "PortalAuth\nService", "SysLoginService", "sys_user / JWT"])
    levels = [76, 66, 56, 46, 36, 26]
    msgs = [
        "POST /portal/auth/login {username,password}",
        "login(body)",
        "loadIdentity(username)：edu_student / edu_teacher",
        "login(username,password,null,null)",
        "BCrypt 校验 + 生成 LoginUser",
        "createToken → JWT；返回 token + user",
    ]
    for y, msg in zip(levels, msgs):
        arrow(ax, (xs[0], y), (xs[4], y), msg)
    ax.text(50, 15, "后续请求：Authorization: Bearer <token> → JwtAuthenticationTokenFilter",
            ha="center", fontsize=9.5, color=PRIMARY)
    save(fig, "auth_sequence.png")


def main():
    arch_overview()
    backend_packages()
    er_grade()
    appeal_state()
    score_state()
    seq_appeal_submit()
    seq_appeal_handle()
    flow_score_query()
    flow_batch_save()
    flow_generator()
    auth_sequence()


if __name__ == "__main__":
    main()
