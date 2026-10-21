# -*- coding: utf-8 -*-
"""
生成需求分析报告所需的两张图：
  1. 系统流程图（system_flow.png）
  2. 系统用例图（use_case.png）
输出到 E:\\AndroidWork\\docs\\img\\
"""
import os
from PIL import Image, ImageDraw, ImageFont

OUT = r"E:\AndroidWork\docs\img"
os.makedirs(OUT, exist_ok=True)

FONT_BOLD = r"C:\Windows\Fonts\msyhbd.ttc"
FONT_REG = r"C:\Windows\Fonts\msyh.ttc"


def font(size, bold=False):
    return ImageFont.truetype(FONT_BOLD if bold else FONT_REG, size, index=0)


# 设计配色（与 App 国风配色一致）
INK = (43, 32, 24)
PAPER = (255, 253, 249)
CINNABAR = (143, 58, 46)
BAMBOO = (62, 124, 106)
AMBER = (217, 147, 47)
INDIGO = (47, 74, 109)
LINE = (150, 138, 122)
FILL_TERM = (247, 240, 228)
FILL_DEC = (252, 244, 226)


def center_text(d, box, text, f, fill=INK, line_gap=4):
    """在矩形区域内居中绘制（支持 \n 换行）"""
    x0, y0, x1, y1 = box
    lines = text.split("\n")
    heights = []
    widths = []
    for ln in lines:
        bb = d.textbbox((0, 0), ln, font=f)
        widths.append(bb[2] - bb[0])
        heights.append(bb[3] - bb[1])
    total_h = sum(heights) + line_gap * (len(lines) - 1)
    cy = y0 + (y1 - y0 - total_h) / 2
    for i, ln in enumerate(lines):
        bb = d.textbbox((0, 0), ln, font=f)
        w = bb[2] - bb[0]
        h = bb[3] - bb[1]
        d.text((x0 + (x1 - x0 - w) / 2 - bb[0], cy - bb[1]), ln, font=f, fill=fill)
        cy += h + line_gap


def rounded(d, box, text, f, fill=FILL_TERM, outline=LINE, radius=12, width=2):
    d.rounded_rectangle(box, radius=radius, fill=fill, outline=outline, width=width)
    center_text(d, box, text, f)


def arrow(d, p1, p2, color=LINE, width=3, head=11):
    d.line([p1, p2], fill=color, width=width)
    import math
    ang = math.atan2(p2[1] - p1[1], p2[0] - p1[0])
    for da in (math.radians(155), math.radians(-155)):
        d.line([p2, (p2[0] + head * math.cos(ang + da), p2[1] + head * math.sin(ang + da))],
               fill=color, width=width)


def arrow_path(d, pts, color=LINE, width=3):
    for i in range(len(pts) - 1):
        if i == len(pts) - 2:
            arrow(d, pts[i], pts[i + 1], color, width)
        else:
            d.line([pts[i], pts[i + 1]], fill=color, width=width)


def diamond(d, box, text, f, fill=FILL_DEC, outline=AMBER):
    x0, y0, x1, y1 = box
    cx, cy = (x0 + x1) / 2, (y0 + y1) / 2
    d.polygon([(cx, y0), (x1, cy), (cx, y1), (x0, cy)], fill=fill, outline=outline, width=2)
    center_text(d, (x0 + 14, y0 + 8, x1 - 14, y1 - 8), text, f)


