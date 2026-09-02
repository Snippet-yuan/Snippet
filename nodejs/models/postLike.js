const { DataTypes } = require("sequelize");
const sequelize = require("./db");

// 帖子点赞记录：一条 (postId, userId) 唯一，用于判断 liked 状态
const PostLike = sequelize.define(
  "PostLike",
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
    tableName: "post_likes",
    updatedAt: false,
    indexes: [{ unique: true, fields: ["postId", "userId"] }],
  }
);

module.exports = PostLike;