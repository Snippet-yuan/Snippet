const BASE_URL = "http://localhost:8080/api/v1";

async function requestLike(postId, method) {
  const res = await fetch(`${BASE_URL}/posts/${postId}/like`, {
    method,
    headers: {
      Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
    },
  });

  const result = await res.json().catch(() => ({}));
  if (!res.ok || result.code !== 0) {
    throw new Error(
      result.message || (method === "POST" ? "点赞失败" : "取消点赞失败"),
    );
  }

  return result.data;
}

// 点赞帖子
export function addLike(postId) {
  return requestLike(postId, "POST");
}

// 移除点赞
export function removeLike(postId) {
  return requestLike(postId, "DELETE");
}
