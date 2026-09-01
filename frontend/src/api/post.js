const BASE_URL = "/api";

/**
 * 创建帖子（支持多图上传）
 * @param {{ title: string, description?: string, tags?: string[], images?: File[] }} payload
 * @returns {{ code: number, message: string, data?: { id: string } }}
 */
export async function createPost({ title, description = "", tags = [], images = [] }) {
  const formData = new FormData();
  formData.append("title", title);
  formData.append("description", description);
  formData.append("tags", JSON.stringify(tags));

  images.forEach((file) => {
    formData.append("images", file);
  });

  const res = await fetch(`${BASE_URL}/posts`, {
    method: "POST",
    headers: {
      // fetch 会自动设置 multipart boundary，不手动写 Content-Type
      Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
    },
    body: formData,
  });

  const data = await res.json().catch(() => ({}));

  if (!res.ok || data.code !== 0) {
    throw new Error(data.message || "发布失败，请稍后重试");
  }

  return data;
}
