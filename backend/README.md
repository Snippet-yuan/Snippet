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

Flyway 会在应用启动时自动执行 `src/main/resources/db/migration` 下的迁移脚本。V2 扩展用户资料字段并创建后续业务需要的表，V3 增加 `token_version` 用于密码修改后的 JWT 统一失效；当前代码已实现 Asset、User、Auth，以及 Post 的创建、草稿保存、修改、删除、发布、公开查看、点赞、收藏和评论。

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

## 创建帖子

创建接口：

~~~text
POST /api/v1/posts
Authorization: Bearer <access-token>
Content-Type: application/json
~~~

请求体当前只接收可选标题：

~~~json
{
  "title": "我的第一篇 Snippet"
}
~~~

服务端从 JWT 的 `sub` 获取 ownerId，不接受前端传入 `ownerId`。标题会去除首尾空格，长度不能超过 200 个字符，也不能包含控制字符或只包含空白字符。创建成功后，服务会在一个事务中写入 `post` 主记录和 `post_draft` 初始草稿，初始状态为 `DRAFT`，正文为空文档，`schemaVersion=1`、`version=0`。

本阶段已完成创建帖子、查看本人帖子详情、保存正文草稿、修改帖子基本信息、删除帖子、发布帖子、公开查看、点赞、收藏和评论；转发将在后续阶段实现。

## 查看帖子详情

获取当前用户自己的帖子详情：

~~~text
GET /api/v1/posts/{postId}
Authorization: Bearer <access-token>
~~~

服务端从 JWT 的 `sub` 获取当前用户 ID，并使用 `postId + ownerId` 查询帖子。只有帖子所有者可以查看草稿内容，访问其他用户的帖子时统一返回 404，避免泄露私密帖子的存在。

返回数据包含帖子标题、描述、slug、状态、草稿正文、内容结构版本、草稿版本和发布时间。当前登录用户查看自己的帖子详情，公开帖子查询使用单独的公开接口。

## 保存帖子草稿

保存当前用户正在编辑的正文：

~~~text
PUT /api/v1/posts/{postId}/draft
Authorization: Bearer <access-token>
Content-Type: application/json
~~~

请求体中的 `content` 必须是 `type=doc` 且包含 `children` 数组的结构化 JSON，`schemaVersion` 当前只能为 1，`version` 必须填写客户端最近一次读取到的草稿版本。

服务端会校验帖子归属、正文大小（最大 1 MB）、嵌套深度、节点数量和危险脚本文本。保存成功后草稿版本自动加 1；如果版本已过期，返回 409，客户端需要重新读取详情后再保存。

## 修改帖子基本信息

修改当前用户帖子的标题和描述，正文仍通过保存草稿接口单独维护：

~~~text
PATCH /api/v1/posts/{postId}
Authorization: Bearer <access-token>
Content-Type: application/json
~~~

请求体只填写需要修改的字段：

~~~json
{
  "title": "修改后的标题",
  "description": "修改后的文章简介"
}
~~~

服务端从 JWT 的 `sub` 获取当前用户 ID，并校验帖子归属。标题会去除首尾空格，描述最大 5000 个字符；未填写的字段保持不变，描述传空白字符可以清空。修改成功后返回最新帖子详情。

## 删除帖子

删除当前用户的帖子：

~~~text
DELETE /api/v1/posts/{postId}
Authorization: Bearer <access-token>
~~~

删除操作在一个事务中执行。服务端先校验帖子归属，再解除已发布版本关联，依次删除图片关联、点赞、收藏、转发、评论、历史版本和草稿，最后删除 `post` 主记录。删除其他用户的帖子时统一返回 404。关联的 asset 文件不会被直接删除，因为资源可能仍被其他业务引用。

## 发布帖子

将当前用户指定版本的草稿发布为文章：

~~~text
POST /api/v1/posts/{postId}/publish
Authorization: Bearer <access-token>
Content-Type: application/json
~~~

请求体：

~~~json
{
  "expectedVersion": 0
}
~~~

