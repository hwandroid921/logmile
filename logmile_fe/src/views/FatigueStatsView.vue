<script setup>
import { ref, computed } from 'vue'
import { days, driveLogs, vehicles } from '@/data/mockData'

const range = ref('14d')

const rangeMap = { '7d': 7, '14d': 14, '30d': 14 } // mock has 14 days max
const slicedDays = computed(() => {
  const n = rangeMap[range.value] ?? 14
  return days.slice(-n)
})

const avgScore    = computed(() => Math.round(slicedDays.value.reduce((s,d) => s + d.avgScore, 0) / slicedDays.value.length))
const dangerTotal = computed(() => slicedDays.value.reduce((s,d) => s + d.danger, 0))
const missingTotal= computed(() => slicedDays.value.reduce((s,d) => s + d.restMiss, 0))
const totalDriveH = computed(() => {
  const totalMin = slicedDays.value.reduce((s,d) => s + d.driveHours, 0)
  return Math.round(totalMin / 60)
})
const totalNightH = computed(() => {
  const totalMin = slicedDays.value.reduce((s,d) => s + d.nightHours, 0)
  return Math.round(totalMin / 60)
})

const maxScore = computed(() => Math.max(...slicedDays.value.map(d => d.avgScore), 1))

// 시나리오 분포
const scenarioA = computed(() => driveLogs.filter(l => l.scenario === 'A').length)
const scenarioB = computed(() => driveLogs.filter(l => l.scenario === 'B').length)
const scenarioC = computed(() => driveLogs.filter(l => l.scenario === 'C').length)
const scenarioTotal = computed(() => driveLogs.length)

// 휴식 분포 (vehicles의 restValid/restSuff/restInvalid/restMiss 합산)
const restSuff    = computed(() => vehicles.reduce((s,v) => s + v.restSuff,    0))
const restValid   = computed(() => vehicles.reduce((s,v) => s + v.restValid,   0))
const restInvalid = computed(() => vehicles.reduce((s,v) => s + v.restInvalid, 0))
const restMiss    = computed(() => vehicles.reduce((s,v) => s + v.restMiss,    0))
const restTotal   = computed(() => restSuff.value + restValid.value + restInvalid.value + restMiss.value)

function restPct(v) { return restTotal.value ? Math.round(v / restTotal.value * 100) : 0 }

// 시간대별 평균 점수 (mock: 24h)
const hourlyScores = [
  0, 0, 12, 28, 38, 42, 35, 30, 25, 22, 28, 34,
  40, 38, 33, 29, 32, 36, 44, 52, 61, 58, 42, 18,
]
const maxHourly = Math.max(...hourlyScores, 1)

// 운행 상태 분포
const statusRunning   = computed(() => driveLogs.filter(l => l.status === 'RUNNING').length)
const statusCompleted = computed(() => driveLogs.filter(l => l.status === 'COMPLETED').length)
const statusStopped   = computed(() => driveLogs.filter(l => l.status === 'STOPPED').length)

function scoreColor(s) {
  if (s >= 70) return 'var(--danger)'
  if (s >= 40) return 'var(--warn)'
  return 'var(--ok)'
}
</script>

