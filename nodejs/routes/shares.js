const express = require("express");
const jwt = require("jsonwebtoken");
const { Post } = require("../models");
const { JWT_SECRET } = require("../utils/jwt");
const { success, fail } = require("../utils/response");

const router = express.Router();

function getUserId(req) {
  const authorization = req.get("Authorization") || "";
  const token = authorization.startsWith("Bearer ") ? authorization.slice(7) : "";
  if (!token) {
    const error = new Error("请先登录");
    error.statusCode = 401;
    throw error;
  }

  try {
    return Number(jwt.verify(token, JWT_SECRET).id);
  } catch {
    const error = new Error("登录已过期，请重新登录");
    error.statusCode = 401;
    throw error;
  }
}

function parsePostId(value) {
  const postId = Number(value);
  return Number.isInteger(postId) && postId > 0 ? postId : null;
}

router.post("/:postId/share", async (req, res) => {
  try {
    const userId = getUserId(req);
    const postId = parsePostId(req.params.postId);
    if (!postId) return res.status(400).json(fail("postId 无效", 400));

    const post = await Post.findByPk(postId, { attributes: ["id", "shareCount"] });
    if (!post) return res.status(404).json(fail("帖子不存在", 404));

    const target = typeof req.body?.target === "string" ? req.body.target.trim() : "copy_link";
    const allowedTargets = ["copy_link", "message", "external"];
    if (!allowedTargets.includes(target)) {
      return res.status(400).json(fail("target 必须是 copy_link、message 或 external", 400));
    }

    await post.increment("shareCount");
    const updatedPost = await Post.findByPk(postId, { attributes: ["shareCount"] });
    res.json(success({
      postId: String(postId),
      sharedBy: String(userId),
      target,
      shareCount: updatedPost.shareCount,
      shareUrl: `${req.protocol}://${req.get("host")}/posts/${postId}`,
    }, "转发成功"));
  } catch (error) {
    const status = error.statusCode || 500;
    res.status(status).json(fail(error.message || "转发失败，请稍后重试", status));
  }
});

module.exports = router;
