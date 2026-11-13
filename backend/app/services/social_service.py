"""社交服务：打卡广场动态、评论、点赞。

用户以 username 标识（后端账号主键）；时间戳统一毫秒，对齐前端。
"""
import time
from typing import Any

from sqlalchemy import delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.entities import Comment, FolkCustom, LikeRecord, Post, User


def _now_ms() -> int:
    return int(time.time() * 1000)


# ---------------------------------------------------------------------------
# 用户信息
# ---------------------------------------------------------------------------

async def _get_user(db: AsyncSession, username: str) -> User | None:
    r = await db.execute(select(User).where(User.username == username))
    return r.scalar_one_or_none()


def _user_brief(user: User) -> dict:
    return {
        "username": user.username,
        "nickname": user.nickname or user.username,
        "avatarColor": user.avatar_color,
    }


# ---------------------------------------------------------------------------
# 动态（post）
# ---------------------------------------------------------------------------

def _post_payload(post: Post, liked_by_me: bool, is_mine: bool, me: str | None) -> dict:
    return {
        "id": post.id,
        "author": post.author,
        "authorName": post.author_name,
        "authorColor": post.author_color,
        "termIndex": post.term_index,
        "termName": post.term_name,
        "content": post.content,
        "imageAsset": post.image_asset,
        "mood": post.mood,
        "likeCount": post.like_count,
        "commentCount": post.comment_count,
        "createdAt": post.created_at_ms,
        "likedByMe": liked_by_me,
        "isMine": is_mine,
    }


async def list_posts(db: AsyncSession, me: str | None) -> list[dict]:
    r = await db.execute(select(Post).order_by(Post.created_at_ms.desc(), Post.id.desc()))
    posts = r.scalars().all()

    liked_ids: set[int] = set()
    if me:
        lr = await db.execute(
            select(LikeRecord.target_id).where(
                LikeRecord.target_type == "post",
                LikeRecord.username == me,
            )
        )
        liked_ids = {row[0] for row in lr.all()}

    return [
        _post_payload(p, p.id in liked_ids, p.author == me, me)
        for p in posts
    ]


async def create_post(
    db: AsyncSession,
    username: str,
    content: str,
    term_index: int,
    term_name: str,
    mood: str | None,
    image_asset: str | None,
) -> dict:
    user = await _get_user(db, username)
    if user is None:
        raise ValueError("用户不存在，请先登录")
    content = content.strip()
    if not content:
        raise ValueError("内容不能为空")

    post = Post(
        author=username,
        author_name=user.nickname or user.username,
        author_color=user.avatar_color,
        term_index=term_index,
        term_name=term_name,
        content=content,
        image_asset=image_asset,
        mood=mood,
        like_count=0,
        comment_count=0,
        created_at_ms=_now_ms(),
    )
    db.add(post)
    await db.commit()
    return _post_payload(post, False, True, username)


async def delete_post(db: AsyncSession, post_id: int, username: str) -> bool:
    r = await db.execute(select(Post).where(Post.id == post_id))
    post = r.scalar_one_or_none()
    if post is None:
        return False
    if post.author != username:
        raise PermissionError("只能删除自己的动态")
    # 连带清理评论与点赞
    await db.execute(delete(Comment).where(Comment.target_type == "post", Comment.target_id == post_id))
    await db.execute(delete(LikeRecord).where(LikeRecord.target_type == "post", LikeRecord.target_id == post_id))
    await db.execute(delete(Post).where(Post.id == post_id))
    await db.commit()
    return True


# ---------------------------------------------------------------------------
# 评论
# ---------------------------------------------------------------------------

def _comment_payload(c: Comment, is_mine: bool) -> dict:
    return {
        "id": c.id,
        "targetType": c.target_type,
        "targetId": c.target_id,
        "parentId": c.parent_id,
        "author": c.author,
        "authorName": c.author_name,
        "authorColor": c.author_color,
        "content": c.content,
        "likeCount": c.like_count,
        "createdAt": c.created_at_ms,
        "isMine": is_mine,
    }


async def list_comments(db: AsyncSession, target_type: str, target_id: int, me: str | None) -> list[dict]:
    r = await db.execute(
        select(Comment)
        .where(Comment.target_type == target_type, Comment.target_id == target_id)
        .order_by(Comment.created_at_ms.asc(), Comment.id.asc())
    )
    comments = r.scalars().all()
    return [_comment_payload(c, c.author == me) for c in comments]


async def comment_count(db: AsyncSession, target_type: str, target_id: int) -> int:
    r = await db.execute(
        select(func.count()).select_from(Comment).where(
            Comment.target_type == target_type, Comment.target_id == target_id
        )
    )
    return r.scalar_one()


async def create_comment(
    db: AsyncSession,
    username: str,
    target_type: str,
    target_id: int,
    content: str,
    parent_id: int,
) -> dict:
    user = await _get_user(db, username)
    if user is None:
        raise ValueError("用户不存在，请先登录")
    content = content.strip()
    if not content:
        raise ValueError("评论内容不能为空")

    c = Comment(
        target_type=target_type,
        target_id=target_id,
        parent_id=parent_id,
        author=username,
        author_name=user.nickname or user.username,
        author_color=user.avatar_color,
        content=content,
        like_count=0,
        created_at_ms=_now_ms(),
    )
    db.add(c)
    # 若评论对象是动态，同步评论计数
    if target_type == "post":
        post = (await db.execute(select(Post).where(Post.id == target_id))).scalar_one_or_none()
        if post:
            post.comment_count += 1
    await db.commit()
    return _comment_payload(c, True)


