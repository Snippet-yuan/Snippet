const BASE_URL = "http://localhost:8080/api/v1";

async function requestFavorite(postId, method) {
  const res = await fetch(`${BASE_URL}/posts/${postId}/favorite`, {
    method,
    headers: {
      Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
    },
  });

  const result = await res.json().catch(() => ({}));
  if (!res.ok || result.code !== 0) {
    throw new Error(
      result.message || (method === "POST" ? "收藏失败" : "取消收藏失败"),
    );
  }

  return result.data;
}

// 添加收藏的帖子
export function addFavorite(postId) {
  return requestFavorite(postId, "POST");
}

// 移除收藏的帖子
export function removeFavorite(postId) {
  return requestFavorite(postId, "DELETE");
}
