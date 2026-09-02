const { sequelize, User } = require("../models");

const bios = [
  "记录生活里的小确幸。",
  "保持好奇，持续学习。",
  "热爱生活，也热爱分享。",
  "正在把平凡的日子过得有趣。",
  "用照片和文字收藏时光。",
  "慢慢来，一切都会发生。",
  "喜欢音乐、电影和远方。",
  "认真生活，快乐创作。",
];

function randomBio() {
  return bios[Math.floor(Math.random() * bios.length)];
}

async function main() {
  const users = await User.findAll({ attributes: ["id", "bio"] });
  for (const user of users) {
    if (!user.bio) {
      user.bio = randomBio();
      await user.save();
    }
  }
  console.log(`已为 ${users.filter((user) => user.bio).length} 个用户补充个性签名`);
}

main()
  .catch((error) => {
    console.error("补充个性签名失败：", error.message);
    process.exitCode = 1;
  })
  .finally(async () => {
    await sequelize.close();
  });
