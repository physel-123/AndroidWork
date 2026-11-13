"""运势 + 占卜 路由。"""
from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.services import fortune_service, divination_service

router = APIRouter(prefix="/api", tags=["fortune"])


@router.get("/fortune")
async def get_fortune(zodiac: str):
    """今日运势。zodiac 为生肖（如「鼠」）。"""
    return fortune_service.generate_fortune(zodiac)


@router.get("/divination/sign")
async def draw_sign(username: str = "guest", db: AsyncSession = Depends(get_db)):
    """抽灵签：真随机；结果按用户+当天持久化，再抽覆盖上一次。"""
    return await divination_service.draw_sign(db, username)


@router.get("/divination/sign/today")
async def today_sign(username: str = "guest", db: AsyncSession = Depends(get_db)):
    """获取当天已抽的灵签结果，没有则返回 null。"""
    return await divination_service.today_sign(db, username)


@router.get("/divination/tarot")
async def draw_tarot(username: str = "guest", count: int = 3, db: AsyncSession = Depends(get_db)):
    """抽塔罗牌：真随机；结果按用户+当天持久化，再抽覆盖上一次。"""
    count = max(1, min(count, 5))
    return await divination_service.draw_tarot(db, username, count)


@router.get("/divination/tarot/today")
async def today_tarot(username: str = "guest", db: AsyncSession = Depends(get_db)):
    """获取当天已抽的塔罗结果，没有则返回 null。"""
    return await divination_service.today_tarot(db, username)
