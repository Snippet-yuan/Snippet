/**
 * 消息 / 会话
 *  - GET /users/me/friends                         好友列表（带会话预览）
 *  - GET /users/me/conversations/:id/messages      单会话消息分页
 */

const express = require("express");
const { Op } = require("sequelize");
const { User, Friendship, Conversation, Message } = require("../../models");
const { getUserId } = require("../../middleware/auth");
const { success, fail } = require("../../utils/response");
const { sanitizeFriend } = require("../../utils/sanitize");

const router = express.Router();

// ---------------------------------------------------------------------------
// GET /users/me/conversations/:conversationId/messages
// ---------------------------------------------------------------------------
router.get("/users/me/conversations/:conversationId/messages", async (req, res) => {
  try {
    const userId = Number(getUserId(req));
    const conversationId = Number(req.params.conversationId);
    if (!Number.isInteger(conversationId) || conversationId <= 0) {
      return res.status(400).json(fail("conversationId 无效", 400));
    }

    const conversation = await Conversation.findOne({
      where: {
        id: conversationId,
        [Op.or]: [{ user1Id: userId }, { user2Id: userId }],
      },
      include: [
        { model: User, as: "user1", attributes: ["id", "nickname", "avatar"] },
        { model: User, as: "user2", attributes: ["id", "nickname", "avatar"] },
      ],
    });
    if (!conversation) return res.status(404).json(fail("会话不存在或无权访问", 404));

    const rawPage = Number(req.query.page || 1);
    const rawLimit = Number(req.query.limit || 30);
    if (!Number.isInteger(rawPage) || rawPage < 1 || !Number.isInteger(rawLimit) || rawLimit < 1) {
      return res.status(400).json(fail("page 或 limit 无效", 400));
    }
    const page = rawPage;
    const limit = Math.min(100, rawLimit);

    const { rows, count } = await Message.findAndCountAll({
      where: { conversationId },
      order: [
        ["sentAt", "DESC"],
        ["id", "DESC"],
      ],
      limit,
      offset: (page - 1) * limit,
    });

    // 倒序查询后正序返回，并标记 isMine
    const messages = rows.reverse().map((m) => ({
      id: String(m.id),
      conversationId: String(m.conversationId),
      senderId: String(m.senderId),
      receiverId: String(m.receiverId),
      content: m.content,
      sentAt: m.sentAt ? m.sentAt.toISOString() : "",
      isMine: Number(m.senderId) === userId,
    }));

    const friend = Number(conversation.user1Id) === userId ? conversation.user2 : conversation.user1;

    res.json(
      success({
        conversationId: String(conversation.id),
        friend: friend ? { id: String(friend.id), nickname: friend.nickname, avatar: friend.avatar } : null,
        items: messages,
        hasMore: page * limit < count,
        page,
        total: count,
      })
    );
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// ---------------------------------------------------------------------------
// GET /users/me/friends — 好友列表（分页，附带会话 ID）
// ---------------------------------------------------------------------------
router.get("/users/me/friends", async (req, res) => {
  try {
    const userId = Number(getUserId(req));
    const page = Math.max(1, parseInt(req.query.page, 10) || 1);
    const limit = Math.min(50, Math.max(1, parseInt(req.query.limit, 10) || 20));

    const [friendships, conversations] = await Promise.all([
      Friendship.findAll({
        where: { userId },
        include: [{ model: User, as: "friend", attributes: ["id", "nickname", "avatar"] }],
        order: [["id", "DESC"]],
        limit,
        offset: (page - 1) * limit,
      }),
      Conversation.findAll({
        where: { [Op.or]: [{ user1Id: userId }, { user2Id: userId }] },
        attributes: ["id", "user1Id", "user2Id"],
      }),
    ]);

    // 每个好友对应一条会话（另一方）
    const conversationIdByFriend = new Map();
    for (const conv of conversations) {
      const friendUserId = Number(conv.user1Id) === userId ? conv.user2Id : conv.user1Id;
      if (!conversationIdByFriend.has(String(friendUserId))) {
        conversationIdByFriend.set(String(friendUserId), String(conv.id));
      }
    }

    res.json(
      success({
        items: friendships.map((fs) => ({
          ...sanitizeFriend(fs),
          conversationId: conversationIdByFriend.get(String(fs.friendUserId)) || null,
        })),
        hasMore: friendships.length === limit,
        page,
      })
    );
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

module.exports = router;
