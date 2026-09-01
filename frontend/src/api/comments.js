const BASE_URL = "/api/v1";

function authHeaders() {
  return { Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}` };
}

/**
 * 获取某条帖子的评价列表
 * @param {string} postId
 * @param {{ page: number }} param1
 * @returns {Promise<{ items: import('@/models/dataModels').Comment[], hasMore: boolean }>}
 */
export async function fetchPostComments(postId, { page } = { page: 1 }) {
  const res = await fetch(`${BASE_URL}/posts/${postId}/comments?page=${page}`, {
    headers: authHeaders(),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok || data.code !== 0) throw new Error(data.message || "获取评价失败");
  return data;
}

/**
 * 评价某条帖子
 * @param {string} postId
 * @param {{ content: string }} param1
 * @returns {Promise<{ item: import('@/models/dataModels').Comment }>}
 */
export async function createComment(postId, { content }) {
  const res = await fetch(`${BASE_URL}/posts/${postId}/comments`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...authHeaders() },
    body: JSON.stringify({ content }),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok || data.code !== 0) throw new Error(data.message || "评价失败");
  return data;
}

/**
 * 删除自己的一条评价
 * @param {string} commentId
 */
export async function deleteComment(commentId) {
  const res = await fetch(`${BASE_URL}/comments/${commentId}`, {
    method: "DELETE",
    headers: authHeaders(),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok || data.code !== 0) throw new Error(data.message || "删除失败");
  return data;
}