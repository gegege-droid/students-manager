# -*- coding: utf-8 -*-
"""
生成《实验三 详细设计报告》docx。
运行：python tools/build_report.py
随后运行 tools/postprocess_docx.py 更新目录、导出 PDF、统计页数。
"""
import os
from docx import Document
from docx.shared import Pt, Cm, Inches, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_LINE_SPACING
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.section import WD_SECTION
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
from docx.enum.text import WD_BREAK

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "report", "assets")
OUT = os.path.join(ROOT, "report", "实验三_详细设计报告.docx")

BODY_FONT = "宋体"
HEAD_FONT = "黑体"
CODE_FONT = "Consolas"


def set_run_font(run, name=BODY_FONT, size=12, bold=False, color=None):
    run.font.name = name
    run._element.get_or_add_rPr().rFonts.set(qn("w:eastAsia"), name)
    run.font.size = Pt(size)
    run.bold = bold
    if color:
        run.font.color.rgb = RGBColor(*color)


def style_document(doc):
    section = doc.sections[0]
    section.page_width = Cm(21.0)
    section.page_height = Cm(29.7)
    section.top_margin = Cm(2.54)
    section.bottom_margin = Cm(2.54)
    section.left_margin = Cm(3.0)
    section.right_margin = Cm(2.6)

    normal = doc.styles["Normal"]
    normal.font.name = BODY_FONT
    normal._element.rPr.rFonts.set(qn("w:eastAsia"), BODY_FONT)
    normal.font.size = Pt(12)
    normal.paragraph_format.line_spacing = 1.5
    normal.paragraph_format.first_line_indent = Pt(24)
    normal.paragraph_format.space_after = Pt(0)
    normal.paragraph_format.space_before = Pt(0)

    for name, size in [("Heading 1", 16), ("Heading 2", 14), ("Heading 3", 12)]:
        st = doc.styles[name]
        st.font.name = HEAD_FONT
        st._element.rPr.rFonts.set(qn("w:eastAsia"), HEAD_FONT)
        st.font.size = Pt(size)
        st.font.bold = True
        st.font.color.rgb = RGBColor(0, 0, 0)
        st.paragraph_format.first_line_indent = None
        st.paragraph_format.line_spacing = 1.2
        st.paragraph_format.space_before = Pt(10 if name == "Heading 1" else 6)
        st.paragraph_format.space_after = Pt(6)

    # 页脚页码
    footer = section.footer.paragraphs[0]
    footer.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = footer.add_run("第 ")
    set_run_font(run, BODY_FONT, 9)
    add_field(footer, "PAGE")
    run = footer.add_run(" 页")
    set_run_font(run, BODY_FONT, 9)


def add_field(paragraph, code):
    run = paragraph.add_run()
    fld_begin = OxmlElement("w:fldChar")
    fld_begin.set(qn("w:fldCharType"), "begin")
    instr = OxmlElement("w:instrText")
    instr.set(qn("xml:space"), "preserve")
    instr.text = code
    fld_sep = OxmlElement("w:fldChar")
    fld_sep.set(qn("w:fldCharType"), "separate")
    fld_text = OxmlElement("w:t")
    fld_text.text = "1"
    fld_end = OxmlElement("w:fldChar")
    fld_end.set(qn("w:fldCharType"), "end")
    run._r.append(fld_begin)
    run._r.append(instr)
    run._r.append(fld_sep)
    run._r.append(fld_text)
    run._r.append(fld_end)


def add_toc(doc):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    run = p.add_run()
    fld_begin = OxmlElement("w:fldChar")
    fld_begin.set(qn("w:fldCharType"), "begin")
    instr = OxmlElement("w:instrText")
    instr.set(qn("xml:space"), "preserve")
    instr.text = 'TOC \\o "1-3" \\h \\z \\u'
    fld_sep = OxmlElement("w:fldChar")
    fld_sep.set(qn("w:fldCharType"), "separate")
    fld_text = OxmlElement("w:t")
    fld_text.text = "（在 Word 中右键“更新域”生成目录）"
    fld_end = OxmlElement("w:fldChar")
    fld_end.set(qn("w:fldCharType"), "end")
    run._r.append(fld_begin)
    run._r.append(instr)
    run._r.append(fld_sep)
    run._r.append(fld_text)
    run._r.append(fld_end)


def P(doc, text, indent=True, bold=False, size=12, align=None, space_after=0):
    p = doc.add_paragraph()
    if not indent:
        p.paragraph_format.first_line_indent = None
    if align is not None:
        p.alignment = align
    p.paragraph_format.space_after = Pt(space_after)
    run = p.add_run(text)
    set_run_font(run, BODY_FONT, size, bold)
    return p


def H(doc, text, level=1):
    h = doc.add_heading(text, level=level)
    for run in h.runs:
        set_run_font(run, HEAD_FONT, 16 if level == 1 else (14 if level == 2 else 12), True)
    return h


def bullets(doc, items):
    for item in items:
        p = doc.add_paragraph(style="List Bullet")
        p.paragraph_format.first_line_indent = None
        p.paragraph_format.line_spacing = 1.5
        run = p.add_run(item)
        set_run_font(run, BODY_FONT, 12)


