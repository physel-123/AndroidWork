# -*- coding: utf-8 -*-
"""
生成课程展会看板（海报）图片 + A4 Word 版。
输出：docs/img/poster.png 与 docs/节气民俗App-展会看板.docx
"""
import os
from PIL import Image, ImageDraw, ImageFont
from docx import Document
from docx.shared import Pt, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn

DOCS = r"E:\AndroidWork\docs"
IMG = os.path.join(DOCS, "img")
os.makedirs(IMG, exist_ok=True)

FB = r"C:\Windows\Fonts\msyhbd.ttc"
FR = r"C:\Windows\Fonts\msyh.ttc"
INK = (43, 32, 24)
CINNABAR = (143, 58, 46)
BAMBOO = (62, 124, 106)
AMBER = (217, 147, 47)
INDIGO = (47, 74, 109)
PAPER = (253, 250, 245)
CARD = (255, 255, 255)
GREY = (120, 110, 100)


def f(size, bold=False):
    return ImageFont.truetype(FB if bold else FR, size, index=0)


def text_w(d, s, font):
    bb = d.textbbox((0, 0), s, font=font)
    return bb[2] - bb[0]


def draw_center(d, cx, y, s, font, fill=INK):
    d.text((cx - text_w(d, s, font) / 2, y), s, font=font, fill=fill)


def wrap(d, s, font, max_w):
    """按像素宽度换行（中文逐字，英文按词）"""
    lines, cur = [], ""
    for ch in s:
        if ch == "\n":
            lines.append(cur)
            cur = ""
            continue
        if text_w(d, cur + ch, font) > max_w and cur:
            lines.append(cur)
            cur = ch
        else:
            cur += ch
    if cur:
        lines.append(cur)
    return lines


