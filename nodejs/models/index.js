/**
 * 模型入口
 * 集中导入所有模型并声明关联关系，供上层按需取用。
 * 关联声明保持与原有逻辑完全一致。
 */

const sequelize = require("./db");

const User = require("./user");
const Friendship = require("./friend");
const FriendRequest = require("./friendRequest");
const Conversation = require("./conversation");
const Message = require("./message");
const Post = require("./post");
const Comment = require("./comment");
const PostLike = require("./postLike");
const PostFavorite = require("./postFavorite");
const Follow = require("./follow");

// ---------------------------------------------------------------------------
// 关联定义
// ---------------------------------------------------------------------------

// 用户 - 帖子
User.hasMany(Post, { foreignKey: "ownerId", as: "posts", onDelete: "CASCADE" });
Post.belongsTo(User, { foreignKey: "ownerId", as: "owner" });

// 用户 - 评论
User.hasMany(Comment, {
  foreignKey: "authorId",
  as: "comments",
  onDelete: "CASCADE",
});
Comment.belongsTo(User, { foreignKey: "authorId", as: "author" });

// 帖子 - 评论
Post.hasMany(Comment, {
  foreignKey: "postId",
  as: "comments",
  onDelete: "CASCADE",
});
Comment.belongsTo(Post, { foreignKey: "postId", as: "post" });

// 用户 - 点赞
User.hasMany(PostLike, {
  foreignKey: "userId",
  as: "likes",
  onDelete: "CASCADE",
});
PostLike.belongsTo(User, { foreignKey: "userId", as: "user" });
Post.hasMany(PostLike, {
  foreignKey: "postId",
  as: "likes",
  onDelete: "CASCADE",
});
PostLike.belongsTo(Post, { foreignKey: "postId", as: "post" });

// 用户 - 收藏
User.hasMany(PostFavorite, {
  foreignKey: "userId",
  as: "favorites",
  onDelete: "CASCADE",
});
PostFavorite.belongsTo(User, { foreignKey: "userId", as: "user" });
Post.hasMany(PostFavorite, {
  foreignKey: "postId",
  as: "favorites",
  onDelete: "CASCADE",
});
PostFavorite.belongsTo(Post, { foreignKey: "postId", as: "post" });

// 好友申请
FriendRequest.belongsTo(User, {
  foreignKey: "senderId",
  as: "sender",
  onDelete: "CASCADE",
});
FriendRequest.belongsTo(User, {
  foreignKey: "receiverId",
  as: "receiver",
  onDelete: "CASCADE",
});

// 好友（双向两行）
Friendship.belongsTo(User, {
  foreignKey: "userId",
  as: "user",
  onDelete: "CASCADE",
});
Friendship.belongsTo(User, {
  foreignKey: "friendUserId",
  as: "friend",
  onDelete: "CASCADE",
});

// 关注（单向）
User.hasMany(Follow, {
  foreignKey: "followerId",
  as: "following",
  onDelete: "CASCADE",
});
Follow.belongsTo(User, {
  foreignKey: "followerId",
  as: "follower",
  onDelete: "CASCADE",
});
User.hasMany(Follow, {
  foreignKey: "followedId",
  as: "followers",
  onDelete: "CASCADE",
});
Follow.belongsTo(User, {
  foreignKey: "followedId",
  as: "followed",
  onDelete: "CASCADE",
});

// 会话 - 参与者
Conversation.belongsTo(User, {
  foreignKey: "user1Id",
  as: "user1",
  onDelete: "CASCADE",
});
Conversation.belongsTo(User, {
  foreignKey: "user2Id",
  as: "user2",
  onDelete: "CASCADE",
});

// 会话 - 消息
Conversation.hasMany(Message, {
  foreignKey: "conversationId",
  as: "messages",
  onDelete: "CASCADE",
});
Message.belongsTo(Conversation, {
  foreignKey: "conversationId",
  as: "conversation",
});

// 用户 - 消息
User.hasMany(Message, {
  foreignKey: "senderId",
  as: "sentMessages",
  onDelete: "CASCADE",
});
User.hasMany(Message, {
  foreignKey: "receiverId",
  as: "receivedMessages",
  onDelete: "CASCADE",
});
Message.belongsTo(User, { foreignKey: "senderId", as: "sender" });
Message.belongsTo(User, { foreignKey: "receiverId", as: "receiver" });

module.exports = {
  sequelize,
  User,
  Friendship,
  FriendRequest,
  Conversation,
  Message,
  Post,
  Comment,
  PostLike,
  PostFavorite,
  Follow,
};
