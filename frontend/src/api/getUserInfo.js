const BASE_URL = "http://127.0.0.1:4523/m1/8784448-8574871-default";
//用户进入个人主页 获取个人信息和个人发布的帖子内容

export async function getUserInfo() {
  const response = await fetch(`${BASE_URL}/users/me`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${localStorage.getItem("snippet_token") || ""}`,
    },
  });

  const result = await response.json().catch(() => ({}));

  if (!response.ok || result.code !== 0) {
    throw new Error(result.message || "获取用户信息失败，请稍后重试");
  }

  return result.data;
}
