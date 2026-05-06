<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { approvalApi } from '@/api/approvalApi'
import AppIcon from '@/components/common/AppIcon.vue'

const router = useRouter()
const authStore = useAuthStore()

const pendingList = ref([])
const loading = ref(false)

async function fetchPending() {
  loading.value = true
  try {
    const res = await approvalApi.getPendingAdmins()
    pendingList.value = res.data
  } catch {
    /* silent */
  } finally {
    loading.value = false
  }
}

const pendingCount = computed(() => pendingList.value.length)
const recentPending = computed(() => pendingList.value.slice(0, 5))

function formatDate(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleDateString('ko-KR', {
    month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit',
  })
}

onMounted(fetchPending)
</script>

<template>
  <div class="view">
    <!-- 헤더 -->
    <div class="view-header">
      <div>
        <h2 class="view-title">최상위 관리자 대시보드</h2>
        <p class="view-sub">LOGMILE 플랫폼 현황을 확인합니다.</p>
      </div>
      <div class="admin-badge">
        <span class="dot dot-ok" />
        <span class="mono">{{ authStore.name }}</span>
        <span class="chip chip-accent">SUPER ADMIN</span>
      </div>
    </div>

    <!-- 요약 카드 -->
    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-icon warn">
          <AppIcon name="users" :size="18" />
        </div>
        <div class="stat-body">
          <div class="stat-label">승인 대기</div>
          <div class="stat-value" :class="{ 'val-warn': pendingCount > 0 }">
            <span v-if="loading">—</span>
            <span v-else>{{ pendingCount }}</span>
            <span class="stat-unit">건</span>
          </div>
        </div>
        <button class="stat-action" @click="router.push({ name: 'superApproval' })">
          처리하기 <AppIcon name="chevron-right" :size="12" />
        </button>
      </div>

      <div class="stat-card">
        <div class="stat-icon ok">
          <AppIcon name="building" :size="18" />
        </div>
        <div class="stat-body">
          <div class="stat-label">등록 업체</div>
          <div class="stat-value">
            <span class="mono" style="font-size:14px; color: var(--text-3)">API 준비 중</span>
          </div>
        </div>
        <button class="stat-action" @click="router.push({ name: 'superCompany' })">
          목록 보기 <AppIcon name="chevron-right" :size="12" />
        </button>
      </div>

      <div class="stat-card">
        <div class="stat-icon accent">
          <AppIcon name="activity" :size="18" />
        </div>
        <div class="stat-body">
          <div class="stat-label">시스템 상태</div>
          <div class="stat-value" style="display:flex; align-items:center; gap:8px">
            <span class="dot dot-ok pulse" />
            <span style="font-size:14px; color:var(--ok); font-weight:600">정상 운영</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 최근 가입 신청 -->
    <div class="card">
      <div class="section-header">
        <h3 class="section-title">최근 가입 신청</h3>
        <button class="btn btn-ghost small-btn" @click="router.push({ name: 'superApproval' })">
          전체 보기
        </button>
      </div>

      <div v-if="loading" class="loading-row">
        <div class="spinner" />
      </div>

      <div v-else-if="recentPending.length === 0" class="empty-row">
        대기 중인 신청이 없습니다.
      </div>

      <div v-else class="pending-list">
        <div
          v-for="item in recentPending"
          :key="item.adminId"
          class="pending-item"
        >
          <div class="pending-avatar">{{ item.name?.charAt(0) }}</div>
          <div class="pending-info">
            <span class="pending-name">{{ item.name }}</span>
            <span class="pending-company mono">{{ item.companyName ?? '업체 미등록' }}</span>
          </div>
          <span class="pending-date mono">{{ formatDate(item.createdAt) }}</span>
          <span class="chip chip-warn">대기</span>
          <button
            class="btn btn-ghost small-btn"
            @click="router.push({ name: 'superApproval' })"
          >
            처리
          </button>
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
}

.view-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
.view-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-1);
  margin: 0 0 4px;
}
.view-sub { font-size: 12.5px; color: var(--text-4); margin: 0; }

.admin-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 12px;
  background: var(--bg-3);
  border: 1px solid var(--line-1);
  border-radius: var(--r-md);
  font-size: 12.5px;
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
.stat-icon.warn { background: var(--warn-soft); color: var(--warn); }
.stat-icon.ok   { background: var(--ok-soft); color: var(--ok); }
.stat-icon.accent { background: var(--accent-soft); color: var(--accent); }

.stat-body { flex: 1; }
.stat-label {
  font-size: 11px;
  color: var(--text-4);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  margin-bottom: 6px;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-1);
  display: flex;
  align-items: baseline;
  gap: 4px;
  font-family: var(--font-mono);
}
.val-warn { color: var(--warn); }
.stat-unit { font-size: 13px; font-weight: 400; color: var(--text-4); }

.stat-action {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  font-size: 12px;
  color: var(--accent);
  cursor: pointer;
  padding: 0;
  font-family: var(--font-sans);
  transition: color 0.15s;
}
.stat-action:hover { color: var(--accent-hover); }

/* 섹션 */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-1);
  margin: 0;
}

.small-btn { font-size: 12px; padding: 5px 10px; }

.loading-row, .empty-row {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  color: var(--text-4);
  font-size: 13px;
  gap: 10px;
}

.spinner {
  width: 22px;
  height: 22px;
  border: 2px solid var(--line-2);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* pending list */
.pending-list { display: flex; flex-direction: column; gap: 2px; }

.pending-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--r-md);
  transition: background 0.12s;
}
.pending-item:hover { background: var(--bg-3); }

.pending-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--accent-soft);
  color: var(--accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.pending-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.pending-name { font-size: 13px; font-weight: 500; color: var(--text-1); }
.pending-company { font-size: 11.5px; color: var(--text-4); }

.pending-date { font-size: 11.5px; color: var(--text-4); white-space: nowrap; }

/* chip-accent */
.chip-accent {
  background: var(--accent-soft);
  color: var(--accent);
  border: 1px solid var(--accent-line);
}
.pulse {
  animation: pulse-ring 2s ease-out infinite;
}
</style>
