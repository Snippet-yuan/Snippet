<template>
  <section class="settings-card">
    <div class="card-header">
      <h2>邮箱</h2>
      <p>管理你的邮箱地址</p>
    </div>

    <div class="email-list">
      <div class="email-item">
        <span class="email-address">example@email.com</span>
        <div class="email-badges">
          <span class="email-badge primary">主要</span>
          <span class="email-badge verified">已验证</span>
        </div>
      </div>
    </div>

    <div class="form-row inline">
      <input
        v-model="form.newEmail"
        type="email"
        placeholder="添加新邮箱"
      />
      <button class="btn-secondary" type="button">添加</button>
    </div>
  </section>
</template>

<script setup>
import { reactive } from "vue";

const form = reactive({
  newEmail: "",
});
</script>

<style scoped src="@/style/Settings/Email.css"></style>
