<script setup>
import { ref, computed } from 'vue'
import AppIcon from '@/components/common/AppIcon.vue'
import { thresholds } from '@/data/mockData'

/* ───────── 임계값 맵 ───────── */
const T = {}
thresholds.forEach(t => {
  T[t.key] = t.value
  T[t.key + '_DELTA'] = t.delta
})

/* ───────── 파라미터 ───────── */
const startHour   = ref(4)
const durationMin = ref(360)
const nightMode   = ref(false)

const rests = ref([
  { atMin: 120, durMin: 20 },
  { atMin: 270, durMin: 35 },
])

const entries = ref([
  { atMin: 0,   kind: 'DEPARTURE',    plate: '경기 80바 1024', location: '한라물류 차고지', ocrConf: 0.99 },
  { atMin: 180, kind: 'HIGHWAY_CCTV', plate: '경기 80바 1024', location: '경부고속 안성IC', ocrConf: 0.95 },
  { atMin: 360, kind: 'ARRIVAL',      plate: '경기 80바 1024', location: '수원 물류센터',   ocrConf: 0.98 },
])

/* ───────── 시뮬레이션 엔진 ───────── */
const NIGHT_H = new Set([22, 23, 0, 1, 2, 3, 4, 5])
const isNight = h => NIGHT_H.has(h % 24)

const continuousSteps = [
  { min: T.CONTINUOUS_DRIVING_240, delta: T.CONTINUOUS_DRIVING_240_DELTA },
  { min: T.CONTINUOUS_DRIVING_180, delta: T.CONTINUOUS_DRIVING_180_DELTA },
  { min: T.CONTINUOUS_DRIVING_120, delta: T.CONTINUOUS_DRIVING_120_DELTA },
  { min: T.CONTINUOUS_DRIVING_90,  delta: T.CONTINUOUS_DRIVING_90_DELTA  },
]
const dailySteps = [
  { min: T.DAILY_DRIVING_600, delta: T.DAILY_DRIVING_600_DELTA },
  { min: T.DAILY_DRIVING_480, delta: T.DAILY_DRIVING_480_DELTA },
  { min: T.DAILY_DRIVING_360, delta: T.DAILY_DRIVING_360_DELTA },
]
const nightSteps = [
  { min: T.NIGHT_DRIVING_120, delta: T.NIGHT_DRIVING_120_DELTA },
  { min: T.NIGHT_DRIVING_60,  delta: T.NIGHT_DRIVING_60_DELTA  },
  { min: T.NIGHT_DRIVING_30,  delta: T.NIGHT_DRIVING_30_DELTA  },
]

const simTrace  = ref([])
const simEvents = ref([])

