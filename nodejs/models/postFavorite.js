const { DataTypes } = require("sequelize");
const sequelize = require("./db");

// 帖子收藏记录：一条 (postId, userId) 唯一，用于判断 favorited 状态
const PostFavorite = sequelize.define(
  "PostFavorite",
  {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true,
    },
    postId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    userId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
  },
  {
    tableName: "post_favorites",
    updatedAt: false,
    indexes: [{ unique: true, fields: ["postId", "userId"] }],
  }
);

module.exports = PostFavorite;