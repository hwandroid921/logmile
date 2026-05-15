<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useTheme } from '@/composables/useTheme'
import AppLogo from './AppLogo.vue'
import AppIcon from '@/components/common/AppIcon.vue'

const { isDark, toggle: toggleTheme } = useTheme()

const route     = useRoute()
const router    = useRouter()
const authStore = useAuthStore()

const adminName    = computed(() => authStore.name ?? '관리자')
const isSuperAdmin = computed(() => authStore.role === 'ROLE_SUPER_ADMIN')
const isLoggedIn   = computed(() => authStore.isLoggedIn)

// 비로그인 공개 메뉴
const publicNavItems = [
  { name: 'publicIntro',    label: 'Project Intro' },
  { name: 'publicFeatures', label: 'Features' },
  { name: 'publicBoard',    label: 'Board' },
  { name: 'publicContact',  label: 'Contact US' },
]

// 관리자 메뉴
const adminNavItems = [
  { name: 'dashboard',    label: '대시보드' },
  { name: 'simulation',   label: '시뮬레이션' },
  { name: 'vehicles',     label: '차량 관리' },
  { name: 'drivers',      label: '운전자 관리' },
  { name: 'driveHistory', label: '운행 이력' },
  { name: 'thresholds',   label: '임계값 설정' },
  { name: 'stats',        label: '피로도 통계' },
]

// 최상위 관리자 메뉴
const superNavItems = [
  { name: 'superHome',     label: '대시보드' },
  { name: 'superApproval', label: '가입 승인' },
  { name: 'superCompany',  label: '업체 관리' },
  { name: 'boardManage',   label: '문의 관리' },
]

const navItems = computed(() => {
  if (!isLoggedIn.value) return publicNavItems
  return isSuperAdmin.value ? superNavItems : adminNavItems
})

function isActive(name) {
  return route.name === name
}

function logout() {
  authStore.clearAuth()
  router.push({ name: 'publicHome' })
}
</script>

<template>
  <header class="topbar">
    <div class="topbar-inner">
      <!-- 로고 -->
      <div class="topbar-left">
        <router-link
          :to="isLoggedIn ? { name: isSuperAdmin ? 'superHome' : 'dashboard' } : { name: 'publicHome' }"
          class="logo-link"
        >
          <AppLogo />
        </router-link>
        <span v-if="isLoggedIn" class="topbar-env mono">
          {{ isSuperAdmin ? 'SUPER_ADMIN' : 'ADMIN · 한라물류센터' }}
        </span>
      </div>

      <!-- 가운데 네비게이션 -->
      <nav class="topbar-nav">
        <router-link
          v-for="item in navItems"
          :key="item.name"
          :to="{ name: item.name }"
          class="nav-item"
          :class="{ active: isActive(item.name) }"
        >
          {{ item.label }}
        </router-link>
      </nav>

      <!-- 우측 -->
      <div class="topbar-right">
        <!-- 다크모드 토글 -->
        <button class="theme-toggle icon-btn" :title="isDark ? '라이트 모드로 전환' : '다크 모드로 전환'" @click="toggleTheme">
          <span class="theme-icon">{{ isDark ? '☀︎' : '☽' }}</span>
        </button>

        <div class="divider" />

        <!-- 비로그인: 업체등록 + 로그인 -->
        <template v-if="!isLoggedIn">
          <router-link :to="{ name: 'signup' }" class="btn-signup">
            업체 등록
          </router-link>
          <router-link :to="{ name: 'login' }" class="btn-login">
            <AppIcon name="user" :size="13" />
            관리자 로그인
          </router-link>
        </template>

        <!-- 로그인: 상태 + 유저칩 + 로그아웃 -->
        <template v-else>
          <div class="live-badge">
            <span class="dot dot-ok blink" />
            <span class="mono">SYSTEM ONLINE</span>
          </div>

          <div class="divider" />

          <button class="icon-btn" title="알림">
            <AppIcon name="bell" :size="16" />
          </button>

          <div class="user-chip">
            <div class="user-avatar">{{ adminName.charAt(0) }}</div>
            <span class="user-name">{{ adminName }}</span>
            <AppIcon name="chevronD" :size="13" style="color: var(--text-4)" />
          </div>

          <button class="icon-btn logout-btn" title="로그아웃" @click="logout">
            <AppIcon name="x" :size="16" />
          </button>
        </template>
      </div>
    </div>
  </header>
