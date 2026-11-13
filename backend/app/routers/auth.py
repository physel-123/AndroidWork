"""账号认证路由（JWT）。"""
from fastapi import APIRouter, Depends, HTTPException
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from pydantic import BaseModel
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.core.security import hash_password, verify_password, create_access_token, decode_token
from app.models.entities import User

router = APIRouter(prefix="/api/auth", tags=["auth"])

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/auth/login")


class RegisterRequest(BaseModel):
    username: str
    password: str
    nickname: str = ""


def _user_payload(user: User, token: str) -> dict:
    return {
        "token": token,
        "id": user.id,
        "username": user.username,
        "nickname": user.nickname,
        "bio": user.bio,
        "avatarColor": user.avatar_color,
        "isDemo": user.is_demo,
    }


async def get_current_user(
    token: str = Depends(oauth2_scheme),
    db: AsyncSession = Depends(get_db),
) -> User:
    username = decode_token(token)
    if not username:
        raise HTTPException(status_code=401, detail="未登录或令牌失效")
    result = await db.execute(select(User).where(User.username == username))
    user = result.scalar_one_or_none()
    if not user:
        raise HTTPException(status_code=401, detail="用户不存在")
    return user


@router.post("/register")
async def register(req: RegisterRequest, db: AsyncSession = Depends(get_db)):
    if len(req.username) < 2 or len(req.password) < 6:
        raise HTTPException(status_code=400, detail="用户名至少 2 位，密码至少 6 位")
    result = await db.execute(select(User).where(User.username == req.username))
    if result.scalar_one_or_none():
        raise HTTPException(status_code=400, detail="用户名已存在")
    user = User(
        username=req.username,
        password_hash=hash_password(req.password),
        nickname=req.nickname or req.username,
        bio="刚刚加入节气民俗",
        avatar_color=0,
        is_demo=False,
    )
    db.add(user)
    await db.commit()
    token = create_access_token(req.username)
    return _user_payload(user, token)


@router.post("/login")
async def login(form: OAuth2PasswordRequestForm = Depends(), db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(User).where(User.username == form.username))
    user = result.scalar_one_or_none()
    if not user or not verify_password(form.password, user.password_hash):
        raise HTTPException(status_code=401, detail="用户名或密码错误")
    token = create_access_token(user.username)
    return _user_payload(user, token)


@router.get("/me")
async def me(user: User = Depends(get_current_user)):
    return {
        "id": user.id,
        "username": user.username,
        "nickname": user.nickname,
        "bio": user.bio,
        "avatarColor": user.avatar_color,
        "isDemo": user.is_demo,
    }
