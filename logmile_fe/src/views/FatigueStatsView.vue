<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import {
  Chart,
  LineController, LineElement, PointElement,
  BarController, BarElement,
  LinearScale, CategoryScale,
  Filler, Tooltip, Legend,
} from 'chart.js'
import { fatigueStatsApi } from '@/api/fatigueStatsApi'

Chart.register(
  LineController, LineElement, PointElement,
  BarController, BarElement,
  LinearScale, CategoryScale,
  Filler, Tooltip, Legend,
)

/* ── 색상 토큰 (tokens.css 기준) ── */
const C = {
  ok:      '#5E8A6F',
  warn:    '#C58A3A',
  danger:  '#B5544A',
  accent:  'rgba(81,95,122,0.75)',
  night:   'rgba(181,84,74,0.55)',
  grid:    'rgba(255,255,255,0.06)',
  text:    'rgba(255,255,255,0.35)',
}

/* ── 상태 ── */
const range   = ref('14d')
const stats   = ref([])
const loading = ref(false)
const error   = ref('')

const rangeMap = { '7d': 7, '14d': 14, '30d': 30 }

/* ── 파생 데이터 ── */
const statDays = computed(() => stats.value.map((row) => ({
  date:          row.date,
  label:         formatDate(row.date),
  avgScore:      Math.round(Number(row.averageFatigueScore ?? 0)),
  danger:        Number(row.dangerEventCount ?? 0),
  restMiss:      Number(row.restViolationCount ?? 0),
  driveMinutes:  Number(row.totalDrivingMinutes ?? 0),
  nightMinutes:  Number(row.nightDrivingMinutes ?? 0),
  driveLogCount: Number(row.driveLogCount ?? 0),
})))

const avgScore     = computed(() => statDays.value.length
  ? Math.round(statDays.value.reduce((s, d) => s + d.avgScore, 0) / statDays.value.length) : 0)
const dangerTotal  = computed(() => statDays.value.reduce((s, d) => s + d.danger, 0))
const missingTotal = computed(() => statDays.value.reduce((s, d) => s + d.restMiss, 0))
const totalDriveMin = computed(() => statDays.value.reduce((s, d) => s + d.driveMinutes, 0))
const totalNightMin = computed(() => statDays.value.reduce((s, d) => s + d.nightMinutes, 0))
const totalDriveLogs = computed(() => statDays.value.reduce((s, d) => s + d.driveLogCount, 0))
const totalDriveH  = computed(() => Math.round(totalDriveMin.value / 60))
const totalNightH  = computed(() => Math.round(totalNightMin.value / 60))

/* ── 유틸 ── */
function formatDate(v) {
  if (!v) return '-'
  const [, m, d] = String(v).split('-')
  return m && d ? `${m}/${d}` : v
}
function scoreColor(s) {
  return s >= 70 ? C.danger : s >= 40 ? C.warn : C.ok
}
function scoreLevel(s) {
  return s >= 70 ? 'DANGER 수준' : s >= 40 ? 'CAUTION 수준' : '정상 범위'
}

/* ── Chart.js 인스턴스 ── */
const scoreCanvas = ref(null)
const driveCanvas = ref(null)
const eventCanvas = ref(null)

let scoreChart = null
let driveChart = null
let eventChart = null

const chartDefaults = {
  responsive: true,
  maintainAspectRatio: false,
  animation: { duration: 400 },
  plugins: {
    legend: {
      labels: { color: C.text, font: { size: 11, family: 'ui-monospace, monospace' }, boxWidth: 10 },
    },
    tooltip: {
      backgroundColor: 'rgba(20,22,28,0.92)',
      titleColor: 'rgba(255,255,255,0.7)',
      bodyColor: '#fff',
      borderColor: 'rgba(255,255,255,0.1)',
      borderWidth: 1,
      cornerRadius: 6,
      padding: 10,
    },
  },
  scales: {
    x: {
      ticks:  { color: C.text, font: { size: 10 }, maxRotation: 0 },
      grid:   { color: C.grid },
      border: { color: 'transparent' },
    },
    y: {
      ticks:  { color: C.text, font: { size: 10 } },
      grid:   { color: C.grid },
      border: { color: 'transparent' },
    },
  },
}

