"""
AI 服务层
封装 AI 调用逻辑，目前使用 Mock 实现
后续可替换为真实的 AI API 调用
"""
import time
import uuid
from datetime import datetime
from typing import AsyncGenerator
from app.models.schemas import (
    ChatRequest, ChatResponse, ChatChoice, Message,
    ChatStreamResponse, StreamChoice
)


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
        # 获取最后一条用户消息
        user_message = request.messages[-1].content if request.messages else ""

        # Mock 响应（后续替换为真实 AI 调用）
        response_content = AIService._generate_mock_response(user_message)

        return ChatResponse(
            id=f"chat-{uuid.uuid4().hex[:8]}",
            created=int(time.time()),
            model=request.model or "gpt-3.5-turbo",
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
        # 获取最后一条用户消息
        user_message = request.messages[-1].content if request.messages else ""

        # Mock 响应内容
        response_content = AIService._generate_mock_response(user_message)

        # 模拟流式输出，将内容分成多个 chunk
        chunk_size = 5  # 每个 chunk 的字符数
        chunks = [response_content[i:i+chunk_size]
                  for i in range(0, len(response_content), chunk_size)]

        response_id = f"chat-{uuid.uuid4().hex[:8]}"
        created = int(time.time())
        model = request.model or "gpt-3.5-turbo"

        for i, chunk in enumerate(chunks):
            # 构建流式响应
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

            # 返回 SSE 格式数据
            yield f"data: {stream_response.model_dump_json()}\n\n"

            # 模拟延迟
            await __import__('asyncio').sleep(0.1)

        # 发送结束标记
        yield "data: [DONE]\n\n"

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
            return f"我收到了你的消息："{user_message}"。\n\n这是一个 Mock 回复。在实际部署时，我会调用真实的 AI 模型来生成更有意义的回答。"


# 全局 AI 服务实例
ai_service = AIService()
