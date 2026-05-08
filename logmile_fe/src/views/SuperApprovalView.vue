<script setup>
import { ref, computed, onMounted } from 'vue'
import { approvalApi } from '@/api/approvalApi'
import AppIcon from '@/components/common/AppIcon.vue'

/* ── 데이터 ── */
const list    = ref([])
const loading = ref(false)
const errorMsg = ref('')

/* ── 필터 ── */
const roleFilter   = ref('ALL')   // ALL | ADMIN | SUPER_ADMIN
const statusFilter = ref('ALL')   // ALL | ACTIVE | INACTIVE | SUSPENDED

/* ── 모달 ── */
const showModal = ref(false)
const draft = ref({ email: '', name: '', phone: '', role: 'ADMIN' })
const addLoading = ref(false)
const addError   = ref('')

/* ── 파생 ── */
const activeCount    = computed(() => list.value.filter(a => a.status === 'ACTIVE').length)
const inactiveCount  = computed(() => list.value.filter(a => a.status === 'INACTIVE').length)
const suspendedCount = computed(() => list.value.filter(a => a.status === 'SUSPENDED').length)

const filtered = computed(() => {
  return list.value.filter(a => {
    const rOk = roleFilter.value   === 'ALL' || a.role   === roleFilter.value
    const sOk = statusFilter.value === 'ALL' || a.status === statusFilter.value
    return rOk && sOk
  })
})

