<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { approvalApi } from '@/api/approvalApi'
import AppIcon from '@/components/common/AppIcon.vue'

const router    = useRouter()
const authStore = useAuthStore()

const adminList = ref([])
const loading   = ref(false)

async function fetchAdmins() {
  loading.value = true
  try {
    const res = await approvalApi.getPendingAdmins()
    adminList.value = res.data.map(a => ({
      ...a,
      status: a.status ?? 'ACTIVE',
    }))
  } catch {
    /* silent */
  } finally {
    loading.value = false
  }
}

const totalAdmins   = computed(() => adminList.value.length)
const recentAdmins  = computed(() => adminList.value.slice(0, 6))

function formatDate(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleDateString('ko-KR', {
    month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}

function statusStyle(s) {
  if (s === 'ACTIVE')    return { color: 'var(--ok)',     bg: 'var(--ok-soft)' }
  if (s === 'SUSPENDED') return { color: 'var(--danger)', bg: 'var(--danger-soft)' }
  return                        { color: 'var(--text-3)', bg: 'var(--bg-4)' }
}

onMounted(fetchAdmins)
</script>

<template>
  <div class="view">
    <!-- 브레드크럼 -->
    <div class="breadcrumb mono">
      <span>최상위 관리자</span>
      <AppIcon name="chevronR" :size="11" class="bc-sep" />
      <span>홈</span>
    </div>

    <!-- 헤더 -->
    <div class="view-header">
      <div>
        <h2 class="view-title">최상위 관리자 홈</h2>
        <p class="view-sub">LOGMILE 플랫폼 운영 현황을 확인합니다.</p>
      </div>
      <div class="admin-badge">
        <span class="dot dot-ok" style="animation: blink-soft 2s infinite" />
        <span class="mono">{{ authStore.name }}</span>
        <span class="chip chip-accent">최상위 관리자</span>
      </div>
    </div>

    <!-- 요약 카드 -->
    <div class="stat-grid">
      <!-- 전체 관리자 -->
      <div class="stat-card">
        <div class="stat-icon accent-icon">
          <AppIcon name="users" :size="18" />
        </div>
        <div class="stat-body">
          <div class="stat-label">전체 관리자</div>
          <div class="stat-value mono">
            <span v-if="loading">—</span>
            <span v-else>{{ totalAdmins }}</span>
            <span class="stat-unit">명</span>
          </div>
        </div>
        <button class="stat-link" @click="router.push({ name: 'superApproval' })">
          계정 관리 <AppIcon name="chevronR" :size="11" />
        </button>
      </div>

      <!-- 등록 업체 -->
      <div class="stat-card">
        <div class="stat-icon ok-icon">
          <AppIcon name="building" :size="18" />
        </div>
        <div class="stat-body">
          <div class="stat-label">등록 업체</div>
          <div class="stat-value">
            <span class="api-pending mono">API 준비 중</span>
          </div>
        </div>
        <button class="stat-link" @click="router.push({ name: 'superCompany' })">
          목록 보기 <AppIcon name="chevronR" :size="11" />
        </button>
      </div>

      <!-- 시스템 상태 -->
      <div class="stat-card">
        <div class="stat-icon ok-icon">
          <AppIcon name="activity" :size="18" />
        </div>
        <div class="stat-body">
          <div class="stat-label">시스템 상태</div>
          <div class="stat-value system-ok">
            <span class="dot dot-ok" style="animation: blink-soft 2s infinite" />
            정상 운영
          </div>
        </div>
      </div>
    </div>

    <!-- 최근 관리자 목록 -->
    <div class="card">
      <div class="section-header">
        <h3 class="section-title">관리자 목록</h3>
        <button
          class="btn btn-ghost small-btn"
          @click="router.push({ name: 'superApproval' })"
        >
          전체 보기
        </button>
      </div>

      <div v-if="loading" class="loading-row">
        <div class="spinner" />
        <span>불러오는 중...</span>
      </div>

      <div v-else-if="recentAdmins.length === 0" class="empty-row">
        등록된 관리자가 없습니다.
      </div>

      <div v-else class="admin-list">
        <div
          v-for="admin in recentAdmins"
          :key="admin.adminId"
          class="admin-item"
        >
          <div class="admin-avatar">{{ admin.name?.charAt(0) }}</div>
          <div class="admin-info">
            <span class="admin-name">{{ admin.name }}</span>
            <span class="admin-email mono">{{ admin.email }}</span>
          </div>
          <span class="admin-company">{{ admin.companyName ?? '-' }}</span>
          <span
            class="status-chip mono"
            :style="{
              color:      statusStyle(admin.status).color,
              background: statusStyle(admin.status).bg,
            }"
          >{{ admin.status }}</span>
          <span class="admin-date mono">{{ formatDate(admin.createdAt) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 32px 32px 40px;
}

/* 브레드크럼 */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: var(--text-3);
  letter-spacing: 0.07em;
}
.bc-sep { opacity: .5; }

/* 헤더 */
.view-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
.view-title { font-size: 20px; font-weight: 700; color: var(--text-1); margin: 0 0 4px; letter-spacing: 0; }
.view-sub   { font-size: 14px; color: var(--text-3); margin: 0; }

.admin-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 12px;
  background: var(--bg-3);
  border: 1px solid var(--line-1);
  border-radius: var(--r-md);
  font-size: 14px;
  color: var(--text-2);
}

