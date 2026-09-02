const { sequelize, User, Post } = require("../models");

const titles = [
  "周末去爬山，山顶的风景太治愈了",
  "今天做的午餐，色香味俱全",
  "新买的相机到了，随手拍了几张",
  "深夜放毒，这家火锅绝了",
  "城市夜景，灯火辉煌",
  "咖啡店打卡，氛围感拉满",
  "第一次尝试滑雪，摔了好多次",
  "我家猫主子今天超乖",
  "海边日落，真的会发光",
  "工作室的一角，慢慢布置起来了",
];

const descriptions = [
  "走了两个小时才到山顶，看到云海的那一刻一切都值得了。",
  "自己动手做的番茄牛腩，配上一碗热米饭，幸福就是那么简单。",
  "还没完全研究明白，先随便拍拍，后面慢慢更新成片。",
  "排了四十分钟队，果然没让人失望，强烈推荐！",
  "加班完回家的路上，发现这座城市的温柔。",
  "周末下午的悠闲时光，一本书一杯拿铁。",
  "虽然一直在摔，但滑起来的感觉太自由了。",
  "它今天居然主动蹭我了，老母亲感动落泪。",
  "傍晚的海边，天空像被打翻的调色盘。",
  "一点点把喜欢的东西填进来，生活要有仪式感。",
];

function pick(arr, count) {
  const copy = [...arr];
  const result = [];
  while (result.length < count && copy.length) {
    result.push(copy.splice(Math.floor(Math.random() * copy.length), 1)[0]);
  }
  return result;
}

async function main() {
  const users = await User.findAll();
  if (users.length === 0) {
    console.error("数据库里没有用户，请先运行 node scripts/seedUsers.js");
    process.exit(1);
  }

  const count = parseInt(process.argv[2] || "30", 10);
  const posts = [];

  for (let i = 0; i < count; i++) {
    const owner = users[i % users.length];
    const imgCount = 1 + Math.floor(Math.random() * 3); // 每帖 1~3 张图
    const images = Array.from(
      { length: imgCount },
      (_, k) => `https://picsum.photos/seed/${Date.now()}_${i}_${k}/600/800`
    );

    posts.push({
      ownerId: owner.id,
      title: titles[i % titles.length],
      description: descriptions[i % descriptions.length],
      images,
      likeCount: Math.floor(Math.random() * 200),
      favoriteCount: Math.floor(Math.random() * 80),
      commentCount: Math.floor(Math.random() * 50),
      shareCount: Math.floor(Math.random() * 20),
    });
  }

  const created = await Post.bulkCreate(posts);
  console.log(`已生成 ${created.length} 条帖子`);
  console.log(`样例：${created[0].title} | 图片 ${created[0].images.length} 张`);

  await sequelize.close();
}

main().catch((err) => {
  console.error("生成失败：", err.message);
  process.exit(1);
});