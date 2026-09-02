const { DataTypes } = require("sequelize");
const sequelize = require("./db");

// 会话：对应 YAML 中的会话列表 / messages 的 conversationId
// 一个会话 = 两个用户的一对一聊天，用唯一索引保证同对用户只有一个会话
const Conversation = sequelize.define(
  "Conversation",
  {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true,
    },
    user1Id: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    user2Id: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    lastMessage: {
      type: DataTypes.STRING(500),
      allowNull: true,
    },
    lastMessageAt: {
      type: DataTypes.DATE,
      allowNull: true,
    },
    // 未读数（分别属于两位参与者）
    unread1Count: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 0,
    },
    unread2Count: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 0,
    },
  },
  {
    tableName: "conversations",
    timestamps: false,
    indexes: [{ unique: true, fields: ["user1Id", "user2Id"] }],
  }
);

module.exports = Conversation;