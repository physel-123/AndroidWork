# 节气民俗后端部署说明

## 服务器信息

| 项目 | 值 |
|---|---|
| IP | 49.232.151.242 |
| 系统 | Ubuntu 24.04.4 LTS |
| 用户 | ubuntu（已配置 SSH 免密） |
| 项目目录 | `/jieqi` |
| 对外端口 | 8000（HTTP） |

## 目录结构（服务器 /jieqi）

```
/jieqi
├── app/                  # FastAPI 应用代码
│   ├── main.py           # 入口
│   ├── core/             # 配置、数据库、安全
│   ├── routers/          # auth / fortune / vision 路由
│   ├── services/         # 运势 / 占卜 / 多模态识别服务
│   ├── models/           # ORM 模型
│   └── data/             # 语料库
├── Dockerfile
├── docker-compose.yml
├── .env                  # 敏感配置（密码/密钥/API Key）
└── requirements.txt
```

## 常用命令（在服务器 /jieqi 目录执行）

```bash
# 构建镜像
docker compose build

# 启动（后台）
docker compose up -d

# 查看日志
docker compose logs -f backend

# 查看状态
docker compose ps

# 停止
docker compose down

# 停止并删除数据卷（慎用！会清空数据库）
docker compose down -v
```

## API 接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/health` | 健康检查 |
| GET | `/docs` | Swagger 文档 |
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录（返回 JWT） |
| GET | `/api/auth/me` | 当前用户 |
| GET | `/api/fortune?zodiac=龙` | 今日运势 |
| GET | `/api/divination/sign` | 抽灵签 |
| GET | `/api/divination/tarot` | 抽塔罗 |
| POST | `/api/palm/analyze` | 手相分析（上传图片） |
| POST | `/api/face/analyze` | 面相分析（上传图片） |

## 启用真实多模态识别

1. 到阿里云百炼平台（DashScope）申请 API Key
2. 编辑 `/jieqi/.env`，填入 `DASHSCOPE_API_KEY=你的key`
3. 重启后端：`docker compose up -d --force-recreate backend`

> 未填 key 时，手相/面相接口返回 mock 特征，链路仍可跑通。

## Android 端接入注意

- 后端地址：`http://49.232.151.242:8000`
- Android 9+ 默认禁止明文 HTTP，需在 App 添加 `network_security_config.xml` 放行该 IP
