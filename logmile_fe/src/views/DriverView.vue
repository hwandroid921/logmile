<script setup>
import { ref, computed, onMounted } from 'vue'
import AppIcon from '@/components/common/AppIcon.vue'
import { driverApi } from '@/api/driverApi'
import { vehicleApi } from '@/api/vehicleApi'

const list       = ref([])
const vehicleList = ref([])   // 배정 드롭다운용
const loading    = ref(true)
const error      = ref(null)
const search     = ref('')
const assignFilter = ref('ALL')

async function fetchData() {
  loading.value = true
  error.value   = null
  try {
    const [dRes, vRes] = await Promise.all([driverApi.getAll(), vehicleApi.getAll()])
    vehicleList.value = vRes.data
    const vehicleMap = Object.fromEntries(vRes.data.map(v => [v.id, v]))
    list.value = dRes.data.map(d => ({
      id:          d.id,
      name:        d.name,
      phone:       d.phone,
      license:     d.licenseType,
      vehicleId:   d.vehicleId,
      plate:       d.vehicleId ? (vehicleMap[d.vehicleId]?.plateNo ?? '—') : '미배정',
      vehicleType: d.vehicleId ? (vehicleMap[d.vehicleId]?.type ?? '—') : '—',
      createdAt:   d.createdAt ? d.createdAt.slice(0, 10) : '—',
    }))
  } catch (e) {
    error.value = '데이터를 불러오는 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

const filtered = computed(() => list.value.filter(d => {
  if (assignFilter.value === 'ASSIGNED'   &&  !d.vehicleId) return false
  if (assignFilter.value === 'UNASSIGNED' && !!d.vehicleId) return false
  if (search.value && !(
    d.name.includes(search.value) ||
    d.plate.includes(search.value) ||
    d.phone.includes(search.value)
  )) return false
  return true
}))

const assignedCount   = computed(() => list.value.filter(d =>  d.vehicleId).length)
const unassignedCount = computed(() => list.value.filter(d => !d.vehicleId).length)

/* ─── 등록/수정 모달 ─── */
const showModal  = ref(false)
const modalMode  = ref('create')   // 'create' | 'edit'
const editTarget = ref(null)
const submitting = ref(false)
const form = ref({ name: '', phone: '', licenseType: '' })

const licenseTypes = ['1종 대형', '1종 보통', '2종 보통', '특수(트레일러)', '특수(레커)']

function openCreate() {
  modalMode.value = 'create'
  editTarget.value = null
  form.value = { name: '', phone: '', licenseType: '' }
  showModal.value = true
}

function openEdit(d) {
  modalMode.value = 'edit'
  editTarget.value = d
  form.value = { name: d.name, phone: d.phone, licenseType: d.license }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editTarget.value = null
}

async function submitModal() {
  if (!form.value.name.trim() || !form.value.phone.trim() || !form.value.licenseType) {
    alert('이름, 전화번호, 면허 종류는 필수 항목입니다.')
    return
  }
  submitting.value = true
  const payload = {
    name:        form.value.name.trim(),
    phone:       form.value.phone.trim(),
    licenseType: form.value.licenseType,
  }
  try {
    if (modalMode.value === 'create') {
      await driverApi.create(payload)
    } else {
      await driverApi.update(editTarget.value.id, payload)
    }
    closeModal()
    await fetchData()
  } catch (e) {
    alert('저장 중 오류가 발생했습니다.')
  } finally {
    submitting.value = false
  }
}

async function removeItem(d) {
  if (!confirm(`운전자 "${d.name}"을(를) 삭제하시겠습니까?`)) return
  try {
    await driverApi.remove(d.id)
    await fetchData()
  } catch (e) {
    alert('삭제 중 오류가 발생했습니다.')
  }
}

/* ─── 차량 배정 모달 ─── */
const showAssignModal  = ref(false)
const assignTarget     = ref(null)
const selectedVehicleId = ref('')
const assigning        = ref(false)

// 배정되지 않은 차량 + 현재 운전자의 차량 포함
const availableVehicles = computed(() => {
  return vehicleList.value.filter(v => v.active && (!v.driverId || v.id === assignTarget.value?.vehicleId))
})

function openAssign(d) {
  assignTarget.value = d
  selectedVehicleId.value = d.vehicleId ?? ''
  showAssignModal.value = true
}

function closeAssignModal() {
  showAssignModal.value = false
  assignTarget.value = null
  selectedVehicleId.value = ''
}

async function submitAssign() {
  if (!selectedVehicleId.value) {
    alert('배정할 차량을 선택해 주세요.')
    return
  }
  assigning.value = true
  try {
    await driverApi.assignVehicle(assignTarget.value.id, Number(selectedVehicleId.value))
    closeAssignModal()
    await fetchData()
  } catch (e) {
    alert('배정 중 오류가 발생했습니다.')
  } finally {
    assigning.value = false
  }
}

async function doUnassign(d) {
  if (!confirm(`"${d.name}" 운전자의 차량 배정을 해제하시겠습니까?`)) return
  try {
    await driverApi.unassign(d.id)
    await fetchData()
  } catch (e) {
    alert('배정 해제 중 오류가 발생했습니다.')
  }
}
</script>

<template>
  <div class="view">
    <div class="breadcrumb mono">
      ADMIN / DRIVER_TABLE · {{ list.length }} record(s)
    </div>

    <div class="page-header">
      <h2 class="page-title">운전자 관리</h2>
      <div style="display:flex;gap:8px;align-items:center;">
        <button class="btn btn-ghost hdr-btn" @click="fetchData">
          <AppIcon name="refresh" :size="13" />새로고침
        </button>
        <button class="btn btn-primary hdr-btn" @click="openCreate">+ 운전자 등록</button>
      </div>
    </div>

    <!-- 요약 카드 -->
    <div class="sum-row">
      <div class="sum-card">
        <span class="sum-dot" style="background:var(--accent)" />
        <span class="sum-lbl mono">ASSIGNED</span>
        <span class="sum-val mono" style="color:var(--accent)">{{ assignedCount }}</span>
      </div>
      <div class="sum-card">
        <span class="sum-dot" style="background:var(--text-3)" />
        <span class="sum-lbl mono">UNASSIGNED</span>
        <span class="sum-val mono" style="color:var(--text-3)">{{ unassignedCount }}</span>
      </div>
      <div class="sum-card">
        <span class="sum-dot" style="background:var(--text-4)" />
        <span class="sum-lbl mono">TOTAL</span>
        <span class="sum-val mono">{{ list.length }}</span>
      </div>
    </div>

    <!-- 필터 -->
    <div class="filter-row">
      <input v-model="search" class="search-inp" placeholder="이름 / 번호판 / 전화 검색" />
      <div class="filter-group">
        <span class="filter-lbl mono">배정</span>
        <div class="filter-btns">
          <button v-for="s in ['ALL','ASSIGNED','UNASSIGNED']" :key="s"
            class="filter-btn mono" :class="{ active: assignFilter===s }"
            @click="assignFilter=s">{{ s }}</button>
        </div>
      </div>
    </div>

    <!-- 로딩 / 에러 -->
    <div v-if="loading" class="state-row mono">데이터 로드 중...</div>
    <div v-else-if="error" class="state-row" style="color:var(--danger)">{{ error }}</div>

    <!-- 테이블 -->
    <div v-else class="card table-card">
      <div class="table-wrap">
        <table class="tbl">
          <thead>
            <tr>
              <th>운전자</th>
              <th>전화번호</th>
              <th>면허</th>
              <th>배정 차량</th>
              <th>차종</th>
              <th>등록일</th>
              <th style="text-align:right">관리</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="d in filtered" :key="d.id">
              <td>
                <div style="display:flex;align-items:center;gap:10px;">
                  <div class="avatar">{{ d.name.charAt(0) }}</div>
                  <div>
                    <div class="cell-name">{{ d.name }}</div>
                    <div class="sub-txt">ID {{ d.id }}</div>
                  </div>
                </div>
              </td>
              <td class="mono" style="font-size:12px;color:var(--text-2)">{{ d.phone }}</td>
              <td class="sub-txt">{{ d.license }}</td>
              <td>
                <div v-if="d.vehicleId" style="display:flex;align-items:center;gap:6px;">
                  <div class="mono plate-txt">{{ d.plate }}</div>
                  <button class="act-btn act-unassign" @click="doUnassign(d)" title="배정 해제">✕</button>
                </div>
                <div v-else style="display:flex;align-items:center;gap:6px;">
                  <span class="sub-txt">미배정</span>
                  <button class="act-btn act-assign" @click="openAssign(d)">배정</button>
                </div>
              </td>
              <td class="sub-txt">{{ d.vehicleType !== '—' ? d.vehicleType : '—' }}</td>
              <td class="mono sub-txt">{{ d.createdAt }}</td>
              <td style="text-align:right;white-space:nowrap;">
                <button class="act-btn" @click="openEdit(d)">수정</button>
                <button class="act-btn act-del" @click="removeItem(d)">삭제</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="filtered.length===0" class="empty-row">조건에 맞는 운전자가 없습니다.</div>
      </div>
    </div>

    <!-- 운전자 등록/수정 모달 -->
    <div v-if="showModal" class="modal-backdrop" @click.self="closeModal">
      <div class="modal">
        <div class="modal-hdr">
          <span class="modal-title">{{ modalMode === 'create' ? '운전자 등록' : '운전자 수정' }}</span>
          <button class="modal-close" @click="closeModal">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <label class="form-lbl">이름 <span class="req">*</span></label>
            <input v-model="form.name" class="form-inp" placeholder="홍길동" />
          </div>
          <div class="form-row">
            <label class="form-lbl">전화번호 <span class="req">*</span></label>
            <input v-model="form.phone" class="form-inp mono" placeholder="010-0000-0000" />
          </div>
          <div class="form-row">
            <label class="form-lbl">면허 종류 <span class="req">*</span></label>
            <select v-model="form.licenseType" class="form-inp">
              <option value="" disabled>면허 선택</option>
              <option v-for="l in licenseTypes" :key="l" :value="l">{{ l }}</option>
            </select>
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

    <!-- 차량 배정 모달 -->
    <div v-if="showAssignModal" class="modal-backdrop" @click.self="closeAssignModal">
      <div class="modal">
        <div class="modal-hdr">
          <span class="modal-title">차량 배정 — {{ assignTarget?.name }}</span>
          <button class="modal-close" @click="closeAssignModal">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <label class="form-lbl">배정할 차량 <span class="req">*</span></label>
            <select v-model="selectedVehicleId" class="form-inp">
              <option value="" disabled>차량 선택</option>
              <option v-for="v in availableVehicles" :key="v.id" :value="v.id">
                {{ v.plateNo }} ({{ v.type }})
              </option>
            </select>
            <div v-if="availableVehicles.length === 0" style="font-size:11.5px;color:var(--text-4);margin-top:4px">
              배정 가능한 활성 차량이 없습니다.
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-ghost" @click="closeAssignModal">취소</button>
          <button class="btn btn-primary" :disabled="assigning || availableVehicles.length === 0" @click="submitAssign">
            {{ assigning ? '배정 중...' : '배정' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view { display:flex; flex-direction:column; gap:16px; padding:32px 32px 40px; }
.breadcrumb { font-size:11px; color:var(--text-4); letter-spacing:0.04em; }
.page-header { display:flex; align-items:center; justify-content:space-between; }
.page-title  { font-size:24px; font-weight:700; color:var(--text-1); margin:0; letter-spacing:-0.01em; }
.hdr-btn { display:flex; align-items:center; gap:5px; font-size:12.5px; padding:7px 13px; }

.sum-row  { display:flex; gap:10px; }
.sum-card {
  display:flex; align-items:center; gap:8px; padding:10px 16px;
  background:var(--bg-2); border:1px solid var(--line-1); border-radius:var(--r-md); flex:1;
}
.sum-dot { width:8px; height:8px; border-radius:50%; flex-shrink:0; }
.sum-lbl { font-size:10px; letter-spacing:0.08em; color:var(--text-4); flex:1; }
.sum-val { font-size:20px; font-weight:700; color:var(--text-2); }

.filter-row  { display:flex; align-items:center; gap:16px; flex-wrap:wrap; }
.search-inp  {
  flex:1; min-width:240px; padding:8px 12px;
  border:1px solid var(--line-2); border-radius:var(--r-md);
  background:var(--bg-1); color:var(--text-1); font-size:13px; outline:none; transition:border-color .15s;
}
.search-inp:focus { border-color:var(--accent-line); }
.filter-group { display:flex; align-items:center; gap:8px; }
.filter-lbl   { font-size:10px; color:var(--text-4); letter-spacing:0.08em; }
.filter-btns  { display:flex; gap:3px; }
.filter-btn {
  padding:4px 9px; border-radius:var(--r-sm); font-size:10.5px;
  background:none; border:1px solid var(--line-2); color:var(--text-3); cursor:pointer; transition:all .12s;
}
.filter-btn.active { background:var(--accent-soft); border-color:var(--accent-line); color:var(--accent); font-weight:600; }

.state-row { padding:40px; text-align:center; font-size:13px; color:var(--text-4); }

.table-card { padding:0; overflow:hidden; }
.table-wrap { overflow-x:auto; }
.tbl { width:100%; border-collapse:collapse; font-size:13px; }
.tbl th {
  padding:10px 14px; text-align:left;
  font-size:10px; font-weight:600; letter-spacing:0.07em; text-transform:uppercase;
  color:var(--text-4); background:var(--bg-3); border-bottom:1px solid var(--line-1); white-space:nowrap;
}
.tbl td { padding:11px 14px; color:var(--text-2); border-bottom:1px solid var(--line-1); vertical-align:middle; }
.tbl tbody tr:last-child td { border-bottom:none; }
.tbl tbody tr:hover td { background:var(--bg-3); }

.avatar {
  width:32px; height:32px; border-radius:50%; flex-shrink:0;
  background:var(--accent-soft); color:var(--accent);
  display:flex; align-items:center; justify-content:center;
  font-size:13px; font-weight:700;
}
.cell-name { font-size:13px; font-weight:500; color:var(--text-1); }
.sub-txt   { font-size:11px; color:var(--text-4); margin-top:2px; }
.plate-txt { font-size:12px; color:var(--text-2); }

.act-btn {
  font-size:11px; padding:3px 9px; margin-left:4px;
  border-radius:var(--r-sm); cursor:pointer; border:1px solid var(--line-2);
  background:transparent; color:var(--text-2); font-family:var(--font-mono);
  transition:all .12s;
}
.act-btn:first-child { margin-left:0; }
.act-btn:hover        { background:var(--accent-soft); border-color:var(--accent-line); color:var(--accent); }
.act-del:hover        { background:rgba(181,84,74,.12); border-color:rgba(181,84,74,.4); color:var(--danger); }
.act-assign           { font-size:10px; padding:2px 7px; color:var(--accent); border-color:var(--accent-line); background:var(--accent-soft); }
.act-unassign         { font-size:10px; padding:2px 6px; color:var(--text-3); }
.act-unassign:hover   { background:rgba(181,84,74,.12); border-color:rgba(181,84,74,.4); color:var(--danger); }

.empty-row { padding:40px; text-align:center; color:var(--text-4); font-size:13px; }

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
.modal-title { font-size:15px; font-weight:700; color:var(--text-1); }
.modal-close {
  background:none; border:none; cursor:pointer; color:var(--text-3);
  font-size:16px; padding:0 4px; transition:color .12s;
}
.modal-close:hover { color:var(--text-1); }
.modal-body   { padding:20px; display:flex; flex-direction:column; gap:14px; }
.modal-footer { display:flex; justify-content:flex-end; gap:8px; padding:14px 20px; border-top:1px solid var(--line-1); }

.form-row { display:flex; flex-direction:column; gap:5px; }
.form-lbl { font-size:11.5px; color:var(--text-3); font-weight:600; }
.req      { color:var(--danger); }
.form-inp {
  padding:8px 11px; border:1px solid var(--line-2); border-radius:var(--r-sm);
  background:var(--bg-2); color:var(--text-1); font-size:13px; outline:none;
  transition:border-color .15s;
}
.form-inp:focus { border-color:var(--accent-line); }
</style>
