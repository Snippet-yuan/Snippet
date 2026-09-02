import mysql from "mysql2/promise";
import { Sequelize } from "sequelize";

const DB_NAME = "snippet";

export const sequelize = new Sequelize(DB_NAME, "root", "123456", {
  host: "localhost",
  dialect: "mysql",
});

/**
 * 确保数据库存在（不存在则自动创建），并返回 Sequelize 实例。
 * 应用启动前调用一次即可，无需手动建库。
 */
export async function initDatabase() {
  const conn = await mysql.createConnection({
    host: "localhost",
    user: "root",
    password: "123456",
  });
  await conn.query(
    `CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci`,
  );
  await conn.end();

  return sequelize;
}

export default sequelize;
