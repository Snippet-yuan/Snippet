const {
  sequelize,
  User,
  Post,
  Comment,
  PostLike,
  PostFavorite,
} = require("../models");

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
  "雨天窝在家里看书，太舒服了",
  "路边的花开了，随手记录一下",
  "第一次自己烤面包，居然没翻车",
  "清晨的公园，空气特别好",
  "出差路上的随手拍",
  "新入的手冲装备，仪式感满满",
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
  "雨声配着热茶，是难得的安静时刻。",
  "总有些小惊喜藏在不经意的角落。",
  "第一次尝试，结果比想象中顺利多了。",
  "慢下来，才能看见生活本来的样子。",
  "旅途的意义，大概就是遇见不同的风景。",
  "从一杯咖啡开始，认真对待每个清晨。",
];

const commentsPool = [
  "拍得真好看！",
  "这个地方值得收藏，下次也想去。",
  "文字和图片都很有氛围感。",
  "生活记录得很棒，喜欢这组照片。",
  "太治愈了，看完心情都变好了。",
  "改天也去试试！",
  "构图好好看，求相机型号。",
  "这是什么神仙地方！",
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

async function main() {
  // 用法：node scripts/seedOtherPosts.js [帖子总数] [排除的用户ID]
  // 默认生成 30 条，由 id != 26 的随机用户发布
  const totalPosts = Math.max(1, Number(process.argv[2] || 30));
  const excludedId = Number(process.argv[3] || 26);

  const users = await User.findAll();
  if (users.length < 2) {
    console.error("数据库中至少需要两个用户，才能生成点赞、收藏和评论数据");
    process.exit(1);
  }

  // 作者池：除被排除用户外的所有用户
  const authors = users.filter((user) => user.id !== excludedId);
  if (authors.length === 0) {
    console.error("没有可用的发布用户，请先创建其他用户");
    process.exit(1);
  }

  // 随机挑选发布者，每人约 1~3 条，总量不超过 totalPosts
  const authorCount = Math.max(1, Math.min(authors.length, Math.ceil(totalPosts / 3)));
  const selectedAuthors = pick(authors, authorCount);

  const posts = [];
  const seedBase = Date.now();
  let postIndex = 0;

  for (const author of selectedAuthors) {
    const postsPerAuthor = randomInt(1, 3);
    for (let i = 0; i < postsPerAuthor && postIndex < totalPosts; i++, postIndex++) {
      const imgCount = randomInt(1, 3);
      const images = Array.from(
        { length: imgCount },
        (_, k) => `https://picsum.photos/seed/other_${seedBase}_${postIndex}_${k}/600/800`
      );
      posts.push({
        ownerId: author.id,
        title: pickOne(titles),
        description: pickOne(descriptions),
        images,
        shareCount: randomInt(0, 20),
      });
    }
  }

  const created = await Post.bulkCreate(posts);
  const likes = [];
  const favorites = [];
  const comments = [];

  for (const post of created) {
    const candidates = users.filter((user) => user.id !== post.ownerId);
    const likeUsers = pick(candidates, Math.min(candidates.length, randomInt(3, 10)));
    const favoriteUsers = pick(candidates, Math.min(candidates.length, randomInt(1, 5)));

    likes.push(...likeUsers.map((user) => ({ postId: post.id, userId: user.id })));
    favorites.push(...favoriteUsers.map((user) => ({ postId: post.id, userId: user.id })));
    comments.push(
      ...pick(candidates, Math.min(candidates.length, randomInt(1, 4))).map((user) => ({
        postId: post.id,
        authorId: user.id,
        content: pickOne(commentsPool),
      }))
    );
  }

  await Promise.all([
    PostLike.bulkCreate(likes),
    PostFavorite.bulkCreate(favorites),
    Comment.bulkCreate(comments),
  ]);

  // 用真实记录数回写计数，保证与点赞/收藏/评论表一致
  for (const post of created) {
    await post.update({
      likeCount: likes.filter((like) => like.postId === post.id).length,
      favoriteCount: favorites.filter((favorite) => favorite.postId === post.id).length,
      commentCount: comments.filter((comment) => comment.postId === post.id).length,
    });
  }

  const ownerIds = [...new Set(created.map((post) => post.ownerId))];
  console.log(`已为 ${ownerIds.length} 位用户生成 ${created.length} 条帖子`);
  console.log(`包含 ${created.reduce((total, post) => total + post.images.length, 0)} 张图片、${likes.length} 个点赞、${favorites.length} 个收藏和 ${comments.length} 条评论`);

  await sequelize.close();
}

main().catch((err) => {
  console.error("生成失败：", err.message);
  process.exit(1);
});