const { DataTypes } = require("sequelize");
const sequelize = require("./db");

const FriendRequest = sequelize.define(
  "FriendRequest",
  {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true,
    },
    senderId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    receiverId: {
      type: DataTypes.INTEGER,
      allowNull: false,
    },
    status: {
      type: DataTypes.ENUM("PENDING", "ACCEPTED", "REJECTED"),
      allowNull: false,
      defaultValue: "PENDING",
    },
  },
  {
    tableName: "friend_requests",
    indexes: [{ unique: true, fields: ["senderId", "receiverId"] }],
  }
);

module.exports = FriendRequest;
