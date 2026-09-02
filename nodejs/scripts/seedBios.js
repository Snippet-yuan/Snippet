const { sequelize, User } = require("../models");

const bios = [
  "把日子过成自己喜欢的样子。",
  "喜欢拍照、散步，也喜欢记录生活里的小确幸。",
  "认真工作，慢慢生活，偶尔出门看看风景。",
  "热爱生活，也在努力成为更好的自己。",
  "在城市里生活，在平凡的日子里寻找惊喜。",
  "喜欢咖啡、音乐和没有计划的周末。",
  "一边学习，一边把生活过得松弛一点。",
  "记录灵感，也记录那些值得记住的瞬间。",
  "愿每一次出发，都能遇见新的风景。",
  "不赶时间，认真感受当下。",
  "喜欢安静，也享受和有趣的人聊天。",
  "白天努力生活，晚上留一点时间给自己。",
  "正在学习与生活和解。",
  "简单生活，保持好奇，偶尔远行。",
  "爱美食，爱电影，爱一切温柔的事物。",
  "慢热但真诚，欢迎分享你的故事。",
  "希望每天都有一点小小的进步。",
  "把喜欢的事情坚持久一点。",
  "生活没有标准答案，开心就好。",
  "在平淡生活里收集闪闪发光的片段。",
  "喜欢新鲜事物，也珍惜熟悉的人。",
  "一名普通但认真生活的人。",
  "有空就读书、运动，或者去看看海。",
  "愿我们都能拥有热爱，也拥有自由。",
];

function pick(items) {
  return items[Math.floor(Math.random() * items.length)];
}

async function main() {
  await sequelize.authenticate();
  const users = await User.findAll();

  for (const user of users) {
    await user.update({ bio: pick(bios) });
  }

  console.log(`已为 ${users.length} 个用户生成随机个性签名`);
  await sequelize.close();
}

main().catch(async (error) => {
  console.error("生成失败：", error.message);
  await sequelize.close();
  process.exit(1);
});
