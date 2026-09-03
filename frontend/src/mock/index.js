import { mockCreatePost } from "./post";

// 模拟延迟，贴近真实网络环境
const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const routes = [
  { pathname: "/api/v1/posts", method: "POST", handler: mockCreatePost },
];

const realFetch = window.fetch.bind(window);

window.fetch = async (input, init = {}) => {
  const url = typeof input === "string" ? input : input.url;
  const method = (init.method || "GET").toUpperCase();
  const pathname = new URL(url, window.location.origin).pathname;

  const route = routes.find(
    (r) => r.method === method && pathname === r.pathname,
  );

  if (!route) {
    return realFetch(input, init);
  }

  const data = await route.handler({ url, method, body: init.body });
  await delay(200 + Math.random() * 300);

  return new Response(JSON.stringify(data), {
    status: 200,
    headers: { "Content-Type": "application/json" },
  });
};