function renderCharts() {
  if (!statDays.value.length) return

  const labels    = statDays.value.map(d => d.label)
  const scores    = statDays.value.map(d => d.avgScore)
  const driveH    = statDays.value.map(d => +(d.driveMinutes / 60).toFixed(1))
  const nightH    = statDays.value.map(d => +(d.nightMinutes / 60).toFixed(1))
  const restMiss  = statDays.value.map(d => d.restMiss)
  const dangers   = statDays.value.map(d => d.danger)
  const ptColors  = scores.map(scoreColor)

  /* 점수 라인 차트 */
  scoreChart?.destroy()
  scoreChart = new Chart(scoreCanvas.value, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label: '평균 피로 점수',
        data: scores,
        borderColor: 'rgba(150,165,190,0.85)',
        backgroundColor: 'rgba(150,165,190,0.08)',
        pointBackgroundColor: ptColors,
        pointBorderColor: ptColors,
        pointRadius: 5,
        pointHoverRadius: 7,
        tension: 0.35,
        fill: true,
        borderWidth: 2,
      }],
    },
    options: {
      ...chartDefaults,
      plugins: {
        ...chartDefaults.plugins,
        legend: { display: false },
        tooltip: {
          ...chartDefaults.plugins.tooltip,
          callbacks: { label: ctx => ` ${ctx.raw}점` },
        },
      },
      scales: {
        x: chartDefaults.scales.x,
        y: { ...chartDefaults.scales.y, min: 0, max: 100,
          ticks: { ...chartDefaults.scales.y.ticks, stepSize: 20,
            callback: v => v + '점' } },
      },
    },
  })

  /* 운행 시간 바 차트 */
  driveChart?.destroy()
  driveChart = new Chart(driveCanvas.value, {
    type: 'bar',
    data: {
      labels,
      datasets: [
        { label: '총 운행(h)',  data: driveH, backgroundColor: C.accent, borderRadius: 3 },
        { label: '야간 운행(h)', data: nightH, backgroundColor: C.night,  borderRadius: 3 },
      ],
    },
    options: {
      ...chartDefaults,
      scales: {
        x: chartDefaults.scales.x,
        y: { ...chartDefaults.scales.y, min: 0,
          ticks: { ...chartDefaults.scales.y.ticks, callback: v => v + 'h' } },
      },
    },
  })

  /* 이벤트 바 차트 */
  eventChart?.destroy()
  eventChart = new Chart(eventCanvas.value, {
    type: 'bar',
    data: {
      labels,
      datasets: [
        { label: '휴식 위반', data: restMiss, backgroundColor: 'rgba(197,138,58,0.65)', borderRadius: 3 },
        { label: 'DANGER',   data: dangers,  backgroundColor: 'rgba(181,84,74,0.65)',  borderRadius: 3 },
      ],
    },
    options: {
      ...chartDefaults,
      scales: {
        x: chartDefaults.scales.x,
        y: { ...chartDefaults.scales.y, min: 0,
          ticks: { ...chartDefaults.scales.y.ticks, stepSize: 1,
            callback: v => Number.isInteger(v) ? v + '건' : '' } },
      },
    },
  })
}

