"""
AI 对话接口
提供普通对话和流式对话功能
"""
from fastapi import APIRouter, HTTPException
from fastapi.responses import StreamingResponse
from app.models.schemas import ChatRequest, ChatResponse, ErrorResponse
from app.services.ai_service import ai_service

router = APIRouter(prefix="/chat", tags=["AI 对话"])


@router.post(
    "/completions",
    response_model=ChatResponse,
    summary="AI 对话",
    responses={
        200: {"description": "对话成功", "model": ChatResponse},
        400: {"description": "请求参数错误", "model": ErrorResponse},
        500: {"description": "服务器内部错误", "model": ErrorResponse},
    }
)
async def chat_completions(request: ChatRequest):
    """
    AI 对话接口

    发送消息列表给 AI，获取回复

    - **messages**: 消息列表，包含角色和内容
    - **model**: 使用的模型名称（可选，默认 gpt-3.5-turbo）
    - **temperature**: 温度参数（可选，默认 0.7）
    - **max_tokens**: 最大生成 token 数（可选，默认 2048）
    - **stream**: 是否使用流式响应（可选，默认 false）
    """
    try:
        if request.stream:
            # 流式响应
            return StreamingResponse(
                ai_service.chat_stream(request),
                media_type="text/event-stream"
            )
        else:
            # 普通响应
            return await ai_service.chat(request)
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"对话处理失败: {str(e)}"
        )


@router.post(
    "/completions/stream",
    summary="AI 流式对话（SSE）",
    response_class=StreamingResponse
)
async def chat_completions_stream(request: ChatRequest):
    """
    AI 流式对话接口（Server-Sent Events）

    以流式方式返回 AI 回复，适合长文本生成场景

    返回格式为 SSE（Server-Sent Events），每个数据包格式为：
    ```
    data: {"id": "...", "choices": [{"delta": {"content": "..."}}]}\n\n
    ```

    结束标记：
    ```
    data: [DONE]\n\n
    ```
    """
    try:
        # 强制使用流式模式
        request.stream = True

        return StreamingResponse(
            ai_service.chat_stream(request),
            media_type="text/event-stream",
            headers={
                "Cache-Control": "no-cache",
                "Connection": "keep-alive",
                "X-Accel-Buffering": "no",  # 禁用 Nginx 缓冲
            }
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"流式对话处理失败: {str(e)}"
        )
