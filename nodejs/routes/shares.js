/**
 * 转发 / 分享
 * POST /:postId/share — 记录转发并递增 shareCount
 */

const express = require("express");
const { Post } = require("../models");
const { getUserId } = require("../middleware/auth");
const { success, fail } = require("../utils/response");

const router = express.Router();

/**
 * 解析正整数 postId，非法返回 null。
 */
function parsePostId(value) {
  const id = Number(value);
  return Number.isInteger(id) && id > 0 ? id : null;
}

router.post("/:postId/share", async (req, res) => {
  try {
    const userId = getUserId(req);
    const postId = parsePostId(req.params.postId);
    if (!postId) return res.status(400).json(fail("postId 无效", 400));

    const post = await Post.findByPk(postId, { attributes: ["id", "shareCount"] });
    if (!post) return res.status(404).json(fail("帖子不存在", 404));

    // 转发目标：复制链接 / 私信 / 外部分享
    const target = typeof req.body?.target === "string" ? req.body.target.trim() : "copy_link";
    const allowed = ["copy_link", "message", "external"];
    if (!allowed.includes(target)) {
      return res.status(400).json(fail("target 必须是 copy_link、message 或 external", 400));
    }

    await post.increment("shareCount");
    const updated = await Post.findByPk(postId, { attributes: ["shareCount"] });

    res.json(
      success(
        {
          postId: String(postId),
          sharedBy: String(userId),
          target,
          shareCount: updated.shareCount,
          shareUrl: `${req.protocol}://${req.get("host")}/posts/${postId}`,
        },
        "转发成功"
      )
    );
  } catch (err) {
    const status = err.statusCode || 500;
    res.status(status).json(fail(err.message || "转发失败，请稍后重试", status));
  }
});

module.exports = router;
