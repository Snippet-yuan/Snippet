const BASE_URL = "http://localhost:8080/api/v1";

export async function addFollow(userId) {
  const response = await fetch(`${BASE_URL}/users/${userId}/follow`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
    },
  });

  const result = await response.json().catch(() => ({}));

  if (result.code !== 0 || !response.ok) {
    throw new Error(result.message || "添加关注失败");
  }

  return result;
}

export async function deleteFollow(userId) {
  const response = await fetch(`${BASE_URL}/users/${userId}/follow`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
    },
  });

  const result = await response.json().catch(() => ({}));

  if (result.code !== 0 || !response.ok) {
    throw new Error(result.message || "取消关注失败");
  }

  return result;
}
