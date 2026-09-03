const BASE_URL = "http://localhost:8080/api/v1";

function authHeaders() {
  return {
    Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
    "Content-Type": "application/json",
  };
}

async function parseResponse(response, fallbackMessage) {
  const result = await response.json().catch(() => ({}));
  if (!response.ok || result.code !== 0) {
    throw new Error(result.message || fallbackMessage);
  }
  return result.data;
}

export async function getFriendRequests() {
  const response = await fetch(`${BASE_URL}/users/me/friend-requests`, {
    headers: authHeaders(),
  });
  return parseResponse(response, "获取好友申请失败，请稍后重试");
}

export async function updateFriendRequest(requestId, action) {
  const response = await fetch(`${BASE_URL}/friend-requests/${requestId}`, {
    method: "PATCH",
    headers: authHeaders(),
    body: JSON.stringify({ action }),
  });
  return parseResponse(response, "处理好友申请失败，请稍后重试");
}
