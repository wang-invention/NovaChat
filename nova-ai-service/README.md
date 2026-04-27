# Nova AI Service

基于 FastAPI 的 AI 对话服务，提供普通对话和流式对话接口。

## 项目结构

```
nova-ai-service/
├── app/
│   ├── api/
│   │   └── v1/
│   │       ├── health.py      # 健康检查接口
│   │       └── chat.py        # AI 对话接口
│   ├── core/
│   │   └── config.py          # 配置管理
│   ├── models/
│   │   └── schemas.py         # 数据模型
│   ├── services/
│   │   └── ai_service.py      # AI 服务层
│   └── utils/                 # 工具函数
├── docs/                      # 文档
├── main.py                    # 主入口
├── start.py                   # 启动脚本
├── requirements.txt           # 依赖
├── .env.example               # 环境变量示例
└── README.md                  # 项目说明
```

## 快速开始

### 1. 安装依赖

```bash
cd nova-ai-service

# 创建虚拟环境（推荐）
python -m venv venv

# 激活虚拟环境
# Windows:
venv\Scripts\activate
# macOS/Linux:
source venv/bin/activate

# 安装依赖
pip install -r requirements.txt
```

### 2. 配置环境变量

```bash
# 复制环境变量示例文件
cp .env.example .env

# 编辑 .env 文件，根据需要修改配置
```

### 3. 启动服务

```bash
# 方式1：使用启动脚本
python start.py

# 方式2：直接运行
python main.py

# 方式3：使用 uvicorn
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

服务启动后访问：
- API 文档：http://localhost:8000/docs
- 健康检查：http://localhost:8000/api/v1/health

## API 接口

### 健康检查

```bash
GET /api/v1/health
```

### AI 对话

```bash
POST /api/v1/chat/completions
Content-Type: application/json

{
  "messages": [
    {"role": "user", "content": "你好"}
  ],
  "model": "gpt-3.5-turbo",
  "temperature": 0.7,
  "max_tokens": 2048,
  "stream": false
}
```

### AI 流式对话（SSE）

```bash
POST /api/v1/chat/completions/stream
Content-Type: application/json

{
  "messages": [
    {"role": "user", "content": "你好"}
  ]
}
```

## 测试示例

### 普通对话

```bash
curl -X POST "http://localhost:8000/api/v1/chat/completions" \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [{"role": "user", "content": "你好"}]
  }'
```

### 流式对话

```bash
curl -X POST "http://localhost:8000/api/v1/chat/completions/stream" \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [{"role": "user", "content": "你好"}]
  }'
```

## 配置说明

| 环境变量 | 默认值 | 说明 |
|---------|-------|------|
| APP_NAME | NovaAI-Service | 应用名称 |
| APP_VERSION | 1.0.0 | 应用版本 |
| DEBUG | false | 调试模式 |
| HOST | 0.0.0.0 | 服务主机 |
| PORT | 8000 | 服务端口 |
| CORS_ORIGINS | ["*"] | 允许的跨域来源 |
| OPENAI_API_KEY | - | OpenAI API Key |
| OPENAI_MODEL | gpt-3.5-turbo | 默认模型 |

## 后续计划

- [ ] 接入真实 AI API（OpenAI、Claude 等）
- [ ] 添加请求限流
- [ ] 添加 API 认证
- [ ] 添加对话历史持久化
- [ ] 添加多模型支持
- [ ] 添加模型切换接口
