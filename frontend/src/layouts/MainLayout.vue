<template>
  <NavBar />
  <SearchBar />

  <main>
    <RouterView />
  </main>
</template>

<script setup>
import NavBar from "@/components/NavBar.vue";
import SearchBar from "@/components/SearchBar.vue";
</script>

<style scoped src="@/style/mainLayout.less"></style>
