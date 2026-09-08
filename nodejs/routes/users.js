/**
 * 用户域路由聚合
 * 将原先单文件的 users.js 按职责拆为多个子模块后，在此统一挂载。
 * 对外保持原有挂载路径不变：app.js 中以 app.use("/api/v1", usersRouter) 引入。
 */

const express = require("express");

const meRouter = require("./users/me");
const searchRouter = require("./users/search");
const messagesRouter = require("./users/messages");
const friendsRouter = require("./users/friends");
const profileRouter = require("./users/profile");

const router = express.Router();

// 用户中心：资料、头像、收藏、帖子
router.use(meRouter);

// 用户搜索
router.use(searchRouter);

// 消息与会话
router.use(messagesRouter);

// 好友申请与关系
router.use(friendsRouter);

// 用户主页资料（对外展示）：/users/:userId、/users/:userId/posts 等
// 保持与原有 router.use("/users", profileRouter) 等效
router.use("/users", profileRouter);

module.exports = router;
