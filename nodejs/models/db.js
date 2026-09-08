/**
 * Sequelize 实例
 * 负责创建与 MySQL 的连接。配置集中于此，便于后续接入环境变量。
 */

const { Sequelize } = require("sequelize");

const sequelize = new Sequelize("snippet", "root", "123456", {
  host: "localhost",
  dialect: "mysql",
});

module.exports = sequelize;
