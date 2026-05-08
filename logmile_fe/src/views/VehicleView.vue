<script setup>
import { ref, computed } from 'vue'
import AppIcon from '@/components/common/AppIcon.vue'
import { vehicles, levelOf } from '@/data/mockData'

const list         = ref(vehicles.map(v => ({ ...v })))
const search       = ref('')
const statusFilter = ref('ALL')
const levelFilter  = ref('ALL')

const filtered = computed(() => list.value.filter(v => {
  if (statusFilter.value !== 'ALL' && v.status !== statusFilter.value) return false
  if (levelFilter.value  !== 'ALL' && v.level  !== levelFilter.value)  return false
  if (search.value && !(v.plate.includes(search.value) || v.driver.includes(search.value))) return false
  return true
}))

const runningCount = computed(() => list.value.filter(v => v.status === 'RUNNING').length)
const dangerCount  = computed(() => list.value.filter(v => v.level === 'DANGER').length)
const cautionCount = computed(() => list.value.filter(v => v.level === 'CAUTION').length)

function fmtMin(m) {
  if (!m) return '—'
  const h = Math.floor(m/60), mn = m%60
  return h > 0 ? `${h}h ${mn}m` : `${mn}m`
}
function statusChip(s) {
  if (s === 'RUNNING')   return { label:'운행중', color:'var(--accent)',  bg:'var(--accent-soft)',  border:'var(--accent-line)' }
  if (s === 'IDLE')      return { label:'대기',   color:'var(--text-3)', bg:'var(--bg-4)',          border:'var(--line-2)' }
  if (s === 'COMPLETED') return { label:'완료',   color:'var(--ok)',     bg:'var(--ok-soft)',       border:'rgba(94,138,111,.3)' }
  return                        { label:'중단',   color:'var(--danger)', bg:'var(--danger-soft)',   border:'rgba(181,84,74,.3)' }
}
function levelChip(l) {
  const m = levelOf(l)
  if (l === 'DANGER')  return { label:'DANGER',  color:'var(--danger)', bg:'var(--danger-soft)',  border:'rgba(181,84,74,.3)' }
  if (l === 'CAUTION') return { label:'CAUTION', color:'var(--warn)',   bg:'var(--warn-soft)',    border:'rgba(197,138,58,.3)' }
  return                      { label:'NORMAL',  color:'var(--ok)',     bg:'var(--ok-soft)',      border:'rgba(94,138,111,.3)' }
}
</script>

<template>
  <div class="view">
    <div class="breadcrumb mono">
      ADMIN / VEHICLE_TABLE · {{ list.length }} record(s) · 한라물류센터
    </div>

    <div class="page-header">
      <h2 class="page-title">차량 관리</h2>
      <button class="btn btn-ghost hdr-btn"><AppIcon name="refresh" :size="13" />새로고침</button>
    </div>

    <!-- 요약 카드 -->
    <div class="sum-row">
      <div class="sum-card">
        <span class="sum-dot" style="background:var(--accent)" />
        <span class="sum-lbl mono">RUNNING</span>
        <span class="sum-val mono" style="color:var(--accent)">{{ runningCount }}</span>
      </div>
      <div class="sum-card">
        <span class="sum-dot" style="background:var(--danger)" />
        <span class="sum-lbl mono">DANGER</span>
        <span class="sum-val mono" style="color:var(--danger)">{{ dangerCount }}</span>
      </div>
      <div class="sum-card">
        <span class="sum-dot" style="background:var(--warn)" />
        <span class="sum-lbl mono">CAUTION</span>
        <span class="sum-val mono" style="color:var(--warn)">{{ cautionCount }}</span>
      </div>
      <div class="sum-card">
        <span class="sum-dot" style="background:var(--text-4)" />
        <span class="sum-lbl mono">TOTAL</span>
        <span class="sum-val mono">{{ list.length }}</span>
      </div>
    </div>

    <!-- 필터 -->
    <div class="filter-row">
      <input v-model="search" class="search-inp" placeholder="번호판 / 운전자 검색" />
      <div class="filter-group">
        <span class="filter-lbl mono">STATUS</span>
        <div class="filter-btns">
          <button v-for="s in ['ALL','RUNNING','IDLE']" :key="s"
            class="filter-btn mono" :class="{ active: statusFilter===s }"
            @click="statusFilter=s">{{ s }}</button>
        </div>
      </div>
      <div class="filter-group">
        <span class="filter-lbl mono">LEVEL</span>
        <div class="filter-btns">
          <button v-for="l in ['ALL','NORMAL','CAUTION','DANGER']" :key="l"
            class="filter-btn mono" :class="{ active: levelFilter===l }"
            @click="levelFilter=l">{{ l }}</button>
        </div>
      </div>
    </div>

    <!-- 테이블 -->
    <div class="card table-card">
      <div class="table-wrap">
        <table class="tbl">
          <thead>
            <tr>
              <th>번호판</th>
              <th>운전자</th>
              <th>차종</th>
              <th>STATUS</th>
              <th>피로 점수</th>
              <th>연속 운행</th>
              <th>일일 누적</th>
              <th>야간</th>
              <th>위치</th>
              <th>시나리오</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="v in filtered" :key="v.id">
              <td>
                <div class="mono plate">{{ v.plate }}</div>
                <div class="sub-txt">{{ v.driveLogId ?? '미운행' }}</div>
              </td>
              <td>
                <div class="cell-name">{{ v.driver }}</div>
                <div class="sub-txt">{{ v.phone }}</div>
              </td>
              <td class="sub-txt">{{ v.type }}</td>
              <td>
                <span class="status-chip mono"
                  :style="{ color: statusChip(v.status).color, background: statusChip(v.status).bg, borderColor: statusChip(v.status).border }">
                  {{ statusChip(v.status).label }}
                </span>
              </td>
              <td>
                <div class="score-row">
                  <span class="mono score-num" :style="{ color: levelOf(v.level).color }">{{ v.score }}</span>
                  <span class="level-chip mono"
                    :style="{ color: levelChip(v.level).color, background: levelChip(v.level).bg, borderColor: levelChip(v.level).border }">
                    {{ levelChip(v.level).label }}
                  </span>
                </div>
                <div class="score-bar-wrap">
                  <div class="score-bar-fill"
                    :style="{ width: v.score+'%', background: levelOf(v.level).color }" />
                </div>
              </td>
              <td class="mono cell-sm">{{ fmtMin(v.contMin) }}</td>
              <td class="mono cell-sm">{{ fmtMin(v.dailyMin) }}</td>
              <td class="mono cell-sm">{{ fmtMin(v.nightMin) }}</td>
              <td class="cell-loc">{{ v.loc }}</td>
              <td>
                <span v-if="v.scenario !== '—'" class="scenario-chip mono"
                  :style="{ color: v.scenario==='C' ? 'var(--danger)' : v.scenario==='B' ? 'var(--warn)' : 'var(--ok)' }">
                  {{ v.scenario }}
                </span>
                <span v-else class="sub-txt">—</span>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="filtered.length===0" class="empty-row">조건에 맞는 차량이 없습니다.</div>
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

