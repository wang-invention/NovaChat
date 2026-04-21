# Day 3 · 统一基础工程 nova-common

> 目标：搭好公共基础模块 `nova-common`，统一响应体、全局异常与工具类，后续所有业务服务直接复用。

---

## 一、最终产出

```
nova-common/
├── pom.xml
└── src/main/java/com/wang/novachat/common/
    ├── constant/
    │   └── CommonConstant.java          # 全局常量（Header、分页默认值…）
    ├── exception/
    │   ├── BusinessException.java       # 业务异常
    │   └── GlobalExceptionHandler.java  # 全局异常处理（@RestControllerAdvice）
    ├── result/
    │   ├── Result.java                  # 统一响应体
    │   ├── ResultCode.java              # 状态码枚举
    │   └── PageResult.java              # 分页响应体
    └── utils/
        ├── JsonUtils.java               # Jackson 封装
        └── IdUtils.java                 # 基于 Hutool 的 UUID / 雪花 ID
```

配套改动：

- `nova-user` 引入 `nova-common` 依赖 + `spring-boot-starter-validation`。
- `UserApplication` 显式扫描 `com.wang.novachat.common`，让 `GlobalExceptionHandler` 生效。
- `HealthController#ping` 返回 `Result<Map>`；新增 `/health/boom` 用于联调全局异常。

---

## 二、关键设计说明

### 1. 响应体 `Result<T>`

- 字段：`code / message / data / timestamp`。
- 去掉 `extends HashMap` —— 继承 Map 会让 Jackson 同时把 Map entry + 字段都序列化出去，前端收到冗余字段。
- 提供一组静态工厂：`success()`、`success(data)`、`fail()`、`fail(message)`、`fail(ResultCode)`、`fail(code, message)` 等，业务代码不要 `new Result(...)`。
- 配合 `ResultCode` 枚举，code 使用 `Integer`，方便前端 `switch` 分支判断。

### 2. 状态码 `ResultCode`

按段划分，避免各模块冲突：

| 段         | 用途                       |
| ---------- | -------------------------- |
| 200        | 成功                       |
| 400 ~ 499  | 通用客户端错误             |
| 500 ~ 599  | 通用服务端错误             |
| 1000 ~ 1999 | 用户模块                  |
| 2000 ~ 2999 | AI / 聊天模块             |
| 3000 ~ 3999 | 订单 / 支付模块           |

### 3. 业务异常 `BusinessException`

- `extends RuntimeException`，走运行时异常，避免污染方法签名。
- 业务代码里直接：
  ```java
  throw new BusinessException(ResultCode.USER_NOT_EXIST);
  ```
- 所有业务异常都由 `GlobalExceptionHandler#handleBusiness` 捕获转 `Result.fail`。

### 4. 全局异常 `GlobalExceptionHandler`

覆盖以下场景：

| 异常类型                                     | 返回状态                            |
| -------------------------------------------- | ----------------------------------- |
| `BusinessException`                          | 原样返回 `code + message`           |
| `MethodArgumentNotValidException`            | `400`，拼接字段级校验失败信息       |
| `BindException`                              | `400`                               |
| `ConstraintViolationException`               | `400`（`@Validated` 方法形参校验）  |
| `MissingServletRequestParameterException`    | `400`                               |
| `HttpMessageNotReadableException`            | `400`（JSON 格式错误）              |
| `HttpRequestMethodNotSupportedException`     | `405`                               |
| `NoHandlerFoundException`                    | `404`                               |
| 兜底 `Exception`                             | `500 系统繁忙`                      |

> ⚠️ 用的是 `jakarta.validation.*`（Spring Boot 3 / Jakarta EE 9+），不是 `javax.validation`。

### 5. 工具类 & 常量

- `JsonUtils`：统一的 `ObjectMapper`，默认：
  - 注册 `JavaTimeModule`，`LocalDateTime` 序列化为 ISO 字符串而非时间戳。
  - `FAIL_ON_UNKNOWN_PROPERTIES=false`，向前兼容老版本 DTO。
  - `NON_NULL` 忽略 null 字段。
- `IdUtils`：封装 Hutool 的 UUID / Snowflake，后续 `workerId` 会改为按服务实例派发。
- `CommonConstant`：`Authorization`、`X-User-Id`、分页默认值等，统一避免魔法字符串。

