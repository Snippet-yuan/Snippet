<template>
  <div class="settings-page">
    <header class="page-header">
      <h1>设置</h1>
      <p>管理你的个人资料、偏好和账户</p>
    </header>

    <div class="settings-layout">
      <aside class="settings-sidebar">
        <nav class="sidebar-nav">
          <button
            v-for="item in menuItems"
            :key="item.key"
            class="sidebar-item"
            :class="{ active: activeMenu === item.key }"
            @click="activeMenu = item.key"
          >
            <component :is="item.icon" :size="20" />
            <span>{{ item.label }}</span>
          </button>
        </nav>
      </aside>

      <main class="settings-content">
        <!-- 个人资料 -->
        <section v-if="activeMenu === 'profile'" class="settings-card">
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
              <div
                v-for="i in 3"
                :key="i"
                class="social-input"
              >
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

        <!-- 外观 -->
        <section v-else-if="activeMenu === 'appearance'" class="settings-card">
          <div class="card-header">
            <h2>外观</h2>
            <p>自定义你的界面风格</p>
          </div>

          <div class="form-row">
            <label>主题</label>
            <div class="theme-grid">
              <label
                v-for="t in themes"
                :key="t.value"
                class="theme-option"
                :class="{ active: form.theme === t.value }"
              >
                <input v-model="form.theme" type="radio" :value="t.value" />
                <span>{{ t.label }}</span>
              </label>
            </div>
          </div>
        </section>

        <!-- 通知 -->
        <section
          v-else-if="activeMenu === 'notifications'"
          class="settings-card"
        >
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

        <!-- 邮箱 -->
        <section v-else-if="activeMenu === 'emails'" class="settings-card">
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

        <!-- 账户与安全 -->
        <section v-else-if="activeMenu === 'password'" class="settings-card">
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
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import {
  PhUser,
  PhPalette,
  PhBell,
  PhEnvelope,
  PhLockKey,
  PhLink,
  PhCamera,
} from "@phosphor-icons/vue";

const activeMenu = ref("profile");

const menuItems = [
  { key: "profile", label: "个人资料", icon: PhUser },
  { key: "appearance", label: "外观", icon: PhPalette },
  { key: "notifications", label: "通知", icon: PhBell },
  { key: "emails", label: "邮箱", icon: PhEnvelope },
  { key: "password", label: "账户与安全", icon: PhLockKey },
];

const themes = [
  { value: "light", label: "浅色" },
  { value: "dark", label: "深色" },
  { value: "system", label: "跟随系统" },
];

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
  name: "",
  bio: "",
  social1: "",
  social2: "",
  social3: "",
  username: "qiaoyuantao",
  theme: "system",
  notifyEmail: true,
  notifyWeb: true,
  notifyMention: true,
  newEmail: "",
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});
</script>

<style scoped src="@/style/settings.less"></style>
