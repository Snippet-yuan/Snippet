const bcrypt = require("bcryptjs");
const { User } = require("../models");
const { signUserToken } = require("../utils/jwt");

function sanitizeUser(user) {
  return {
    id: String(user.id),
    email: user.email,
    nickname: user.nickname,
    avatar: user.avatar,
    background: user.background,
  };
}

async function register({ email, password, nickname }) {
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
    avatar: "",
    background: "",
  });

  const token = signUserToken(user);

  return {
    token,
    user: sanitizeUser(user),
  };
}

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

  return {
    token,
    user: sanitizeUser(user),
  };
}

module.exports = {
  register,
  login,
};
