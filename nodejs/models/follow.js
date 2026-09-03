const { DataTypes } = require("sequelize");
const sequelize = require("./db");

// 关注关系：单向关注，A 关注 B 写入一条记录（followerId → followedId）
// 无需对方同意，可随时取关；followerId + followedId 联合唯一，防止重复关注
const Follow = sequelize.define(
  "Follow",
  {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true,
    },
    // 关注者（主动发起关注的人）
    followerId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    // 被关注者
    followedId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
  },
  {
    tableName: "follows",
    updatedAt: false,
    indexes: [
      { unique: true, fields: ["followerId", "followedId"] },
      { fields: ["followedId"] },
    ],
  }
);

module.exports = Follow;
