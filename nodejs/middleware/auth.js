/**
 * 认证中间件 / 工具
 * 统一从 Authorization 头解析 JWT 并返回当前用户 ID。
 * 保持与原有各 route 文件中 getUserId 行为完全一致。
 */

const jwt = require("jsonwebtoken");
const { JWT_SECRET } = require("../utils/jwt");

/**
 * 从请求头解析并验证 JWT，返回用户 ID。
 * @param {import("express").Request} req
 * @returns {number|string} 用户 ID
 * @throws {Error} 401 未登录或 token 过期
 */
function getUserId(req) {
  const authorization = req.get("Authorization") || "";
  const token = authorization.startsWith("Bearer ") ? authorization.slice(7) : "";

  if (!token) {
    const err = new Error("请先登录");
    err.statusCode = 401;
    throw err;
  }

  try {
    return jwt.verify(token, JWT_SECRET).id;
  } catch {
    const err = new Error("登录已过期，请重新登录");
    err.statusCode = 401;
    throw err;
  }
}

/**
 * Express 中间件：校验登录态，失败直接返回 401。
 * 通过后将 userId 挂到 req.userId 供后续处理器使用。
 */
function requireAuth(req, res, next) {
  try {
    req.userId = getUserId(req);
    next();
  } catch (err) {
    const status = err.statusCode || 401;
    const { fail } = require("../utils/response");
    res.status(status).json(fail(err.message, status));
  }
}

module.exports = { getUserId, requireAuth };
