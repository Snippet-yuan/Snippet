/**
 * 帖子域路由聚合
 * 原单文件 posts.js 按职责拆为 feed / likes / favorites 三个子模块后在此聚合。
 * 对外保持原挂载路径：app.js 中以 app.use("/api/v1/posts", postsRouter) 引入。
 */

const express = require("express");

const feedRouter = require("./posts/feed");
const likesRouter = require("./posts/likes");
const favoritesRouter = require("./posts/favorites");

const router = express.Router();

router.use(feedRouter);
router.use(likesRouter);
router.use(favoritesRouter);

module.exports = router;
