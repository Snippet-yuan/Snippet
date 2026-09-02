<script setup>
import {
  PhUserCirclePlus,
  PhEnvelope,
  PhEnvelopeOpen,
} from "@phosphor-icons/vue";
defineProps({
  isScrolled: {
    type: Boolean,
    default: false,
  },
});
</script>

<template>
  <div class="operation-bar" :class="{ 'is-scrolled': isScrolled }">
    <PhEnvelope :size="32" />
    <!-- <PhEnvelopeOpen :size="32" /> -->
    <PhUserCirclePlus :size="32" />
  </div>
</template>

<style scoped src="@/style/operationBar.css"></style>
