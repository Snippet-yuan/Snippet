//获取好友列表 用户id 获取好友列表
const BASE_URL = "http://localhost:8080/api/v1";

export async function getFriendsList() {
  const response = await fetch(`${BASE_URL}/users/me/friends`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
    },
  });

  const result = await response.json().catch(() => ({}));

  if (!response.ok || result.code !== 0) {
    throw new Error(result.message || "获取好友列表失败，请稍后重试");
  }

  return result.data;
}
