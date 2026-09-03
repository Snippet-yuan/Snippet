const { sequelize, User, Friendship, Conversation, Message } = require("../models");

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

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function shuffle(items) {
  return [...items].sort(() => Math.random() - 0.5);
}

function getConversationKey(user1Id, user2Id) {
  return [Number(user1Id), Number(user2Id)].sort((a, b) => a - b).join(":");
}

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

async function main() {
  await sequelize.authenticate();

  const owner = await User.findByPk(26);
  if (!owner) {
    throw new Error("找不到 id=26 的用户");
  }

  const friendships = await Friendship.findAll({
    where: { userId: owner.id },
    attributes: ["friendUserId"],
  });
  const friendIds = [...new Set(friendships.map(({ friendUserId }) => Number(friendUserId)))];

  if (!friendIds.length) {
    throw new Error("用户 26 当前没有好友，请先运行 npm run seed:friends");
  }

  const friends = await User.findAll({ where: { id: friendIds }, attributes: ["id"] });
  const friendIdSet = new Set(friends.map(({ id }) => Number(id)));
  const validFriendIds = friendIds.filter((friendId) => friendIdSet.has(friendId));

  await sequelize.transaction(async (transaction) => {
    const createdConversationIds = [];

    for (const friendId of shuffle(validFriendIds)) {
      const [user1Id, user2Id] = [owner.id, friendId].sort((a, b) => a - b);
      const [conversation] = await Conversation.findOrCreate({
        where: { user1Id, user2Id },
        defaults: {
          lastMessage: "",
          lastMessageAt: null,
          unread1Count: 0,
          unread2Count: 0,
        },
        transaction,
      });

      const messageCount = randomInt(12, 32);
      const messages = buildMessages(conversation, owner.id, friendId, messageCount);
      await Message.bulkCreate(messages, { transaction });

      const lastMessage = messages[messages.length - 1];
      const ownerIsUser1 = Number(conversation.user1Id) === Number(owner.id);
      await conversation.update(
        {
          lastMessage: lastMessage.content,
          lastMessageAt: lastMessage.sentAt,
          unread1Count: ownerIsUser1 && Number(lastMessage.receiverId) === Number(owner.id) ? randomInt(0, 3) : 0,
          unread2Count: !ownerIsUser1 && Number(lastMessage.receiverId) === Number(owner.id) ? randomInt(0, 3) : 0,
        },
        { transaction },
      );
      createdConversationIds.push(conversation.id);
    }

    console.log(`已为用户 26 生成 ${createdConversationIds.length} 个好友会话，共 ${createdConversationIds.length} 组对话历史`);
  });
}

main()
  .catch((error) => {
    console.error("生成消息历史失败：", error.message);
    process.exitCode = 1;
  })
  .finally(async () => {
    await sequelize.close();
  });
