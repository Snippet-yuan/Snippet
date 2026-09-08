/**
 * 好友关系
 *  - POST   /friends              发送好友申请
 *  - DELETE /friends/:userId      删除好友（双向，幂等）
 *  - GET    /users/me/friend-requests  申请列表（收/发）
 *  - PATCH  /friend-requests/:id      处理申请（ACCEPT / REJECT）
 */

const express = require("express");
const { Op } = require("sequelize");
const { User, Friendship, FriendRequest } = require("../../models");
const { getUserId } = require("../../middleware/auth");
const { success, fail } = require("../../utils/response");
const { sanitizeFriendRequest } = require("../../utils/sanitize");

const router = express.Router();

// ---------------------------------------------------------------------------
// POST /friends — 发送好友申请
// ---------------------------------------------------------------------------
router.post("/friends", async (req, res) => {
  try {
    const senderId = getUserId(req);
    const receiverId = Number(req.body?.userId);
    if (!Number.isInteger(receiverId)) return res.status(400).json(fail("userId 无效", 400));
    if (receiverId === Number(senderId)) return res.status(400).json(fail("不能添加自己", 400));

    const receiver = await User.findByPk(receiverId);
    if (!receiver) return res.status(404).json(fail("用户不存在", 404));

    const existedFriendship = await Friendship.findOne({
      where: { userId: senderId, friendUserId: receiverId },
    });
    if (existedFriendship) return res.status(409).json(fail("已经是好友", 409));

    const existingRequest = await FriendRequest.findOne({
      where: {
        [Op.or]: [
          { senderId, receiverId },
          { senderId: receiverId, receiverId: senderId },
        ],
      },
      order: [["id", "DESC"]],
    });

    // 已有待处理申请
    if (existingRequest?.status === "PENDING") {
      if (Number(existingRequest.senderId) === Number(senderId)) {
        return res.status(409).json(fail("好友申请已发送，请等待对方同意", 409));
      }
      return res.status(409).json(fail("对方已向你发送好友申请，请在好友请求中处理", 409));
    }

    // 复用已拒绝/已接受的旧记录
    if (existingRequest) {
      existingRequest.senderId = senderId;
      existingRequest.receiverId = receiverId;
      existingRequest.status = "PENDING";
      await existingRequest.save();
      return res.json(success({ id: String(existingRequest.id), status: existingRequest.status }, "好友申请已重新发送"));
    }

    const request = await FriendRequest.create({ senderId, receiverId });
    res.status(201).json(success({ id: String(request.id), status: request.status }, "好友申请已发送，等待对方同意"));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// ---------------------------------------------------------------------------
// DELETE /friends/:userId — 删除好友（双向）
// ---------------------------------------------------------------------------
router.delete("/friends/:userId", async (req, res) => {
  try {
    const userId = Number(getUserId(req));
    const targetId = Number(req.params.userId);
    if (!Number.isInteger(targetId) || targetId <= 0) return res.status(400).json(fail("userId 无效", 400));
    if (userId === targetId) return res.status(400).json(fail("不能删除自己", 400));

    const destroyed = await Friendship.destroy({
      where: {
        [Op.or]: [
          { userId, friendUserId: targetId },
          { userId: targetId, friendUserId: userId },
        ],
      },
    });

    // 清理历史申请，避免删除后无法重新添加
    await FriendRequest.destroy({
      where: {
        [Op.or]: [
          { senderId: userId, receiverId: targetId },
          { senderId: targetId, receiverId: userId },
        ],
      },
    });

    if (!destroyed) return res.status(404).json(fail("你们还不是好友", 404));
    res.json(success({ userId: String(targetId), isFriend: false }, "已删除好友"));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message || "删除好友失败，请稍后重试", status));
  }
});

// ---------------------------------------------------------------------------
// GET /users/me/friend-requests — 申请列表
// ---------------------------------------------------------------------------
router.get("/users/me/friend-requests", async (req, res) => {
  try {
    const userId = getUserId(req);
    const [received, sent] = await Promise.all([
      FriendRequest.findAll({
        where: { receiverId: userId, status: "PENDING" },
        include: [{ model: User, as: "sender", attributes: ["id", "nickname", "avatar"] }],
        order: [["id", "DESC"]],
      }),
      FriendRequest.findAll({
        where: { senderId: userId, status: "PENDING" },
        include: [{ model: User, as: "receiver", attributes: ["id", "nickname", "avatar"] }],
        order: [["id", "DESC"]],
      }),
    ]);

    res.json(
      success({
        received: received.map((r) => sanitizeFriendRequest(r, "sender")),
        sent: sent.map((r) => sanitizeFriendRequest(r, "receiver")),
      })
    );
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// ---------------------------------------------------------------------------
// PATCH /friend-requests/:id — 处理申请
// ---------------------------------------------------------------------------
router.patch("/friend-requests/:id", async (req, res) => {
  try {
    const receiverId = getUserId(req);
    const request = await FriendRequest.findOne({
      where: { id: req.params.id, receiverId, status: "PENDING" },
      include: [{ model: User, as: "sender", attributes: ["id", "nickname", "avatar"] }],
    });
    if (!request) return res.status(404).json(fail("好友申请不存在或已处理", 404));

    const action = req.body?.action;
    if (!["ACCEPT", "REJECT"].includes(action)) {
      return res.status(400).json(fail("action 必须是 ACCEPT 或 REJECT", 400));
    }

    request.status = action === "ACCEPT" ? "ACCEPTED" : "REJECTED";
    await request.save();

    if (action === "ACCEPT") {
      await Friendship.findOrCreate({ where: { userId: request.senderId, friendUserId: receiverId } });
      await Friendship.findOrCreate({ where: { userId: receiverId, friendUserId: request.senderId } });
    }

    res.json(
      success({ id: String(request.id), status: request.status }, action === "ACCEPT" ? "好友申请已接受" : "好友申请已拒绝")
    );
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

module.exports = router;
