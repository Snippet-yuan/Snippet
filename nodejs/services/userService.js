/**
 * 用户服务
 * 负责注册 / 登录的核心业务逻辑：参数校验、查重、密码加解密、签发 token。
 * 由 routes/auth.js 调用，保持路由层轻薄。
 */

const bcrypt = require("bcryptjs");
const { User } = require("../models");
const { signUserToken } = require("../utils/jwt");

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/**
 * 将用户记录脱敏为对外返回结构。
 * @param {import("sequelize").Model} user
 */
function sanitizeUser(user) {
  return {
    id: String(user.id),
    email: user.email,
    nickname: user.nickname,
    bio: user.bio,
    avatar: user.avatar,
    createdAt: user.createdAt ? user.createdAt.toISOString() : "",
    background: user.background,
  };
}

// ---------------------------------------------------------------------------
// 业务方法
// ---------------------------------------------------------------------------

/**
 * 注册新用户。
 * @param {{ email: string, password: string, nickname: string }} params
 * @returns {Promise<{ token: string, user: object }>}
 * @throws {Error} 400 参数缺失 / 409 邮箱已注册
 */
async function register({ email, password, nickname }) {
  // 统一做 trim / 小写，避免 " Test@Example.com " 产生脏数据
  email = typeof email === "string" ? email.trim().toLowerCase() : email;
  nickname = typeof nickname === "string" ? nickname.trim() : nickname;

  if (!email || !password || !nickname) {
    const err = new Error("email、password、nickname 不能为空");
    err.statusCode = 400;
    throw err;
  }

  const existed = await User.findOne({ where: { email } });
  if (existed) {
    const err = new Error("该邮箱已注册");
    err.statusCode = 409;
    throw err;
  }

  const hashedPassword = await bcrypt.hash(password, 10);
  const user = await User.create({
    email,
    password: hashedPassword,
    nickname,
    bio: "",
    avatar: "",
    background: "",
  });

  const token = signUserToken(user);
  return { token, user: sanitizeUser(user) };
}

/**
 * 登录。
 * @param {{ email: string, password: string }} params
 * @returns {Promise<{ token: string, user: object }>}
 * @throws {Error} 400 参数缺失 / 401 凭证错误
 */
async function login({ email, password }) {
  email = typeof email === "string" ? email.trim().toLowerCase() : email;

  if (!email || !password) {
    const err = new Error("email、password 不能为空");
    err.statusCode = 400;
    throw err;
  }

  const user = await User.findOne({ where: { email } });
  if (!user) {
    const err = new Error("邮箱或密码错误");
    err.statusCode = 401;
    throw err;
  }

  const ok = await bcrypt.compare(password, user.password);
  if (!ok) {
    const err = new Error("邮箱或密码错误");
    err.statusCode = 401;
    throw err;
  }

  const token = signUserToken(user);
  return { token, user: sanitizeUser(user) };
}

module.exports = { register, login };
