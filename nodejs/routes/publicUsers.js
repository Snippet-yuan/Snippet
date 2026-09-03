const express = require("express");
const jwt = require("jsonwebtoken");
const { Op } = require("sequelize");
const { User, Post, PostLike, PostFavorite } = require("../models");
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

function sanitizePost(post, favorited = false) {
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
    favorited,
    createdAt: post.createdAt,
  };
}

const ownerInclude = {
  model: User,
  as: "owner",
  attributes: ["id", "nickname", "avatar"],
};

async function getPublicUser(userId) {
  return User.findByPk(userId, {
    attributes: ["id", "email", "nickname", "bio", "avatar", "background", "createdAt"],
  });
}

router.get("/:userId/posts", async (req, res) => {
  try {
    const currentUserId = getUserId(req);
    const userId = parsePositiveInteger(req.params.userId);
    if (!userId) return res.status(400).json(fail("userId 无效", 400));

    const user = await getPublicUser(userId);
    if (!user) return res.status(404).json(fail("用户不存在", 404));

    const page = parsePositiveInteger(req.query.page, 1);
    const limit = parsePositiveInteger(req.query.limit, 20);
    if (!page || !limit) return res.status(400).json(fail("page 或 limit 无效", 400));
    const pageSize = Math.min(limit, 50);

    const { rows, count } = await Post.findAndCountAll({
      where: { ownerId: userId },
      include: [ownerInclude],
      order: [["id", "DESC"]],
      limit: pageSize,
      offset: (page - 1) * pageSize,
    });

    const likedIds = await PostLike.findAll({
      where: { userId: currentUserId, postId: { [Op.in]: rows.map((post) => post.id) } },
      attributes: ["postId"],
    });
    const likedIdSet = new Set(likedIds.map(({ postId }) => Number(postId)));

    res.json(success({
      items: rows.map((post) => ({ ...sanitizePost(post), liked: likedIdSet.has(Number(post.id)) })),
      hasMore: page * pageSize < count,
      page,
      total: count,
    }));
  } catch (error) {
    const status = error.statusCode || 500;
    res.status(status).json(fail(error.message, status));
  }
});

router.get("/:userId/favorites", async (req, res) => {
  try {
    const currentUserId = getUserId(req);
    const userId = parsePositiveInteger(req.params.userId);
    if (!userId) return res.status(400).json(fail("userId 无效", 400));
    if (!await getPublicUser(userId)) return res.status(404).json(fail("用户不存在", 404));

    const page = parsePositiveInteger(req.query.page, 1);
    const limit = parsePositiveInteger(req.query.limit, 20);
    if (!page || !limit) return res.status(400).json(fail("page 或 limit 无效", 400));
    const pageSize = Math.min(limit, 50);

    const { rows, count } = await PostFavorite.findAndCountAll({
      where: { userId },
      include: [{ model: Post, as: "post", include: [ownerInclude] }],
      order: [["id", "DESC"]],
      limit: pageSize,
      offset: (page - 1) * pageSize,
    });

    const postIds = rows.filter((favorite) => favorite.post).map((favorite) => favorite.post.id);
    const likedRows = await PostLike.findAll({
      where: { userId: currentUserId, postId: { [Op.in]: postIds } },
      attributes: ["postId"],
    });
    const likedIdSet = new Set(likedRows.map(({ postId }) => Number(postId)));

    res.json(success({
      items: rows.filter((favorite) => favorite.post).map((favorite) => ({
        ...sanitizePost(favorite.post, true),
        liked: likedIdSet.has(Number(favorite.post.id)),
        favoritedAt: favorite.createdAt ? favorite.createdAt.toISOString() : "",
      })),
      hasMore: page * pageSize < count,
      page,
      total: count,
    }));
  } catch (error) {
    const status = error.statusCode || 500;
    res.status(status).json(fail(error.message, status));
  }
});

module.exports = router;