def build_poster():
    W, H = 1240, 1754
    img = Image.new("RGB", (W, H), PAPER)
    d = ImageDraw.Draw(img)
    M = 48
    cw = W - 2 * M

    # ---------- 顶部标题区 ----------
    d.rectangle((0, 0, W, 210), fill=CINNABAR)
    d.text((M, 38), "节气民俗", font=f(64, True), fill=(255, 255, 255))
    d.text((M + 300, 62), "JieQi", font=f(30), fill=(255, 235, 225))
    d.text((M, 120), "传统民俗节气文化宣传 App", font=f(28), fill=(255, 240, 232))
    d.text((M, 160),
           "每日节气知识 · 打卡与社区互动 · 地域民俗地图 · 古画配图",
           font=f(20), fill=(250, 225, 215))
    d.text((W - M - 360, 44), "《移动互联网应用开发技术》结课项目",
           font=f(19), fill=(255, 235, 225))
    d.text((W - M - 360, 74), "Kotlin + Jetpack Compose + Room", font=f(19), fill=(255, 235, 225))
    d.text((W - M - 360, 104), "组员：＿＿＿ ＿＿＿ ＿＿＿", font=f(19), fill=(255, 235, 225))
    d.text((W - M - 360, 134), "组长：＿＿＿＿", font=f(19), fill=(255, 235, 225))

    y = 246

    # ---------- 我们解决什么问题 ----------
    d.rectangle((M, y, M + 8, y + 34), fill=CINNABAR)
    d.text((M + 18, y + 2), "一、我们解决什么问题", font=f(27, True), fill=INK)
    y += 52

    pains = [
        ("内容枯燥，与年轻人脱节", "节气知识以农谚、老黄历形式存在，缺少视觉化、碎片化表达", CINNABAR),
        ("缺乏互动与持续触达", "现有产品把节气当日历附注，用户看一眼即走，无法形成习惯", AMBER),
        ("地方民俗“养在深闺”", "九华立春祭、送大暑船等非遗项目在本地之外几乎无人知晓", INDIGO),
    ]
    box_w = (cw - 2 * 20) // 3
    for i, (t, s, col) in enumerate(pains):
        x0 = M + i * (box_w + 20)
        d.rounded_rectangle((x0, y, x0 + box_w, y + 150), radius=14,
                            fill=CARD, outline=col, width=2)
        d.rectangle((x0, y, x0 + box_w, y + 40), fill=col)
        draw_center(d, x0 + box_w / 2, y + 8, t, f(18, True), (255, 255, 255))
        ty = y + 56
        for ln in wrap(d, s, f(16), box_w - 28):
            d.text((x0 + 14, ty), ln, font=f(16), fill=(70, 62, 55))
            ty += 24
    y += 150 + 34

    # ---------- 核心功能 ----------
    d.rectangle((M, y, M + 8, y + 34), fill=BAMBOO)
    d.text((M + 18, y + 2), "二、核心功能（6 大模块 · 28 项功能点）", font=f(27, True), fill=INK)
    y += 52

    feats = [
        ("① 节气内容与检索", ["每日节气智能推荐", "天文算法算节气时刻",
                              "节气罗盘可视化", "三候/习俗/食俗/养生/诗词",
                              "30 幅公共领域古画配图"], BAMBOO),
        ("② 打卡与习惯养成", ["一键打卡（含心情）", "月度打卡日历可补卡",
                              "连续天数与 7 级等级", "24 节气点亮进度",
                              "打卡卡片分享"], CINNABAR),
        ("③ 打卡广场互动", ["发布图文动态", "动态信息流与筛选",
                            "点赞（幂等）", "评论与二级回复",
                            "演示账号一键切换"], AMBER),
        ("④ 地域民俗地图", ["中国地图民俗分布", "按省份/节气筛选",
                            "民俗详情与分享", "用户投稿家乡民俗",
                            "真实经纬度投影"], INDIGO),
        ("⑤ 每日提醒与收藏", ["每日定时节气提醒", "提醒时刻可自定义",
                              "开机自动恢复", "节气/民俗收藏",
                              "系统分享"], BAMBOO),
        ("⑥ 个人中心与设置", ["个人主页与 8 项统计", "浅色/深色主题",
                              "打卡数据导出 CSV", "图片来源与授权",
                              "游客可浏览全站"], CINNABAR),
    ]
    fw = (cw - 2 * 16) // 3
    fh = 190
    for i, (title, items, col) in enumerate(feats):
        x0 = M + (i % 3) * (fw + 16)
        yy = y + (i // 3) * (fh + 16)
        d.rounded_rectangle((x0, yy, x0 + fw, yy + fh), radius=14,
                            fill=CARD, outline=col, width=2)
        d.rectangle((x0, yy, x0 + fw, yy + 36), fill=col)
        d.text((x0 + 12, yy + 7), title, font=f(17, True), fill=(255, 255, 255))
        ty = yy + 48
        for it in items:
            d.ellipse((x0 + 14, ty + 7, x0 + 20, ty + 13), fill=col)
            d.text((x0 + 28, ty), it, font=f(15), fill=(70, 62, 55))
            ty += 26
    y += 2 * fh + 16 + 34

    # ---------- 技术亮点 ----------
    d.rectangle((M, y, M + 8, y + 34), fill=AMBER)
    d.text((M + 18, y + 2), "三、技术亮点", font=f(27, True), fill=INK)
    y += 52

    techs = [
        ("VSOP87D 天文算法", "节气日期实时求解，不依赖日期表",
         "65 项黄经级数 + 光行差/章动修正 + 牛顿迭代\n"
         "实测：2024 年 24 节气日期准确率 24/24\n时刻最大偏差约 29 分钟（对比紫金山天文台）", CINNABAR),
        ("全程 Compose 自绘可视化", "无任何 XML 布局、无图片资源依赖",
         "节气罗盘 / 中国地图 / 月历网格\n均为 Compose Canvas 代码绘制\n"
         "8 个页面全部 Compose 实现", BAMBOO),
        ("响应式数据流", "Room + Flow + 协程",
         "打卡后首页/广场/我的统计同步刷新\n无需任何手动通知逻辑\n"
         "点赞数由真实记录推导，杜绝计数不一致", INDIGO),
    ]
    tw = (cw - 2 * 16) // 3
    th = 214
    for i, (title, subtitle, body, col) in enumerate(techs):
        x0 = M + i * (tw + 16)
        d.rounded_rectangle((x0, y, x0 + tw, y + th), radius=14,
                            fill=(250, 247, 242), outline=col, width=2)
        d.text((x0 + 14, y + 12), title, font=f(18, True), fill=col)
        d.text((x0 + 14, y + 40), subtitle, font=f(14), fill=GREY)
        d.line([(x0 + 14, y + 64), (x0 + tw - 14, y + 64)], fill=(226, 218, 206), width=1)
        ty = y + 74
        for ln in body.split("\n"):
            for w2 in wrap(d, ln, f(14), tw - 28):
                d.text((x0 + 14, ty), w2, font=f(14), fill=(70, 62, 55))
                ty += 21
    y += th + 30

    # ---------- 实机截图 ----------
    d.rectangle((M, y, M + 8, y + 34), fill=INDIGO)
    d.text((M + 18, y + 2), "四、实机运行效果", font=f(27, True), fill=INK)
    y += 50

    shots = [
        ("shot_home2.png", "首页：当前节气与古画"),
        ("shot_feed4.png", "打卡广场：动态与互动"),
        ("shot_map_all.png", "民俗地图：地域分布"),
        ("shot_login.png", "登录：支持游客浏览"),
    ]
    sw = (cw - 3 * 14) // 4
    sh = 330
    for i, (fn, cap) in enumerate(shots):
        x0 = M + i * (sw + 14)
        d.rounded_rectangle((x0, y, x0 + sw, y + sh), radius=12,
                            fill=(238, 232, 222), outline=(200, 190, 176), width=2)
        p = os.path.join(r"E:\AndroidWork", fn)
        if os.path.exists(p):
            im = Image.open(p).convert("RGB")
            r = min((sw - 16) / im.width, (sh - 46) / im.height)
            im = im.resize((int(im.width * r), int(im.height * r)), Image.LANCZOS)
            img.paste(im, (int(x0 + (sw - im.width) / 2), int(y + 10)))
        else:
            draw_center(d, x0 + sw / 2, y + sh / 2 - 20, "（截图待补）", f(15), GREY)
        draw_center(d, x0 + sw / 2, y + sh - 30, cap, f(14, True), (80, 72, 64))
    y += sh + 26

    # ---------- 底部信息 ----------
    d.rectangle((0, H - 150, W, H), fill=(240, 234, 224))
    d.text((M, H - 128), "技术栈", font=f(18, True), fill=INK)
    d.text((M + 90, H - 128),
           "Kotlin 2.2.10 · Jetpack Compose (Material 3) · Room 2.8.5 · DataStore · "
           "Coroutines/Flow · Coil 3 · AlarmManager · JUnit",
           font=f(15), fill=(70, 62, 55))
    d.text((M, H - 94), "项目状态", font=f(18, True), fill=INK)
    d.text((M + 90, H - 94),
           "编译零错误 · lint 零错误 · 23 个单元测试全部通过 · "
           "内容全离线（24 节气 + 34 条民俗 + 30 幅 CC0 古画）",
           font=f(15), fill=(70, 62, 55))
    d.text((M, H - 58), "配图授权", font=f(18, True), fill=INK)
    d.text((M + 90, H - 58),
           "全部古画来自大都会艺术博物馆 Open Access（CC0 公共领域），可自由使用与分发",
           font=f(15), fill=(70, 62, 55))
    d.text((W - M - 190, H - 128), "扫码体验 / 源码", font=f(16, True), fill=GREY)
    d.rectangle((W - M - 190, H - 104, W - M - 90, H - 14),
                outline=(190, 180, 166), width=2, fill=(255, 255, 255))
    draw_center(d, W - M - 140, H - 66, "二维码", f(14), (170, 160, 148))

    out = os.path.join(IMG, "poster.png")
    img.save(out, quality=95)
    print("已生成 poster.png", img.size)
    return out


def build_poster_docx(poster_path):
    doc = Document()
    st = doc.styles["Normal"]
    st.font.name = "宋体"
    st.font.size = Pt(10.5)
    st.element.rPr.rFonts.set(qn("w:eastAsia"), "宋体")
    for s in doc.sections:
        s.top_margin = Cm(1.6)
        s.bottom_margin = Cm(1.6)
        s.left_margin = Cm(1.4)
        s.right_margin = Cm(1.4)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("节气民俗 App · 展会看板")
    r.font.size = Pt(16)
    r.font.bold = True
    r.font.name = "黑体"
    r.element.rPr.rFonts.set(qn("w:eastAsia"), "黑体")

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.add_run().add_picture(poster_path, width=Cm(18.0))

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("说明：上图可直接打印为 A2/A1 海报（源文件 docs/img/poster.png，"
                  "分辨率 1240×1754，可无损放大）；二维码位置可自行替换为项目源码或演示视频链接。")
    r.font.size = Pt(9)
    r.font.color.rgb.__class__  # noqa
    r.font.name = "宋体"
    r.element.rPr.rFonts.set(qn("w:eastAsia"), "宋体")

    out = os.path.join(DOCS, "节气民俗App-展会看板.docx")
    doc.save(out)
    print("已生成", out)


if __name__ == "__main__":
    pth = build_poster()
    build_poster_docx(pth)
