const express = require("express");
const multer = require("multer");
const path = require("path");
const crypto = require("crypto");
const { Op } = require("sequelize");
const { User, Friendship } = require("../models");
const { JWT_SECRET } = require("../utils/jwt");
const { success, fail } = require("../utils/response");
const jwt = require("jsonwebtoken");

const router = express.Router();
const avatarStorage = multer.diskStorage({
  destination: path.join(__dirname, "../uploads/avatars"),
  filename: (req, file, callback) => {
    const extension = path.extname(file.originalname).toLowerCase() || ".jpg";
    callback(null, `${Date.now()}-${crypto.randomUUID()}${extension}`);
  },
});
const avatarUpload = multer({
  storage: avatarStorage,
  limits: { fileSize: 5 * 1024 * 1024 },
  fileFilter: (req, file, callback) => {
    callback(null, file.mimetype.startsWith("image/"));
  },
});

function getUserId(req) {
  const authorization = req.get("Authorization") || "";
  const token = authorization.startsWith("Bearer ")
    ? authorization.slice(7)
    : "";

  if (!token) {
    const err = new Error("请先登录");
    err.statusCode = 401;
    throw err;
  }

  try {
    return jwt.verify(token, JWT_SECRET).id;
  } catch (err) {
    const authError = new Error("登录已过期，请重新登录");
    authError.statusCode = 401;
    throw authError;
  }
}

function sanitizeUser(user) {
  return {
    id: String(user.id),
    email: user.email,
    nickname: user.nickname,
    avatar: user.avatar,
    background: user.background,
  };
}

function sanitizeFriend(friendship) {
  return {
    id: String(friendship.id),
    userId: String(friendship.friendUserId),
    nickname: friendship.friend.nickname,
    avatar: friendship.friend.avatar,
    onlineStatus: "OFFLINE",
    lastMessage: friendship.lastMessage || "",
    unreadCount: friendship.unreadCount || 0,
  };
}

async function getCurrentUser(req) {
  const userId = getUserId(req);
  const user = await User.findByPk(userId);
  if (!user) {
    const err = new Error("用户不存在");
    err.statusCode = 404;
    throw err;
  }
  return user;
}

router.get("/users/me", async (req, res) => {
  try {
    res.json(success(sanitizeUser(await getCurrentUser(req))));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

router.post("/users/me/avatar", avatarUpload.single("avatar"), async (req, res) => {
  try {
    const user = await getCurrentUser(req);
    if (!req.file) return res.status(400).json(fail("请选择图片文件", 400));

    const baseUrl = `${req.protocol}://${req.get("host")}`;
    user.avatar = `${baseUrl}/uploads/avatars/${req.file.filename}`;
    await user.save();
    res.json(success(sanitizeUser(user), "头像更新成功"));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

router.patch("/users/me", async (req, res) => {
  try {
    const user = await getCurrentUser(req);
    const { avatar, nickname, background } = req.body || {};

    if (avatar !== undefined) {
      if (typeof avatar !== "string" || !/^data:image\/(jpeg|jpg|png|webp|gif);base64,/.test(avatar)) {
        return res.status(400).json(fail("头像格式无效", 400));
      }
      if (avatar.length > 5 * 1024 * 1024) {
        return res.status(413).json(fail("头像文件不能超过 5MB", 413));
      }
      user.avatar = avatar;
    }
    if (nickname !== undefined) user.nickname = String(nickname).trim();
    if (background !== undefined) user.background = String(background).trim();
    await user.save();
    res.json(success(sanitizeUser(user), "用户资料更新成功"));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

router.get("/users/search", async (req, res) => {
  try {
    getUserId(req);
    const keyword = String(req.query.keyword || "").trim();
    if (!keyword) return res.json(success([]));

    const users = await User.findAll({
      where: {
        [Op.or]: [
          { nickname: { [Op.like]: `%${keyword}%` } },
          ...(Number.isInteger(Number(keyword)) ? [{ id: Number(keyword) }] : []),
        ],
      },
      attributes: ["id", "nickname", "avatar"],
      limit: 20,
      order: [["id", "ASC"]],
    });
    res.json(
      success(
        users.map((user) => ({
          id: String(user.id),
          nickname: user.nickname,
          avatar: user.avatar,
        }))
      )
    );
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

router.get("/users/me/friends", async (req, res) => {
  try {
    const userId = getUserId(req);
    const page = Math.max(1, parseInt(req.query.page, 10) || 1);
    const limit = Math.min(50, Math.max(1, parseInt(req.query.limit, 10) || 20));
    const friendships = await Friendship.findAll({
      where: { userId },
      include: [{ model: User, as: "friend", attributes: ["id", "nickname", "avatar"] }],
      order: [["id", "DESC"]],
      limit,
      offset: (page - 1) * limit,
    });
    res.json(success({ items: friendships.map(sanitizeFriend), hasMore: friendships.length === limit, page }));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

router.post("/friends", async (req, res) => {
  try {
    const userId = getUserId(req);
    const friendUserId = Number(req.body?.userId);
    if (!Number.isInteger(friendUserId)) return res.status(400).json(fail("userId 无效", 400));
    if (friendUserId === Number(userId)) return res.status(400).json(fail("不能添加自己", 400));

    const friend = await User.findByPk(friendUserId);
    if (!friend) return res.status(404).json(fail("用户不存在", 404));
    const existed = await Friendship.findOne({ where: { userId, friendUserId } });
    if (existed) return res.status(409).json(fail("已经是好友", 409));

    const friendship = await Friendship.create({ userId, friendUserId });
    await Friendship.create({ userId: friendUserId, friendUserId: userId });
    friendship.friend = friend;
    res.json(success(sanitizeFriend(friendship), "添加好友成功"));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

module.exports = router;
