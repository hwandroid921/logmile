<script setup>
import AppTopbar from './AppTopbar.vue'
import AppSidebar from './AppSidebar.vue'
</script>

<template>
  <div class="app-shell">
    <!-- 상단바 -->
    <AppTopbar />

    <div class="app-body">
      <!-- 사이드바 -->
      <AppSidebar />

      <!-- 메인 콘텐츠 -->
      <main class="app-main">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-shell {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: var(--bg-0);
}

.app-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.app-main {
  flex: 1;
  overflow-y: auto;
  padding: 0;
  background: var(--bg-0);
}
</style>
