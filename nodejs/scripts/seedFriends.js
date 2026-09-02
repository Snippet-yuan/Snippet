const { sequelize, User, Friendship } = require("../models");

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function shuffle(items) {
  return [...items].sort(() => Math.random() - 0.5);
}

async function main() {
  await sequelize.authenticate();
  const users = await User.findAll({ attributes: ["id"] });

  if (users.length < 2) {
    console.log("至少需要 2 个用户才能生成好友关系");
    return;
  }

  const userIds = users.map((user) => user.id);
  const pairs = new Set();
  const rows = [];
  const friendCounts = new Map(userIds.map((id) => [id, 0]));
  const existing = await Friendship.findAll({
    attributes: ["userId", "friendUserId"],
  });

  for (const friendship of existing) {
    const pairKey = [friendship.userId, friendship.friendUserId]
      .sort((a, b) => a - b)
      .join(":");
    pairs.add(pairKey);
    friendCounts.set(friendship.userId, (friendCounts.get(friendship.userId) || 0) + 1);
  }

  for (const userId of userIds) {
    const candidates = shuffle(userIds.filter((id) => id !== userId));
    const targetCount = randomInt(0, Math.min(20, candidates.length));

    for (const friendUserId of candidates) {
      if (friendCounts.get(userId) >= targetCount) break;
      const pairKey = [userId, friendUserId].sort((a, b) => a - b).join(":");
      if (pairs.has(pairKey)) continue;

      pairs.add(pairKey);
      friendCounts.set(userId, friendCounts.get(userId) + 1);
      friendCounts.set(friendUserId, friendCounts.get(friendUserId) + 1);
      rows.push(
        { userId, friendUserId },
        { userId: friendUserId, friendUserId: userId },
      );
    }
  }

  await sequelize.transaction(async (transaction) => {
    await Friendship.bulkCreate(rows, {
      transaction,
      ignoreDuplicates: true,
    });
  });

  console.log(`已为 ${users.length} 个用户生成 ${pairs.size} 对好友关系`);
  console.log("每个用户最多 20 个好友，已存在的关系不会重复生成");
}

main()
  .catch((error) => {
    console.error("生成好友失败：", error.message);
    process.exitCode = 1;
  })
  .finally(async () => {
    await sequelize.close();
  });
