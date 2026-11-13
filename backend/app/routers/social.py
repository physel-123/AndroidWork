"""社交路由：打卡广场动态、评论、点赞。"""
from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.services import social_service

router = APIRouter(prefix="/api/social", tags=["social"])


class PostCreate(BaseModel):
    username: str
    content: str
    termIndex: int = 0
    termName: str = ""
    mood: str | None = None
    imageAsset: str | None = None


class CommentCreate(BaseModel):
    username: str
    targetType: str
    targetId: int
    content: str
    parentId: int = 0


class FolkCustomCreate(BaseModel):
    username: str
    title: str
    termName: str | None = None
    province: str = ""
    city: str = ""
    latitude: float = 0.0
    longitude: float = 0.0
    description: str = ""


# ---------------------------------------------------------------------------
# 动态
# ---------------------------------------------------------------------------

@router.get("/posts")
async def list_posts(username: str = "", db: AsyncSession = Depends(get_db)):
    """动态列表，username 为空表示未登录（likedByMe 恒 false）。"""
    me = username or None
    return await social_service.list_posts(db, me)


@router.post("/posts")
async def create_post(body: PostCreate, db: AsyncSession = Depends(get_db)):
    try:
        return await social_service.create_post(
            db, body.username, body.content, body.termIndex, body.termName, body.mood, body.imageAsset
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.delete("/posts/{post_id}")
async def delete_post(post_id: int, username: str, db: AsyncSession = Depends(get_db)):
    try:
        ok = await social_service.delete_post(db, post_id, username)
    except PermissionError as e:
        raise HTTPException(status_code=403, detail=str(e))
    if not ok:
        raise HTTPException(status_code=404, detail="动态不存在")
    return {"ok": True}


# ---------------------------------------------------------------------------
# 评论
# ---------------------------------------------------------------------------

@router.get("/comments")
async def list_comments(targetType: str, targetId: int, username: str = "", db: AsyncSession = Depends(get_db)):
    me = username or None
    return await social_service.list_comments(db, targetType, targetId, me)


@router.get("/comments/count")
async def comment_count(targetType: str, targetId: int, db: AsyncSession = Depends(get_db)):
    return await social_service.comment_count(db, targetType, targetId)


@router.post("/comments")
async def create_comment(body: CommentCreate, db: AsyncSession = Depends(get_db)):
    try:
        return await social_service.create_comment(
            db, body.username, body.targetType, body.targetId, body.content, body.parentId
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.delete("/comments/{comment_id}")
async def delete_comment(comment_id: int, username: str, db: AsyncSession = Depends(get_db)):
    try:
        ok = await social_service.delete_comment(db, comment_id, username)
    except PermissionError as e:
        raise HTTPException(status_code=403, detail=str(e))
    if not ok:
        raise HTTPException(status_code=404, detail="评论不存在")
    return {"ok": True}


# ---------------------------------------------------------------------------
# 点赞
# ---------------------------------------------------------------------------

@router.get("/like")
async def get_like(targetType: str, targetId: int, username: str, db: AsyncSession = Depends(get_db)):
    liked = await social_service.is_liked(db, targetType, targetId, username)
    count = await social_service.like_count(db, targetType, targetId)
    return {"liked": liked, "likeCount": count}


@router.post("/like")
async def toggle_like(targetType: str, targetId: int, username: str, db: AsyncSession = Depends(get_db)):
    return await social_service.toggle_like(db, targetType, targetId, username)


# ---------------------------------------------------------------------------
# 统计
# ---------------------------------------------------------------------------

@router.get("/member_count")
async def member_count(db: AsyncSession = Depends(get_db)):
    return await social_service.member_count(db)


# ---------------------------------------------------------------------------
# 民俗投稿
# ---------------------------------------------------------------------------

@router.get("/folk_customs")
async def list_folk_customs(db: AsyncSession = Depends(get_db)):
    return await social_service.list_folk_customs(db)


@router.post("/folk_customs")
async def create_folk_custom(body: FolkCustomCreate, db: AsyncSession = Depends(get_db)):
    try:
        return await social_service.create_folk_custom(
            db, body.username, body.title, body.termName, body.province,
            body.city, body.latitude, body.longitude, body.description
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.delete("/folk_customs/{custom_id}")
async def delete_folk_custom(custom_id: int, username: str, db: AsyncSession = Depends(get_db)):
    try:
        ok = await social_service.delete_folk_custom(db, custom_id, username)
    except PermissionError as e:
        raise HTTPException(status_code=403, detail=str(e))
    if not ok:
        raise HTTPException(status_code=404, detail="投稿不存在")
    return {"ok": True}