/* ── API ── */
async function fetchList() {
  loading.value  = true
  errorMsg.value = ''
  try {
    const res = await approvalApi.getPendingAdmins()
    // 백엔드 응답이 대기 목록이므로 PENDING → ACTIVE 로 매핑하여 표시
    list.value = res.data.map(a => ({
      ...a,
      status: a.status ?? 'ACTIVE',
    }))
  } catch {
    errorMsg.value = '목록을 불러오는 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

const STATUS_CYCLE = { ACTIVE: 'INACTIVE', INACTIVE: 'SUSPENDED', SUSPENDED: 'ACTIVE' }

async function toggleStatus(admin) {
  if (admin.role === 'SUPER_ADMIN') return
  const next = STATUS_CYCLE[admin.status] ?? 'ACTIVE'
  admin.status = next
}

function openModal() {
  draft.value = { email: '', name: '', phone: '', role: 'ADMIN' }
  addError.value  = ''
  showModal.value = true
}

async function submitAdd() {
  if (!draft.value.email || !draft.value.name) {
    addError.value = '이메일과 이름은 필수 입력 항목입니다.'
    return
  }
  addLoading.value = true
  addError.value   = ''
  try {
    // POST /api/super/admins (백엔드 준비 후 연동)
    const newAdmin = {
      adminId:     Date.now(),
      email:       draft.value.email,
      name:        draft.value.name,
      phone:       draft.value.phone || '-',
      role:        draft.value.role,
      companyName: '-',
      status:      'ACTIVE',
      createdAt:   new Date().toISOString(),
    }
    list.value.unshift(newAdmin)
    showModal.value = false
  } catch (e) {
    addError.value = `추가 중 오류가 발생했습니다. (${e.response?.status ?? '알 수 없음'})`
  } finally {
    addLoading.value = false
  }
}

function formatDate(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleDateString('ko-KR', {
    year: '2-digit', month: '2-digit', day: '2-digit',
  })
}

function statusStyle(s) {
  if (s === 'ACTIVE')    return { color: 'var(--ok)',      bg: 'var(--ok-soft)',      border: 'rgba(94,138,111,.3)' }
  if (s === 'SUSPENDED') return { color: 'var(--danger)',  bg: 'var(--danger-soft)',  border: 'rgba(181,84,74,.3)' }
  return                        { color: 'var(--text-3)',  bg: 'var(--bg-4)',         border: 'var(--line-2)' }
}

onMounted(fetchList)
</script>

<template>
  <div class="view">
    <!-- 브레드크럼 -->
    <div class="breadcrumb mono">
      <span>SUPER_ADMIN</span>
      <AppIcon name="chevronR" :size="11" class="bc-sep" />
      <span>ADMIN_TABLE</span>
      <span class="bc-count">· {{ filtered.length }} record(s)</span>
    </div>

    <!-- 헤더 -->
    <div class="view-header">
      <h2 class="view-title">관리자 계정</h2>
      <div class="header-actions">
        <button class="btn btn-ghost hdr-btn" :disabled="loading" @click="fetchList">
          <AppIcon name="refresh" :size="13" />
          새로고침
        </button>
        <button class="btn btn-primary hdr-btn" @click="openModal">
          <AppIcon name="plus" :size="13" />
          관리자 추가
        </button>
      </div>
    </div>

    <!-- 에러 배너 -->
    <div v-if="errorMsg" class="error-banner">{{ errorMsg }}</div>

    <!-- 요약 카드 -->
    <div class="summary-row">
      <div class="sum-card">
        <span class="sum-dot dot-ok" />
        <span class="sum-label mono">ACTIVE</span>
        <span class="sum-val mono ok-val">{{ activeCount }}</span>
      </div>
      <div class="sum-card">
        <span class="sum-dot dot-gray" />
        <span class="sum-label mono">INACTIVE</span>
        <span class="sum-val mono">{{ inactiveCount }}</span>
      </div>
      <div class="sum-card">
        <span class="sum-dot dot-danger" />
        <span class="sum-label mono">SUSPENDED</span>
        <span class="sum-val mono danger-val">{{ suspendedCount }}</span>
      </div>
    </div>

    <!-- 필터 -->
    <div class="filter-row">
      <!-- ROLE 필터 -->
      <div class="filter-group">
        <span class="filter-label mono">ROLE</span>
        <div class="filter-btns">
          <button
            v-for="r in ['ALL', 'ADMIN', 'SUPER_ADMIN']"
            :key="r"
            class="filter-btn mono"
            :class="{ active: roleFilter === r }"
            @click="roleFilter = r"
          >{{ r }}</button>
        </div>
      </div>

      <!-- STATUS 필터 -->
      <div class="filter-group">
        <span class="filter-label mono">STATUS</span>
        <div class="filter-btns">
          <button
            v-for="s in ['ALL', 'ACTIVE', 'INACTIVE', 'SUSPENDED']"
            :key="s"
            class="filter-btn mono"
            :class="{ active: statusFilter === s }"
            @click="statusFilter = s"
          >{{ s }}</button>
        </div>
      </div>
    </div>

    <!-- 로딩 -->
    <div v-if="loading" class="empty-state">
      <div class="spinner" />
      <span>목록 불러오는 중...</span>
    </div>

    <!-- 빈 상태 -->
    <div v-else-if="filtered.length === 0" class="empty-state">
      <AppIcon name="users" :size="30" style="opacity:.25" />
      <p class="empty-title">표시할 계정이 없습니다</p>
    </div>

    <!-- 테이블 -->
    <div v-else class="card table-card">
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>이름 / 이메일</th>
              <th>전화</th>
              <th>ROLE</th>
              <th>COMPANY</th>
              <th>STATUS</th>
              <th>등록일</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="admin in filtered" :key="admin.adminId">
              <td class="cell-mono cell-id">{{ admin.adminId }}</td>
              <td>
                <div class="name-cell">
                  <span class="cell-name">{{ admin.name }}</span>
                  <span class="cell-email mono">{{ admin.email }}</span>
                </div>
              </td>
              <td class="cell-mono">{{ admin.phone ?? '-' }}</td>
              <td>
                <span
                  class="role-badge mono"
                  :class="admin.role === 'SUPER_ADMIN' ? 'role-super' : 'role-admin'"
                >{{ admin.role === 'SUPER_ADMIN' ? 'SUPER' : 'ADMIN' }}</span>
              </td>
              <td class="cell-company">{{ admin.companyName ?? '-' }}</td>
              <td>
                <button
                  class="status-btn mono"
                  :class="{ 'status-disabled': admin.role === 'SUPER_ADMIN' }"
                  :style="{
                    color:            statusStyle(admin.status).color,
                    background:       statusStyle(admin.status).bg,
                    borderColor:      statusStyle(admin.status).border,
                  }"
                  :disabled="admin.role === 'SUPER_ADMIN'"
                  @click="toggleStatus(admin)"
                >{{ admin.status }}</button>
              </td>
              <td class="cell-mono cell-date">{{ formatDate(admin.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ── 추가 모달 ── -->
    <Teleport to="body">
      <div v-if="showModal" class="modal-backdrop" @click.self="showModal = false">
        <div class="modal-card">
          <div class="modal-header">
            <span class="modal-title">관리자 추가</span>
            <button class="modal-close" @click="showModal = false">
              <AppIcon name="x" :size="14" />
            </button>
          </div>

          <div class="modal-body">
            <p v-if="addError" class="add-error">{{ addError }}</p>

            <div class="field">
              <label class="field-label">이메일 <span class="required">*</span></label>
              <input v-model="draft.email" type="email" class="field-input mono" placeholder="admin@company.kr" />
            </div>
            <div class="field">
              <label class="field-label">이름 <span class="required">*</span></label>
              <input v-model="draft.name" type="text" class="field-input" placeholder="홍길동" />
            </div>
            <div class="field">
              <label class="field-label">전화번호</label>
              <input v-model="draft.phone" type="text" class="field-input mono" placeholder="010-0000-0000" />
            </div>
            <div class="field">
              <label class="field-label">역할</label>
              <div class="role-toggle">
                <button
                  class="role-opt"
                  :class="{ active: draft.role === 'ADMIN' }"
                  @click="draft.role = 'ADMIN'"
                >업체 관리자</button>
                <button
                  class="role-opt"
                  :class="{ active: draft.role === 'SUPER_ADMIN' }"
                  @click="draft.role = 'SUPER_ADMIN'"
                >최상위 관리자</button>
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn btn-ghost" @click="showModal = false">취소</button>
            <button class="btn btn-primary" :disabled="addLoading" @click="submitAdd">
              {{ addLoading ? '추가 중...' : '추가' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.view {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 32px 32px 40px;
}

/* 브레드크럼 */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 10.5px;
  color: var(--text-4);
  letter-spacing: 0.07em;
}
.bc-sep { opacity: .5; }
.bc-count { color: var(--text-4); margin-left: 4px; }

/* 헤더 */
.view-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.view-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-1);
  margin: 0;
  letter-spacing: -0.01em;
}
.header-actions { display: flex; gap: 8px; }
.hdr-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12.5px;
  padding: 7px 13px;
}

/* 에러 배너 */
.error-banner {
  padding: 10px 14px;
  background: var(--danger-soft);
  border: 1px solid rgba(181,84,74,.25);
  border-radius: var(--r-md);
  font-size: 13px;
  color: var(--danger);
}

/* 요약 카드 */
.summary-row {
  display: flex;
  gap: 10px;
}
.sum-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: var(--bg-2);
  border: 1px solid var(--line-1);
  border-radius: var(--r-md);
  flex: 1;
}
.sum-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.dot-ok      { background: var(--ok); }
.dot-gray    { background: var(--text-4); }
.dot-danger  { background: var(--danger); }
.sum-label { font-size: 10px; letter-spacing: 0.08em; color: var(--text-4); flex: 1; }
.sum-val   { font-size: 20px; font-weight: 700; color: var(--text-2); }
.ok-val     { color: var(--ok); }
.danger-val { color: var(--danger); }

/* 필터 */
.filter-row {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}
.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}
.filter-label {
  font-size: 10px;
  color: var(--text-4);
  letter-spacing: 0.08em;
  white-space: nowrap;
}
.filter-btns { display: flex; gap: 3px; }
.filter-btn {
  padding: 4px 9px;
  border-radius: var(--r-sm);
  font-size: 10.5px;
  background: none;
  border: 1px solid var(--line-2);
  color: var(--text-3);
  cursor: pointer;
  letter-spacing: 0.05em;
  transition: background .12s, color .12s, border-color .12s;
}
.filter-btn.active {
  background: var(--accent-soft);
  border-color: var(--accent-line);
  color: var(--accent);
  font-weight: 600;
}

