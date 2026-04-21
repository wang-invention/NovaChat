# NovaChat 开发日志索引

按天沉淀开发过程，每篇文档都包含：**目标 / 产出 / 关键设计 / 验证步骤 / 踩坑 / 下一步**，方便随时续写。

| Day   | 主题                           | 文档                                                             |
| ----- | ------------------------------ | ---------------------------------------------------------------- |
| Day 1 | 项目立项 & 技术选型            | （见根目录 README）                                              |
| Day 2 | SpringCloud 基础骨架           | [day02-springcloud-skeleton.md](./day02-springcloud-skeleton.md) |
| Day 3 | 统一基础工程 nova-common       | [day03-common-skeleton.md](./day03-common-skeleton.md)           |
| Day 4 | 数据库初始化（三库五表）       | _TODO 待归档_                                                    |
| Day 5 | MyBatis-Plus 接入（预留）      | _TODO_                                                           |
| Day 6 | 用户注册登录 + 密码加密        | [day06-user-register-login.md](./day06-user-register-login.md)   |
| Day 7 | JWT 鉴权 + 网关统一过滤器      | _TODO_                                                           |
| Day 8 | AI 聊天模块（LangChain 对接）  | _TODO_                                                           |

## 写作约定

1. 文件命名：`dayNN-主题-英文kebab.md`。
2. 每篇文档必有：
   - 顶部一句话目标
   - 最终目录树（截至当天）
   - 依赖版本表（若有新增）
   - 可复制运行的启动命令
   - "踩坑清单" 表格
   - "Checklist" 收尾
   - "下一步" 预告
3. 代码改动与文档 **同一次 commit** 提交，保证历史可追溯。
