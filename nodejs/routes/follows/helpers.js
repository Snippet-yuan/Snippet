/**
 * 关注模块 - 共享辅助函数
 */

const { Op } = require("sequelize");
const { Follow } = require("../../models");

/**
 * 查询当前用户是否关注了某些用户，返回映射 { [followedId]: true }。
 * @param {number[]} followedIds
 * @param {number|string} currentUserId
 * @returns {Promise<Record<string, boolean>>}
 */
async function getFollowingMap(followedIds, currentUserId) {
  if (!followedIds.length) return {};
  const follows = await Follow.findAll({
    where: { followerId: currentUserId, followedId: { [Op.in]: followedIds } },
    attributes: ["followedId"],
  });
  const map = {};
  for (const f of follows) map[f.followedId] = true;
  return map;
}

/**
 * 获取某用户的关注数与粉丝数。
 * @param {number|string} targetId
 * @returns {Promise<{ followingCount: number, followerCount: number }>}
 */
async function getFollowCounts(targetId) {
  const [followingCount, followerCount] = await Promise.all([
    Follow.count({ where: { followerId: targetId } }),
    Follow.count({ where: { followedId: targetId } }),
  ]);
  return { followingCount, followerCount };
}

module.exports = { getFollowingMap, getFollowCounts };
