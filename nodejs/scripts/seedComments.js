const { sequelize, User, Post, Comment } = require("../models");

// 随机评论内容池（尽量贴合通用帖子语境）
const commentPool = [
  "拍得真好看！",
  "这个角度绝了，学到了。",
  "文案好有感觉，收藏了。",
  "颜色也太舒服了吧。",
  "种草了，周末就去打卡。",
  "看完心情都变好了，谢谢分享。",
  "构图好干净，看着很舒服。",
  "氛围感拉满，想问问是哪家店？",
  "这组图也太会拍了。",
  "第几张最好看，我选三。",
  "生活气息满满，爱了。",
  "请问用的什么设备拍的？",
  "同款快乐，我上次去也这样。",
  "这也太治愈了，直接当壁纸。",
  "羡慕住了，我也想出去玩。",
  "细节拍得真好，放大看了好几遍。",
  "很有故事感的一张。",
  "前排围观，好看！",
  "已转发给朋友，他说也要去。",
  "第一眼还以为是电影截图。",
  "光线运用得太妙了。",
  "被安利到了，马上安排。",
  "看完想立刻出门走走。",
  "每次刷到你都能发现新惊喜。",
  "色调统一，强迫症福音。",
  "这张可以当明信片了。",
  "景好看，拍得更好。",
  "隔着屏幕都感受到那份惬意了。",
  "好想知道你平时怎么选景的。",
  "多更新！根本看不够。",
];

function pick(arr, count) {
  const copy = [...arr];
  const result = [];
  while (result.length < count && copy.length) {
    result.push(copy.splice(Math.floor(Math.random() * copy.length), 1)[0]);
  }
  return result;
}

function pickOne(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

function randomInt(min, max) {
  return min + Math.floor(Math.random() * (max - min + 1));
}

// 生成最近 maxDays 天内的随机时间，避免所有评论挤在同一秒
function randomRecentDate(maxDays) {
  const now = Date.now();
  const offsetMs = Math.floor(Math.random() * maxDays * 24 * 60 * 60 * 1000);
  return new Date(now - offsetMs);
}

async function main() {
  const minPerPost = Math.max(1, Number(process.argv[2] || 2));
  const maxPerPost = Math.max(minPerPost, Number(process.argv[3] || 8));

  const [posts, users] = await Promise.all([
    Post.findAll({ attributes: ["id", "ownerId"] }),
    User.findAll({ attributes: ["id"] }),
  ]);

  if (!posts.length) {
    console.error("数据库中没有帖子，请先生成帖子数据");
    process.exit(1);
  }
  if (users.length < 2) {
    console.error("数据库中至少需要两个用户，才能生成评论数据");
    process.exit(1);
  }

  const comments = [];
  for (const post of posts) {
    // 评论人尽量排除楼主本人，评论不重复选人
    const candidates = users.filter((user) => Number(user.id) !== Number(post.ownerId));
    const pool = candidates.length ? candidates : users;
    const count = randomInt(minPerPost, Math.min(maxPerPost, pool.length));

    for (const user of pick(pool, count)) {
      comments.push({
        postId: post.id,
        authorId: user.id,
        content: pickOne(commentPool),
        createdAt: randomRecentDate(30),
      });
    }
  }

  // 分批写入，避免一次插入过多数据
  const BATCH_SIZE = 500;
  for (let i = 0; i < comments.length; i += BATCH_SIZE) {
    await Comment.bulkCreate(comments.slice(i, i + BATCH_SIZE));
  }

  // 同步 commentCount：按真实评论条数回写
  const { fn, col } = require("sequelize");
  const grouped = await Comment.findAll({
    attributes: ["postId", [fn("COUNT", col("id")), "total"]],
    group: ["postId"],
    raw: true,
  });
  for (const row of grouped) {
    await Post.update({ commentCount: row.total }, { where: { id: row.postId } });
  }

  console.log(`已为 ${posts.length} 条帖子生成 ${comments.length} 条评论`);
  console.log(`评论数已同步：每帖 ${minPerPost}~${Math.min(maxPerPost, users.length - 1)} 条`);
}

main()
  .catch((error) => {
    console.error("生成评论失败：", error.message);
    process.exitCode = 1;
  })
  .finally(async () => {
    await sequelize.close();
  });
