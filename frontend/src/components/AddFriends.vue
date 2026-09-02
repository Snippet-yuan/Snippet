<template>
  <Teleport to="body">
    <Transition name="add-friends">
      <div v-if="modelValue" class="add-friends-overlay" @click.self="close">
        <div class="add-friends-panel" role="dialog" aria-modal="true">
          <button class="modal-close-btn" @click="close">×</button>

          <div class="add-friends-header">
            <h2>添加好友</h2>
            <p>搜索昵称或输入用户 ID，找到你想认识的人</p>
          </div>

          <div class="add-friends-search">
            <PhMagnifyingGlass class="search-icon" :size="20" />
            <input
              v-model="keyword"
              type="text"
              placeholder="搜索昵称，或输入完整用户 ID"
              @input="handleInput"
              @keyup.enter="searchNow"
            />
            <button v-if="keyword" class="search-clear" @click="clearSearch">
              ×
            </button>
          </div>

          <div class="add-friends-body">
            <div v-if="loading" class="state-text">搜索中…</div>
            <div v-else-if="error" class="state-text error">{{ error }}</div>

            <div v-else-if="results.length" class="result-list">
              <div v-for="user in results" :key="user.id" class="user-item">
                <img
                  class="user-avatar"
                  :src="user.avatar || '/src/assets/avatar/user-avatar.jpeg'"
                  :alt="user.nickname"
                />
                <div class="user-info">
                  <span class="user-nickname">{{ user.nickname }}</span>
                  <span class="user-id">ID: {{ user.id }}</span>
                </div>
                <button
                  class="add-btn"
                  :disabled="
                    addStatus[user.id] === 'adding' ||
                    addStatus[user.id] === 'added'
                  "
                  @click="handleAdd(user)"
                >
                  {{
                    addStatus[user.id] === "added"
                      ? "已添加"
                      : addStatus[user.id] === "adding"
                        ? "添加中…"
                        : "添加"
                  }}
                </button>
              </div>
            </div>

            <div v-else-if="keyword.trim()" class="state-text">
              未找到匹配用户
            </div>
            <div v-else class="state-text hint">输入昵称或用户 ID 开始搜索</div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch, onUnmounted } from "vue";
import { PhMagnifyingGlass } from "@phosphor-icons/vue";
import { searchUsers } from "@/api/aboutFriends/searchUsers";
import { addFriend as addFriendApi } from "@/api/aboutFriends/addFriend";

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["update:modelValue", "added"]);

const keyword = ref("");
const results = ref([]);
const loading = ref(false);
const error = ref("");
const addStatus = ref({});

let searchTimer = null;
let escapeHandler = null;

function close() {
  emit("update:modelValue", false);
}

function clearSearch() {
  keyword.value = "";
  results.value = [];
  error.value = "";
}

function handleInput() {
  clearTimeout(searchTimer);
  error.value = "";

  if (!keyword.value.trim()) {
    results.value = [];
    return;
  }

  searchTimer = setTimeout(searchNow, 300);
}

async function searchNow() {
  clearTimeout(searchTimer);
  const q = keyword.value.trim();
  if (!q) return;

  loading.value = true;
  error.value = "";

  try {
    const data = await searchUsers(q);
    results.value = Array.isArray(data) ? data : data?.items || [];
  } catch (e) {
    error.value = e.message;
    results.value = [];
  } finally {
    loading.value = false;
  }
}

async function handleAdd(user) {
  addStatus.value[user.id] = "adding";

  try {
    await addFriendApi(user.id);
    addStatus.value[user.id] = "added";
    emit("added", user);
  } catch (e) {
    error.value = e.message;
    addStatus.value[user.id] = "";
  }
}

function bindEsc() {
  if (escapeHandler) return;
  escapeHandler = (e) => {
    if (e.key === "Escape") close();
  };
  document.addEventListener("keydown", escapeHandler);
}

function unbindEsc() {
  if (escapeHandler) {
    document.removeEventListener("keydown", escapeHandler);
    escapeHandler = null;
  }
}

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      bindEsc();
      document.body.style.overflow = "hidden";
    } else {
      unbindEsc();
      document.body.style.overflow = "";
      clearSearch();
    }
  },
);

onUnmounted(() => {
  clearTimeout(searchTimer);
  unbindEsc();
  document.body.style.overflow = "";
});
</script>

<style scoped src="@/style/addFriends.css"></style>
