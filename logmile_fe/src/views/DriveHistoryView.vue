<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '@/components/common/AppIcon.vue'
import { driveLogs, plateTimeline, restEvents, fmtMin } from '@/data/mockData'

const router = useRouter()

const logs       = ref(driveLogs)
const ptl        = ref(plateTimeline)
const re         = ref(restEvents)
const selectedId = ref('DL-2026-0438')
const filters    = ref({ q:'', scenario:'ALL', status:'ALL', level:'ALL' })

const filtered = computed(() => logs.value.filter(l => {
  if (filters.value.scenario !== 'ALL' && l.scenario !== filters.value.scenario) return false
  if (filters.value.status   !== 'ALL' && l.status   !== filters.value.status)   return false
  if (filters.value.level    !== 'ALL' && l.level    !== filters.value.level)     return false
  if (filters.value.q && !(l.plate.includes(filters.value.q) || l.driver.includes(filters.value.q) || l.id.includes(filters.value.q))) return false
  return true
}))

const selected = computed(() => logs.value.find(l => l.id === selectedId.value) || logs.value[0])

const selectedRests = computed(() =>
  re.value.filter(r => r.drive === selected.value.id)
)

function scoreColor(s) {
  if (s >= 70) return 'var(--danger)'
  if (s >= 40) return 'var(--warn)'
  return 'var(--ok)'
}
function statusBadge(s) {
  if (s === 'RUNNING')   return { label:'RUNNING',   color:'var(--accent)' }
  if (s === 'COMPLETED') return { label:'COMPLETED', color:'var(--text-3)' }
  if (s === 'STOPPED')   return { label:'STOPPED',   color:'var(--danger)' }
  return { label:s, color:'var(--text-3)' }
}
function scenarioBadge(s) {
  if (s === 'A') return { label:'A · 정상',     color:'var(--ok)' }
  if (s === 'B') return { label:'B · 경계',     color:'var(--warn)' }
  if (s === 'C') return { label:'C · 피로 누적', color:'var(--danger)' }
  return { label:'—', color:'var(--text-3)' }
}
function restBadge(t) {
  if (t === 'SUFFICIENT') return { label:'SUFFICIENT', color:'var(--ok)',     bg:'rgba(94,138,111,.14)' }
  if (t === 'VALID')      return { label:'VALID',      color:'var(--accent)', bg:'rgba(81,95,122,.16)'  }
  if (t === 'INVALID')    return { label:'INVALID',    color:'var(--danger)', bg:'rgba(181,84,74,.14)'  }
  return { label:'PENDING', color:'var(--text-3)', bg:'var(--bg-2)' }
}
function sourceBadge(s) {
  const m = { DEPARTURE:'출발', ARRIVAL:'도착', HIGHWAY_CCTV:'고속도로 CCTV', REST_AREA_CCTV:'휴게소 CCTV' }
  return m[s] || s
}
function sourceColor(s) {
  if (s==='DEPARTURE' || s==='ARRIVAL') return 'var(--accent)'
  if (s==='REST_AREA_CCTV') return 'var(--ok)'
  return 'var(--text-3)'
}
</script>

