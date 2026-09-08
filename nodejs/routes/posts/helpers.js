/**
 * 帖子模块 - 共享辅助函数
 * 供 feed / likes / favorites / detail 共用，保持与原 posts.js 行为一致。
 */

const { User, Post, PostLike, PostFavorite } = require("../../models");

/**
 * 查询单篇帖子并附带当前用户的点赞/收藏状态。
 * @param {number} postId
 * @param {number|string} userId
 * @returns {Promise<{ post: import("sequelize").Model, liked: boolean, favorited: boolean } | null>}
 */
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

module.exports = { getPostWithState };
