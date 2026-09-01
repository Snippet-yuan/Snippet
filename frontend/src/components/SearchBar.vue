<template>
  <div class="search-bar" :class="{ 'message-search-bar': isMessagePage }">
    <div class="search-box" @click="openDropdown">
      <PhMagnifyingGlass class="search-icon" :size="24" />
      <input
        v-model="keyword"
        type="text"
        placeholder="搜索"
        @keyup.enter="handleSearch"
        @keydown.esc="closeDropdown"
      />
      <PhEraser class="search-earser" :size="24" @click.stop="clearKeyword" />
      <PhCaretDown
        class="search-arrow"
        :class="{ 'is-open': showDropdown }"
        :size="24"
        @click.stop="toggleDropdown"
      />
      <transition name="dropdown">
        <div v-show="showDropdown" class="search-dropdown">
          <div
            v-for="item in categories"
            :key="item.value"
            class="dropdown-item"
            :class="{ active: activeCategory === item.value }"
            @click.stop="selectCategory(item)"
          >
            <component :is="item.icon" :size="18" class="dropdown-item-icon" />
            <span class="dropdown-item-label">{{ item.label }}</span>
            <PhCheck
              v-if="activeCategory === item.value"
              :size="16"
              class="dropdown-item-check"
            />
          </div>
        </div>
      </transition>
    </div>
    <RouterLink to="/user/1" class="search-avatar" title="我的主页">
      <img
        class="user-avator"
        src="@/assets/avatar/user-avatar.jpeg"
        alt="avatar"
      />
    </RouterLink>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import {
  PhMagnifyingGlass,
  PhEraser,
  PhCaretDown,
  PhCode,
  PhDatabase,
  PhDeviceMobile,
  PhPaintBrush,
  PhWrench,
  PhStar,
  PhDotsThree,
  PhCheck,
} from "@phosphor-icons/vue";

const route = useRoute();
const keyword = ref("");
const showDropdown = ref(false);
const activeCategory = ref("all");

const isMessagePage = computed(() => route.path.startsWith("/message"));

const categories = [
  { label: "全部", value: "all", icon: PhDotsThree },
  { label: "前端", value: "frontend", icon: PhCode },
  { label: "后端", value: "backend", icon: PhDatabase },
  { label: "移动端", value: "mobile", icon: PhDeviceMobile },
  { label: "设计", value: "design", icon: PhPaintBrush },
  { label: "工具", value: "tools", icon: PhWrench },
  { label: "收藏", value: "favorites", icon: PhStar },
];

function openDropdown() {
  showDropdown.value = true;
}

function toggleDropdown() {
  showDropdown.value = !showDropdown.value;
}

function closeDropdown() {
  showDropdown.value = false;
}

function selectCategory(item) {
  activeCategory.value = item.value;
  showDropdown.value = false;
}

function clearKeyword() {
  keyword.value = "";
}

function handleSearch() {
  showDropdown.value = false;
  // TODO: 跳转到搜索结果页
  console.log("搜索", keyword.value.trim());
}

function handleGlobalClick(e) {
  if (!e.target.closest(".search-bar")) {
    closeDropdown();
  }
}

onMounted(() => {
  document.addEventListener("click", handleGlobalClick);
});

onBeforeUnmount(() => {
  document.removeEventListener("click", handleGlobalClick);
});
</script>

<style scoped src="@/style/searchBar.less"></style>
