<template>
  <nav class="nav-bar">
    <a href="/" class="snippet-link">Snippet</a>
    <RouterLink to="/" class="nav-link">Explore</RouterLink>
    <RouterLink to="/user/1" class="nav-link">Profile</RouterLink>
    <RouterLink to="/create" class="nav-link">Create</RouterLink>
    <RouterLink to="/message" class="nav-link">Message</RouterLink>
    <RouterLink to="/settings" class="nav-link">Settings</RouterLink>
  </nav>
</template>

<script setup>
import "@/style/NavBar.css";
</script>

<style scoped src="@/style/NavBar.css"></style>
