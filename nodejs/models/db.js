const { Sequelize } = require("sequelize");

// orm实例
const sequelize = new Sequelize("snippet", "root", "123456", {
  host: "localhost",
  dialect: "mysql", // 数据库类型
});

module.exports = sequelize;