</template>

<style scoped>
.topbar {
  position: sticky;
  top: 0;
  z-index: 50;
  height: var(--topbar-height);
  background: var(--topbar-bg);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--line-1);
  flex-shrink: 0;
}

.topbar-inner {
  max-width: 1920px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 48px;
}

/* 로고 영역 */
.topbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.logo-link {
  text-decoration: none;
  display: flex;
  align-items: center;
}

.topbar-env {
  font-size: 10px;
  letter-spacing: 0.08em;
  color: var(--text-4);
  padding: 2px 7px;
  background: var(--bg-3);
  border: 1px solid var(--line-2);
  border-radius: var(--r-sm);
}

/* 네비게이션 */
.topbar-nav {
  display: flex;
  align-items: center;
  gap: 50px;
  flex: 1;
  justify-content: center;
}

.nav-item {
  padding: 7px 13px;
  font-size: 13.5px;
  font-weight: 600;
  border-radius: var(--r-sm);
  color: var(--text-3);
  text-decoration: none;
  transition: background 0.12s, color 0.12s;
  white-space: nowrap;
}
.nav-item:hover {
  background: var(--bg-3);
  color: var(--text-1);
}
.nav-item.active {
  background: var(--accent-soft);
  color: var(--accent);
  border: 1px solid var(--accent-line);
}

/* 우측 */
.topbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
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
.icon-btn:hover { background: var(--bg-3); color: var(--text-1); }
.logout-btn:hover { color: var(--danger); }

.user-chip {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 4px 10px 4px 5px;
  background: var(--bg-3);
  border: 1px solid var(--line-2);
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.15s;
}
.user-chip:hover { background: var(--bg-2); }

.user-avatar {
  width: 24px; height: 24px; border-radius: 50%;
  background: var(--accent-soft); border: 1px solid var(--accent-line);
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 700; color: var(--accent);
}

.user-name { font-size: 13px; font-weight: 500; color: var(--text-2); }

.btn-login {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 7px 16px;
  background: var(--accent-soft);
  border: 1px solid var(--accent-line);
  border-radius: var(--r-md);
  font-size: 13px;
  font-weight: 600;
  color: var(--accent);
  text-decoration: none;
  transition: background 0.15s;
}
.btn-login:hover { background: var(--accent); color: var(--accent-ink); }

.btn-signup {
  display: inline-flex;
  align-items: center;
  padding: 7px 16px;
  background: transparent;
  border: 1px solid var(--line-2);
  border-radius: var(--r-md);
  font-size: 13px;
  font-weight: 600;
  color: var(--text-2);
  text-decoration: none;
  transition: all 0.15s;
}
.btn-signup:hover { background: var(--bg-3); border-color: var(--line-3); color: var(--text-1); }

/* 다크모드 토글 */
.theme-toggle { font-size: 15px; width: 32px; height: 32px; }
.theme-icon { line-height: 1; display: flex; align-items: center; justify-content: center; }

/* 점 & 애니메이션 */
.dot { width: 6px; height: 6px; border-radius: 50%; display: inline-block; }
.dot-ok  { background: var(--ok); box-shadow: 0 0 0 3px var(--ok-soft); }
.blink   { animation: blink-soft 2s infinite; }
@keyframes blink-soft { 0%, 100% { opacity: 1; } 50% { opacity: 0.55; } }
</style>
