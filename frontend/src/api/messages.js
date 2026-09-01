const BASE_URL = "/api/v1";

function authHeaders() {
  return { Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}` };
}

/**
 * 获取某个会话的聊天记录（分页，按时间倒序）
 * @param {string} conversationId
 * @param {{ page: number }} param1
 * @returns {Promise<{ items: import('@/models/dataModels').Message[], hasMore: boolean }>}
 */
export async function fetchMessages(conversationId, { page } = { page: 1 }) {
  const res = await fetch(
    `${BASE_URL}/conversations/${conversationId}/messages?page=${page}`,
    { headers: authHeaders() },
  );
  const data = await res.json().catch(() => ({}));
  if (!res.ok || data.code !== 0) throw new Error(data.message || "获取聊天记录失败");
  return data;
}

/**
 * 发送一条消息
 * @param {string} conversationId
 * @param {{ content: string }} param1
 * @returns {Promise<{ item: import('@/models/dataModels').Message }>}
 */
export async function sendMessage(conversationId, { content }) {
  const res = await fetch(`${BASE_URL}/conversations/${conversationId}/messages`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...authHeaders() },
    body: JSON.stringify({ content }),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok || data.code !== 0) throw new Error(data.message || "发送失败");
  return data;
}

/**
 * 获取会话列表（好友 + 最后一条消息 + 未读数）
 * @returns {Promise<{ items: Array<import('@/models/dataModels').Friend> }>}
 */
export async function fetchConversations() {
  const res = await fetch(`${BASE_URL}/conversations`, {
    headers: authHeaders(),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok || data.code !== 0) throw new Error(data.message || "获取会话列表失败");
  return data;
}