---

## 三、依赖策略

`nova-common` 的 web 相关依赖全部 `<optional>true</optional>`：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <optional>true</optional>
</dependency>
```

理由：

1. `GlobalExceptionHandler` 依赖 `spring-webmvc`，但 `nova-gateway` 是 WebFlux，不能强拉进去。
2. 业务 MVC 服务（如 `nova-user`）自己会显式引入 `spring-boot-starter-web`，common 的 optional 依赖只负责编译期可见。
3. 未来如果有纯 worker / 定时任务服务，也不会被迫引入一整套 web。

---

## 四、使用方式

### 1. 在业务服务里引入

```xml
<dependency>
    <groupId>com.wang</groupId>
    <artifactId>nova-common</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 让全局异常处理器生效

common 不在服务启动类所在包下，默认 `@ComponentScan` 扫不到，必须显式声明：

```java
@SpringBootApplication(scanBasePackages = {
        "com.wang.novachat.user",
        "com.wang.novachat.common"
})
```

### 3. Controller 写法

```java
@GetMapping("/{id}")
public Result<UserVO> get(@PathVariable Long id) {
    UserVO vo = userService.getById(id);
    if (vo == null) {
        throw new BusinessException(ResultCode.USER_NOT_EXIST);
    }
    return Result.success(vo);
}
```

---

## 五、联调验证

启动 `nova-user`，通过 Gateway（Day 2 已搭好）或直连访问：

```bash
# 正常成功
curl http://127.0.0.1:8081/user/health/ping

# 业务异常（应返回 code=1001）
curl http://127.0.0.1:8081/user/health/boom?type=biz

# 系统异常（应返回 code=500 系统繁忙）
curl http://127.0.0.1:8081/user/health/boom?type=sys
```

期望响应：

```json
// /health/ping
{
  "code": 200,
  "message": "操作成功",
  "data": { "app": "nova-user", "port": 8081, "status": "UP", "time": "2026-04-21T10:00:00" },
  "timestamp": 1745208000000
}

// /health/boom?type=biz
{ "code": 1001, "message": "用户不存在", "data": null, "timestamp": ... }

// /health/boom?type=sys
{ "code": 500, "message": "系统繁忙，请稍后再试", "data": null, "timestamp": ... }
```

---

## 六、踩坑清单

| 现象                                                  | 原因                                                                                                        | 解决                                                                                   |
| ----------------------------------------------------- | ----------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------- |
| `GlobalExceptionHandler` 不生效                       | `@SpringBootApplication` 默认只扫启动类所在包                                                               | 启动类加 `scanBasePackages` 把 `com.wang.novachat.common` 也包进去                     |
| `import javax.validation.*` 报红                      | Spring Boot 3 已切到 `jakarta.*`                                                                            | 改用 `jakarta.validation.*`                                                            |
| `Result` 里 `code / message` 字段重复 or 被当成 Map   | 原实现 `extends HashMap` 导致 Jackson 同时序列化 Map entry 和字段                                           | 去掉 HashMap 继承，改为标准 POJO                                                       |
| gateway 启动报 `spring-webmvc` 相关 ClassNotFound     | common 把 web 当必选依赖                                                                                    | 把 common 里的 `spring-boot-starter-web` 设为 `optional=true`                          |
| `NoHandlerFoundException` 不走全局异常                | Spring Boot 3 默认会把 404 交给静态资源兜底                                                                 | `application.yml` 里加 `spring.mvc.throw-exception-if-no-handler-found=true`（可选）   |

---

## 七、Day 3 收尾 Checklist

- [x] `nova-common` 子模块建好并被父工程聚合
- [x] `Result / ResultCode / PageResult` 就位
- [x] `BusinessException + GlobalExceptionHandler` 覆盖业务、参数校验、兜底
- [x] `CommonConstant / JsonUtils / IdUtils` 工具类就位
- [x] `nova-user` 接入 common，`/health/ping` 返回统一 `Result`
- [x] 新增 `/health/boom` 用于联调两类异常

---

## 八、下一步（Day 4 预告）

- 引入 MySQL + MyBatis-Plus，建 `user` 表
- 实现注册 / 登录接口（DTO + `@Valid` 校验，直接走全局异常）
- 引入 JWT，网关统一鉴权过滤器，把用户信息通过 `X-User-Id` 透传下游
