#!/bin/bash
set -e

echo "========== NovaChat 构建部署脚本 =========="

# 构建 common 模块
echo "[1/4] 构建 nova-common..."
mvn clean install -DskipTests -pl nova-common

# 并行构建其他模块
echo "[2/4] 构建微服务模块..."
mvn clean package -DskipTests -pl nova-gateway -am &
mvn clean package -DskipTests -pl nova-user -am &
mvn clean package -DskipTests -pl nova-chat -am &
wait

echo "[3/4] 构建 Docker 镜像..."
docker-compose build

echo "[4/4] 启动服务..."
docker-compose up -d

echo ""
echo "========== 启动完成 =========="
echo "服务状态："
docker-compose ps
echo ""
echo "访问地址："
echo "  - Gateway:   http://localhost:10000"
echo "  - Swagger:   http://localhost:10000/swagger-ui.html"
echo "  - Nacos:     http://localhost:18848/nacos (nacos/nacos)"
echo "  - MinIO:     http://localhost:19001 (minioadmin/minioadmin123)"
echo "  - MySQL:     localhost:13306 (root/root123)"
echo "  - Redis:     localhost:16379"
