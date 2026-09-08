/**
 * 帖子 - 收藏
 * POST   /:postId/favorite   收藏
 * DELETE /:postId/favorite   取消收藏
 */

const express = require("express");
const { Op } = require("sequelize");
const { Post } = require("../../models");
const { getUserId } = require("../../middleware/auth");
const { success, fail } = require("../../utils/response");
const { getPostWithState } = require("./helpers");

const router = express.Router();

// 收藏
router.post("/:postId/favorite", async (req, res) => {
  try {
    const userId = getUserId(req);
    const postId = Number(req.params.postId);
    if (!Number.isInteger(postId) || postId <= 0) return res.status(400).json(fail("postId 无效", 400));

    const { PostFavorite } = require("../../models");
    const state = await getPostWithState(postId, userId);
    if (!state) return res.status(404).json(fail("帖子不存在", 404));

    if (!state.favorited) {
      await PostFavorite.create({ postId, userId });
      await state.post.increment("favoriteCount");
    }

    const updated = await Post.findByPk(postId, { attributes: ["favoriteCount"] });
    res.json(success({ postId: String(postId), favorited: true, favoriteCount: updated.favoriteCount }, "收藏成功"));
  } catch (err) {
    const status = err.statusCode || (err.name === "SequelizeUniqueConstraintError" ? 409 : 500);
    res.status(status).json(fail(err.message || "收藏失败，请稍后重试", status));
  }
});

// 取消收藏
router.delete("/:postId/favorite", async (req, res) => {
  try {
    const userId = getUserId(req);
    const postId = Number(req.params.postId);
    if (!Number.isInteger(postId) || postId <= 0) return res.status(400).json(fail("postId 无效", 400));

    const { PostFavorite } = require("../../models");
    const state = await getPostWithState(postId, userId);
    if (!state) return res.status(404).json(fail("帖子不存在", 404));

    if (state.favorited) {
      await PostFavorite.destroy({ where: { postId, userId } });
      await state.post.decrement("favoriteCount", { by: 1, where: { favoriteCount: { [Op.gt]: 0 } } });
    }

    const updated = await Post.findByPk(postId, { attributes: ["favoriteCount"] });
    res.json(success({ postId: String(postId), favorited: false, favoriteCount: updated.favoriteCount }, "取消收藏成功"));
  } catch (err) {
    const status = err.statusCode || (err.name === "SequelizeUniqueConstraintError" ? 409 : 500);
    res.status(status).json(fail(err.message || "取消收藏失败，请稍后重试", status));
  }
});

module.exports = router;
