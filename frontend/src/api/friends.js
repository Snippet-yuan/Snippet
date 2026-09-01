const BASE_URL = "/api/v1";

function authHeaders() {
  return { Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}` };
}

/**
 * 获取我的好友列表（分页）
 * @param {{ page: number }} param0
 * @returns {Promise<{ items: import('@/models/dataModels').Friend[], hasMore: boolean }>}
 */
export async function fetchFriends({ page } = { page: 1 }) {
  const res = await fetch(`${BASE_URL}/users/me/friends?page=${page}`, {
    headers: authHeaders(),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok || data.code !== 0) throw new Error(data.message || "获取好友列表失败");
  return data;
}

/**
 * 添加好友
 * @param {{ userId: string }} param0
 */
export async function addFriend({ userId }) {
  const res = await fetch(`${BASE_URL}/friends`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...authHeaders() },
    body: JSON.stringify({ userId }),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok || data.code !== 0) throw new Error(data.message || "添加好友失败");
  return data;
}

/**
 * 删除好友
 * @param {string} friendId
 */
export async function removeFriend(friendId) {
  const res = await fetch(`${BASE_URL}/friends/${friendId}`, {
    method: "DELETE",
    headers: authHeaders(),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok || data.code !== 0) throw new Error(data.message || "删除好友失败");
  return data;
}