/* 빈/로딩 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 56px 24px;
  color: var(--text-4);
}
.empty-title { font-size: 13px; margin: 0; }
.spinner {
  width: 26px; height: 26px;
  border: 2px solid var(--line-2);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin .8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 테이블 */
.table-card { padding: 0; overflow: hidden; }
.table-wrap { overflow-x: auto; }
.table { width: 100%; border-collapse: collapse; font-size: 13px; }
.table th {
  padding: 10px 14px;
  text-align: left;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  color: var(--text-4);
  background: var(--bg-3);
  border-bottom: 1px solid var(--line-1);
  white-space: nowrap;
}
.table td {
  padding: 11px 14px;
  color: var(--text-2);
  border-bottom: 1px solid var(--line-1);
  vertical-align: middle;
}
.table tbody tr:last-child td { border-bottom: none; }
.table tbody tr:hover td { background: var(--bg-3); }

.cell-mono  { font-family: var(--font-mono); font-size: 11.5px; }
.cell-id    { color: var(--text-4); }
.cell-date  { color: var(--text-4); }
.cell-company { color: var(--text-3); font-size: 12.5px; }

.name-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.cell-name  { font-weight: 500; color: var(--text-1); font-size: 13px; }
.cell-email { font-size: 11px; color: var(--text-4); }

