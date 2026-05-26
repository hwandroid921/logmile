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
import { driveHistoryApi } from '@/api/driveHistoryApi'

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
  grid:    'rgba(81,95,122,0.14)',
  text:    'rgba(31,38,48,0.58)',
}

/* ── 상태 ── */
const range   = ref('14d')
const stats   = ref([])
const driveDetails = ref([])
const loading = ref(false)
const error   = ref('')

const rangeMap = { '1d': 1, '7d': 7, '14d': 14, '30d': 30 }

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
const hasStatsData = computed(() => statDays.value.some(day =>
  day.avgScore > 0 ||
  day.danger > 0 ||
  day.restMiss > 0 ||
  day.driveMinutes > 0 ||
  day.nightMinutes > 0 ||
  day.driveLogCount > 0,
))

const avgScore     = computed(() => statDays.value.length
  ? Math.round(statDays.value.reduce((s, d) => s + d.avgScore, 0) / statDays.value.length) : 0)
const dangerTotal  = computed(() => statDays.value.reduce((s, d) => s + d.danger, 0))
const missingTotal = computed(() => statDays.value.reduce((s, d) => s + d.restMiss, 0))
const totalDriveMin = computed(() => statDays.value.reduce((s, d) => s + d.driveMinutes, 0))
const totalNightMin = computed(() => statDays.value.reduce((s, d) => s + d.nightMinutes, 0))
const totalDriveLogs = computed(() => statDays.value.reduce((s, d) => s + d.driveLogCount, 0))
const totalDriveH  = computed(() => Math.round(totalDriveMin.value / 60))
const totalNightH  = computed(() => Math.round(totalNightMin.value / 60))
const hasDriveChartData = computed(() => totalDriveMin.value > 0 || totalNightMin.value > 0)
const hasEventChartData = computed(() => missingTotal.value > 0 || dangerTotal.value > 0)
const driveRows = computed(() => driveDetails.value
  .map((log) => {
    const continuousMinutes = Math.max(
      0,
      ...(log.fatigueEvents ?? []).map(ev => Number(ev.continuousDrivingMinutes ?? 0)),
    )
    const totalMinutes = Number(log.totalDrivingMinutes ?? 0)
    const nightMinutes = Number(log.nightDrivingMinutes ?? 0)
    return {
      driveLogId: log.driveLogId,
      date: String(log.startedAt ?? '').slice(0, 10),
      label: formatDate(String(log.startedAt ?? '').slice(0, 10)),
      plateNo: log.plateNo ?? '-',
      driverName: log.driverName ?? '-',
      status: log.status ?? '-',
      continuousMinutes,
      totalMinutes,
      nightMinutes,
      nightRatio: totalMinutes > 0 ? Math.round((nightMinutes / totalMinutes) * 100) : 0,
    }
  })
  .sort((a, b) => {
    if (a.date !== b.date) return b.date.localeCompare(a.date)
    return b.nightMinutes - a.nightMinutes || b.continuousMinutes - a.continuousMinutes
  })
)
const dailyDriveDetails = computed(() => {
  const map = new Map(statDays.value.map(day => [day.date, {
    date: day.date,
    label: day.label,
    continuousMinutes: 0,
    nightRatio: day.driveMinutes > 0 ? Math.round((day.nightMinutes / day.driveMinutes) * 100) : 0,
    logs: [],
  }]))
  for (const row of driveRows.value) {
    if (!map.has(row.date)) {
      map.set(row.date, {
        date: row.date,
        label: row.label,
        continuousMinutes: 0,
        nightRatio: 0,
        logs: [],
      })
    }
    const acc = map.get(row.date)
    acc.continuousMinutes = Math.max(acc.continuousMinutes, row.continuousMinutes)
    acc.logs.push(row)
  }
  return statDays.value.map(day => map.get(day.date))
})
const hasDrivingStatusData = computed(() => hasDriveChartData.value ||
  driveRows.value.some(row => row.continuousMinutes > 0 || row.totalMinutes > 0 || row.nightMinutes > 0)
)
const longestContinuousRow = computed(() => driveRows.value
  .slice()
  .sort((a, b) => b.continuousMinutes - a.continuousMinutes)[0] ?? null
)
const averageNightMinutesPerLog = computed(() => totalDriveLogs.value > 0
  ? Math.round(totalNightMin.value / totalDriveLogs.value)
  : 0
)
const normalDays = computed(() => statDays.value.filter(d => d.avgScore < 40).length)
const cautionDays = computed(() => statDays.value.filter(d => d.avgScore >= 40 && d.avgScore < 70).length)
const dangerDays = computed(() => statDays.value.filter(d => d.avgScore >= 70).length)
const peakScoreDay = computed(() => statDays.value
  .slice()
  .sort((a, b) => b.avgScore - a.avgScore)[0] ?? null
)
const top5MaxScore = computed(() => driveDetails.value
  .filter(log => log.maxFatigueScore != null)
  .sort((a, b) => (b.maxFatigueScore ?? 0) - (a.maxFatigueScore ?? 0))
  .slice(0, 5)
  .map(log => ({
    driveLogId:      log.driveLogId,
    date:            String(log.startedAt ?? '').slice(0, 10),
    label:           formatDate(String(log.startedAt ?? '').slice(0, 10)),
    plateNo:         log.plateNo ?? '-',
    driverName:      log.driverName ?? '-',
    maxFatigueScore: log.maxFatigueScore ?? 0,
  }))
)

