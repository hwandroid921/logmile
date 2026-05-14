<script setup>
import { computed, onMounted, ref } from 'vue'
import { fatigueStatsApi } from '@/api/fatigueStatsApi'

const range = ref('14d')
const stats = ref([])
const loading = ref(false)
const error = ref('')

const rangeMap = { '7d': 7, '14d': 14, '30d': 30 }

const statDays = computed(() => stats.value.map((row) => {
  const score = Math.round(Number(row.averageFatigueScore ?? 0))

  return {
    date: row.date,
    label: formatDate(row.date),
    avgScore: score,
    danger: Number(row.dangerEventCount ?? 0),
    restMiss: Number(row.restViolationCount ?? 0),
    driveMinutes: Number(row.totalDrivingMinutes ?? 0),
    nightMinutes: Number(row.nightDrivingMinutes ?? 0),
    driveLogCount: Number(row.driveLogCount ?? 0),
  }
}))

const avgScore = computed(() => {
  if (!statDays.value.length) return 0

  const total = statDays.value.reduce((sum, day) => sum + day.avgScore, 0)
  return Math.round(total / statDays.value.length)
})

const dangerTotal = computed(() => statDays.value.reduce((sum, day) => sum + day.danger, 0))
const missingTotal = computed(() => statDays.value.reduce((sum, day) => sum + day.restMiss, 0))
const totalDriveMin = computed(() => statDays.value.reduce((sum, day) => sum + day.driveMinutes, 0))
const totalNightMin = computed(() => statDays.value.reduce((sum, day) => sum + day.nightMinutes, 0))
const totalDriveLogs = computed(() => statDays.value.reduce((sum, day) => sum + day.driveLogCount, 0))
const totalDriveH = computed(() => Math.round(totalDriveMin.value / 60))
const totalNightH = computed(() => Math.round(totalNightMin.value / 60))
const maxScore = computed(() => Math.max(...statDays.value.map((day) => day.avgScore), 1))
const maxDriveMinutes = computed(() => Math.max(...statDays.value.map((day) => day.driveMinutes), 1))
const maxEventCount = computed(() => Math.max(
  ...statDays.value.map((day) => Math.max(day.danger, day.restMiss)),
  1,
))

function setRange(nextRange) {
  if (range.value === nextRange) return

  range.value = nextRange
  fetchStats()
}