<template>
  <div class="view">
    <div class="breadcrumb mono">
      ADMIN / FATIGUE_STATS · RANGE {{ range.toUpperCase() }} · 한라물류센터 · 차량 {{ vehicles.length }}
    </div>

    <div class="page-header">
      <div>
        <h2 class="page-title">통계 · {{ range }} 리포트</h2>
        <p class="page-sub">피로도 점수 누적 데이터 기반 운행 통계 — {{ slicedDays.length }}일 집계</p>
      </div>
      <div class="range-btns">
        <button v-for="r in ['7d','14d','30d']" :key="r"
          class="range-btn mono" :class="{ active: range===r }"
          @click="range=r">{{ r }}</button>
      </div>
    </div>

    <!-- KPI 카드 -->
    <div class="kpi-row">
      <div class="kpi-card">
        <div class="kpi-label mono">AVG SCORE · {{ range }}</div>
        <div class="kpi-val" :style="{ color: scoreColor(avgScore) }">{{ avgScore }}</div>
        <div class="kpi-sub">{{ avgScore >= 70 ? 'DANGER 수준' : avgScore >= 40 ? 'CAUTION 수준' : '정상 범위' }}</div>
        <div class="kpi-bar-wrap">
          <div class="kpi-bar-fill" :style="{ width: avgScore+'%', background: scoreColor(avgScore) }" />
        </div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label mono">DANGER 발생 (≥70점)</div>
        <div class="kpi-val" style="color:var(--danger)">{{ dangerTotal }}</div>
        <div class="kpi-sub">차량×일 기준 {{ slicedDays.length }}일 누적</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label mono">휴식 누락 이벤트</div>
        <div class="kpi-val" style="color:var(--warn)">{{ missingTotal }}</div>
        <div class="kpi-sub">필요 시점 미실시 횟수</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label mono">총 운행 / 야간</div>
        <div class="kpi-val">{{ totalDriveH }}<span class="kpi-unit">h</span></div>
        <div class="kpi-sub">야간 {{ totalNightH }}h 포함</div>
      </div>
    </div>

    <!-- 일별 평균 점수 -->
    <div class="card chart-card">
      <div class="card-hdr">
        <div class="card-title">일별 평균 피로 점수</div>
        <span class="mono" style="font-size:11px;color:var(--text-4)">{{ slicedDays.length }}일 · 전 차량 평균</span>
      </div>
      <div class="bar-chart">
        <div v-for="d in slicedDays" :key="d.date" class="bar-col">
          <div class="bar-wrap">
            <div class="bar-fill"
              :style="{ height: (d.avgScore / maxScore * 100)+'%', background: scoreColor(d.avgScore) }"
              :title="d.avgScore+'점'" />
          </div>
          <div class="bar-label mono">{{ d.date }}</div>
        </div>
      </div>
      <div class="bar-legend">
        <span class="legend-dot" style="background:var(--ok)" /><span class="legend-txt">NORMAL</span>
        <span class="legend-dot" style="background:var(--warn)" /><span class="legend-txt">CAUTION</span>
        <span class="legend-dot" style="background:var(--danger)" /><span class="legend-txt">DANGER</span>
      </div>
    </div>

    <div class="two-col">
      <!-- 시나리오 분포 -->
      <div class="card dist-card">
        <div class="card-hdr">
          <div class="card-title">시나리오 분포</div>
          <span class="mono" style="font-size:11px;color:var(--text-4)">{{ scenarioTotal }}건</span>
        </div>
        <div class="dist-list">
          <div class="dist-item">
            <div class="dist-top">
              <span class="mono dist-lbl" style="color:var(--ok)">SCENARIO A</span>
              <span class="mono dist-pct">{{ scenarioA }} / {{ scenarioTotal }}</span>
            </div>
            <div class="dist-bar-wrap">
              <div class="dist-bar-fill" :style="{ width: (scenarioA/scenarioTotal*100)+'%', background:'var(--ok)' }" />
            </div>
            <div class="dist-note">정상 운행 · 피로 무위험</div>
          </div>
          <div class="dist-item">
            <div class="dist-top">
              <span class="mono dist-lbl" style="color:var(--warn)">SCENARIO B</span>
              <span class="mono dist-pct">{{ scenarioB }} / {{ scenarioTotal }}</span>
            </div>
            <div class="dist-bar-wrap">
              <div class="dist-bar-fill" :style="{ width: (scenarioB/scenarioTotal*100)+'%', background:'var(--warn)' }" />
            </div>
            <div class="dist-note">주의 수준 · 휴식 권고</div>
          </div>
          <div class="dist-item">
            <div class="dist-top">
              <span class="mono dist-lbl" style="color:var(--danger)">SCENARIO C</span>
              <span class="mono dist-pct">{{ scenarioC }} / {{ scenarioTotal }}</span>
            </div>
            <div class="dist-bar-wrap">
              <div class="dist-bar-fill" :style="{ width: (scenarioC/scenarioTotal*100)+'%', background:'var(--danger)' }" />
            </div>
            <div class="dist-note">위험 수준 · 전화 권고</div>
          </div>
        </div>
      </div>

      <!-- 휴식 4단계 분포 -->
      <div class="card dist-card">
        <div class="card-hdr">
          <div class="card-title">휴식 유형 분포</div>
          <span class="mono" style="font-size:11px;color:var(--text-4)">{{ restTotal }}건</span>
        </div>
        <div class="dist-list">
          <div class="dist-item">
            <div class="dist-top">
              <span class="mono dist-lbl" style="color:var(--ok)">SUFFICIENT</span>
              <span class="mono dist-pct">{{ restSuff }}건 · {{ restPct(restSuff) }}%</span>
            </div>
            <div class="dist-bar-wrap">
              <div class="dist-bar-fill" :style="{ width: restPct(restSuff)+'%', background:'var(--ok)' }" />
            </div>
            <div class="dist-note">충분 휴식 (≥30분) · -20점 보정</div>
          </div>
          <div class="dist-item">
            <div class="dist-top">
              <span class="mono dist-lbl" style="color:#5B8FA8">VALID</span>
              <span class="mono dist-pct">{{ restValid }}건 · {{ restPct(restValid) }}%</span>
            </div>
            <div class="dist-bar-wrap">
              <div class="dist-bar-fill" :style="{ width: restPct(restValid)+'%', background:'#5B8FA8' }" />
            </div>
            <div class="dist-note">유효 휴식 (15~30분) · -10점 보정</div>
          </div>
          <div class="dist-item">
            <div class="dist-top">
              <span class="mono dist-lbl" style="color:var(--warn)">INVALID</span>
              <span class="mono dist-pct">{{ restInvalid }}건 · {{ restPct(restInvalid) }}%</span>
            </div>
            <div class="dist-bar-wrap">
              <div class="dist-bar-fill" :style="{ width: restPct(restInvalid)+'%', background:'var(--warn)' }" />
            </div>
            <div class="dist-note">불충분 휴식 (&lt;15분) · 보정 없음</div>
          </div>
          <div class="dist-item">
            <div class="dist-top">
              <span class="mono dist-lbl" style="color:var(--danger)">MISSING</span>
              <span class="mono dist-pct">{{ restMiss }}건 · {{ restPct(restMiss) }}%</span>
            </div>
            <div class="dist-bar-wrap">
              <div class="dist-bar-fill" :style="{ width: restPct(restMiss)+'%', background:'var(--danger)' }" />
            </div>
            <div class="dist-note">휴식 누락 · +10~+25점 가산</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 시간대별 평균 점수 -->
    <div class="card chart-card">
      <div class="card-hdr">
        <div class="card-title">시간대별 평균 피로 점수</div>
        <span class="mono" style="font-size:11px;color:var(--text-4)">00:00 ~ 23:00 · 전 운행 평균</span>
      </div>
      <div class="hour-chart">
        <div v-for="(sc, h) in hourlyScores" :key="h" class="hour-col">
          <div class="hour-bar-wrap">
            <div class="hour-bar-fill"
              :style="{ height: (sc / maxHourly * 100)+'%', background: scoreColor(sc) }" />
          </div>
          <div class="hour-label mono">{{ String(h).padStart(2,'0') }}</div>
        </div>
      </div>
    </div>

    <!-- 운행 상태 분포 -->
    <div class="card">
      <div class="card-hdr">
        <div class="card-title">운행 상태 분포</div>
        <span class="mono" style="font-size:11px;color:var(--text-4)">최근 {{ driveLogs.length }}건 기준</span>
      </div>
      <div class="status-row">
        <div class="status-card">
          <div class="status-dot" style="background:var(--accent)" />
          <div class="mono status-lbl">RUNNING</div>
          <div class="status-num mono" style="color:var(--accent)">{{ statusRunning }}</div>
          <div class="status-bar-wrap">
            <div class="status-bar-fill" :style="{ width: (statusRunning/driveLogs.length*100)+'%', background:'var(--accent)' }" />
          </div>
        </div>
        <div class="status-card">
          <div class="status-dot" style="background:var(--ok)" />
          <div class="mono status-lbl">COMPLETED</div>
          <div class="status-num mono" style="color:var(--ok)">{{ statusCompleted }}</div>
          <div class="status-bar-wrap">
            <div class="status-bar-fill" :style="{ width: (statusCompleted/driveLogs.length*100)+'%', background:'var(--ok)' }" />
          </div>
        </div>
        <div class="status-card">
          <div class="status-dot" style="background:var(--warn)" />
          <div class="mono status-lbl">STOPPED</div>
          <div class="status-num mono" style="color:var(--warn)">{{ statusStopped }}</div>
          <div class="status-bar-wrap">
            <div class="status-bar-fill" :style="{ width: (statusStopped/driveLogs.length*100)+'%', background:'var(--warn)' }" />
          </div>
        </div>
      </div>
    </div>
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

