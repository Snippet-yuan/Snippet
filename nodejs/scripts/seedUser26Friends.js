const bcrypt = require("bcryptjs");
const { Op } = require("sequelize");
const { sequelize, User, Friendship, Conversation, Message } = require("../models");

const OWNER_ID = 26;
const TARGET_FRIEND_COUNT = 20;
const MESSAGE_MIN = 12;
const MESSAGE_MAX = 32;

// ============ 随机工具 ============

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function shuffle(items) {
  return [...items].sort(() => Math.random() - 0.5);
}

function pick(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

// ============ 新用户素材（补足候选人数用） ============

const given = ["晨曦", "晚风", "山茶", "拾光", "柚子", "苏打", "薄荷", "南巷", "初雪", "半夏", "青柠", "鹿鸣", "小满", "云舒", "听雨", "星河", "拾柒", "木子"];
const extra = ["同学", "先生", "小姐", "不加糖", "爱睡觉", "在旅途", "看风景", "吃西瓜", "打篮球", "写代码", "晒太阳", "追月亮", "666", "233", "小号"];
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

function randomEmail(index) {
  const domain = pick(["qq.com", "163.com", "gmail.com", "outlook.com", "foxmail.com"]);
  return `friend26_${Date.now() % 100000}_${index}@${domain}`;
}

function randomAvatar() {
  return `https://i.pravatar.cc/150?img=${randomInt(1, 70)}`;
}

function randomBackground() {
  return `https://picsum.photos/seed/${randomInt(0, 1000)}/1080/720`;
}

// ============ 聊天模板 ============

const conversationTemplates = [
  [
    ["最近怎么样？好久没聊天了。", "还不错，最近刚忙完一个项目。你呢？"],
    ["我也差不多，周末要不要一起吃饭？", "可以啊，周六下午有空。"],
    ["那就定在三点？我找一家安静点的店。", "好，到时候把地址发我。"],
    ["没问题，周六见！", "周六见。"],
  ],
  [
    ["你上次推荐的那部剧看完了。", "怎么样？我觉得后半段特别精彩。"],
    ["确实，最后两集看得我停不下来。", "哈哈，我就知道你会喜欢。"],
    ["还有类似的剧吗？周末想继续看。", "我整理几个发你，都是节奏比较舒服的。"],
    ["收到，今晚就从第一部开始。", "看完记得告诉我感受。"],
  ],
  [
    ["明天早上一起跑步吗？", "可以，还是公园东门见？"],
    ["对，七点半怎么样？", "七点半没问题，我会提前到。"],
    ["最近天气越来越热了，记得带水。", "好的，你也别跑太快，等我一起。"],
    ["放心，明天见。", "明天见！"],
  ],
  [
    ["我刚发现一家很好吃的面馆。", "在哪里？看起来好吃吗？"],
    ["就在公司附近，汤底很鲜，分量也很足。", "听起来不错，下次一起去试试。"],
    ["可以，工作日中午去应该不用排太久。", "那下周找一天去。"],
    ["好，我提前看看哪天方便。", "等你的消息。"],
  ],
  [
    ["最近在学什么新东西？", "在学摄影，刚开始研究怎么拍夜景。"],
    ["听起来很有意思，拍出好看的照片记得分享。", "当然，你要是有空也可以一起出来拍。"],
    ["好啊，正好我也想换个地方散散心。", "那我们找个周末去老城区。"],
    ["一言为定。", "一言为定。"],
  ],
];

function buildMessages(conversation, ownerId, friendId, count) {
  const template = conversationTemplates[randomInt(0, conversationTemplates.length - 1)];
  const messages = [];
  const startAt = new Date(Date.now() - randomInt(2, 24) * 24 * 60 * 60 * 1000);
  let senderId = Math.random() > 0.5 ? ownerId : friendId;

  for (let index = 0; index < count; index++) {
    const pair = template[index % template.length];
    const content = pair[index % 2];
    const sentAt = new Date(startAt.getTime() + index * randomInt(8, 95) * 60 * 1000);
    messages.push({
      conversationId: conversation.id,
      senderId,
      receiverId: senderId === ownerId ? friendId : ownerId,
      content,
      sentAt,
    });
    senderId = senderId === ownerId ? friendId : ownerId;
  }

  return messages;
}

// ============ 主流程 ============

async function main() {
  await sequelize.authenticate();

  const owner = await User.findByPk(OWNER_ID);
  if (!owner) throw new Error(`找不到 id=${OWNER_ID} 的用户`);

  const existing = await Friendship.findAll({
    where: { userId: owner.id },
    attributes: ["friendUserId"],
  });
  const existingFriendIds = [...new Set(existing.map(({ friendUserId }) => Number(friendUserId)))];
  console.log(`用户 26 当前好友：${existingFriendIds.length} 个`);

  const otherUsers = await User.findAll({
    where: { id: { [Op.ne]: owner.id } },
    attributes: ["id", "nickname"],
    order: [["id", "ASC"]],
  });
  let candidates = otherUsers.filter((user) => !existingFriendIds.includes(Number(user.id)));

  // 候选不足时先补充新用户
  const needed = Math.max(0, TARGET_FRIEND_COUNT - existingFriendIds.length);
  if (candidates.length < needed) {
    const missing = needed - candidates.length;
    const hashedPassword = await bcrypt.hash("123456", 10);
    const freshUsers = Array.from({ length: missing }, (_, i) => ({
      email: randomEmail(i),
      password: hashedPassword,
      nickname: `${pick(given)}${pick(extra)}${Date.now() % 10000}_${i}`,
      bio: pick(bios),
      avatar: randomAvatar(),
      background: randomBackground(),
    }));
    const created = await User.bulkCreate(freshUsers);
    console.log(`候选用户不足，已补充 ${created.length} 个新用户（默认密码 123456）`);
    candidates = [...candidates, ...created];
  }

  const picked = shuffle(candidates).slice(0, needed).map((user) => Number(user.id));
  if (picked.length) {
    const rows = [];
    for (const friendId of picked) {
      rows.push({ userId: owner.id, friendUserId: friendId });
      rows.push({ userId: friendId, friendUserId: owner.id });
    }
    await Friendship.bulkCreate(rows, { ignoreDuplicates: true });
    console.log(`已为 26 新增 ${picked.length} 位好友：${picked.join(", ")}`);
  } else {
    console.log("好友数已达标，无需新增");
  }

  const allFriendIds = [...new Set([...existingFriendIds, ...picked])];
  const summary = [];

  await sequelize.transaction(async (transaction) => {
    for (const friendId of shuffle(allFriendIds)) {
      const [user1Id, user2Id] = [owner.id, friendId].sort((a, b) => a - b);
      const [conversation] = await Conversation.findOrCreate({
        where: { user1Id, user2Id },
        defaults: { lastMessage: "", lastMessageAt: null, unread1Count: 0, unread2Count: 0 },
        transaction,
      });

      const messageCount = randomInt(MESSAGE_MIN, MESSAGE_MAX);
      const messages = buildMessages(conversation, owner.id, friendId, messageCount);
      await Message.bulkCreate(messages, { transaction });

      const lastMessage = messages[messages.length - 1];
      const ownerIsUser1 = Number(conversation.user1Id) === Number(owner.id);
      await conversation.update(
        {
          lastMessage: lastMessage.content,
          lastMessageAt: lastMessage.sentAt,
          unread1Count: 0,
          unread2Count: 0,
        },
        { transaction },
      );

      // 同步两条好友记录上的会话预览
      const preview = { lastMessage: lastMessage.content, lastMessageAt: lastMessage.sentAt };
      await Friendship.update(
        { ...preview, unreadCount: ownerIsUser1 ? conversation.unread1Count : conversation.unread2Count },
        { where: { userId: owner.id, friendUserId: friendId }, transaction },
      );
      await Friendship.update(
        { ...preview, unreadCount: ownerIsUser1 ? conversation.unread2Count : conversation.unread1Count },
        { where: { userId: friendId, friendUserId: owner.id }, transaction },
      );

      summary.push({ friendId, messageCount });
    }
  });

  console.log(`用户 26 现有好友：${allFriendIds.length} 个，本次为 ${summary.length} 位好友生成会话`);
  summary.forEach(({ friendId, messageCount }) => console.log(`  - 好友 ${friendId}：${messageCount} 条消息`));

  const totalMessages = await Message.count({
    where: { [Op.or]: [{ senderId: owner.id }, { receiverId: owner.id }] },
  });
  console.log(`用户 26 相关的消息总数：${totalMessages}`);
}

main()
  .catch((error) => {
    console.error("生成失败：", error.message);
    process.exitCode = 1;
  })
  .finally(async () => {
    await sequelize.close();
  });
