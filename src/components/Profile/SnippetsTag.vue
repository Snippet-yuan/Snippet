<template>
  <div class="tag-item-container">
    <div class="tag-item">
      <img class="cover-image" src="" alt="" />
      <div class="title">
        <h3>标题</h3>
        <p>描述</p>
      </div>
      <div class="footer">
        <span>点赞</span>
        <span>评论</span>
      </div>
      <div class="user-info">
        <img class="user-avatar" src="" alt="" />
        <span class="user-name">用户名</span>
        <span class="user-id">ID:1234567890</span>
      </div>
    </div>

    <div class="tag-item">
      <img class="cover-image" src="" alt="" />
      <div class="title">
        <h3>标题</h3>
        <p>描述</p>
      </div>
      <div class="footer">
        <span>点赞</span>
        <span>评论</span>
      </div>
      <div class="user-info">
        <img class="user-avatar" src="" alt="" />
        <span class="user-name">用户名</span>
        <span class="user-id">ID:1234567890</span>
      </div>
    </div>
    <div class="tag-item">
      <img class="cover-image" src="" alt="" />
      <div class="title">
        <h3>标题</h3>
        <p>描述</p>
      </div>
      <div class="footer">
        <span>点赞</span>
        <span>评论</span>
      </div>
      <div class="user-info">
        <img class="user-avatar" src="" alt="" />
        <span class="user-name">用户名</span>
        <span class="user-id">ID:1234567890</span>
      </div>
    </div>
    <div class="tag-item">
      <img class="cover-image" src="" alt="" />
      <div class="title">
        <h3>标题</h3>
        <p>描述</p>
      </div>
      <div class="footer">
        <span>点赞</span>
        <span>评论</span>
      </div>
      <div class="user-info">
        <img class="user-avatar" src="" alt="" />
        <span class="user-name">用户名</span>
        <span class="user-id">ID:1234567890</span>
      </div>
    </div>
    <div class="tag-item">
      <img class="cover-image" src="" alt="" />
      <div class="title">
        <h3>标题</h3>
        <p>描述</p>
      </div>
      <div class="footer">
        <span>点赞</span>
        <span>评论</span>
      </div>
      <div class="user-info">
        <img class="user-avatar" src="" alt="" />
        <span class="user-name">用户名</span>
        <span class="user-id">ID:1234567890</span>
      </div>
    </div>
    <div class="tag-item">
      <img class="cover-image" src="" alt="" />
      <div class="title">
        <h3>标题</h3>
        <p>描述</p>
      </div>
      <div class="footer">
        <span>点赞</span>
        <span>评论</span>
      </div>
      <div class="user-info">
        <img class="user-avatar" src="" alt="" />
        <span class="user-name">用户名</span>
        <span class="user-id">ID:1234567890</span>
      </div>
    </div>
    <div class="tag-item">
      <img class="cover-image" src="" alt="" />
      <div class="title">
        <h3>标题</h3>
        <p>描述</p>
      </div>
      <div class="footer">
        <span>点赞</span>
        <span>评论</span>
      </div>
      <div class="user-info">
        <img class="user-avatar" src="" alt="" />
        <span class="user-name">用户名</span>
        <span class="user-id">ID:1234567890</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";

const msg = ref("");
</script>

<style scoped>
.tag-item {
  box-sizing: border-box;
  height: 300px;
  border: 1px solid black;
  border-left: none;
  border-top: none;
}

.tag-item:nth-child(1) {
  background-color: #fb3e3e;
}
.tag-item:nth-child(n + 2) {
  background-color: #c5e7ff;
}

.tag-item-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  width: 600px;
  border: 1px solid black;
  border-right: none;
  border-bottom: none;
}
</style>
