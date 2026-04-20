# Day 2 · SpringCloud 基础骨架

> 目标：搭好父工程 + Gateway + Nacos + user-service，跑通服务注册与网关转发。

---

## 一、最终产出

```
NovaChat/
├── pom.xml                  # 父工程（packaging=pom，统一 BOM）
├── nova-gateway/            # 网关服务，端口 8080
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/wang/novachat/gateway/
│       │   ├── GatewayApplication.java
│       │   └── config/HealthRouterConfig.java
│       └── resources/
│           ├── bootstrap.yml
│           └── application.yml
└── nova-user/               # 用户服务，端口 8081，context-path=/user
    ├── pom.xml
    └── src/main/
        ├── java/com/wang/novachat/user/
        │   ├── UserApplication.java
        │   └── controller/HealthController.java
        └── resources/
            ├── bootstrap.yml
            └── application.yml
```

---

## 二、技术栈与版本选型

| 组件                  | 版本         | 说明                         |
| --------------------- | ------------ | ---------------------------- |
| JDK                   | 17           | Spring Boot 3 最低要求       |
| Spring Boot           | 3.2.5        | 基础框架                     |
| Spring Cloud          | 2023.0.1     | 对应 Boot 3.2 的发布列车     |
| Spring Cloud Alibaba  | 2023.0.1.0   | Nacos / Sentinel / Seata BOM |
| Nacos Server          | 2.2.x / 2.3.x | 推荐 2.3.x                  |
| Lombok                | 1.18.32      | 简化样板代码                 |
| MyBatis-Plus          | 3.5.5        | Boot3-starter，预留          |
| Hutool                | 5.8.26       | 工具箱，预留                 |

> 三个版本必须匹配，否则启动就会报 Autoconfigure 冲突。记忆口诀：**Boot 3.2 → Cloud 2023.0 → Alibaba 2023.0.x**。

---

## 三、关键设计说明

### 1. 父工程 `pom.xml`
- `packaging=pom`，仅做 **依赖版本统一** 与 **子模块聚合**。
- 通过三个 `import` BOM（spring-boot-dependencies、spring-cloud-dependencies、spring-cloud-alibaba-dependencies）锁定全部子模块版本，子模块不再写版本号。
- `pluginManagement` 里提前声明 `spring-boot-maven-plugin` 并排除 lombok，后续子模块直接继承即可。
- 加了阿里云 Maven 仓库源，国内拉取速度快。

### 2. `nova-gateway`
- 依赖：`spring-cloud-starter-gateway` + nacos discovery + nacos config + loadbalancer + **spring-cloud-starter-bootstrap**。
  - ⚠️ Spring Cloud 2020+ 之后，`bootstrap.yml` 不再默认加载，**必须显式依赖 `spring-cloud-starter-bootstrap`**，否则 `bootstrap.yml` 不生效。
- `GatewayApplication` 开启 `@EnableDiscoveryClient`（Boot3 下可省，但显式声明更清晰）。
- 路由策略：
  - 显式路由 `nova-user`：`/api/user/**` → `lb://nova-user`，`StripPrefix=2`（去掉 `/api/user`）。
  - 同时开启 `discovery.locator.enabled=true`，作为保底（以服务名直连，如 `/nova-user/**`）。
- 健康接口：Gateway 基于 WebFlux，用 `RouterFunction` 方式暴露 `/health/ping`。

### 3. `nova-user`
- 依赖：`spring-boot-starter-web` + actuator + nacos discovery + nacos config + bootstrap。
- `server.servlet.context-path=/user`，配合 Gateway `StripPrefix=2`，最终外部访问路径稳定：
  ```
  外部 → Gateway                     → user-service
  /api/user/health/ping   /health/ping   /user/health/ping
  ```
- Actuator 暴露 `health,info`，配合 Nacos 服务健康检查（后续可用于 Sentinel / K8s 探针）。

---

## 四、接口约定

### 1. 直连 user-service
```
GET http://127.0.0.1:8081/user/health/ping
GET http://127.0.0.1:8081/user/actuator/health
```

### 2. 通过 Gateway 访问
```
GET http://127.0.0.1:8080/health/ping               # gateway 自身
GET http://127.0.0.1:8080/api/user/health/ping      # 显式路由到 user
GET http://127.0.0.1:8080/nova-user/user/health/ping # discovery locator 保底
```

返回示例：
```json
{
  "app": "nova-user",
  "port": 8081,
  "status": "UP",
  "time": "2026-04-20T15:40:00"
}
```

---

## 五、启动步骤

### 1. 启动 Nacos（单机模式）
```bash
# 解压 nacos-server-2.3.x
cd nacos/bin
# Windows
startup.cmd -m standalone
# Linux/Mac
sh startup.sh -m standalone
```
控制台：<http://127.0.0.1:8848/nacos>，默认账号 `nacos / nacos`。

### 2. 启动两个服务
```bash
# 项目根目录
mvn clean install -DskipTests

# 方式一：IDEA 里直接运行 GatewayApplication / UserApplication
# 方式二：命令行
mvn -pl nova-user spring-boot:run
mvn -pl nova-gateway spring-boot:run
```

### 3. 验证
- Nacos 控制台 → 服务管理 → 服务列表，应能看到：`nova-gateway`、`nova-user`，分组 `NOVA_GROUP`。
- 浏览器访问上面的三条 URL，应都能返回 `status=UP`。

---

## 六、踩坑清单（遇到时先查这里）

| 现象 | 原因 | 解决 |
| ---- | ---- | ---- |
| `bootstrap.yml` 不生效，读不到 Nacos 配置 | Spring Cloud 2020+ 默认关闭 bootstrap | 加依赖 `spring-cloud-starter-bootstrap` |
| `No spring.config.import property has been defined` | 新版 Cloud 用 `spring.config.import` 替代 bootstrap | 方案 A：加 bootstrap 依赖（已采用）；方案 B：改用 `spring.config.import=nacos:xxx` |
| `No instances available for nova-user` | 网关路由写了 `lb://`，但未引入 `spring-cloud-starter-loadbalancer` | gateway pom 已补上 loadbalancer 依赖 |
| Nacos 注册不上 | 1. Nacos 没起 2. namespace/group 不一致 3. 防火墙 | 检查 8848 端口、`bootstrap.yml` 中的 namespace/group |
| Boot 3 下 Lombok 报错 | 注解处理器未配置 | IDEA：Settings → Build → Compiler → Annotation Processors 勾选 Enable |

---

## 七、Day 2 收尾 Checklist

- [x] 根 `pom.xml` 改造为父工程，三大 BOM 就位
- [x] `nova-gateway` 能启动，注册进 Nacos
- [x] `nova-user` 能启动，注册进 Nacos
- [x] `/health/ping` 可直连、可经网关访问
- [x] 路由规则 `/api/user/**` → `nova-user` 跑通
- [x] 开发文档归档到 `docs/day02-*.md`

---

## 八、下一步（Day 3 预告）

- 引入 MySQL + MyBatis-Plus，建 `user` 表
- 实现注册/登录接口
- 引入 JWT，网关统一鉴权过滤器
- 补充 `nova-common` 模块（统一响应体、全局异常）
