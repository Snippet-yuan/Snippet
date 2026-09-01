const BASE_URL = "/api/v1";

function authHeaders() {
  return { Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}` };
}

// ============================================================
// 获取我发布的帖子（分页）
// ============================================================

/**
 * @param {{ page: number }} param0
 * @returns {Promise<{ items: import('@/models/dataModels').Post[], hasMore: boolean }>}
 */
export async function fetchMyPosts({ page } = { page: 1 }) {
  const res = await fetch(`${BASE_URL}/users/me/posts?page=${page}`, {
    headers: authHeaders(),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok || data.code !== 0) throw new Error(data.message || "获取帖子列表失败");
  return data;
}

// ============================================================
// 获取帖子详情
// ============================================================

/**
 * @param {string} postId
 * @returns {Promise<{ item: import('@/models/dataModels').Post }>}
 */
export async function fetchPostDetail(postId) {
  const res = await fetch(`${BASE_URL}/posts/${postId}`, {
    headers: authHeaders(),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok || data.code !== 0) throw new Error(data.message || "获取帖子详情失败");
  return data;
}

// ============================================================
// 创建帖子（支持多图上传）
// ============================================================

/**
 * @param {{ title: string, description?: string, images?: File[] }} payload
 * @returns {Promise<{ item: import('@/models/dataModels').Post }>}
 */
export async function createPost({ title, description = "", images = [] }) {
  const formData = new FormData();
  formData.append("title", title);
  formData.append("description", description);
  images.forEach((file) => formData.append("images", file));

  const res = await fetch(`${BASE_URL}/posts`, {
    method: "POST",
    headers: authHeaders(),
    body: formData,
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok || data.code !== 0) throw new Error(data.message || "发布失败，请稍后重试");
  return data;
}

// ============================================================
// 点赞 / 取消点赞
// ============================================================

/**
 * @param {string} postId
 * @param {{ liked: boolean }} param1
 * @returns {Promise<{ liked: boolean, counters: { likeCount: number } }>}
 */
export async function likePost(postId, { liked }) {
  const res = await fetch(`${BASE_URL}/posts/${postId}/like`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...authHeaders() },
    body: JSON.stringify({ liked: !liked }),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok || data.code !== 0) throw new Error(data.message || "操作失败");
  return data;
}

// ============================================================
// 收藏 / 取消收藏
// ============================================================

/**
 * @param {string} postId
 * @param {{ favorited: boolean }} param1
 * @returns {Promise<{ favorited: boolean, counters: { favoriteCount: number } }>}
 */
export async function favoritePost(postId, { favorited }) {
  const res = await fetch(`${BASE_URL}/posts/${postId}/favorite`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...authHeaders() },
    body: JSON.stringify({ favorited: !favorited }),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok || data.code !== 0) throw new Error(data.message || "操作失败");
  return data;
}

// ============================================================
// 转发
// ============================================================

/**
 * @param {string} postId
 * @returns {Promise<{ counters: { shareCount: number } }>}
 */
export async function sharePost(postId) {
  const res = await fetch(`${BASE_URL}/posts/${postId}/share`, {
    method: "POST",
    headers: authHeaders(),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok || data.code !== 0) throw new Error(data.message || "操作失败");
  return data;
}