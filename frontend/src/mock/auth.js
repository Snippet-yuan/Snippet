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
        email,
        nickname: "@cname",
        avatar: "@image('80x80', '#7c3aed', '#fff', 'S')",
        background: "@image('1200x600', '#4c1d95', '#fff', 'BG')",
      },
    });
  }

  return { code: 401, message: "邮箱或密码错误" };
}

export async function mockRegister({ body }) {
  const { email, password, nickname } = JSON.parse(body || "{}");

  if (!email || !password || !nickname) {
    return { code: 400, message: "请填写完整的注册信息" };
  }

  return Mock.mock({
    code: 0,
    message: "注册成功",
    token: "@guid",
    user: {
      id: "@id",
      email,
      nickname,
      avatar: "@image('80x80', '#7c3aed', '#fff', 'S')",
      background: "@image('1200x600', '#4c1d95', '#fff', 'BG')",
    },
  });
}