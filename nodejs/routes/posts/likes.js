/**
 * 帖子 - 点赞
 * POST   /:postId/like    点赞
 * DELETE /:postId/like    取消点赞
 */

const express = require("express");
const { Op } = require("sequelize");
const { Post } = require("../../models");
const { getUserId } = require("../../middleware/auth");
const { success, fail } = require("../../utils/response");
const { getPostWithState } = require("./helpers");

const router = express.Router();

// 点赞
router.post("/:postId/like", async (req, res) => {
  try {
    const userId = getUserId(req);
    const postId = Number(req.params.postId);
    if (!Number.isInteger(postId) || postId <= 0) return res.status(400).json(fail("postId 无效", 400));

    const { PostLike } = require("../../models");
    const state = await getPostWithState(postId, userId);
    if (!state) return res.status(404).json(fail("帖子不存在", 404));

    if (!state.liked) {
      await PostLike.create({ postId, userId });
      await state.post.increment("likeCount");
    }

    const updated = await Post.findByPk(postId, { attributes: ["likeCount"] });
    res.json(success({ postId: String(postId), liked: true, likeCount: updated.likeCount }, "点赞成功"));
  } catch (err) {
    const status = err.statusCode || (err.name === "SequelizeUniqueConstraintError" ? 409 : 500);
    res.status(status).json(fail(err.message || "点赞失败，请稍后重试", status));
  }
});

// 取消点赞
router.delete("/:postId/like", async (req, res) => {
  try {
    const userId = getUserId(req);
    const postId = Number(req.params.postId);
    if (!Number.isInteger(postId) || postId <= 0) return res.status(400).json(fail("postId 无效", 400));

    const { PostLike } = require("../../models");
    const state = await getPostWithState(postId, userId);
    if (!state) return res.status(404).json(fail("帖子不存在", 404));

    if (state.liked) {
      await PostLike.destroy({ where: { postId, userId } });
      await state.post.decrement("likeCount", { by: 1, where: { likeCount: { [Op.gt]: 0 } } });
    }

    const updated = await Post.findByPk(postId, { attributes: ["likeCount"] });
    res.json(success({ postId: String(postId), liked: false, likeCount: updated.likeCount }, "取消点赞成功"));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message || "取消点赞失败，请稍后重试", status));
  }
});

module.exports = router;
