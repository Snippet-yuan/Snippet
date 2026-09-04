const BASE_URL = "http://localhost:8080/api/v1";

export async function getPostOwnerInfo(userId) {
  const response = await fetch(`${BASE_URL}/users/${userId}`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
    },
  });

  const result = await response.json().catch(() => ({}));

  if (result.message !== "ok" || result.code !== 0) {
    throw new Error(result.message || "获取用户信息失败，请稍后重试");
  }

  return result.data;
}
window.getPostOwnerInfo = getPostOwnerInfo;
