const BASE_URL = "http://127.0.0.1:4523/m1/8784448-8574871-default";

/**
 * 搜索用户（支持昵称模糊搜索 / 用户 ID 精确搜索）
 * @param {string} keyword
 * @returns {Promise<Array<{ id: string, nickname: string, avatar: string }>>}
 */
export async function searchUsers(keyword) {
  const response = await fetch(
    `${BASE_URL}/users/search?keyword=${encodeURIComponent(keyword)}`,
    {
      method: "GET",
      headers: {
        Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
        "Content-Type": "application/json",
      },
    }
  );

  const result = await response.json().catch(() => ({}));

  if (!response.ok || result.code !== 0) {
    throw new Error(result.message || "搜索用户失败，请稍后重试");
  }

  const data = result.data || [];
  return Array.isArray(data) ? data : data.items || [];
}
