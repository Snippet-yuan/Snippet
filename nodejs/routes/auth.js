/**
 * 认证路由
 * POST /api/v1/auth/login     登录
 * POST /api/v1/auth/register  注册
 *
 * 薄控制器：校验与业务逻辑在 services/userService 中完成。
 */

const express = require("express");
const userService = require("../services/userService");
const { success, fail } = require("../utils/response");

const router = express.Router();

// 登录
router.post("/login", async (req, res) => {
  try {
    const data = await userService.login(req.body);
    res.json(success(data));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// 注册
router.post("/register", async (req, res) => {
  try {
    const data = await userService.register(req.body);
    res.status(201).json({ code: 0, message: "注册成功", data });
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

module.exports = router;