<template>
  <div class="view">
    <div class="breadcrumb mono">
      ADMIN / DRIVE_LOG · {{ logs.length }} record(s) · 가상 물류센터 (한라물류)
    </div>

    <div class="page-header">
      <div>
        <h2 class="page-title">운행 이력</h2>
        <p class="page-sub">drive_log · plate_recognition_event · rest_event 통합 표시</p>
      </div>
      <div style="display:flex;gap:8px;">
        <button class="btn btn-ghost small-btn">CSV 내보내기</button>
        <button class="btn btn-ghost small-btn" @click="filters={q:'',scenario:'ALL',status:'ALL',level:'ALL'}">필터 초기화</button>
      </div>
    </div>

    <!-- 필터 -->
    <div class="filter-row">
      <input v-model="filters.q" class="search-inp" placeholder="번호판 / 운전자 / DL-ID 검색" />
      <div class="filter-group">
        <span class="filter-lbl mono">시나리오</span>
        <div class="filter-btns">
          <button v-for="s in ['ALL','A','B','C']" :key="s"
            class="filter-btn mono" :class="{ active: filters.scenario===s }"
            @click="filters.scenario=s">{{ s }}</button>
        </div>
      </div>
      <div class="filter-group">
        <span class="filter-lbl mono">상태</span>
        <div class="filter-btns">
          <button v-for="s in ['ALL','RUNNING','COMPLETED','STOPPED']" :key="s"
            class="filter-btn mono" :class="{ active: filters.status===s }"
            @click="filters.status=s">{{ s }}</button>
        </div>
      </div>
      <div class="filter-group">
        <span class="filter-lbl mono">등급</span>
        <div class="filter-btns">
          <button v-for="l in ['ALL','NORMAL','CAUTION','DANGER']" :key="l"
            class="filter-btn mono" :class="{ active: filters.level===l }"
            @click="filters.level=l">{{ l }}</button>
        </div>
      </div>
    </div>

    <!-- 테이블 + 상세 -->
    <div class="content-grid">

      <!-- 좌측: 테이블 -->
      <div class="card table-card">
        <div class="table-wrap">
          <table class="tbl">
            <thead>
              <tr>
                <th>DL-ID</th>
                <th>차량 / 운전자</th>
                <th>시나리오</th>
                <th>상태</th>
                <th>운행시간</th>
                <th>야간</th>
                <th>휴식</th>
                <th style="text-align:right">최고점수</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="l in filtered" :key="l.id"
                class="log-row" :class="{ active: l.id === selectedId }"
                @click="selectedId = l.id">
                <td>
                  <div class="mono id-txt">{{ l.id }}</div>
                  <div class="sub-txt">{{ l.startedAt.slice(5,16) }}</div>
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
                <td class="mono cell-sm">{{ fmtMin(l.nightMin) }}</td>
                <td class="mono cell-sm">{{ l.restCount }}회</td>
                <td style="text-align:right">
                  <div class="mono peak-score" :style="{ color: scoreColor(l.peak) }">{{ l.peak }}</div>
                  <div class="peak-label" :style="{ color: scoreColor(l.peak) }">{{ l.level }}</div>
                </td>
              </tr>
            </tbody>
          </table>
          <div v-if="filtered.length===0" class="empty-row">조건에 맞는 운행 기록이 없습니다.</div>
        </div>
      </div>

      <!-- 우측: 상세 패널 -->
      <div class="card detail-card" v-if="selected">
        <div class="detail-header">
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px;">
            <span class="mono" style="font-size:11px;color:var(--text-4)">{{ selected.id }}</span>
            <span class="st-badge mono" :style="{ borderColor: statusBadge(selected.status).color, color: statusBadge(selected.status).color }">
              {{ statusBadge(selected.status).label }}
            </span>
          </div>
          <div style="display:flex;align-items:flex-end;gap:14px;">
            <div>
              <div style="font-size:18px;font-weight:700;color:var(--text-1)">{{ selected.plate }}</div>
              <div class="sub-txt">{{ selected.driver }} · 시작 {{ selected.startedAt.slice(5,16) }}</div>
            </div>
            <div style="margin-left:auto;text-align:right;">
              <div class="mono" style="font-size:30px;font-weight:800;line-height:1" :style="{ color: scoreColor(selected.peak) }">{{ selected.peak }}</div>
              <div class="mono" style="font-size:10.5px;margin-top:2px" :style="{ color: scoreColor(selected.peak) }">PEAK SCORE · {{ selected.level }}</div>
            </div>
          </div>
        </div>

        <!-- 메타 -->
        <div class="detail-meta">
          <div>
            <div class="meta-lbl">SCENARIO</div>
            <div class="mono" style="font-size:12px;font-weight:700;margin-top:4px" :style="{ color: scenarioBadge(selected.scenario).color }">{{ scenarioBadge(selected.scenario).label }}</div>
          </div>
          <div>
            <div class="meta-lbl">TOTAL / NIGHT</div>
            <div class="mono" style="font-size:12px;font-weight:700;color:var(--text-1);margin-top:4px">{{ fmtMin(selected.totalMin) }} / {{ fmtMin(selected.nightMin) }}</div>
          </div>
          <div>
            <div class="meta-lbl">OCR</div>
            <div class="mono" style="font-size:12px;font-weight:700;color:var(--text-1);margin-top:4px">
              {{ Math.round(selected.ocrConf*100) }}%
              <span v-if="selected.manual" style="font-size:9.5px;color:var(--warn);margin-left:4px">[수동입력]</span>
              <span v-else-if="selected.ocrConf<0.85" style="font-size:9.5px;color:var(--warn);margin-left:4px">low</span>
            </div>
          </div>
        </div>

        <!-- 번호판 인식 -->
        <div class="detail-section">
          <div class="section-title">
            번호판 인식 이벤트
            <span class="mono" style="font-size:10px;color:var(--text-4);margin-left:6px">{{ ptl.length }} events</span>
          </div>
          <div v-for="(e,i) in ptl" :key="i" class="evt-row">
            <span class="mono" style="color:var(--text-4);width:56px">{{ e.t }}</span>
            <span class="mono" style="font-weight:700;width:110px" :style="{ color: sourceColor(e.source) }">{{ sourceBadge(e.source) }}</span>
            <span style="color:var(--text-2);flex:1;font-size:11px">{{ e.loc }}</span>
            <span class="mono" style="color:var(--text-3);text-align:right;font-size:10.5px">{{ Math.round(e.conf*100) }}%</span>
          </div>
        </div>

        <!-- 휴식 이벤트 -->
        <div class="detail-section">
          <div class="section-title">
            휴식 이벤트
            <span class="mono" style="font-size:10px;color:var(--text-4);margin-left:6px">VALID(15m+) / SUFFICIENT(30m+) / INVALID</span>
          </div>
          <template v-if="selectedRests.length > 0">
            <div v-for="r in selectedRests" :key="r.id" class="rest-row">
              <span class="mono" style="color:var(--text-2);flex:1">{{ r.startAt }} → {{ r.endAt }}</span>
              <span class="mono" style="font-weight:700;color:var(--text-1)">{{ r.minutes }}m</span>
              <span class="rest-badge mono" :style="{ color: restBadge(r.type).color, background: restBadge(r.type).bg }">{{ restBadge(r.type).label }}</span>
            </div>
          </template>
          <div v-else class="mono" style="font-size:11px;color:var(--text-4);padding:10px 0">이 운행에는 기록된 휴식 이벤트가 없습니다.</div>
        </div>

        <!-- 상세 보기 버튼 -->
        <div class="detail-footer">
          <button class="btn btn-primary full-btn" @click="router.push({ name:'driveHistoryDetail', params:{ id: selected.id } })">
            <AppIcon name="arrow" :size="13" /> 전체 상세 보기
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view { display:flex; flex-direction:column; gap:16px; padding:32px 32px 40px; }
.breadcrumb { font-size:11px; color:var(--text-4); letter-spacing:0.04em; }

