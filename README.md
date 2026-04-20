## ChatNova

## 项目简介

ChatNova 是一个基于微服务架构的 AI 对话系统，集成大语言模型能力，支持用户管理、订单支付与 AI 智能聊天功能，打造可扩展的 AI
SaaS 平台。

系统采用前后端分离 + 微服务架构设计，具备高可扩展性与工程化实践能力。

## 核心功能模块

### 用户模块

- 用户注册 / 登录
- JWT / Token 鉴权
- 用户信息管理
- 积分体系（可扩展）

### AI 聊天模块

- 基于大模型的智能对话
- LangChain 构建 AI 调用链路
- 支持多轮对话
- 聊天记录持久化
- 可扩展多模型接入（OpenAI / DeepSeek / 本地模型）

### 订单模块

- 积分充值订单
- 支付状态管理
- 订单记录查询
- 异步通知机制（MQ）

## 技术栈

### 后端核心

- Spring Boot 3
- Spring Cloud Alibaba
- Nacos（服务注册与配置中心）
- Gateway（网关）
- OpenFeign（服务调用）
- Sentinel（限流熔断）

### 数据与中间件

- MySQL 8（持久化存储）
- Redis（缓存 / 会话 / Token）
- RabbitMQ（异步消息队列）

### AI能力

- Python
- LangChain
- 大语言模型（DeepSeek）

### 工程化

- Docker（容器化部署）
- Maven（依赖管理）
- Vue3（前端）