<template>
  <div class="profile-layout">
    <div class="profile-body">
      <div class="profile-main">
        <div class="user-info">
          <div class="user-info-content">
            <UserInfo />
          </div>
        </div>

        <div class="snippet-list">
          <SnippetList />
        </div>
      </div>

      <aside class="friend-list">
        <ProfileAsideBarLayout />
      </aside>
    </div>

    <div class="footer">
      <Footer />
    </div>
  </div>
</template>

<script setup>
import SnippetList from "@/layouts/SnippetsTagLayout.vue";
import UserInfo from "@/components/Profile/UserInfo.vue";
import ProfileAsideBarLayout from "@/layouts/ProfileAsideBarLayout.vue";
import Footer from "@/components/Footer.vue";
</script>

<style scoped src="@/style/profileLayout.less"></style>
