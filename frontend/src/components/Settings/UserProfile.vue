<template>
  <section class="settings-card">
    <div class="card-header">
      <h2>个人资料</h2>
      <p>编辑你的公开资料信息</p>
    </div>

    <div class="profile-preview">
      <div class="avatar-wrapper">
        <img
          src="https://avatars.githubusercontent.com/u/1?v=4"
          alt="avatar"
        />
        <button class="avatar-edit" type="button">
          <PhCamera :size="16" />
        </button>
      </div>
      <div class="profile-names">
        <h3>桥元涛</h3>
        <span>@qiaoyuantao</span>
      </div>
    </div>

    <div class="form-row">
      <label>显示名称</label>
      <input v-model="form.name" type="text" placeholder="你的名称" />
    </div>

    <div class="form-row">
      <label>个人简介</label>
      <textarea
        v-model="form.bio"
        rows="3"
        placeholder="写点什么介绍自己..."
      ></textarea>
    </div>

    <div class="form-row">
      <label>社交链接</label>
      <div class="social-stack">
        <div v-for="i in 3" :key="i" class="social-input">
          <PhLink :size="18" />
          <input
            v-model="form['social' + i]"
            type="text"
            placeholder="https://"
          />
        </div>
      </div>
    </div>

    <div class="card-footer">
      <button class="btn-primary" type="button">保存更改</button>
    </div>
  </section>
</template>

<script setup>
import { reactive } from "vue";
import { PhLink, PhCamera } from "@phosphor-icons/vue";

const form = reactive({
  name: "",
  bio: "",
  social1: "",
  social2: "",
  social3: "",
});
</script>

<style scoped src="@/style/Settings/UserProfile.css"></style>