.page-header { display:flex; align-items:flex-start; justify-content:space-between; gap:12px; }
.page-title  { font-size:24px; font-weight:700; color:var(--text-1); margin:0 0 4px; letter-spacing:-0.01em; }
.page-sub    { font-size:12.5px; color:var(--text-3); margin:0; }
.small-btn   { font-size:12.5px; padding:7px 12px; }

.filter-row { display:flex; align-items:center; gap:12px; flex-wrap:wrap; }
.search-inp {
  flex:1; min-width:220px; padding:8px 12px;
  border:1px solid var(--line-2); border-radius:var(--r-md);
  background:var(--bg-1); color:var(--text-1); font-size:13px; outline:none;
}
.search-inp:focus { border-color:var(--accent-line); }
.filter-group { display:flex; align-items:center; gap:6px; }
.filter-lbl   { font-size:10px; color:var(--text-4); letter-spacing:0.06em; white-space:nowrap; }
.filter-btns  { display:flex; gap:3px; }
.filter-btn {
  padding:4px 9px; border-radius:var(--r-sm); font-size:10.5px;
  background:none; border:1px solid var(--line-2); color:var(--text-3); cursor:pointer; transition:all .12s;
}
.filter-btn.active { background:var(--accent-soft); border-color:var(--accent-line); color:var(--accent); font-weight:600; }

