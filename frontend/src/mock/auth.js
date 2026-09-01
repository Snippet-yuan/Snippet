import Mock from "mockjs";

export async function mockLogin({ body }) {
  const { email, password } = JSON.parse(body || "{}");

  if (!email || !password) {
    return { code: 400, message: "请输入邮箱和密码" };
  }

  if (email === "test@qq.com" && password === "123456") {
    return Mock.mock({
      code: 0,
      message: "ok",
      token: "@guid",
      user: {
        id: "@id",
        name: "@cname",
        email,
        avatar: "@image('80x80', '#7c3aed', '#fff', 'S')",
      },
    });
  }

  return { code: 401, message: "邮箱或密码错误" };
}
