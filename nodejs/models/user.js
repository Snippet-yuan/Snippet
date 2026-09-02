const { DataTypes } = require("sequelize");
const sequelize = require("./db");

// 用户：对应 YAML 中的 UserProfile（email 登录 + 主页信息）
const User = sequelize.define(
  "User",
  {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true,
    },
    email: {
      type: DataTypes.STRING(100),
      allowNull: false,
      unique: true,
    },
    password: {
      type: DataTypes.STRING(255),
      allowNull: false,
    },
    nickname: {
      type: DataTypes.STRING(50),
      allowNull: false,
    },
    bio: {
      type: DataTypes.STRING(255),
      allowNull: false,
      defaultValue: "",
    },
    avatar: {
      // 头像由前端裁剪为 data URL，图片内容可能超过普通 VARCHAR 的长度限制。
      type: DataTypes.TEXT,
      allowNull: false,
      defaultValue: "",
    },
    background: {
      type: DataTypes.STRING(500),
      allowNull: false,
      defaultValue: "",
    },
  },
  {
    tableName: "users",
  }
);

module.exports = User;