const express = require("express");
const router = express.Router();
const { Op } = require("sequelize");
const { User, Post, PostLike, PostFavorite } = require("../models");
const jwt = require("jsonwebtoken");
const { JWT_SECRET } = require("../utils/jwt");
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

async function getPostWithState(postId, userId) {
  const post = await Post.findByPk(postId, {
    include: [{ model: User, as: "owner", attributes: ["id", "nickname", "avatar"] }],
  });
  if (!post) return null;

  const [liked, favorited] = await Promise.all([
    PostLike.findOne({ where: { postId, userId } }),
    PostFavorite.findOne({ where: { postId, userId } }),
  ]);
  return { post, liked: Boolean(liked), favorited: Boolean(favorited) };
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

    // 可选登录：携带有效 token 时，为当前用户查询每帖的点赞/收藏状态
    let userId = null;
    try {
      userId = getUserId(req);
    } catch {
      userId = null;
    }

    let likedIds = new Set();
    let favoritedIds = new Set();
    if (userId && posts.length) {
      const postIds = posts.map((p) => p.id);
      const [likes, favorites] = await Promise.all([
        PostLike.findAll({
          where: { userId, postId: { [Op.in]: postIds } },
          attributes: ["postId"],
        }),
        PostFavorite.findAll({
          where: { userId, postId: { [Op.in]: postIds } },
          attributes: ["postId"],
        }),
      ]);
      likedIds = new Set(likes.map((l) => String(l.postId)));
      favoritedIds = new Set(favorites.map((f) => String(f.postId)));
    }

    const hasMore = posts.length >= limit;

    res.json(
      success({
        items: posts.map((p) => ({
          ...sanitizePost(p),
          liked: likedIds.has(String(p.id)),
          favorited: favoritedIds.has(String(p.id)),
        })),
        hasMore,
      })
    );
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// POST /api/v1/posts/:postId/like
router.post("/:postId/like", async (req, res) => {
  try {
    const userId = getUserId(req);
    const postId = Number(req.params.postId);
    if (!Number.isInteger(postId) || postId <= 0) {
      return res.status(400).json(fail("postId 无效", 400));
    }

    const state = await getPostWithState(postId, userId);
    if (!state) return res.status(404).json(fail("帖子不存在", 404));
    if (!state.liked) {
      await PostLike.create({ postId, userId });
      await state.post.increment("likeCount");
    }

    const updatedPost = await Post.findByPk(postId, { attributes: ["likeCount"] });
    res.json(success({ postId: String(postId), liked: true, likeCount: updatedPost.likeCount }, "点赞成功"));
  } catch (err) {
    const status = err.statusCode || (err.name === "SequelizeUniqueConstraintError" ? 409 : 500);
    res.status(status).json(fail(err.message || "点赞失败，请稍后重试", status));
  }
});

// DELETE /api/v1/posts/:postId/like
router.delete("/:postId/like", async (req, res) => {
  try {
    const userId = getUserId(req);
    const postId = Number(req.params.postId);
    if (!Number.isInteger(postId) || postId <= 0) {
      return res.status(400).json(fail("postId 无效", 400));
    }

    const state = await getPostWithState(postId, userId);
    if (!state) return res.status(404).json(fail("帖子不存在", 404));
    if (state.liked) {
      await PostLike.destroy({ where: { postId, userId } });
      await state.post.decrement("likeCount", { by: 1, where: { likeCount: { [Op.gt]: 0 } } });
    }

    const updatedPost = await Post.findByPk(postId, { attributes: ["likeCount"] });
    res.json(success({ postId: String(postId), liked: false, likeCount: updatedPost.likeCount }, "取消点赞成功"));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message || "取消点赞失败，请稍后重试", status));
  }
});

// POST /api/v1/posts/:postId/favorite
router.post("/:postId/favorite", async (req, res) => {
  try {
    const userId = getUserId(req);
    const postId = Number(req.params.postId);
    if (!Number.isInteger(postId) || postId <= 0) {
      return res.status(400).json(fail("postId 无效", 400));
    }

    const state = await getPostWithState(postId, userId);
    if (!state) return res.status(404).json(fail("帖子不存在", 404));
    if (!state.favorited) {
      await PostFavorite.create({ postId, userId });
      await state.post.increment("favoriteCount");
    }

    const updatedPost = await Post.findByPk(postId, { attributes: ["favoriteCount"] });
    res.json(success({ postId: String(postId), favorited: true, favoriteCount: updatedPost.favoriteCount }, "收藏成功"));
  } catch (err) {
    const status = err.statusCode || (err.name === "SequelizeUniqueConstraintError" ? 409 : 500);
    res.status(status).json(fail(err.message || "收藏失败，请稍后重试", status));
  }
});

// DELETE /api/v1/posts/:postId/favorite
router.delete("/:postId/favorite", async (req, res) => {
  try {
    const userId = getUserId(req);
    const postId = Number(req.params.postId);
    if (!Number.isInteger(postId) || postId <= 0) {
      return res.status(400).json(fail("postId 无效", 400));
    }

    const state = await getPostWithState(postId, userId);
    if (!state) return res.status(404).json(fail("帖子不存在", 404));
    if (state.favorited) {
      await PostFavorite.destroy({ where: { postId, userId } });
      await state.post.decrement("favoriteCount", { by: 1, where: { favoriteCount: { [Op.gt]: 0 } } });
    }

    const updatedPost = await Post.findByPk(postId, { attributes: ["favoriteCount"] });
    res.json(success({ postId: String(postId), favorited: false, favoriteCount: updatedPost.favoriteCount }, "取消收藏成功"));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message || "取消收藏失败，请稍后重试", status));
  }
});

// GET /api/v1/posts/:postId
router.get("/:postId", async (req, res) => {
  try {
    const postId = Number(req.params.postId);
    if (!Number.isInteger(postId) || postId <= 0) {
      return res.status(400).json(fail("postId 无效", 400));
    }

    const userId = getUserId(req);
    const state = await getPostWithState(postId, userId);
    if (!state) {
      return res.status(404).json(fail("帖子不存在", 404));
    }

    res.json(success({
      ...sanitizePost(state.post),
      liked: state.liked,
      favorited: state.favorited,
    }));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

module.exports = router;