function runSim() {
  const restMap   = new Map()
  rests.value.forEach(r => { if (r.atMin > 0 && r.durMin > 0) restMap.set(r.atMin, r.durMin) })
  const entryMap  = new Map()
  entries.value.forEach(e => entryMap.set(e.atMin, e))

  let score = 0, contMin = 0, dailyMin = 0, nightMin = 0
  let inRest = false, restRemaining = 0
  const firedC = new Set(), firedD = new Set(), firedN = new Set()
  const trace  = [], events = []

  for (let m = 0; m <= durationMin.value; m++) {
    const hour = Math.floor((startHour.value * 60 + m) / 60) % 24

    // 번호판 이벤트
    if (entryMap.has(m)) {
      const e = entryMap.get(m)
      events.push({ t: m, label: `${e.location} · ${e.plate}`, kind: e.kind, delta: 0, conf: e.ocrConf })
    }

    // 휴식 시작
    if (!inRest && restMap.has(m)) {
      const dur = restMap.get(m)
      inRest = true; restRemaining = dur
      contMin = 0
      if (dur >= T.REST_SUFFICIENT_MINUTES) {
        const d = T.REST_CORRECTION_SUFFICIENT_SCORE_DELTA
        score = Math.max(0, score + d)
        events.push({ t: m, label: `충분 휴식 ${dur}분`, kind: 'rest_sufficient', delta: d })
      } else if (dur >= T.REST_VALID_MINUTES) {
        const d = T.REST_CORRECTION_VALID_SCORE_DELTA
        score = Math.max(0, score + d)
        events.push({ t: m, label: `유효 휴식 ${dur}분`, kind: 'rest_valid', delta: d })
      } else {
        events.push({ t: m, label: `무효 휴식 ${dur}분 (<15m)`, kind: 'rest_invalid', delta: 0 })
      }
    }

    if (inRest) {
      restRemaining--
      if (restRemaining <= 0) inRest = false
    } else {
      // 운행 중
      contMin++; dailyMin++
      if (isNight(hour)) nightMin++

      // 연속 운행 임계값
      for (const s of continuousSteps) {
        if (contMin >= s.min && !firedC.has(s.min)) {
          firedC.add(s.min); score += s.delta
          events.push({ t: m, label: `연속 ${s.min}분 초과`, kind: 'continuous', delta: s.delta })
          break
        }
      }
      // 일일 누적
      for (const s of dailySteps) {
        if (dailyMin >= s.min && !firedD.has(s.min)) {
          firedD.add(s.min); score += s.delta
          events.push({ t: m, label: `일일 누적 ${s.min}분 초과`, kind: 'daily', delta: s.delta })
          break
        }
      }
      // 야간
      if (nightMode.value || isNight(hour)) {
        for (const s of nightSteps) {
          if (nightMin >= s.min && !firedN.has(s.min)) {
            firedN.add(s.min); score += s.delta
            events.push({ t: m, label: `야간 누적 ${s.min}분 초과`, kind: 'night', delta: s.delta })
            break
          }
        }
      }
    }

    score = Math.max(0, Math.min(100, score))
    trace.push({ t: m, score, inRest })
  }

  simTrace.value  = trace
  simEvents.value = events
}

// 초기 실행
runSim()

/* ───────── 파생값 ───────── */
const maxScore  = computed(() => simTrace.value.reduce((m, p) => Math.max(m, p.score), 0))
const finalScore= computed(() => simTrace.value[simTrace.value.length - 1]?.score ?? 0)
const levelLabel= computed(() => finalScore.value >= 70 ? 'DANGER' : finalScore.value >= 40 ? 'CAUTION' : 'NORMAL')
const levelColor= computed(() => finalScore.value >= 70 ? 'var(--danger)' : finalScore.value >= 40 ? 'var(--warn)' : 'var(--ok)')
const peakColor = computed(() => maxScore.value >= 70 ? 'var(--danger)' : maxScore.value >= 40 ? 'var(--warn)' : 'var(--ok)')

// 차트용: 5분 단위 샘플링
const chartPoints = computed(() => {
  const step = 5
  return simTrace.value.filter(p => p.t % step === 0)
})
const chartMax = computed(() => Math.max(Math.max(...chartPoints.value.map(p => p.score)), 10))

function fmtTime(m) {
  const abs = startHour.value * 60 + m
  const h = Math.floor(abs / 60) % 24
  const mn = abs % 60
  return `${String(h).padStart(2,'0')}:${String(mn).padStart(2,'0')}`
}

function eventColor(kind) {
  if (kind === 'rest_sufficient') return 'var(--ok)'
  if (kind === 'rest_valid')      return '#5B8FA8'
  if (kind === 'rest_invalid')    return 'var(--warn)'
  if (kind === 'continuous')      return 'var(--danger)'
  if (kind === 'daily')           return 'var(--warn)'
  if (kind === 'night')           return '#8B7EBB'
  return 'var(--accent)'
}
function eventIcon(kind) {
  if (kind.startsWith('rest'))   return '◎'
  if (kind === 'continuous')     return '▲'
  if (kind === 'daily')          return '◇'
  if (kind === 'night')          return '☽'
  return '◉'
}
function deltaLabel(d) {
  if (!d) return ''
  return d > 0 ? `+${d}pt` : `${d}pt`
}

