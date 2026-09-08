/**
 * 用户主页资料（对外展示）
 * GET /:userId            个人资料 + 关系 / 统计
 * GET /:userId/posts      某人的帖子列表
 * GET /:userId/favorites  某人的收藏列表
 *
 * 挂载于 /api/v1/users
 */

const express = require("express");
const { Op } = require("sequelize");
const { User, Post, PostLike, PostFavorite, Friendship, FriendRequest, Follow } = require("../../models");
const { getUserId } = require("../../middleware/auth");
const { success, fail } = require("../../utils/response");
const { sanitizeProfile, sanitizePost } = require("../../utils/sanitize");
const { parsePagination } = require("../../utils/pagination");

const router = express.Router();

// ---------------------------------------------------------------------------
// 常量
// ---------------------------------------------------------------------------

const OWNER_INCLUDE = {
  model: User,
  as: "owner",
  attributes: ["id", "nickname", "avatar"],
};

// ---------------------------------------------------------------------------
// GET /:userId — 个人资料与关系/统计
// ---------------------------------------------------------------------------
router.get("/:userId", async (req, res) => {
  try {
    const currentUserId = getUserId(req);
    const targetId = Number(req.params.userId);
    if (!Number.isInteger(targetId) || targetId <= 0) return res.status(400).json(fail("userId 无效", 400));

    const user = await User.findByPk(targetId, {
      attributes: ["id", "nickname", "bio", "avatar", "background", "createdAt"],
    });
    if (!user) return res.status(404).json(fail("用户不存在", 404));

    const [postCount, favoriteCount, friendship, friendRequest, followingRecord, followerRecord, followingCount, followerCount] =
      await Promise.all([
        Post.count({ where: { ownerId: targetId } }),
        PostFavorite.count({ where: { userId: targetId } }),
        Friendship.findOne({
          where: { [Op.or]: [{ userId: currentUserId, friendUserId: targetId }, { userId: targetId, friendUserId: currentUserId }] },
          attributes: ["id"],
        }),
        FriendRequest.findOne({
          where: {
            status: "PENDING",
            [Op.or]: [
              { senderId: currentUserId, receiverId: targetId },
              { senderId: targetId, receiverId: currentUserId },
            ],
          },
          attributes: ["id", "senderId", "receiverId", "status"],
          order: [["id", "DESC"]],
        }),
        Follow.findOne({ where: { followerId: currentUserId, followedId: targetId }, attributes: ["id"] }),
        Follow.findOne({ where: { followerId: targetId, followedId: currentUserId }, attributes: ["id"] }),
        Follow.count({ where: { followerId: targetId } }),
        Follow.count({ where: { followedId: targetId } }),
      ]);

    // 好友申请状态：NONE / PENDING_SENT / PENDING_RECEIVED
    let friendRequestStatus = "NONE";
    if (friendRequest) {
      friendRequestStatus = Number(friendRequest.senderId) === currentUserId ? "PENDING_SENT" : "PENDING_RECEIVED";
    }

    res.json(
      success({
        ...sanitizeProfile(user),
        isSelf: currentUserId === targetId,
        isFriend: Boolean(friendship),
        friendRequestStatus,
        friendRequestId: friendRequest ? String(friendRequest.id) : null,
        isFollowing: Boolean(followingRecord),
        followedBy: Boolean(followerRecord),
        postCount,
        favoriteCount,
        followingCount,
        followerCount,
      })
    );
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// ---------------------------------------------------------------------------
// GET /:userId/posts
// ---------------------------------------------------------------------------
router.get("/:userId/posts", async (req, res) => {
  try {
    const currentUserId = getUserId(req);
    const targetId = Number(req.params.userId);
    if (!Number.isInteger(targetId) || targetId <= 0) return res.status(400).json(fail("userId 无效", 400));

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

    const likedIds = rows.length
      ? (
          await PostLike.findAll({
            where: { postId: rows.map((p) => p.id), userId: currentUserId },
            attributes: ["postId"],
          })
        ).map((item) => String(item.postId))
      : [];

    res.json(
      success({
        items: rows.map((post) => sanitizePost(post, { liked: likedIds.includes(String(post.id)) })),
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

// ---------------------------------------------------------------------------
// GET /:userId/favorites
// ---------------------------------------------------------------------------
router.get("/:userId/favorites", async (req, res) => {
  try {
    const targetId = Number(req.params.userId);
    if (!Number.isInteger(targetId) || targetId <= 0) return res.status(400).json(fail("userId 无效", 400));

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

    const items = rows
      .filter((fav) => fav.post)
      .map((fav) => ({
        ...sanitizePost(fav.post, { favorited: true }),
        favoritedAt: fav.createdAt ? fav.createdAt.toISOString() : "",
      }));

    res.json(
      success({
        items,
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

module.exports = router;
