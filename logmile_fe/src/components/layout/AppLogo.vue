<script setup>
// Claude Design shared.jsx Logo 컴포넌트 → Vue 변환
defineProps({
  size: { type: Number, default: 22 },
  showText: { type: Boolean, default: true },
})
</script>

<template>
  <div class="logo-wrap">
    <!-- 5개 슬레이트 바: 왼쪽(높음) → 오른쪽(낮음) -->
    <svg :width="size + 10" :height="size" viewBox="0 0 40 28" fill="none">
      <rect x="0"  y="2"  width="6" height="24" rx="1.5" fill="#515F7A"/>
      <rect x="7"  y="6"  width="6" height="20" rx="1.5" fill="#747F95"/>
      <rect x="14" y="10" width="6" height="16" rx="1.5" fill="#979EAE"/>
      <rect x="21" y="14" width="6" height="12" rx="1.5" fill="#B8BFC9"/>
      <rect x="28" y="18" width="6" height="8"  rx="1.5" fill="#DCDFE4"/>
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
  font-size: 17px;
  letter-spacing: 0;
  color: var(--text-1);
}
</style>
