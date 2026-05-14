<script setup>
import { computed, onMounted, ref } from 'vue'
import AppIcon from '@/components/common/AppIcon.vue'
import { companyApi } from '@/api/companyApi'

const companies = ref([])
const loading = ref(false)
const error = ref('')

const activeCount = computed(() => companies.value.filter(company => company.active).length)
const inactiveCount = computed(() => companies.value.length - activeCount.value)

async function fetchCompanies() {
  loading.value = true
  error.value = ''
  try {
    const res = await companyApi.getAll()
    companies.value = res.data
  } catch (err) {
    error.value = '업체 목록을 불러오는 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

async function toggleCompany(company) {
  error.value = ''
  try {
    const res = company.active
      ? await companyApi.deactivate(company.id)
      : await companyApi.activate(company.id)
    companies.value = companies.value.map(item => item.id === company.id ? res.data : item)
  } catch (err) {
    error.value = '업체 상태 변경 중 오류가 발생했습니다.'
  }
}

function formatDate(value) {
  if (!value) return '-'
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date(value))
}

onMounted(fetchCompanies)
</script>

<template>
  <div class="view">
    <div class="view-header">
      <div>
        <h2 class="view-title">업체 관리</h2>
        <p class="view-sub">등록된 운송 업체 목록을 확인하고 관리합니다.</p>
      </div>
      <button class="btn btn-ghost" :disabled="loading" @click="fetchCompanies">
        <AppIcon name="refresh" :size="14" />
        새로고침
      </button>
    </div>

    <div class="summary-grid">
      <div class="summary-card">
        <span class="mono summary-label">TOTAL</span>
        <strong>{{ companies.length }}</strong>
        <span>등록 업체</span>
      </div>
      <div class="summary-card">
        <span class="mono summary-label">ACTIVE</span>
        <strong class="ok">{{ activeCount }}</strong>
        <span>활성 업체</span>
      </div>
      <div class="summary-card">
        <span class="mono summary-label">INACTIVE</span>
        <strong class="danger">{{ inactiveCount }}</strong>
        <span>비활성 업체</span>
      </div>
    </div>

    <div v-if="error" class="error-box">{{ error }}</div>

    <div class="card table-card">
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>업체 ID</th>
              <th>업체명</th>
              <th>주소</th>
              <th>연락처</th>
              <th>등록일</th>
              <th>상태</th>
              <th style="text-align:right">관리</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="7" class="placeholder-row">
                <AppIcon name="building" :size="20" class="ph-icon" />
                <span>업체 목록을 불러오는 중입니다.</span>
              </td>
            </tr>
            <tr v-else-if="companies.length === 0">
              <td colspan="7" class="placeholder-row">
                <AppIcon name="building" :size="20" class="ph-icon" />
                <span>표시할 업체가 없습니다.</span>
              </td>
            </tr>
            <tr v-for="company in companies" v-else :key="company.id">
              <td class="mono">#{{ company.id }}</td>
              <td>
                <strong>{{ company.name }}</strong>
              </td>
              <td>{{ company.address || '-' }}</td>
              <td class="mono">{{ company.phone || '-' }}</td>
              <td class="mono">{{ formatDate(company.createdAt) }}</td>
              <td>
                <span class="status-pill" :class="{ inactive: !company.active }">
                  {{ company.active ? 'ACTIVE' : 'INACTIVE' }}
                </span>
              </td>
              <td style="text-align:right">
                <button
                  class="btn btn-ghost action-btn"
                  :class="{ danger: company.active }"
                  @click="toggleCompany(company)"
                >
                  {{ company.active ? '비활성화' : '활성화' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
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
.view-header { display: flex; align-items: flex-start; justify-content: space-between; }
.view-title { font-size: 18px; font-weight: 700; color: var(--text-1); margin: 0 0 4px; }
.view-sub { font-size: 12.5px; color: var(--text-4); margin: 0; }

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.summary-card {
  background: var(--bg-2);
  border: 1px solid var(--line-1);
  border-radius: var(--r-md);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.summary-label {
  font-size: 10px;
  color: var(--text-4);
  letter-spacing: 0.08em;
}
.summary-card strong {
  font-size: 30px;
  line-height: 1;
  color: var(--text-1);
}
.summary-card strong.ok { color: var(--ok); }
.summary-card strong.danger { color: var(--danger); }
.summary-card span:last-child {
  font-size: 11px;
  color: var(--text-4);
}

.error-box {
  padding: 12px 14px;
  border-radius: var(--r-md);
  border: 1px solid rgba(181, 84, 74, 0.28);
  background: var(--danger-soft);
  color: var(--danger);
  font-size: 12.5px;
}

/* 테이블 */
.table-card { padding: 0; overflow: hidden; }
.table-wrap { overflow-x: auto; }
.table { width: 100%; border-collapse: collapse; font-size: 13px; }
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
}
.table td {
  padding: 13px 16px;
  color: var(--text-2);
  border-top: 1px solid var(--line-1);
  vertical-align: middle;
}
.placeholder-row {
  padding: 48px 16px;
  text-align: center;
  color: var(--text-4);
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.ph-icon { opacity: 0.4; }
.status-pill {
  display: inline-flex;
  align-items: center;
  padding: 3px 8px;
  border-radius: 999px;
  background: rgba(94, 138, 111, 0.14);
  color: var(--ok);
  border: 1px solid rgba(94, 138, 111, 0.22);
  font-family: var(--font-mono);
  font-size: 10.5px;
}
.status-pill.inactive {
  background: rgba(181, 84, 74, 0.12);
  color: var(--danger);
  border-color: rgba(181, 84, 74, 0.22);
}
.action-btn {
  font-size: 11.5px;
  padding: 5px 10px;
}
.action-btn.danger:hover {
  color: var(--danger);
  border-color: rgba(181, 84, 74, 0.28);
}
</style>
