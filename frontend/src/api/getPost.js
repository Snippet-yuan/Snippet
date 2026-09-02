const BASE_URL = "http://localhost:8080/api/v1/posts";

const getPosts = async () => {
  const response = await fetch(`${BASE_URL}`);
  return response.json();
};

export default getPosts;
