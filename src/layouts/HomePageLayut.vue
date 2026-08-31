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
  padding: 0 20px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.1);
  margin: 0 20px;
}
</style>
