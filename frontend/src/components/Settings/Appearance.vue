<template>
  <section class="settings-card">
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
</template>

<script setup>
import { reactive } from "vue";

const themes = [
  { value: "light", label: "浅色" },
  { value: "dark", label: "深色" },
  { value: "system", label: "跟随系统" },
];

const form = reactive({
  theme: "system",
});
</script>

<style scoped src="@/style/Settings/Appearance.css"></style>
