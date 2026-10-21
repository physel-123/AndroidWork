# -*- coding: utf-8 -*-
"""
生成"四人分工"图表（同时输出样式一致的 Word 版）。
输出：
  docs\img\division_of_work.png
  docs\节气民俗App-四人分工表.docx
"""
import os
from PIL import Image, ImageDraw, ImageFont

DOCS = r"E:\AndroidWork\docs"
IMG = os.path.join(DOCS, "img")
os.makedirs(IMG, exist_ok=True)

FB = r"C:\Windows\Fonts\msyhbd.ttc"
FR = r"C:\Windows\Fonts\msyh.ttc"

INK = (43, 32, 24)
PAPER = (253, 250, 245)
CARD = (255, 255, 255)
GREY = (120, 110, 100)

# 四个人各一个主题色（沿用 App 的节气配色体系）
C1 = (62, 124, 106)    # 竹青  - M1
C2 = (143, 58, 46)     # 朱砂  - M2
C3 = (217, 147, 47)    # 琥珀  - M3
C4 = (47, 74, 109)     # 靛青  - M4


def f(size, bold=False):
    return ImageFont.truetype(FB if bold else FR, size, index=0)


def tw(d, s, font):
    bb = d.textbbox((0, 0), s, font=font)
    return bb[2] - bb[0]


def center(d, cx, y, s, font, fill=INK):
    d.text((cx - tw(d, s, font) / 2, y), s, font=font, fill=fill)


def wrap(d, s, font, max_w):
    lines, cur = [], ""
    for ch in s:
        if ch == "\n":
            lines.append(cur); cur = ""; continue
        if tw(d, cur + ch, font) > max_w and cur:
            lines.append(cur); cur = ch
        else:
            cur += ch
    if cur:
        lines.append(cur)
    return lines


# 四个人 / 四个模块的数据
MEMBERS = [
    {
        "no": "成员 1",
        "role": "组长",
        "color": C1,
        "module": "M1  节气内容展示与检索",
        "extra": "＋ 注册 / 登录（基础支撑）",
        "points": [
            "F1.1 当日节气智能推荐",
            "F1.2 节气日期天文计算与倒计时",
            "F1.3 二十四节气图谱检索",
            "F1.4 节气文化档案",
            "F1.5 古画配图浏览与全屏查看",
        ],
        "extra_points": [
            "S0.1 注册（账号唯一校验）",
            "S0.2 登录（加盐摘要校验）",
            "S0.3 登录态持久化",
            "S0.4 游客浏览模式",
            "S0.5 演示账号一键切换",
        ],
        "files": [
            "domain/SolarTermCalculator.kt",
            "domain/TermDateUtils.kt",
            "data/content/*.kt",
            "ui/screens/HomeScreen.kt",
            "ui/screens/TermListScreen.kt",
            "ui/screens/TermDetailScreen.kt",
            "ui/components/TermWheel.kt",
            "data/UserRepository.kt",
            "ui/screens/LoginScreen.kt",
        ],
    },
    {
        "no": "成员 2",
        "role": "组员",
        "color": C2,
        "module": "M2  节气打卡与习惯养成",
        "extra": "",
        "points": [
            "F2.1 一键打卡（含心情备注）",
            "F2.2 月度打卡日历（补打卡/取消）",
            "F2.3 连续打卡与成长等级",
            "F2.4 二十四节气点亮进度",
            "F2.5 打卡卡片分享",
        ],
        "extra_points": [],
        "files": [
            "domain/CheckInCalculator.kt",
            "data/Repositories.kt（打卡部分）",
            "data/db/Entities.kt（check_in）",
            "ui/screens/CheckInScreen.kt",
            "src/test/CheckInCalculatorTest.kt",
        ],
    },
    {
        "no": "成员 3",
        "role": "组员",
        "color": C3,
        "module": "M3  打卡广场与社区互动",
        "extra": "",
        "points": [
            "F3.1 发布图文打卡动态",
            "F3.2 动态信息流与筛选",
            "F3.3 点赞（幂等）",
            "F3.4 评论与二级回复",
            "F3.5 内容删除与分享",
        ],
        "extra_points": [],
        "files": [
            "data/SocialRepository.kt",
            "data/db/SocialEntities.kt",
            "data/db/SocialDaos.kt",
            "ui/screens/FeedScreen.kt",
            "ui/components/CommentViews.kt",
        ],
    },
    {
        "no": "成员 4",
        "role": "组员",
        "color": C4,
        "module": "M4  地域民俗地图",
        "extra": "＋ 每日提醒与收藏（M5）",
        "points": [
            "F4.1 中国地图民俗分布可视化",
            "F4.2 按省份 / 节气筛选",
            "F4.3 民俗详情查看与分享",
            "F4.4 用户民俗投稿",
        ],
        "extra_points": [
            "F5.1 每日定时节气提醒",
            "F5.2 提醒时刻自定义",
            "F5.3 开机自动恢复提醒",
            "F5.4 节气 / 民俗收藏",
        ],
        "files": [
            "ui/components/FolkMapCanvas.kt",
            "ui/components/ProvinceOutlines.kt",
            "ui/screens/FolkMapScreen.kt",
            "data/FolkCustomRepository.kt",
            "data/content/FolkCustomContent.kt",
            "notify/ReminderScheduler.kt",
            "notify/DailyReminderReceiver.kt",
        ],
    },
]


