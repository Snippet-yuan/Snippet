<template>
  <Teleport to="body">
    <Transition name="toast">
      <div
        v-if="visible"
        class="toast"
        :class="`toast--${type}`"
        role="status"
        aria-live="polite"
      >
        <PhCheckCircle
          v-if="type === 'success'"
          :size="20"
          :weight="'fill'"
          class="toast__icon"
        />
        <PhWarningCircle v-else :size="20" :weight="'fill'" class="toast__icon" />
        <span class="toast__message">{{ message }}</span>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref } from "vue";
import { PhCheckCircle, PhWarningCircle } from "@phosphor-icons/vue";

const visible = ref(false);
const type = ref("success");
const message = ref("");

let timer = null;

function show({ type: nextType = "success", message: nextMessage = "" } = {}) {
  type.value = nextType;
  message.value = nextMessage;
  visible.value = true;

  clearTimeout(timer);
  timer = setTimeout(() => {
    visible.value = false;
  }, 2200);
}

defineExpose({ show });
</script>

<style scoped src="@/style/toast.less"></style>
