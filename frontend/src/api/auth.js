const BASE_URL = "http://127.0.0.1:4523/m1/8784448-8574871-default";

// ============================================================
// 登录
// ============================================================

/**
 * @param {{ email: string, password: string }} param0
 * @returns {Promise<{ token: string, user: { id: string, email: string, nickname: string, avatar: string, background: string } }>}
 */
export async function login({ email, password }) {
  const res = await fetch(`${BASE_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });

  const result = await res.json().catch(() => ({}));

  if (!res.ok || result.code !== 0) {
    throw new Error(result.message || "登录失败，请稍后重试");
  }

  return result.data;
}

// ============================================================
// 注册
// ============================================================

/**
 * @param {{ email: string, password: string, nickname: string }} param0
 * @returns {Promise<{ token: string, user: { id: string, email: string, nickname: string, avatar: string, background: string } }>}
 */
export async function register({ email, password, nickname }) {
  const res = await fetch(`${BASE_URL}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password, nickname }),
  });

  const result = await res.json().catch(() => ({}));

  console.log(result);
  console.log(result.data);

  if (!res.ok || result.code !== 0) {
    throw new Error(result.message || "注册失败，请稍后重试");
  }

  return result.data;
}
