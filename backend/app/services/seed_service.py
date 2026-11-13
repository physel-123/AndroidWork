"""启动播种：创建内置演示账号 + 演示动态。"""
import logging
import time

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import hash_password
from app.models.entities import Comment, LikeRecord, Post, User

logger = logging.getLogger(__name__)

DEMO_PASSWORD = "123456"

# 演示账号定义（与前端 LoginScreen 展示的一致）
DEMO_ACCOUNTS = [
    {"username": "linxiaoman", "nickname": "林小满", "bio": "在杭州读大二，喜欢拍节气里的花", "avatar_color": 0},
    {"username": "ahe", "nickname": "阿禾", "bio": "农学专业，跟着节气种地", "avatar_color": 6},
    {"username": "qingtuan", "nickname": "青团君", "bio": "爱吃也会做，节气食俗收集控", "avatar_color": 5},
    {"username": "shiguang", "nickname": "拾光", "bio": "中学语文老师，做班会素材", "avatar_color": 15},
]


async def seed_demo_users(db: AsyncSession) -> None:
    """首次启动时播种演示账号（幂等：已存在则跳过）。"""
    for account in DEMO_ACCOUNTS:
        result = await db.execute(
            select(User).where(User.username == account["username"])
        )
        if result.scalar_one_or_none():
            continue
        db.add(
            User(
                username=account["username"],
                nickname=account["nickname"],
                password_hash=hash_password(DEMO_PASSWORD),
                bio=account["bio"],
                avatar_color=account["avatar_color"],
                is_demo=True,
            )
        )
    await db.commit()
    logger.info("演示账号已就绪（%d 个）", len(DEMO_ACCOUNTS))


async def seed_demo_social(db: AsyncSession) -> None:
    """播种演示动态/评论/点赞（幂等：已有动态则跳过）。"""
    existing = (await db.execute(select(Post).limit(1))).scalar_one_or_none()
    if existing is not None:
        return

    now_ms = int(time.time() * 1000)
    hour = 60 * 60 * 1000

    users = {}
    for account in DEMO_ACCOUNTS:
        r = await db.execute(select(User).where(User.username == account["username"]))
        u = r.scalar_one_or_none()
        if u:
            users[account["username"]] = u

    if len(users) < 4:
        return

    # 参数：作者 username、节气序号、节气名、心情、正文、点赞者 usernames、评论条数、距今小时
    seeds = [
        ("ahe", 15, "白露", "清爽",
         "白露第一天，去田里看了稻子，稻穗已经开始低头了。老话说「白露白迷迷，秋分稻秀齐」，真准。",
         ["linxiaoman", "qingtuan", "shiguang"], 4, 26),
        ("qingtuan", 15, "白露", "馋了",
         "按老规矩收了清露，又煮了一壶白露茶，配着龙眼吃。福建朋友说他们那儿白露必吃龙眼，试了一下确实甜。",
         ["linxiaoman", "ahe", "shiguang"], 4, 20),
        ("shiguang", 16, "秋分", "充实",
         "秋分要竖蛋，试了七次终于立住一个！还去采了秋菜煮秋汤，「秋汤灌脏，洗涤肝肠」，图个吉利。",
         ["linxiaoman", "ahe", "qingtuan", "shiguang"], 2, 8),
        ("linxiaoman", 16, "秋分", "惬意",
         "秋分昼夜平分，今天在西湖边一直走到天黑。桂花的味道一阵一阵的，节气真的是能闻出来的。",
         ["linxiaoman", "qingtuan"], 2, 5),
        ("ahe", 17, "寒露", "惦记",
         "寒露脚不露，今天开始给爸妈寄厚袜子了。顺便泡了菊花酒，等冬至的时候正好能喝。",
         ["shiguang"], 1, 2),
    ]

    posts: list[Post] = []
    for (author, term_index, term_name, mood, content, likers, comment_cnt, hours_ago) in seeds:
        u = users[author]
        post = Post(
            author=author,
            author_name=u.nickname or u.username,
            author_color=u.avatar_color,
            term_index=term_index,
            term_name=term_name,
            content=content,
            mood=mood,
            like_count=len(likers),
            comment_count=comment_cnt,
            created_at_ms=now_ms - hours_ago * hour,
        )
        db.add(post)
        posts.append(post)

    await db.flush()  # 拿到 post id

    # 点赞记录
    for i, (author, term_index, term_name, mood, content, likers, comment_cnt, hours_ago) in enumerate(seeds):
        post = posts[i]
        for liker in likers:
            db.add(LikeRecord(
                target_type="post",
                target_id=post.id,
                username=liker,
                created_at_ms=post.created_at_ms + 60_000,
            ))

    # 评论
    comment_seeds = [
        ("linxiaoman", "白露茶配龙眼，学到了，明天就试", 24),
        ("qingtuan", "我们这边白露是吃红薯，也很香", 22),
        ("ahe", "竖蛋成功也太厉害了，我一次都没立住", 7),
        ("shiguang", "秋汤是什么味道呀？想试试", 6),
    ]
    for post in posts[-2:]:  # 最新两条动态补评论
        for (author, content, hours_ago) in comment_seeds:
            u = users[author]
            db.add(Comment(
                target_type="post",
                target_id=post.id,
                author=author,
                author_name=u.nickname or u.username,
                author_color=u.avatar_color,
                content=content,
                like_count=(hash(content) % 7),
                created_at_ms=now_ms - hours_ago * hour,
            ))

    # 白露节气详情下放一条评论
    u = users["shiguang"]
    db.add(Comment(
        target_type="term",
        target_id=15,
        author="shiguang",
        author_name=u.nickname or u.username,
        author_color=u.avatar_color,
        content="讲白露一定让学生背「露从今夜白」，配上天气变化特别好懂。",
        like_count=5,
        created_at_ms=now_ms - 12 * hour,
    ))

    await db.commit()
    logger.info("演示动态/评论/点赞已播种")