/* KPI */
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

/* 차트 카드 */
.card-hdr   { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }
.card-title { font-size:14px; font-weight:700; color:var(--text-1); }
.chart-card { padding:20px; }

/* 일별 바차트 */
.bar-chart {
  display:flex; align-items:flex-end; gap:4px; height:120px; padding-bottom:24px;
  border-bottom:1px solid var(--line-1);
}
.bar-col   { flex:1; display:flex; flex-direction:column; align-items:center; height:100%; }
.bar-wrap  { flex:1; width:100%; display:flex; align-items:flex-end; }
.bar-fill  { width:100%; border-radius:2px 2px 0 0; transition:height .4s; min-height:2px; }
.bar-label { font-size:9px; color:var(--text-4); margin-top:5px; white-space:nowrap; }

.bar-legend { display:flex; align-items:center; gap:14px; margin-top:10px; }
.legend-dot { width:8px; height:8px; border-radius:50%; display:inline-block; }
.legend-txt { font-size:11px; color:var(--text-3); }

/* 2컬럼 */
.two-col { display:grid; grid-template-columns:1fr 1fr; gap:10px; }
.dist-card { padding:20px; }
.dist-list { display:flex; flex-direction:column; gap:16px; }
.dist-item { display:flex; flex-direction:column; gap:6px; }
.dist-top  { display:flex; align-items:center; justify-content:space-between; }
.dist-lbl  { font-size:11px; font-weight:700; letter-spacing:0.04em; }
.dist-pct  { font-size:11px; color:var(--text-3); }
.dist-bar-wrap { height:6px; background:var(--bg-3); border-radius:3px; overflow:hidden; }
.dist-bar-fill { height:100%; border-radius:3px; transition:width .4s; }
.dist-note { font-size:10.5px; color:var(--text-4); }