function addRest()  { rests.value.push({ atMin: 60, durMin: 20 }) }
function delRest(i) { rests.value.splice(i, 1) }
function addEntry() { entries.value.push({ atMin: 0, kind: 'HIGHWAY_CCTV', plate: '경기 80바 1024', location: '', ocrConf: 0.95 }) }
function delEntry(i){ entries.value.splice(i, 1) }
</script>

<template>
  <div class="view">
    <div class="breadcrumb mono">
      ADMIN / SIMULATION · FR-B01 · 피로 점수 누적 시뮬레이터
    </div>

    <div class="page-header">
      <div>
        <h2 class="page-title">운행 시뮬레이션</h2>
        <p class="page-sub">파라미터를 조정하고 실행하면 1분 단위로 피로 점수가 어떻게 누적되는지 확인할 수 있습니다.</p>
      </div>
      <button class="btn btn-primary run-btn" @click="runSim">
        <AppIcon name="refresh" :size="13" />시뮬레이션 실행
      </button>
    </div>

    <div class="main-grid">
      <!-- ── 파라미터 패널 ── -->
      <div class="param-col">
        <!-- 기본 설정 -->
        <div class="card param-card">
          <div class="param-title">기본 파라미터</div>

          <div class="param-row">
            <label class="param-lbl mono">START_HOUR</label>
            <div class="param-ctrl">
              <input v-model.number="startHour" type="range" min="0" max="23" class="slider" />
              <span class="mono param-val">{{ String(startHour).padStart(2,'0') }}:00</span>
            </div>
          </div>

          <div class="param-row">
            <label class="param-lbl mono">DURATION</label>
            <div class="param-ctrl">
              <input v-model.number="durationMin" type="range" min="30" max="720" step="10" class="slider" />
              <span class="mono param-val">
                {{ Math.floor(durationMin/60) > 0 ? Math.floor(durationMin/60)+'h ' : '' }}{{ durationMin%60 }}m
              </span>
            </div>
          </div>

          <div class="param-row">
            <label class="param-lbl mono">NIGHT_MODE</label>
            <label class="toggle">
              <input type="checkbox" v-model="nightMode" />
              <span class="toggle-track" /><span class="toggle-lbl mono">{{ nightMode ? 'ON' : 'OFF' }}</span>
            </label>
          </div>
        </div>

        <!-- 휴식 설정 -->
        <div class="card param-card">
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:12px;">
            <div class="param-title">휴식 이벤트 ({{ rests.length }}건)</div>
            <button class="add-btn mono" @click="addRest">+ 추가</button>
          </div>
          <div v-if="rests.length === 0" class="empty-hint">휴식 이벤트가 없습니다.</div>
          <div v-for="(r, i) in rests" :key="i" class="rest-param-item">
            <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:6px;">
              <span class="mono rest-idx">REST #{{ i+1 }}</span>
              <button class="del-btn" @click="delRest(i)">✕</button>
            </div>
            <div class="rest-inputs">
              <div class="input-group">
                <span class="input-lbl mono">AT (분)</span>
                <input v-model.number="r.atMin" type="number" min="0" :max="durationMin" class="num-inp mono" />
              </div>
              <div class="input-group">
                <span class="input-lbl mono">DURATION (분)</span>
                <input v-model.number="r.durMin" type="number" min="1" max="120" class="num-inp mono" />
              </div>
            </div>
            <div class="rest-type-hint mono"
              :style="{ color: r.durMin >= T.REST_SUFFICIENT_MINUTES ? 'var(--ok)' : r.durMin >= T.REST_VALID_MINUTES ? '#5B8FA8' : 'var(--warn)' }">
              {{ r.durMin >= T.REST_SUFFICIENT_MINUTES ? 'SUFFICIENT (-20pt)' : r.durMin >= T.REST_VALID_MINUTES ? 'VALID (-10pt)' : 'INVALID (보정없음)' }}
            </div>
          </div>
        </div>

        <!-- 번호판 인식 설정 -->
        <div class="card param-card">
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:12px;">
            <div class="param-title">번호판 인식 이벤트 ({{ entries.length }}건)</div>
            <button class="add-btn mono" @click="addEntry">+ 추가</button>
          </div>
          <div v-if="entries.length === 0" class="empty-hint">등록된 인식 이벤트가 없습니다.</div>
          <div v-for="(e, i) in entries" :key="i" class="entry-item">
            <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:6px;">
              <span class="mono rest-idx">ENTRY #{{ i+1 }}</span>
              <button class="del-btn" @click="delEntry(i)">✕</button>
            </div>
            <div class="entry-inputs">
              <div class="input-group">
                <span class="input-lbl mono">AT (분)</span>
                <input v-model.number="e.atMin" type="number" min="0" :max="durationMin" class="num-inp mono" />
              </div>
              <div class="input-group">
                <span class="input-lbl mono">KIND</span>
                <select v-model="e.kind" class="num-inp mono sel-inp">
                  <option>DEPARTURE</option>
                  <option>ARRIVAL</option>
                  <option>HIGHWAY_CCTV</option>
                  <option>REST_AREA_CCTV</option>
                </select>
              </div>
              <div class="input-group" style="grid-column:1/-1">
                <span class="input-lbl mono">LOCATION</span>
                <input v-model="e.location" type="text" class="num-inp mono loc-inp" placeholder="위치" />
              </div>
              <div class="input-group">
                <span class="input-lbl mono">OCR_CONF</span>
                <input v-model.number="e.ocrConf" type="number" min="0" max="1" step="0.01" class="num-inp mono" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ── 결과 패널 ── -->
      <div class="result-col">
        <!-- KPI 요약 -->
        <div class="kpi-strip">
          <div class="kpi-card">
            <div class="kpi-lbl mono">PEAK SCORE</div>
            <div class="kpi-num" :style="{ color: peakColor }">{{ maxScore }}</div>
          </div>
          <div class="kpi-card">
            <div class="kpi-lbl mono">FINAL SCORE</div>
            <div class="kpi-num" :style="{ color: levelColor }">{{ finalScore }}</div>
          </div>
          <div class="kpi-card">
            <div class="kpi-lbl mono">FINAL LEVEL</div>
            <div class="kpi-num" :style="{ color: levelColor }">{{ levelLabel }}</div>
          </div>
          <div class="kpi-card">
            <div class="kpi-lbl mono">EVENTS</div>
            <div class="kpi-num">{{ simEvents.length }}</div>
          </div>
        </div>

        <!-- 피로 점수 차트 -->
        <div class="card chart-card">
          <div class="chart-hdr">
            <div class="card-title">누적 피로 점수 추이</div>
            <span class="mono" style="font-size:11px;color:var(--text-4)">5분 단위 샘플 · {{ chartPoints.length }}점</span>
          </div>
          <div class="score-chart">
            <!-- Y 가이드라인 -->
            <div class="guide g-danger" />
            <div class="guide g-caution" />
            <!-- 바 -->
            <div v-for="p in chartPoints" :key="p.t" class="chart-col"
              :title="`${fmtTime(p.t)} · ${p.score}점`">
              <div class="chart-bar" :class="{ rest: p.inRest }"
                :style="{
                  height: (p.score / 100 * 100) + '%',
                  background: p.inRest ? 'var(--line-2)' :
                    p.score >= 70 ? 'var(--danger)' : p.score >= 40 ? 'var(--warn)' : 'var(--ok)'
                }" />
            </div>
          </div>
          <!-- Y축 레이블 -->
          <div class="chart-yaxis mono">
            <span style="color:var(--danger)">100</span>
            <span style="color:var(--danger)">70 ←DANGER</span>
            <span style="color:var(--warn)">40 ←CAUTION</span>
            <span>0</span>
          </div>
          <!-- X축 -->
          <div class="chart-xaxis">
            <span v-for="p in chartPoints.filter((_,i,a) => i % Math.max(1, Math.floor(a.length/8)) === 0)" :key="p.t"
              class="mono">{{ fmtTime(p.t) }}</span>
          </div>
        </div>

        <!-- 이벤트 로그 -->
        <div class="card event-card">
          <div class="chart-hdr">
            <div class="card-title">이벤트 로그</div>
            <span class="mono" style="font-size:11px;color:var(--text-4)">{{ simEvents.length }}건</span>
          </div>
          <div class="event-list">
            <div v-if="simEvents.length === 0" class="empty-hint">이벤트가 없습니다.</div>
            <div v-for="(ev, i) in simEvents" :key="i" class="event-row">
              <span class="ev-icon" :style="{ color: eventColor(ev.kind) }">{{ eventIcon(ev.kind) }}</span>
              <span class="mono ev-time">{{ fmtTime(ev.t) }}</span>
              <span class="ev-label">{{ ev.label }}</span>
              <span v-if="ev.delta" class="mono ev-delta"
                :style="{ color: ev.delta > 0 ? 'var(--danger)' : 'var(--ok)' }">
                {{ deltaLabel(ev.delta) }}
              </span>
              <span v-if="ev.conf" class="mono ev-conf">{{ Math.round(ev.conf*100) }}%</span>
            </div>
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
.page-sub    { font-size:12.5px; color:var(--text-3); margin:0; max-width:600px; }
.run-btn     { display:flex; align-items:center; gap:6px; font-size:13px; padding:10px 20px; flex-shrink:0; }

