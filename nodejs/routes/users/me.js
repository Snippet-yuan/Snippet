/**
 * 用户中心 - 当前用户资料与资源
 * 负责 /users/me 相关的全部接口：
 *  - GET    /users/me                查询当前用户
 *  - POST   /users/me/avatar         上传头像
 *  - POST   /users/me/background     上传背景图
 *  - PATCH  /users/me                更新资料（JSON / multipart 二合一）
 *  - GET    /users/me/favorites      当前用户收藏列表
 *  - GET    /users/me/posts          当前用户帖子列表
 */

const express = require("express");
const { User, Post, PostFavorite } = require("../../models");
const { getUserId } = require("../../middleware/auth");
const { success, fail } = require("../../utils/response");
const { sanitizeUser } = require("../../utils/sanitize");
const { avatarUpload, backgroundUpload, profileUpload } = require("../../utils/upload");

const router = express.Router();

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/**
 * 判断是否为“清空”哨兵值。
 * 前端清空字段时会传 null / "__NULL__" / "null"。
 */
function isNullSentinel(value) {
  return value === null || value === "__NULL__" || value === "null";
}

/**
 * 根据 token 获取当前用户完整记录。
 * @throws {Error} 404 用户不存在
 */
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

/**
 * 将 Post 序列化为前端卡片结构（我的帖子/收藏共用）。
 */
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

// ---------------------------------------------------------------------------
// GET /users/me — 查询当前登录用户
// ---------------------------------------------------------------------------
router.get("/users/me", async (req, res) => {
  try {
    res.json(success(sanitizeUser(await getCurrentUser(req))));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// ---------------------------------------------------------------------------
// POST /users/me/avatar — 头像上传
// ---------------------------------------------------------------------------
router.post(
  "/users/me/avatar",
  (req, res, next) => {
    avatarUpload.single("avatar")(req, res, (err) => {
      if (err) {
        const status = err.code === "LIMIT_FILE_SIZE" ? 413 : err.statusCode || 400;
        return res
          .status(status)
          .json(fail(status === 413 ? "头像文件不能超过 5MB" : err.message, status));
      }
      next();
    });
  },
  async (req, res) => {
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
  }
);

// ---------------------------------------------------------------------------
// PATCH /users/me — 更新资料（支持 JSON 与 multipart）
// 字段：nickname / bio / avatar / background
// avatar 支持：文件上传 / http URL / base64 dataURL / null 清空
// ---------------------------------------------------------------------------
router.patch(
  "/users/me",
  (req, res, next) => {
    // 仅 multipart 请求走 multer
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
  },
  async (req, res) => {
    try {
      const user = await getCurrentUser(req);
      const baseUrl = `${req.protocol}://${req.get("host")}`;
      const fields = req.body || {};
      const files = req.files || {};

      const hasNickname = Object.prototype.hasOwnProperty.call(fields, "nickname");
      const hasBio = Object.prototype.hasOwnProperty.call(fields, "bio");
      const hasAvatarField = Object.prototype.hasOwnProperty.call(fields, "avatar");
      const hasBackgroundField = Object.prototype.hasOwnProperty.call(fields, "background");
      const avatarFile = files.avatar?.[0] || null;
      const backgroundFile = files.background?.[0] || null;

      // 至少传一个可更新字段
      if (!hasNickname && !hasBio && !hasAvatarField && !hasBackgroundField && !avatarFile && !backgroundFile) {
        return res.status(400).json(fail("没有可更新的字段", 400));
      }

      // 昵称
      if (hasNickname) {
        const raw = fields.nickname;
        if (isNullSentinel(raw)) return res.status(400).json(fail("昵称不能为空", 400));
        const trimmed = String(raw).trim();
        if (trimmed === "") return res.status(400).json(fail("昵称不能为空字符串", 400));
        if (trimmed.length > 30) return res.status(400).json(fail("昵称不能超过 30 个字符", 400));
        user.nickname = trimmed;
      }

      // 个性签名
      if (hasBio) {
        const raw = fields.bio;
        if (isNullSentinel(raw)) {
          user.bio = "";
        } else {
          const trimmed = String(raw).trim();
          if (trimmed.length > 200) return res.status(400).json(fail("个性签名不能超过 200 个字符", 400));
          user.bio = trimmed;
        }
      }

      // 头像：优先文件，其次字段
      if (avatarFile) {
        if (avatarFile.size > 5 * 1024 * 1024) return res.status(413).json(fail("头像不能超过 5MB", 413));
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
          if (raw.length > 5 * 1024 * 1024) return res.status(413).json(fail("头像不能超过 5MB", 413));
          user.avatar = raw;
        } else {
          return res.status(400).json(fail("头像格式无效", 400));
        }
      }

      // 背景图
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
  }
);

// ---------------------------------------------------------------------------
// POST /users/me/background — 背景图上传
// ---------------------------------------------------------------------------
router.post(
  "/users/me/background",
  (req, res, next) => {
    backgroundUpload.single("background")(req, res, (err) => {
      if (err) {
        const status = err.code === "LIMIT_FILE_SIZE" ? 413 : err.statusCode || 400;
        return res
          .status(status)
          .json(fail(status === 413 ? "背景图不能超过 10MB" : err.message, status));
      }
      next();
    });
  },
  async (req, res) => {
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
  }
);

// ---------------------------------------------------------------------------
// GET /users/me/favorites — 当前用户收藏列表（分页）
// ---------------------------------------------------------------------------
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

    const items = rows
      .filter((fav) => fav.post)
      .map((fav) => ({
        ...sanitizeMyPost(fav.post),
        favorited: true,
        favoritedAt: fav.createdAt ? fav.createdAt.toISOString() : "",
      }));

    res.json(success({ items, hasMore: page * limit < count, page, total: count }));
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message, status));
  }
});

// ---------------------------------------------------------------------------
// GET /users/me/posts — 当前用户帖子列表（分页）
// ---------------------------------------------------------------------------
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

module.exports = router;
