<template>
  <Empty v-if="snippets.length === 0" message="暂无内容" />
  <div v-else class="tag-item-container">
    <TagItem v-for="item in snippets" :key="item.id" :item="item" />
  </div>
</template>

<script setup>
import { ref } from "vue";
import TagItem from "../TagItem.vue";
import Empty from "@/components/Empty.vue";
import bg1 from "@/assets/images/bg-1.jpg";
import bg2 from "@/assets/images/bg-2.jpg";
import bg3 from "@/assets/images/bg-3.jpg";
import bg4 from "@/assets/images/bg-4.jpg";
import bg5 from "@/assets/images/bg-5.jpg";
import bg6 from "@/assets/images/bg-6.jpg";
import bg7 from "@/assets/images/bg-7.jpg";
import avatarImg from "@/assets/avatar/user-avatar.jpeg";

const snippets = ref([
  {
    id: 1,
    cover: bg1,
    title: "标题 1",
    desc: "描述 1",
    likes: 0,
    comments: 0,
    avatar: avatarImg,
    userName: "用户名",
    userId: "1234567890",
  },
  {
    id: 2,
    cover: bg2,
    title: "标题 2",
    desc: "描述 2",
    likes: 0,
    comments: 0,
    avatar: avatarImg,
    userName: "用户名",
    userId: "1234567890",
  },
  {
    id: 3,
    cover: bg3,
    title: "标题 3",
    desc: "描述 3",
    likes: 0,
    comments: 0,
    avatar: avatarImg,
    userName: "用户名",
    userId: "1234567890",
  },
  {
    id: 4,
    cover: bg4,
    title: "标题 4",
    desc: "描述 4",
    likes: 0,
    comments: 0,
    avatar: avatarImg,
    userName: "用户名",
    userId: "1234567890",
  },
  {
    id: 5,
    cover: bg5,
    title: "标题 5",
    desc: "描述 5",
    likes: 0,
    comments: 0,
    avatar: avatarImg,
    userName: "用户名",
    userId: "1234567890",
  },
  {
    id: 6,
    cover: bg6,
    title: "标题 6",
    desc: "描述 6",
    likes: 0,
    comments: 0,
    avatar: avatarImg,
    userName: "用户名",
    userId: "1234567890",
  },
  {
    id: 7,
    cover: bg7,
    title: "标题 7",
    desc: "描述 7",
    likes: 0,
    comments: 0,
    avatar: avatarImg,
    userName: "用户名",
    userId: "1234567890",
  },
]);
</script>

<style scoped>
.tag-item-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  width: 600px;
  min-height: 422px;
  border: 1px solid black;
  border-right: none;
  border-bottom: none;
  /* padding-bottom: 91px; */
}

/* .tag-item-container :deep(.tag-item:nth-child(1)) {
  background-color: #fb3e3e;
} */
.tag-item-container :deep(.tag-item:nth-child(n + 2)) {
  background-color: #ffffff;
}
</style>
