<script setup>
import AppTopbar from './AppTopbar.vue'
</script>

<template>
  <div class="app-shell">
    <AppTopbar />
    <main class="app-main">
      <slot />
    </main>
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

.app-main {
  flex: 1;
  overflow-y: auto;
  background: var(--bg-0);
}
</style>
