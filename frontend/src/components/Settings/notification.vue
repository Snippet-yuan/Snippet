<template>
  <section class="settings-card">
    <div class="card-header">
      <h2>通知</h2>
      <p>选择你希望接收的通知类型</p>
    </div>

    <div
      v-for="item in notificationItems"
      :key="item.key"
      class="toggle-row"
    >
      <div>
        <span class="toggle-label">{{ item.label }}</span>
        <span class="toggle-desc">{{ item.desc }}</span>
      </div>
      <label class="switch">
        <input v-model="form[item.key]" type="checkbox" />
        <span class="slider"></span>
      </label>
    </div>
  </section>
</template>

<script setup>
import { reactive } from "vue";

const notificationItems = [
  {
    key: "notifyEmail",
    label: "邮件通知",
    desc: "通过电子邮件接收重要更新",
  },
  {
    key: "notifyWeb",
    label: "站内通知",
    desc: "在网页上显示通知提醒",
  },
  {
    key: "notifyMention",
    label: "@提及通知",
    desc: "有人提到你时发送通知",
  },
];

const form = reactive({
  notifyEmail: true,
  notifyWeb: true,
  notifyMention: true,
});
</script>

<style scoped src="@/style/Settings/notification.css"></style>
