/**
 * 文件上传配置
 * 集中管理头像 / 背景图 / 个人资料的 multer 配置，保持与原有 users.js 行为一致。
 */

const multer = require("multer");
const path = require("path");
const crypto = require("crypto");
const fs = require("fs");

// ---------------------------------------------------------------------------
// 目录初始化
// ---------------------------------------------------------------------------

const avatarDirectory = path.join(__dirname, "../uploads/avatars");
const backgroundDirectory = path.join(__dirname, "../uploads/backgrounds");

fs.mkdirSync(avatarDirectory, { recursive: true });
fs.mkdirSync(backgroundDirectory, { recursive: true });

// ---------------------------------------------------------------------------
// 通用校验
// ---------------------------------------------------------------------------

/**
 * 仅允许图片类型。
 */
function imageFileFilter(req, file, callback) {
  if (!file.mimetype.startsWith("image/")) {
    const err = new Error("请选择图片文件");
    err.statusCode = 400;
    return callback(err);
  }
  callback(null, true);
}

// ---------------------------------------------------------------------------
// 存储引擎
// ---------------------------------------------------------------------------

const avatarStorage = multer.diskStorage({
  destination: avatarDirectory,
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase() || ".jpg";
    cb(null, `${Date.now()}-${crypto.randomUUID()}${ext}`);
  },
});

const backgroundStorage = multer.diskStorage({
  destination: backgroundDirectory,
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase() || ".jpg";
    cb(null, `${Date.now()}-${crypto.randomUUID()}${ext}`);
  },
});

const profileStorage = multer.diskStorage({
  destination: (req, file, cb) => {
    if (file.fieldname === "avatar") return cb(null, avatarDirectory);
    if (file.fieldname === "background") return cb(null, backgroundDirectory);
    return cb(null, avatarDirectory);
  },
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase() || ".jpg";
    cb(null, `${Date.now()}-${crypto.randomUUID()}${ext}`);
  },
});

// ---------------------------------------------------------------------------
// Multer 实例
// ---------------------------------------------------------------------------

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

const profileUpload = multer({
  storage: profileStorage,
  limits: { fileSize: 10 * 1024 * 1024 },
  fileFilter: imageFileFilter,
});

module.exports = {
  avatarDirectory,
  backgroundDirectory,
  avatarUpload,
  backgroundUpload,
  profileUpload,
  imageFileFilter,
};
