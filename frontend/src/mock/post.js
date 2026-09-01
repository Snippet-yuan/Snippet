import Mock from "mockjs";

export async function mockCreatePost({ body }) {
  const formData = body instanceof FormData ? body : new FormData();
  const title = formData.get("title") || "";

  if (!title.trim()) {
    return { code: 400, message: "请输入帖子标题" };
  }

  const imageCount = formData.getAll("images").length;

  return Mock.mock({
    code: 0,
    message: "发布成功",
    item: {
      id: "@id",
      ownerId: () => localStorage.getItem("snippet_user_id") || "@id",
      ownerNickname: "@cname",
      ownerAvatar: "@image('80x80', '#7c3aed', '#fff', 'S')",
      title,
      description: formData.get("description") || "",
      images: Array.from({ length: imageCount }, () =>
        Mock.mock("@image('600x400', '#a78bfa', '#fff', 'P')"),
      ),
      counters: {
        likeCount: 0,
        favoriteCount: 0,
        commentCount: 0,
        shareCount: 0,
      },
      liked: false,
      favorited: false,
      createdAt: Mock.mock("@datetime"),
    },
  });
}