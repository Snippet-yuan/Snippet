const express = require("express");
const cors = require("cors");
const path = require("path");
const { sequelize } = require("./models");

const authRouter = require("./routes/auth");
const postsRouter = require("./routes/posts");
const usersRouter = require("./routes/users");

const app = express();

app.use(cors());
app.use(express.json({ limit: "1mb" }));
app.use("/uploads", express.static(path.join(__dirname, "uploads")));

// 先同步表，再启动服务器
async function start() {
  try {
    await sequelize.authenticate();
    console.log("数据库连接成功");

    // 开发阶段同步表结构，生产环境请换成 migration
    await sequelize.sync();
    console.log("表同步完成：", Object.keys(sequelize.models).join(", "));

    app.use("/api/v1/auth", authRouter);
    app.use("/api/v1", usersRouter);
    app.use("/api/v1/posts", postsRouter);

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
