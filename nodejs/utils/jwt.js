const jwt = require("jsonwebtoken");

const JWT_SECRET = process.env.JWT_SECRET || "snippet-secret-key";
const JWT_EXPIRES_IN = process.env.JWT_EXPIRES_IN || "7d";

function signUserToken(user) {
  return jwt.sign(
    {
      id: user.id,
      email: user.email,
    },
    JWT_SECRET,
    { expiresIn: JWT_EXPIRES_IN }
  );
}

module.exports = { JWT_SECRET, signUserToken };
