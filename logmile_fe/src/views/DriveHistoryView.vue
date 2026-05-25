<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '@/components/common/AppIcon.vue'
import { driveHistoryApi } from '@/api/driveHistoryApi'

const router = useRouter()

const logs        = ref([])
const selectedId  = ref(null)
const detail      = ref(null)
const loading     = ref(true)
const detailLoading = ref(false)
const error       = ref(null)

const filters = ref({ q: '', status: 'ALL', level: 'ALL' })

const pageSize    = ref(10)
const currentPage = ref(1)
watch([filters, pageSize], () => { currentPage.value = 1 }, { deep: true })

function fmtMin(m) {
  if (!m && m !== 0) return '—'
  const h = Math.floor(m / 60), mn = m % 60
  return h > 0 ? `${h}h ${mn}m` : `${mn}m`
}
function fmtDate(dt) {
  if (!dt) return '—'
  return String(dt).slice(5, 16).replace('T', ' ')
}

async function fetchList() {
  loading.value = true
  error.value   = null
  try {
    const res = await driveHistoryApi.getList()
    logs.value = res.data.map(l => ({
      id:       l.driveLogId,
      plate:    l.plateNo,
      driver:   l.driverName,
      scenario: l.scenarioType,
      status:   l.status,
      startedAt: l.startedAt,
      endedAt:   l.endedAt,
      totalMin:  l.totalDrivingMinutes,
      peak:      l.maxFatigueScore ?? 0,
      level:     l.maxFatigueLevel ?? 'NORMAL',
    }))
    if (logs.value.length) {
      selectedId.value = logs.value[0].id
      await loadDetail(logs.value[0].id)
    }
  } catch (e) {
    error.value = '운행 이력을 불러오는 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

async function loadDetail(id) {
  detailLoading.value = true
  try {
    const res = await driveHistoryApi.getDetail(id)
    detail.value = res.data
  } catch (e) {
    detail.value = null
  } finally {
    detailLoading.value = false
  }
}

async function selectLog(id) {
  selectedId.value = id
  await loadDetail(id)
}

onMounted(fetchList)

const filteredAll = computed(() => logs.value.filter(l => {
  if (filters.value.status !== 'ALL' && l.status !== filters.value.status) return false
  if (filters.value.level  !== 'ALL' && l.level  !== filters.value.level)  return false
  if (filters.value.q && !(l.plate.includes(filters.value.q) || l.driver.includes(filters.value.q) || String(l.id).includes(filters.value.q))) return false
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

const selected = computed(() => logs.value.find(l => l.id === selectedId.value) || logs.value[0])

function scoreColor(s) {
  if (s >= 70) return 'var(--danger)'
  if (s >= 40) return 'var(--warn)'
  return 'var(--ok)'
}
function statusBadge(s) {
  if (s === 'RUNNING')   return { label: '운행중', color: 'var(--accent)' }
  if (s === 'COMPLETED') return { label: '완료',   color: 'var(--text-3)' }
  if (s === 'STOPPED')   return { label: '중단',   color: 'var(--danger)' }
  return { label: s ?? '—', color: 'var(--text-3)' }
}
function scenarioBadge(s) {
  if (s === 'A') return { label: 'A · 정상',      color: 'var(--ok)' }
  if (s === 'B') return { label: 'B · 경계',      color: 'var(--warn)' }
  if (s === 'C') return { label: 'C · 피로 누적', color: 'var(--danger)' }
  return { label: s ?? '—', color: 'var(--text-3)' }
}
function restTypeBadge(t) {
  if (t === 'SUFFICIENT') return { label: '충분 휴식', color: 'var(--ok)',     bg: 'rgba(94,138,111,.14)' }
  if (t === 'VALID')      return { label: '유효 휴식', color: 'var(--accent)', bg: 'rgba(81,95,122,.16)'  }
  return                         { label: '휴식 부족', color: 'var(--danger)', bg: 'rgba(181,84,74,.14)'  }
}
</script>

<template>
  <div class="view">
    <div class="breadcrumb mono">
      관리자 / 운행 이력 · {{ logs.length }}건
    </div>

    <div class="page-header">
      <div>
        <h2 class="page-title">운행 이력</h2>
        <p class="page-sub">drive_log · fatigue_event · rest_event 통합 표시</p>
      </div>
      <div style="display:flex;gap:8px;">
        <button class="btn btn-ghost small-btn" @click="filters={q:'',status:'ALL',level:'ALL'}">필터 초기화</button>
        <button class="btn btn-ghost small-btn" @click="fetchList"><AppIcon name="refresh" :size="12" />새로고침</button>
      </div>
    </div>

    <!-- 로딩 / 에러 -->
    <div v-if="loading" class="state-row mono">운행 이력 로드 중...</div>
    <div v-else-if="error" class="state-row" style="color: var(--danger)">{{ error }}</div>

    <template v-else>
      <!-- 필터 -->
      <div class="filter-row">
        <input v-model="filters.q" class="search-inp" placeholder="번호판 / 운전자 / ID 검색" />
        <div class="filter-group">
          <span class="filter-lbl mono">상태</span>
          <div class="filter-btns">
            <button v-for="s in ['ALL','RUNNING','COMPLETED','STOPPED']" :key="s"
              class="filter-btn mono" :class="{ active: filters.status===s }"
              @click="filters.status=s">{{ s === 'ALL' ? '전체' : statusBadge(s).label }}</button>
          </div>
        </div>
        <div class="filter-group">
          <span class="filter-lbl mono">등급</span>
          <div class="filter-btns">
            <button v-for="l in ['ALL','NORMAL','CAUTION','DANGER']" :key="l"
              class="filter-btn mono" :class="{ active: filters.level===l }"
              @click="filters.level=l">{{ l === 'ALL' ? '전체' : l === 'NORMAL' ? '정상' : l === 'CAUTION' ? '주의' : '위험' }}</button>
          </div>
        </div>
        <select v-model.number="pageSize" class="pg-sel mono">
          <option :value="10">10개</option>
          <option :value="20">20개</option>
          <option :value="50">50개</option>
        </select>
      </div>

      <!-- 테이블 + 상세 -->
      <div class="content-grid">

        <!-- 좌측: 테이블 -->
        <div class="card table-card">
          <div class="table-wrap">
            <table class="tbl">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>차량 / 운전자</th>
                  <th>시나리오</th>
                  <th>상태</th>
                  <th>운행시간</th>
                  <th style="text-align:right">최고점수</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="l in paginated" :key="l.id"
                  class="log-row" :class="{ active: l.id === selectedId }"
                  @click="selectLog(l.id)">
                  <td>
                    <div class="mono id-txt">#{{ l.id }}</div>
                    <div class="sub-txt">{{ fmtDate(l.startedAt) }}</div>
                  </td>
                  <td>
                    <div class="cell-name">{{ l.plate }}</div>
                    <div class="sub-txt">{{ l.driver }}</div>
                  </td>
                  <td>
                    <span class="sc-badge mono" :style="{ color: scenarioBadge(l.scenario).color }">
                      {{ scenarioBadge(l.scenario).label }}
                    </span>
                  </td>
                  <td>
                    <span class="st-badge mono" :style="{ borderColor: statusBadge(l.status).color, color: statusBadge(l.status).color }">
                      {{ statusBadge(l.status).label }}
                    </span>
                  </td>
                  <td class="mono cell-sm">{{ fmtMin(l.totalMin) }}</td>
                  <td style="text-align:right">
                    <div class="mono peak-score" :style="{ color: scoreColor(l.peak) }">{{ l.peak }}</div>
                    <div class="peak-label" :style="{ color: scoreColor(l.peak) }">{{ l.level }}</div>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="filteredAll.length===0" class="empty-row">조건에 맞는 운행 기록이 없습니다.</div>
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

        <!-- 우측: 상세 패널 -->
        <div class="card detail-card" v-if="selected">
          <div class="detail-header">
            <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px;">
              <span class="mono" style="font-size: 14px;color: var(--text-3)">#{{ selected.id }}</span>
              <span class="st-badge mono" :style="{ borderColor: statusBadge(selected.status).color, color: statusBadge(selected.status).color }">
                {{ statusBadge(selected.status).label }}
              </span>
            </div>
            <div style="display:flex;align-items:flex-end;gap:14px;">
              <div>
                <div style="font-size:18px;font-weight:700;color: var(--text-1)">{{ selected.plate }}</div>
                <div class="sub-txt">{{ selected.driver }} · 시작 {{ fmtDate(selected.startedAt) }}</div>
              </div>
              <div style="margin-left:auto;text-align:right;">
                <div class="mono" style="font-size:30px;font-weight:800;line-height:1" :style="{ color: scoreColor(selected.peak) }">{{ selected.peak }}</div>
                <div class="mono" style="font-size: 14px;margin-top:2px" :style="{ color: scoreColor(selected.peak) }">최고 · {{ selected.level === 'DANGER' ? '위험' : selected.level === 'CAUTION' ? '주의' : '정상' }}</div>
              </div>
            </div>
          </div>

          <!-- 메타 -->
          <div class="detail-meta">
            <div>
              <div class="meta-lbl">시나리오</div>
              <div class="mono" style="font-size: 14px;font-weight:700;margin-top:4px" :style="{ color: scenarioBadge(selected.scenario).color }">
                {{ scenarioBadge(selected.scenario).label }}
              </div>
            </div>
            <div>
              <div class="meta-lbl">총 운행</div>
              <div class="mono" style="font-size: 14px;font-weight:700;color: var(--text-1);margin-top:4px">{{ fmtMin(selected.totalMin) }}</div>
            </div>
            <div>
              <div class="meta-lbl">종료 시각</div>
              <div class="mono" style="font-size: 14px;font-weight:600;color: var(--text-2);margin-top:4px">{{ fmtDate(selected.endedAt) || '운행중' }}</div>
            </div>
          </div>

          <!-- 로딩 -->
          <div v-if="detailLoading" class="detail-section mono" style="font-size: 14px;color: var(--text-3)">상세 정보 로드 중...</div>

          <!-- 휴식 이벤트 -->
          <div v-else-if="detail" class="detail-section">
            <div class="section-title">
              휴식 이벤트
              <span class="mono" style="font-size: 14px;color: var(--text-3);margin-left:6px">
                {{ detail.restEvents?.length ?? 0 }}건
              </span>
            </div>
            <template v-if="detail.restEvents?.length">
              <div v-for="r in detail.restEvents" :key="r.id" class="rest-row">
                <span class="mono" style="color: var(--text-2);flex:1;font-size: 14px">
                  {{ fmtDate(r.restStartedAt) }} → {{ fmtDate(r.restEndedAt) }}
                </span>
                <span class="mono" style="font-weight:700;color: var(--text-1)">{{ r.restMinutes }}m</span>
                <span class="rest-badge mono" :style="{ color: restTypeBadge(r.restType).color, background: restTypeBadge(r.restType).bg }">
                  {{ restTypeBadge(r.restType).label }}
                </span>
              </div>
            </template>
            <div v-else class="mono" style="font-size: 14px;color: var(--text-3);padding:10px 0">
              기록된 휴식 이벤트가 없습니다.
            </div>
          </div>

          <!-- 상세 보기 버튼 -->
          <div class="detail-footer">
            <button class="btn btn-primary full-btn"
              @click="router.push({ name:'driveHistoryDetail', params:{ id: selected.id } })">
              <AppIcon name="arrow" :size="13" /> 전체 상세 보기
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.view { display:flex; flex-direction:column; gap:16px; padding:32px 32px 40px; }
.breadcrumb { font-size: 14px; color: var(--text-3); letter-spacing:0.04em; }

.page-header { display:flex; align-items:flex-start; justify-content:space-between; gap:12px; }
.page-title  { font-size:24px; font-weight:700; color: var(--text-1); margin:0 0 4px; letter-spacing: 0; }
.page-sub    { font-size: 14px; color: var(--text-3); margin:0; }
.small-btn   { font-size: 14px; padding:7px 12px; display:flex; align-items:center; gap:5px; }

.state-row { padding:40px; text-align:center; font-size: 14px; color: var(--text-3); }

.filter-row { display:flex; align-items:center; gap:12px; flex-wrap:wrap; }
.search-inp {
  flex:1; min-width:220px; padding:8px 12px;
  border:1px solid var(--line-2); border-radius:var(--r-md);
  background:var(--bg-1); color: var(--text-1); font-size: 14px; outline:none;
}
.search-inp:focus { border-color: var(--accent-line); }
.filter-group { display:flex; align-items:center; gap:6px; }
.filter-lbl   { font-size: 14px; color: var(--text-3); letter-spacing:0.06em; white-space:nowrap; }
.filter-btns  { display:flex; gap:3px; }
.filter-btn {
  padding:4px 9px; border-radius:var(--r-sm); font-size: 14px;
  background:none; border:1px solid var(--line-2); color: var(--text-3); cursor:pointer; transition:all .12s;
}
.filter-btn.active { background:var(--accent-soft); border-color: var(--accent-line); color: var(--accent); font-weight:600; }

.content-grid { display:grid; grid-template-columns:1.35fr 1fr; gap:20px; align-items:start; }

.table-card { padding:0; overflow:hidden; }
.table-wrap { overflow-x:auto; }
.tbl { width:100%; border-collapse:collapse; font-size: 14px; }
.tbl th {
  padding:10px 14px; text-align:left;
  font-size: 16px; font-weight: 700; letter-spacing:0.04em;
  font-family:var(--font-mono); color: var(--text-3); background:var(--bg-2); border-bottom:1px solid var(--line-1);
}
.tbl td { padding:12px 14px; color: var(--text-2); border-bottom:1px solid var(--line-1); vertical-align:middle; }
.tbl tbody tr:last-child td { border-bottom:none; }
.log-row { cursor:pointer; transition:background .1s; }
.log-row:hover td  { background:var(--bg-3); }
.log-row.active td { background:var(--bg-2); }

.id-txt    { font-size: 14px; font-weight:600; color: var(--text-1); }
.cell-name { font-size: 16px; font-weight:600; color: var(--text-1); }
.sub-txt   { font-size: 14px; color: var(--text-3); margin-top:2px; }
.cell-sm   { font-size: 14px; color: var(--text-2); }

.sc-badge { font-size: 14px; font-weight:700; }
.st-badge {
  display:inline-block; font-size: 14px; padding:2px 7px;
  border-radius:2px; border:1px solid; letter-spacing:0.04em;
}
.peak-score { font-size:15px; font-weight:800; }
.peak-label { font-size: 14px; margin-top:1px; }
.empty-row  { padding:40px; text-align:center; color: var(--text-3); font-size: 14px; }

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

.detail-card { padding:0; overflow:hidden; position:sticky; top:24px; }
.detail-header { padding:18px 20px; background:var(--bg-2); border-bottom:1px solid var(--line-1); }

.detail-meta {
  display:grid; grid-template-columns:repeat(3,1fr); gap:14px;
  padding:14px 20px; border-bottom:1px solid var(--line-1);
  font-family:var(--font-mono);
}
.meta-lbl { font-size: 14px; color: var(--text-3); letter-spacing:0.04em; }

.detail-section { padding:16px 20px; border-bottom:1px solid var(--line-1); }
.section-title  { font-size: 16px; font-weight:700; color: var(--text-2); margin-bottom:10px; }

.rest-row {
  display:flex; align-items:center; gap:10px;
  padding:6px 0; border-top:1px dashed var(--line-1);
  font-family:var(--font-mono); font-size: 14px;
}
.rest-badge {
  padding:2px 8px; border-radius:2px; font-size: 14px; font-weight:700;
  letter-spacing:0.04em; text-align:center;
}
.detail-footer { padding:14px 20px; background:var(--bg-2); border-top:1px solid var(--line-1); }
.full-btn { width:100%; justify-content:center; gap:6px; display:flex; align-items:center; }
</style>
