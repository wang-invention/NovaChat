# Day 16 · Python AI 服务搭建（FastAPI）

> 目标：搭建基于 FastAPI 的 AI 服务，提供基础对话接口。

---

## 一、项目结构

```
nova-ai-service/
├── app/
│   ├── api/v1/
│   │   ├── health.py          # 健康检查接口
│   │   └── chat.py            # AI 对话接口
│   ├── core/
│   │   └── config.py          # 配置管理
│   ├── models/
│   │   └── schemas.py         # Pydantic 数据模型
│   ├── services/
│   │   └── ai_service.py      # AI 服务层（Mock 实现）
│   └── utils/                 # 工具函数
├── main.py                    # FastAPI 主入口
├── start.py                   # 启动脚本
├── requirements.txt           # Python 依赖
├── .env.example               # 环境变量示例
└── README.md                  # 项目说明
```

---

## 二、核心依赖

```txt
fastapi==0.109.0              # Web 框架
uvicorn[standard]==0.27.0     # ASGI 服务器
pydantic==2.5.3               # 数据验证
pydantic-settings==2.1.0      # 配置管理
httpx==0.26.0                 # HTTP 客户端
python-dotenv==1.0.0          # 环境变量
```

---

## 三、关键代码

### 3.1 主入口（main.py）

```python
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(
    title="NovaAI-Service",
    version="1.0.0",
    docs_url="/docs",
)

# CORS 配置
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
app.include_router(health.router, prefix="/api/v1")
app.include_router(chat.router, prefix="/api/v1")
```

### 3.2 数据模型（schemas.py）

```python
class Message(BaseModel):
    role: Literal["system", "user", "assistant"]
    content: str

class ChatRequest(BaseModel):
    messages: List[Message]
    model: Optional[str] = "gpt-3.5-turbo"
    temperature: Optional[float] = 0.7
    max_tokens: Optional[int] = 2048
    stream: Optional[bool] = False

class ChatResponse(BaseModel):
    id: str
    model: str
    choices: List[ChatChoice]
```

### 3.3 AI 对话接口（chat.py）

```python
@router.post("/completions", response_model=ChatResponse)
async def chat_completions(request: ChatRequest):
    if request.stream:
        return StreamingResponse(
            ai_service.chat_stream(request),
            media_type="text/event-stream"
        )
    return await ai_service.chat(request)
```

### 3.4 流式响应（SSE）

```python
async def chat_stream(request: ChatRequest) -> AsyncGenerator[str, None]:
    # 模拟流式输出
    for chunk in chunks:
        response = ChatStreamResponse(...)
        yield f"data: {response.model_dump_json()}\n\n"
        await asyncio.sleep(0.1)
    yield "data: [DONE]\n\n"
```

---

## 四、接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / | 服务信息 |
| GET | /docs | Swagger UI 文档 |
| GET | /api/v1/health | 健康检查 |
| GET | /api/v1/health/ready | 就绪检查 |
| GET | /api/v1/health/live | 存活检查 |
| POST | /api/v1/chat/completions | AI 对话 |
| POST | /api/v1/chat/completions/stream | 流式对话（SSE） |

---

## 五、启动服务

```bash
cd nova-ai-service

# 安装依赖
pip install -r requirements.txt

# 启动服务
python start.py

# 或使用 uvicorn
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

---

## 六、测试接口

### 6.1 健康检查

```bash
curl http://localhost:8000/api/v1/health
```

### 6.2 普通对话

```bash
curl -X POST "http://localhost:8000/api/v1/chat/completions" \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [{"role": "user", "content": "你好"}]
  }'
```

### 6.3 流式对话

```bash
curl -X POST "http://localhost:8000/api/v1/chat/completions/stream" \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [{"role": "user", "content": "你好"}]
  }'
```

---

## 七、Mock 回复逻辑

当前使用简单的关键词匹配生成 Mock 回复：

| 关键词 | 回复 |
|--------|------|
| 你好/hello | 问候语 |
| 天气 | 无法获取天气 |
| 时间/日期 | 当前时间 |
| 帮助/help | 功能列表 |
| 谢谢 | 不客气 |
| 其他 | 默认回复 |

---

## 八、下一步（Day 17 预告）

- 接入真实 AI API（OpenAI/Claude）
- 添加 API 认证（JWT）
- 添加请求限流
- 添加对话历史持久化
