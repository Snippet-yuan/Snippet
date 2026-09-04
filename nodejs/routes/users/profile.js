const express = require("express");
const jwt = require("jsonwebtoken");
const { Op } = require("sequelize");
const { User, Post, PostLike, PostFavorite, Friendship, FriendRequest, Follow } = require("../../models");
const { JWT_SECRET } = require("../../utils/jwt");
const { success, fail } = require("../../utils/response");

const router = express.Router();

const POST_ATTRIBUTES = ["id", "ownerId", "title", "description", "images", "likeCount", "favoriteCount", "commentCount", "shareCount", "createdAt"];

const OWNER_INCLUDE = {
  model: User,
  as: "owner",
  attributes: ["id", "nickname", "avatar"],
};

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

function parsePagination(query) {
  const page = Number(query.page ?? 1);
  const limit = Number(query.limit ?? 20);
  if (!Number.isInteger(page) || page < 1 || !Number.isInteger(limit) || limit < 1) {
    return null;
  }
  return { page, limit: Math.min(limit, 50) };
}

function sanitizeProfile(user) {
  return {
    id: String(user.id),
    nickname: user.nickname,
    bio: user.bio || "",
    avatar: user.avatar || "",
    background: user.background || "",
    createdAt: user.createdAt ? user.createdAt.toISOString() : "",
  };
}

function sanitizePost(post, state = {}) {
  return {
    id: String(post.id),
    ownerId: String(post.ownerId),
    ownerNickname: post.owner?.nickname || "",
    ownerAvatar: post.owner?.avatar || "",
    title: post.title,
    description: post.description || "",
    images: Array.isArray(post.images) ? post.images : [],
    counters: {
      likeCount: post.likeCount || 0,
      favoriteCount: post.favoriteCount || 0,
      commentCount: post.commentCount || 0,
      shareCount: post.shareCount || 0,
    },
    liked: Boolean(state.liked),
    favorited: Boolean(state.favorited),
    createdAt: post.createdAt,
  };
}

// GET /api/v1/users/:userId
router.get("/:userId", async (req, res) => {
  try {
    const userId = getUserId(req);
    const targetId = Number(req.params.userId);
    if (!Number.isInteger(targetId) || targetId <= 0) {
      return res.status(400).json(fail("userId 无效", 400));
    }

    const user = await User.findByPk(targetId, {
      attributes: ["id", "nickname", "bio", "avatar", "background", "createdAt"],
    });
    if (!user) return res.status(404).json(fail("用户不存在", 404));

    const [postCount, favoriteCount, friendship, friendRequest, followingRecord, followerRecord, followingCount, followerCount] = await Promise.all([
      Post.count({ where: { ownerId: targetId } }),
      PostFavorite.count({ where: { userId: targetId } }),
      Friendship.findOne({
        where: {
          [Op.or]: [
            { userId, friendUserId: targetId },
            { userId: targetId, friendUserId: userId },
          ],
        },
        attributes: ["id"],
      }),
      FriendRequest.findOne({
        where: {
          status: "PENDING",
          [Op.or]: [
            { senderId: userId, receiverId: targetId },
            { senderId: targetId, receiverId: userId },
          ],
        },
        attributes: ["id", "senderId", "receiverId", "status"],
        order: [["id", "DESC"]],
      }),
      Follow.findOne({ where: { followerId: userId, followedId: targetId }, attributes: ["id"] }),
      Follow.findOne({ where: { followerId: targetId, followedId: userId }, attributes: ["id"] }),
      Follow.count({ where: { followerId: targetId } }),
      Follow.count({ where: { followedId: targetId } }),
    ]);

    let friendRequestStatus = "NONE";
    if (friendRequest) {
      friendRequestStatus = Number(friendRequest.senderId) === userId ? "PENDING_SENT" : "PENDING_RECEIVED";
    }

    res.json(success({
      ...sanitizeProfile(user),
      isSelf: userId === targetId,
      isFriend: Boolean(friendship),
      friendRequestStatus,
      friendRequestId: friendRequest ? String(friendRequest.id) : null,
      isFollowing: Boolean(followingRecord),
      followedBy: Boolean(followerRecord),
      postCount,
      favoriteCount,
      followingCount,
      followerCount,
    }));
  } catch (error) {
    const status = error.statusCode || 500;
    res.status(status).json(fail(error.message, status));
  }
});

// GET /api/v1/users/:userId/posts
router.get("/:userId/posts", async (req, res) => {
  try {
    const userId = getUserId(req);
    const targetId = Number(req.params.userId);
    if (!Number.isInteger(targetId) || targetId <= 0) {
      return res.status(400).json(fail("userId 无效", 400));
    }

    const user = await User.findByPk(targetId, { attributes: ["id"] });
    if (!user) return res.status(404).json(fail("用户不存在", 404));

    const pagination = parsePagination(req.query);
    if (!pagination) return res.status(400).json(fail("page 或 limit 无效", 400));

    const { rows, count } = await Post.findAndCountAll({
      where: { ownerId: targetId },
      include: [OWNER_INCLUDE],
      order: [["id", "DESC"]],
      limit: pagination.limit,
      offset: (pagination.page - 1) * pagination.limit,
    });

    const likedPostIds = rows.length
      ? (await PostLike.findAll({ where: { postId: rows.map((post) => post.id), userId }, attributes: ["postId"] })).map((item) => String(item.postId))
      : [];

    res.json(success({
      items: rows.map((post) => sanitizePost(post, { liked: likedPostIds.includes(String(post.id)) })),
      hasMore: pagination.page * pagination.limit < count,
      page: pagination.page,
      total: count,
    }));
  } catch (error) {
    const status = error.statusCode || 500;
    res.status(status).json(fail(error.message, status));
  }
});

// GET /api/v1/users/:userId/favorites
router.get("/:userId/favorites", async (req, res) => {
  try {
    const userId = getUserId(req);
    const targetId = Number(req.params.userId);
    if (!Number.isInteger(targetId) || targetId <= 0) {
      return res.status(400).json(fail("userId 无效", 400));
    }

    const user = await User.findByPk(targetId, { attributes: ["id"] });
    if (!user) return res.status(404).json(fail("用户不存在", 404));

    const pagination = parsePagination(req.query);
    if (!pagination) return res.status(400).json(fail("page 或 limit 无效", 400));

    const { rows, count } = await PostFavorite.findAndCountAll({
      where: { userId: targetId },
      include: [{ model: Post, as: "post", include: [OWNER_INCLUDE] }],
      order: [["id", "DESC"]],
      limit: pagination.limit,
      offset: (pagination.page - 1) * pagination.limit,
    });

    const items = rows.filter((favorite) => favorite.post).map((favorite) => ({
      ...sanitizePost(favorite.post, { favorited: true }),
      favoritedAt: favorite.createdAt ? favorite.createdAt.toISOString() : "",
    }));

    res.json(success({
      items,
      hasMore: pagination.page * pagination.limit < count,
      page: pagination.page,
      total: count,
    }));
  } catch (error) {
    const status = error.statusCode || 500;
    res.status(status).json(fail(error.message, status));
  }
});

module.exports = router;
