<script setup>
// Claude Design shared.jsx Logo 컴포넌트 → Vue 변환
defineProps({
  size: { type: Number, default: 22 },
  showText: { type: Boolean, default: true },
})
</script>

<template>
  <div class="logo-wrap">
    <svg :width="size" :height="size" viewBox="0 0 28 28" fill="none">
      <rect x="1" y="1" width="26" height="26" rx="6" fill="var(--bg-3)" stroke="var(--line-3)" />
      <!-- Stylized 'L' road tracing into M -->
      <path d="M7 7v14h6" stroke="var(--accent)" stroke-width="2.2" stroke-linecap="round" />
      <path d="M13 21V11l4 5 4-5v10" stroke="var(--text-1)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
      <circle cx="7" cy="7" r="1.6" fill="var(--accent)" />
    </svg>
    <span v-if="showText" class="logo-text">logmile</span>
  </div>
</template>

<style scoped>
.logo-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  user-select: none;
}
.logo-text {
  font-weight: 700;
  font-size: 15px;
  letter-spacing: -0.01em;
  color: var(--text-1);
}
</style>
