/**
 * 序列化 / 脱敏工具
 * 集中管理所有面向前端返回的字段映射，保持与原有各 route 中 sanitize 逻辑一致。
 */

// ---------------------------------------------------------------------------
// User 相关
// ---------------------------------------------------------------------------

/**
 * 将 User 模型实例转为前端所需的精简对象。
 * @param {import("sequelize").Model} user
 */
function sanitizeUser(user) {
  return {
    id: String(user.id),
    email: user.email,
    nickname: user.nickname,
    bio: user.bio,
    avatar: user.avatar,
    background: user.background,
    createdAt: user.createdAt ? user.createdAt.toISOString() : "",
  };
}

/**
 * 个人主页对外展示的用户资料（不含 email）。
 */
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

// ---------------------------------------------------------------------------
// Post 相关
// ---------------------------------------------------------------------------

/**
 * 将 Post 模型实例转为前端标准结构。
 * @param {import("sequelize").Model} post
 * @param {{ liked?: boolean, favorited?: boolean }} [state]
 */
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

// ---------------------------------------------------------------------------
// Friendship / FriendRequest 相关
// ---------------------------------------------------------------------------

/**
 * 将 Friendship 记录转为前端好友项。
 */
function sanitizeFriend(friendship) {
  return {
    id: String(friendship.id),
    userId: String(friendship.friendUserId),
    nickname: friendship.friend.nickname,
    avatar: friendship.friend.avatar,
    onlineStatus: "OFFLINE",
    lastMessage: friendship.lastMessage || "",
    lastMessageAt: friendship.lastMessageAt ? friendship.lastMessageAt.toISOString() : "",
    unreadCount: friendship.unreadCount || 0,
  };
}

/**
 * 将 FriendRequest 记录转为前端申请项。
 * @param {import("sequelize").Model} request
 * @param {string} userKey - 关联的用户别名（sender / receiver）
 */
function sanitizeFriendRequest(request, userKey) {
  const user = request[userKey];
  return {
    id: String(request.id),
    userId: String(user.id),
    nickname: user.nickname,
    avatar: user.avatar,
    status: request.status,
    createdAt: request.createdAt ? request.createdAt.toISOString() : "",
  };
}

// ---------------------------------------------------------------------------
// Comment 相关
// ---------------------------------------------------------------------------

/**
 * 将 Comment 模型实例转为前端结构。
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

module.exports = {
  sanitizeUser,
  sanitizeProfile,
  sanitizePost,
  sanitizeFriend,
  sanitizeFriendRequest,
  sanitizeComment,
};