const nightRatio = computed(() => totalDriveMin.value > 0
  ? Math.round((totalNightMin.value / totalDriveMin.value) * 100)
  : 0
)
const averageDriveMinutesPerLog = computed(() => totalDriveLogs.value > 0
  ? Math.round(totalDriveMin.value / totalDriveLogs.value)
  : 0
)
const scoreTrend = computed(() => {
  if (statDays.value.length < 2) return { delta: 0, label: '비교 데이터 부족' }
  const mid = Math.floor(statDays.value.length / 2)
  const prev = statDays.value.slice(0, mid)
  const recent = statDays.value.slice(mid)
  const avg = list => list.length
    ? list.reduce((sum, d) => sum + d.avgScore, 0) / list.length
    : 0
  const delta = Math.round(avg(recent) - avg(prev))
  return {
    delta,
    label: delta > 0 ? `+${delta}점 상승` : delta < 0 ? `${delta}점 하락` : '변화 없음',
  }
})
const eventDays = computed(() => statDays.value
  .filter(d => d.restMiss > 0 || d.danger > 0)
  .map(d => ({
    ...d,
    totalEvents: d.restMiss + d.danger,
  }))
)
const topEventDay = computed(() => eventDays.value
  .slice()
  .sort((a, b) => b.totalEvents - a.totalEvents)[0] ?? null
)

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
  return s >= 70 ? '위험 수준' : s >= 40 ? '주의 수준' : '정상 범위'
}
function formatMinutes(v) {
  if (!v) return '0분'
  const h = Math.floor(v / 60)
  const m = v % 60
  return h > 0 ? `${h}h ${m}m` : `${m}분`
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
      labels: { color: C.text, font: { size: 11, family: '"IBM Plex Mono", "Roboto Mono", ui-monospace, monospace' }, boxWidth: 10 },
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
  if (!hasStatsData.value) {
    scoreChart?.destroy()
    driveChart?.destroy()
    eventChart?.destroy()
    scoreChart = null
    driveChart = null
    eventChart = null
    return
  }

  const labels    = statDays.value.map(d => d.label)
  const scores    = statDays.value.map(d => d.avgScore)
  const driveH    = statDays.value.map(d => +(d.driveMinutes / 60).toFixed(1))
  const nightH    = statDays.value.map(d => +(d.nightMinutes / 60).toFixed(1))
  const continuousH = dailyDriveDetails.value.map(d => +(d.continuousMinutes / 60).toFixed(1))
  const restMiss  = statDays.value.map(d => d.restMiss)
  const dangers   = statDays.value.map(d => d.danger)
  const ptColors  = scores.map(scoreColor)

  scoreChart?.destroy()
  scoreChart = null
  if (scoreCanvas.value) {
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
  }

  driveChart?.destroy()
  driveChart = null
  if (driveCanvas.value && hasDrivingStatusData.value) {
    const maxDriveH = Math.max(...driveH, ...nightH, ...continuousH, 1)
    driveChart = new Chart(driveCanvas.value, {
      type: 'bar',
      data: {
        labels,
        datasets: [
          { label: '총 운행(h)',  data: driveH, backgroundColor: C.accent, borderRadius: 3 },
          { label: '최장 연속 운행(h)', data: continuousH, backgroundColor: 'rgba(197,138,58,0.58)', borderRadius: 3 },
          { label: '야간 운행(h)', data: nightH, backgroundColor: C.night,  borderRadius: 3 },
        ],
      },
      options: {
        ...chartDefaults,
        scales: {
          x: chartDefaults.scales.x,
          y: { ...chartDefaults.scales.y, min: 0, max: Math.ceil(maxDriveH),
            ticks: { ...chartDefaults.scales.y.ticks, callback: v => v + 'h' } },
        },
      },
    })
  }

  eventChart?.destroy()
  eventChart = null
  if (eventCanvas.value && hasEventChartData.value) {
    const maxEventCount = Math.max(...restMiss, ...dangers, 3)
    eventChart = new Chart(eventCanvas.value, {
      type: 'bar',
      data: {
        labels,
        datasets: [
          { label: '휴식 위반', data: restMiss, backgroundColor: 'rgba(197,138,58,0.65)', borderRadius: 3 },
          { label: '위험',     data: dangers,  backgroundColor: 'rgba(181,84,74,0.65)',  borderRadius: 3 },
        ],
      },
      options: {
        ...chartDefaults,
        scales: {
          x: chartDefaults.scales.x,
          y: { ...chartDefaults.scales.y, min: 0, max: Math.ceil(maxEventCount),
            ticks: { ...chartDefaults.scales.y.ticks, stepSize: 1,
              callback: v => Number.isInteger(v) ? v + '건' : '' } },
        },
      },
    })
  }
}

