const BASE_URL = "http://localhost:8080/api/v1";

export async function getOthersPost(userId, page, limit) {
  const response = await fetch(
    `${BASE_URL}/users/${userId}/posts?page=${page}&limit=${limit}`,
    {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
      },
    },
  );
  const result = await response.json().catch(() => ({}));

  if (!response.ok || result.code !== 0) {
    throw new Error(result.message || "获取他人帖子失败");
  }

  return result.data;
}