/* 메인 그리드 */
.main-grid { display:grid; grid-template-columns:300px 1fr; gap:16px; align-items:start; }

/* 파라미터 */
.param-col  { display:flex; flex-direction:column; gap:12px; position:sticky; top:24px; }
.param-card { padding:16px; display:flex; flex-direction:column; gap:14px; }
.param-title{ font-size:13px; font-weight:700; color:var(--text-1); }
.param-row  { display:flex; flex-direction:column; gap:6px; }
.param-lbl  { font-size:9.5px; letter-spacing:0.06em; color:var(--text-4); }
.param-ctrl { display:flex; align-items:center; gap:10px; }
.slider     { flex:1; accent-color:var(--accent); cursor:pointer; }
.param-val  { font-size:12px; font-weight:700; color:var(--text-1); min-width:52px; text-align:right; }

/* 토글 */
.toggle { display:flex; align-items:center; gap:8px; cursor:pointer; }
.toggle input { display:none; }
.toggle-track {
  width:36px; height:20px; border-radius:10px; background:var(--line-2);
  position:relative; transition:background .2s;
}
.toggle input:checked + .toggle-track { background:var(--accent); }
.toggle-track::after {
  content:''; position:absolute; top:3px; left:3px;
  width:14px; height:14px; border-radius:50%; background:#fff;
  transition:transform .2s;
}
.toggle input:checked + .toggle-track::after { transform:translateX(16px); }
.toggle-lbl { font-size:11px; color:var(--text-3); }

