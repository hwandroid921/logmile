<script setup>
import { ref, onMounted } from 'vue'
import { approvalApi } from '@/api/approvalApi'
import AppIcon from '@/components/common/AppIcon.vue'

const list = ref([])
const loading = ref(false)
const actionLoading = ref(null)  // 처리 중인 adminId
const errorMsg = ref('')

async function fetchList() {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await approvalApi.getPendingAdmins()
    list.value = res.data
  } catch {
    errorMsg.value = '목록을 불러오는 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

async function doAction(adminId, action) {
  actionLoading.value = adminId
  try {
    if (action === 'approve') await approvalApi.approve(adminId)
    else if (action === 'reject') await approvalApi.reject(adminId)
    // 처리 후 목록에서 제거
    list.value = list.value.filter((a) => a.adminId !== adminId)
  } catch (e) {
    errorMsg.value = `처리 중 오류가 발생했습니다. (${e.response?.status ?? '알 수 없음'})`
  } finally {
    actionLoading.value = null
  }
}

function formatDate(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleDateString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
  })
}

onMounted(fetchList)
</script>

<template>
  <div class="view">
    <!-- 헤더 -->
    <div class="view-header">
      <div>
        <h2 class="view-title">가입 승인 / 거절</h2>
        <p class="view-sub">승인 대기 중인 업체 관리자 목록입니다.</p>
      </div>
      <button class="btn btn-ghost icon-btn" :disabled="loading" @click="fetchList">
        <AppIcon name="refresh" :size="14" />
        새로고침
      </button>
    </div>

    <!-- 에러 -->
    <div v-if="errorMsg" class="error-banner">{{ errorMsg }}</div>

    <!-- 로딩 -->
    <div v-if="loading" class="empty-state">
      <div class="spinner" />
      <span>목록 불러오는 중...</span>
    </div>

    <!-- 빈 목록 -->
    <div v-else-if="!loading && list.length === 0" class="empty-state">
      <AppIcon name="check" :size="32" class="empty-icon" />
      <p class="empty-title">대기 중인 가입 신청이 없습니다</p>
      <p class="empty-sub">새로운 신청이 들어오면 이 목록에 표시됩니다.</p>
    </div>

    <!-- 테이블 -->
    <div v-else class="card table-card">
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>관리자 이름</th>
              <th>이메일</th>
              <th>소속 업체</th>
              <th>신청일</th>
              <th>상태</th>
              <th>처리</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in list" :key="item.adminId">
              <td class="cell-name">{{ item.name }}</td>
              <td class="cell-mono">{{ item.email }}</td>
              <td>
                <span class="company-chip">{{ item.companyName ?? '-' }}</span>
              </td>
              <td class="cell-mono cell-date">{{ formatDate(item.createdAt) }}</td>
              <td>
                <span class="chip chip-warn">대기</span>
              </td>
              <td class="cell-actions">
                <button
                  class="btn btn-ok action-btn"
                  :disabled="actionLoading === item.adminId"
                  @click="doAction(item.adminId, 'approve')"
                >
                  <AppIcon name="check" :size="12" />
                  승인
                </button>
                <button
                  class="btn btn-danger action-btn"
                  :disabled="actionLoading === item.adminId"
                  @click="doAction(item.adminId, 'reject')"
                >
                  <AppIcon name="x" :size="12" />
                  거절
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="table-footer">
        총 <strong>{{ list.length }}</strong>건 대기 중
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

.view-sub {
  font-size: 12.5px;
  color: var(--text-4);
  margin: 0;
}

.icon-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  padding: 7px 12px;
  white-space: nowrap;
}

.error-banner {
  padding: 10px 14px;
  background: var(--danger-soft);
  border: 1px solid rgba(209, 139, 126, 0.25);
  border-radius: var(--r-md);
  font-size: 13px;
  color: var(--danger);
}

/* 빈 상태 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 64px 24px;
  color: var(--text-4);
}
.empty-icon { opacity: 0.3; margin-bottom: 4px; }
.empty-title { font-size: 14px; font-weight: 600; color: var(--text-3); margin: 0; }
.empty-sub { font-size: 12.5px; margin: 0; }

.spinner {
  width: 28px;
  height: 28px;
  border: 2px solid var(--line-2);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 테이블 카드 */
.table-card { padding: 0; overflow: hidden; }

.table-wrap { overflow-x: auto; }

.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.table th {
  padding: 11px 16px;
  text-align: left;
  font-size: 10.5px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--text-4);
  background: var(--bg-3);
  border-bottom: 1px solid var(--line-1);
  white-space: nowrap;
}

.table td {
  padding: 12px 16px;
  color: var(--text-2);
  border-bottom: 1px solid var(--line-1);
  vertical-align: middle;
}

.table tbody tr:last-child td { border-bottom: none; }
.table tbody tr:hover td { background: var(--bg-3); }

.cell-name { font-weight: 500; color: var(--text-1); }
.cell-mono { font-family: var(--font-mono); font-size: 12px; }
.cell-date { color: var(--text-4); }

.company-chip {
  display: inline-block;
  padding: 2px 8px;
  background: var(--bg-4);
  border-radius: var(--r-sm);
  font-size: 12px;
  color: var(--text-2);
}

.cell-actions {
  display: flex;
  gap: 6px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 5px 10px;
  font-size: 12px;
}
.action-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* ok / danger 버튼 변형 */
.btn-ok {
  background: var(--ok-soft);
  color: var(--ok);
  border: 1px solid rgba(138, 186, 154, 0.3);
}
.btn-ok:hover:not(:disabled) {
  background: rgba(138, 186, 154, 0.22);
}

.btn-danger {
  background: var(--danger-soft);
  color: var(--danger);
  border: 1px solid rgba(209, 139, 126, 0.3);
}
.btn-danger:hover:not(:disabled) {
  background: rgba(209, 139, 126, 0.22);
}

.table-footer {
  padding: 10px 16px;
  font-size: 12px;
  color: var(--text-4);
  border-top: 1px solid var(--line-1);
  background: var(--bg-3);
}
</style>
