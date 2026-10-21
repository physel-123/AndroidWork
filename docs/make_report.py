# -*- coding: utf-8 -*-
"""
按《移动互联网应用开发技术》结课报告模板（需求分析）重做报告。
版式遵循模板批注：
  正文     宋体 小四(12pt) 左对齐 1.5 倍行距
  一级标题  黑体 二号(22pt) 居中
  二级标题  黑体 三号(16pt) 居左加粗
  三级标题  黑体 小四(12pt) 加粗
输出：docs/节气民俗App-结课报告（需求分析）.docx
"""
import os
from docx import Document
from docx.shared import Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

DOCS = r"E:\AndroidWork\docs"
IMG = os.path.join(DOCS, "img")
OUT = os.path.join(DOCS, "节气民俗App-结课报告（需求分析）.docx")

CINNABAR = RGBColor(0x8F, 0x3A, 0x2E)
GREY = RGBColor(0x60, 0x5A, 0x52)

doc = Document()

# ---------------------------------------------------------------------
# 版式：正文宋体小四 1.5 倍行距
# ---------------------------------------------------------------------
st = doc.styles["Normal"]
st.font.name = "宋体"
st.font.size = Pt(12)                       # 小四
st.element.rPr.rFonts.set(qn("w:eastAsia"), "宋体")
st.paragraph_format.line_spacing = 1.5
st.paragraph_format.space_before = Pt(0)
st.paragraph_format.space_after = Pt(0)
st.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.LEFT

# 一级标题：黑体 二号 居中
h1 = doc.styles["Heading 1"]
h1.font.name = "黑体"
h1.font.size = Pt(22)
h1.font.bold = True
h1.font.color.rgb = RGBColor(0, 0, 0)
h1.element.rPr.rFonts.set(qn("w:eastAsia"), "黑体")
h1.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.CENTER
h1.paragraph_format.line_spacing = 1.0
h1.paragraph_format.space_before = Pt(12)
h1.paragraph_format.space_after = Pt(12)

# 二级标题：黑体 三号 居左
h2 = doc.styles["Heading 2"]
h2.font.name = "黑体"
h2.font.size = Pt(16)
h2.font.bold = True
h2.font.color.rgb = RGBColor(0, 0, 0)
h2.element.rPr.rFonts.set(qn("w:eastAsia"), "黑体")
h2.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.LEFT
h2.paragraph_format.line_spacing = 1.0
h2.paragraph_format.space_before = Pt(10)
h2.paragraph_format.space_after = Pt(6)

# 三级标题：黑体 小四
h3 = doc.styles["Heading 3"]
h3.font.name = "黑体"
h3.font.size = Pt(12)
h3.font.bold = True
h3.font.color.rgb = RGBColor(0, 0, 0)
h3.element.rPr.rFonts.set(qn("w:eastAsia"), "黑体")
h3.paragraph_format.space_before = Pt(8)
h3.paragraph_format.space_after = Pt(4)

for s in doc.sections:
    s.top_margin = Cm(2.5)
    s.bottom_margin = Cm(2.5)
    s.left_margin = Cm(3.0)
    s.right_margin = Cm(2.6)


def para(text="", size=12, bold=False, align=None, color=None, first_indent=True,
         font="宋体", space_after=0, line=1.5):
    p = doc.add_paragraph()
    p.paragraph_format.line_spacing = line
    p.paragraph_format.space_after = Pt(space_after)
    if align:
        p.alignment = align
    if first_indent:
        p.paragraph_format.first_line_indent = Pt(size * 2)
    if text:
        r = p.add_run(text)
        r.font.size = Pt(size)
        r.font.bold = bold
        r.font.name = font
        r.element.rPr.rFonts.set(qn("w:eastAsia"), font)
        if color:
            r.font.color.rgb = color
    return p


def body(text):
    return para(text)


