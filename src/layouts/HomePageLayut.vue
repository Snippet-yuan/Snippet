<template>
  <div class="home-page-layout">
    <HomePageMainContent />
    <HomePageSidebar />
  </div>
</template>

<script setup>
import { ref } from "vue";
import HomePageMainContent from "@/components/HomePage/HomePageMainContent/HomePageCard.vue";
import HomePageSidebar from "@/components/HomePage/HomePageSidebar.vue";

const msg = ref("");
</script>

<style scoped>
.home-page-layout {
  margin-top: 72px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #9f9f9f;
  padding-left: 212px;
}
</style>
