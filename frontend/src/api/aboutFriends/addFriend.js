const BASE_URL = "http://127.0.0.1:4523/m1/8784448-8574871-default";

/**
 * 通过用户 ID 添加好友
 * @param {string} userId
 * @returns {Promise<object>}
 */
export async function addFriend(userId) {
  const response = await fetch(`${BASE_URL}/friends`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ userId }),
  });

  const result = await response.json().catch(() => ({}));

  if (!response.ok || result.code !== 0) {
    throw new Error(result.message || "添加好友失败，请稍后重试");
  }

  return result.data;
}
