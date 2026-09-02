const { DataTypes } = require("sequelize");
const sequelize = require("./db");

// 好友关系：对应 YAML 中的 Friend
// 约定：A 与 B 成为好友时写入两条记录（userId→friendUserId 各一行），
// 这样每个人都能独立维护自己的未读数/最后一条消息预览
const Friendship = sequelize.define(
  "Friendship",
  {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true,
    },
    // 当前用户 ID
    userId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    // 好友的用户 ID
    friendUserId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    unreadCount: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 0,
    },
    lastMessage: {
      type: DataTypes.STRING(500),
      allowNull: true,
    },
    lastMessageAt: {
      type: DataTypes.DATE,
      allowNull: true,
    },
  },
  {
    tableName: "friendships",
    updatedAt: false,
    indexes: [{ unique: true, fields: ["userId", "friendUserId"] }],
  }
);

module.exports = Friendship;