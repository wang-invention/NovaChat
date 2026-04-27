"""
应用配置
使用 Pydantic Settings 管理配置
"""
from typing import List
from pydantic_settings import BaseSettings
from pydantic import Field


class Settings(BaseSettings):
    """应用配置类"""

    # 应用信息
    APP_NAME: str = Field(default="NovaAI-Service", description="应用名称")
    APP_VERSION: str = Field(default="1.0.0", description="应用版本")
    DEBUG: bool = Field(default=False, description="调试模式")

    # 服务配置
    HOST: str = Field(default="0.0.0.0", description="服务主机")
    PORT: int = Field(default=8000, description="服务端口")

    # CORS 配置
    CORS_ORIGINS: List[str] = Field(default=["*"], description="允许的跨域来源")

    # OpenAI 配置
    OPENAI_API_KEY: str = Field(default="", description="OpenAI API Key")
    OPENAI_BASE_URL: str = Field(default="https://api.openai.com/v1", description="OpenAI Base URL")
    OPENAI_MODEL: str = Field(default="gpt-3.5-turbo", description="默认模型")

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        case_sensitive = True


# 全局配置实例
settings = Settings()