async def delete_comment(db: AsyncSession, comment_id: int, username: str) -> bool:
    r = await db.execute(select(Comment).where(Comment.id == comment_id))
    c = r.scalar_one_or_none()
    if c is None:
        return False
    if c.author != username:
        raise PermissionError("只能删除自己的评论")
    await db.execute(delete(Comment).where(Comment.id == comment_id))
    if c.target_type == "post":
        post = (await db.execute(select(Post).where(Post.id == c.target_id))).scalar_one_or_none()
        if post and post.comment_count > 0:
            post.comment_count -= 1
    await db.commit()
    return True


# ---------------------------------------------------------------------------
# 点赞
# ---------------------------------------------------------------------------

async def is_liked(db: AsyncSession, target_type: str, target_id: int, username: str) -> bool:
    r = await db.execute(
        select(LikeRecord).where(
            LikeRecord.target_type == target_type,
            LikeRecord.target_id == target_id,
            LikeRecord.username == username,
        )
    )
    return r.scalar_one_or_none() is not None


async def like_count(db: AsyncSession, target_type: str, target_id: int) -> int:
    r = await db.execute(
        select(func.count()).select_from(LikeRecord).where(
            LikeRecord.target_type == target_type, LikeRecord.target_id == target_id
        )
    )
    return r.scalar_one()


async def toggle_like(db: AsyncSession, target_type: str, target_id: int, username: str) -> dict:
    """点赞/取消点赞，返回 {liked, likeCount}。"""
    existing = (
        await db.execute(
            select(LikeRecord).where(
                LikeRecord.target_type == target_type,
                LikeRecord.target_id == target_id,
                LikeRecord.username == username,
            )
        )
    ).scalar_one_or_none()

    if existing:
        await db.execute(delete(LikeRecord).where(LikeRecord.id == existing.id))
        delta = -1
        liked = False
    else:
        db.add(LikeRecord(target_type=target_type, target_id=target_id, username=username, created_at_ms=_now_ms()))
        delta = 1
        liked = True

    # 同步业务表冗余计数
    if target_type == "post":
        post = (await db.execute(select(Post).where(Post.id == target_id))).scalar_one_or_none()
        if post:
            post.like_count = max(0, post.like_count + delta)

    await db.commit()
    count = await like_count(db, target_type, target_id)
    return {"liked": liked, "likeCount": count}


async def likes_given(db: AsyncSession, username: str) -> int:
    r = await db.execute(
        select(func.count()).select_from(LikeRecord).where(LikeRecord.username == username)
    )
    return r.scalar_one()


async def member_count(db: AsyncSession) -> int:
    r = await db.execute(select(func.count()).select_from(User))
    return r.scalar_one()


# ---------------------------------------------------------------------------
# 民俗投稿
# ---------------------------------------------------------------------------

def _folk_payload(f: FolkCustom) -> dict:
    return {
        "id": f.id,
        "title": f.title,
        "termName": f.term_name,
        "province": f.province,
        "city": f.city,
        "latitude": f.latitude,
        "longitude": f.longitude,
        "description": f.description,
        "contributor": f.contributor,
        "contributorName": f.contributor_name,
        "createdAt": f.created_at_ms,
    }


async def list_folk_customs(db: AsyncSession) -> list[dict]:
    r = await db.execute(select(FolkCustom).order_by(FolkCustom.created_at_ms.desc(), FolkCustom.id.desc()))
    return [_folk_payload(f) for f in r.scalars().all()]


async def create_folk_custom(
    db: AsyncSession,
    username: str,
    title: str,
    term_name: str | None,
    province: str,
    city: str,
    latitude: float,
    longitude: float,
    description: str,
) -> dict:
    user = await _get_user(db, username)
    if user is None:
        raise ValueError("用户不存在，请先登录")
    title = title.strip()
    if not title:
        raise ValueError("民俗名称不能为空")

    f = FolkCustom(
        title=title,
        term_name=(term_name or "").strip() or None,
        province=province.strip(),
        city=city.strip(),
        latitude=latitude,
        longitude=longitude,
        description=description.strip(),
        contributor=username,
        contributor_name=user.nickname or user.username,
        created_at_ms=_now_ms(),
    )
    db.add(f)
    await db.commit()
    return _folk_payload(f)


async def delete_folk_custom(db: AsyncSession, custom_id: int, username: str) -> bool:
    r = await db.execute(select(FolkCustom).where(FolkCustom.id == custom_id))
    f = r.scalar_one_or_none()
    if f is None:
        return False
    if f.contributor != username:
        raise PermissionError("只能删除自己的投稿")
    await db.execute(delete(FolkCustom).where(FolkCustom.id == custom_id))
    await db.commit()
    return True