/* 시간대 */
.hour-chart {
  display:flex; align-items:flex-end; gap:2px; height:100px; padding-bottom:20px;
  border-bottom:1px solid var(--line-1);
}
.hour-col     { flex:1; display:flex; flex-direction:column; align-items:center; height:100%; }
.hour-bar-wrap { flex:1; width:100%; display:flex; align-items:flex-end; }
.hour-bar-fill { width:100%; border-radius:1px 1px 0 0; transition:height .4s; min-height:1px; }
.hour-label    { font-size:8px; color:var(--text-4); margin-top:4px; }

/* 상태 분포 */
.status-row { display:grid; grid-template-columns:repeat(3,1fr); gap:10px; }
.status-card {
  background:var(--bg-2); border:1px solid var(--line-1); border-radius:var(--r-md);
  padding:16px; display:flex; flex-direction:column; gap:6px;
}
.status-dot  { width:8px; height:8px; border-radius:50%; }
.status-lbl  { font-size:10px; letter-spacing:0.07em; color:var(--text-4); }
.status-num  { font-size:28px; font-weight:800; letter-spacing:-0.02em; }
.status-bar-wrap { height:4px; background:var(--bg-3); border-radius:2px; overflow:hidden; }
.status-bar-fill { height:100%; border-radius:2px; transition:width .4s; }
</style>
