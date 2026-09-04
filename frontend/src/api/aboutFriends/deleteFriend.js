const BASE_URL = "http://localhost:8080/api/v1";

/**
 * 删除好友（按用户 ID，双向解除好友关系）
 * @param {string} userId
 * @returns {Promise<object>}
 */
export async function deleteFriend(userId) {
  const response = await fetch(`${BASE_URL}/friends/${userId}`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
    },
  });

  const result = await response.json().catch(() => ({}));

  if (!response.ok || result.code !== 0) {
    throw new Error(result.message || "删除好友失败，请稍后重试");
  }

  return result.data;
}
