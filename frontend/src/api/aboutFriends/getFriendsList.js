//获取好友列表 用户id 获取好友列表
const BASE_URL = "http://127.0.0.1:4523/m1/8784448-8574871-default";

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

  console.log("123");
  console.log(result.data);

  return result.data;
}