def build_chart():
    W = 1600
    M = 40
    col_gap = 18
    col_w = (W - 2 * M - 3 * col_gap) // 4

    # 先粗算高度：取最高的那一列
    probe = Image.new("RGB", (10, 10), PAPER)
    d0 = ImageDraw.Draw(probe)

    def col_height(m):
        h = 0
        h += 34                                    # 序号条
        h += 44                                    # 模块名（可能 2 行）
        h += 10
        h += 30                                    # "功能点" 小标题
        h += len(m["points"]) * 26 + 8
        if m["extra"]:
            h += 34                                # 附加模块头
            h += len(m["extra_points"]) * 26 + 8
        h += 26                                    # "涉及文件" 小标题
        h += len(m["files"]) * 18 + 14
        return h

    body_h = max(col_height(m) for m in MEMBERS)
    TOP = 178          # 标题区
    BOTTOM = 96        # 底部备注
    H = TOP + body_h + BOTTOM

    img = Image.new("RGB", (W, H), PAPER)
    d = ImageDraw.Draw(img)

    # ---------- 标题 ----------
    d.rectangle((0, 0, W, 96), fill=(43, 32, 24))
    d.text((M, 22), "节气民俗 App　四人分工图", font=f(36, True), fill=(255, 255, 255))
    d.text((M, 70), "M1—M4 四个功能模块 · 每人独立工作区 · 注册登录由组长额外承担",
           font=f(17), fill=(210, 198, 184))

    # 图例
    lx = W - M - 470
    for i, (label, col) in enumerate([("M1 内容", C1), ("M2 打卡", C2),
                                      ("M3 互动", C3), ("M4 地图", C4)]):
        x = lx + i * 118
        d.rounded_rectangle((x, 40, x + 26, 62), radius=6, fill=col)
        d.text((x + 32, 42), label, font=f(15), fill=(235, 226, 214))

    # ---------- 四个成员列 ----------
    top = TOP
    for i, m in enumerate(MEMBERS):
        x0 = M + i * (col_w + col_gap)
        x1 = x0 + col_w
        y = top
        col = m["color"]

        # 卡片底
        d.rounded_rectangle((x0, y, x1, y + body_h), radius=14,
                            fill=CARD, outline=(224, 216, 204), width=2)
        # 顶部序号条
        d.rounded_rectangle((x0, y, x1, y + 34), radius=14, fill=col)
        d.rectangle((x0, y + 20, x1, y + 34), fill=col)   # 压平下圆角
        d.text((x0 + 12, y + 6), "%s　·　%s" % (m["no"], m["role"]), font=f(15, True),
               fill=(255, 255, 255))
        y += 34

        # 模块名
        y += 10
        for ln in wrap(d, m["module"], f(19, True), col_w - 24):
            d.text((x0 + 12, y), ln, font=f(19, True), fill=col)
            y += 25
        y += 6

        # 附加模块
        if m["extra"]:
            d.rounded_rectangle((x0 + 12, y, x1 - 12, y + 26), radius=6,
                                fill=tuple(int(c * 0.12 + 255 * 0.88) for c in col))
            d.text((x0 + 18, y + 4), m["extra"], font=f(13, True), fill=col)
            y += 34

        # 功能点
        d.text((x0 + 12, y), "承载功能点", font=f(14, True), fill=GREY)
        y += 24
        for p in m["points"]:
            d.ellipse((x0 + 16, y + 8, x0 + 22, y + 14), fill=col)
            for ln in wrap(d, p, f(15), col_w - 50):
                d.text((x0 + 30, y), ln, font=f(15), fill=(60, 54, 48))
                y += 22
            y += 4

        # 附加功能点
        if m["extra_points"]:
            d.text((x0 + 12, y), "附加功能点", font=f(14, True), fill=GREY)
            y += 24
            for p in m["extra_points"]:
                d.ellipse((x0 + 16, y + 8, x0 + 22, y + 14), outline=col, width=2)
                for ln in wrap(d, p, f(14), col_w - 50):
                    d.text((x0 + 30, y), ln, font=f(14), fill=(90, 82, 72))
                    y += 21
                y += 2

        # 涉及文件
        y += 8
        d.line([(x0 + 12, y), (x1 - 12, y)], fill=(230, 223, 212), width=1)
        y += 8
        d.text((x0 + 12, y), "主要涉及文件", font=f(14, True), fill=GREY)
        y += 22
        for fp in m["files"]:
            for ln in wrap(d, "· " + fp, f(13), col_w - 30):
                d.text((x0 + 14, y), ln, font=f(13), fill=(120, 112, 102))
                y += 17

    # ---------- 底部说明 ----------
    by = H - BOTTOM + 10
    d.line([(M, by), (W - M, by)], fill=(210, 200, 186), width=2)
    ty = by + 12
    notes = [
        "分工原则：按功能模块划分，每人负责 1 个模块（4—5 个功能点），四个模块相互独立、可并行开发；"
        "注册 / 登录作为基础支撑模块不单独计为功能项，由组长额外承担。",
        "协作约定：各自在独立工作区开发，通过 Git 按模块分支提交；"
        "公共部分（主题配色、依赖版本、数据库实体）由组长统一维护后再同步给组员。",
    ]
    for n in notes:
        for ln in wrap(d, n, f(14), W - 2 * M):
            d.text((M, ty), ln, font=f(14), fill=(105, 96, 86))
            ty += 20

    out = os.path.join(IMG, "division_of_work.png")
    img.save(out, quality=96)
    print("已生成", out, img.size)
    return out


