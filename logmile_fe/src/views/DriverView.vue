<script setup>
import { ref, computed } from 'vue'
import AppIcon from '@/components/common/AppIcon.vue'
import { drivers, levelOf } from '@/data/mockData'

const list        = ref(drivers.map(d => ({ ...d })))
const search      = ref('')
const levelFilter = ref('ALL')

const filtered = computed(() => list.value.filter(d => {
  if (levelFilter.value !== 'ALL' && d.level !== levelFilter.value) return false
  if (search.value && !(d.name.includes(search.value) || d.plate.includes(search.value) || d.phone.includes(search.value))) return false
  return true
}))

const dangerCount  = computed(() => list.value.filter(d => d.level === 'DANGER').length)
const cautionCount = computed(() => list.value.filter(d => d.level === 'CAUTION').length)
const normalCount  = computed(() => list.value.filter(d => d.level === 'NORMAL').length)

function levelChipStyle(l) {
  if (l === 'DANGER')  return { color:'var(--danger)', bg:'var(--danger-soft)',  border:'rgba(181,84,74,.3)' }
  if (l === 'CAUTION') return { color:'var(--warn)',   bg:'var(--warn-soft)',    border:'rgba(197,138,58,.3)' }
  return                      { color:'var(--ok)',     bg:'var(--ok-soft)',      border:'rgba(94,138,111,.3)' }
}
function statusStyle(s) {
  if (s === 'RUNNING') return { color:'var(--accent)', bg:'var(--accent-soft)', border:'var(--accent-line)' }
  return                      { color:'var(--text-3)', bg:'var(--bg-4)',        border:'var(--line-2)' }
}
</script>

<template>
  <div class="view">
    <div class="breadcrumb mono">
      ADMIN / DRIVER_TABLE · {{ list.length }} record(s) · 한라물류센터
    </div>

    <div class="page-header">
      <h2 class="page-title">운전자 관리</h2>
      <button class="btn btn-ghost hdr-btn"><AppIcon name="refresh" :size="13" />새로고침</button>
    </div>

    <!-- 요약 카드 -->
    <div class="sum-row">
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
        <span class="sum-dot" style="background:var(--ok)" />
        <span class="sum-lbl mono">NORMAL</span>
        <span class="sum-val mono" style="color:var(--ok)">{{ normalCount }}</span>
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
              <th>운전자</th>
              <th>전화번호</th>
              <th>면허</th>
              <th>배정 차량</th>
              <th>STATUS</th>
              <th>피로 점수</th>
              <th>입사일</th>
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
                <div class="mono plate-txt">{{ d.plate }}</div>
              </td>
              <td>
                <span class="status-chip mono"
                  :style="{ color: statusStyle(d.status).color, background: statusStyle(d.status).bg, borderColor: statusStyle(d.status).border }">
                  {{ d.status === 'RUNNING' ? '운행중' : '대기' }}
                </span>
              </td>
              <td>
                <div class="score-row">
                  <span class="mono score-num" :style="{ color: levelOf(d.level).color }">{{ d.score }}</span>
                  <span class="level-chip mono"
                    :style="{ color: levelChipStyle(d.level).color, background: levelChipStyle(d.level).bg, borderColor: levelChipStyle(d.level).border }">
                    {{ d.level }}
                  </span>
                </div>
                <div class="score-bar-wrap">
                  <div class="score-bar-fill" :style="{ width: d.score+'%', background: levelOf(d.level).color }" />
                </div>
              </td>
              <td class="mono sub-txt">{{ d.employedAt }}</td>
            </tr>
          </tbody>
        </table>
        <div v-if="filtered.length===0" class="empty-row">조건에 맞는 운전자가 없습니다.</div>
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

.empty-row { padding:40px; text-align:center; color:var(--text-4); font-size:13px; }
</style>
