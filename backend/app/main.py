"""FastAPI 应用入口。"""
import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.core.database import engine, Base, async_session_factory
from app.routers import auth, fortune, vision, social
from app.services.seed_service import seed_demo_users, seed_demo_social

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    # 启动时自动建表（简化部署；生产可用 Alembic）
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    logger.info("数据库表已就绪")

    # 播种演示账号（幂等）
    async with async_session_factory() as session:
        try:
            await seed_demo_users(session)
        except Exception as e:
            logger.warning("演示账号播种失败：%s", e)

    # 播种演示动态/评论/点赞（幂等）
    async with async_session_factory() as session:
        try:
            await seed_demo_social(session)
        except Exception as e:
            logger.warning("演示社交数据播种失败：%s", e)

    yield
    await engine.dispose()


app = FastAPI(title="节气民俗后端", version="1.0.0", lifespan=lifespan)

app.include_router(auth.router)
app.include_router(fortune.router)
app.include_router(vision.router)
app.include_router(social.router)


@app.get("/health")
async def health():
    return {"status": "ok"}


@app.get("/")
async def root():
    return {"app": "jieqi-backend", "docs": "/docs"}
