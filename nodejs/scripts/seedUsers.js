const bcrypt = require("bcryptjs");
const { sequelize, User } = require("../models");

// 随机中文昵称素材
const given = ["晨曦", "晚风", "山茶", "拾光", "柚子", "苏打", "薄荷", "南巷", "初雪", "半夏", "青柠", "鹿鸣", "小满", "云舒", "听雨", "星河", "拾柒", "木子"];
const extra = ["同学", "先生", "小姐", "不加糖", "爱睡觉", "在旅途", "看风景", "吃西瓜", "打篮球", "写代码", "晒太阳", "追月亮", "666", "233", "小号"];

function pick(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

function randomEmail(index) {
  const domain = pick(["qq.com", "163.com", "gmail.com", "outlook.com", "foxmail.com"]);
  // 用拼音风格字母组合保证唯一
  return `user${Date.now() % 100000}_${index}@${domain}`;
}

function randomAvatar() {
  const n = 1 + Math.floor(Math.random() * 70);
  return `https://i.pravatar.cc/150?img=${n}`;
}

function randomBackground() {
  const seed = Math.floor(Math.random() * 1000);
  return `https://picsum.photos/seed/${seed}/1080/720`;
}

async function main() {
  const count = parseInt(process.argv[2] || "20", 10);
  const password = process.argv[3] || "123456";
  const hashedPassword = await bcrypt.hash(password, 10);

  const users = Array.from({ length: count }, (_, i) => ({
    email: randomEmail(i),
    password: hashedPassword,
    nickname: `${pick(given)}${pick(extra)}${i === 0 ? "" : i}`,
    avatar: randomAvatar(),
    background: randomBackground(),
  }));

  const created = await User.bulkCreate(users);
  console.log(`已生成 ${created.length} 个用户，默认密码：${password}`);

  // 打印邮箱清单，方便登录测试
  created.slice(0, 20).forEach((u) => {
    console.log(`  ${u.nickname}  ${u.email}`);
  });

  await sequelize.close();
}

main().catch((err) => {
  console.error("生成失败：", err.message);
  process.exit(1);
});