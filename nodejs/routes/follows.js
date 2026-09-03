const express = require("express");
const jwt = require("jsonwebtoken");
const { Op } = require("sequelize");
const { Follow, User } = require("../models");
const { JWT_SECRET } = require("../utils/jwt");
const { success, fail } = require("../utils/response");

// 挂载于 /api/v1/users，完整路径如下：
//   POST   /api/v1/users/:userId/follow    关注用户
//   DELETE /api/v1/users/:userId/follow    取消关注
//   GET    /api/v1/users/me/following      我的关注列表
//   GET    /api/v1/users/me/followers      我的粉丝列表
//   GET    /api/v1/users/:userId/following 某人的关注列表
//   GET    /api/v1/users/:userId/followers 某人的粉丝列表
const router = express.Router();

// ============ 辅助函数 ============

function getUserId(req) {
  const authorization = req.get("Authorization") || "";
  const token = authorization.startsWith("Bearer ") ? authorization.slice(7) : "";
  if (!token) {
    const err = new Error("请先登录");
    err.statusCode = 401;
    throw err;
  }
  try {
    return jwt.verify(token, JWT_SECRET).id;
  } catch {
    const err = new Error("登录已过期，请重新登录");
    err.statusCode = 401;
    throw err;
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

/**
 * 查询当前用户是否关注了某些用户，返回 { [userId]: true } 形式的映射
 */
async function getFollowingMap(followedIds, currentUserId) {
  if (!followedIds.length) return {};
  const follows = await Follow.findAll({
    where: { followerId: currentUserId, followedId: { [Op.in]: followedIds } },
    attributes: ["followedId"],
  });
  const map = {};
  follows.forEach((follow) => {
    map[follow.followedId] = true;
  });
  return map;
}

async function getFollowCounts(targetId) {
  const [followingCount, followerCount] = await Promise.all([
    Follow.count({ where: { followerId: targetId } }),
    Follow.count({ where: { followedId: targetId } }),
  ]);
  return { followingCount, followerCount };
}

// ============ 关注 / 取消关注 ============

// POST /api/v1/users/:userId/follow — 关注用户
router.post("/:userId/follow", async (req, res) => {
  try {
    const currentUserId = getUserId(req);
    const targetId = Number(req.params.userId);
    if (!Number.isInteger(targetId) || targetId <= 0) {
      return res.status(400).json(fail("userId 无效", 400));
    }
    if (currentUserId === targetId) {
      return res.status(400).json(fail("不能关注自己", 400));
    }

    const user = await User.findByPk(targetId, { attributes: ["id"] });
    if (!user) return res.status(404).json(fail("用户不存在", 404));

    const existing = await Follow.findOne({
      where: { followerId: currentUserId, followedId: targetId },
    });
    if (existing) {
      return res.status(409).json(fail("已关注该用户", 409));
    }

    await Follow.create({ followerId: currentUserId, followedId: targetId });
    const counts = await getFollowCounts(targetId);

    res.status(201).json(success({
      followedUserId: String(targetId),
      following: true,
      ...counts,
    }, "关注成功"));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// DELETE /api/v1/users/:userId/follow — 取消关注（幂等）
router.delete("/:userId/follow", async (req, res) => {
  try {
    const currentUserId = getUserId(req);
    const targetId = Number(req.params.userId);
    if (!Number.isInteger(targetId) || targetId <= 0) {
      return res.status(400).json(fail("userId 无效", 400));
    }

    await Follow.destroy({
      where: { followerId: currentUserId, followedId: targetId },
    });
    const counts = await getFollowCounts(targetId);

    res.json(success({
      followedUserId: String(targetId),
      following: false,
      ...counts,
    }, "已取消关注"));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// ============ 关注列表 / 粉丝列表 ============

// GET /api/v1/users/me/following — 当前用户的关注列表
router.get("/me/following", async (req, res) => {
  try {
    const currentUserId = getUserId(req);
    const pagination = parsePagination(req.query);
    if (!pagination) return res.status(400).json(fail("page 或 limit 无效", 400));

    const { rows, count } = await Follow.findAndCountAll({
      where: { followerId: currentUserId },
      include: [{ model: User, as: "followed", attributes: ["id", "nickname", "avatar", "bio"] }],
      order: [["id", "DESC"]],
      limit: pagination.limit,
      offset: (pagination.page - 1) * pagination.limit,
    });

    res.json(success({
      items: rows.map((follow) => ({
        id: String(follow.followed.id),
        nickname: follow.followed.nickname,
        avatar: follow.followed.avatar || "",
        bio: follow.followed.bio || "",
        followedAt: follow.createdAt ? follow.createdAt.toISOString() : "",
      })),
      hasMore: pagination.page * pagination.limit < count,
      page: pagination.page,
      total: count,
    }));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// GET /api/v1/users/me/followers — 当前用户的粉丝列表
router.get("/me/followers", async (req, res) => {
  try {
    const currentUserId = getUserId(req);
    const pagination = parsePagination(req.query);
    if (!pagination) return res.status(400).json(fail("page 或 limit 无效", 400));

    const { rows, count } = await Follow.findAndCountAll({
      where: { followedId: currentUserId },
      include: [{ model: User, as: "follower", attributes: ["id", "nickname", "avatar", "bio"] }],
      order: [["id", "DESC"]],
      limit: pagination.limit,
      offset: (pagination.page - 1) * pagination.limit,
    });

    const followerIds = rows.map((follow) => follow.follower.id);
    const followingMap = await getFollowingMap(followerIds, currentUserId);

    res.json(success({
      items: rows.map((follow) => ({
        id: String(follow.follower.id),
        nickname: follow.follower.nickname,
        avatar: follow.follower.avatar || "",
        bio: follow.follower.bio || "",
        isFollowing: !!followingMap[follow.follower.id],
        followedAt: follow.createdAt ? follow.createdAt.toISOString() : "",
      })),
      hasMore: pagination.page * pagination.limit < count,
      page: pagination.page,
      total: count,
    }));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// GET /api/v1/users/:userId/following — 指定用户的关注列表
router.get("/:userId/following", async (req, res) => {
  try {
    const currentUserId = getUserId(req);
    const targetId = Number(req.params.userId);
    if (!Number.isInteger(targetId) || targetId <= 0) {
      return res.status(400).json(fail("userId 无效", 400));
    }

    const pagination = parsePagination(req.query);
    if (!pagination) return res.status(400).json(fail("page 或 limit 无效", 400));

    const { rows, count } = await Follow.findAndCountAll({
      where: { followerId: targetId },
      include: [{ model: User, as: "followed", attributes: ["id", "nickname", "avatar", "bio"] }],
      order: [["id", "DESC"]],
      limit: pagination.limit,
      offset: (pagination.page - 1) * pagination.limit,
    });

    const followedIds = rows.map((follow) => follow.followed.id);
    const followingMap = await getFollowingMap(followedIds, currentUserId);

    res.json(success({
      items: rows.map((follow) => ({
        id: String(follow.followed.id),
        nickname: follow.followed.nickname,
        avatar: follow.followed.avatar || "",
        bio: follow.followed.bio || "",
        isFollowing: !!followingMap[follow.followed.id],
      })),
      hasMore: pagination.page * pagination.limit < count,
      page: pagination.page,
      total: count,
    }));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// GET /api/v1/users/:userId/followers — 指定用户的粉丝列表
router.get("/:userId/followers", async (req, res) => {
  try {
    const currentUserId = getUserId(req);
    const targetId = Number(req.params.userId);
    if (!Number.isInteger(targetId) || targetId <= 0) {
      return res.status(400).json(fail("userId 无效", 400));
    }

    const pagination = parsePagination(req.query);
    if (!pagination) return res.status(400).json(fail("page 或 limit 无效", 400));

    const { rows, count } = await Follow.findAndCountAll({
      where: { followedId: targetId },
      include: [{ model: User, as: "follower", attributes: ["id", "nickname", "avatar", "bio"] }],
      order: [["id", "DESC"]],
      limit: pagination.limit,
      offset: (pagination.page - 1) * pagination.limit,
    });

    const followerIds = rows.map((follow) => follow.follower.id);
    const followingMap = await getFollowingMap(followerIds, currentUserId);

    res.json(success({
      items: rows.map((follow) => ({
        id: String(follow.follower.id),
        nickname: follow.follower.nickname,
        avatar: follow.follower.avatar || "",
        bio: follow.follower.bio || "",
        isFollowing: !!followingMap[follow.follower.id],
      })),
      hasMore: pagination.page * pagination.limit < count,
      page: pagination.page,
      total: count,
    }));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// GET /api/v1/users/:userId/follow-status — 查询关注状态与关注数（轻量，适合个人主页展示）
router.get("/:userId/follow-status", async (req, res) => {
  try {
    const currentUserId = getUserId(req);
    const targetId = Number(req.params.userId);
    if (!Number.isInteger(targetId) || targetId <= 0) {
      return res.status(400).json(fail("userId 无效", 400));
    }

    const user = await User.findByPk(targetId, { attributes: ["id"] });
    if (!user) return res.status(404).json(fail("用户不存在", 404));

    const following = await Follow.findOne({
      where: { followerId: currentUserId, followedId: targetId },
      attributes: ["id"],
    });

    const [followingCount, followerCount] = await Promise.all([
      Follow.count({ where: { followerId: targetId } }),
      Follow.count({ where: { followedId: targetId } }),
    ]);

    res.json(success({
      userId: String(targetId),
      isSelf: currentUserId === targetId,
      isFollowing: !!following,
      followingCount,
      followerCount,
    }));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

module.exports = router;