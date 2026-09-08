/**
 * 应用入口
 * 负责：中间件装配、静态资源托管、数据库同步、路由挂载与启动。
 */

const express = require("express");
const cors = require("cors");
const path = require("path");
const { sequelize } = require("./models");

// 路由
const authRouter = require("./routes/auth");
const postsRouter = require("./routes/posts");
const usersRouter = require("./routes/users");
const commentsRouter = require("./routes/comments");
const sharesRouter = require("./routes/shares");
const followsRouter = require("./routes/follows");

const app = express();

// ---------------------------------------------------------------------------
// 中间件
// ---------------------------------------------------------------------------
app.use(cors());
app.use(express.json({ limit: "1mb" }));
// 静态资源：上传的头像 / 背景图
app.use("/uploads", express.static(path.join(__dirname, "uploads")));

// ---------------------------------------------------------------------------
// 启动
// ---------------------------------------------------------------------------
async function start() {
  try {
    await sequelize.authenticate();
    console.log("数据库连接成功");

    // 开发阶段同步表结构，生产环境请换成 migration
    await sequelize.sync({ alter: true });
    console.log("表同步完成：", Object.keys(sequelize.models).join(", "));

    // 路由挂载（注意顺序：/users 相关的 follows 需在 users 之前或之后均可，此处保持原顺序）
    app.use("/api/v1/auth", authRouter);
    app.use("/api/v1", usersRouter);
    app.use("/api/v1/users", followsRouter);
    app.use("/api/v1/posts", postsRouter);
    app.use("/api/v1/posts", commentsRouter);
    app.use("/api/v1/posts", sharesRouter);

    const PORT = process.env.PORT || 8080;
    app.listen(PORT, () => {
      console.log(`服务已启动：http://localhost:${PORT}`);
    });
  } catch (err) {
    console.error("启动失败：", err.message);
    process.exit(1);
  }
}

start();