/* ── API ── */
async function fetchStats() {
  loading.value = true
  error.value   = ''
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

function setRange(r) {
  if (range.value === r) return
  range.value = r
  fetchStats()
}

/* 데이터 변경 시 차트 갱신 */
watch(statDays, () => {
  // canvas가 DOM에 마운트된 상태여야 그릴 수 있음
  if (scoreCanvas.value) renderCharts()
}, { flush: 'post' })

onMounted(fetchStats)
onUnmounted(() => { scoreChart?.destroy(); driveChart?.destroy(); eventChart?.destroy() })
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
        <button v-for="r in ['7d','14d','30d']" :key="r"
          class="range-btn mono" :class="{ active: range === r }"
          :disabled="loading" @click="setRange(r)">{{ r }}</button>
      </div>
    </div>

    <div v-if="error" class="notice error-box">{{ error }}</div>

    <div v-if="loading" class="card state-card mono">피로도 통계 데이터를 불러오는 중입니다.</div>
    <div v-else-if="!statDays.length" class="card state-card">표시할 피로도 통계 데이터가 없습니다.</div>

    <template v-else>
      <!-- KPI 요약 카드 -->
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
          <div class="kpi-val" style="color:#B5544A">{{ dangerTotal }}</div>
          <div class="kpi-sub">일별 위험 이벤트 누적</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label mono">휴식 위반</div>
          <div class="kpi-val" style="color:#C58A3A">{{ missingTotal }}</div>
          <div class="kpi-sub">휴식 판단 위반 누적</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label mono">총 운행 / 야간</div>
          <div class="kpi-val">{{ totalDriveH }}<span class="kpi-unit">h</span></div>
          <div class="kpi-sub">야간 {{ totalNightH }}h · 운행 {{ totalDriveLogs }}건</div>
        </div>
      </div>

      <!-- 피로 점수 라인 차트 -->
      <div class="card chart-card">
        <div class="card-hdr">
          <div class="card-title">일별 평균 피로 점수</div>
          <span class="mono card-meta">{{ statDays.length }}일 · 점수 0~100</span>
        </div>
        <div class="canvas-wrap" style="height:180px">
          <canvas ref="scoreCanvas" />
        </div>
        <div class="score-band-row mono">
          <span style="color:#5E8A6F">● NORMAL  0~39</span>
          <span style="color:#C58A3A">● CAUTION 40~69</span>
          <span style="color:#B5544A">● DANGER  ≥70</span>
        </div>
      </div>

      <!-- 하단 두 차트 -->
      <div class="two-col">
        <div class="card chart-card">
          <div class="card-hdr">
            <div class="card-title">일별 운행 시간</div>
            <span class="mono card-meta">총 {{ totalDriveH }}h · 야간 {{ totalNightH }}h</span>
          </div>
          <div class="canvas-wrap" style="height:180px">
            <canvas ref="driveCanvas" />
          </div>
        </div>

        <div class="card chart-card">
          <div class="card-hdr">
            <div class="card-title">휴식 위반 / 위험 이벤트</div>
            <span class="mono card-meta">{{ missingTotal + dangerTotal }}건</span>
          </div>
          <div class="canvas-wrap" style="height:180px">
            <canvas ref="eventCanvas" />
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
  background:none; border:1px solid var(--line-2); color:var(--text-3);
  cursor:pointer; transition:all .12s;
}
.range-btn.active { background:var(--accent-soft); border-color:var(--accent-line); color:var(--accent); font-weight:700; }
.range-btn:disabled { opacity:.45; cursor:wait; }

.notice   { border-radius:var(--r-md); font-size:12px; padding:12px 14px; }
.error-box { border:1px solid rgba(181,84,74,.32); background:rgba(181,84,74,.08); color:#B5544A; }
.state-card { min-height:180px; display:flex; align-items:center; justify-content:center; color:var(--text-3); font-size:13px; }

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

.chart-card { padding:20px; }
.card-hdr   { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }
.card-title { font-size:14px; font-weight:700; color:var(--text-1); }
.card-meta  { font-size:11px; color:var(--text-4); }

.canvas-wrap { position:relative; }
.canvas-wrap canvas { display:block; width:100% !important; }

.score-band-row {
  display:flex; gap:20px; margin-top:12px;
  font-size:10.5px; letter-spacing:0.04em;
}

.two-col { display:grid; grid-template-columns:1fr 1fr; gap:10px; }

@media (max-width:1024px) {
  .kpi-row { grid-template-columns:repeat(2,1fr); }
  .two-col { grid-template-columns:1fr; }
}
@media (max-width:720px) {
  .view { padding:20px 16px 28px; }
  .page-header { flex-direction:column; }
  .kpi-row { grid-template-columns:1fr; }
}
</style>
