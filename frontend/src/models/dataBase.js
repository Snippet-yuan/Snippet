const { Sequelize } = require("sequelize");

const sequelize = new Sequelize("snippet", "root", "123456", {
  host: "localhost",
  dialect: "mysql",
});

module.exports = sequelize;
