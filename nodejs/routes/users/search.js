/**
 * 用户搜索
 * GET /users/search?keyword=xxx
 * 按昵称模糊匹配，数字关键词额外按 ID 精确匹配。
 */

const express = require("express");
const { Op } = require("sequelize");
const { User } = require("../../models");
const { getUserId } = require("../../middleware/auth");
const { success, fail } = require("../../utils/response");

const router = express.Router();

router.get("/users/search", async (req, res) => {
  try {
    getUserId(req); // 需登录

    const keyword = String(req.query.keyword || "").trim();
    if (!keyword) return res.json(success([]));

    const isNumericId = Number.isInteger(Number(keyword));

    const users = await User.findAll({
      where: {
        [Op.or]: [
          { nickname: { [Op.like]: `%${keyword}%` } },
          ...(isNumericId ? [{ id: Number(keyword) }] : []),
        ],
      },
      attributes: ["id", "nickname", "avatar"],
      limit: 20,
      order: [["id", "ASC"]],
    });

    res.json(
      success(
        users.map((u) => ({
          id: String(u.id),
          nickname: u.nickname,
          avatar: u.avatar,
        }))
      )
    );
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

module.exports = router;
