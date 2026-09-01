<template>
  <div>
    <div class="user-info">
      <div class="friend-list">
        <ProfileAsideBarLayout />
      </div>

      <div class="user-info-content">
        <UserInfo />
      </div>
    </div>

    <div class="snippet-list">
      <SnippetList />
    </div>

    <div class="footer">
      <Footer />
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import SnippetList from "@/layouts/SnippetsTagLayout.vue";
import UserInfo from "@/components/Profile/UserInfo.vue";
import ProfileAsideBarLayout from "@/layouts/ProfileAsideBarLayout.vue";
import Footer from "@/components/Footer.vue";

const msg = ref("");
</script>

<style scoped src="@/style/profileLayout.less"></style>
