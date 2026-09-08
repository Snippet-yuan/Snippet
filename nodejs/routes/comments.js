/**
 * 评论域路由聚合
 * 原单文件 comments.js 按职责拆为 list / create 后在此聚合。
 * 对外保持原挂载路径：app.js 中以 app.use("/api/v1/posts", commentsRouter) 引入。
 */

const express = require("express");

const listRouter = require("./comments/list");
const createRouter = require("./comments/create");

const router = express.Router();

router.use(listRouter);
router.use(createRouter);

module.exports = router;
