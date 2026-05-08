<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import AppLogo from './AppLogo.vue'
import AppIcon from '@/components/common/AppIcon.vue'

const router = useRouter()
const authStore = useAuthStore()

const adminName = computed(() => authStore.name ?? '관리자')
const isSuperAdmin = computed(() => authStore.role === 'ROLE_SUPER_ADMIN')

function logout() {
  authStore.clearAuth()
  router.push({ name: 'login' })
}
</script>

<template>
  <header class="topbar">
    <!-- 로고 -->
    <div class="topbar-left">
      <AppLogo />
      <span class="topbar-env">
        {{ isSuperAdmin ? '최상위 관리자' : '업체 관리자' }}
      </span>
    </div>

    <!-- 우측 상태 + 유저 -->
    <div class="topbar-right">
      <!-- 실시간 상태 표시 -->
      <div class="live-badge">
        <span class="dot dot-ok" style="animation: blink-soft 2s infinite" />
        <span class="mono">LIVE</span>
      </div>

      <div class="divider" />

      <!-- 알림 버튼 -->
      <button class="icon-btn" title="알림">
        <AppIcon name="bell" :size="16" />
      </button>

      <!-- 유저 칩 -->
      <div class="user-chip">
        <div class="user-avatar">{{ adminName.charAt(0) }}</div>
        <span class="user-name">{{ adminName }}</span>
        <AppIcon name="chevronR" :size="13" style="color: var(--text-4)" />
      </div>

      <!-- 로그아웃 -->
      <button class="icon-btn logout-btn" title="로그아웃" @click="logout">
        <AppIcon name="logout" :size="16" />
      </button>
    </div>
  </header>
</template>

<style scoped>
.topbar {
  position: sticky;
  top: 0;
  z-index: 50;
  height: var(--topbar-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: rgba(241, 243, 246, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--line-1);
  flex-shrink: 0;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.topbar-env {
  font-family: var(--font-mono);
  font-size: 10.5px;
  letter-spacing: 0.08em;
  color: var(--text-4);
  padding: 2px 7px;
  background: var(--bg-3);
  border: 1px solid var(--line-2);
  border-radius: var(--r-sm);
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.live-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: var(--ok);
  letter-spacing: 0.06em;
}

.divider {
  width: 1px;
  height: 18px;
  background: var(--line-2);
}

.icon-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--r-sm);
  color: var(--text-3);
  transition: background 0.15s, color 0.15s;
}
.icon-btn:hover {
  background: var(--bg-3);
  color: var(--text-1);
}
.logout-btn:hover {
  color: var(--danger);
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 4px 10px 4px 5px;
  background: var(--bg-3);
  border: 1px solid var(--line-2);
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}
.user-chip:hover {
  background: var(--bg-4);
  border-color: var(--line-3);
}

.user-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--accent-soft);
  border: 1px solid var(--accent-line);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  color: var(--accent);
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-2);
}
</style>
