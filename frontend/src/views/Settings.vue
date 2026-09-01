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
        <UserProfile v-if="activeMenu === 'profile'" />
        <Appearance v-else-if="activeMenu === 'appearance'" />
        <Notification
          v-else-if="activeMenu === 'notifications'"
        />
        <Email v-else-if="activeMenu === 'emails'" />
        <Security v-else-if="activeMenu === 'password'" />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import {
  PhUser,
  PhPalette,
  PhBell,
  PhEnvelope,
  PhLockKey,
} from "@phosphor-icons/vue";
import UserProfile from "@/components/Settings/UserProfile.vue";
import Appearance from "@/components/Settings/Appearance.vue";
import Notification from "@/components/Settings/notification.vue";
import Email from "@/components/Settings/Email.vue";
import Security from "@/components/Settings/Security.vue";

const activeMenu = ref("profile");

const menuItems = [
  { key: "profile", label: "个人资料", icon: PhUser },
  { key: "appearance", label: "外观", icon: PhPalette },
  { key: "notifications", label: "通知", icon: PhBell },
  { key: "emails", label: "邮箱", icon: PhEnvelope },
  { key: "password", label: "账户与安全", icon: PhLockKey },
];
</script>

<style scoped src="@/style/settings.less"></style>
