"""ORM 模型定义。"""
from datetime import datetime

from sqlalchemy import String, Text, Integer, BigInteger, Boolean, DateTime, func
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class User(Base):
    __tablename__ = "users"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    username: Mapped[str] = mapped_column(String(64), unique=True, index=True, nullable=False)
    nickname: Mapped[str] = mapped_column(String(64), default="")
    password_hash: Mapped[str] = mapped_column(String(128), nullable=False)
    bio: Mapped[str] = mapped_column(String(255), default="")
    avatar_color: Mapped[int] = mapped_column(Integer, default=0)
    is_demo: Mapped[bool] = mapped_column(default=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )


class FortuneRecord(Base):
    """运势/占卜记录：按「用户 + 类型 + 日期」记录一次结果，便于回看与幂等。"""

    __tablename__ = "fortune_records"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    username: Mapped[str] = mapped_column(String(64), index=True, nullable=False)
    kind: Mapped[str] = mapped_column(String(32), nullable=False)  # fortune / divination / palm / face
    seed_key: Mapped[str] = mapped_column(String(128), index=True, nullable=False)
    result: Mapped[str] = mapped_column(Text, nullable=False)  # JSON 字符串
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )


class Post(Base):
    """打卡广场动态。"""

    __tablename__ = "posts"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    author: Mapped[str] = mapped_column(String(64), index=True, nullable=False)  # 作者 username
    author_name: Mapped[str] = mapped_column(String(64), default="")
    author_color: Mapped[int] = mapped_column(Integer, default=0)
    term_index: Mapped[int] = mapped_column(Integer, default=0)  # 1..24
    term_name: Mapped[str] = mapped_column(String(32), default="")
    content: Mapped[str] = mapped_column(Text, nullable=False)
    image_asset: Mapped[str | None] = mapped_column(String(255), nullable=True)
    mood: Mapped[str | None] = mapped_column(String(32), nullable=True)
    like_count: Mapped[int] = mapped_column(Integer, default=0)
    comment_count: Mapped[int] = mapped_column(Integer, default=0)
    created_at_ms: Mapped[int] = mapped_column(BigInteger, default=0)  # 毫秒时间戳


class Comment(Base):
    """评论/回复：同时服务节气详情、民俗、打卡动态三场景。"""

    __tablename__ = "comments"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    target_type: Mapped[str] = mapped_column(String(16), nullable=False)  # term / folk / post
    target_id: Mapped[int] = mapped_column(Integer, index=True, nullable=False)
    parent_id: Mapped[int] = mapped_column(Integer, default=0)  # 0 表示顶级评论
    author: Mapped[str] = mapped_column(String(64), nullable=False)
    author_name: Mapped[str] = mapped_column(String(64), default="")
    author_color: Mapped[int] = mapped_column(Integer, default=0)
    content: Mapped[str] = mapped_column(Text, nullable=False)
    like_count: Mapped[int] = mapped_column(Integer, default=0)
    created_at_ms: Mapped[int] = mapped_column(BigInteger, default=0)


class LikeRecord(Base):
    """点赞记录：target_type + target_id + username 唯一，防重复点赞。"""

    __tablename__ = "like_records"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    target_type: Mapped[str] = mapped_column(String(16), nullable=False)
    target_id: Mapped[int] = mapped_column(Integer, index=True, nullable=False)
    username: Mapped[str] = mapped_column(String(64), nullable=False)
    created_at_ms: Mapped[int] = mapped_column(BigInteger, default=0)


class FolkCustom(Base):
    """民俗投稿（用户补充的「我家的民俗」）。"""

    __tablename__ = "folk_customs"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    title: Mapped[str] = mapped_column(String(128), nullable=False)
    term_name: Mapped[str | None] = mapped_column(String(32), nullable=True)
    province: Mapped[str] = mapped_column(String(32), default="")
    city: Mapped[str] = mapped_column(String(64), default="")
    latitude: Mapped[float] = mapped_column(default=0.0)
    longitude: Mapped[float] = mapped_column(default=0.0)
    description: Mapped[str] = mapped_column(Text, default="")
    contributor: Mapped[str] = mapped_column(String(64), default="")  # username
    contributor_name: Mapped[str] = mapped_column(String(64), default="")
    created_at_ms: Mapped[int] = mapped_column(BigInteger, default=0)
