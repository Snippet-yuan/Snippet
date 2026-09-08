/**
 * 关注域路由聚合
 * 原单文件 follows.js 按职责拆为 actions / lists 两个子模块后在此聚合。
 * 对外保持原挂载路径：app.js 中以 app.use("/api/v1/users", followsRouter) 引入。
 */

const express = require("express");

const actionsRouter = require("./follows/actions");
const listsRouter = require("./follows/lists");

const router = express.Router();

// 关注 / 取关
router.use(actionsRouter);
// 列表与状态查询
router.use(listsRouter);

module.exports = router;
