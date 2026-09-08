/**
 * JWT 工具
 * 提供签名密钥、有效期与签发方法。
 */

const jwt = require("jsonwebtoken");

const JWT_SECRET = process.env.JWT_SECRET || "snippet-secret-key";
const JWT_EXPIRES_IN = process.env.JWT_EXPIRES_IN || "7d";

/**
 * 为用户签发 JWT。
 * @param {{ id: number|string, email: string }} user
 * @returns {string} token
 */
function signUserToken(user) {
  return jwt.sign({ id: user.id, email: user.email }, JWT_SECRET, { expiresIn: JWT_EXPIRES_IN });
}

module.exports = { JWT_SECRET, JWT_EXPIRES_IN, signUserToken };
