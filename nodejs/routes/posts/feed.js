/**
 * 帖子流 - 信息流查询
 * GET /              支持 lastId 游标分页
 * GET /:postId       单篇详情（需登录，返回 liked/favorited）
 */

const express = require("express");
const { Op } = require("sequelize");
const { User, Post, PostLike, PostFavorite } = require("../../models");
const { getUserId } = require("../../middleware/auth");
const { success, fail } = require("../../utils/response");
const { sanitizePost } = require("../../utils/sanitize");
const { getPostWithState } = require("./helpers");

const router = express.Router();

// ---------------------------------------------------------------------------
// GET / — 信息流（游标分页）
// ---------------------------------------------------------------------------
router.get("/", async (req, res) => {
  try {
    const limit = Math.min(50, Math.max(1, parseInt(req.query.limit, 10) || 20));
    const lastId = parseInt(req.query.lastId, 10) || null;

    const where = {};
    if (lastId) where.id = { [Op.lt]: lastId };

    const posts = await Post.findAll({
      where,
      order: [["id", "DESC"]],
      limit,
      include: [{ model: User, as: "owner", attributes: ["id", "nickname", "avatar"] }],
    });

    // 可选登录：携带有效 token 时返回点赞/收藏状态
    let currentUserId = null;
    try {
      currentUserId = getUserId(req);
    } catch {
      currentUserId = null;
    }

    let likedIds = new Set();
    let favoritedIds = new Set();
    if (currentUserId && posts.length) {
      const postIds = posts.map((p) => p.id);
      const [likes, favorites] = await Promise.all([
        PostLike.findAll({ where: { userId: currentUserId, postId: { [Op.in]: postIds } }, attributes: ["postId"] }),
        PostFavorite.findAll({ where: { userId: currentUserId, postId: { [Op.in]: postIds } }, attributes: ["postId"] }),
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

// ---------------------------------------------------------------------------
// GET /:postId — 单篇详情
// ---------------------------------------------------------------------------
router.get("/:postId", async (req, res) => {
  try {
    const postId = Number(req.params.postId);
    if (!Number.isInteger(postId) || postId <= 0) return res.status(400).json(fail("postId 无效", 400));

    const userId = getUserId(req);
    const state = await getPostWithState(postId, userId);
    if (!state) return res.status(404).json(fail("帖子不存在", 404));

    res.json(success({ ...sanitizePost(state.post), liked: state.liked, favorited: state.favorited }));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

module.exports = router;
