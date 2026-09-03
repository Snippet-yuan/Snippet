const BASE_URL = "http://localhost:8080/api/v1";

export async function sendComment(postId, content) {
  const res = await fetch(`${BASE_URL}/posts/${postId}/comments`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ content }),
  });

  const result = await res.json().catch(() => ({}));

  if (!res.ok || result.code !== 0) {
    throw new Error(result.message || "发送评论失败");
  }
  return result.data;
}
