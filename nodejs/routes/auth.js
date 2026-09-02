const express = require("express");
const router = express.Router();
const userService = require("../services/userService");
const { success, fail } = require("../utils/response");

router.post("/login", async (req, res) => {
  try {
    const data = await userService.login(req.body);
    res.json(success(data));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

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