# =====================================================================
# 图 1：系统流程图
# =====================================================================
def build_flow():
    W, H = 1080, 1680
    img = Image.new("RGB", (W, H), PAPER)
    d = ImageDraw.Draw(img)
    f_title = font(30, True)
    f_box = font(19)
    f_small = font(16)
    f_note = font(15)

    d.text((40, 26), "图 2-1  节气民俗 App 系统流程图", font=f_title, fill=INK)
    d.line([(40, 68), (W - 40, 68)], fill=CINNABAR, width=3)

    CX = W // 2          # 主流程中线
    BW = 380             # 主流程框宽
    LX = CX - BW // 2
    RX = CX + BW // 2

    def box(y, h, text, fill=FILL_TERM):
        rounded(d, (LX, y, RX, y + h), text, f_box, fill=fill)
        return y, y + h

    # ---- 主流程 ----
    y = 96
    box(y, 56, "启动 App", (232, 240, 236)); y += 56
    arrow(d, (CX, y), (CX, y + 26)); y += 26

    box(y, 60, "读取本地登录态（DataStore）", (250, 246, 238)); y += 60
    arrow(d, (CX, y), (CX, y + 26)); y += 26

    # 判断：是否已登录
    dh = 96
    diamond(d, (LX + 40, y, RX - 40, y + dh), "是否已登录？", f_box)
    y += dh
    # 否 -> 右
    arrow_path(d, [(RX - 40, y - dh // 2), (RX + 90, y - dh // 2), (RX + 90, y + 20)])
    d.text((RX + 96, y - dh // 2 - 26), "否", font=f_small, fill=CINNABAR)
    rounded(d, (RX + 20, y + 20, RX + 160, y + 116), "登录 / 注册页\n（可先逛逛）", f_small,
            (252, 240, 236), CINNABAR)
    arrow_path(d, [(RX + 90, y + 116), (RX + 90, y + 170), (CX, y + 170)])
    d.text((RX - 30, y - dh // 2 - 26), "是", font=f_small, fill=BAMBOO)
    y += 190
    arrow(d, (CX, y - 20), (CX, y), BAMBOO)

    box(y, 60, "加载节气天文算法\n计算当前节气 / 下一节气与倒计时", (240, 246, 243)); y += 60
    arrow(d, (CX, y), (CX, y + 26)); y += 26

    box(y, 56, "播种内置内容\n（24 节气库 + 民俗 + 演示账号）", (250, 246, 238)); y += 56
    arrow(d, (CX, y), (CX, y + 26)); y += 26

    box(y, 56, "进入主界面（底部 6 个一级页面）", (232, 240, 236)); y += 56
    arrow(d, (CX, y), (CX, y + 26)); y += 26

    # 六个模块横向排列
    mod_y = y
    mods = ["首页\n每日节气", "节气\n图谱检索", "广场\n打卡互动",
            "打卡\n习惯养成", "民俗地图\n地域探索", "我的\n设置管理"]
    mw, gap = 150, 12
    total = len(mods) * mw + (len(mods) - 1) * gap
    sx = (W - total) // 2
    for i, m in enumerate(mods):
        x0 = sx + i * (mw + gap)
        rounded(d, (x0, mod_y, x0 + mw, mod_y + 84), m, f_small, (246, 243, 236), LINE, 10)
        arrow(d, (x0 + mw // 2, mod_y - 26), (x0 + mw // 2, mod_y - 2), LINE, 2, 8)
    d.line([(CX, mod_y - 26), (sx + mw // 2, mod_y - 26)], fill=LINE, width=3)
    d.line([(CX, mod_y - 26), (sx + total - mw // 2, mod_y - 26)], fill=LINE, width=3)
    y = mod_y + 84

    # 汇聚
    for i in range(len(mods)):
        x0 = sx + i * (mw + gap) + mw // 2
        d.line([(x0, y), (x0, y + 30)], fill=LINE, width=2)
    d.line([(sx + mw // 2, y + 30), (sx + total - mw // 2, y + 30)], fill=LINE, width=2)
    d.line([(CX, y + 30), (CX, y + 56)], fill=LINE, width=3)
    arrow(d, (CX, y + 30), (CX, y + 58))
    y += 58

    # 内层循环：用户操作
    dh2 = 104
    diamond(d, (LX - 10, y, RX + 10, y + dh2),
            "用户操作类型？", f_box)
    y += dh2

    branches = [
        ("浏览内容", "无需登录\n读取内置内容 + 已存数据", BAMBOO),
        ("打卡 / 互动", "读写 Room 数据库\n（打卡、点赞、评论、动态）", CINNABAR),
        ("设置 / 提醒", "写入 DataStore\n调度 AlarmManager 通知", INDIGO),
    ]
    bw, bgap = 300, 24
    total_b = len(branches) * bw + (len(branches) - 1) * bgap
    bx = (W - total_b) // 2
    by = y + 40
    for i, (name, desc, col) in enumerate(branches):
        x0 = bx + i * (bw + bgap)
        rounded(d, (x0, by, x0 + bw, by + 108), name + "\n" + desc, f_small,
                (252, 248, 244), col, 12)
        arrow(d, (x0 + bw // 2, y - 4), (x0 + bw // 2, by - 2), col, 3, 9)
        d.line([(x0 + bw // 2, y + 6), (x0 + bw // 2, y + 6)], fill=col, width=1)
    # 从菱形底部引出横向总线
    d.line([(CX, y - 6), (CX, y + 22)], fill=LINE, width=3)
    d.line([(bx + bw // 2, y + 22), (bx + total_b - bw // 2, y + 22)], fill=LINE, width=3)
    y = by + 108

    # 各分支汇聚回主流程
    for i in range(len(branches)):
        x0 = bx + i * (bw + bgap) + bw // 2
        d.line([(x0, y), (x0, y + 28)], fill=LINE, width=2)
    d.line([(bx + bw // 2, y + 28), (bx + total_b - bw // 2, y + 28)], fill=LINE, width=2)
    d.line([(CX, y + 28), (CX, y + 56)], fill=LINE, width=3)
    y += 56

    box(y, 56, "刷新界面（Compose 响应式重组）", (240, 246, 243)); y += 56
    arrow(d, (CX, y), (CX, y + 26)); y += 26

    # 返回回到内层循环
    loop_y = y
    arrow_path(d, [(CX, loop_y), (CX, loop_y + 34)], BAMBOO)
    box(loop_y + 34, 56, "是否退出 App？", (250, 246, 238))
    # 否 -> 回到「用户操作类型」
    back_x = LX - 60
    arrow_path(d, [(LX, loop_y + 62), (back_x, loop_y + 62), (back_x, mod_y + 190),
                   (LX - 10, mod_y + 190)], BAMBOO)
    d.text((back_x + 8, loop_y + 20), "否，继续使用", font=f_note, fill=BAMBOO)
    y = loop_y + 34 + 56

    # 是 -> 结束
    arrow(d, (CX, y), (CX, y + 26), CINNABAR)
    d.text((CX + 10, y + 4), "是", font=f_small, fill=CINNABAR)
    y += 26
    rounded(d, (CX - 110, y, CX + 110, y + 56), "结束", f_box, (240, 232, 230), CINNABAR)
    y += 56

    # 图注
    note = ("说明：图中虚线回路表示“用户操作 → 数据读写 → 界面刷新”的持续循环；\n"
            "节气日期由 VSOP87D 天文算法实时计算，不依赖网络与日期表。")
    d.multiline_text((40, H - 76), note, font=f_note, fill=(110, 100, 90), spacing=6)

    img.save(os.path.join(OUT, "system_flow.png"))
    print("已生成 system_flow.png", img.size)


# =====================================================================
# 图 2：系统用例图
# =====================================================================
def build_usecase():
    W, H = 1180, 700
    img = Image.new("RGB", (W, H), PAPER)
    d = ImageDraw.Draw(img)
    f_title = font(30, True)
    f_actor = font(17, True)
    f_uc = font(16)
    f_note = font(15)
    f_group = font(15, True)

    d.text((40, 26), "图 3-1  节气民俗 App 系统用例图", font=f_title, fill=INK)
    d.line([(40, 68), (W - 40, 68)], fill=CINNABAR, width=3)

    def actor(cx, cy, name):
        d.ellipse((cx - 19, cy - 58, cx + 19, cy - 20), outline=CINNABAR, width=3)
        d.line([(cx, cy - 20), (cx, cy + 20)], fill=CINNABAR, width=3)
        d.line([(cx - 28, cy - 4), (cx + 28, cy - 4)], fill=CINNABAR, width=3)
        d.line([(cx, cy + 20), (cx - 22, cy + 54)], fill=CINNABAR, width=3)
        d.line([(cx, cy + 20), (cx + 22, cy + 54)], fill=CINNABAR, width=3)
        bb = d.textbbox((0, 0), name, font=f_actor)
        d.text((cx - (bb[2] - bb[0]) / 2 - bb[0], cy + 60), name, font=f_actor, fill=INK)

    def uc(cx, cy, text, w=228, h=58, color=BAMBOO):
        d.ellipse((cx - w // 2, cy - h // 2, cx + w // 2, cy + h // 2),
                  fill=(252, 250, 246), outline=color, width=2)
        center_text(d, (cx - w // 2 + 8, cy - h // 2 + 4, cx + w // 2 - 8, cy + h // 2 - 4),
                    text, f_uc)

    # 系统边界
    bx0, by0, bx1, by1 = 250, 96, W - 250, H - 100
    d.rounded_rectangle((bx0, by0, bx1, by1), radius=18, outline=LINE, width=2)
    d.text((bx0 + 14, by0 + 10), "节气民俗 App 系统边界", font=f_note, fill=(120, 110, 100))

    mid = (bx0 + bx1) // 2
    col_l = mid - 128
    col_r = mid + 128

    # 参与者
    visitor_x = 108
    user_x = W - 108
    actor(visitor_x, 250, "游客")
    actor(user_x, 250, "注册用户")

    browse = [("浏览节气知识", col_l, 172), ("节气图谱检索", col_l, 254),
              ("地域民俗地图", col_r, 172), ("收藏与分享", col_l, 336)]
    interact = [("节气打卡", col_l, 452), ("发布打卡动态", col_r, 254),
                ("点赞与评论", col_r, 336), ("民俗投稿", col_r, 452)]

    for text, cx, cy in browse:
        uc(cx, cy, text, color=BAMBOO)
    for text, cx, cy in interact:
        uc(cx, cy, text, color=CINNABAR)

    # 分组分隔线
    sep_y = 396
    d.line([(bx0 + 24, sep_y), (bx1 - 24, sep_y)], fill=(220, 212, 200), width=2)
    d.text((bx0 + 30, sep_y + 6), "浏览类用例（游客可用）", font=f_group, fill=BAMBOO)
    d.text((bx1 - 210, sep_y + 6), "互动类用例（需登录）", font=f_group, fill=CINNABAR)

    # 登录 / 注册（互动用例 include 它）
    lx, ly = mid - 128, 556
    uc(lx, ly, "登录 / 注册", w=250, color=INDIGO)

    # 游客 -> 浏览类
    for _, cx, cy in browse:
        d.line([(visitor_x + 30, 250), (cx - 114, cy)], fill=(203, 193, 178), width=2)

    # 注册用户 -> 互动类 + 登录
    for _, cx, cy in interact:
        d.line([(user_x - 30, 250), (cx + 114, cy)], fill=(203, 193, 178), width=2)
    d.line([(user_x - 30, 250), (lx + 125, ly)], fill=(203, 193, 178), width=2)

    # include 关系：仅从相邻的两个互动用例引出，避免线条杂乱
    arrow(d, (col_l, 452 + 29), (lx + 20, ly - 22), (170, 160, 148), 2, 9)
    arrow(d, (col_r, 452 + 29), (lx + 110, ly - 24), (170, 160, 148), 2, 9)
    d.text((lx + 118, ly - 4), "«include»  互动操作前需登录", font=f_note, fill=INDIGO)

    d.text((bx0 + 20, by1 + 18),
           "说明：管理员 / 内容提供方（文化机构、非遗传承人）在课程版本中由「内置内容 + 民俗投稿」承担，"
           "未单独设置后台角色。",
           font=f_note, fill=(120, 110, 100))

    img.save(os.path.join(OUT, "use_case.png"))
    print("已生成 use_case.png", img.size)


if __name__ == "__main__":
    build_flow()
    build_usecase()
