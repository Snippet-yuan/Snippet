<template>
  <HomePageLayout />
</template>

<script setup>
import { onMounted, onBeforeUnmount, nextTick } from "vue";
import { onBeforeRouteLeave } from "vue-router";
import HomePageLayout from "../layouts/HomePageLayut.vue";

const SCROLL_KEY = "snippet:explore:scrollY";
const SCROLL_PENDING_KEY = "snippet:explore:scrollY:pending";

let ticking = false;
function saveScroll() {
  const y = window.scrollY;
  sessionStorage.setItem(SCROLL_KEY, String(y));
  sessionStorage.setItem(SCROLL_PENDING_KEY, String(y));
}

function scheduleSaveScroll() {
  if (ticking) return;
  ticking = true;
  requestAnimationFrame(() => {
    saveScroll();
    ticking = false;
  });
}

function restoreScroll() {
  const pending = sessionStorage.getItem(SCROLL_PENDING_KEY);
  if (pending !== null) {
    sessionStorage.removeItem(SCROLL_PENDING_KEY);
    const y = Number(pending);
    if (!Number.isNaN(y) && y > 0) {
      nextTick(() => window.scrollTo(0, y));
      return;
    }
  }

  const saved = sessionStorage.getItem(SCROLL_KEY);
  if (saved !== null) {
    const y = Number(saved);
    if (!Number.isNaN(y) && y > 0) {
      nextTick(() => window.scrollTo(0, y));
    }
  }
}

onMounted(() => {
  restoreScroll();
  window.addEventListener("scroll", scheduleSaveScroll, { passive: true });
});

onBeforeUnmount(() => {
  saveScroll();
  window.removeEventListener("scroll", scheduleSaveScroll);
});

onBeforeRouteLeave(() => {
  saveScroll();
});
</script>

<style scoped></style>
