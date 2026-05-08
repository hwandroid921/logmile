<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import AppIcon from '@/components/common/AppIcon.vue'

const route = useRoute()
const authStore = useAuthStore()

const isSuperAdmin = computed(() => authStore.role === 'ROLE_SUPER_ADMIN')

// ROLE_SUPER_ADMIN 메뉴
const superMenus = [
  { section: 'MONITOR' },
  { name: 'superHome',      label: '대시보드',    icon: 'chart' },
  { section: 'MANAGE' },
  { name: 'superApproval',  label: '가입 승인/거절', icon: 'users' },
  { name: 'superCompany',   label: '업체 관리',   icon: 'building' },
]

// ROLE_ADMIN 메뉴
const adminMenus = [
  { section: 'MONITOR' },
  { name: 'dashboard',      label: '대시보드',    icon: 'chart' },
  { name: 'simulation',     label: '시뮬레이션',  icon: 'activity' },
  { section: 'MANAGE' },
  { name: 'vehicles',       label: '차량 관리',   icon: 'truck' },
  { name: 'drivers',        label: '운전자 관리', icon: 'users' },
  { name: 'driveHistory',   label: '운행 이력',   icon: 'list' },
  { section: 'CONFIG' },
  { name: 'thresholds',     label: '임계값 설정', icon: 'settings' },
  { name: 'stats',          label: '피로도 통계', icon: 'bolt' },
]

const menus = computed(() => isSuperAdmin.value ? superMenus : adminMenus)

function isActive(name) {
  return route.name === name
}
</script>

<template>
  <aside class="sidebar">
    <nav class="sidebar-nav">
      <template v-for="item in menus" :key="item.section ?? item.name">
        <!-- 섹션 레이블 -->
        <div v-if="item.section" class="nav-section">{{ item.section }}</div>

        <!-- 메뉴 아이템 -->
        <router-link
          v-else
          :to="{ name: item.name }"
          class="nav-item"
          :class="{ active: isActive(item.name) }"
        >
          <AppIcon :name="item.icon" :size="15" class="nav-icon" />
          <span class="nav-label">{{ item.label }}</span>
          <!-- 활성 인디케이터 -->
          <span v-if="isActive(item.name)" class="active-dot" />
        </router-link>
      </template>
    </nav>

    <!-- 하단 버전 정보 -->
    <div class="sidebar-footer">
      <span class="mono">v1.0.0</span>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  flex-shrink: 0;
  height: 100%;
  background: var(--bg-0);
  border-right: 1px solid var(--line-1);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

/* 섹션 레이블 */
.nav-section {
  font-family: var(--font-mono);
  font-size: 9.5px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--text-4);
  padding: 12px 10px 6px;
  margin-top: 4px;
}
.nav-section:first-child { margin-top: 0; }

/* 메뉴 아이템 */
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: var(--r-md);
  font-size: 13px;
  font-weight: 500;
  color: var(--text-3);
  text-decoration: none;
  transition: background 0.15s, color 0.15s;
  position: relative;
  cursor: pointer;
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

.nav-icon {
  flex-shrink: 0;
  opacity: 0.7;
}
.nav-item.active .nav-icon {
  opacity: 1;
}

.nav-label {
  flex: 1;
}

/* 활성 오른쪽 점 표시 */
.active-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--accent);
}

/* 하단 */
.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--line-1);
  font-size: 10.5px;
  color: var(--text-4);
  letter-spacing: 0.04em;
}
</style>
