"""运势生成服务：确定性随机 + 语料组合。"""
import hashlib
import random
from datetime import date

from app.data import fortune_corpus as c


def _seeded_rng(seed_key: str) -> random.Random:
    """用 seed 生成可复现的随机数发生器，保证同一天同一人结果一致。"""
    digest = hashlib.sha256(seed_key.encode("utf-8")).hexdigest()
    return random.Random(int(digest[:16], 16))


def generate_fortune(zodiac: str, day: date | None = None) -> dict:
    """生成今日运势。

    zodiac: 生肖（鼠牛虎兔龙蛇马羊猴鸡狗猪）
    day: 日期，默认今天
    """
    day = day or date.today()
    seed_key = f"{zodiac}|{day.isoformat()}"
    rng = _seeded_rng(seed_key)

    # 四个维度各生成一个星级（1~5），用不同子 seed 保证各维度独立
    dimensions = {}
    total = 0
    for i, dim in enumerate(["事业", "财运", "感情", "健康"]):
        stars = rng.randint(1, 5)
        total += stars
        texts = c.FORTUNE_DIMENSIONS[dim][stars]
        dimensions[dim] = {
            "stars": stars,
            "text": texts[rng.randint(0, len(texts) - 1)],
        }

    avg = round(total / 4)  # 综合星级
    summary = c.SUMMARY_BY_SCORE[avg]

    lucky_color = rng.choice(c.LUCKY_COLORS)
    lucky_number = rng.choice(c.LUCKY_NUMBERS)
    yi = rng.choice(c.SUITABLE)
    yi2 = rng.choice(c.SUITABLE)
    ji = rng.choice(c.AVOID)

    return {
        "date": day.isoformat(),
        "zodiac": zodiac,
        "overall_stars": avg,
        "summary": summary,
        "dimensions": dimensions,
        "lucky_color": lucky_color,
        "lucky_number": lucky_number,
        "suitable": f"宜{yi}，宜{yi2}",
        "avoid": f"忌{ji}",
        "disclaimer": "内容为传统文化趣味解读，仅供娱乐参考。",
    }