服务端从 JWT 的 `sub` 获取当前用户 ID，并校验帖子归属和草稿版本。发布时会再次校验正文结构，在一个事务中创建递增的 `post_revision` 历史版本，再更新 `post.status=PUBLISHED`、`published_revision_id` 和 `published_at`。版本不一致时返回 409，客户端需要先重新读取帖子详情。

## 公开查看帖子

根据公开标识查看已经发布的帖子：

~~~text
GET /api/v1/public/posts/{slug}
~~~

该接口无需 JWT。服务端只查询 `post.status=PUBLISHED` 的帖子，并根据 `published_revision_id` 读取正式发布版本，不读取 `post_draft`。帖子未发布、公开标识不存在或正式版本缺失时不会返回草稿内容；前两种情况返回 404，正式版本数据异常返回 500。

## 点赞帖子

点赞、取消点赞和查询当前用户的点赞状态：

~~~text
POST   /api/v1/posts/{postId}/like
DELETE /api/v1/posts/{postId}/like
GET    /api/v1/posts/{postId}/like
Authorization: Bearer <access-token>
~~~

点赞关系写入 `post_like` 表，服务端从 JWT 的 `sub` 获取用户 ID，不接受前端传入 `userId`。只有 `PUBLISHED` 帖子可以被点赞；重复点赞和重复取消点赞保持幂等，响应中的 `liked` 表示操作后的状态。数据库通过 `(post_id, user_id)` 唯一约束防止同一用户重复点赞。

## 收藏帖子

收藏、取消收藏和查询当前用户的收藏状态：

~~~text
POST   /api/v1/posts/{postId}/favorite
DELETE /api/v1/posts/{postId}/favorite
GET    /api/v1/posts/{postId}/favorite
Authorization: Bearer <access-token>
~~~

收藏关系写入 `post_favorite` 表，服务端从 JWT 的 `sub` 获取用户 ID，不接受前端传入 `userId`。只有 `PUBLISHED` 帖子可以被收藏；重复收藏和重复取消收藏保持幂等，响应中的 `favorited` 表示操作后的状态。数据库通过 `(post_id, user_id)` 唯一约束防止同一用户重复收藏。

## 帖子评论

发表评论：

~~~text
POST /api/v1/posts/{postId}/comments
Authorization: Bearer <access-token>
Content-Type: application/json
~~~

请求体只接收纯文本评论内容：

~~~json
{
  "content": "这篇文章很有帮助"
}
~~~

服务端从 JWT 的 `sub` 获取评论作者 ID，不接受请求体中的 `authorId` 或 `userId`。只有已发布的帖子可以发表评论；评论内容不能为空，去除首尾空白后最大 2000 个字符，并拒绝非法控制字符。评论按纯文本保存，前端展示时必须使用文本节点或其他安全的输出编码，不能把评论直接当作 HTML 插入页面。

公开查看评论：

~~~text
GET /api/v1/public/posts/{slug}/comments?limit=20&offset=0
~~~

评论查询无需登录，只能通过已发布帖子的 `slug` 读取。`limit` 默认 20，最大 100；`offset` 默认 0，最大 10000。公开响应只返回评论 ID、帖子 ID、作者 ID、作者展示名称、头像资源 ID、评论内容和创建时间，不返回邮箱、密码哈希、账号状态等私密字段；已停用账号的评论不会出现在公开列表中。

删除自己的评论：

~~~text
DELETE /api/v1/posts/{postId}/comments/{commentId}
Authorization: Bearer <access-token>
~~~

删除 SQL 会同时匹配 `post_id`、`comment_id` 和 JWT 中的 `author_id`。因此用户只能删除自己的评论；评论不存在或不属于当前用户时统一返回 404，避免泄露评论归属信息。帖子删除时，服务端会在同一事务中清理该帖子的评论关联。

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
3. post：创建帖子、保存草稿、修改、删除、发布、公开查看、点赞、收藏；
4. post_revision：历史版本和恢复；
5. Redis：会话、登录限流、公开帖子缓存。

保存草稿时使用 post_draft.version 做乐观锁；发布时在一个事务中创建 post_revision 并更新 post.published_revision_id。

内容 JSON 建议保留 schemaVersion，只允许白名单元素和样式字段，不直接保存任意 HTML、JavaScript 或 CSS。
