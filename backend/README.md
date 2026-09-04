# Snippet Backend

Snippet 的后端基础工程，技术栈为 Java 17、Spring Boot 3.4、MyBatis、MySQL 8、Redis 和 Flyway。

## 本地启动依赖

~~~powershell
docker compose up -d mysql redis
~~~

默认开发数据库：

~~~text
地址：localhost:3307
数据库：snippet
用户：snippet
密码：snippet_dev_password
~~~

Flyway 会在应用启动时自动执行 `src/main/resources/db/migration` 下的迁移脚本。V2 扩展用户资料字段并创建后续业务需要的表，V3 增加 `token_version` 用于密码修改后的 JWT 统一失效；当前代码只实现 Asset、User 和 Auth 相关代码。

## 启动应用

PowerShell 中可以先设置数据库密码：

~~~powershell
$env:DB_PASSWORD = "snippet_dev_password"
mvn spring-boot:run
~~~

健康检查：

~~~text
GET http://localhost:8080/api/v1/system/health
GET http://localhost:8080/actuator/health
~~~

## Asset 图片上传

上传接口：

~~~text
POST /api/v1/assets/upload
Authorization: Bearer <access-token>
Content-Type: multipart/form-data
file=<图片文件>
~~~

接口只接受 PNG、JPEG、GIF 图片，默认单文件上限为 10 MB。资源 owner 从 JWT 的 sub 读取，前端不需要也不能传入 ownerId。服务会校验图片内容、生成随机对象键、计算 SHA-256，并将文件元数据登记到 V1 的 asset 表。

可通过环境变量配置资源存储：

~~~text
SNIPPET_ASSET_STORAGE_PATH=./data/assets
SNIPPET_ASSET_PUBLIC_BASE_URL=/api/v1/assets/files
SNIPPET_ASSET_MAX_FILE_SIZE=10MB
SNIPPET_ASSET_MAX_REQUEST_SIZE=12MB
~~~

返回结果中的 url 由 SNIPPET_ASSET_PUBLIC_BASE_URL 与对象键拼接；如果使用对象存储或 CDN，请将该前缀配置为对应的公开访问地址。

## 当前用户资料

获取当前登录用户资料：

~~~text
GET /api/v1/users/me
Authorization: Bearer <access-token>
~~~

返回 `id、username、email、nickname、avatarAssetId、backgroundAssetId`。服务端从 JWT 的 `sub` 获取用户 ID，不接受前端传入的 `userId`。响应不会返回密码哈希、账号状态、令牌或其他内部安全字段。

## 修改当前用户资料

~~~text
PATCH /api/v1/users/me
Authorization: Bearer <access-token>
Content-Type: application/json
~~~

请求中只填写需要修改的字段，未填写字段保持不变：

~~~json
{
  "email": "user@example.com",
  "nickname": "Snippet 用户",
  "avatarAssetId": 10,
  "backgroundAssetId": 11
}
~~~

头像和背景图资源必须属于当前用户，并且状态为 READY。邮箱和用户名必须保持唯一。当前 V2 的可选资料字段没有默认伪造值，已有用户未提供的数据保持为空。

## 修改密码

~~~text
PUT /api/v1/auth/password
Authorization: Bearer <access-token>
Content-Type: application/json
~~~

请求体需要同时提供当前密码、新密码和确认密码。服务端会再次验证当前密码，确认成功后使用 BCrypt 保存新密码哈希，并在同一条数据库更新中将用户的 `token_version` 加 1，绝不保存或返回明文密码。

密码修改成功后，当前 JWT 以及该用户之前签发的所有 JWT 都会被服务端拒绝；前端必须使用新密码重新调用登录接口获取新 JWT。旧 JWT 不需要在数据库中逐个保存或物理删除，认证时通过版本不匹配实现统一撤销。

## 业务实现建议

推荐按以下顺序补充：

1. auth：注册、登录、密码修改；
2. user：当前用户资料和头像；
3. post：创建帖子、保存草稿、发布；
4. post_revision：历史版本和恢复；
5. Redis：会话、登录限流、公开帖子缓存。

保存草稿时使用 post_draft.version 做乐观锁；发布时在一个事务中创建 post_revision 并更新 post.published_revision_id。

内容 JSON 建议保留 schemaVersion，只允许白名单元素和样式字段，不直接保存任意 HTML、JavaScript 或 CSS。
