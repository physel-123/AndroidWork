"""占卜服务：灵签抽签 + 塔罗抽牌。

抽签结果按「用户 + 类型 + 当天」持久化到数据库：
- 每次抽签都是真随机（SystemRandom）
- 当天抽完后再次打开仍显示该签（从库中读取）
- 再抽一次则覆盖上一次的结果
"""
import json
import random
from datetime import date, datetime, timezone

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.data import divination_corpus as c
from app.models.entities import FortuneRecord

# 真随机：使用系统熵源，不依赖可预测的 seed
_RNG = random.SystemRandom()


def _draw_sign_once() -> dict:
    """抽取一支灵签（真随机），返回结果字典（不含 date/type）。"""
    sign = _RNG.choice(c.SIGN_LIST)
    return {
        "name": sign["name"],
        "level": sign["level"],
        "category": sign.get("category", "综合"),
        "poem": sign["poem"],
        "interpret": sign["interpret"],
        "total_signs": len(c.SIGN_LIST),
    }


def _draw_tarot_once(count: int) -> dict:
    """抽取塔罗牌（真随机），返回结果字典（不含 date/type）。"""
    deck = c.TAROT_DECK
    cards = _RNG.sample(deck, min(count, len(deck)))
    positions = ["过去", "现在", "未来", "关键因素", "建议"][: len(cards)]

    result_cards = []
    levels = []
    for i, card in enumerate(cards):
        upright = _RNG.random() > 0.5
        orientation = "正位" if upright else "逆位"
        meaning = card["upright"] if upright else card["reversed"]
        suit = card.get("suit", "大阿卡纳")
        result_cards.append({
            "position": positions[i],
            "name": card["name"],
            "suit": suit,
            "orientation": orientation,
            "meaning": meaning,
        })
        levels.append(4 if upright else 2)

    avg = round(sum(levels) / len(levels)) if levels else 3
    summary = c.TAROT_SUMMARY.get(avg, c.TAROT_SUMMARY[3])

    return {
        "deck_size": len(deck),
        "cards": result_cards,
        "overall_level": avg,
        "summary": summary,
    }


async def _save_record(
    db: AsyncSession, username: str, kind: str, result: dict
) -> None:
    """保存/覆盖当天某用户某类型的结果（按 username+kind 唯一）。"""
    today = date.today().isoformat()
    seed_key = f"{username}|{kind}"
    # 查当天已有记录（同一天同类型只保留一条）
    existing = (
        await db.execute(
            select(FortuneRecord).where(
                FortuneRecord.username == username,
                FortuneRecord.kind == kind,
                FortuneRecord.seed_key == seed_key,
            )
        )
    ).scalar_one_or_none()

    if existing:
        existing.result = json.dumps(result, ensure_ascii=False)
        existing.created_at = datetime.now(timezone.utc)
    else:
        db.add(FortuneRecord(
            username=username,
            kind=kind,
            seed_key=seed_key,
            result=json.dumps(result, ensure_ascii=False),
        ))
    await db.commit()


async def _load_record(
    db: AsyncSession, username: str, kind: str
) -> dict | None:
    """读取当天某用户某类型的结果，没有则返回 None。"""
    seed_key = f"{username}|{kind}"
    row = (
        await db.execute(
            select(FortuneRecord).where(
                FortuneRecord.username == username,
                FortuneRecord.kind == kind,
                FortuneRecord.seed_key == seed_key,
            )
        )
    ).scalar_one_or_none()
    if not row:
        return None
    return json.loads(row.result)


def _sign_payload(result: dict, day: date | None = None) -> dict:
    day = day or date.today()
    return {
        "type": "sign",
        "date": day.isoformat(),
        "name": result["name"],
        "level": result["level"],
        "category": result.get("category", "综合"),
        "poem": result["poem"],
        "interpret": result["interpret"],
        "total_signs": result.get("total_signs", len(c.SIGN_LIST)),
        "disclaimer": "签文为传统民俗文化趣味内容，仅供娱乐参考。",
    }


def _tarot_payload(result: dict, day: date | None = None) -> dict:
    day = day or date.today()
    return {
        "type": "tarot",
        "date": day.isoformat(),
        "deck_size": result.get("deck_size", len(c.TAROT_DECK)),
        "cards": result["cards"],
        "overall_level": result["overall_level"],
        "summary": result["summary"],
        "disclaimer": "塔罗牌为娱乐性质的趣味占卜，仅供参考。",
    }


async def draw_sign(db: AsyncSession, username: str = "guest", day: date | None = None) -> dict:
    """抽灵签：真随机；结果持久化，再抽覆盖上一次。"""
    result = _draw_sign_once()
    await _save_record(db, username, "sign", result)
    return _sign_payload(result, day)


async def draw_tarot(db: AsyncSession, username: str = "guest", count: int = 3, day: date | None = None) -> dict:
    """抽塔罗：真随机；结果持久化，再抽覆盖上一次。"""
    count = max(1, min(count, 5))
    result = _draw_tarot_once(count)
    await _save_record(db, username, "tarot", result)
    return _tarot_payload(result, day)


async def today_sign(db: AsyncSession, username: str = "guest", day: date | None = None) -> dict | None:
    """获取当天已保存的灵签结果，没有则返回 None。"""
    result = await _load_record(db, username, "sign")
    return _sign_payload(result, day) if result else None


async def today_tarot(db: AsyncSession, username: str = "guest", day: date | None = None) -> dict | None:
    """获取当天已保存的塔罗结果，没有则返回 None。"""
    result = await _load_record(db, username, "tarot")
    return _tarot_payload(result, day) if result else None
