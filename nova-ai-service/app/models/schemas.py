"""
数据模型定义
使用 Pydantic 进行数据验证
"""
from typing import List, Optional, Literal
from pydantic import BaseModel, Field
from datetime import datetime


class Message(BaseModel):
    """对话消息模型"""
    role: Literal["system", "user", "assistant"] = Field(
        ...,
        description="消息角色: system-系统, user-用户, assistant-助手"
    )
    content: str = Field(default="", description="消息内容")


class ChatRequest(BaseModel):
    """对话请求模型"""
    messages: List[Message] = Field(
        ...,
        description="消息列表",
        min_length=1
    )
    model: Optional[str] = Field(
        default="gpt-3.5-turbo",
        description="使用的模型名称"
    )
    temperature: Optional[float] = Field(
        default=0.7,
        ge=0,
        le=2,
        description="温度参数，控制随机性"
    )
    max_tokens: Optional[int] = Field(
        default=2048,
        ge=1,
        le=4096,
        description="最大生成token数"
    )
    stream: Optional[bool] = Field(
        default=False,
        description="是否使用流式响应"
    )


class ChatChoice(BaseModel):
    """对话选择结果"""
    index: int = Field(default=0, description="选择索引")
    message: Message = Field(..., description="消息内容")
    finish_reason: Optional[str] = Field(
        default=None,
        description="结束原因"
    )


class ChatResponse(BaseModel):
    """对话响应模型"""
    id: str = Field(..., description="响应ID")
    object: str = Field(default="chat.completion", description="对象类型")
    created: int = Field(..., description="创建时间戳")
    model: str = Field(..., description="使用的模型")
    choices: List[ChatChoice] = Field(..., description="选择结果列表")


class StreamChoice(BaseModel):
    """流式响应选择"""
    index: int = Field(default=0, description="选择索引")
    delta: dict = Field(..., description="增量内容")
    finish_reason: Optional[str] = Field(
        default=None,
        description="结束原因"
    )


class ChatStreamResponse(BaseModel):
    """流式对话响应模型"""
    id: str = Field(..., description="响应ID")
    object: str = Field(default="chat.completion.chunk", description="对象类型")
    created: int = Field(..., description="创建时间戳")
    model: str = Field(..., description="使用的模型")
    choices: List[StreamChoice] = Field(..., description="选择结果列表")


class HealthResponse(BaseModel):
    """健康检查响应"""
    status: str = Field(default="ok", description="服务状态")
    version: str = Field(..., description="服务版本")
    timestamp: datetime = Field(default_factory=datetime.now, description="当前时间")


class ErrorResponse(BaseModel):
    """错误响应模型"""
    code: int = Field(..., description="错误码")
    message: str = Field(..., description="错误信息")
    detail: Optional[str] = Field(default=None, description="详细错误信息")