def build_docx(chart_path):
    from docx import Document
    from docx.shared import Pt, Cm
    from docx.enum.text import WD_ALIGN_PARAGRAPH
    from docx.enum.table import WD_TABLE_ALIGNMENT
    from docx.oxml.ns import qn
    from docx.oxml import OxmlElement

    doc = Document()
    st = doc.styles["Normal"]
    st.font.name = "宋体"
    st.font.size = Pt(12)
    st.element.rPr.rFonts.set(qn("w:eastAsia"), "宋体")
    for s in doc.sections:
        s.top_margin = Cm(2.0)
        s.bottom_margin = Cm(2.0)
        s.left_margin = Cm(2.2)
        s.right_margin = Cm(2.2)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("四人分工表")
    r.font.size = Pt(18)
    r.font.bold = True
    r.font.name = "黑体"
    r.element.rPr.rFonts.set(qn("w:eastAsia"), "黑体")

    doc.add_paragraph().add_run().add_picture(chart_path, width=Cm(17.2))

    # 明细表
    p = doc.add_paragraph()
    r = p.add_run("表 1  M1—M4 分工明细")
    r.font.size = Pt(10.5)
    r.font.bold = True
    r.font.name = "宋体"
    r.element.rPr.rFonts.set(qn("w:eastAsia"), "宋体")

    t = doc.add_table(rows=1, cols=4)
    t.style = "Table Grid"
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    heads = ["成员 / 角色", "承担模块", "功能点", "主要交付物"]
    for i, h in enumerate(heads):
        c = t.rows[0].cells[i]
        c.text = ""
        pp = c.paragraphs[0]
        pp.alignment = WD_ALIGN_PARAGRAPH.CENTER
        rr = pp.add_run(h)
        rr.font.bold = True
        rr.font.size = Pt(10)
        rr.font.name = "黑体"
        rr.element.rPr.rFonts.set(qn("w:eastAsia"), "黑体")
        shd = OxmlElement("w:shd")
        shd.set(qn("w:fill"), "F2EBE0")
        c._tc.get_or_add_tcPr().append(shd)

    rows = [
        ("成员 1\n（组长）", "M1 节气内容展示与检索\n＋ S0 注册 / 登录",
         "F1.1—F1.5（5 项）\nS0.1—S0.5（5 项）",
         "节气内容库、节气罗盘与详情页、\nVSOP87D 天文算法、账号与会话"),
        ("成员 2", "M2 节气打卡与习惯养成", "F2.1—F2.5（5 项）",
         "打卡数据模型与月度日历、\n成长等级与点亮进度"),
        ("成员 3", "M3 打卡广场与社区互动", "F3.1—F3.5（5 项）",
         "动态发布与信息流、\n点赞评论与二级回复"),
        ("成员 4", "M4 地域民俗地图\n＋ M5 每日提醒与收藏",
         "F4.1—F4.4（4 项）\nF5.1—F5.4（4 项）",
         "地图绘制与经纬度投影、\n民俗投稿、提醒调度"),
    ]
    for row in rows:
        cells = t.add_row().cells
        for i, v in enumerate(row):
            cells[i].text = ""
            pp = cells[i].paragraphs[0]
            rr = pp.add_run(v)
            rr.font.size = Pt(9.5)
            rr.font.name = "宋体"
            rr.element.rPr.rFonts.set(qn("w:eastAsia"), "宋体")
    for row in t.rows:
        for i, w in enumerate([3.0, 4.6, 3.4, 5.2]):
            row.cells[i].width = Cm(w)

    doc.add_paragraph()
    for note in [
        "说明一：上表中成员姓名请按实际情况填写。",
        "说明二：注册与登录作为基础支撑模块不单独计为功能项，由组长额外承担。",
        "说明三：四人各自在独立工作区（独立工程副本或 Git 分支）开发，"
        "公共部分（主题、依赖版本、数据库实体）由组长统一维护后同步。",
    ]:
        p = doc.add_paragraph()
        r = p.add_run("· " + note)
        r.font.size = Pt(10.5)
        r.font.name = "宋体"
        r.element.rPr.rFonts.set(qn("w:eastAsia"), "宋体")

    out = os.path.join(DOCS, "节气民俗App-四人分工表.docx")
    doc.save(out)
    print("已生成", out)


if __name__ == "__main__":
    p = build_chart()
    build_docx(p)
