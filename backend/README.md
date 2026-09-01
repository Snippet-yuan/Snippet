# Snippet Backend

Snippet 的后端基础工程，技术栈为 Java 17、Spring Boot 3.4、MyBatis、MySQL 8、Redis 和 Flyway。

## 本地启动依赖

```powershell
docker compose up -d mysql redis
```

默认开发数据库：

```text
地址：localhost:3307
数据库：snippet
用户：snippet
密码：snippet_dev_password
```

Flyway 会在应用启动时自动执行 `src/main/resources/db/migration` 下的迁移脚本。

## 启动应用

PowerShell 中先设置数据库密码，并生成仅用于本地开发的 JWT 签名密钥：

```powershell
$env:DB_PASSWORD = "snippet_dev_password"
$jwtBytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($jwtBytes)
$env:SNIPPET_JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
mvn spring-boot:run
```

真实的 `SNIPPET_JWT_SECRET` 不能提交到 Git；不同环境应使用不同的随机密钥。

健康检查：

```text
GET http://localhost:8080/api/v1/system/health
GET http://localhost:8080/actuator/health
```

## 业务实现建议

推荐按以下顺序补充：

1. `auth`：注册、登录、退出、当前用户；
2. `user`：用户资料和头像；
3. `post`：创建帖子、保存草稿、发布；
4. `asset`：图片上传和资源归属校验；
5. `post_revision`：历史版本和恢复；
6. Redis：会话、登录限流、公开帖子缓存。

保存草稿时使用 `post_draft.version` 做乐观锁；发布时在一个事务中创建 `post_revision` 并更新 `post.published_revision_id`。

内容 JSON 建议保留 `schemaVersion`，只允许白名单元素和样式字段，不直接保存任意 HTML、JavaScript 或 CSS。