.content-grid { display:grid; grid-template-columns:1.35fr 1fr; gap:20px; align-items:start; }

.table-card { padding:0; overflow:hidden; }
.table-wrap { overflow-x:auto; }
.tbl { width:100%; border-collapse:collapse; font-size:12.5px; }
.tbl th {
  padding:10px 14px; text-align:left;
  font-size:10px; font-weight:500; letter-spacing:0.04em;
  font-family:var(--font-mono); color:var(--text-4); background:var(--bg-2); border-bottom:1px solid var(--line-1);
}
.tbl td { padding:12px 14px; color:var(--text-2); border-bottom:1px solid var(--line-1); vertical-align:middle; }
.tbl tbody tr:last-child td { border-bottom:none; }
.log-row { cursor:pointer; transition:background .1s; }
.log-row:hover td  { background:var(--bg-3); }
.log-row.active td { background:var(--bg-2); }

.id-txt    { font-size:11.5px; font-weight:600; color:var(--text-1); }
.cell-name { font-size:13px; font-weight:600; color:var(--text-1); }
.sub-txt   { font-size:10.5px; color:var(--text-4); margin-top:2px; }
.cell-sm   { font-size:11.5px; color:var(--text-2); }

.sc-badge { font-size:10.5px; font-weight:700; }
.st-badge {
  display:inline-block; font-size:10px; padding:2px 7px;
  border-radius:2px; border:1px solid; letter-spacing:0.04em;
}
.peak-score { font-size:15px; font-weight:800; }
.peak-label { font-size:9.5px; margin-top:1px; }
.empty-row  { padding:40px; text-align:center; color:var(--text-4); font-size:13px; }

/* 상세 패널 */
.detail-card { padding:0; overflow:hidden; position:sticky; top:24px; }
.detail-header { padding:18px 20px; background:var(--bg-2); border-bottom:1px solid var(--line-1); }

.detail-meta {
  display:grid; grid-template-columns:repeat(3,1fr); gap:14px;
  padding:14px 20px; border-bottom:1px solid var(--line-1);
  font-family:var(--font-mono);
}
.meta-lbl { font-size:10px; color:var(--text-4); letter-spacing:0.04em; }

.detail-section { padding:16px 20px; border-bottom:1px solid var(--line-1); }
.section-title  { font-size:12px; font-weight:700; color:var(--text-2); margin-bottom:10px; }

.evt-row {
  display:flex; align-items:center; gap:10px;
  padding:5px 0; border-top:1px dashed var(--line-1);
  font-family:var(--font-mono); font-size:11px;
}
.rest-row {
  display:flex; align-items:center; gap:10px;
  padding:6px 0; border-top:1px dashed var(--line-1);
  font-family:var(--font-mono); font-size:11px;
}
.rest-badge {
  padding:2px 8px; border-radius:2px; font-size:9.5px; font-weight:700;
  letter-spacing:0.04em; text-align:center;
}
.detail-footer { padding:14px 20px; background:var(--bg-2); border-top:1px solid var(--line-1); }
.full-btn { width:100%; justify-content:center; gap:6px; display:flex; align-items:center; }
</style>
