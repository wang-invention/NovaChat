"""
AI 服务层
封装 AI 调用逻辑，目前使用 Mock 实现
后续可替换为真实的 AI API 调用
"""
import time
import uuid
import os
from datetime import datetime
from typing import AsyncGenerator, List
from app.models.schemas import (
    ChatRequest, ChatResponse, ChatChoice, Message,
    ChatStreamResponse, StreamChoice, PolishResponse
)
from app.core.config import settings


class AIService:
    """AI 服务类"""

    @staticmethod
    async def chat(request: ChatRequest) -> ChatResponse:
        """
        普通对话接口

        Args:
            request: 对话请求

        Returns:
            对话响应
        """
        user_message = request.messages[-1].content if request.messages else ""

        try:
            from langchain_openai import ChatOpenAI
            from langchain_core.messages import HumanMessage

            api_key = settings.DEEPSEEK_API_KEY
            if not api_key or api_key == "your-deepseek-api-key":
                response_content = AIService._generate_mock_response(user_message)
            else:
                llm = ChatOpenAI(
                    model=settings.DEEPSEEK_MODEL,
                    api_key=api_key,
                    base_url=settings.DEEPSEEK_BASE_URL,
                    temperature=request.temperature or 0.7,
                    max_tokens=request.max_tokens or 2048,
                )
                messages = [HumanMessage(content=m.content) for m in request.messages]
                response = await llm.ainvoke(messages)
                response_content = response.content

        except Exception as e:
            print(f"[AI Service] LangChain 调用失败: {e}")
            response_content = AIService._generate_mock_response(user_message)

        return ChatResponse(
            id=f"chat-{uuid.uuid4().hex[:8]}",
            created=int(time.time()),
            model=request.model or settings.DEEPSEEK_MODEL,
            choices=[
                ChatChoice(
                    index=0,
                    message=Message(
                        role="assistant",
                        content=response_content
                    ),
                    finish_reason="stop"
                )
            ]
        )

    @staticmethod
    async def chat_stream(request: ChatRequest) -> AsyncGenerator[str, None]:
        """
        流式对话接口（SSE）

        Args:
            request: 对话请求

        Yields:
            SSE 格式的数据流
        """
        user_message = request.messages[-1].content if request.messages else ""

        try:
            from langchain_openai import ChatOpenAI
            from langchain_core.messages import HumanMessage

            api_key = settings.DEEPSEEK_API_KEY
            if not api_key or api_key == "your-deepseek-api-key":
                response_content = AIService._generate_mock_response(user_message)
                chunks = [response_content[i:i+5] for i in range(0, len(response_content), 5)]
            else:
                llm = ChatOpenAI(
                    model=settings.DEEPSEEK_MODEL,
                    api_key=api_key,
                    base_url=settings.DEEPSEEK_BASE_URL,
                    temperature=request.temperature or 0.7,
                    max_tokens=request.max_tokens or 2048,
                    streaming=True,
                )
                messages = [HumanMessage(content=m.content) for m in request.messages]
                response = await llm.ainvoke(messages)
                response_content = response.content
                chunks = [response_content[i:i+5] for i in range(0, len(response_content), 5)]

        except Exception as e:
            print(f"[AI Service] LangChain 流式调用失败: {e}")
            response_content = AIService._generate_mock_response(user_message)
            chunks = [response_content[i:i+5] for i in range(0, len(response_content), 5)]

        response_id = f"chat-{uuid.uuid4().hex[:8]}"
        created = int(time.time())
        model = request.model or settings.DEEPSEEK_MODEL

        for i, chunk in enumerate(chunks):
            stream_response = ChatStreamResponse(
                id=response_id,
                created=created,
                model=model,
                choices=[
                    StreamChoice(
                        index=0,
                        delta={"content": chunk},
                        finish_reason=None if i < len(chunks) - 1 else "stop"
                    )
                ]
            )
            yield f"data: {stream_response.model_dump_json()}\n\n"
            await __import__('asyncio').sleep(0.05)

        yield "data: [DONE]\n\n"

    @staticmethod
    async def polish_message(text: str) -> PolishResponse:
        """
        消息润色功能
        使用 LangChain + DeepSeek API 对消息进行润色

        Args:
            text: 需要润色的原文

        Returns:
            4种风格的润色结果
        """
        try:
            from langchain_openai import ChatOpenAI
            from langchain_core.messages import HumanMessage
            from langchain_core.prompts import ChatPromptTemplate
            from langchain_core.output_parsers import StrOutputParser
        except ImportError:
            return PolishResponse(results=[
                f"[Mock] 温柔亲切版：{text}",
                f"[Mock] 幽默轻松版：{text}",
                f"[Mock] 高情商版：{text}",
                f"[Mock] 简洁正式版：{text}",
            ])

        api_key = settings.DEEPSEEK_API_KEY
        if not api_key or api_key == "your-deepseek-api-key":
            return PolishResponse(results=[
                f"[请配置 DeepSeek API Key] 温柔亲切版：{text}",
                f"[请配置 DeepSeek API Key] 幽默轻松版：{text}",
                f"[请配置 DeepSeek API Key] 高情商版：{text}",
                f"[请配置 DeepSeek API Key] 简洁正式版：{text}",
            ])

        llm = ChatOpenAI(
            model=settings.DEEPSEEK_MODEL,
            api_key=api_key,
            base_url=settings.DEEPSEEK_BASE_URL,
            temperature=0.8,
        )

        prompt = ChatPromptTemplate.from_messages([
            ("system", """你是一个聊天话术助手，负责帮用户润色聊天消息，要求：
1. 保持原意不变，不改变用户想表达的核心意思
2. 只优化语气、表达方式，让句子更自然、更好听、更得体
3. 分4个风格返回，每个风格只给一句话，不要多余解释：
   - 温柔亲切版
   - 幽默轻松版
   - 高情商版
   - 简洁正式版
4. 不要加表情、不要加序号，只返回纯文字句子"""),
            ("human", "{text}")
        ])

        chain = prompt | llm | StrOutputParser()
        result = chain.invoke({"text": text})

        lines = [line.strip() for line in result.split("\n") if line.strip()]
        return PolishResponse(results=lines[:4] if len(lines) >= 4 else lines)

    @staticmethod
    def _generate_mock_response(user_message: str) -> str:
        """
        生成 Mock 响应

        Args:
            user_message: 用户消息

        Returns:
            Mock 回复内容
        """
        # 简单的关键词匹配
        user_message_lower = user_message.lower()

        if any(kw in user_message_lower for kw in ["你好", "hello", "hi"]):
            return "你好！我是 Nova AI 助手，很高兴为你服务。有什么我可以帮助你的吗？"

        elif any(kw in user_message_lower for kw in ["天气", "weather"]):
            return "我暂时无法获取实时天气信息。建议你查看天气预报应用或网站获取准确的天气数据。"

        elif any(kw in user_message_lower for kw in ["时间", "time", "日期", "date"]):
            now = datetime.now().strftime("%Y年%m月%d日 %H:%M:%S")
            return f"当前时间是：{now}"

        elif any(kw in user_message_lower for kw in ["帮助", "help", "能做什么"]):
            return """我可以帮你：
1. 回答问题
2. 提供建议
3. 辅助写作
4. 解释概念
5. 进行简单计算

请告诉我你需要什么帮助！"""

        elif any(kw in user_message_lower for kw in ["谢谢", "thanks", "thank you"]):
            return "不客气！如果还有其他问题，随时告诉我。"

        else:
            return f"我收到了你的消息：\u201c{user_message}\u201d。\n\n这是一个 Mock 回复。在实际部署时，我会调用真实的 AI 模型来生成更有意义的回答。"


# 全局 AI 服务实例
ai_service = AIService()