.role-badge {
  display: inline-block;
  padding: 2px 7px;
  border-radius: var(--r-sm);
  font-size: 10px;
  letter-spacing: 0.07em;
  font-weight: 600;
  border: 1px solid;
}
.role-super { background: var(--accent-soft); color: var(--accent); border-color: var(--accent-line); }
.role-admin { background: var(--bg-4);        color: var(--text-3); border-color: var(--line-2); }

.status-btn {
  padding: 3px 9px;
  border-radius: var(--r-sm);
  font-size: 10px;
  letter-spacing: 0.06em;
  font-weight: 600;
  border: 1px solid;
  cursor: pointer;
  transition: opacity .15s;
}
.status-btn:hover:not(:disabled) { opacity: .75; }
.status-disabled { cursor: default; opacity: .6; }

/* 모달 */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
}
.modal-card {
  width: 420px;
  background: var(--bg-1);
  border: 1px solid var(--line-1);
  border-radius: var(--r-lg);
  box-shadow: 0 12px 40px rgba(0,0,0,.18);
  overflow: hidden;
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--line-1);
}
.modal-title { font-size: 15px; font-weight: 600; color: var(--text-1); }
.modal-close {
  width: 28px; height: 28px;
  display: flex; align-items: center; justify-content: center;
  border-radius: var(--r-sm);
  color: var(--text-4);
  transition: background .12s, color .12s;
}
.modal-close:hover { background: var(--bg-3); color: var(--text-2); }
.modal-body { padding: 20px; display: flex; flex-direction: column; gap: 14px; }
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 14px 20px;
  border-top: 1px solid var(--line-1);
  background: var(--bg-2);
}

.add-error {
  padding: 8px 12px;
  background: var(--danger-soft);
  border: 1px solid rgba(181,84,74,.25);
  border-radius: var(--r-sm);
  font-size: 12px;
  color: var(--danger);
  margin: 0;
}

.field { display: flex; flex-direction: column; gap: 5px; }
.field-label {
  font-size: 10.5px;
  font-weight: 600;
  color: var(--text-3);
  letter-spacing: 0.07em;
  text-transform: uppercase;
}
.required { color: var(--danger); }
.field-input {
  width: 100%;
  box-sizing: border-box;
  background: var(--bg-0);
  border: 1px solid var(--line-2);
  border-radius: var(--r-md);
  padding: 8px 11px;
  font-size: 13px;
  color: var(--text-1);
  outline: none;
  transition: border-color .15s;
}
.field-input:focus { border-color: var(--accent-line); }

.role-toggle {
  display: flex;
  background: var(--bg-3);
  border: 1px solid var(--line-2);
  border-radius: var(--r-md);
  padding: 3px;
  gap: 3px;
}
.role-opt {
  flex: 1;
  padding: 6px 10px;
  border-radius: var(--r-sm);
  font-size: 12.5px;
  font-weight: 500;
  color: var(--text-4);
  background: none;
  border: none;
  cursor: pointer;
  transition: background .12s, color .12s;
  font-family: inherit;
}
.role-opt.active {
  background: var(--bg-1);
  color: var(--accent);
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(81,95,122,.1);
}
</style>
