<template>
  <nav class="nav-bar">
    <a href="#" class="snippet-link">
      <img src="/Snippet-120*120.svg" alt="Snippet logo" />
    </a>
    <RouterLink to="/" class="nav-link" v-slot="{ isExactActive }">
      <PhHouseLine :size="28" :weight="isExactActive ? 'fill' : 'regular'" />
    </RouterLink>
    <RouterLink to="/user/1" class="nav-link">
      <PhUserCircle :size="28" :weight="isUserActive ? 'fill' : 'regular'" />
    </RouterLink>
    <RouterLink to="/create" class="nav-link" v-slot="{ isExactActive }">
      <PhPlusSquare :size="28" :weight="isExactActive ? 'fill' : 'regular'" />
    </RouterLink>
    <RouterLink to="/message" class="nav-link" v-slot="{ isExactActive }">
      <PhChatTeardropDots
        :size="28"
        :weight="isExactActive ? 'fill' : 'regular'"
      />
    </RouterLink>
    <div class="nav-spacer"></div>
    <RouterLink
      to="/settings"
      class="nav-link settings-link"
      v-slot="{ isExactActive }"
    >
      <PhGear :size="28" :weight="isExactActive ? 'fill' : 'regular'" />
    </RouterLink>
  </nav>
</template>

<script setup>
import { useRoute } from "vue-router";
import { computed } from "vue";

import {
  PhHouseLine,
  PhPlusSquare,
  PhChatTeardropDots,
  PhUserCircle,
  PhGear,
} from "@phosphor-icons/vue";

const route = useRoute();

const isUserActive = computed(() => route.path.startsWith("/user/"));
</script>

<style scoped src="@/style/navBar.less"></style>
