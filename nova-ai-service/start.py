#!/usr/bin/env python3
"""
Nova AI Service 启动脚本
"""
import uvicorn
from app.core.config import settings

if __name__ == "__main__":
    print(f"🚀 启动 {settings.APP_NAME} v{settings.APP_VERSION}")
    print(f"📍 服务地址: http://{settings.HOST}:{settings.PORT}")
    print(f"📚 API 文档: http://{settings.HOST}:{settings.PORT}/docs")
    print(f"❤️  健康检查: http://{settings.HOST}:{settings.PORT}/api/v1/health")
    print("-" * 50)

    uvicorn.run(
        "main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=settings.DEBUG,
        log_level="info",
        access_log=True
    )
