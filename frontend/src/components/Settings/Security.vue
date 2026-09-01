<template>
  <section class="settings-card">
    <div class="card-header">
      <h2>账户与安全</h2>
      <p>更新密码或管理账户</p>
    </div>

    <div class="form-row">
      <label>用户名</label>
      <div class="inline-input">
        <input v-model="form.username" type="text" />
        <button class="btn-secondary" type="button">修改</button>
      </div>
    </div>

    <div class="form-row">
      <label>修改密码</label>
      <input
        v-model="form.oldPassword"
        type="password"
        placeholder="当前密码"
      />
      <input
        v-model="form.newPassword"
        type="password"
        class="mt-12"
        placeholder="新密码"
      />
      <input
        v-model="form.confirmPassword"
        type="password"
        class="mt-12"
        placeholder="确认新密码"
      />
      <button class="btn-primary mt-16" type="button">更新密码</button>
    </div>

    <div class="danger-zone">
      <h3>删除账户</h3>
      <p>删除后所有数据将无法恢复，请谨慎操作。</p>
      <button class="btn-danger" type="button">删除账户</button>
    </div>
  </section>
</template>

<script setup>
import { reactive } from "vue";

const form = reactive({
  username: "qiaoyuantao",
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});
</script>

<style scoped src="@/style/Settings/Security.css"></style>