/* ── API ── */
async function fetchStats() {
  loading.value = true
  error.value   = ''
  try {
    const days = rangeMap[range.value] ?? 14
    const [{ data: statsData }, { data: historyData }] = await Promise.all([
      fatigueStatsApi.getStats({ days }),
      driveHistoryApi.getList(),
    ])
    stats.value = Array.isArray(statsData) ? statsData : []

    const fromDate = stats.value[0]?.date
    const toDate = stats.value[stats.value.length - 1]?.date
    const logs = Array.isArray(historyData)
      ? historyData.filter(log => {
        const date = String(log.startedAt ?? '').slice(0, 10)
        if (!date) return false
        if (fromDate && date < fromDate) return false
        if (toDate && date > toDate) return false
        return true
      }).slice(0, 30)
      : []
    const detailResults = await Promise.allSettled(
      logs.map(log => driveHistoryApi.getDetail(log.driveLogId)),
    )
    driveDetails.value = detailResults
      .filter(result => result.status === 'fulfilled')
      .map(result => result.value.data)
  } catch (err) {
    stats.value = []
    driveDetails.value = []
    error.value = err?.response?.data?.message || '통계 데이터를 불러오지 못했습니다.'
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
watch([statDays, dailyDriveDetails], () => {
  // canvas가 DOM에 마운트된 상태여야 그릴 수 있음
  renderCharts()
}, { flush: 'post' })

onMounted(fetchStats)
onUnmounted(() => { scoreChart?.destroy(); driveChart?.destroy(); eventChart?.destroy() })
</script>

<template>
  <div class="view">
    <div class="breadcrumb mono">
      관리자 / 통계 · API /api/fatigue/stats · 기간 {{ range }}
    </div>

    <div class="page-header">
      <div>
        <h2 class="page-title">통계 · {{ range }} 리포트</h2>
        <p class="page-sub">피로도 점수 누적 데이터 기반 운행 통계 — {{ statDays.length }}일 집계</p>
      </div>
      <div class="range-btns">
        <button v-for="r in ['1d','7d','14d','30d']" :key="r"
          class="range-btn mono" :class="{ active: range === r }"
          :disabled="loading" @click="setRange(r)">{{ r }}</button>
      </div>
    </div>

    <div v-if="error" class="notice error-box">{{ error }}</div>
    <div v-else-if="!loading && !hasStatsData" class="notice empty-notice mono">
      데이터 없음 · 조회 기간 내 집계된 운행/피로도 데이터가 없습니다.
    </div>

    <div v-if="loading" class="card state-card mono">통계 데이터를 불러오는 중입니다.</div>

    <template v-else>
      <!-- KPI 요약 카드 -->
      <div class="kpi-row">
        <div class="kpi-card">
          <div class="kpi-label mono">평균 피로 점수</div>
          <div class="kpi-val" :style="{ color: scoreColor(avgScore) }">{{ avgScore }}</div>
          <div class="kpi-sub">{{ range }} 기준 · <strong>{{ scoreLevel(avgScore) }}</strong></div>
          <div class="kpi-bar-wrap">
            <div class="kpi-bar-fill" :style="{ width: avgScore + '%', background: scoreColor(avgScore) }" />
          </div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label mono">위험 이벤트</div>
          <div class="kpi-val text-danger">{{ dangerTotal }}</div>
          <div class="kpi-sub">{{ range }} 기간 누적 발생 건수</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label mono">휴식 위반</div>
          <div class="kpi-val text-warn">{{ missingTotal }}</div>
          <div class="kpi-sub">{{ range }} 기간 누적 위반 건수</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label mono">총 운행 시간</div>
          <div class="kpi-val">{{ totalDriveH }}<span class="kpi-unit">h</span></div>
          <div class="kpi-sub">야간 {{ totalNightH }}h · {{ totalDriveLogs }}건 운행</div>
        </div>
      </div>

      <!-- 통계 인사이트 -->
      <div class="insight-grid">
        <div class="card insight-card">
          <div class="insight-label mono">최고 피로일</div>
          <div class="insight-main" :style="{ color: scoreColor(peakScoreDay?.avgScore ?? 0) }">
            {{ peakScoreDay?.label ?? '-' }}
          </div>
          <div class="insight-sub">
            평균 <strong>{{ peakScoreDay?.avgScore ?? 0 }}점</strong> · 휴식 위반 {{ peakScoreDay?.restMiss ?? 0 }}건 · 위험 {{ peakScoreDay?.danger ?? 0 }}건
          </div>
        </div>

        <div class="card insight-card">
          <div class="insight-label mono">야간 운행 비율</div>
          <div class="insight-main">{{ nightRatio }}<span>%</span></div>
          <div class="insight-sub">총 {{ totalDriveH }}h 중 야간 {{ totalNightH }}h</div>
        </div>

        <div class="card insight-card">
          <div class="insight-label mono">건당 운행 시간</div>
          <div class="insight-main">{{ formatMinutes(averageDriveMinutesPerLog) }}</div>
          <div class="insight-sub">{{ totalDriveLogs }}건 운행 기준 평균</div>
        </div>

        <div class="card insight-card">
          <div class="insight-label mono">건당 야간 운행</div>
          <div class="insight-main text-danger">{{ formatMinutes(averageNightMinutesPerLog) }}</div>
          <div class="insight-sub">운행 1건당 야간 운행 평균</div>
        </div>

        <div class="card insight-card">
          <div class="insight-label mono">최장 연속 운행</div>
          <div class="insight-main text-warn">{{ formatMinutes(longestContinuousRow?.continuousMinutes ?? 0) }}</div>
          <div class="insight-sub">
            {{ longestContinuousRow?.plateNo ?? '차량 정보 없음' }} · {{ longestContinuousRow?.driverName ?? '운전자 정보 없음' }}
          </div>
        </div>
      </div>

      <!-- 피로 점수 라인 차트 + Top 5 최고 피로도 -->
      <div class="two-col">
        <div class="card chart-card">
          <div class="card-hdr">
            <div class="card-title">일별 평균 피로 점수</div>
            <span class="mono card-meta">{{ statDays.length }}일 · 점수 0~100</span>
          </div>
          <div v-if="hasStatsData" class="canvas-wrap" style="height:180px">
            <canvas ref="scoreCanvas" />
          </div>
          <div v-else class="chart-empty" style="height:180px">
            조회된 통계 데이터가 없습니다.
          </div>
          <div v-if="hasStatsData" class="level-bars score-level-bars">
            <div class="level-row">
              <span class="mono level-label normal">정상</span>
              <div class="level-track"><div class="level-fill normal" :style="{ width: statDays.length ? (normalDays / statDays.length * 100) + '%' : '0%' }" /></div>
              <strong>{{ normalDays }}일 <span class="level-range">0~39</span></strong>
            </div>
            <div class="level-row">
              <span class="mono level-label caution">주의</span>
              <div class="level-track"><div class="level-fill caution" :style="{ width: statDays.length ? (cautionDays / statDays.length * 100) + '%' : '0%' }" /></div>
              <strong>{{ cautionDays }}일 <span class="level-range">40~69</span></strong>
            </div>
            <div class="level-row">
              <span class="mono level-label danger">위험</span>
              <div class="level-track"><div class="level-fill danger" :style="{ width: statDays.length ? (dangerDays / statDays.length * 100) + '%' : '0%' }" /></div>
              <strong>{{ dangerDays }}일 <span class="level-range">≥70</span></strong>
            </div>
          </div>
        </div>

        <div class="card chart-card">
          <div class="card-hdr">
            <div class="card-title">최고 피로 점수 TOP 5</div>
            <span class="mono card-meta">{{ range }} · 운행별 최대 점수 기준</span>
          </div>
          <div v-if="top5MaxScore.length" class="top5-list">
            <div v-for="(item, i) in top5MaxScore" :key="item.driveLogId" class="top5-row">
              <span class="mono top5-rank">{{ i + 1 }}</span>
              <div class="top5-score" :style="{ color: scoreColor(item.maxFatigueScore) }">
                {{ item.maxFatigueScore }}
              </div>
              <div class="top5-info">
                <div class="top5-meta mono">{{ item.label }}</div>
                <div class="top5-vehicle">
                  <strong>{{ item.plateNo }}</strong>
                  <span>{{ item.driverName }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="chart-empty" style="height:140px">
            운행 상세 데이터가 없습니다.
          </div>
        </div>
      </div>

      <!-- 하단 두 차트 -->
      <div class="two-col">
        <div class="card chart-card">
          <div class="card-hdr">
            <div>
              <div class="card-title">일별 운행 시간</div>
              <p class="card-sub">총 운행 / 최장 연속 운행 / 야간 운행을 같은 축에서 비교</p>
            </div>
            <span class="mono card-meta">총 {{ totalDriveH }}h · 야간 {{ totalNightH }}h · 야간 {{ nightRatio }}%</span>
          </div>
          <div v-if="hasDrivingStatusData" class="canvas-wrap" style="height:180px">
            <canvas ref="driveCanvas" />
          </div>
          <div v-else class="chart-empty">
            종료된 운행 시간이 아직 집계되지 않았습니다.
          </div>
          <div class="drive-legend-row">
            <span class="chip chip-brand">총 운행</span>
            <span class="chip chip-warn">최장 연속 운행</span>
            <span class="chip chip-danger">야간 운행</span>
          </div>
          <div v-if="hasDrivingStatusData" class="daily-night-list">
            <div v-for="day in dailyDriveDetails" :key="day.date" class="night-day-row">
              <span class="mono night-day-date">{{ day.label }}</span>
              <div class="night-ratio-track">
                <div class="night-ratio-fill" :style="{ width: day.nightRatio + '%' }" />
              </div>
              <strong class="mono">{{ day.nightRatio }}%</strong>
            </div>
          </div>
          <div v-if="driveRows.length" class="drive-log-list">
            <div v-for="row in driveRows.slice(0, 8)" :key="row.driveLogId" class="drive-log-row">
              <div class="drive-log-main">
                <span class="mono drive-log-date">{{ row.label }}</span>
                <strong>{{ row.plateNo }}</strong>
                <span>{{ row.driverName }}</span>
              </div>
              <div class="drive-log-metrics mono">
                <span>연속 {{ formatMinutes(row.continuousMinutes) }}</span>
                <span>총 {{ formatMinutes(row.totalMinutes) }}</span>
                <span class="night-metric">야간 {{ formatMinutes(row.nightMinutes) }} · {{ row.nightRatio }}%</span>
              </div>
            </div>
          </div>
          <div v-else class="chart-note">
            차량/운전자별 운행 상세는 운행 이력 상세 API 응답이 있을 때 표시됩니다.
          </div>
        </div>

        <div class="card chart-card">
          <div class="card-hdr">
            <div class="card-title">휴식 위반 / 위험 이벤트</div>
            <span class="mono card-meta">{{ missingTotal + dangerTotal }}건</span>
          </div>
          <div v-if="hasEventChartData" class="event-summary-grid">
            <div class="event-summary warn">
              <span class="mono event-summary-label">휴식 위반</span>
              <strong>{{ missingTotal }}</strong>
              <span>휴식 위반 누적</span>
            </div>
            <div class="event-summary danger">
              <span class="mono event-summary-label">위험 이벤트</span>
              <strong>{{ dangerTotal }}</strong>
              <span>위험 이벤트 누적</span>
            </div>
            <div class="event-summary peak">
              <span class="mono event-summary-label">최다 발생일</span>
              <strong>{{ topEventDay?.label }}</strong>
              <span>{{ topEventDay?.totalEvents }}건 발생</span>
            </div>
          </div>
          <div v-if="hasEventChartData" class="canvas-wrap" style="height:180px">
            <canvas ref="eventCanvas" />
          </div>
          <div v-if="hasEventChartData" class="event-day-list">
            <div v-for="day in eventDays" :key="day.date" class="event-day-row">
              <span class="mono event-day-date">{{ day.label }}</span>
              <span class="chip chip-warn">휴식 위반 {{ day.restMiss }}건</span>
              <span class="chip chip-danger">위험 {{ day.danger }}건</span>
            </div>
          </div>
          <div v-else class="chart-empty">
            휴식 위반 또는 위험 이벤트가 아직 없습니다.
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.view { display:flex; flex-direction:column; gap:16px; padding:20px 20px 32px; }
.breadcrumb { font-size: 14px; color: var(--text-3); letter-spacing:0.04em; }

.page-header { display:flex; align-items:flex-start; justify-content:space-between; gap:24px; }
.page-title  { font-size:24px; font-weight:700; color: var(--text-1); margin:0 0 4px; letter-spacing: 0; }
.page-sub    { font-size: 14px; color: var(--text-3); margin:0; }
.range-btns  { display:flex; gap:4px; }
.range-btn {
  padding:6px 14px; border-radius:var(--r-md); font-size: 14px;
  background:none; border:1px solid var(--line-2); color: var(--text-3);
  cursor:pointer; transition:all .12s;
}
.range-btn.active { background:var(--accent-soft); border-color: var(--accent-line); color: var(--accent); font-weight:700; }
.range-btn:disabled { opacity:.45; cursor:wait; }

.notice    { border-radius:var(--r-md); font-size: 14px; padding:12px 14px; }
.error-box { border:1px solid var(--danger); background:var(--danger-soft); color: var(--danger); }
.empty-notice { border:1px solid var(--line-2); background:var(--bg-2); color: var(--text-3); }
.state-card { min-height:180px; display:flex; align-items:center; justify-content:center; color: var(--text-3); font-size: 14px; }

.kpi-row { display:grid; grid-template-columns:repeat(4,1fr); gap:10px; }
.kpi-card {
  background:var(--bg-1); border:1px solid var(--line-2); border-radius:var(--r-lg);
  padding:16px 18px; display:flex; flex-direction:column; gap:4px;
  box-shadow:var(--shadow-1);
}
.kpi-label { font-size: 14px; letter-spacing:0.04em; color: var(--text-3); font-weight:600; }
.kpi-val   { font-size:32px; font-weight:800; color: var(--text-1); letter-spacing: 0; line-height:1; }
.kpi-unit  { font-size:16px; font-weight:400; margin-left:2px; }
.kpi-sub   { font-size: 14px; color: var(--text-3); }
.kpi-sub strong { color: var(--text-2); font-weight:600; }
.kpi-bar-wrap { height:3px; background:var(--bg-3); border-radius:2px; overflow:hidden; margin-top:8px; }
.kpi-bar-fill { height:100%; border-radius:2px; transition:width .4s; }
.text-warn { color: var(--warn); }
.text-danger { color: var(--danger); }

.chart-card { padding:20px; }
.card-hdr   { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }
.card-hdr.compact { margin-bottom:12px; }
.card-title { font-size: 16px; font-weight:700; color: var(--text-1); }
.card-sub { margin:3px 0 0; font-size: 14px; color: var(--text-3); }
.card-meta  { font-size: 14px; color: var(--text-3); }
.insight-grid {
  display:grid; grid-template-columns:repeat(5,1fr); gap:10px;
}
.insight-card {
  padding:16px 18px; min-height:122px; display:flex; flex-direction:column; justify-content:space-between;
}
.level-card { justify-content:flex-start; }
.level-bars { display:flex; flex-direction:column; gap:9px; }
.level-row {
  display:grid; grid-template-columns:72px 1fr 34px; gap:8px; align-items:center;
}
.level-label { font-size: 14px; letter-spacing:0.06em; }
.level-label.normal { color: var(--ok); }
.level-label.caution { color: var(--warn); }
.level-label.danger { color: var(--danger); }
.level-track {
  height:7px; background:var(--bg-3); border-radius:999px; overflow:hidden;
}
.level-fill { height:100%; border-radius:999px; transition:width .35s; }
.level-fill.normal { background:var(--ok); }
.level-fill.caution { background:var(--warn); }
.level-fill.danger { background:var(--danger); }
.level-row strong { font-size: 14px; color: var(--text-2); text-align:right; }
.insight-label { font-size: 14px; letter-spacing:0.04em; color: var(--text-3); font-weight:600; }
.insight-main {
  font-size:24px; font-weight:800; color: var(--text-1); line-height:1.05;
}
.insight-main span { font-size: 14px; font-weight:500; margin-left:2px; }
.insight-main.up { color: var(--danger); }
.insight-main.down { color: var(--ok); }
.insight-sub { font-size: 14px; color: var(--text-3); line-height:1.45; }
.insight-sub strong { color: var(--text-2); font-weight:600; }

.canvas-wrap { position:relative; }
.canvas-wrap canvas { display:block; width:100% !important; }
.chart-empty {
  height:180px; display:flex; align-items:center; justify-content:center;
  border:1px dashed var(--line-2); border-radius:var(--r-md);
  color: var(--text-3); font-size: 14px; background:var(--bg-2);
}
.event-summary-grid {
  display:grid; grid-template-columns:repeat(3,1fr); gap:8px; margin-bottom:14px;
}
.event-summary {
  min-width:0; padding:10px 12px; border:1px solid var(--line-1);
  border-radius:var(--r-md); background:var(--bg-2);
  display:flex; flex-direction:column; gap:2px;
}
.event-summary.warn { border-color:rgba(197,138,58,.25); background:var(--warn-soft); }
.event-summary.danger { border-color:rgba(181,84,74,.25); background:var(--danger-soft); }
.event-summary.peak { border-color: var(--accent-line); background:var(--accent-soft); }
.event-summary-label { font-size: 14px; letter-spacing:0.07em; color: var(--text-3); }
.event-summary strong { font-size:22px; line-height:1; color: var(--text-1); }
.event-summary span:last-child { font-size: 14px; color: var(--text-3); }
.event-day-list {
  margin-top:12px; display:flex; flex-direction:column; gap:6px;
}
.event-day-row {
  display:flex; align-items:center; gap:8px; min-height:30px;
  padding:6px 8px; border-radius:var(--r-sm); background:var(--bg-2);
}
.event-day-date { width:44px; flex-shrink:0; font-size: 14px; color: var(--text-3); }
.drive-legend-row {
  display:flex; flex-wrap:wrap; gap:8px; margin-top:12px;
}
.daily-night-list {
  margin-top:12px; display:grid; grid-template-columns:repeat(2,1fr); gap:6px 12px;
}
.night-day-row {
  display:grid; grid-template-columns:42px 1fr 38px; gap:8px; align-items:center;
  min-height:26px; padding:4px 0;
}
.night-day-date { font-size: 14px; color: var(--text-3); }
.night-ratio-track {
  height:7px; background:var(--bg-3); border-radius:999px; overflow:hidden;
}
.night-ratio-fill {
  height:100%; background:var(--danger); border-radius:999px; transition:width .35s;
}
.night-day-row strong { font-size: 14px; color: var(--danger); text-align:right; }
.drive-log-list {
  margin-top:14px; display:flex; flex-direction:column; gap:6px;
}
.drive-log-row {
  display:flex; align-items:center; justify-content:space-between; gap:12px;
  padding:9px 10px; border:1px solid var(--line-1); border-radius:var(--r-md);
  background:var(--bg-2);
}
.drive-log-main {
  display:flex; align-items:center; gap:8px; min-width:0;
}
.drive-log-main strong { color: var(--text-1); font-size: 14px; white-space:nowrap; }
.drive-log-main span:last-child {
  color: var(--text-3); font-size: 14px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;
}
.drive-log-date { color: var(--text-3); font-size: 14px; width:42px; flex-shrink:0; }
.drive-log-metrics {
  display:flex; align-items:center; justify-content:flex-end; flex-wrap:wrap; gap:8px;
  color: var(--text-3); font-size: 14px;
}
.night-metric { color: var(--danger); }
.chart-note {
  margin-top:12px; padding:10px 12px; border:1px dashed var(--line-2); border-radius:var(--r-md);
  color: var(--text-3); background:var(--bg-2); font-size: 14px;
}

.score-level-bars {
  margin-top:14px; gap:8px;
}
.level-range {
  font-size: 14px; font-weight:400; color: var(--text-3); margin-left:3px;
}

.top5-list { display:flex; flex-direction:column; gap:8px; }
.top5-row {
  display:grid; grid-template-columns:20px 52px 1fr; gap:10px; align-items:center;
  padding:10px 12px; border:1px solid var(--line-1); border-radius:var(--r-md);
  background:var(--bg-2);
}
.top5-rank { font-size: 14px; color: var(--text-3); text-align:center; }
.top5-score { font-size:26px; font-weight:800; line-height:1; }
.top5-info { min-width:0; }
.top5-meta { font-size: 14px; color: var(--text-3); }
.top5-vehicle { display:flex; align-items:center; gap:6px; margin-top:3px; }
.top5-vehicle strong { font-size: 14px; color: var(--text-1); white-space:nowrap; }
.top5-vehicle span { font-size: 14px; color: var(--text-3); overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }

.two-col { display:grid; grid-template-columns:1fr 1fr; gap:10px; }

@media (max-width:1024px) {
  .kpi-row { grid-template-columns:repeat(2,1fr); }
  .insight-grid { grid-template-columns:repeat(2,1fr); }
  .two-col { grid-template-columns:1fr; }
  .daily-night-list { grid-template-columns:1fr; }
}
@media (max-width:720px) {
  .view { padding:20px 16px 28px; }
  .page-header { flex-direction:column; }
  .kpi-row { grid-template-columns:1fr; }
  .insight-grid { grid-template-columns:1fr; }
  .event-summary-grid { grid-template-columns:1fr; }
  .event-day-row { align-items:flex-start; flex-direction:column; }
  .drive-log-row { align-items:flex-start; flex-direction:column; }
  .drive-log-metrics { justify-content:flex-start; }
}

/* ─── 반응형 레이아웃 ─── */
@media (min-width: 1360px) {
  .view { padding: 24px 32px 40px; }
}
@media (min-width: 1720px) {
  .view { padding: 28px 56px 48px; }
}
@media (min-width: 1860px) {
  .view { padding: 32px 64px 56px; }
}
</style>
