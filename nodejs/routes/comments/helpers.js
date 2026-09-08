/**
 * 评论模块 - 共享辅助
 */

const { User } = require("../../models");

/**
 * 序列化评论记录。
 * @param {import("sequelize").Model} comment
 */
function sanitizeComment(comment) {
  return {
    id: String(comment.id),
    postId: String(comment.postId),
    authorId: String(comment.authorId),
    authorNickname: comment.author?.nickname || "",
    authorAvatar: comment.author?.avatar || "",
    content: comment.content,
    createdAt: comment.createdAt ? comment.createdAt.toISOString() : "",
  };
}

/** 作者关联（复用） */
const authorInclude = {
  model: User,
  as: "author",
  attributes: ["id", "nickname", "avatar"],
};

/**
 * 解析正整数，非法返回 null。
 */
function parsePositiveInteger(value, fallback) {
  const parsed = Number(value ?? fallback);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
}

module.exports = { sanitizeComment, authorInclude, parsePositiveInteger };
