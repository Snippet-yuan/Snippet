const express = require("express");
const router = express.Router();
const { Op } = require("sequelize");
const { User, Post } = require("../models");
const { success, fail } = require("../utils/response");

function sanitizePost(post) {
  return {
    id: String(post.id),
    ownerId: String(post.ownerId),
    ownerNickname: post.owner?.nickname || "",
    ownerAvatar: post.owner?.avatar || "",
    title: post.title,
    description: post.description,
    images: Array.isArray(post.images) ? post.images : [],
    counters: {
      likeCount: post.likeCount || 0,
      favoriteCount: post.favoriteCount || 0,
      commentCount: post.commentCount || 0,
      shareCount: post.shareCount || 0,
    },
    liked: false,
    favorited: false,
    createdAt: post.createdAt,
  };
}

// GET /api/v1/posts
// GET /api/v1/posts?lastId=15&limit=20
// 无限滚动：不传 lastId 返回最新的，传 lastId 返回比它 id 更小的
router.get("/", async (req, res) => {
  try {
    const limit = Math.min(50, Math.max(1, parseInt(req.query.limit) || 20));
    const lastId = parseInt(req.query.lastId) || null;

    const where = {};
    if (lastId) {
      where.id = { [Op.lt]: lastId };
    }

    const posts = await Post.findAll({
      where,
      order: [["id", "DESC"]],
      limit,
      include: [
        {
          model: User,
          as: "owner",
          attributes: ["id", "nickname", "avatar"],
        },
      ],
    });

    const hasMore = posts.length >= limit;

    res.json(
      success({
        items: posts.map((p) => sanitizePost(p)),
        hasMore,
      })
    );
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// GET /api/v1/posts/:postId
router.get("/:postId", async (req, res) => {
  try {
    const post = await Post.findByPk(parseInt(req.params.postId), {
      include: [
        {
          model: User,
          as: "owner",
          attributes: ["id", "nickname", "avatar"],
        },
      ],
    });
    if (!post) {
      return res.status(404).json(fail("帖子不存在", 404));
    }
    res.json(success(sanitizePost(post)));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

module.exports = router;
