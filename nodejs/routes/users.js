const express = require("express");
const multer = require("multer");
const path = require("path");
const crypto = require("crypto");
const fs = require("fs");
const { Op } = require("sequelize");
const { User, Friendship, FriendRequest, Conversation, Message, Post, PostFavorite } = require("../models");
const { JWT_SECRET } = require("../utils/jwt");
const { success, fail } = require("../utils/response");
const jwt = require("jsonwebtoken");

const router = express.Router();
const usersProfileRouter = require("./users/profile");

const avatarDirectory = path.join(__dirname, "../uploads/avatars");
const backgroundDirectory = path.join(__dirname, "../uploads/backgrounds");
fs.mkdirSync(avatarDirectory, { recursive: true });
fs.mkdirSync(backgroundDirectory, { recursive: true });
const avatarStorage = multer.diskStorage({
  destination: avatarDirectory,
  filename: (req, file, callback) => {
    const extension = path.extname(file.originalname).toLowerCase() || ".jpg";
    callback(null, `${Date.now()}-${crypto.randomUUID()}${extension}`);
  },
});
const backgroundStorage = multer.diskStorage({
  destination: backgroundDirectory,
  filename: (req, file, callback) => {
    const extension = path.extname(file.originalname).toLowerCase() || ".jpg";
    callback(null, `${Date.now()}-${crypto.randomUUID()}${extension}`);
  },
});
function imageFileFilter(req, file, callback) {
    if (!file.mimetype.startsWith("image/")) {
      const error = new Error("请选择图片文件");
      error.statusCode = 400;
      return callback(error);
    }
    callback(null, true);
}
const avatarUpload = multer({
  storage: avatarStorage,
  limits: { fileSize: 5 * 1024 * 1024 },
  fileFilter: imageFileFilter,
});
const backgroundUpload = multer({
  storage: backgroundStorage,
  limits: { fileSize: 10 * 1024 * 1024 },
  fileFilter: imageFileFilter,
});
const profileStorage = multer.diskStorage({
  destination: (req, file, cb) => {
    if (file.fieldname === "avatar") return cb(null, avatarDirectory);
    if (file.fieldname === "background") return cb(null, backgroundDirectory);
    return cb(null, avatarDirectory);
  },
  filename: (req, file, cb) => {
    const extension = path.extname(file.originalname).toLowerCase() || ".jpg";
    cb(null, `${Date.now()}-${crypto.randomUUID()}${extension}`);
  },
});
const profileUpload = multer({
  storage: profileStorage,
  limits: { fileSize: 10 * 1024 * 1024 },
  fileFilter: imageFileFilter,
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
    bio: user.bio,
    avatar: user.avatar,
    background: user.background,
    createdAt: user.createdAt ? user.createdAt.toISOString() : "",
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

function sanitizeFriendRequest(request, userKey) {
  const user = request[userKey];
  return {
    id: String(request.id),
    userId: String(user.id),
    nickname: user.nickname,
    avatar: user.avatar,
    status: request.status,
    createdAt: request.createdAt ? request.createdAt.toISOString() : "",
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

function sanitizeMyPost(post) {
  return {
    id: String(post.id),
    ownerId: String(post.ownerId),
    ownerNickname: post.owner?.nickname || "",
    ownerAvatar: post.owner?.avatar || "",
    title: post.title,
    description: post.description,
    images: Array.isArray(post.images) ? post.images : [],
    counters: {
      likeCount: post.likeCount || 0,
      favoriteCount: post.favoriteCount || 0,
      commentCount: post.commentCount || 0,
      shareCount: post.shareCount || 0,
    },
    liked: false,
    favorited: false,
    createdAt: post.createdAt,
  };
}

router.get("/users/me", async (req, res) => {
  try {
    res.json(success(sanitizeUser(await getCurrentUser(req))));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

router.post("/users/me/avatar", (req, res, next) => {
  avatarUpload.single("avatar")(req, res, (err) => {
    if (err) {
      const status = err.code === "LIMIT_FILE_SIZE" ? 413 : err.statusCode || 400;
      return res.status(status).json(fail(status === 413 ? "头像文件不能超过 5MB" : err.message, status));
    }
    next();
  });
}, async (req, res) => {
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

function isNullSentinel(value) {
  return value === null || value === "__NULL__" || value === "null";
}

router.patch("/users/me", (req, res, next) => {
  const contentType = req.headers["content-type"] || "";
  if (contentType.includes("multipart/form-data")) {
    profileUpload.fields([
      { name: "avatar", maxCount: 1 },
      { name: "background", maxCount: 1 },
    ])(req, res, (err) => {
      if (err) {
        const status = err.code === "LIMIT_FILE_SIZE" ? 413 : err.statusCode || 400;
        const message = status === 413 ? "图片不能超过 10MB（头像 5MB/背景 10MB）" : err.message;
        return res.status(status).json(fail(message, status));
      }
      next();
    });
  } else {
    next();
  }
}, async (req, res) => {
  try {
    const user = await getCurrentUser(req);
    const baseUrl = `${req.protocol}://${req.get("host")}`;
    const isMultipart = (req.headers["content-type"] || "").includes("multipart/form-data");
    const fields = req.body || {};
    const files = req.files || {};

    const hasNickname = Object.prototype.hasOwnProperty.call(fields, "nickname");
    const hasBio = Object.prototype.hasOwnProperty.call(fields, "bio");
    const hasAvatarField = Object.prototype.hasOwnProperty.call(fields, "avatar");
    const hasBackgroundField = Object.prototype.hasOwnProperty.call(fields, "background");
    const avatarFile = files.avatar?.[0] || null;
    const backgroundFile = files.background?.[0] || null;

    if (!hasNickname && !hasBio && !hasAvatarField && !hasBackgroundField && !avatarFile && !backgroundFile) {
      return res.status(400).json(fail("没有可更新的字段", 400));
    }

    if (hasNickname) {
      const raw = fields.nickname;
      if (isNullSentinel(raw)) {
        return res.status(400).json(fail("昵称不能为空", 400));
      }
      const trimmed = String(raw).trim();
      if (trimmed === "") {
        return res.status(400).json(fail("昵称不能为空字符串", 400));
      }
      if (trimmed.length > 30) {
        return res.status(400).json(fail("昵称不能超过 30 个字符", 400));
      }
      user.nickname = trimmed;
    }

    if (hasBio) {
      const raw = fields.bio;
      if (isNullSentinel(raw)) {
        user.bio = "";
      } else {
        const trimmed = String(raw).trim();
        if (trimmed.length > 200) {
          return res.status(400).json(fail("个性签名不能超过 200 个字符", 400));
        }
        user.bio = trimmed;
      }
    }

    if (avatarFile) {
      if (avatarFile.size > 5 * 1024 * 1024) {
        return res.status(413).json(fail("头像不能超过 5MB", 413));
      }
      user.avatar = `${baseUrl}/uploads/avatars/${avatarFile.filename}`;
    } else if (hasAvatarField) {
      const raw = fields.avatar;
      if (isNullSentinel(raw)) {
        user.avatar = "";
      } else if (typeof raw === "string" && raw.trim() === "") {
        return res.status(400).json(fail("头像不能为空字符串，请传 null 清空或上传文件", 400));
      } else if (typeof raw === "string" && raw.trim().startsWith("http")) {
        user.avatar = raw.trim();
      } else if (typeof raw === "string" && /^data:image\/(jpeg|jpg|png|webp|gif);base64,/.test(raw)) {
        if (raw.length > 5 * 1024 * 1024) {
          return res.status(413).json(fail("头像不能超过 5MB", 413));
        }
        user.avatar = raw;
      } else {
        return res.status(400).json(fail("头像格式无效", 400));
      }
    }

    if (backgroundFile) {
      user.background = `${baseUrl}/uploads/backgrounds/${backgroundFile.filename}`;
    } else if (hasBackgroundField) {
      const raw = fields.background;
      if (isNullSentinel(raw)) {
        user.background = "";
      } else if (typeof raw === "string" && raw.trim() === "") {
        return res.status(400).json(fail("背景图不能为空字符串，请传 null 清空或上传文件", 400));
      } else if (typeof raw === "string") {
        user.background = String(raw).trim();
      } else {
        return res.status(400).json(fail("背景图格式无效", 400));
      }
    }

    await user.save();
    res.json(success(sanitizeUser(user), "用户资料更新成功"));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

router.post("/users/me/background", (req, res, next) => {
  backgroundUpload.single("background")(req, res, (err) => {
    if (err) {
      const status = err.code === "LIMIT_FILE_SIZE" ? 413 : err.statusCode || 400;
      return res.status(status).json(fail(status === 413 ? "背景图不能超过 10MB" : err.message, status));
    }
    next();
  });
}, async (req, res) => {
  try {
    const user = await getCurrentUser(req);
    if (!req.file) return res.status(400).json(fail("请选择背景图片", 400));
    user.background = `${req.protocol}://${req.get("host")}/uploads/backgrounds/${req.file.filename}`;
    await user.save();
    res.json(success(sanitizeUser(user), "背景图更新成功"));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

router.get("/users/me/favorites", async (req, res) => {
  try {
    const userId = getUserId(req);
    const rawPage = Number(req.query.page || 1);
    const rawLimit = Number(req.query.limit || 20);
    if (!Number.isInteger(rawPage) || rawPage < 1 || !Number.isInteger(rawLimit) || rawLimit < 1) {
      return res.status(400).json(fail("page 或 limit 无效", 400));
    }

    const page = rawPage;
    const limit = Math.min(50, rawLimit);
    const { rows, count } = await PostFavorite.findAndCountAll({
      where: { userId },
      order: [["id", "DESC"]],
      limit,
      offset: (page - 1) * limit,
      include: [
        {
          model: Post,
          as: "post",
          include: [{ model: User, as: "owner", attributes: ["id", "nickname", "avatar"] }],
        },
      ],
    });

    const items = rows.filter((favorite) => favorite.post).map((favorite) => ({
      ...sanitizeMyPost(favorite.post),
      favorited: true,
      favoritedAt: favorite.createdAt ? favorite.createdAt.toISOString() : "",
    }));

    res.json(success({
      items,
      hasMore: page * limit < count,
      page,
      total: count,
    }));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

router.get("/users/me/posts", async (req, res) => {
  try {
    const userId = getUserId(req);
    const page = Math.max(1, parseInt(req.query.page, 10) || 1);
    const limit = Math.min(50, Math.max(1, parseInt(req.query.limit, 10) || 20));

    const { rows, count } = await Post.findAndCountAll({
      where: { ownerId: userId },
      order: [["id", "DESC"]],
      limit,
      offset: (page - 1) * limit,
      include: [{ model: User, as: "owner", attributes: ["id", "nickname", "avatar"] }],
    });

    const items = rows.map((post) => ({
      id: String(post.id),
      ownerId: String(post.ownerId),
      ownerNickname: post.owner?.nickname || "",
      ownerAvatar: post.owner?.avatar || "",
      title: post.title,
      description: post.description,
      images: Array.isArray(post.images) ? post.images : [],
      counters: {
        likeCount: post.likeCount || 0,
        favoriteCount: post.favoriteCount || 0,
        commentCount: post.commentCount || 0,
        shareCount: post.shareCount || 0,
      },
      liked: false,
      favorited: false,
      createdAt: post.createdAt,
    }));

    res.json(success({ items, hasMore: (page - 1) * limit + items.length < count, page, total: count }));
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
      order: [["sentAt", "DESC"], ["id", "DESC"]],
      limit,
      offset: (page - 1) * limit,
    });

    const messages = rows.reverse().map((message) => ({
      id: String(message.id),
      conversationId: String(message.conversationId),
      senderId: String(message.senderId),
      receiverId: String(message.receiverId),
      content: message.content,
      sentAt: message.sentAt ? message.sentAt.toISOString() : "",
      isMine: Number(message.senderId) === userId,
    }));
    const friend = Number(conversation.user1Id) === userId ? conversation.user2 : conversation.user1;

    res.json(success({
      conversationId: String(conversation.id),
      friend: friend ? {
        id: String(friend.id),
        nickname: friend.nickname,
        avatar: friend.avatar,
      } : null,
      items: messages,
      hasMore: page * limit < count,
      page,
      total: count,
    }));
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
    if (existingRequest?.status === "PENDING") {
      if (Number(existingRequest.senderId) === Number(senderId)) {
        return res.status(409).json(fail("好友申请已发送，请等待对方同意", 409));
      }
      return res.status(409).json(fail("对方已向你发送好友申请，请在好友请求中处理", 409));
    }
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
    res.json(success({
      received: received.map((request) => sanitizeFriendRequest(request, "sender")),
      sent: sent.map((request) => sanitizeFriendRequest(request, "receiver")),
    }));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

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
    res.json(success({ id: String(request.id), status: request.status }, action === "ACCEPT" ? "好友申请已接受" : "好友申请已拒绝"));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

module.exports = router;