.sum-row { display:flex; gap:10px; }
.sum-card {
  display:flex; align-items:center; gap:8px; padding:10px 16px;
  background:var(--bg-2); border:1px solid var(--line-1); border-radius:var(--r-md); flex:1;
}
.sum-dot { width:8px; height:8px; border-radius:50%; flex-shrink:0; }
.sum-lbl { font-size:10px; letter-spacing:0.08em; color:var(--text-4); flex:1; }
.sum-val { font-size:20px; font-weight:700; color:var(--text-2); }

.filter-row { display:flex; align-items:center; gap:16px; flex-wrap:wrap; }
.search-inp {
  flex:1; min-width:240px; padding:8px 12px;
  border:1px solid var(--line-2); border-radius:var(--r-md);
  background:var(--bg-1); color:var(--text-1); font-size:13px; outline:none;
  transition:border-color .15s;
}
.search-inp:focus { border-color:var(--accent-line); }
.filter-group { display:flex; align-items:center; gap:8px; }
.filter-lbl   { font-size:10px; color:var(--text-4); letter-spacing:0.08em; white-space:nowrap; }
.filter-btns  { display:flex; gap:3px; }
.filter-btn {
  padding:4px 9px; border-radius:var(--r-sm); font-size:10.5px;
  background:none; border:1px solid var(--line-2); color:var(--text-3);
  cursor:pointer; transition:all .12s;
}
.filter-btn.active {
  background:var(--accent-soft); border-color:var(--accent-line); color:var(--accent); font-weight:600;
}

.table-card { padding:0; overflow:hidden; }
.table-wrap { overflow-x:auto; }
.tbl { width:100%; border-collapse:collapse; font-size:13px; }
.tbl th {
  padding:10px 14px; text-align:left;
  font-size:10px; font-weight:600; letter-spacing:0.07em; text-transform:uppercase;
  color:var(--text-4); background:var(--bg-3); border-bottom:1px solid var(--line-1);
  white-space:nowrap;
}
.tbl td { padding:11px 14px; color:var(--text-2); border-bottom:1px solid var(--line-1); vertical-align:middle; }
.tbl tbody tr:last-child td { border-bottom:none; }
.tbl tbody tr:hover td { background:var(--bg-3); }

.plate     { font-size:12.5px; font-weight:700; color:var(--text-1); }
.cell-name { font-size:13px; font-weight:500; color:var(--text-1); }
.sub-txt   { font-size:11px; color:var(--text-4); margin-top:2px; }
.cell-sm   { font-size:11.5px; color:var(--text-3); }
.cell-loc  { font-size:12px; color:var(--text-3); max-width:140px; }

.status-chip {
  display:inline-block; padding:2px 8px; border-radius:var(--r-sm);
  font-size:10px; font-weight:600; letter-spacing:0.06em; border:1px solid;
}
.score-row { display:flex; align-items:center; gap:6px; margin-bottom:4px; }
.score-num { font-size:16px; font-weight:800; letter-spacing:-0.01em; }
.level-chip {
  display:inline-block; padding:1px 6px; border-radius:var(--r-sm);
  font-size:9px; font-weight:700; letter-spacing:0.06em; border:1px solid;
}
.score-bar-wrap { height:4px; background:var(--bg-3); border-radius:2px; overflow:hidden; width:80px; }
.score-bar-fill { height:100%; border-radius:2px; transition:width .3s; }
.scenario-chip { font-size:12px; font-weight:700; }

.empty-row { padding:40px; text-align:center; color:var(--text-4); font-size:13px; }
</style>
