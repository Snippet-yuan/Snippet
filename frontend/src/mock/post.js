import Mock from "mockjs";

export async function mockCreatePost({ body }) {
  const formData = body instanceof FormData ? body : new FormData();
  const title = formData.get("title") || "";

  if (!title.trim()) {
    return { code: 400, message: "请输入帖子标题" };
  }

  return Mock.mock({
    code: 0,
    message: "发布成功",
    "data|1": [
      {
        id: "@id",
      },
    ],
  });
}
