#!/bin/bash

echo "==== 停止所有服务容器 ===="
docker stop novachat-mysql novachat-redis novachat-minio novachat-nacos novachat-gateway novachat-user novachat-chat 2>/dev/null

echo "==== 删除所有服务容器 ===="
docker rm novachat-mysql novachat-redis novachat-minio novachat-nacos novachat-gateway novachat-user novachat-chat 2>/dev/null

echo "==== 删除镜像 (可选，清理后会重新拉取/构建) ===="
docker rmi mysql:8.0 redis:7-alpine minio/minio:latest nacos/nacos-server:v2.3.2 2>/dev/null
# 如果你 Gateway/User/Chat 是本地 build 的，可以选择不删镜像，保留本地构建
# docker rmi novachat-gateway novachat-user novachat-chat 2>/dev/null

echo "==== 删除所有卷 / 数据目录 ===="
rm -rf ./mysql_data ./redis_data ./minio_data ./nacos_data

echo "==== Cleanup complete ===="