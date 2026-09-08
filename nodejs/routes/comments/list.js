/**
 * 评论列表
 * GET /:postId/comments — 分页查询评论（按时间倒序）
 */

const express = require("express");
const { Comment, Post } = require("../../models");
const { getUserId } = require("../../middleware/auth");
const { success, fail } = require("../../utils/response");
const { sanitizeComment, authorInclude, parsePositiveInteger } = require("./helpers");

const router = express.Router();

router.get("/:postId/comments", async (req, res) => {
  try {
    getUserId(req); // 需登录

    const postId = parsePositiveInteger(req.params.postId);
    if (!postId) return res.status(400).json(fail("postId 无效", 400));

    const post = await Post.findByPk(postId, { attributes: ["id"] });
    if (!post) return res.status(404).json(fail("帖子不存在", 404));

    const page = parsePositiveInteger(req.query.page, 1);
    const limit = parsePositiveInteger(req.query.limit, 20);
    if (!page || !limit) return res.status(400).json(fail("page 或 limit 无效", 400));

    const pageSize = Math.min(limit, 50);
    const { rows, count } = await Comment.findAndCountAll({
      where: { postId },
      include: [authorInclude],
      order: [
        ["createdAt", "DESC"],
        ["id", "DESC"],
      ],
      limit: pageSize,
      offset: (page - 1) * pageSize,
    });

    res.json(
      success({
        items: rows.map(sanitizeComment),
        hasMore: page * pageSize < count,
        page,
        total: count,
      })
    );
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

module.exports = router;