/* 휴식 파라미터 */
.add-btn {
  font-size:10px; padding:3px 9px; border-radius:var(--r-sm);
  background:var(--accent-soft); color:var(--accent); border:1px solid var(--accent-line);
  cursor:pointer; letter-spacing:0.03em;
}
.del-btn {
  font-size:10px; padding:1px 6px; border-radius:var(--r-sm);
  background:none; border:1px solid var(--line-2); color:var(--text-4); cursor:pointer;
}
.del-btn:hover { color:var(--danger); border-color:rgba(181,84,74,.4); }
.rest-param-item {
  padding:10px 12px; background:var(--bg-2); border:1px solid var(--line-1); border-radius:var(--r-md);
  margin-bottom:8px;
}
.rest-idx { font-size:10px; letter-spacing:0.05em; color:var(--text-3); }
.rest-inputs { display:grid; grid-template-columns:1fr 1fr; gap:8px; margin-bottom:6px; }
.rest-type-hint { font-size:10px; letter-spacing:0.04em; }
.entry-item { padding:10px 12px; background:var(--bg-2); border:1px solid var(--line-1); border-radius:var(--r-md); margin-bottom:8px; }
.entry-inputs { display:grid; grid-template-columns:1fr 1fr; gap:8px; }
.input-group { display:flex; flex-direction:column; gap:4px; }
.input-lbl   { font-size:9px; letter-spacing:0.05em; color:var(--text-4); }
.num-inp {
  padding:4px 7px; border:1px solid var(--line-2); border-radius:var(--r-sm);
  background:var(--bg-1); color:var(--text-1); font-size:12px; outline:none; transition:border-color .15s;
  width:100%; box-sizing:border-box;
}
.num-inp:focus { border-color:var(--accent-line); }
.sel-inp { cursor:pointer; }
.loc-inp  { width:100%; }
.empty-hint { font-size:11.5px; color:var(--text-4); padding:8px 0; }

