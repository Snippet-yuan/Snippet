/**
 * 发布评论
 * POST /:postId/comments — 事务内创建评论并递增帖子计数
 */

const express = require("express");
const { Comment, Post } = require("../../models");
const { getUserId } = require("../../middleware/auth");
const { success, fail } = require("../../utils/response");
const { sanitizeComment, authorInclude, parsePositiveInteger } = require("./helpers");

const router = express.Router();

router.post("/:postId/comments", async (req, res) => {
  try {
    const authorId = getUserId(req);
    const postId = parsePositiveInteger(req.params.postId);
    if (!postId) return res.status(400).json(fail("postId 无效", 400));

    const content = typeof req.body?.content === "string" ? req.body.content.trim() : "";
    if (!content) return res.status(400).json(fail("评论内容不能为空", 400));
    if (content.length > 500) return res.status(400).json(fail("评论内容不能超过 500 个字符", 400));

    // 事务：创建评论 + 递增计数，保证一致性
    const comment = await Comment.sequelize.transaction(async (transaction) => {
      const post = await Post.findByPk(postId, { transaction, attributes: ["id"] });
      if (!post) {
        const err = new Error("帖子不存在");
        err.statusCode = 404;
        throw err;
      }
      const created = await Comment.create({ postId, authorId, content }, { transaction });
      await Post.increment("commentCount", { where: { id: postId }, transaction });
      return created;
    });

    const result = await Comment.findByPk(comment.id, { include: [authorInclude] });
    res.status(201).json(success(sanitizeComment(result), "评论成功"));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

module.exports = router;