async function fetchStats() {
  loading.value = true
  error.value = ''

  try {
    const days = rangeMap[range.value] ?? 14
    const { data } = await fatigueStatsApi.getStats({ days })
    stats.value = Array.isArray(data) ? data : []
  } catch (err) {
    stats.value = []
    error.value = err?.response?.data?.message || '피로도 통계 데이터를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

function formatDate(value) {
  if (!value) return '-'

  const [, month, day] = String(value).split('-')
  return month && day ? `${month}/${day}` : value
}

function scoreColor(score) {
  if (score >= 70) return 'var(--danger)'
  if (score >= 40) return 'var(--warn)'
  return 'var(--ok)'
}

function scoreLevel(score) {
  if (score >= 70) return 'DANGER 수준'
  if (score >= 40) return 'CAUTION 수준'
  return '정상 범위'
}

function pct(value, max) {
  return max ? Math.round((value / max) * 100) : 0
}

onMounted(fetchStats)
</script>

<template>
  <div class="view">
    <div class="breadcrumb mono">
      ADMIN / FATIGUE_STATS · API /api/fatigue/stats · RANGE {{ range.toUpperCase() }}
    </div>

    <div class="page-header">
      <div>
        <h2 class="page-title">통계 · {{ range }} 리포트</h2>
        <p class="page-sub">피로도 점수 누적 데이터 기반 운행 통계 — {{ statDays.length }}일 집계</p>
      </div>
      <div class="range-btns">
        <button
          v-for="r in ['7d','14d','30d']"
          :key="r"
          class="range-btn mono"
          :class="{ active: range === r }"
          :disabled="loading"
          @click="setRange(r)"
        >
          {{ r }}
        </button>
      </div>
    </div>

    <div v-if="error" class="notice error-box">
      {{ error }}
    </div>

    <div v-if="loading" class="card state-card">
      피로도 통계 데이터를 불러오는 중입니다.
    </div>

    <div v-else-if="!statDays.length" class="card state-card">
      표시할 피로도 통계 데이터가 없습니다.
    </div>

    <template v-else>
      <div class="kpi-row">
        <div class="kpi-card">
          <div class="kpi-label mono">AVG SCORE · {{ range }}</div>
          <div class="kpi-val" :style="{ color: scoreColor(avgScore) }">{{ avgScore }}</div>
          <div class="kpi-sub">{{ scoreLevel(avgScore) }}</div>
          <div class="kpi-bar-wrap">
            <div class="kpi-bar-fill" :style="{ width: avgScore + '%', background: scoreColor(avgScore) }" />
          </div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label mono">DANGER 발생</div>
          <div class="kpi-val" style="color:var(--danger)">{{ dangerTotal }}</div>
          <div class="kpi-sub">일별 위험 이벤트 누적</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label mono">휴식 위반</div>
          <div class="kpi-val" style="color:var(--warn)">{{ missingTotal }}</div>
          <div class="kpi-sub">휴식 판단 위반 누적</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label mono">총 운행 / 야간</div>
          <div class="kpi-val">{{ totalDriveH }}<span class="kpi-unit">h</span></div>
          <div class="kpi-sub">야간 {{ totalNightH }}h · 운행 {{ totalDriveLogs }}건</div>
        </div>
      </div>

      <div class="card chart-card">
        <div class="card-hdr">
          <div class="card-title">일별 평균 피로 점수</div>
          <span class="mono card-meta">{{ statDays.length }}일 · 전체 운행 평균</span>
        </div>
        <div class="bar-chart">
          <div v-for="day in statDays" :key="day.date" class="bar-col">
            <div class="bar-wrap">
              <div
                class="bar-fill"
                :style="{ height: pct(day.avgScore, maxScore) + '%', background: scoreColor(day.avgScore) }"
                :title="day.avgScore + '점'"
              />
            </div>
            <div class="bar-label mono">{{ day.label }}</div>
          </div>
        </div>
        <div class="bar-legend">
          <span class="legend-dot" style="background:var(--ok)" /><span class="legend-txt">NORMAL</span>
          <span class="legend-dot" style="background:var(--warn)" /><span class="legend-txt">CAUTION</span>
          <span class="legend-dot" style="background:var(--danger)" /><span class="legend-txt">DANGER</span>
        </div>
      </div>

      <div class="two-col">
        <div class="card chart-card">
          <div class="card-hdr">
            <div class="card-title">일별 운행 시간</div>
            <span class="mono card-meta">총 {{ totalDriveH }}h</span>
          </div>
          <div class="dist-list">
            <div v-for="day in statDays" :key="day.date" class="dist-item">
              <div class="dist-top">
                <span class="mono dist-lbl">{{ day.label }}</span>
                <span class="mono dist-pct">{{ Math.round(day.driveMinutes / 60) }}h · 야간 {{ Math.round(day.nightMinutes / 60) }}h</span>
              </div>
              <div class="dist-bar-wrap">
                <div class="dist-bar-fill" :style="{ width: pct(day.driveMinutes, maxDriveMinutes) + '%', background:'var(--accent)' }" />
              </div>
            </div>
          </div>
        </div>

        <div class="card chart-card">
          <div class="card-hdr">
            <div class="card-title">휴식 위반 / 위험 이벤트</div>
            <span class="mono card-meta">{{ missingTotal + dangerTotal }}건</span>
          </div>
          <div class="event-list">
            <div v-for="day in statDays" :key="day.date" class="event-row">
              <div class="event-date mono">{{ day.label }}</div>
              <div class="event-bars">
                <div class="event-track">
                  <div class="event-fill warn" :style="{ width: pct(day.restMiss, maxEventCount) + '%' }" />
                </div>
                <div class="event-track">
                  <div class="event-fill danger" :style="{ width: pct(day.danger, maxEventCount) + '%' }" />
                </div>
              </div>
              <div class="event-count mono">{{ day.restMiss }} / {{ day.danger }}</div>
            </div>
          </div>
          <div class="bar-legend">
            <span class="legend-dot" style="background:var(--warn)" /><span class="legend-txt">REST VIOLATION</span>
            <span class="legend-dot" style="background:var(--danger)" /><span class="legend-txt">DANGER EVENT</span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.view { display:flex; flex-direction:column; gap:16px; padding:32px 32px 40px; }
.breadcrumb { font-size:11px; color:var(--text-4); letter-spacing:0.04em; }

.page-header { display:flex; align-items:flex-start; justify-content:space-between; gap:24px; }
.page-title  { font-size:24px; font-weight:700; color:var(--text-1); margin:0 0 4px; letter-spacing:-0.01em; }
.page-sub    { font-size:12.5px; color:var(--text-3); margin:0; }
.range-btns  { display:flex; gap:4px; }
.range-btn {
  padding:6px 14px; border-radius:var(--r-md); font-size:11.5px;
  background:none; border:1px solid var(--line-2); color:var(--text-3); cursor:pointer; transition:all .12s;
}
.range-btn.active { background:var(--accent-soft); border-color:var(--accent-line); color:var(--accent); font-weight:700; }
.range-btn:disabled { opacity:.45; cursor:wait; }

.notice {
  border-radius:var(--r-md); font-size:12px; padding:12px 14px;
}
.error-box {
  border:1px solid rgba(214,69,69,.32); background:rgba(214,69,69,.08); color:var(--danger);
}
.state-card {
  min-height:180px; display:flex; align-items:center; justify-content:center;
  color:var(--text-3); font-size:13px;
}

.kpi-row { display:grid; grid-template-columns:repeat(4,1fr); gap:10px; }
.kpi-card {
  background:var(--bg-2); border:1px solid var(--line-1); border-radius:var(--r-md);
  padding:16px 18px; display:flex; flex-direction:column; gap:4px;
}
.kpi-label { font-size:9.5px; letter-spacing:0.07em; color:var(--text-4); }
.kpi-val   { font-size:32px; font-weight:800; color:var(--text-1); letter-spacing:-0.02em; line-height:1; }
.kpi-unit  { font-size:16px; font-weight:400; margin-left:2px; }
.kpi-sub   { font-size:11px; color:var(--text-4); }
.kpi-bar-wrap { height:3px; background:var(--bg-3); border-radius:2px; overflow:hidden; margin-top:8px; }
.kpi-bar-fill { height:100%; border-radius:2px; transition:width .4s; }

.card-hdr   { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }
.card-title { font-size:14px; font-weight:700; color:var(--text-1); }
.card-meta  { font-size:11px; color:var(--text-4); }
.chart-card { padding:20px; }

.bar-chart {
  display:flex; align-items:flex-end; gap:4px; height:120px; padding-bottom:24px;
  border-bottom:1px solid var(--line-1);
}
.bar-col   { flex:1; display:flex; flex-direction:column; align-items:center; height:100%; min-width:0; }
.bar-wrap  { flex:1; width:100%; display:flex; align-items:flex-end; }
.bar-fill  { width:100%; border-radius:2px 2px 0 0; transition:height .4s; min-height:2px; }
.bar-label { font-size:9px; color:var(--text-4); margin-top:5px; white-space:nowrap; }

.bar-legend { display:flex; align-items:center; gap:14px; margin-top:10px; flex-wrap:wrap; }
.legend-dot { width:8px; height:8px; border-radius:50%; display:inline-block; }
.legend-txt { font-size:11px; color:var(--text-3); }

.two-col { display:grid; grid-template-columns:1fr 1fr; gap:10px; }
.dist-list { display:flex; flex-direction:column; gap:14px; }
.dist-item { display:flex; flex-direction:column; gap:6px; }
.dist-top  { display:flex; align-items:center; justify-content:space-between; gap:10px; }
.dist-lbl  { font-size:11px; font-weight:700; letter-spacing:0.04em; color:var(--text-3); }
.dist-pct  { font-size:11px; color:var(--text-3); white-space:nowrap; }
.dist-bar-wrap { height:6px; background:var(--bg-3); border-radius:3px; overflow:hidden; }
.dist-bar-fill { height:100%; border-radius:3px; transition:width .4s; }

.event-list { display:flex; flex-direction:column; gap:12px; }
.event-row { display:grid; grid-template-columns:44px 1fr 54px; align-items:center; gap:10px; }
.event-date { font-size:11px; color:var(--text-4); }
.event-bars { display:flex; flex-direction:column; gap:4px; }
.event-track { height:5px; border-radius:3px; overflow:hidden; background:var(--bg-3); }
.event-fill { height:100%; border-radius:3px; transition:width .4s; }
.event-fill.warn { background:var(--warn); }
.event-fill.danger { background:var(--danger); }
.event-count { font-size:11px; color:var(--text-3); text-align:right; }

@media (max-width: 1024px) {
  .kpi-row { grid-template-columns:repeat(2,1fr); }
  .two-col { grid-template-columns:1fr; }
}

@media (max-width: 720px) {
  .view { padding:20px 16px 28px; }
  .page-header { flex-direction:column; }
  .kpi-row { grid-template-columns:1fr; }
  .bar-chart { gap:2px; }
}
</style>
