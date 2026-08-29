<template>
  <div class="settings-page">
    <h1 class="settings-title">设置</h1>

    <section class="settings-card">
      <h2>个人资料</h2>
      <label class="settings-row">
        <span>用户名</span>
        <input v-model="form.username" type="text" placeholder="请输入用户名" />
      </label>
      <label class="settings-row">
        <span>邮箱</span>
        <input v-model="form.email" type="email" placeholder="请输入邮箱" />
      </label>
      <label class="settings-row">
        <span>个人简介</span>
        <textarea v-model="form.bio" placeholder="介绍一下自己..."></textarea>
      </label>
    </section>

    <section class="settings-card">
      <h2>通知设置</h2>
      <div class="toggle-row">
        <span>新关注提醒</span>
        <label class="switch">
          <input v-model="form.notifyFollow" type="checkbox" />
          <span class="slider"></span>
        </label>
      </div>
      <div class="toggle-row">
        <span>评论回复提醒</span>
        <label class="switch">
          <input v-model="form.notifyComment" type="checkbox" />
          <span class="slider"></span>
        </label>
      </div>
      <div class="toggle-row">
        <span>系统公告</span>
        <label class="switch">
          <input v-model="form.notifySystem" type="checkbox" />
          <span class="slider"></span>
        </label>
      </div>
    </section>

    <section class="settings-card">
      <h2>账号安全</h2>
      <div class="settings-row">
        <span>修改密码</span>
        <button class="danger-btn" type="button" @click="handleChangePassword">
          去修改
        </button>
      </div>
      <div class="settings-row">
        <span>退出登录</span>
        <button class="danger-btn" type="button" @click="handleLogout">
          退出登录
        </button>
      </div>
    </section>

    <button class="save-btn" type="button" @click="handleSave">保存设置</button>
  </div>
</template>

<script setup>
import { reactive } from "vue";

const form = reactive({
  username: "小秋",
  email: "xiaoqiu@example.com",
  bio: "热爱分享代码的前端工程师，专注 Vue 与前端工程化",
  notifyFollow: true,
  notifyComment: true,
  notifySystem: false,
});

function handleSave() {
  // TODO: 对接保存接口
  console.log("保存设置", { ...form });
}

function handleChangePassword() {
  console.log("跳转到修改密码");
}

function handleLogout() {
  console.log("退出登录");
}
</script>

<style scoped src="@/style/settings.css"></style>