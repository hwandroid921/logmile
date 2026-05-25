<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import AppIcon from '@/components/common/AppIcon.vue'
import { vehicleApi } from '@/api/vehicleApi'
import { driverApi } from '@/api/driverApi'

const list        = ref([])
const driverList  = ref([])   // assign 드롭다운용
const loading     = ref(true)
const error       = ref(null)
const search      = ref('')
const statusFilter = ref('ALL')

const pageSize    = ref(10)
const currentPage = ref(1)
watch([search, statusFilter, pageSize], () => { currentPage.value = 1 })

async function fetchData() {
  loading.value = true
  error.value   = null
  try {
    const [vRes, dRes] = await Promise.all([vehicleApi.getAll(), driverApi.getAll()])
    driverList.value = dRes.data
    const driverMap = Object.fromEntries(dRes.data.map(d => [d.id, d]))
    list.value = vRes.data.map(v => ({
      id:          v.id,
      plate:       v.plateNo,
      type:        v.type,
      driverId:    v.driverId,
      driverName:  v.driverId ? (driverMap[v.driverId]?.name ?? '—') : '미배정',
      driverPhone: v.driverId ? (driverMap[v.driverId]?.phone ?? '—') : '—',
      active:      v.active,
      createdAt:   v.createdAt ? v.createdAt.slice(0, 10) : '—',
    }))
  } catch (e) {
    error.value = '데이터를 불러오는 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

const filteredAll = computed(() => list.value.filter(v => {
  if (statusFilter.value === 'ACTIVE'   && !v.active)  return false
  if (statusFilter.value === 'INACTIVE' &&  v.active)  return false
  if (search.value && !(v.plate.includes(search.value) || v.driverName.includes(search.value))) return false
  return true
}))

const totalPages    = computed(() => Math.max(1, Math.ceil(filteredAll.value.length / pageSize.value)))
const paginated     = computed(() => {
  const s = (currentPage.value - 1) * pageSize.value
  return filteredAll.value.slice(s, s + pageSize.value)
})
const visiblePages  = computed(() => {
  const total = totalPages.value
  const cur   = currentPage.value
  let start = Math.max(1, cur - 2)
  let end   = Math.min(total, start + 4)
  if (end - start < 4) start = Math.max(1, end - 4)
  const pages = []
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

const activeCount   = computed(() => list.value.filter(v =>  v.active).length)
const inactiveCount = computed(() => list.value.filter(v => !v.active).length)

function statusChip(active) {
  return active
    ? { label: '활성', color: 'var(--ok)',     bg: 'var(--ok-soft)',     border: 'rgba(94,138,111,.3)' }
    : { label: '비활성', color: 'var(--text-3)', bg: 'var(--bg-4)',       border: 'var(--line-2)' }
}

/* ─── 모달 상태 ─── */
const showModal  = ref(false)
const modalMode  = ref('create')   // 'create' | 'edit'
const editTarget = ref(null)
const submitting = ref(false)
const form = ref({ plateNo: '', type: '', driverId: '', active: true })

const vehicleTypes = ['카고', '탑차', '윙바디', '냉동탑차', '트레일러', '기타']

// 배정되지 않은 운전자 + 현재 수정 대상 차량의 운전자 포함
const availableDrivers = computed(() => {
  return driverList.value.filter(d => !d.vehicleId || d.id === editTarget.value?.driverId)
})

function openCreate() {
  modalMode.value = 'create'
  editTarget.value = null
  form.value = { plateNo: '', type: '', driverId: '', active: true }
  showModal.value = true
}

function openEdit(v) {
  modalMode.value = 'edit'
  editTarget.value = v
  form.value = { plateNo: v.plate, type: v.type, driverId: v.driverId ?? '', active: v.active }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editTarget.value = null
}

async function submitModal() {
  if (!form.value.plateNo.trim() || !form.value.type) {
    alert('번호판과 차종은 필수 항목입니다.')
    return
  }
  submitting.value = true
  const payload = {
    plateNo:  form.value.plateNo.trim(),
    type:     form.value.type,
    driverId: form.value.driverId !== '' ? Number(form.value.driverId) : null,
    active:   form.value.active,
  }
  try {
    if (modalMode.value === 'create') {
      await vehicleApi.create(payload)
    } else {
      await vehicleApi.update(editTarget.value.id, payload)
    }
    closeModal()
    await fetchData()
  } catch (e) {
    alert('저장 중 오류가 발생했습니다.')
  } finally {
    submitting.value = false
  }
}

async function removeItem(v) {
  if (!confirm(`차량 "${v.plate}"을(를) 삭제하시겠습니까?`)) return
  try {
    await vehicleApi.remove(v.id)
    await fetchData()
  } catch (e) {
    alert('삭제 중 오류가 발생했습니다.')
  }
}
</script>

<template>
  <div class="view">
    <div class="breadcrumb mono">
      관리자 / 차량 목록 · {{ list.length }}건
    </div>

    <div class="page-header">
      <h2 class="page-title">차량 관리</h2>
      <div style="display:flex;gap:8px;align-items:center;">
        <button class="btn btn-ghost hdr-btn" @click="fetchData">
          <AppIcon name="refresh" :size="13" />새로고침
        </button>
        <button class="btn btn-primary hdr-btn" @click="openCreate">+ 차량 등록</button>
      </div>
    </div>

    <!-- 요약 카드 -->
    <div class="sum-row">
      <div class="sum-card">
        <span class="sum-dot" style="background:var(--ok)" />
        <span class="sum-lbl mono">운행 가능</span>
        <span class="sum-val mono" style="color: var(--ok)">{{ activeCount }}</span>
      </div>
      <div class="sum-card">
        <span class="sum-dot" style="background:var(--text-3)" />
        <span class="sum-lbl mono">비활성</span>
        <span class="sum-val mono" style="color: var(--text-3)">{{ inactiveCount }}</span>
      </div>
      <div class="sum-card">
        <span class="sum-dot" style="background:var(--text-3)" />
        <span class="sum-lbl mono">전체</span>
        <span class="sum-val mono">{{ list.length }}</span>
      </div>
    </div>

    <!-- 필터 -->
    <div class="filter-row">
      <input v-model="search" class="search-inp" placeholder="번호판 / 운전자 검색" />
      <div class="filter-group">
        <span class="filter-lbl mono">상태</span>
        <div class="filter-btns">
          <button v-for="s in ['ALL','ACTIVE','INACTIVE']" :key="s"
            class="filter-btn mono" :class="{ active: statusFilter===s }"
            @click="statusFilter=s">{{ s === 'ALL' ? '전체' : s === 'ACTIVE' ? '운행 가능' : '비활성' }}</button>
        </div>
      </div>
      <select v-model.number="pageSize" class="pg-sel mono">
        <option :value="10">10개</option>
        <option :value="20">20개</option>
        <option :value="50">50개</option>
      </select>
    </div>

    <!-- 로딩 / 에러 -->
    <div v-if="loading" class="state-row mono">데이터 로드 중...</div>
    <div v-else-if="error" class="state-row" style="color: var(--danger)">{{ error }}</div>

    <!-- 테이블 -->
    <div v-else class="card table-card">
      <div class="table-wrap">
        <table class="tbl">
          <thead>
            <tr>
              <th>번호판</th>
              <th>차종</th>
              <th>배정 운전자</th>
              <th>연락처</th>
              <th>상태</th>
              <th>등록일</th>
              <th style="text-align:right">관리</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="v in paginated" :key="v.id">
              <td>
                <div class="mono plate">{{ v.plate }}</div>
                <div class="sub-txt">ID {{ v.id }}</div>
              </td>
              <td class="sub-txt">{{ v.type }}</td>
              <td>
                <div class="cell-name">{{ v.driverName }}</div>
              </td>
              <td class="mono" style="font-size: 14px;color: var(--text-2)">{{ v.driverPhone }}</td>
              <td>
                <span class="status-chip mono"
                  :style="{ color: statusChip(v.active).color, background: statusChip(v.active).bg, borderColor: statusChip(v.active).border }">
                  {{ statusChip(v.active).label }}
                </span>
              </td>
              <td class="mono sub-txt">{{ v.createdAt }}</td>
              <td style="text-align:right;white-space:nowrap;">
                <button class="act-btn" @click="openEdit(v)">수정</button>
                <button class="act-btn act-del" @click="removeItem(v)">삭제</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="filteredAll.length===0" class="empty-row">조건에 맞는 차량이 없습니다.</div>
      </div>
      <!-- 페이지네이션 -->
      <div v-if="filteredAll.length > 0" class="pg-footer">
        <span class="pg-info mono">총 {{ filteredAll.length }}건</span>
        <div class="pg-nav">
          <button class="pg-btn mono" :disabled="currentPage===1" @click="currentPage=1">«</button>
          <button class="pg-btn mono" :disabled="currentPage===1" @click="currentPage--">‹</button>
          <button v-for="p in visiblePages" :key="p"
            class="pg-btn mono" :class="{ active: p===currentPage }"
            @click="currentPage=p">{{ p }}</button>
          <button class="pg-btn mono" :disabled="currentPage===totalPages" @click="currentPage++">›</button>
          <button class="pg-btn mono" :disabled="currentPage===totalPages" @click="currentPage=totalPages">»</button>
        </div>
        <span class="pg-info mono">{{ (currentPage-1)*pageSize+1 }}–{{ Math.min(currentPage*pageSize, filteredAll.length) }}</span>
      </div>
    </div>

    <!-- 등록/수정 모달 -->
    <div v-if="showModal" class="modal-backdrop" @click.self="closeModal">
      <div class="modal">
        <div class="modal-hdr">
          <span class="modal-title">{{ modalMode === 'create' ? '차량 등록' : '차량 수정' }}</span>
          <button class="modal-close" @click="closeModal">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <label class="form-lbl">번호판 <span class="req">*</span></label>
            <input v-model="form.plateNo" class="form-inp mono" placeholder="예) 12가 3456" />
          </div>
          <div class="form-row">
            <label class="form-lbl">차종 <span class="req">*</span></label>
            <select v-model="form.type" class="form-inp">
              <option value="" disabled>차종 선택</option>
              <option v-for="t in vehicleTypes" :key="t" :value="t">{{ t }}</option>
            </select>
          </div>
          <div class="form-row">
            <label class="form-lbl">배정 운전자</label>
            <select v-model="form.driverId" class="form-inp">
              <option value="">미배정</option>
              <option v-for="d in availableDrivers" :key="d.id" :value="d.id">{{ d.name }} ({{ d.phone }})</option>
            </select>
          </div>
          <div class="form-row" style="align-items:center;">
            <label class="form-lbl">활성 상태</label>
            <label class="toggle-wrap">
              <input type="checkbox" v-model="form.active" />
              <span class="toggle-label">{{ form.active ? '활성' : '비활성' }}</span>
            </label>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-ghost" @click="closeModal">취소</button>
          <button class="btn btn-primary" :disabled="submitting" @click="submitModal">
            {{ submitting ? '저장 중...' : (modalMode === 'create' ? '등록' : '저장') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view { display:flex; flex-direction:column; gap:16px; padding:32px 32px 40px; }
.breadcrumb { font-size: 14px; color: var(--text-3); letter-spacing:0.04em; }

.page-header { display:flex; align-items:center; justify-content:space-between; }
.page-title  { font-size:24px; font-weight:700; color: var(--text-1); margin:0; letter-spacing: 0; }
.hdr-btn { display:flex; align-items:center; gap:5px; font-size: 14px; padding:7px 13px; }

.sum-row { display:flex; gap:10px; }
.sum-card {
  display:flex; align-items:center; gap:8px; padding:10px 16px;
  background:var(--bg-2); border:1px solid var(--line-1); border-radius:var(--r-md); flex:1;
}
.sum-dot { width:8px; height:8px; border-radius:50%; flex-shrink:0; }
.sum-lbl { font-size: 14px; letter-spacing:0.08em; color: var(--text-3); flex:1; }
.sum-val { font-size:20px; font-weight:700; color: var(--text-2); }

.filter-row { display:flex; align-items:center; gap:16px; flex-wrap:wrap; }
.search-inp {
  flex:1; min-width:240px; padding:8px 12px;
  border:1px solid var(--line-2); border-radius:var(--r-md);
  background:var(--bg-1); color: var(--text-1); font-size: 14px; outline:none;
  transition:border-color .15s;
}
.search-inp:focus { border-color: var(--accent-line); }
.filter-group { display:flex; align-items:center; gap:8px; }
.filter-lbl   { font-size: 14px; color: var(--text-3); letter-spacing:0.08em; white-space:nowrap; }
.filter-btns  { display:flex; gap:3px; }
.filter-btn {
  padding:4px 9px; border-radius:var(--r-sm); font-size: 14px;
  background:none; border:1px solid var(--line-2); color: var(--text-3);
  cursor:pointer; transition:all .12s;
}
.filter-btn.active {
  background:var(--accent-soft); border-color: var(--accent-line); color: var(--accent); font-weight:600;
}

.state-row { padding:40px; text-align:center; font-size: 14px; color: var(--text-3); }

.table-card { padding:0; overflow:hidden; }
.table-wrap { overflow-x:auto; }
.tbl { width:100%; border-collapse:collapse; font-size: 14px; }
.tbl th {
  padding:10px 14px; text-align:left;
  font-size: 16px; font-weight: 700; letter-spacing:0.07em; text-transform:uppercase;
  color: var(--text-3); background:var(--bg-3); border-bottom:1px solid var(--line-1);
  white-space:nowrap;
}
.tbl td { padding:11px 14px; color: var(--text-2); border-bottom:1px solid var(--line-1); vertical-align:middle; }
.tbl tbody tr:last-child td { border-bottom:none; }
.tbl tbody tr:hover td { background:var(--bg-3); }

.plate     { font-size: 16px; font-weight:700; color: var(--text-1); }
.cell-name { font-size: 16px; font-weight: 600; color: var(--text-1); }
.sub-txt   { font-size: 14px; color: var(--text-3); margin-top:2px; }

.status-chip {
  display:inline-block; padding:2px 8px; border-radius:var(--r-sm);
  font-size: 14px; font-weight:600; letter-spacing:0.06em; border:1px solid;
}

.act-btn {
  font-size: 14px; padding:3px 9px; margin-left:4px;
  border-radius:var(--r-sm); cursor:pointer; border:1px solid var(--line-2);
  background:transparent; color: var(--text-2); font-family:var(--font-mono);
  transition:all .12s;
}
.act-btn:hover     { background:var(--accent-soft); border-color: var(--accent-line); color: var(--accent); }
.act-del:hover     { background:rgba(181,84,74,.12); border-color:rgba(181,84,74,.4); color: var(--danger); }

.empty-row { padding:40px; text-align:center; color: var(--text-3); font-size: 14px; }

/* 페이지네이션 */
.pg-footer {
  display:flex; align-items:center; justify-content:space-between;
  padding:10px 14px; border-top:1px solid var(--line-1); background:var(--bg-2);
}
.pg-info { font-size:11px; color:var(--text-4); }
.pg-nav  { display:flex; align-items:center; gap:4px; }
.pg-btn  {
  min-width:28px; height:26px; padding:0 6px;
  border:1px solid var(--line-2); border-radius:var(--r-sm);
  background:var(--bg-1); color:var(--text-2); font-size:11px;
  cursor:pointer; transition:all .12s; font-family:var(--font-mono);
}
.pg-btn:hover:not(:disabled) { border-color:var(--accent-line); color:var(--accent); background:var(--accent-soft); }
.pg-btn:disabled { opacity:.35; cursor:default; }
.pg-btn.active { background:var(--accent); border-color:var(--accent); color:#fff; font-weight:700; }
.pg-sel {
  padding:3px 8px; border:1px solid var(--line-2); border-radius:var(--r-sm);
  background:var(--bg-1); color:var(--text-2); font-size:11px; cursor:pointer; outline:none;
}
.pg-sel:focus { border-color:var(--accent-line); }

/* 모달 */
.modal-backdrop {
  position:fixed; inset:0; background:rgba(0,0,0,.45); z-index:200;
  display:flex; align-items:center; justify-content:center;
}
.modal {
  background:var(--bg-1); border:1px solid var(--line-1); border-radius:var(--r-lg);
  width:420px; max-width:95vw; box-shadow:0 20px 60px rgba(0,0,0,.35);
}
.modal-hdr {
  display:flex; align-items:center; justify-content:space-between;
  padding:16px 20px; border-bottom:1px solid var(--line-1);
}
.modal-title { font-size:15px; font-weight:700; color: var(--text-1); }
.modal-close {
  background:none; border:none; cursor:pointer; color: var(--text-3);
  font-size:16px; padding:0 4px; transition:color .12s;
}
.modal-close:hover { color: var(--text-1); }
.modal-body   { padding:20px; display:flex; flex-direction:column; gap:14px; }
.modal-footer { display:flex; justify-content:flex-end; gap:8px; padding:14px 20px; border-top:1px solid var(--line-1); }

.form-row { display:flex; flex-direction:column; gap:5px; }
.form-lbl { font-size: 14px; color: var(--text-3); font-weight:600; }
.req      { color: var(--danger); }
.form-inp {
  padding:8px 11px; border:1px solid var(--line-2); border-radius:var(--r-sm);
  background:var(--bg-2); color: var(--text-1); font-size: 14px; outline:none;
  transition:border-color .15s;
}
.form-inp:focus { border-color: var(--accent-line); }

.toggle-wrap { display:flex; align-items:center; gap:8px; cursor:pointer; }
.toggle-label { font-size: 14px; color: var(--text-2); }
</style>
