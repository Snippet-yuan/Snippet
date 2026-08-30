const BASE_URL = "/api";

/**
 * 登录(发送请求，返回数据)
 * @param {{ email: string, password: string }} { email, password }
 * @returns {{ token: string, user: { id: string, name: string, email: string, avatar: string } }}
 */
export async function login({ email, password }) {
  const res = await fetch(`${BASE_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });

  // 如果请求失败，返回空对象
  const data = await res.json().catch(() => ({}));

  // console.log(data);

  if (!res.ok || data.code !== 0) {
    throw new Error(data.message || "登录失败，请稍后重试");
  }

  return data;
}
