const express = require("express");
const jwt = require("jsonwebtoken");
const { Op } = require("sequelize");
const { Comment, Post, User } = require("../models");
const { JWT_SECRET } = require("../utils/jwt");
const { success, fail } = require("../utils/response");

const router = express.Router();

function getUserId(req) {
  const authorization = req.get("Authorization") || "";
  const token = authorization.startsWith("Bearer ") ? authorization.slice(7) : "";
  if (!token) {
    const error = new Error("请先登录");
    error.statusCode = 401;
    throw error;
  }

  try {
    return Number(jwt.verify(token, JWT_SECRET).id);
  } catch {
    const error = new Error("登录已过期，请重新登录");
    error.statusCode = 401;
    throw error;
  }
}

function parsePositiveInteger(value, fallback) {
  const parsed = Number(value ?? fallback);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
}

function sanitizeComment(comment) {
  return {
    id: String(comment.id),
    postId: String(comment.postId),
    authorId: String(comment.authorId),
    authorNickname: comment.author?.nickname || "",
    authorAvatar: comment.author?.avatar || "",
    content: comment.content,
    createdAt: comment.createdAt ? comment.createdAt.toISOString() : "",
  };
}

const authorInclude = {
  model: User,
  as: "author",
  attributes: ["id", "nickname", "avatar"],
};

router.get("/:postId/comments", async (req, res) => {
  try {
    getUserId(req);
    const postId = parsePositiveInteger(req.params.postId);
    if (!postId) return res.status(400).json(fail("postId 无效", 400));

    const post = await Post.findByPk(postId, { attributes: ["id"] });
    if (!post) return res.status(404).json(fail("帖子不存在", 404));

    const page = parsePositiveInteger(req.query.page, 1);
    const limit = parsePositiveInteger(req.query.limit, 20);
    if (!page || !limit) return res.status(400).json(fail("page 或 limit 无效", 400));

    const { rows, count } = await Comment.findAndCountAll({
      where: { postId },
      include: [authorInclude],
      order: [["createdAt", "DESC"], ["id", "DESC"]],
      limit: Math.min(limit, 50),
      offset: (page - 1) * Math.min(limit, 50),
    });

    res.json(success({
      items: rows.map(sanitizeComment),
      hasMore: page * Math.min(limit, 50) < count,
      page,
      total: count,
    }));
  } catch (error) {
    const status = error.statusCode || 500;
    res.status(status).json(fail(error.message, status));
  }
});

router.post("/:postId/comments", async (req, res) => {
  try {
    const authorId = getUserId(req);
    const postId = parsePositiveInteger(req.params.postId);
    if (!postId) return res.status(400).json(fail("postId 无效", 400));

    const content = typeof req.body?.content === "string" ? req.body.content.trim() : "";
    if (!content) return res.status(400).json(fail("评论内容不能为空", 400));
    if (content.length > 500) return res.status(400).json(fail("评论内容不能超过 500 个字符", 400));

    const comment = await Comment.sequelize.transaction(async (transaction) => {
      const post = await Post.findByPk(postId, { transaction, attributes: ["id"] });
      if (!post) {
        const error = new Error("帖子不存在");
        error.statusCode = 404;
        throw error;
      }

      const createdComment = await Comment.create({ postId, authorId, content }, { transaction });
      await Post.increment("commentCount", { where: { id: postId }, transaction });
      return createdComment;
    });

    const result = await Comment.findByPk(comment.id, { include: [authorInclude] });
    res.status(201).json(success(sanitizeComment(result), "评论成功"));
  } catch (error) {
    const status = error.statusCode || 500;
    res.status(status).json(fail(error.message, status));
  }
});

module.exports = router;
