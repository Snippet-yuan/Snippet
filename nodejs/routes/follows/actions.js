/**
 * 关注 / 取消关注
 * POST   /:userId/follow   关注用户
 * DELETE /:userId/follow   取消关注（幂等）
 */

const express = require("express");
const { User, Follow } = require("../../models");
const { getUserId } = require("../../middleware/auth");
const { success, fail } = require("../../utils/response");
const { getFollowCounts } = require("./helpers");

const router = express.Router();

// 关注
router.post("/:userId/follow", async (req, res) => {
  try {
    const currentUserId = getUserId(req);
    const targetId = Number(req.params.userId);
    if (!Number.isInteger(targetId) || targetId <= 0) return res.status(400).json(fail("userId 无效", 400));
    if (currentUserId === targetId) return res.status(400).json(fail("不能关注自己", 400));

    const user = await User.findByPk(targetId, { attributes: ["id"] });
    if (!user) return res.status(404).json(fail("用户不存在", 404));

    const existing = await Follow.findOne({ where: { followerId: currentUserId, followedId: targetId } });
    if (existing) return res.status(409).json(fail("已关注该用户", 409));

    await Follow.create({ followerId: currentUserId, followedId: targetId });
    const counts = await getFollowCounts(targetId);

    res.status(201).json(success({ followedUserId: String(targetId), following: true, ...counts }, "关注成功"));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// 取消关注（幂等：重复取消仍返回成功）
router.delete("/:userId/follow", async (req, res) => {
  try {
    const currentUserId = getUserId(req);
    const targetId = Number(req.params.userId);
    if (!Number.isInteger(targetId) || targetId <= 0) return res.status(400).json(fail("userId 无效", 400));

    await Follow.destroy({ where: { followerId: currentUserId, followedId: targetId } });
    const counts = await getFollowCounts(targetId);

    res.json(success({ followedUserId: String(targetId), following: false, ...counts }, "已取消关注"));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

module.exports = router;