def add_caption(doc, text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.first_line_indent = None
    p.paragraph_format.space_before = Pt(3)
    p.paragraph_format.space_after = Pt(8)
    run = p.add_run(text)
    set_run_font(run, BODY_FONT, 10.5)
    run.italic = False


def figure(doc, name, caption, width=6.3):
    path = os.path.join(ASSETS, name)
    if not os.path.exists(path):
        P(doc, "（缺少图：%s）" % name, indent=False)
        return
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.first_line_indent = None
    run = p.add_run()
    run.add_picture(path, width=Inches(width))
    add_caption(doc, caption)


def shade_cell(cell, color="D9EAF7"):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:fill"), color)
    tcPr.append(shd)


def table(doc, headers, rows, caption=None, widths=None, font_size=9):
    if caption:
        add_caption(doc, caption)
    t = doc.add_table(rows=1, cols=len(headers))
    t.style = "Table Grid"
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    t.autofit = True
    for i, h in enumerate(headers):
        cell = t.rows[0].cells[i]
        cell.text = ""
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.first_line_indent = None
        run = p.add_run(h)
        set_run_font(run, BODY_FONT, font_size, True)
        shade_cell(cell)
    for row in rows:
        cells = t.add_row().cells
        for i, value in enumerate(row):
            cells[i].text = ""
            p = cells[i].paragraphs[0]
            p.paragraph_format.first_line_indent = None
            p.paragraph_format.line_spacing = 1.15
            run = p.add_run(str(value))
            set_run_font(run, BODY_FONT, font_size)
    if widths:
        for i, w in enumerate(widths):
            for row in t.rows:
                row.cells[i].width = Cm(w)
    doc.add_paragraph().paragraph_format.space_after = Pt(2)
    return t


def code(doc, text, caption=None):
    if caption:
        add_caption(doc, caption)
    p = doc.add_paragraph()
    p.paragraph_format.first_line_indent = None
    p.paragraph_format.line_spacing = 1.0
    p.paragraph_format.left_indent = Cm(0.6)
    p.paragraph_format.space_after = Pt(6)
    pPr = p._p.get_or_add_pPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:fill"), "F2F2F2")
    pPr.append(shd)
    run = p.add_run(text.rstrip())
    set_run_font(run, CODE_FONT, 9)


def cover(doc):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.first_line_indent = None
    p.paragraph_format.space_before = Pt(80)
    run = p.add_run("软件工程实验报告三")
    set_run_font(run, HEAD_FONT, 26, True)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.first_line_indent = None
    p.paragraph_format.space_before = Pt(28)
    run = p.add_run("实验名称：软件项目详细设计报告撰写")
    set_run_font(run, BODY_FONT, 16)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.first_line_indent = None
    p.paragraph_format.space_before = Pt(10)
    run = p.add_run("项目名称：成绩管理系统")
    set_run_font(run, BODY_FONT, 16)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.first_line_indent = None
    p.paragraph_format.space_before = Pt(26)
    run = p.add_run("项目组成员")
    set_run_font(run, HEAD_FONT, 14, True)

    t = doc.add_table(rows=1, cols=5)
    t.style = "Table Grid"
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    headers = ["姓名", "学号", "角色", "工作内容", "评分"]
    for i, h in enumerate(headers):
        cell = t.rows[0].cells[i]
        cell.text = ""
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.first_line_indent = None
        run = p.add_run(h)
        set_run_font(run, BODY_FONT, 10.5, True)
        shade_cell(cell)
    roles = ["组长/后端", "数据库/测试", "前端/联调", "文档/评审", "（待补充）", "（待补充）", "（待补充）"]
    works = ["总体设计、Portal API", "数据库与生成器", "Vue 用户端", "详细设计文档", "", "", ""]
    for i in range(7):
        cells = t.add_row().cells
        values = ["（待填写）", "（待填写）", roles[i], works[i], ""]
        for j, value in enumerate(values):
            cells[j].text = ""
            p = cells[j].paragraphs[0]
            p.paragraph_format.first_line_indent = None
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER if j in (0, 1, 2, 4) else WD_ALIGN_PARAGRAPH.LEFT
            run = p.add_run(value)
            set_run_font(run, BODY_FONT, 10.5)
    for row in t.rows:
        for i, w in enumerate([2.2, 3.2, 2.6, 5.2, 1.6]):
            row.cells[i].width = Cm(w)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.first_line_indent = None
    p.paragraph_format.space_before = Pt(36)
    run = p.add_run("2026 年 10 月")
    set_run_font(run, BODY_FONT, 14)

    doc.add_page_break()


def intro(doc):
    H(doc, "1 引言", 1)
    H(doc, "1.1 编写目的", 2)
    P(doc, "本文档是成绩管理系统的详细设计规格说明书。它把实验二《概要设计》中的模块划分进一步下沉到类、方法、SQL 和前端组件级别，说明每个模块的实现文件结构、方法调用关系与具体实现过程，使 C++、C# 或 Java 程序员能够依据本文档独立完成编码或维护。")
    P(doc, "本文档同时作为实验三的交付物：实验三要求在 RuoYi-Vue 基础上使用 ruoyi-generator 完成管理端开发，并选择至少 3 个模块进行详细设计；用户侧应用前后端联调属于加分项，本文档也将其纳入设计范围。")

    H(doc, "1.2 范围", 2)
    P(doc, "设计范围包括：用户登录与权限、成绩查询、成绩录入与修改、查分申请与受理、后台成绩管理（代码生成）五个模块。系统边界为：学生查看本人成绩并提交查分；教师维护所带班级成绩并受理查分；管理员通过 RuoYi 管理端维护基础数据。注册、找回密码、短信通知、附件上传、多级审批不在本期范围。")

    H(doc, "1.3 术语与缩写", 2)
    table(doc, ["术语", "说明"], [
        ["RuoYi-Vue", "基于 Spring Boot + Vue 的开源快速开发框架，本系统后端基座"],
        ["Portal", "面向学生/教师的用户侧接口前缀 /portal"],
        ["JWT", "JSON Web Token，RuoYi 用于无状态登录认证"],
        ["CRUD", "Create / Read / Update / Delete，代码生成器生成的标准管理功能"],
        ["PENDING", "查分申请待受理状态"],
        ["APPEALING", "成绩因存在待受理查分申请而锁定"],
        ["VO", "View Object，本系统用 Map 或领域对象返回给前端的数据结构"],
    ], caption="表 1-1 术语与缩写", widths=[3.5, 11.0])

    H(doc, "1.4 参考资料", 2)
    bullets(doc, [
        "《软件工程实验报告三》实验要求 PPT；",
        "《网上招聘系统详细设计》样例（详细设计样例参考.doc）；",
        "RuoYi-Vue v3.9.1 官方源码与文档：https://doc.ruoyi.vip/ruoyi-vue/；",
        "实验一《成绩管理系统需求规格说明书》与 Vue3 前端实现；",
        "MySQL 5.7、Spring Boot 2.5.15、Vue 3、Vite 文档。",
    ])

    H(doc, "1.5 文档约定", 2)
    P(doc, "本文档中的伪代码采用 Java 风格；以“……”表示省略的实现细节。文件路径以实验三交付目录 grade-system 为根。图、表分别按“图 章-序号”“表 章-序号”编号。代码块中的类名、方法名与源码保持一致。")

    doc.add_page_break()


def overview(doc):
    H(doc, "2 系统设计概述", 1)
    H(doc, "2.1 系统目标与范围", 2)
    P(doc, "成绩管理系统服务于高中期末成绩管理。系统分为用户侧与管理端：用户侧面向学生和班主任，管理端面向系统管理员。用户侧复用实验一的 6 个 Vue3 页面，通过 /portal 接口访问真实后端；管理端使用 RuoYi 内置的 Vue2 页面，承载 ruoyi-generator 生成的成绩、查分申请、课程、学生、教师、班级六个 CRUD 模块。")
    P(doc, "系统需要解决三个核心问题：一是成绩数据只能由教师录入、修改且必须留痕；二是学生查分申请必须有明确的状态与处理结果；三是成绩、申请、修改记录三张表在受理更正时必须保持一致。")

    H(doc, "2.2 总体架构", 2)
    P(doc, "系统采用前后端分离架构。用户侧 Vue3 运行在 5173，管理端 Vue2 运行在 8081，二者均通过 HTTP/JSON 调用 8080 的 Spring Boot 后端。后端按 RuoYi 的分层组织：Controller 负责协议与参数，Service 负责业务规则与事务，Mapper/XML 负责 SQL，MySQL 存储业务数据，Redis 缓存登录令牌与系统配置。")
    figure(doc, "arch_overview.png", "图 2-1 系统总体架构", 6.4)

    H(doc, "2.3 技术选型", 2)
    table(doc, ["层次", "技术", "版本", "用途"], [
        ["后端框架", "Spring Boot", "2.5.15", "Web、依赖注入、事务"],
        ["安全", "Spring Security + JWT", "5.7.14", "登录认证、接口鉴权"],
        ["持久层", "MyBatis + PageHelper", "3.5.x", "SQL 映射、分页"],
        ["数据库", "MySQL", "5.7.26", "业务数据"],
        ["缓存", "Redis", "8.10.2", "登录 token、配置缓存"],
        ["代码生成", "ruoyi-generator", "3.9.1", "管理端 CRUD 代码生成"],
        ["管理端", "Vue2 + Element UI", "2.6.12", "RuoYi 后台界面"],
        ["用户侧", "Vue3 + Vite + Pinia", "3.5 / 8 / 4", "学生/教师页面"],
        ["构建", "Maven / npm", "3.x / 11", "后端/前端构建"],
    ], caption="表 2-1 技术选型", widths=[3.0, 5.0, 2.4, 5.0])

    H(doc, "2.4 运行环境与部署", 2)
    table(doc, ["组件", "端口", "部署位置", "说明"], [
        ["MySQL", "3306", "phpStudy", "数据库 ry-vue，root/123456"],
        ["Redis", "6379", "本机 Windows", "无密码，database 0"],
        ["后端", "8080", "ruoyi-admin.jar", "JDK 17 启动"],
        ["管理端", "8081", "backend/ruoyi-ui", "Vue2 + webpack4"],
        ["用户端", "5173", "user-front", "Vue3 + Vite，代理 /api"],
    ], caption="表 2-2 部署与端口", widths=[3.0, 2.0, 4.5, 6.0])

    H(doc, "2.5 模块划分", 2)
    table(doc, ["模块", "面向角色", "主要职责", "实现位置"], [
        ["用户登录与权限", "学生/教师/管理员", "登录、JWT、角色数据范围", "PortalAuthController/Service、RuoYi Security"],
        ["成绩查询", "学生/教师", "按学期/课程筛选、状态推导、统计", "PortalScoreController/Service、ScoreQueryView"],
        ["成绩录入与修改", "教师", "批量录入、单条修改、留痕、锁定校验", "PortalScoreService、ScoreAddView/ScoreEditView"],
        ["查分申请与受理", "学生/教师", "提交、待办、受理 KEEP/CORRECT、驳回", "PortalAppealController/Service、AppealApplyView/AppealHandleView"],
        ["后台成绩管理", "管理员", "生成代码、菜单、管理端 CRUD", "ruoyi-generator、ruoyi-system、ruoyi-ui"],
    ], caption="表 2-3 模块划分", widths=[4.0, 3.0, 6.0, 5.0])

    doc.add_page_break()


def design_overview(doc):
    H(doc, "3 详细设计概述", 1)
    H(doc, "3.1 设计原则与详细程度约定", 2)
    P(doc, "本系统遵循“薄 Controller、厚 Service、纯 Mapper”的原则：Controller 只做参数接收与统一响应；Service 承担身份校验、数据范围、状态机、事务与计算；Mapper/XML 只写 SQL。前端遵循“视图只收集输入与展示，业务规则集中在 store/后端”的原则。")
    P(doc, "详细程度约定：每个模块给出文件结构表、方法调用关系表、关键方法的伪代码、流程图或时序图、校验与异常表。伪代码保留真实的方法签名和关键分支，省略 getter/setter、日志、空值判断等样板代码；读者应能据此还原实现。")

    H(doc, "3.2 后端包结构与分层", 2)
    figure(doc, "backend_packages.png", "图 3-1 后端包结构与依赖关系", 6.2)
    table(doc, ["包/目录", "职责", "关键类"], [
        ["com.ruoyi.web.controller.portal", "用户侧 Controller", "PortalAuthController、PortalScoreController、PortalAppealController"],
        ["com.ruoyi.portal.service", "用户侧业务与事务", "PortalAuthService、PortalScoreService、PortalAppealService"],
        ["com.ruoyi.portal.mapper", "用户侧 Mapper 接口", "PortalAuthMapper、PortalScoreMapper、PortalAppealMapper"],
        ["mapper/portal/*.xml", "用户侧 SQL", "显式列别名，避免下划线映射关闭导致字段为空"],
        ["com.ruoyi.system.*", "代码生成模块", "ScoreController/Service/Mapper、Appeal、Course……"],
        ["com.ruoyi.common.utils", "公共工具", "GradeCalc、SecurityUtils、StringUtils"],
        ["com.ruoyi.framework", "安全与令牌", "SecurityConfig、TokenService、SysLoginService"],
    ], caption="表 3-1 后端包职责", widths=[5.0, 4.5, 7.0])

    H(doc, "3.3 数据库总体设计", 2)
    P(doc, "数据库包含 7 张业务表：班级、学生、教师、课程、成绩、查分申请、成绩修改记录。成绩表以 (student_id, course_id, term) 唯一约束保证一人一课程一学期只有一条记录；查分申请与修改记录分别保存申请快照和变更历史，不覆盖原数据。")
    figure(doc, "er_grade.png", "图 3-2 成绩管理系统 ER 图", 6.4)
    table(doc, ["表名", "主键", "关键约束", "说明"], [
        ["edu_classroom", "class_id", "class_name 唯一", "班级与班主任"],
        ["edu_student", "student_id", "class_id 索引", "学生基本信息"],
        ["edu_teacher", "teacher_id", "—", "教师基本信息"],
        ["edu_course", "course_id", "(course_name, term) 唯一", "课程、学分、任课教师"],
        ["edu_score", "score_id", "(student_id, course_id, term) 唯一", "平时/期末/总评/绩点"],
        ["edu_appeal", "appeal_id", "status + apply_time 索引", "查分申请与处理结果"],
        ["edu_score_change_log", "log_id", "score_id 索引", "成绩变更留痕"],
    ], caption="表 3-2 业务表清单", widths=[4.5, 2.4, 5.2, 4.4])

    H(doc, "3.4 接口与响应约定", 2)
    table(doc, ["项目", "约定"], [
        ["用户侧前缀", "/portal；前端通过 Vite 代理 /api → 8080"],
        ["认证", "Authorization: Bearer <JWT>；登录接口 @Anonymous"],
        ["成功响应", '{ "code": 200, "msg": "操作成功", "data": ... }'],
        ["业务失败", "ServiceException → 全局异常处理 → code=500 + 中文 msg"],
        ["未登录/过期", "code=401，前端清理 token 并跳转登录"],
        ["日期", "SQL DATE_FORMAT 返回 yyyy-MM-dd HH:mm:ss"],
        ["分页", "管理端 TableDataInfo；用户端一次返回、前端分页"],
    ], caption="表 3-3 接口与响应约定", widths=[3.0, 12.0])

    H(doc, "3.5 成绩与查分状态机", 2)
    P(doc, "查分申请只有三个状态：PENDING、ACCEPTED、REJECTED。PENDING 是唯一可操作状态；ACCEPTED（KEEP/CORRECT）与 REJECTED 为终态。成绩状态不单独存储，由“是否存在 PENDING 申请”和“是否存在修改记录”推导：先判断 APPEALING，再判断 MODIFIED，否则 NORMAL。")
    figure(doc, "appeal_state.png", "图 3-3 查分申请状态机", 5.8)
    figure(doc, "score_state.png", "图 3-4 成绩状态推导", 5.8)

    doc.add_page_break()


def module_login(doc):
    H(doc, "4 用户登录与权限模块详细设计", 1)
    H(doc, "4.1 模块职责与用例", 2)
    P(doc, "本模块负责用户侧登录、身份解析与数据范围。学生与教师凭证复用 RuoYi 的 sys_user 表，业务身份存放在 edu_student/edu_teacher；登录成功后签发 JWT，后续每个 /portal 请求通过过滤器解析用户名，再解析出 STUDENT/TEACHER、班级与所带班级。管理员账号不属于门户身份，不能登录用户端。")
    bullets(doc, [
        "用例 1：学生输入学号/密码登录，进入成绩查询页；",
        "用例 2：教师输入工号/密码登录，进入受理查分页；",
        "用例 3：JWT 过期后前端清理会话并回到登录页；",
        "用例 4：学生访问教师数据时被 Service 层拒绝。",
    ])

    H(doc, "4.2 文件结构", 2)
    table(doc, ["文件", "职责"], [
        ["web/controller/portal/PortalAuthController.java", "登录、当前身份接口"],
        ["portal/service/PortalAuthService.java", "身份加载、调用 SysLoginService 签发 token"],
        ["portal/mapper/PortalAuthMapper.java", "学生/教师/所带班级查询"],
        ["mapper/portal/PortalAuthMapper.xml", "SQL：edu_student/edu_teacher/edu_classroom"],
        ["user-front/src/stores/user.js", "前端会话、token 持久化、角色映射"],
        ["user-front/src/api/request.js", "Bearer token、响应信封、401 处理"],
        ["db/03_portal_accounts.sql", "门户账号、角色、关闭验证码"],
    ], caption="表 4-1 登录模块文件结构", widths=[7.5, 7.5])

    H(doc, "4.3 类与调用关系", 2)
    figure(doc, "auth_sequence.png", "图 4-1 登录时序图", 6.2)
    table(doc, ["调用方", "被调用方", "说明"], [
        ["LoginView.onSubmit", "userStore.login", "收集账号密码，等待登录结果"],
        ["userStore.login", "api.login", "POST /api/portal/auth/login"],
        ["PortalAuthController.login", "PortalAuthService.login", "先解析身份，再认证"],
        ["PortalAuthService.login", "PortalAuthMapper.selectStudent/Teacher", "确认是门户账号"],
        ["PortalAuthService.login", "SysLoginService.login", "BCrypt 校验并创建 JWT"],
        ["JwtAuthenticationTokenFilter", "TokenService", "后续请求解析 token、加载 LoginUser"],
    ], caption="表 4-2 登录调用关系", widths=[4.5, 5.5, 6.0])

    H(doc, "4.4 控制层设计", 2)
    P(doc, "PortalAuthController 只暴露两个接口：POST /portal/auth/login 与 GET /portal/auth/profile。登录接口使用 @Anonymous，使 SecurityConfig 将其加入 permitAll；profile 需要有效 token，用于刷新页面后同步身份。")
    code(doc, """@RestController
@RequestMapping("/portal/auth")
public class PortalAuthController {
    @Autowired private PortalAuthService authService;

    @Anonymous
    @PostMapping("/login")
    public AjaxResult login(@RequestBody Map<String,Object> body) {
        String username = str(body.get("username"));
        String password = str(body.get("password"));
        return AjaxResult.success(authService.login(username, password));
    }

    @GetMapping("/profile")
    public AjaxResult profile() {
        return AjaxResult.success(authService.currentUser());
    }
}""", caption="代码 4-1 PortalAuthController 关键代码")

    H(doc, "4.5 模型层与认证流程", 2)
    P(doc, "loadIdentity 先查 edu_student（按学号），命中则返回 STUDENT 身份；再查 edu_teacher（按工号），命中则查其所带班级返回 TEACHER；都不命中抛出 ServiceException。登录时先调用 loadIdentity，避免 admin 等非门户账号获得 token。")
    code(doc, """public Map<String,Object> loadIdentity(String loginId) {
    Map student = authMapper.selectStudent(loginId);
    if (student != null && !student.isEmpty()) {
        return user(loginId, "STUDENT", student.get("name"),
                    student.get("classId"), student.get("className"),
                    Collections.emptyList());
    }
    Map teacher = authMapper.selectTeacher(loginId);
    if (teacher != null && !teacher.isEmpty()) {
        List<String> classIds = authMapper.selectManagedClassIds(loginId);
        return user(loginId, "TEACHER", teacher.get("name"), "", "", classIds);
    }
    throw new ServiceException("非学生/教师账号，无法登录用户端");
}

public Map<String,Object> login(String username, String password) {
    Map user = loadIdentity(username);          // 1. 必须是门户账号
    String token = loginService.login(username, password, null, null); // 2. RuoYi 认证
    return result(token, user);                 // 3. token + 身份
}""", caption="代码 4-2 门户身份解析与登录伪代码")

    H(doc, "4.6 权限与校验", 2)
    table(doc, ["校验点", "规则", "违反后的行为"], [
        ["账号存在性", "user_name 在 edu_student/edu_teacher 中存在", "ServiceException：非学生/教师账号"],
        ["密码", "BCrypt 校验通过", "RuoYi 返回“用户不存在/密码错误”"],
        ["角色范围", "学生只能看自己；教师只能看所带班级", "ServiceException：无权操作该班级"],
        ["token", "30 分钟有效，Redis 缓存 LoginUser", "401，前端回登录页"],
        ["验证码", "sys.account.captchaEnabled=false", "登录不需要 code/uuid"],
    ], caption="表 4-3 登录与权限校验", widths=[3.5, 7.0, 6.0])

    doc.add_page_break()


def module_score_query(doc):
    H(doc, "5 成绩查询模块详细设计（完整型）", 1)
    H(doc, "5.1 模块职责与需求编号", 2)
    P(doc, "成绩查询模块对应实验一 P2 页面，需求编号 FR-P2-01~FR-P2-10。学生登录后默认看到当前学期成绩，可按学期与课程关键字筛选，看到平时/期末/总评/绩点/状态，以及已修课程数、总学分、平均分、平均绩点；教师复用同一接口查看所带班级。")
    bullets(doc, [
        "FR-P2-01/02：默认学期、学期下拉；",
        "FR-P2-03：课程名称/课程号搜索；",
        "FR-P2-04/05：列表列与公式计算；",
        "FR-P2-06/07：分数着色与统计；",
        "FR-P2-09：行内申请查分跳转；",
        "FR-P2-10：前端每页 10 条分页。",
    ])

    H(doc, "5.2 代码文件结构", 2)
    table(doc, ["层", "文件", "职责"], [
        ["视图", "user-front/src/views/student/ScoreQueryView.vue", "筛选区、统计、表格、分页、申请查分按钮"],
        ["状态", "user-front/src/stores/score.js", "fetchScores/scoreRowsOfStudent/scoreStatusOf"],
        ["接口", "user-front/src/api/request.js", "queryScores(params)"],
        ["控制", "web/controller/portal/PortalScoreController.java", "GET /portal/scores"],
        ["服务", "portal/service/PortalScoreService.java", "身份与数据范围、调用 Mapper"],
        ["映射", "portal/mapper/PortalScoreMapper.java", "selectScoreViews"],
        ["SQL", "resources/mapper/portal/PortalScoreMapper.xml", "多表 JOIN + CASE 状态推导"],
        ["实体", "ruoyi-system/.../domain/Score.java", "管理端生成实体（复用字段口径）"],
    ], caption="表 5-1 成绩查询模块文件结构", widths=[2.0, 6.5, 6.5])

    H(doc, "5.3 类与方法调用关系", 2)
    table(doc, ["调用顺序", "方法", "输入/输出"], [
        ["1", "ScoreQueryView.onMounted → data.ensureLoaded()", "—"],
        ["2", "scoreStore.fetchScores() → api.queryScores()", "term/keyword 可选"],
        ["3", "PortalScoreController.list(term,keyword,classId,courseId)", "AjaxResult<List<Map>>"],
        ["4", "PortalScoreService.list(...)", "解析身份，组装 params"],
        ["5", "PortalAuthService.currentUser()", "JWT 用户名 → 角色/班级"],
        ["6", "PortalScoreMapper.selectScoreViews(params)", "List<Map>"],
        ["7", "ScoreQueryView rows/stats/pagedRows", "筛选、统计、分页"],
    ], caption="表 5-2 成绩查询调用关系", widths=[2.0, 7.5, 5.5])

    H(doc, "5.4 接口设计", 2)
    table(doc, ["方法", "路径", "参数", "返回"], [
        ["GET", "/portal/scores", "term, keyword, classId, courseId（可选）", "成绩 VO 数组"],
        ["GET", "/portal/scores/roster", "classId, courseId, term", "名单行（含 recorded）"],
        ["GET", "/portal/scores/{scoreId}/logs", "—", "修改记录数组"],
    ], caption="表 5-3 成绩查询相关接口", widths=[2.0, 5.0, 5.5, 4.5])

    H(doc, "5.5 处理流程", 2)
    figure(doc, "flow_score_query.png", "图 5-1 成绩查询处理流程", 5.6)
    P(doc, "流程的关键是“数据范围在 Service 层决定”：学生把 studentId 设为当前登录账号；教师把 classIds 设为所带班级，若请求里带 classId 还要校验是否在所带班级内。SQL 只负责过滤与状态推导，不在前端拼状态。")

    H(doc, "5.6 关键 SQL 与状态推导", 2)
    P(doc, "selectScoreViews 连接成绩、学生、班级、课程四张表，用两个 EXISTS 子查询推导状态。先判断 PENDING 申请，再判断修改记录，保证“查分中”优先于“已修改”。日期用 DATE_FORMAT 输出，避免 Jackson 时区差异。")
    code(doc, """select s.score_id as scoreId, s.student_id as studentId,
       st.student_name as studentName, st.class_id as classId,
       cl.class_name as className, co.course_id as courseId,
       co.course_name as courseName, co.credit as credit,
       s.term as term, s.usual_score as usualScore,
       s.final_score as finalScore, s.total_score as totalScore,
       s.gpa as gpa,
       case
         when exists (select 1 from edu_appeal a
                       where a.student_id=s.student_id
                         and a.course_id=s.course_id
                         and a.term=s.term
                         and a.status='PENDING') then 'APPEALING'
         when exists (select 1 from edu_score_change_log l
                       where l.student_id=s.student_id
                         and l.course_id=s.course_id) then 'MODIFIED'
         else 'NORMAL'
       end as status
  from edu_score s
  join edu_student st on st.student_id = s.student_id
  join edu_classroom cl on cl.class_id = st.class_id
  join edu_course co on co.course_id = s.course_id
 where ...""", caption="代码 5-1 selectScoreViews 的关键 SQL")

    H(doc, "5.7 前端实现", 2)
    P(doc, "ScoreQueryView 保留实验一的筛选区、统计卡片、表格与分页；唯一变化是数据来源从 localStorage 改为 store 缓存。store 的 fetchScores 调用后端，scoreRowsOfStudent 仍是同步方法，因此视图的 computed 不需要改写。统计函数 calcAvgScore/calcAvgGpa 直接复用实验一 utils/score.js。")
    code(doc, """// stores/score.js
async fetchScores(params = {}) {
  const rows = await api.queryScores(params)
  this.scores = rows || []
  return this.scores
},

scoreRowsOfStudent(studentId, { term = '', keyword = '' } = {}) {
  return this.scores
    .filter(s => s.studentId === studentId)
    .filter(s => !term || s.term === term)
    .filter(s => !keyword || s.courseName.includes(keyword) || s.courseId.includes(keyword))
    .map(s => ({ ...s, status: this.scoreStatusOf(s) }))
    .sort((a,b) => a.courseId.localeCompare(b.courseId))
}""", caption="代码 5-2 Vue store 的成绩查询实现")

    H(doc, "5.8 校验与异常", 2)
    table(doc, ["场景", "处理"], [
        ["未登录/ token 过期", "后端 401；前端清理 token，跳转登录"],
        ["学生查询他人成绩", "Service 强制 studentId=当前用户，不暴露他人数据"],
        ["教师请求非所带班级", "assertManageClass 抛“无权操作该班级”"],
        ["无成绩", "返回空数组，前端显示“暂无成绩数据”"],
        ["关键字为空", "不追加关键字条件"],
        ["学期为空", "默认前端当前学期；后端不强制"],
    ], caption="表 5-4 成绩查询校验", widths=[6.0, 9.0])

    figure(doc, "ui-student-scores.png", "图 5-2 学生成绩查询页面（真实后端数据）", 6.3)
    doc.add_page_break()


def module_score_edit(doc):
    H(doc, "6 成绩录入与修改模块详细设计", 1)
    H(doc, "6.1 职责与文件结构", 2)
    P(doc, "成绩录入（P4）与修改（P5）面向教师。录入按班级+课程+学期加载名单，批量填写平时/期末，系统计算总评与绩点；修改先按学号/姓名/班级+课程定位成绩，填写修改原因后保存，并在修改记录中留痕。若成绩存在 PENDING 查分申请，状态为 APPEALING，禁止直接修改。")
    table(doc, ["层", "文件", "职责"], [
        ["视图", "views/teacher/ScoreAddView.vue", "班级/课程/学期选择、批量录入、自动计算"],
        ["视图", "views/teacher/ScoreEditView.vue", "查询定位、原/新成绩对照、原因校验、修改记录"],
        ["状态", "stores/score.js", "fetchRoster/saveBatch/updateScore/logsOf"],
        ["控制", "PortalScoreController.java", "roster、batch、PUT /{scoreId}"],
        ["服务", "PortalScoreService.java", "班级校验、0~100 校验、PENDING 锁定、事务"],
        ["SQL", "PortalScoreMapper.xml", "selectRoster/insertScore/updateScore/insertLog"],
    ], caption="表 6-1 成绩录入与修改文件结构", widths=[2.0, 6.0, 7.0])

    H(doc, "6.2 类与方法调用关系", 2)
    table(doc, ["操作", "调用链"], [
        ["加载名单", "ScoreAddView.loadRoster → store.fetchRoster → GET /portal/scores/roster → Service.roster → Mapper.selectRoster"],
        ["批量保存", "ScoreAddView.onSave → store.saveBatch → POST /portal/scores/batch → Service.saveBatch → selectScoreByKeyForUpdate/insert/update"],
        ["查询定位", "ScoreEditView.onSearch → data.scores（已加载）→ 唯一命中 loadTarget → fetchLogs"],
        ["单条修改", "ScoreEditView.onConfirmSave → store.updateScore → PUT /portal/scores/{id} → Service.updateScore → updateScore + insertLog"],
    ], caption="表 6-2 录入与修改调用关系", widths=[3.0, 12.0])

    H(doc, "6.3 批量录入流程", 2)
    figure(doc, "flow_batch_save.png", "图 6-1 批量录入流程", 5.6)
    P(doc, "saveBatch 在一个事务内逐行处理。先校验班级属于当前教师、学生属于该班级、成绩为 0~100；再按 (studentId, courseId, term) 锁行。如果已有成绩且存在 PENDING 申请，整批回滚；否则更新或插入。每行都重新计算总评/绩点，不信任前端传值。")
    code(doc, """@Transactional(rollbackFor = Exception.class)
public Map<String,Object> saveBatch(Map<String,Object> body) {
    User user = currentUser(); assertTeacher(user);
    String classId = str(body,"classId"), courseId = str(body,"courseId"), term = str(body,"term");
    assertManageClass(user, classId);
    int saved = 0;
    for (Row row : rows(body)) {
        validateScore(row.usualScore); validateScore(row.finalScore);
        if (!classId.equals(scoreMapper.selectStudentClass(row.studentId)))
            throw new ServiceException("学生不属于该班级");
        Score old = scoreMapper.selectScoreByKeyForUpdate(row.studentId, courseId, term);
        if (old != null && countPending(row.studentId, courseId, term) > 0)
            throw new ServiceException("该课程正在查分受理中，禁止修改");
        Integer total = GradeCalc.total(row.usualScore, row.finalScore);
        BigDecimal gpa = GradeCalc.gpa(total);
        if (old == null) scoreMapper.insertScore(newScore(...));
        else scoreMapper.updateScore(update(...));
        saved++;
    }
    return map("savedCount", saved);
}""", caption="代码 6-1 批量录入伪代码")

    H(doc, "6.4 单条修改与留痕事务", 2)
    P(doc, "updateScore 先按 scoreId 查出成绩视图，校验班级与 PENDING 状态；然后校验新成绩与 5~100 字原因；更新成绩后插入一条 edu_score_change_log，记录 old/new 的平时、期末、总评，source=MANUAL，operator 与时间由后端写入。更新成绩与写日志必须同事务，否则会出现“改了分但没有记录”。")
    code(doc, """@Transactional(rollbackFor = Exception.class)
public Map<String,Object> updateScore(String scoreId, Map body) {
    User user = currentUser(); assertTeacher(user);
    ScoreView score = mapper.selectScoreViewById(scoreId);
    assertManageClass(user, score.classId);
    if (countPending(score.studentId, score.courseId, score.term) > 0)
        throw new ServiceException("该成绩查分中，禁止直接修改");
    Integer usual = intVal(body,"usualScore"), fin = intVal(body,"finalScore");
    String reason = str(body,"reason");
    if (reason.length() < 5 || reason.length() > 100)
        throw new ServiceException("请填写 5~100 字的修改原因");
    Integer total = GradeCalc.total(usual, fin);
    mapper.updateScore(scoreId, usual, fin, total, GradeCalc.gpa(total), user.loginId);
    mapper.insertLog(score, total, reason, "MANUAL", user.loginId);
    return mapper.selectScoreViewById(scoreId);
}""", caption="代码 6-2 单条修改与留痕伪代码")

    H(doc, "6.5 校验规则", 2)
    table(doc, ["字段", "规则", "错误提示"], [
        ["平时/期末", "0~100 整数，两项都必填", "成绩必须是 0~100 的整数"],
        ["班级", "必须是教师所带班级", "无权操作该班级"],
        ["学生", "必须属于所选班级", "学生不属于班级"],
        ["修改原因", "去空格后 5~100 字", "请填写 5~100 字的修改原因"],
        ["查分锁定", "同学生+课程+学期存在 PENDING", "该成绩查分中，禁止直接修改"],
        ["重复成绩", "同一学生+课程+学期只允许一条", "数据库唯一键 + Service 校验"],
    ], caption="表 6-3 成绩录入/修改校验", widths=[3.0, 7.0, 5.5])

    H(doc, "6.6 前端实现要点", 2)
    P(doc, "ScoreAddView 在 onMounted 中先 ensureLoaded，再默认选中教师第一个班级与第一门课程，随后 loadRoster。保存时把 classId 一并提交（原实验一 Mock 版本不需要班级，因为数据在本地）。ScoreEditView 的日志改为按 scoreId 拉取，loadTarget 时调用 data.fetchLogs(scoreId)，logs 计算属性仍读取 data.logsOf，视图无需大改。")
    figure(doc, "ui-teacher-add.png", "图 6-2 批量录入页面", 6.2)
    doc.add_page_break()


def module_appeal(doc):
    H(doc, "7 查分申请与受理模块详细设计（完整型）", 1)
    H(doc, "7.1 模块职责与需求编号", 2)
    P(doc, "查分模块对应 P3/P6，需求编号 FR-P3-01~09、FR-P6-01~10。学生选择已出成绩的课程，填写 10~200 字理由提交；系统禁止同课程重复 PENDING。教师看到待办列表，受理时可选择 KEEP（维持）或 CORRECT（更正），或驳回；受理/驳回必须填写 5~200 字意见。")
    bullets(doc, [
        "FR-P3-01/02：选择课程并带出原成绩；",
        "FR-P3-03/05：理由长度与必填校验；",
        "FR-P3-07/09：我的申请列表与处理结果查看；",
        "FR-P6-01~07：待办/已办标签、受理弹窗、结果与状态迁移；",
        "FR-P6-08/10：待办角标、驳回理由。",
    ])

    H(doc, "7.2 文件结构", 2)
    table(doc, ["层", "文件", "职责"], [
        ["视图", "views/student/AppealApplyView.vue", "选课程、理由校验、提交、我的申请"],
        ["视图", "views/teacher/AppealHandleView.vue", "待办/已办、受理/驳回弹窗"],
        ["状态", "stores/score.js", "submitAppeal/handleAppeal/rejectAppeal/hasPendingAppeal"],
        ["控制", "PortalAppealController.java", "POST /appeals、GET /mine、GET、PUT handle/reject"],
        ["服务", "PortalAppealService.java", "状态机、班级校验、事务、申请 ID"],
        ["映射", "PortalAppealMapper.java + XML", "申请视图、FOR UPDATE、插入/更新"],
        ["实体", "ruoyi-system/.../domain/Appeal.java", "管理端生成实体"],
    ], caption="表 7-1 查分模块文件结构", widths=[2.0, 6.2, 6.8])

    H(doc, "7.3 类与方法调用关系", 2)
    table(doc, ["场景", "调用链"], [
        ["学生提交", "AppealApplyView.onSubmit → store.submitAppeal → POST /portal/appeals → Service.submit → selectScoreByStudentCourse/countPending/insert"],
        ["学生查看", "AppealApplyView.myAppeals → store.fetchMine → GET /portal/appeals/mine → Service.mine → Mapper.selectAppealViews"],
        ["教师待办", "AppealHandleView.onMounted → ensureLoaded → GET /portal/appeals?status=PENDING → Service.list → Mapper"],
        ["教师受理", "AppealHandleView.onConfirm → store.handleAppeal → PUT /handle → Service.handle → updateHandle/updateTotal/insertLog"],
        ["教师驳回", "AppealHandleView.onConfirm → store.rejectAppeal → PUT /reject → Service.reject → updateReject"],
    ], caption="表 7-2 查分模块调用关系", widths=[3.0, 12.0])

    H(doc, "7.4 接口设计", 2)
    table(doc, ["方法", "路径", "请求", "响应"], [
        ["POST", "/portal/appeals", "{courseId,reason}", "新申请 VO"],
        ["GET", "/portal/appeals/mine", "—", "本人申请列表"],
        ["GET", "/portal/appeals", "status=PENDING|HANDLED", "教师待办/已办"],
        ["PUT", "/portal/appeals/{id}/handle", "{result:KEEP|CORRECT,correctedScore,opinion}", "更新后的申请"],
        ["PUT", "/portal/appeals/{id}/reject", "{opinion}", "更新后的申请"],
    ], caption="表 7-3 查分接口", widths=[2.0, 6.0, 5.0, 4.0])

    H(doc, "7.5 提交申请流程", 2)
    figure(doc, "seq_appeal_submit.png", "图 7-1 学生提交查分申请时序", 6.2)
    P(doc, "submit 必须由学生发起。后端忽略前端可能传来的 studentId，一律使用 JWT 中的学号；按 studentId+courseId 找最近一条成绩，找不到则拒绝；用该成绩的 term 做重复检查；申请 ID 采用 AP+yyyyMMdd+3 位序号，并在插入前查当天最大编号。")
    code(doc, """@Transactional(rollbackFor = Exception.class)
public Map<String,Object> submit(Map body) {
    User user = currentUser(); assertStudent(user);
    String studentId = user.loginId;
    String courseId = str(body,"courseId");
    String reason = str(body,"reason");
    if (reason.length() < 10 || reason.length() > 200)
        throw new ServiceException("请填写 10~200 字的申请理由");
    Score score = scoreMapper.selectScoreByStudentCourse(studentId, courseId);
    if (score == null) throw new ServiceException("该课程暂无成绩");
    if (appealMapper.countPending(studentId, courseId, score.term) > 0)
        throw new ServiceException("该课程已有待受理的查分申请");
    String appealId = nextAppealId(); // AP + yyyyMMdd + 3 位序号
    appealMapper.insertAppeal(appealId, studentId, courseId, score.term,
                              score.totalScore, reason);
    return appealMapper.selectAppealViewById(appealId);
}""", caption="代码 7-1 提交查分申请伪代码")

    H(doc, "7.6 受理/驳回流程", 2)
    figure(doc, "seq_appeal_handle.png", "图 7-2 教师受理查分申请时序", 6.2)
    P(doc, "handle 先 SELECT ... FOR UPDATE 锁定申请行，校验 status=PENDING、申请所属班级在教师管理范围内、意见 5~200 字。KEEP 只更新申请；CORRECT 还要锁定成绩行，更新 total_score/gpa/manual_total，并插入一条 source=APPEAL 的修改记录。三张表的更新在同一个 @Transactional 中完成。")
    code(doc, """@Transactional(rollbackFor = Exception.class)
public Map<String,Object> handle(String appealId, Map body) {
    User user = currentUser(); assertTeacher(user);
    Appeal appeal = appealMapper.selectAppealForUpdate(appealId);
    if (appeal == null) throw new ServiceException("查分申请不存在");
    if (!"PENDING".equals(appeal.status)) throw new ServiceException("该申请已处理");
    assertManageClass(user, appeal.classId);
    String opinion = str(body,"opinion");
    if (opinion.length() < 5 || opinion.length() > 200)
        throw new ServiceException("请填写 5~200 字的处理意见");
    String result = str(body,"result");
    if ("CORRECT".equals(result)) {
        Integer corrected = intVal(body,"correctedScore");
        if (corrected == null || corrected < 0 || corrected > 100)
            throw new ServiceException("更正后的成绩必须是 0~100 的整数");
        Score score = scoreMapper.selectScoreByKeyForUpdate(
                appeal.studentId, appeal.courseId, appeal.term);
        scoreMapper.updateTotal(score.scoreId, corrected,
                                GradeCalc.gpa(corrected), user.loginId);
        scoreMapper.insertLog(score, corrected, "查分受理更正：" + opinion,
                              "APPEAL", user.loginId);
    }
    appealMapper.updateHandle(appealId, result, corrected, opinion, user.loginId);
    return appealMapper.selectAppealViewById(appealId);
}""", caption="代码 7-2 受理查分申请伪代码")

    H(doc, "7.7 三表一致性与并发控制", 2)
    table(doc, ["一致性点", "实现"], [
        ["申请与成绩", "CORRECT 时申请保存 correctedScore，成绩保存 totalScore/gpa，二者在同一事务"],
        ["成绩与日志", "updateTotal 与 insertLog 同事务；日志保存 old/new 与 source=APPEAL"],
        ["重复受理", "selectAppealForUpdate 锁行 + status=PENDING 检查；第二次请求失败"],
        ["重复修改", "成绩行 FOR UPDATE + countPending 检查；查分中禁止修改"],
        ["申请编号并发", "当天最大编号 + 1；主键唯一约束兜底"],
        ["数据范围", "教师只受理 classId 在自己 manageClassIds 内的申请"],
    ], caption="表 7-4 一致性与并发控制", widths=[4.0, 11.0])

    H(doc, "7.8 前端实现要点", 2)
    P(doc, "AppealApplyView 的 myScores 来自 store 的 scoreRowsOfStudent，提交前用 hasPendingAppeal 做前端预校验，后端仍会再校验。AppealHandleView 的 pendingRows/handledRows 来自 appealRowsByStatus；受理/驳回调用 async action，成功后 store 重新拉取申请与成绩，标签角标与列表自动更新。")
    figure(doc, "ui-teacher-appeals.png", "图 7-3 教师受理查分页面", 6.2)
    figure(doc, "ui-admin-appeal.png", "图 7-4 管理端查分申请维护页面", 6.2)

    H(doc, "7.9 异常与校验", 2)
    table(doc, ["场景", "处理"], [
        ["学生重复提交同课程 PENDING", "ServiceException（前端预校验 + 后端兜底）"],
        ["申请不存在/已处理", "ServiceException，事务回滚"],
        ["教师受理非所带班级", "ServiceException：无权操作该班级"],
        ["CORRECT 未填/越界分数", "ServiceException：更正后的成绩必须是 0~100 的整数"],
        ["驳回未填理由", "ServiceException：请填写 5~200 字的驳回理由"],
        ["并发重复受理", "FOR UPDATE + status 检查，第二次失败"],
    ], caption="表 7-5 查分模块异常处理", widths=[6.0, 9.0])
    doc.add_page_break()


def module_generator(doc):
    H(doc, "8 后台成绩管理模块（代码生成）详细设计", 1)
    H(doc, "8.1 需求与目标", 2)
    P(doc, "实验三要求运行 ruoyi-generator，设计数据库表并生成管理端代码；成绩管理系统至少完成一张成绩管理表。本系统以 edu_score 为核心，同时生成 edu_appeal、edu_course、edu_student、edu_teacher、edu_classroom 的 CRUD，并只生成 edu_score_change_log 的 domain/mapper 供二次开发使用。")
    figure(doc, "flow_generator.png", "图 8-1 代码生成与落位流程", 6.2)

    H(doc, "8.2 生成器配置", 2)
    table(doc, ["配置", "值", "说明"], [
        ["author", "grade-team", "生成代码作者"],
        ["packageName", "com.ruoyi.system", "生成到 ruoyi-system 模块"],
        ["autoRemovePre", "true", "去掉表前缀，edu_score → Score"],
        ["tablePrefix", "edu_", "表前缀"],
        ["allowOverwrite", "false", "不覆盖本地文件，下载 zip 后手工复制"],
        ["parentMenuId", "2000", "菜单挂在「成绩管理」目录下"],
    ], caption="表 8-1 generator.yml 与生成选项", widths=[3.5, 4.5, 7.0])
    P(doc, "className 在导入表时由 GenUtils.initTable 计算，因此必须先改配置并重启后端再导入；若已导入，需要清空 gen_table/gen_table_column 后重新导入。businessName 取表名最后一个下划线后的部分，例如 edu_score → score，接口为 /system/score。")

    H(doc, "8.3 导入与元数据编辑", 2)
    P(doc, "登录管理端后进入「系统工具 → 代码生成 → 导入」，选择 7 张 edu_* 表。每张表需要确认：功能名（中文）、模板 crud、包路径 com.ruoyi.system、模块名 system、业务名、上级菜单。字段层面设置列表/查询/编辑/新增/必填与显示类型；主键 isInsert=0，计算字段（total_score/gpa/manual_total、申请的状态/处理结果等）不参与新增/编辑。")
    code(doc, """# 通过 API 自动完成导入与配置（tools/gen_admin.py）
POST /login                         -> token
POST /tool/gen/importTable?tables=edu_score,edu_appeal,...
GET  /tool/gen/{tableId}            -> info + columns
PUT  /tool/gen                      -> functionName, params.parentMenuId=2000,
                                       columns[].isList/isQuery/isEdit/isInsert
GET  /tool/gen/batchGenCode?tables=... -> ruoyi.zip""", caption="代码 8-1 生成器 API 调用顺序")

    H(doc, "8.4 生成文件结构与落位", 2)
    table(doc, ["生成文件", "落位", "说明"], [
        ["main/java/com/ruoyi/system/domain/Score.java", "ruoyi-system/.../domain", "实体，继承 BaseEntity"],
        ["main/java/com/ruoyi/system/mapper/ScoreMapper.java", "ruoyi-system/.../mapper", "MyBatis 接口"],
        ["main/java/com/ruoyi/system/service/IScoreService.java", "ruoyi-system/.../service", "服务接口"],
        ["main/java/com/ruoyi/system/service/impl/ScoreServiceImpl.java", "ruoyi-system/.../service/impl", "CRUD 实现 + 二次开发"],
        ["main/java/com/ruoyi/system/controller/ScoreController.java", "ruoyi-system/.../controller", "REST /system/score"],
        ["main/resources/mapper/system/ScoreMapper.xml", "ruoyi-system/.../mapper/system", "动态 SQL"],
        ["vue/api/system/score.js", "ruoyi-ui/src/api/system", "前端 API"],
        ["vue/views/system/score/index.vue", "ruoyi-ui/src/views/system/score", "Vue2 管理页面"],
        ["scoreMenu.sql", "db/04_menu_grade.sql", "菜单与按钮权限"],
    ], caption="表 8-2 生成文件落位", widths=[6.5, 5.5, 4.0])

    H(doc, "8.5 菜单与权限", 2)
    P(doc, "先创建父菜单 2000「成绩管理」（M 目录，path=grade，icon=education），再执行生成器 SQL。每个模块生成 list/query/add/edit/remove/export 六个权限，例如 system:score:list。admin 角色拥有全部权限；门户学生/教师不分配这些菜单，只能访问 /portal。菜单路径为 /grade/score、/grade/appeal 等，组件为 system/<business>/index。")

    H(doc, "8.6 二次开发：ScoreServiceImpl", 2)
    P(doc, "生成器只提供标准 CRUD。为保证管理端新增/修改也遵守成绩公式，ScoreServiceImpl 在 insert/update 中统一生成 scoreId、重算 totalScore/gpa、设置 manualTotal=0、写入录入人/时间。计算逻辑抽取到公共工具 GradeCalc，与 Portal 服务共用，避免两套口径。")
    code(doc, """@Override
public int insertScore(Score score) {
    if (StringUtils.isEmpty(score.getScoreId()))
        score.setScoreId("SC" + System.currentTimeMillis());
    Integer total = GradeCalc.total(score.getUsualScore(), score.getFinalScore());
    score.setTotalScore(total);
    score.setGpa(GradeCalc.gpa(total));
    score.setManualTotal("0");
    score.setRecorderId(SecurityUtils.getUsername());
    score.setRecordTime(DateUtils.getNowDate());
    score.setCreateTime(DateUtils.getNowDate());
    return scoreMapper.insertScore(score);
}

@Override
public int updateScore(Score score) {
    Integer total = GradeCalc.total(score.getUsualScore(), score.getFinalScore());
    score.setTotalScore(total);
    score.setGpa(GradeCalc.gpa(total));
    score.setManualTotal("0");
    score.setUpdateTime(DateUtils.getNowDate());
    return scoreMapper.updateScore(score);
}""", caption="代码 8-2 生成代码的二次开发")

    H(doc, "8.7 管理端验证", 2)
    P(doc, "重启后端后用 admin/admin123 登录 8081，左侧出现「成绩管理」及六个子菜单；成绩列表显示 12 条数据，支持按学号/课程/学期查询；新增一条 60/70 的成绩，后端保存的总评是 66、绩点 1.6，而不是前端可能伪造的 99/9.9。")
    figure(doc, "ui-admin-score.png", "图 8-2 生成的成绩管理页面", 6.3)

    H(doc, "8.8 代码生成常见问题", 2)
    table(doc, ["问题", "原因/解决"], [
        ["类名带 Edu 前缀", "autoRemovePre/tablePrefix 未生效，改配置重启后重新导入"],
        ["菜单挂到系统工具", "未设置 params.parentMenuId=2000"],
        ["生成接口 404", "只执行了 mvn package 未 clean，fat jar 内嵌旧 ruoyi-system.jar；执行 mvn clean package"],
        ["页面字段为空", "列 isList/isEdit/isInsert 未设置，用 gen_admin.py 重新配置"],
    ], caption="表 8-3 代码生成问题排查", widths=[4.0, 11.0])
    doc.add_page_break()


def reuse_and_config(doc):
    H(doc, "9 复用模块", 1)
    P(doc, "系统遵循复用原则，尽量不重复造轮子。后端复用 RuoYi 的控制器基类、统一响应、分页、安全与工具类；生成器复用 Velocity 模板与 GenTable 元数据；前端复用实验一的 6 个页面、纯函数组件与设计令牌。")
    table(doc, ["复用项", "位置", "用途"], [
        ["BaseController", "ruoyi-common/.../core/controller", "startPage/getDataTable/success/toAjax"],
        ["AjaxResult / TableDataInfo", "ruoyi-common/.../core/domain、page", "统一响应与分页"],
        ["BaseEntity", "ruoyi-common/.../core/domain", "审计字段"],
        ["SecurityUtils", "ruoyi-common/.../utils", "当前用户、BCrypt、权限判断"],
        ["GradeCalc", "ruoyi-common/.../utils", "总评/绩点公式，前后端一致"],
        ["PageHelper", "ruoyi-common", "管理端分页"],
        ["ruoyi-generator", "ruoyi-generator", "Velocity 模板、GenUtils、VelocityUtils"],
        ["Vue 组件", "user-front/src/components", "DataTable/BaseModal/StatusTag/ScoreCell/FilterBar/Pagination"],
        ["useToast", "user-front/src/composables", "全局轻提示"],
        ["utils/score.js / validate.js", "user-front/src/utils", "公式与校验文案"],
    ], caption="表 9-1 复用模块清单", widths=[4.0, 6.0, 6.0])

    H(doc, "10 配置文件说明", 1)
    table(doc, ["文件", "关键配置", "说明"], [
        ["backend/ruoyi-admin/src/main/resources/application.yml", "server.port=8080、token.header=Authorization、token.expireTime=30、Redis localhost:6379", "端口、JWT、Redis"],
        [".../application-druid.yml", "url=jdbc:mysql://localhost:3306/ry-vue?useSSL=false、root/123456", "数据源"],
        ["backend/ruoyi-generator/src/main/resources/generator.yml", "packageName=com.ruoyi.system、autoRemovePre=true、tablePrefix=edu_", "代码生成"],
        ["backend/ruoyi-ui/vue.config.js", "port=8081、proxy /dev-api → 8080", "管理端开发服务器"],
        ["user-front/vite.config.js", "port=5173、proxy /api → 8080", "用户端开发服务器"],
        ["user-front/src/api/request.js", "BASE_URL=/api/portal、Bearer token、信封处理", "用户端接口层"],
    ], caption="表 10-1 配置文件说明", widths=[6.5, 6.5, 3.0])

    P(doc, "注意：RuoYi 前端默认端口 80，本工程改为 8081 以避免与用户端/其他服务冲突；管理端 Vue2 + webpack4 在 Node 24 下需要设置 NODE_OPTIONS=--openssl-legacy-provider；用户端 Vite8 需要 Node ≥20.19。")

    doc.add_page_break()


def extra_features(doc):
    H(doc, "11 前端增强：个人中心与成绩预测", 1)
    H(doc, "11.1 个人中心", 2)
    P(doc, "个人中心（#/profile）是学生与教师共用的页面，路由不限制角色。页面从 user store 读取当前身份，从 score store 读取成绩与查分申请，展示四块内容：身份卡片、基本信息、学习/教学概览、账号与安全；底部再根据角色给出“小贴士”。学生看到已修课程、总学分、平均分、平均绩点、最高分课程与待受理申请；教师看到管理班级数、课程数、待受理与已处理数量。")
    table(doc, ["文件", "职责"], [
        ["user-front/src/views/ProfileView.vue", "身份卡片、概览、账号安全、小贴士"],
        ["user-front/src/router/index.js", "新增 /profile 路由（无角色限制）"],
        ["user-front/src/components/AppSidebar.vue", "学生/教师菜单都增加「个人中心」"],
        ["user-front/src/components/AppHeader.vue", "右上角欢迎语可点击进入个人中心"],
        ["user-front/src/stores/{user,score}.js", "身份与成绩/申请数据来源"],
    ], caption="表 11-1 个人中心文件结构", widths=[7.0, 8.0])
    figure(doc, "ui-student-profile.png", "图 11-1 学生个人中心", 6.2)
    figure(doc, "ui-teacher-profile.png", "图 11-2 教师个人中心", 6.2)
    P(doc, "个人中心不新增后端接口：身份来自 JWT 对应的 /portal/auth/profile，成绩与申请来自已有的 /portal/scores 与 /portal/appeals。这样既复用了现有数据权限（学生只能看自己、教师只能看所带班级），又避免为展示页新增一套冗余 API。")

    H(doc, "11.2 成绩预测与最近成绩趋势", 2)
    P(doc, "成绩查询页在统计条下方新增“预测下次考试成绩”卡片和“最近成绩起伏”折线图。预测规则很简单：取该生最近 8 门已出成绩课程的总评，计算算术平均，保留 1 位小数，作为下次考试的参考值；同时在卡片上展示最高分、最低分和相对首门课程的升降幅度。折线图用纯 SVG 绘制，不引入 ECharts 等额外依赖，包含横向网格、纵轴刻度、平均分虚线、数据点和课程名标签。")
    code(doc, """// ScoreQueryView.vue
const trendRows = computed(() =>
  data.scoreRowsOfStudent(user.loginId, {})
    .filter(r => r.totalScore !== null && r.totalScore !== undefined)
    .sort((a,b) => String(a.recordTime||'').localeCompare(String(b.recordTime||'')))
    .slice(-8)
)
const predictedScore = computed(() => {
  if (!trendRows.value.length) return null
  const sum = trendRows.value.reduce((acc,r) => acc + Number(r.totalScore), 0)
  return Number((sum / trendRows.value.length).toFixed(1))
})
// ScoreTrendChart.vue：把每门课映射为 {label: courseName, value: totalScore}
// x = padding + index * innerWidth/(n-1)，y = padding + (1 - value/100) * innerHeight""",
         caption="代码 11-1 预测成绩与趋势数据的计算")
    table(doc, ["文件", "职责"], [
        ["user-front/src/views/student/ScoreQueryView.vue", "趋势数据计算、预测卡片、布局与响应式样式"],
        ["user-front/src/components/ScoreTrendChart.vue", "SVG 折线图、网格、平均线、数据点与标签"],
        ["user-front/src/utils/score.js", "复用 calcAvgScore / calcAvgGpa"],
    ], caption="表 11-2 预测成绩文件结构", widths=[7.5, 7.5])
    figure(doc, "ui-student-predict.png", "图 11-3 预测成绩卡片与最近成绩折线图", 6.3)
    table(doc, ["场景", "处理"], [
        ["没有成绩", "不显示预测卡片；折线图显示空状态"],
        ["只有 1 门成绩", "预测值等于该门总评；折线图绘制一个点"],
        ["成绩不足 8 门", "取全部已出成绩课程"],
        ["筛选条件改变", "预测卡片基于全部成绩，不受表格筛选影响"],
        ["窗口宽度 < 960px", "预测卡片与图表改为上下排列"],
    ], caption="表 11-3 预测成绩边界处理", widths=[4.5, 10.5])

    H(doc, "11.3 变更影响", 2)
    P(doc, "新增功能只修改用户端前端，不改数据库、Portal API 与管理端生成代码。用户端页面由 6 个增加到 7 个；成绩查询页在不改变原有筛选、统计、表格与分页的前提下，在表格上方增加预测区。原有 6 个页面的业务规则、接口契约与测试用例保持不变。")
    doc.add_page_break()


def appendices(doc):
    H(doc, "附录 A 数据库 DDL（节选）", 1)
    P(doc, "完整脚本见 db/01_schema_grade.sql；以下节选成绩与查分两张核心表。")
    code(doc, """create table edu_score (
  score_id varchar(32) not null comment '成绩编号',
  student_id varchar(16) not null comment '学号',
  course_id varchar(16) not null comment '课程编号',
  term varchar(20) not null comment '学期',
  usual_score int(3) comment '平时成绩',
  final_score int(3) comment '期末成绩',
  total_score int(3) comment '总评成绩',
  gpa decimal(3,1) comment '绩点',
  manual_total char(1) default '0' comment '总评是否人工更正',
  recorder_id varchar(16) comment '录入人工号',
  record_time datetime comment '录入时间',
  primary key (score_id),
  unique key uk_score_stu_course_term (student_id, course_id, term)
) engine=innodb default charset=utf8mb4 comment='学生成绩表';

create table edu_appeal (
  appeal_id varchar(32) not null comment '申请编号',
  student_id varchar(16) not null,
  course_id varchar(16) not null,
  term varchar(20) not null,
  original_score int(3) comment '原总评',
  reason varchar(300) not null,
  apply_time datetime not null,
  status varchar(16) not null default 'PENDING',
  result varchar(16) comment 'KEEP/CORRECT',
  corrected_score int(3),
  opinion varchar(300),
  handler_id varchar(16),
  handle_time datetime,
  primary key (appeal_id),
  key idx_appeal_status_time (status, apply_time)
) engine=innodb default charset=utf8mb4 comment='查分申请表';""", caption="代码 A-1 核心表 DDL 节选")

    H(doc, "附录 B 接口清单", 1)
    table(doc, ["分类", "方法", "路径", "说明"], [
        ["门户", "POST", "/portal/auth/login", "登录"],
        ["门户", "GET", "/portal/auth/profile", "当前身份"],
        ["门户", "GET", "/portal/options", "学期/班级/课程"],
        ["门户", "GET", "/portal/scores", "成绩列表"],
        ["门户", "GET", "/portal/scores/roster", "班级名单"],
        ["门户", "GET", "/portal/scores/{id}/logs", "修改记录"],
        ["门户", "POST", "/portal/scores/batch", "批量录入"],
        ["门户", "PUT", "/portal/scores/{id}", "单条修改"],
        ["门户", "POST", "/portal/appeals", "提交查分"],
        ["门户", "GET", "/portal/appeals/mine", "我的申请"],
        ["门户", "GET", "/portal/appeals", "教师待办/已办"],
        ["门户", "PUT", "/portal/appeals/{id}/handle", "受理"],
        ["门户", "PUT", "/portal/appeals/{id}/reject", "驳回"],
        ["管理端", "GET/POST/PUT/DELETE", "/system/{score|appeal|course|student|teacher|classroom}", "生成 CRUD"],
        ["生成器", "POST", "/tool/gen/importTable", "导入表"],
        ["生成器", "PUT", "/tool/gen", "保存生成配置"],
        ["生成器", "GET", "/tool/gen/download/{table}", "下载代码"],
    ], caption="表 B-1 接口清单", widths=[2.5, 4.5, 6.5, 2.5])

    H(doc, "附录 C 测试用例与结果", 1)
    table(doc, ["编号", "用例", "预期", "结果"], [
        ["U-01", "学生 2021001 登录", "9 条成绩", "通过"],
        ["U-02", "HIS101 状态", "查分中", "通过"],
        ["U-03", "重复申请 HIS101", "被拒绝", "通过"],
        ["U-04", "提交 MAT101 申请", "PENDING", "通过"],
        ["U-05", "教师受理 CORRECT 56", "HIS101=56，绩点 0.0，生成 APPEAL 日志", "通过"],
        ["U-06", "驳回 AP20250115002", "REJECTED", "通过"],
        ["U-07", "批量录入 2 人", "savedCount=2，名单已录 4 人", "通过"],
        ["U-08", "查分中修改成绩", "被拒绝", "通过"],
        ["A-01", "管理端成绩列表", "12 条，搜索/导出可用", "通过"],
        ["A-02", "生成页面新增 60/70", "后端保存 66/1.6", "通过"],
        ["G-01", "代码生成", "zip 含 Score.java/Controller/Menu.sql", "通过"],
    ], caption="表 C-1 测试用例与结果", widths=[2.0, 5.0, 6.5, 2.5])

    H(doc, "附录 D 文件清单（节选）", 1)
    table(doc, ["路径", "说明"], [
        ["db/00_grade_system_all.sql", "一键初始化"],
        ["backend/ruoyi-admin/.../controller/portal/*.java", "用户侧 4 个 Controller"],
        ["backend/ruoyi-admin/.../portal/service/*.java", "用户侧 4 个 Service"],
        ["backend/ruoyi-admin/.../resources/mapper/portal/*.xml", "用户侧 4 个 Mapper XML"],
        ["backend/ruoyi-common/.../utils/GradeCalc.java", "成绩计算公共工具"],
        ["backend/ruoyi-system/.../system/controller/*.java", "生成的管理端 Controller"],
        ["backend/ruoyi-system/.../system/domain/*.java", "生成的实体"],
        ["backend/ruoyi-ui/src/views/system/{score,appeal,course,student,teacher,classroom}", "生成的管理端 Vue2 页面"],
        ["user-front/src/api/request.js", "用户端真实接口层"],
        ["user-front/src/stores/{user,score}.js", "会话与成绩缓存"],
        ["user-front/src/views/**", "6 个用户端页面"],
        ["tools/gen_admin.py", "生成器自动化"],
        ["tools/smoke_test.py / ui_smoke.mjs / admin_ui_smoke.mjs", "测试脚本"],
        ["tools/build_report.py / postprocess_docx.py", "报告生成"],
    ], caption="表 D-1 文件清单", widths=[8.0, 7.0])


def main():
    doc = Document()
    style_document(doc)
    cover(doc)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.first_line_indent = None
    run = p.add_run("目  录")
    set_run_font(run, HEAD_FONT, 16, True)
    add_toc(doc)
    doc.add_page_break()

    intro(doc)
    overview(doc)
    design_overview(doc)
    module_login(doc)
    module_score_query(doc)
    module_score_edit(doc)
    module_appeal(doc)
    module_generator(doc)
    reuse_and_config(doc)
    extra_features(doc)
    appendices(doc)

    doc.save(OUT)
    print("saved", OUT)


if __name__ == "__main__":
    main()
