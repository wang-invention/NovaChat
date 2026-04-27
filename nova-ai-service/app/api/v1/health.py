"""
健康检查接口
"""
from datetime import datetime
from fastapi import APIRouter
from app.models.schemas import HealthResponse
from app.core.config import settings

router = APIRouter(prefix="/health", tags=["健康检查"])


@router.get("", response_model=HealthResponse, summary="健康检查")
async def health_check():
    """
    服务健康检查接口

    返回服务状态、版本和当前时间
    """
    return HealthResponse(
        status="ok",
        version=settings.APP_VERSION,
        timestamp=datetime.now()
    )


@router.get("/ready", summary="就绪检查")
async def readiness_check():
    """
    服务就绪检查

    用于 Kubernetes 等容器编排平台的就绪探针
    """
    return {"ready": True}


@router.get("/live", summary="存活检查")
async def liveness_check():
    """
    服务存活检查

    用于 Kubernetes 等容器编排平台的存活探针
    """
    return {"alive": True}