/* 요약 카드 그리드 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
.stat-card {
  background: var(--bg-2);
  border: 1px solid var(--line-1);
  border-radius: var(--r-lg);
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.stat-icon {
  width: 38px;
  height: 38px;
  border-radius: var(--r-md);
  display: flex;
  align-items: center;
  justify-content: center;
}
.accent-icon { background: var(--accent-soft); color: var(--accent); }
.ok-icon     { background: var(--ok-soft);     color: var(--ok); }

.stat-body { flex: 1; }
.stat-label {
  font-size: 14px;
  color: var(--text-3);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  margin-bottom: 6px;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-1);
  display: flex;
  align-items: center;
  gap: 5px;
}
.stat-unit { font-size: 14px; font-weight: 400; color: var(--text-3); }
.api-pending { font-size: 14px; color: var(--text-3); }
.system-ok { font-size: 15px; font-weight: 600; color: var(--ok); gap: 8px; }

.stat-link {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 14px;
  color: var(--accent);
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  font-family: var(--font-sans);
  transition: color .15s;
}
.stat-link:hover { color: var(--accent-hover); }

/* 섹션 헤더 */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.section-title { font-size: 16px; font-weight: 600; color: var(--text-1); margin: 0; }
.small-btn { font-size: 14px; padding: 5px 10px; }

/* 로딩 / 빈 상태 */
.loading-row, .empty-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 32px;
  color: var(--text-3);
  font-size: 14px;
}
.spinner {
  width: 22px; height: 22px;
  border: 2px solid var(--line-2);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin .8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 관리자 목록 */
.admin-list { display: flex; flex-direction: column; gap: 2px; }
.admin-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 9px 10px;
  border-radius: var(--r-md);
  transition: background .12s;
}
.admin-item:hover { background: var(--bg-3); }

.admin-avatar {
  width: 30px; height: 30px;
  border-radius: 50%;
  background: var(--accent-soft);
  color: var(--accent);
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 700;
  flex-shrink: 0;
}
.admin-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}
.admin-name  { font-size: 16px; font-weight: 500; color: var(--text-1); }
.admin-email { font-size: 14px; color: var(--text-3); }
.admin-company { font-size: 14px; color: var(--text-3); white-space: nowrap; }

.status-chip {
  padding: 2px 7px;
  border-radius: var(--r-sm);
  font-size: 14px;
  letter-spacing: 0.06em;
  font-weight: 600;
  white-space: nowrap;
}
.admin-date { font-size: 14px; color: var(--text-3); white-space: nowrap; }

/* chip-accent */
.chip-accent {
  background: var(--accent-soft);
  color: var(--accent);
  border: 1px solid var(--accent-line);
}
</style>
