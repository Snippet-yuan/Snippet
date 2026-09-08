/**
 * 他人公开数据
 * GET /:userId/posts     某用户的帖子列表
 * GET /:userId/favorites 某用户的收藏列表
 *
 * 挂载于 /api/v1/public-users 或通过 users/profile 暴露，此处保留对 /api/v1 之外的公开访问能力。
 * 当前 app.js 未直接挂载本路由，保留以兼容历史用法。
 */

const express = require("express");
const { Op } = require("sequelize");
const { User, Post, PostLike, PostFavorite } = require("../models");
const { getUserId } = require("../middleware/auth");
const { success, fail } = require("../utils/response");
const { sanitizePost } = require("../utils/sanitize");
const { parsePositiveInteger } = require("../utils/pagination");

const router = express.Router();

/** 作者关联 */
const ownerInclude = {
  model: User,
  as: "owner",
  attributes: ["id", "nickname", "avatar"],
};

/** 查询公开用户信息，存在性校验用 */
async function getPublicUser(userId) {
  return User.findByPk(userId, {
    attributes: ["id", "email", "nickname", "bio", "avatar", "background", "createdAt"],
  });
}

// 某用户的帖子列表
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

    const likedRows = await PostLike.findAll({
      where: { userId: currentUserId, postId: { [Op.in]: rows.map((p) => p.id) } },
      attributes: ["postId"],
    });
    const likedSet = new Set(likedRows.map(({ postId }) => Number(postId)));

    res.json(
      success({
        items: rows.map((post) => ({ ...sanitizePost(post), liked: likedSet.has(Number(post.id)) })),
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

// 某用户的收藏列表
router.get("/:userId/favorites", async (req, res) => {
  try {
    const currentUserId = getUserId(req);
    const userId = parsePositiveInteger(req.params.userId);
    if (!userId) return res.status(400).json(fail("userId 无效", 400));
    if (!(await getPublicUser(userId))) return res.status(404).json(fail("用户不存在", 404));

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

    const postIds = rows.filter((fav) => fav.post).map((fav) => fav.post.id);
    const likedRows = await PostLike.findAll({
      where: { userId: currentUserId, postId: { [Op.in]: postIds } },
      attributes: ["postId"],
    });
    const likedSet = new Set(likedRows.map(({ postId }) => Number(postId)));

    res.json(
      success({
        items: rows
          .filter((fav) => fav.post)
          .map((fav) => ({
            ...sanitizePost(fav.post, { favorited: true }),
            liked: likedSet.has(Number(fav.post.id)),
            favoritedAt: fav.createdAt ? fav.createdAt.toISOString() : "",
          })),
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