def caption(text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.line_spacing = 1.0
    p.paragraph_format.space_before = Pt(6)
    p.paragraph_format.space_after = Pt(10)
    r = p.add_run(text)
    r.font.size = Pt(10.5)
    r.font.name = "宋体"
    r.element.rPr.rFonts.set(qn("w:eastAsia"), "宋体")
    r.font.bold = True
    return p


def picture(path, width_cm, cap=None):
    if not os.path.exists(path):
        para("【缺图：%s】" % os.path.basename(path), color=CINNABAR)
        return
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(6)
    p.add_run().add_picture(path, width=Cm(width_cm))
    if cap:
        caption(cap)


def table(headers, rows, widths=None, font_size=9.5):
    t = doc.add_table(rows=1, cols=len(headers))
    t.style = "Table Grid"
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    hdr = t.rows[0].cells
    for i, h in enumerate(headers):
        hdr[i].text = ""
        p = hdr[i].paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.line_spacing = 1.0
        r = p.add_run(h)
        r.font.bold = True
        r.font.size = Pt(font_size)
        r.font.name = "黑体"
        r.element.rPr.rFonts.set(qn("w:eastAsia"), "黑体")
        shd = OxmlElement("w:shd")
        shd.set(qn("w:fill"), "F2EBE0")
        hdr[i]._tc.get_or_add_tcPr().append(shd)
    for row in rows:
        cells = t.add_row().cells
        for i, v in enumerate(row):
            cells[i].text = ""
            p = cells[i].paragraphs[0]
            p.paragraph_format.line_spacing = 1.0
            p.paragraph_format.space_after = Pt(1)
            r = p.add_run(str(v))
            r.font.size = Pt(font_size)
            r.font.name = "宋体"
            r.element.rPr.rFonts.set(qn("w:eastAsia"), "宋体")
    if widths:
        for row in t.rows:
            for i, w in enumerate(widths):
                row.cells[i].width = Cm(w)
    doc.add_paragraph().paragraph_format.space_after = Pt(2)
    return t


def set_cell(cell, text, bold=False, size=10.5, align=WD_ALIGN_PARAGRAPH.LEFT,
             font=None):
    cell.text = ""
    p = cell.paragraphs[0]
    p.alignment = align
    p.paragraph_format.line_spacing = 1.0
    r = p.add_run(text)
    r.font.size = Pt(size)
    r.font.bold = bold
    f = font or ("黑体" if bold else "宋体")
    r.font.name = f
    r.element.rPr.rFonts.set(qn("w:eastAsia"), f)


# =====================================================================
# 封面（按模板版式）
# =====================================================================
for _ in range(2):
    doc.add_paragraph()

para("大连科技学院", size=18, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER,
     first_indent=False, font="黑体", space_after=6)
para("《移动互联网应用开发技术》", size=16, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER,
     first_indent=False, font="黑体", space_after=30)

cover = doc.add_table(rows=6, cols=2)
cover.alignment = WD_TABLE_ALIGNMENT.CENTER
rows = [("题　　目", "传统民俗节气文化宣传 App ——「节气民俗」"),
        ("学生学号", "＿＿＿＿＿＿＿＿＿＿"),
        ("学生姓名", "＿＿＿＿＿＿＿＿＿＿"),
        ("专业班级", "软件工程（专升本）24-3班"),
        ("教　　师", "侯尚君　　职称：工程师"),
        ("所在单位", "信息科学与技术学院软件工程教研室")]
for i, (k, v) in enumerate(rows):
    c0, c1 = cover.rows[i].cells
    c0.width = Cm(3.2)
    c1.width = Cm(9.0)
    set_cell(c0, k, bold=True, size=12, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_cell(c1, v, size=12, align=WD_ALIGN_PARAGRAPH.LEFT)

doc.add_paragraph()
para("完成日期　　　年　　月　　日", size=12, align=WD_ALIGN_PARAGRAPH.CENTER,
     first_indent=False)
para("（请补填学号、姓名与完成日期）", size=9, align=WD_ALIGN_PARAGRAPH.CENTER,
     first_indent=False, color=GREY)

doc.add_paragraph().add_run().add_break(WD_BREAK.PAGE)

# =====================================================================
# 目录（静态目次，格式同模板）
# =====================================================================
para("目　　录", size=16, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER,
     first_indent=False, font="黑体", space_after=12)
toc = [
    ("第1章  绪　　论", 0), ("1.1  开发背景", 1), ("1.2  国内外研究现状", 1),
    ("1.3  系统设计目标", 1), ("1.4  项目分工", 1),
    ("第2章  项目所采用的技术", 0), ("2.1  JDK", 1), ("2.2  Android Studio", 1),
    ("2.3  Android SDK", 1), ("2.4  SQLite 数据库", 1),
    ("2.5  核心技术选型说明", 1),
    ("第3章  需求分析", 0), ("3.1  系统流程图", 1), ("3.2  系统功能分析", 1),
    ("3.3  系统用例图", 1), ("3.4  系统用例详细说明", 1), ("3.5  系统开发环境", 1),
    ("参考文献", 0), ("附　　录", 0),
]
for text, lvl in toc:
    p = doc.add_paragraph()
    p.paragraph_format.line_spacing = 1.5
    p.paragraph_format.left_indent = Pt(21 * lvl)
    r = p.add_run(text)
    if lvl == 0:
        r.font.size = Pt(16)      # 三号黑体（章）
        r.font.bold = True
        r.font.name = "黑体"
        r.element.rPr.rFonts.set(qn("w:eastAsia"), "黑体")
    else:
        r.font.size = Pt(14)      # 四号宋体（1.1…）
        r.font.name = "宋体"
        r.element.rPr.rFonts.set(qn("w:eastAsia"), "宋体")
para("说明：本目次为静态文本，如需自动更新页码请在 Word 中使用"
     "「引用 → 目录 → 自动目录」，或在定稿后手动补充页码。",
     size=9, first_indent=False, color=GREY, space_after=0)

doc.add_paragraph().add_run().add_break(WD_BREAK.PAGE)

# =====================================================================
# 第1章 绪论
# =====================================================================
doc.add_heading("第1章  绪　　论", level=1)

doc.add_heading("1.1  开发背景", level=2)
body("二十四节气是中国人通过观察太阳周年运动、认知一年中时令、气候、物候变化规律所形成的知识体系，"
     "被誉为“中国第五大发明”。2016 年 11 月 30 日，联合国教科文组织将“二十四节气——中国人通过观察"
     "太阳周年运动而形成的时间知识体系及其实践”正式列入人类非物质文化遗产代表作名录，"
     "标志着这一源自农耕文明的时间知识体系获得了国际公认的文化价值认定。")
body("然而，在城市化与数字化双重冲击下，节气文化的代际传承正面临明显断裂，具体表现在三个方面：")
body("（1）内容枯燥、与年轻人脱节。节气知识主要以农谚、老黄历、教科书片段的形式存在，"
     "表达方式偏“说明文”，缺少视觉化、故事化、碎片化的呈现，年轻人往往“知道有这个节气，"
     "但不知道它跟自己的生活有什么关系”。")
body("（2）缺乏互动与持续触达。现有工具类产品多把节气作为日历中的一行附注信息，用户看一眼即走，"
     "没有形成“第二天还想再打开”的理由，文化传播停留在一次性曝光，难以沉淀为长期认知。")
body("（3）地方性民俗“养在深闺人未识”。节气习俗高度依赖地域，例如浙江衢州的九华立春祭、"
     "浙江台州的送大暑船、山东荣成的谷雨祭海，这些已被列入国家级非物质文化遗产名录的项目，"
     "在本地之外几乎无人知晓，缺少统一的地域化展示载体。")
body("从市场环境看，现有节气类产品可归为三类，整体格局呈现“有工具、缺社区；有内容、缺互动”的特征。"
     "工具型日历（手机系统日历、万年历类 App）仅把节气作为日期标注，无文化内容与留存设计；"
     "内容展示型产品（如「知时节」等节气主题 App）以美图、诗词、节气知识图文为主，"
     "但偏单向浏览，缺乏打卡、评论等互动机制，也没有地域民俗维度；"
     "数字文创型产品（故宫系列读物、节气短视频）内容质量高，"
     "但体验依赖第三方平台，用户沉淀在平台而非文化产品本身。")
body("与此同时，国家层面持续倡导文化自信与“优秀传统文化进校园”。《关于实施中华优秀传统文化传承发展"
     "工程的意见》明确提出要“善于运用现代科技手段，丰富传统文化的表现形式”。"
     "这为面向青少年的轻量化、趣味化节气科普产品提供了明确的政策空间。")
body("综上，本项目选择“传统民俗节气文化宣传 App”作为结课项目，"
     "以打卡与社区互动为抓手，把节气文化做成可 daily 触达、可互动的移动产品，"
     "既契合国家文化传承导向，也符合移动互联网应用开发技术的课程训练目标。")

doc.add_heading("1.2  国内外研究现状", level=2)

doc.add_heading("1.2.1  国内同类产品", level=3)
body("国内与节气相关的移动产品主要分为工具型与内容型两类，代表性产品情况如下表所示。")
table(["产品名称", "定位", "核心功能", "不足与启示"],
      [["知时节\n（iOS）", "节气美学内容 App",
        "二十四节气图文与插画展示；节气诗词与物候知识；节气当天提醒",
        "视觉与文案质量高，是内容型产品的标杆；但偏单向浏览，"
        "本项目据此确立“一节气一色 + 古画配图”的视觉方案并补足互动"],
       ["手机系统日历 /\n万年历类 App", "工具型日历",
        "节气日期标注与提醒；农历、宜忌、节假日查询",
        "工具属性强但文化厚度不足，用户“用完即走”；"
        "本项目保留其每日提醒能力，用内容与打卡补足留存"],
       ["故宫二十四节气\n（读物与数字文创）", "IP 化文化传播",
        "以宫墙、古建、文物讲述节气；成体系图文与动画内容",
        "验证了“用真实文物与古画承载节气”的可行性；"
        "本项目的 30 幅 CC0 公共领域古画配图正是这一思路的低成本实现"]],
      widths=[3.0, 2.6, 4.6, 5.0])
caption("表 1-1  国内同类产品功能对比")

doc.add_heading("1.2.2  国外同类产品", level=3)
body("节气文化在东亚文化圈有共同渊源。日本至今保留“七十二候”体系，"
     "即把每个节气再细分为三候，形成七十二个物候节点，并已出现相应的移动产品。")
table(["产品名称", "国家/地区", "核心功能", "与本项目的差异"],
      [["Madobe\n（まどべ 七十二候）", "日本",
        "以“和风日翻”形式展示当日七十二候；桌面小组件展示候名与物候插画；季节感视觉设计",
        "以“展示 + 小组件”为主，属于静态内容型；"
        "本项目在此基础上增加了打卡、社区互动与地域民俗地图"],
       ["72 Seasons\n（Shichijūniko）", "日本 / 国际",
        "七十二候日期与释义查询；季节性配图与提醒",
        "面向日本本土文化语境；本项目面向中国二十四节气与地方民俗，"
        "并强调“地域性”这一中国民俗的突出特征"]],
      widths=[3.2, 2.2, 5.4, 4.4])
caption("表 1-2  国外同类产品功能对比")
body("总体来看，国外产品在“物候细分（七十二候）”与“桌面常驻”两点上做得比国内产品更细致，"
     "但均不涉及社区互动与地域民俗采集，这正是本项目的差异化空间。")

doc.add_heading("1.2.3  相关学术研究", level=3)
body("国内外学者围绕节气文化的数字化传播已积累了一定成果，主要可分为三个方向。")
body("（1）节气文化的数字艺术表达。杨靖茹在《二十四节气的数字艺术传播方式研究——以动态插画表现为例》"
     "（《大众文艺》2021 年第 5 期）中指出：二十四节气作为我国重要的非物质文化遗产，"
     "在农耕社会式微与数字媒体兴起的背景下正逐渐淡出公众视野；"
     "数字媒体艺术为节气文化提供了新的传播载体，文章以动态插画为例探讨了节气意象的视觉转译方法。"
     "该研究对本项目的启示是：节气内容必须以视觉化、动态化的方式呈现，"
     "因此本项目为 24 个节气各配置真实古画，并以 Canvas 绘制可交互的节气罗盘。")
body("（2）传统文化短视频的情感传播机制。《互动仪式链视域下传统文化短视频的共情传播实践与策略"
     "——以光明日报 B 站号二十四节气倒计时短视频为例》（《新楚文化》2023 年第 35 期）"
     "以柯林斯的“互动仪式链”理论为框架，分析了节气短视频如何通过"
     "“共同关注焦点 + 情感共享”形成用户的情感连带。"
     "该研究对本项目的启示是：文化传播要形成“互动仪式”，单一的内容投放不足以建立情感连接，"
     "必须设计用户可参与、可反馈的互动环节——这正是本项目加入“打卡广场 + 点赞评论”的理论依据。")
body("（3）AR/VR 与传统文化体验。ACM 会议论文《From Embodied Ritual to Cultural Pride: "
     "A Framework for Fostering Cultural Identity through a VR Experience of the Twenty-Four Solar Terms》"
     "提出了通过 VR 具身体验二十四节气仪式来培育文化认同的框架；"
     "另一项针对女书文化的研究《Exploring Cultural Heritage with AR: The TAM Case Study of Nvshu》"
     "基于技术接受模型（TAM）验证了增强现实对文化遗产体验感知有用性与易用性的正向影响。"
     "两项研究共同说明：沉浸式与增强现实技术能显著提升文化体验的参与感。"
     "考虑到课程周期与设备条件，本项目暂未实现 AR 模块，但已在架构上预留扩展点——"
     "配图资源统一由 Coil 组件加载（同一组件可加载静态图、动图与视频首帧）。")
body("综上，现有研究普遍认同“数字化 + 互动化”是传统文化传播的有效路径，"
     "但面向节气文化的、同时具备内容展示、习惯养成（打卡）、社交互动、"
     "地域民俗地图四个维度的轻量化移动产品仍然缺失，本项目正是对这一空白的填补。")

doc.add_heading("1.3  系统设计目标", level=2)
body("本项目的总体目标是：面向青少年与大学生群体，构建一个可 daily 触达、可互动的"
     "二十四节气与地方民俗科普 App，把传统文化从“知识条目”转变为“可参与的生活方式”。"
     "具体目标分解如下表所示。")
table(["目标类别", "具体目标", "衡量方式"],
      [["内容目标", "以离线方式提供 24 节气完整文化档案与地方民俗条目",
        "内置 24 节气内容库 + 34 条各地民俗 + 30 幅公共领域配画"],
       ["准确性目标", "节气日期不依赖人工维护的日期表，可长期自动正确",
        "用天文算法实时计算；公历日期准确率 100%（对比紫金山天文台公布值）"],
       ["留存目标", "让用户“第二天还想打开”",
        "每日提醒推送 + 一键打卡 + 连续打卡天数与等级成长体系"],
       ["互动目标", "把单向浏览变成双向参与",
        "打卡广场支持发布动态、点赞、评论与二级回复；节气详情页内嵌讨论区"],
       ["地域目标", "让地方民俗可见、可探索、可补充",
        "中国地图按真实经纬度打点，支持省份/节气筛选与用户投稿"],
       ["工程目标", "可编译、可安装、可维护，适合课程周期内独立完成",
        "编译零错误、lint 零错误、23 个单元测试全部通过"]],
      widths=[2.4, 6.2, 6.6])
caption("表 1-3  系统设计目标")

doc.add_heading("1.4  项目分工", level=2)
body("本项目按功能模块进行分工，每人负责 1 个功能模块（含 4—5 个功能点）。"
     "注册与登录作为基础支撑模块，由组长额外承担。具体分工见下表。")
table(["功能划分", "负责人", "主要功能点", "交付物"],
      [["M1 节气内容展示与检索\n（兼基础支撑：注册 / 登录）", "＿＿＿＿（组长）",
        "当日节气推荐、天文算法计算节气时刻、二十四节气图谱检索、"
        "节气文化档案、古画配图浏览；注册、登录、会话保持、游客模式",
        "节气内容库、节气罗盘与详情页、\nVSOP87D 算法模块、账号体系"],
       ["M2 节气打卡与习惯养成", "＿＿＿＿",
        "一键打卡、月度打卡日历（补打卡/取消）、连续打卡与成长等级、"
        "二十四节气点亮进度、打卡卡片分享",
        "打卡数据模型与日历、\n成长等级与点亮进度"],
       ["M3 打卡广场与社区互动", "＿＿＿＿",
        "发布图文动态、动态信息流与筛选、点赞、评论与二级回复、"
        "内容删除与分享",
        "动态发布与信息流、\n点赞评论、演示数据播种"],
       ["M4 地域民俗地图\nM5 每日提醒与收藏", "＿＿＿＿",
        "中国地图民俗分布可视化、按省份/节气筛选、民俗详情与分享、"
        "民俗投稿；每日定时提醒、开机恢复、收藏",
        "地图绘制与经纬度投影、\n民俗投稿、提醒调度与收藏"]],
      widths=[4.4, 2.6, 6.0, 3.6])
caption("表 1-4  项目分工表")
body("说明：若小组为 3 人，可将 M5 拆入 M2 与 M4；若为 4 人则按上表执行；"
     "人数更多时可独立出 M6“个人中心与设置”并增加“节气知识闯关答题”模块。")

doc.add_paragraph().add_run().add_break(WD_BREAK.PAGE)

# =====================================================================
# 第2章 项目所采用的技术
# =====================================================================
doc.add_heading("第2章  项目所采用的技术", level=1)

doc.add_heading("2.1  JDK", level=2)
body("JDK（Java Development Kit）是 Android 应用构建与运行的基础环境。"
     "本项目的构建工具链要求 JDK 17 及以上版本，开发中直接使用 Android Studio 自带的 "
     "JetBrains Runtime（JBR）21，无需单独安装配置。")
body("需要特别说明的是：本机系统默认 JDK 为 1.8，而 Android Gradle Plugin 9.x 要求 JDK 17 以上，"
     "因此在 gradle.properties 中通过 org.gradle.java.home 显式指定构建 JDK 为 JBR 21，"
     "确保 Gradle 与 AGP 运行在受支持的环境中。")
body("在本项目中，JDK 提供的关键能力是 java.time 时间 API（LocalDate、LocalDateTime、Duration），"
     "节气天文算法中的儒略日换算、节气日期与时刻计算、倒计时计算均基于该 API 实现。"
     "为此项目将 minSdk 设为 26（Android 8.0），以保证 java.time 在无需脱糖（desugaring）的情况下直接可用。")

doc.add_heading("2.2  Android Studio", level=2)
body("Android Studio 是 Google 官方推出的 Android 集成开发环境，本项目使用版本为 2026.1.1。"
     "在项目开发中，Android Studio 提供了以下支撑：")
body("（1）工程管理与构建：通过 Gradle 构建系统管理依赖、编译、打包与签名，"
     "本项目使用 Kotlin DSL（.kts）编写构建脚本，构建产物为 app-debug.apk（约 24.5 MB）。")
body("（2）编码与调试：提供 Kotlin 语言支持、Compose 实时预览（@Preview）、"
     "Layout Inspector 与 Logcat，配合真机（Android 14）联调完成全部界面与交互验证。")
body("（3）静态检查：内置 Lint 工具对 API 兼容性、资源使用、代码规范进行检查，"
     "本项目最终达到 lint 零错误（仅剩 11 条“存在更新版本”类提示）。")
body("（4）模拟器与真机：支持 AVD 模拟器与 USB 真机调试，本项目主要使用真机验证"
     "（Redmi 平板，Android 14 / API 34）。")

doc.add_heading("2.3  Android SDK", level=2)
body("Android SDK 提供 Android 平台 API、构建工具与运行环境。本项目的 SDK 配置如下表所示。")
table(["配置项", "取值", "说明"],
      [["compileSdk", "36", "编译所用平台版本（Android 16）"],
       ["targetSdk", "36", "目标适配版本"],
       ["minSdk", "26", "最低支持 Android 8.0，保证 java.time API 直接可用"],
       ["buildToolsVersion", "36.x / 37.0.0", "SDK 构建工具"],
       ["Android Gradle Plugin", "9.2.1", "与 Gradle 9.4.1 匹配（AGP 9.x 要求 Gradle ≥ 9.4.1）"],
       ["Gradle", "9.4.1", "构建工具，通过 wrapper 锁定版本"]],
      widths=[4.2, 3.2, 7.8])
caption("表 2-1  Android SDK 与构建工具配置")
body("在 SDK 版本选择上本项目遇到并解决了两个实际问题：一是 AGP 9.x 起内置 Kotlin 支持，"
     "不能再手动应用 org.jetbrains.kotlin.android 插件，否则报“Cannot add extension with name 'kotlin'”；"
     "二是最新的 androidx 与 Coil 依赖要求 compileSdk 37，而本机仅安装 android-36 平台，"
     "因此逐一读取依赖的 AAR 元数据（minCompileSdk），"
     "将依赖锁定在兼容 compileSdk 36 的版本（Compose BOM 2026.06.01、Room 2.8.5、Coil 3.4.0）。")

doc.add_heading("2.4  SQLite 数据库", level=2)
body("SQLite 是 Android 系统内置的轻量级关系型数据库，无需独立进程即可在应用内运行。"
     "本项目通过 Room 持久化框架使用 SQLite，共设计 7 张数据表，"
     "分为业务数据与互动数据两类：")
table(["分类", "表名", "用途", "关键设计"],
      [["业务数据", "check_in", "打卡记录", "以日期字符串（yyyy-MM-dd）作主键，天然防重复打卡"],
       ["", "favorite", "收藏（节气/民俗）", "统一 key（term:立春 / custom:12），一张表服务两类对象"],
       ["", "folk_custom", "地方民俗条目", "isBuiltIn 区分内置数据与用户投稿，内置数据不可删"],
       ["互动数据", "user", "本地账号", "username 唯一索引；密码存盐+摘要，不存明文"],
       ["", "post", "打卡动态", "作者昵称冗余存储，列表渲染免联表；createdAt 建索引"],
       ["", "comment", "评论与回复", "targetType+targetId 服务节气/民俗/动态三类对象；parentId 支持二级回复"],
       ["", "like_record", "点赞记录", "联合主键（类型+对象ID+用户ID）天然防止重复点赞"]],
      widths=[2.2, 2.6, 3.4, 7.0])
caption("表 2-2  数据库表设计")
body("Room 在本项目中提供的关键支撑是 Flow 查询：DAO 方法返回 Flow<List<T>>，"
     "数据一经修改，所有订阅该数据的界面自动刷新。例如用户点击“点赞”，"
     "点赞记录写入 SQLite 后，点赞图标状态与点赞数在广场、动态卡片等界面同步更新，"
     "无需编写任何手动刷新逻辑。")

doc.add_heading("2.5  核心技术选型说明", level=2)
body("除上述开发环境外，项目在应用层选用了以下核心技术，选型总览如下表。")
table(["层次", "技术选型", "版本", "为项目提供的支撑"],
      [["开发语言", "Kotlin", "2.2.10",
        "空安全、扩展函数与协程显著减少样板代码；全部源码均为 Kotlin（无 Java 文件）"],
       ["UI 框架", "Jetpack Compose\n(Material 3)", "Compose 1.11.4",
        "声明式 UI，8 个页面全部用可组合函数实现，工程内无任何 layout XML；"
        "支持用 Kotlin 定义主题，实现“一节气一色”"],
       ["架构模式", "单 Activity +\nCompose Navigation", "Navigation 2.9.8",
        "单 Activity 减少生命周期复杂度；NavHost 统一管理 8 个页面路由与传参"],
       ["数据持久化", "Room", "2.8.5",
        "编译期校验 SQL；Flow 查询实现界面与数据自动同步"],
       ["键值存储", "DataStore", "1.2.1",
        "保存登录态、提醒设置、主题模式；替代 SharedPreferences，原生支持 Flow"],
       ["异步与响应式", "Coroutines + Flow", "—",
        "数据库读写不阻塞主线程；界面状态随数据源自动重组"],
       ["图片加载", "Coil 3", "3.4.0",
        "统一加载静态图/GIF/视频首帧，30 幅古画配图与动态配图全靠它"],
       ["自定义绘图", "Compose Canvas", "—",
        "节气罗盘、中国地图、月历网格均为代码绘制，零图片资源依赖"],
       ["通知与调度", "AlarmManager +\nNotificationCompat", "—",
        "实现每日节气提醒，无需精确闹钟权限，符合省电规范"],
       ["单元测试", "JUnit 4", "4.13.2",
        "天文算法与打卡统计以纯函数实现，共 23 个用例全部通过"]],
      widths=[2.4, 3.4, 2.4, 7.0])
caption("表 2-3  核心技术选型总览")

doc.add_heading("2.5.1  VSOP87D 天文算法（项目技术亮点）", level=3)
body("节气的本质是太阳在黄道上的视位置每运行 15° 所对应的时刻"
     "（立春 315°、春分 0°、夏至 90°、冬至 270°）。若采用“查日期表”的传统做法，"
     "既只能覆盖有限年份，也无法给出精确到分钟的节气时刻。本项目采用 VSOP87D "
     "地球日心黄经的截断周期项级数实时求解：")
body("第一步，计算太阳地心视黄经。用 65 项黄经周期项级数求和得到地球日心黄经，"
     "换算为太阳地心黄经后，施加光行差与章动修正。")
body("第二步，牛顿迭代求根。以平气法给出的常年平均日期为初值，"
     "用太阳日均行度 0.9856473°/天换算角度残差，迭代逼近目标黄经对应时刻，"
     "并以二分法兜底保证求解鲁棒性。")
body("该算法为项目提供的关键支撑是：节气日期可在 1900—2100 年区间内自动保持正确，"
     "无需任何人工维护的日期表。经单元测试与紫金山天文台公布的 2024 年 24 节气时刻逐一比对，"
     "公历日期准确率 24/24，时刻最大偏差约 29 分钟，远优于应用展示需求。")

doc.add_paragraph().add_run().add_break(WD_BREAK.PAGE)

# =====================================================================
# 第3章 需求分析
# =====================================================================
doc.add_heading("第3章  需求分析", level=1)

doc.add_heading("3.1  系统流程图", level=2)
body("整个系统的流程图如图 3.1 所示。")
picture(os.path.join(IMG, "system_flow.png"), 15.0, "图 3.1  系统流程图")
body("由图 3.1 可见，系统流程分为三个阶段：")
body("（1）启动与初始化阶段。用户启动 App 后，系统首先读取本地登录态（DataStore）；"
     "若未登录则进入登录/注册页，同时允许用户选择“先逛逛”以游客身份进入；"
     "随后系统调用天文算法计算当前节气与下一节气，并完成内置内容播种"
     "（24 节气内容库、民俗数据、演示账号），最后进入主界面。")
body("（2）功能入口阶段。主界面底部设置 6 个一级页面入口，"
     "分别为首页（每日节气）、节气（图谱检索）、广场（打卡互动）、"
     "打卡（习惯养成）、民俗地图（地域探索）、我的（设置管理）。")
body("（3）操作与数据循环阶段。用户操作分为三类：浏览内容（只读，无需登录）、"
     "打卡与互动（读写 Room 数据库）、设置与提醒（写入 DataStore 并调度通知）。"
     "任何数据变更都会通过 Flow 触发界面自动重组，形成"
     "“用户操作 → 数据读写 → 界面刷新”的持续循环，直至用户退出应用。")

doc.add_heading("3.2  系统功能分析", level=2)
body("根据流程图可分析出本系统的系统用例图，如图 3.2 所示"
     "（见 3.3 节）。从功能角度看，本系统大体可划分为 6 个功能模块、共 28 项功能点，"
     "另设注册/登录作为基础支撑模块（不单独计为功能项）。")

body("1．节气内容展示与检索模块（M1）")
body("该模块是系统的内容底座，也是用户打开 App 后最先接触的部分，"
     "包含当日节气智能推荐、节气日期天文计算与倒计时、二十四节气图谱检索、"
     "节气文化档案（三候、习俗、食俗、养生、诗词）、古画配图浏览与全屏查看 5 项功能点。")
body("任何用户（含游客）打开本软件时，首页即根据当前系统日期自动计算并展示当前节气，"
     "包括节气名称、所属季节、节气序号、释义以及距下一个节气的倒计时。"
     "用户点击「节气」页可看到以 Canvas 绘制的二十四节气罗盘，"
     "当前节气高亮显示于 12 点方向，点击任一节气即可进入详情页。"
     "详情页展示该节气的由来与含义、三候物候、传统习俗（逐条编号）、时令食俗、"
     "顺时养生建议与节气诗话，并配有 1—2 幅来自大都会艺术博物馆开放数据（CC0 公共领域）"
     "的中国古代绘画，可点击全屏查看。检索支持按春/夏/秋/冬筛选，"
     "也支持按节气名、拼音、习俗与食俗关键词搜索（如输入“饺子”“踏青”）。")

body("2．节气打卡与习惯养成模块（M2）")
body("该模块用于把文化学习转化为可坚持的日常行为，"
     "包含一键打卡、月度打卡日历、连续打卡与成长等级、二十四节气点亮进度、"
     "打卡卡片分享 5 项功能点。")
body("注册用户点击「立即打卡」后，系统先判断当日是否已打卡，"
     "未打卡则弹出心情备注输入框（可跳过），确认后把打卡记录写入数据库"
     "并自动归属到当前所处的节气。"
     "连续天数采用“今天未打卡但昨天打过则不清零”的规则，避免用户产生挫败感；"
     "累计天数对应 7 级传习等级（初识节气→节气新手→节气学徒→民俗达人→"
     "节气守护者→非遗传习人→二十四节气大家）。"
     "月度日历支持对遗漏日期补打卡、对误操作取消打卡，未来日期不可操作；"
     "被打卡过的节气会在“二十四节气点亮进度”中以该节气的主题色显示，形成收集式激励。")

body("3．打卡广场与社区互动模块（M3）")
body("该模块是项目的互动核心，包含发布图文动态、动态信息流与筛选、点赞、"
     "评论与二级回复、内容删除与分享 5 项功能点。")
body("注册用户可在「广场」页发布打卡动态：输入正文（不超过 300 字）、"
     "选择心情标签（清爽、惬意、充实、馋了等）、并从当前节气的配画中选择一张作为配图。"
     "发布后动态立即出现在信息流顶部，其他用户可点赞、评论与回复。"
     "点赞为幂等操作（重复点击即取消）；评论支持二级回复；"
     "用户可删除自己发布的动态与评论。为便于课堂演示多人互动效果，"
     "系统内置 4 个演示账号（密码统一 123456），可在登录页一键切换身份，"
     "并预置了 5 条示例动态及其评论与点赞数据。")

body("4．地域民俗地图模块（M4）")
body("该模块用于解决地方民俗“养在深闺人未识”的问题，"
     "包含中国地图民俗分布可视化、按省份/节气筛选、民俗详情查看与分享、"
     "用户民俗投稿 4 项功能点。")
body("系统以 Canvas 绘制中国省级行政区轮廓，底图轮廓与民俗数据点"
     "统一使用真实经纬度线性投影，因此数据点必然落在正确的省份内。"
     "用户点击地图上的数据点即可查看该条民俗的名称、流传地区、相关节气与详细介绍；"
     "点击省份可直接筛选该省民俗；也可按节气筛选或输入关键词搜索。"
     "注册用户可点击「投稿」补充自己家乡的民俗，"
     "填写名称、省份、城市、相关节气与介绍后，系统按省份中心坐标自动定位到地图，"
     "并以“我投稿的”标识展示，用户可自行删除，内置数据则不可删除。")

body("5．每日提醒与收藏模块（M5）")
body("该模块承担“每日触达”职责，包含每日定时提醒、提醒时刻自定义、"
     "开机自动恢复提醒、节气/民俗收藏 4 项功能点。")
body("用户可在「我的」页开启或关闭每日节气提醒，并通过时间选择器自定义提醒时刻"
     "（默认 08:00）。系统采用 AlarmManager 的非精确重复闹钟实现，"
     "不需要精确闹钟权限，符合 Android 省电规范；设备重启后由开机广播接收器"
     "从 DataStore 恢复调度。通知内容动态生成：当前节气 + 今日习俗 + 今日食俗 + "
     "未打卡提醒，点击通知直达首页。")

body("6．个人中心与设置模块（M6）")
body("该模块包含个人主页与统计、主题模式切换、打卡数据导出与清空、"
     "图片来源与授权展示 4 项功能点。")
body("个人主页展示头像、昵称、个性签名与 8 项统计（累计打卡、连续天数、最长纪录、"
     "本月打卡、我的动态、送出点赞、收藏、点亮节气）。"
     "主题支持跟随系统、浅色、深色三种模式。"
     "打卡记录可导出为 CSV 文本并复制到剪贴板，便于用户自行留存；"
     "也可一键清空本机数据。此外，「我的 → 图片来源与授权」页完整列出了"
     "全部 30 幅配画的作者、年代、材质与藏品编号，体现学术规范。")

body("7．注册与登录模块（基础支撑，由组长承担）", )
body("该模块不单独计为功能项，但为互动类功能的前置条件，"
     "包含注册（账号唯一校验）、登录（加盐摘要校验）、登录态持久化、"
     "游客浏览模式、演示账号一键切换 5 项功能点。")
body("账号保存在本机 SQLite 数据库中，密码不存明文，"
     "采用 SecureRandom 生成 16 字节随机盐、与密码拼接后经 SHA-256 摘要存储；"
     "登录态由 DataStore 持久化，应用重启后自动恢复。"
     "系统默认允许游客浏览全部内容，仅在打卡、点赞、评论、发布动态、投稿等"
     "写操作时提示登录，既降低了使用门槛，也保证了互动数据的真实性。")

table(["模块", "功能点", "对应页面", "优先级"],
      [["M1 节气内容展示与检索",
        "F1.1 当日节气智能推荐；F1.2 节气日期计算与倒计时；F1.3 节气图谱检索；"
        "F1.4 节气文化档案；F1.5 古画配图浏览", "首页、节气、节气详情", "高"],
       ["M2 节气打卡与习惯养成",
        "F2.1 一键打卡（含心情）；F2.2 月度打卡日历；F2.3 连续打卡与等级；"
        "F2.4 节气点亮进度；F2.5 打卡卡片分享", "打卡", "高"],
       ["M3 打卡广场与社区互动",
        "F3.1 发布图文动态；F3.2 信息流与筛选；F3.3 点赞；F3.4 评论与回复；"
        "F3.5 内容删除与分享", "打卡广场", "高"],
       ["M4 地域民俗地图",
        "F4.1 民俗分布可视化；F4.2 省份/节气筛选；F4.3 民俗详情与分享；"
        "F4.4 民俗投稿", "民俗地图", "中"],
       ["M5 每日提醒与收藏",
        "F5.1 每日定时提醒；F5.2 提醒时刻自定义；F5.3 开机自动恢复；F5.4 收藏",
        "我的、各详情页", "中"],
       ["M6 个人中心与设置",
        "F6.1 个人主页与统计；F6.2 主题模式；F6.3 数据导出与清空；F6.4 图片来源展示",
        "我的", "中"],
       ["S0 注册与登录（基础支撑）",
        "S0.1 注册；S0.2 登录；S0.3 会话持久化；S0.4 游客模式；S0.5 演示账号切换",
        "登录/注册页", "高（组长）"]],
      widths=[3.6, 7.4, 3.0, 1.8])
caption("表 3-1  系统功能结构表")

doc.add_heading("3.3  系统用例图", level=2)
body("系统涉及两类参与者：游客（未登录，可浏览全部内容）与注册用户"
     "（可进行打卡、互动、投稿等写操作）。互动类用例均 «include» 登录/注册用例。"
     "系统用例图如图 3.2 所示。")
picture(os.path.join(IMG, "use_case.png"), 15.5, "图 3.2  系统用例图")
body("说明：管理员与内容提供方（文化机构、非遗传承人）在课程版本中由"
     "“内置权威内容 + 用户民俗投稿”承担，未单独设置后台管理角色。")

doc.add_heading("3.4  系统用例详细说明", level=2)
body("下表对 8 个核心用例进行详细说明，后续功能实现与本节内容一一对应。"
     "用例编号规则为 UC-模块号-序号。")

use_cases = [
    ("UC-1-1", "浏览当日节气", "游客 / 注册用户",
     "用户启动 App 后，在首页看到当前节气、精确时刻、释义、今日习俗与食俗，"
     "并了解距离下一个节气的时间。",
     "1. 用户启动 App；2. 系统读取本地登录态；3. 系统用天文算法计算当前节气与下一节气；"
     "4. 系统展示节气主卡片、倒计时、今日一俗、三候、养生与诗话；5. 用户可点击进入节气详情。",
     "首页展示与当天相符的节气信息；倒计时精确到小时",
     "节气日期由算法实时计算，无需联网，无需人工维护"),
    ("UC-1-2", "检索节气图谱", "游客 / 注册用户",
     "用户通过节气罗盘或关键词，快速定位并查看某个节气的完整文化档案。",
     "1. 进入「节气」页；2. 系统展示二十四节气罗盘与节气卡片列表；"
     "3. 用户点击罗盘节气，或按四季筛选，或输入关键词搜索；"
     "4. 系统跳转详情页，展示释义、三候、习俗、食俗、养生、诗词与古画配图。",
     "罗盘可点击定位；搜索支持节气名、拼音、习俗与食俗关键词",
     "配图统一走 Coil 组件，可加载静态图与动图"),
    ("UC-2-1", "每日打卡", "注册用户",
     "用户记录当天与节气的相遇，获得连续打卡天数与成长等级反馈，形成坚持动力。",
     "1. 点击「立即打卡」；2. 系统判断今日是否已打卡；3. 弹出心情备注框（可跳过）；"
     "4. 确认后写入打卡记录并归属当前节气；5. 刷新连续天数、累计天数与点亮进度。",
     "同一日期只能打卡一次；“今天未打卡但昨天打过”时连续天数不清零",
     "打卡表以日期字符串作主键，从数据库层面天然防重复"),
    ("UC-2-2", "查看打卡日历", "注册用户",
     "用户以月历形式回顾打卡情况，并对遗漏日期补打卡或取消误操作。",
     "1. 进入「打卡」页；2. 系统按月历展示打卡状态并标记节气日；"
     "3. 用户点击日期：已打卡则取消，未打卡则补打卡（未来日期不可操作）；"
     "4. 系统同步更新统计与点亮进度。",
     "点击日期可补打卡/取消；节气日有独立标记；未来日期不可操作",
     "统计逻辑以纯函数实现，可直接单元测试"),
    ("UC-3-1", "发布打卡动态", "注册用户",
     "用户把自己的节气生活以图文形式发布到广场，与他人分享。",
     "1. 进入「广场」页点击「发布动态」；2. 系统弹出编辑框；"
     "3. 用户输入正文、选择心情标签、从当前节气配画中选一张作为配图；"
     "4. 写入动态记录并刷新信息流；5. 其他用户可点赞、评论。",
     "正文不为空；配图来自当前节气配画库；发布后立即出现在信息流顶部",
     "作者昵称冗余存储，列表渲染无需联表查询"),
    ("UC-3-2", "点赞与评论", "注册用户",
     "用户对节气、民俗或打卡动态表达态度与看法，形成互动。",
     "1. 点击心形图标点赞（再次点击取消）；2. 点击评论图标展开评论区并发送内容；"
     "3. 可回复某条评论形成二级回复；4. 系统实时更新点赞数与评论数。",
     "点赞幂等；评论支持二级回复；可删除自己的内容",
     "点赞表用「类型+对象ID+用户ID」联合主键防重复；点赞数由真实记录推导"),
    ("UC-4-1", "浏览地域民俗地图", "游客 / 注册用户",
     "用户在中国地图上查看各地民俗分布，并按省份或节气筛选、查看详情。",
     "1. 进入「民俗地图」页；2. 系统按真实经纬度绘制民俗分布点；"
     "3. 点击数据点查看详情，或点击省份筛选；4. 可按节气筛选或关键词搜索；5. 可分享某条民俗。",
     "数据点落在正确省份内；点击省份可筛选；支持省份+节气组合筛选",
     "底图轮廓与数据点使用同一套经纬度投影，确保不错位"),
    ("UC-4-2", "投稿地方民俗", "注册用户",
     "用户补充自己家乡的民俗，让更多地方性知识被看见，实现文化共建。",
     "1. 点击「投稿」；2. 系统弹出投稿表单；3. 填写名称、省份、城市、相关节气与介绍；"
     "4. 系统校验必填项，按省份中心坐标定位到地图并写入数据库；"
     "5. 以“我投稿的”标识展示，用户可自行删除。",
     "名称/省份/城市/介绍为必填；按省份自动定位；用户投稿可删，内置数据不可删",
     "用 isBuiltIn 字段区分来源，保护内置内容不被误删"),
]
table(["用例编号", "用例名称", "参与者", "用例描述", "基本流程", "预期结果", "实现要点"],
      [list(uc) for uc in use_cases],
      widths=[1.6, 2.0, 1.8, 3.6, 4.8, 3.2, 3.2], font_size=8.5)
caption("表 3-2  系统用例详细说明表")

doc.add_heading("3.5  系统开发环境", level=2)
body("本系统的开发与运行环境如下表所示。")
table(["类别", "项目", "配置 / 版本"],
      [["硬件环境", "开发主机", "Windows 10 x64"],
       ["", "测试设备", "Redmi 平板（Android 14 / API 34）"],
       ["软件环境", "操作系统", "Windows 10"],
       ["", "开发工具", "Android Studio 2026.1.1"],
       ["", "构建 JDK", "JetBrains Runtime 21（JBR）"],
       ["", "构建工具", "Gradle 9.4.1 + Android Gradle Plugin 9.2.1"],
       ["", "编译 / 目标 SDK", "compileSdk 36 / targetSdk 36"],
       ["", "最低支持版本", "minSdk 26（Android 8.0）"],
       ["", "开发语言", "Kotlin 2.2.10"],
       ["", "UI 框架", "Jetpack Compose（Material 3，Compose 1.11.4）"],
       ["", "数据库", "SQLite（通过 Room 2.8.5 访问）"],
       ["", "项目类型", "Android 单模块应用（app 模块）"]],
      widths=[2.6, 4.2, 8.4])
caption("表 3-3  系统开发环境")

doc.add_paragraph().add_run().add_break(WD_BREAK.PAGE)

# =====================================================================
# 参考文献
# =====================================================================
doc.add_heading("参考文献", level=1)
refs = [
    "杨靖茹. 二十四节气的数字艺术传播方式研究——以动态插画表现为例[J]. 大众文艺, 2021(05).",
    "互动仪式链视域下传统文化短视频的共情传播实践与策略——以光明日报B站号二十四节气倒计时短视频为例[J]. "
    "新楚文化, 2023(35).",
    "From Embodied Ritual to Cultural Pride: A Framework for Fostering Cultural Identity through a VR "
    "Experience of the Twenty-Four Solar Terms[C]. ACM, 2025.",
    "Exploring Cultural Heritage with AR: The TAM Case Study of Nvshu[C]. Computer Animation and Social "
    "Agents (CASA), Springer, 2025.",
    "中国农业博物馆. 二十四节气——中国人通过观察太阳周年运动而形成的时间知识体系及其实践[Z]. "
    "联合国教科文组织人类非物质文化遗产代表作名录, 2016.",
    "Google. Modern Android App Architecture Guide[EB/OL]. https://developer.android.com/topic/architecture.",
    "Google. Jetpack Compose Documentation[EB/OL]. https://developer.android.com/develop/ui/compose.",
    "Google. Save data in a local database using Room[EB/OL]. "
    "https://developer.android.com/training/data-storage/room.",
    "Jean Meeus. Astronomical Algorithms (2nd Edition)[M]. Willmann-Bell, 1998.",
    "紫金山天文台. 中国天文年历[M]. 北京: 科学出版社.",
]
for i, r in enumerate(refs, start=1):
    p = doc.add_paragraph()
    p.paragraph_format.line_spacing = 1.5
    p.paragraph_format.left_indent = Pt(24)
    p.paragraph_format.first_line_indent = Pt(-24)
    run = p.add_run("[%d]  %s" % (i, r))
    run.font.size = Pt(10.5)
    run.font.name = "宋体"
    run.element.rPr.rFonts.set(qn("w:eastAsia"), "宋体")

doc.add_paragraph().add_run().add_break(WD_BREAK.PAGE)

# =====================================================================
# 附录
# =====================================================================
doc.add_heading("附　　录", level=1)
doc.add_heading("附录 A  同类产品功能截图", level=2)
body("按要求提供 2—3 张同类产品的核心功能截图。由于截图涉及第三方应用界面，"
     "请按下表指引自行截图后替换本节内容（在手机应用商店搜索对应名称即可）。")
table(["序号", "产品", "获取方式", "建议截取的核心功能"],
      [["图 A-1", "知时节（iOS App Store）", "App Store 搜索「知时节」",
        "首页节气展示界面（体现节气图文与视觉风格）"],
       ["图 A-2", "手机系统日历 / 万年历类 App", "手机自带日历或应用商店搜索「万年历」",
        "日历中的节气标注与节气提醒设置"],
       ["图 A-3", "Madobe 七十二候（App Store）", "App Store 搜索「Madobe」",
        "七十二候展示与桌面小组件"]],
      widths=[1.8, 4.6, 4.6, 5.0])
for i in range(3):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(10)
    r = p.add_run("【此处插入图 A-%d 截图】" % (i + 1))
    r.font.size = Pt(10.5)
    r.font.color.rgb = CINNABAR

doc.add_heading("附录 B  工程结构说明", level=2)
body("工程采用单模块结构，源码目录组织如下表所示。")
table(["目录 / 文件", "职责"],
      [["MainActivity.kt", "唯一的 Activity，承载 Compose 导航与登录态判断"],
       ["domain/SolarTermCalculator.kt", "VSOP87D 天文算法、节气时刻求解、儒略日换算"],
       ["domain/TermDateUtils.kt", "当前/下一节气、倒计时、年度节气表（带缓存）"],
       ["domain/CheckInCalculator.kt", "连续打卡、等级、月历排版（纯函数，可单测）"],
       ["data/db/", "Room 实体、DAO、数据库定义（7 张表）"],
       ["data/content/", "24 节气内容库、民俗数据、配画库、每日内容工厂"],
       ["data/", "仓库层：打卡、收藏、民俗、账号、互动"],
       ["ui/screens/", "8 个页面：首页、节气、详情、广场、打卡、地图、我的、图片来源"],
       ["ui/components/", "通用组件、节气罗盘、中国地图、配画展示、评论区"],
       ["ui/theme/", "国风配色（24 节气主题色）、字体、Material 3 主题"],
       ["notify/", "AlarmManager 提醒调度与通知接收器"],
       ["src/test/", "单元测试：天文算法 6 项、节气工具 9 项、打卡逻辑 8 项"]],
      widths=[5.4, 9.8])

doc.save(OUT)
print("已生成：", OUT)
print("大小：%.1f KB" % (os.path.getsize(OUT) / 1024))