/* 결과 */
.result-col { display:flex; flex-direction:column; gap:12px; }

.kpi-strip { display:grid; grid-template-columns:repeat(4,1fr); gap:10px; }
.kpi-card {
  background:var(--bg-2); border:1px solid var(--line-1); border-radius:var(--r-md);
  padding:14px 16px; display:flex; flex-direction:column; gap:4px;
}
.kpi-lbl { font-size:9.5px; letter-spacing:0.07em; color:var(--text-4); }
.kpi-num { font-size:26px; font-weight:800; color:var(--text-1); letter-spacing:-0.02em; }

/* 차트 */
.chart-card { padding:20px; }
.chart-hdr  { display:flex; align-items:center; justify-content:space-between; margin-bottom:14px; }
.card-title { font-size:14px; font-weight:700; color:var(--text-1); }

.score-chart {
  position:relative; height:160px;
  display:flex; align-items:flex-end; gap:1px;
  border-left:1px solid var(--line-2); border-bottom:1px solid var(--line-2);
  padding:8px 0 0; overflow:hidden;
}
.guide {
  position:absolute; left:0; right:0; height:1px;
}
.g-danger  { bottom:70%; background:rgba(181,84,74,.35); border-top:1px dashed rgba(181,84,74,.4); }
.g-caution { bottom:40%; background:rgba(197,138,58,.3); border-top:1px dashed rgba(197,138,58,.4); }

.chart-col { flex:1; height:100%; display:flex; align-items:flex-end; }
.chart-bar { width:100%; border-radius:1px 1px 0 0; transition:height .3s; min-height:1px; }
.chart-bar.rest { opacity:.5; }

.chart-yaxis {
  display:flex; flex-direction:column; justify-content:space-between;
  font-size:9px; color:var(--text-4); height:160px; margin-top:-160px;
  pointer-events:none; padding:0 4px;
}
.chart-xaxis {
  display:flex; justify-content:space-between;
  font-size:9px; color:var(--text-4); margin-top:4px;
}

/* 이벤트 로그 */
.event-card { padding:20px; }
.event-list { display:flex; flex-direction:column; gap:1px; }
.event-row {
  display:flex; align-items:center; gap:8px; padding:7px 10px;
  border-radius:var(--r-sm); font-size:12px;
}
.event-row:hover { background:var(--bg-2); }
.ev-icon  { font-size:10px; flex-shrink:0; }
.ev-time  { font-size:11px; color:var(--text-3); flex-shrink:0; min-width:42px; }
.ev-label { flex:1; color:var(--text-2); }
.ev-delta { font-size:11px; font-weight:700; flex-shrink:0; }
.ev-conf  { font-size:10.5px; color:var(--text-4); flex-shrink:0; }
</style>
