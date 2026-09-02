const { sequelize } = require("./models");

async function main() {
  try {
    await sequelize.authenticate();
    console.log("数据库连接成功");
    // 首次运行创建数据表；开发期改字段可用 { alter: true }，生产环境请用 migration
    await sequelize.sync();
    console.log("数据表同步完成：", Object.keys(sequelize.models).join(", "));
  } catch (err) {
    console.error("初始化失败：", err.message);
  } finally {
    await sequelize.close();
  }
}

main();