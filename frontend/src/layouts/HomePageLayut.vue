<template>
  <div class="home-page-layout">
    <HomePageMainContent />
    <HomePageSidebar />
  </div>
</template>

<script setup>
import HomePageMainContent from "@/components/HomePage/HomePageMainContent/HomePageCard.vue";
import HomePageSidebar from "@/components/HomePage/HomePageSidebar.vue";
</script>

<style scoped>
.home-page-layout {
  margin-top: 72px;
  display: flex;
  background-color: #9f9f9f;
}
</style>
