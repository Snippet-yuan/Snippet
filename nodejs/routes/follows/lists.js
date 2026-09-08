/**
 * 关注列表 / 粉丝列表
 * GET /me/following            当前用户的关注列表
 * GET /me/followers            当前用户的粉丝列表
 * GET /:userId/following       指定用户的关注列表
 * GET /:userId/followers       指定用户的粉丝列表
 * GET /:userId/follow-status   关注状态与计数（轻量）
 */

const express = require("express");
const { User, Follow } = require("../../models");
const { getUserId } = require("../../middleware/auth");
const { success, fail } = require("../../utils/response");
const { parsePagination } = require("../../utils/pagination");
const { getFollowingMap } = require("./helpers");

const router = express.Router();

// 当前用户的关注列表
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

    res.json(
      success({
        items: rows.map((f) => ({
          id: String(f.followed.id),
          nickname: f.followed.nickname,
          avatar: f.followed.avatar || "",
          bio: f.followed.bio || "",
          followedAt: f.createdAt ? f.createdAt.toISOString() : "",
        })),
        hasMore: pagination.page * pagination.limit < count,
        page: pagination.page,
        total: count,
      })
    );
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// 当前用户的粉丝列表
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

    const followerIds = rows.map((f) => f.follower.id);
    const followingMap = await getFollowingMap(followerIds, currentUserId);

    res.json(
      success({
        items: rows.map((f) => ({
          id: String(f.follower.id),
          nickname: f.follower.nickname,
          avatar: f.follower.avatar || "",
          bio: f.follower.bio || "",
          isFollowing: !!followingMap[f.follower.id],
          followedAt: f.createdAt ? f.createdAt.toISOString() : "",
        })),
        hasMore: pagination.page * pagination.limit < count,
        page: pagination.page,
        total: count,
      })
    );
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// 指定用户的关注列表
router.get("/:userId/following", async (req, res) => {
  try {
    const currentUserId = getUserId(req);
    const targetId = Number(req.params.userId);
    if (!Number.isInteger(targetId) || targetId <= 0) return res.status(400).json(fail("userId 无效", 400));

    const pagination = parsePagination(req.query);
    if (!pagination) return res.status(400).json(fail("page 或 limit 无效", 400));

    const { rows, count } = await Follow.findAndCountAll({
      where: { followerId: targetId },
      include: [{ model: User, as: "followed", attributes: ["id", "nickname", "avatar", "bio"] }],
      order: [["id", "DESC"]],
      limit: pagination.limit,
      offset: (pagination.page - 1) * pagination.limit,
    });

    const followedIds = rows.map((f) => f.followed.id);
    const followingMap = await getFollowingMap(followedIds, currentUserId);

    res.json(
      success({
        items: rows.map((f) => ({
          id: String(f.followed.id),
          nickname: f.followed.nickname,
          avatar: f.followed.avatar || "",
          bio: f.followed.bio || "",
          isFollowing: !!followingMap[f.followed.id],
        })),
        hasMore: pagination.page * pagination.limit < count,
        page: pagination.page,
        total: count,
      })
    );
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// 指定用户的粉丝列表
router.get("/:userId/followers", async (req, res) => {
  try {
    const currentUserId = getUserId(req);
    const targetId = Number(req.params.userId);
    if (!Number.isInteger(targetId) || targetId <= 0) return res.status(400).json(fail("userId 无效", 400));

    const pagination = parsePagination(req.query);
    if (!pagination) return res.status(400).json(fail("page 或 limit 无效", 400));

    const { rows, count } = await Follow.findAndCountAll({
      where: { followedId: targetId },
      include: [{ model: User, as: "follower", attributes: ["id", "nickname", "avatar", "bio"] }],
      order: [["id", "DESC"]],
      limit: pagination.limit,
      offset: (pagination.page - 1) * pagination.limit,
    });

    const followerIds = rows.map((f) => f.follower.id);
    const followingMap = await getFollowingMap(followerIds, currentUserId);

    res.json(
      success({
        items: rows.map((f) => ({
          id: String(f.follower.id),
          nickname: f.follower.nickname,
          avatar: f.follower.avatar || "",
          bio: f.follower.bio || "",
          isFollowing: !!followingMap[f.follower.id],
        })),
        hasMore: pagination.page * pagination.limit < count,
        page: pagination.page,
        total: count,
      })
    );
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// 关注状态与计数（适合个人主页展示）
router.get("/:userId/follow-status", async (req, res) => {
  try {
    const currentUserId = getUserId(req);
    const targetId = Number(req.params.userId);
    if (!Number.isInteger(targetId) || targetId <= 0) return res.status(400).json(fail("userId 无效", 400));

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

    res.json(
      success({
        userId: String(targetId),
        isSelf: currentUserId === targetId,
        isFollowing: !!following,
        followingCount,
        followerCount,
      })
    );
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

module.exports = router;
