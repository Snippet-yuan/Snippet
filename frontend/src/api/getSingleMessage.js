const BASE_URL = "http://localhost:8080/api/v1";

export async function getSingleMessage(conversationId, page = 1, limit = 30) {
  const response = await fetch(
    `${BASE_URL}/users/me/conversations/${conversationId}/messages?page=${page}&limit=${limit}`,
    {
      method: "GET",
      headers: {
        Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
      },
    },
  );
  const result = await response.json().catch(() => ({}));

  if (!response.ok || result.code !== 0) {
    throw new Error(result.message || "获取聊天记录失败，请稍后重试");
  }

  return result.data;
}
