<script setup>
import { computed, onMounted, onUnmounted, ref } from "vue";
import {
  PhEnvelope,
  PhEnvelopeOpen,
  PhUserCirclePlus,
} from "@phosphor-icons/vue";
import AddFriends from "@/components/AddFriends.vue";
import { getFriendRequests, updateFriendRequest } from "@/api/aboutFriends/friendRequests";

defineProps({
  isScrolled: {
    type: Boolean,
    default: false,
  },
});

const showAddFriends = ref(false);

const isRequestListOpen = ref(false);

const receivedRequests = ref([]);
const sentRequests = ref([]);
const requestError = ref("");
const requestCount = computed(() => receivedRequests.value.length + sentRequests.value.length);

async function loadFriendRequests() {
  requestError.value = "";
  try {
    const data = await getFriendRequests();
    receivedRequests.value = data?.received || [];
    sentRequests.value = data?.sent || [];
  } catch (error) {
    requestError.value = error.message;
  }
}

async function handleRequest(request, action) {
  try {
    await updateFriendRequest(request.id, action);
    receivedRequests.value = receivedRequests.value.filter(({ id }) => id !== request.id);
  } catch (error) {
    requestError.value = error.message;
  }
}

function toggleRequestList() {
  isRequestListOpen.value = !isRequestListOpen.value;
  if (isRequestListOpen.value) loadFriendRequests();
}

function closeRequestList(event) {
  if (!event.target.closest(".request-wrapper")) {
    isRequestListOpen.value = false;
  }
}

onMounted(() => {
  loadFriendRequests();
  document.addEventListener("click", closeRequestList);
});

onUnmounted(() => {
  document.removeEventListener("click", closeRequestList);
});
</script>

<template>
  <div class="operation-bar" :class="{ 'is-scrolled': isScrolled }">
    <div class="request-wrapper">
      <button
        class="operation-button"
        type="button"
        aria-label="好友请求"
        :aria-expanded="isRequestListOpen"
        @click.stop="toggleRequestList"
      >
        <PhEnvelopeOpen v-if="isRequestListOpen" :size="32" />
        <PhEnvelope v-else :size="32" />
      </button>

      <div
        v-if="isRequestListOpen"
        class="request-dropdown"
        @click.stop
      >
        <div class="dropdown-header">
          <h3>好友请求</h3>
          <span>{{ requestCount }}</span>
        </div>

        <p v-if="requestError" class="request-error">{{ requestError }}</p>

        <section class="request-section">
          <p class="section-title">等待你接受</p>
          <p v-if="!receivedRequests.length" class="empty-request">暂无申请</p>
          <div
            v-for="request in receivedRequests"
            :key="request.id"
            class="request-item"
          >
            <img
              class="avatar avatar-image"
              :src="request.avatar || '/src/assets/avatar/user-avatar.jpeg'"
              :alt="request.nickname"
            />
            <div class="request-copy">
              <strong>{{ request.nickname }}</strong>
              <span>想和你成为好友</span>
            </div>
            <div class="request-actions">
              <button class="accept-button" type="button" @click="handleRequest(request, 'ACCEPT')">接受</button>
              <button class="reject-button" type="button" @click="handleRequest(request, 'REJECT')">拒绝</button>
            </div>
          </div>
        </section>

        <section class="request-section sent-section">
          <p class="section-title">已发送，等待对方同意</p>
          <p v-if="!sentRequests.length" class="empty-request">没有等待验证的好友申请</p>
          <div
            v-for="request in sentRequests"
            :key="request.id"
            class="request-item"
          >
            <img
              class="avatar avatar-image sent-avatar"
              :src="request.avatar || '/src/assets/avatar/user-avatar.jpeg'"
              :alt="request.nickname"
            />
            <div class="request-copy">
              <strong>{{ request.nickname }}</strong>
              <span>好友请求已发送，等待对方同意</span>
            </div>
            <span class="pending-label">等待中</span>
          </div>
        </section>
      </div>
    </div>

    <button class="operation-button" type="button" aria-label="添加好友" @click="showAddFriends = true">
      <PhUserCirclePlus :size="32" />
    </button>
    <AddFriends v-model="showAddFriends" />
  </div>
</template>

<style scoped src="@/style/operationBar.css"></style>
