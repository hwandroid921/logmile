<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '@/components/common/AppIcon.vue'
import { dashboardApi } from '@/api/dashboardApi'

const router = useRouter()

/* ─── Korea map dot grid ─── */
const mapDots = Array.from({ length: 20 }, (_, r) =>
  Array.from({ length: 20 }, (_, c) => ({ cx: 30 + c * 30, cy: 20 + r * 28 }))
).flat()

/* ─── 시드 데이터 ─── */
const SEED_VEHICLES = [
  { id:1,  plate:'경기 80바 1024', driver:'김민수', type:'카고 5톤',    score:78, level:'DANGER',  status:'RUNNING', spd:87, contMin:384, dailyMin:672, nightMin:204, restValid:1, restSuff:0, restInvalid:1, restMiss:2, loc:'경부고속 · 안성IC',  startedAt:'03:18', scenario:'C', driveLogId:'DL-2026-0438', pos:{x:280,y:170} },
  { id:2,  plate:'경기 80바 1025', driver:'박정호', type:'윙바디 11톤', score:56, level:'CAUTION', status:'RUNNING', spd:78, contMin:270, dailyMin:480, nightMin:108, restValid:2, restSuff:0, restInvalid:0, restMiss:1, loc:'서해안 · 서산IC',    startedAt:'06:11', scenario:'B', driveLogId:'DL-2026-0437', pos:{x:212,y:218} },
  { id:3,  plate:'경기 80바 1026', driver:'이영준', type:'카고 4.5톤',  score:18, level:'NORMAL',  status:'RUNNING', spd:92, contMin:108, dailyMin:186, nightMin:0,   restValid:0, restSuff:1, restInvalid:0, restMiss:0, loc:'중부고속 · 음성IC',  startedAt:'11:02', scenario:'A', driveLogId:'DL-2026-0436', pos:{x:300,y:230} },
  { id:4,  plate:'경기 80바 1027', driver:'최성훈', type:'윙바디 8톤',  score:32, level:'NORMAL',  status:'RUNNING', spd:81, contMin:180, dailyMin:342, nightMin:30,  restValid:1, restSuff:1, restInvalid:0, restMiss:0, loc:'영동고속 · 여주IC',  startedAt:'08:50', scenario:'A', driveLogId:'DL-2026-0435', pos:{x:328,y:162} },
  { id:5,  plate:'경기 80바 1028', driver:'정우석', type:'카고 5톤',    score:12, level:'NORMAL',  status:'RUNNING', spd:76, contMin:60,  dailyMin:114, nightMin:0,   restValid:0, restSuff:0, restInvalid:0, restMiss:0, loc:'남해고속 · 진주IC',  startedAt:'12:39', scenario:'A', driveLogId:'DL-2026-0434', pos:{x:354,y:352} },
  { id:6,  plate:'경기 80바 1029', driver:'강지훈', type:'카고 5톤',    score:84, level:'DANGER',  status:'RUNNING', spd:64, contMin:342, dailyMin:618, nightMin:168, restValid:1, restSuff:0, restInvalid:1, restMiss:2, loc:'중부내륙 · 점촌IC',  startedAt:'04:02', scenario:'C', driveLogId:'DL-2026-0433', pos:{x:340,y:268} },
  { id:7,  plate:'경기 80바 1030', driver:'한승연', type:'카고 2.5톤',  score:48, level:'CAUTION', status:'RUNNING', spd:71, contMin:228, dailyMin:384, nightMin:72,  restValid:1, restSuff:0, restInvalid:0, restMiss:1, loc:'서울외곽 · 송내IC',  startedAt:'07:45', scenario:'B', driveLogId:'DL-2026-0432', pos:{x:248,y:124} },
  { id:8,  plate:'경기 80바 1031', driver:'조영민', type:'윙바디 11톤', score:41, level:'CAUTION', status:'RUNNING', spd:84, contMin:252, dailyMin:426, nightMin:48,  restValid:1, restSuff:0, restInvalid:0, restMiss:1, loc:'서해안 · 당진IC',    startedAt:'06:50', scenario:'B', driveLogId:'DL-2026-0431', pos:{x:228,y:170} },
  { id:9,  plate:'경기 80바 1032', driver:'윤태경', type:'카고 5톤',    score:0,  level:'NORMAL',  status:'IDLE',    spd:0,  contMin:0,   dailyMin:0,   nightMin:0,   restValid:0, restSuff:0, restInvalid:0, restMiss:0, loc:'한라물류 · 차고지',  startedAt:'—',     scenario:'—', driveLogId:null, pos:{x:252,y:98} },
  { id:10, plate:'경기 80바 1033', driver:'서동현', type:'윙바디 8톤',  score:0,  level:'NORMAL',  status:'IDLE',    spd:0,  contMin:0,   dailyMin:0,   nightMin:0,   restValid:0, restSuff:0, restInvalid:0, restMiss:0, loc:'한라물류 · 차고지',  startedAt:'—',     scenario:'—', driveLogId:null, pos:{x:252,y:98} },
]

/* ─── 헬퍼 ─── */
function fbColor(val, thr) {
  if (thr[1] != null && val > thr[1]) return 'var(--danger)'
  if (thr[0] != null && val > thr[0]) return 'var(--warn)'
  return 'var(--ok)'
}
function fbPct(val, max) { return Math.min(100, (val / max) * 100) }
function fmtHM(mins) {
  if (!mins) return '—'
  const h = Math.floor(mins / 60), m = mins % 60
  return h > 0 ? `${h}h ${m}m` : `${m}m`
}
function levelLabel(l)    { return l === 'DANGER' ? '위험' : l === 'CAUTION' ? '주의' : '정상' }
function levelChipCls(l)  { return l === 'DANGER' ? 'chip chip-danger' : l === 'CAUTION' ? 'chip chip-warn' : 'chip chip-ok' }
function scColor(c)       { return c === 'C' ? 'var(--danger)' : c === 'B' ? 'var(--warn)' : 'var(--ok)' }
function statusChipCls(s) { return s === 'RUNNING' ? 'chip chip-info' : s === 'COMPLETED' ? 'chip chip-ok' : 'chip chip-mute' }
function statusLabel(s)   { return s === 'RUNNING' ? '운행중' : s === 'COMPLETED' ? '완료' : '대기' }
function tlEventColor(e)  { return e === '위험 진입' ? 'var(--danger)' : e === '주의 진입' ? 'var(--warn)' : 'var(--accent)' }
function evColor(k)       { return k === 'danger' ? 'var(--danger)' : k === 'warn' ? 'var(--warn)' : 'var(--accent)' }

/* ─── State ─── */
const now        = ref(new Date().toLocaleString('ko-KR', { hour12: false }))
const mapTab     = ref('전체')
const mapTabs    = ['전체', '위험', '주의', '정상']
const vehicles   = ref(SEED_VEHICLES)
const selectedId = ref(SEED_VEHICLES[0].id)
const hoveredId  = ref(null)

async function refresh() {
  now.value = new Date().toLocaleString('ko-KR', { hour12: false })
  await fetchData()
}

/* ─── API 로드 ─── */
async function fetchData() {
  try {
    const [, vRes] = await Promise.all([dashboardApi.getSummary(), dashboardApi.getVehicles()])
    vehicles.value = SEED_VEHICLES.map(s => {
      const a = vRes.data.find(v => v.plateNo === s.plate)
      if (!a) return s
      return {
        ...s,
        score:      a.fatigueScore ?? s.score,
        level:      a.fatigueLevel ?? s.level,
        driveLogId: a.driveLogId   ?? s.driveLogId,
        startedAt:  a.startedAt ? String(a.startedAt).slice(11, 16) : s.startedAt,
      }
    })
  } catch { /* seed 데이터 유지 */ }
}

let timer = null
onMounted(() => { fetchData(); timer = setInterval(fetchData, 5000) })
onUnmounted(() => clearInterval(timer))

/* ─── 기본 파생값 ─── */
const selected         = computed(() => vehicles.value.find(v => v.id === selectedId.value) || null)
const runningVehicles  = computed(() => vehicles.value.filter(v => v.status === 'RUNNING'))
const filteredVehicles = computed(() => {
  const r = runningVehicles.value
  if (mapTab.value === '전체') return r
  const need = mapTab.value === '위험' ? 'DANGER' : mapTab.value === '주의' ? 'CAUTION' : 'NORMAL'
  return r.filter(v => v.level === need)
})
const sortedFiltered = computed(() => filteredVehicles.value.slice().sort((a, b) => b.score - a.score))

const runningCount = computed(() => runningVehicles.value.length)
const dangerCount  = computed(() => runningVehicles.value.filter(v => v.level === 'DANGER').length)
const cautionCount = computed(() => runningVehicles.value.filter(v => v.level === 'CAUTION').length)
const normalCount  = computed(() => runningVehicles.value.filter(v => v.level === 'NORMAL').length)
const idleCount    = computed(() => vehicles.value.length - runningCount.value)

const scores   = computed(() => runningVehicles.value.map(v => v.score))
const avgScore = computed(() => scores.value.length ? scores.value.reduce((a, b) => a + b, 0) / scores.value.length : 0)
const stdev    = computed(() => {
  const avg = avgScore.value
  return scores.value.length ? Math.sqrt(scores.value.reduce((s, x) => s + (x - avg) ** 2, 0) / scores.value.length) : 0
})

/* ─── KPI Tile 1: DistChart ─── */
const kpiBuckets = computed(() => {
  const arr = new Array(14).fill(0)
  scores.value.forEach(s => { arr[Math.min(13, Math.floor(s / (100 / 14)))]++ })
  return arr
})
const maxBucket = computed(() => Math.max(...kpiBuckets.value, 1))
const distBars  = computed(() => {
  const w = 200, h = 36
  return kpiBuckets.value.map((b, i) => ({
    x: (i / 14) * w + 1,
    y: h - Math.max(2, (b / maxBucket.value) * (h - 4)),
    w: (w / 14) - 2,
    h: Math.max(2, (b / maxBucket.value) * (h - 4)),
  }))
})
const meanX = computed(() => (avgScore.value / 100) * 200)

/* ─── KPI Tile 2: MAX CONT DRIVE ─── */
const maxCont      = computed(() => runningVehicles.value.reduce((m, v) => Math.max(m, v.contMin || 0), 0))
const maxContH     = computed(() => Math.floor(maxCont.value / 60))
const maxContM     = computed(() => maxCont.value % 60)
const contGaugePct = computed(() => Math.min(100, (maxCont.value / 300) * 100))
const contColor    = computed(() => maxCont.value >= 180 ? 'var(--danger)' : maxCont.value >= 90 ? 'var(--warn)' : 'var(--ok)')
const contDelta    = computed(() => maxCont.value >= 180 ? 45 : maxCont.value >= 120 ? 25 : 10)

/* ─── KPI Tile 3: DANGER weekly bars ─── */
const weeklyBase   = [1, 2, 0, 2, 3, 1]
const weeklyFull   = computed(() => [...weeklyBase, dangerCount.value])
const weeklyMax    = computed(() => Math.max(...weeklyFull.value, 1))
const weeklyMean   = 1.43
const weeklyBars   = computed(() => {
  const h = 36
  return weeklyFull.value.map((d, i) => {
    const bh = (d / weeklyMax.value) * (h - 4)
    return { x: i * (200 / 7) + 1, y: h - bh, w: (200 / 7) - 4, h: bh, today: i === 6 }
  })
})
const weeklyMeanY  = computed(() => 36 - (weeklyMean / weeklyMax.value) * (36 - 4))

/* ─── KPI Tile 5: Fleet Health donut (compact) ─── */
const DS = 78, DT = 11
const DR_K = (DS - DT) / 2
const DC_K = 2 * Math.PI * DR_K
const donutArcsK = computed(() => {
  const total = runningCount.value || 1
  const segs = [
    { value: normalCount.value,  color: '#5E8A6F' },
    { value: cautionCount.value, color: '#C58A3A' },
    { value: dangerCount.value,  color: '#B5544A' },
  ]
  let acc = 0
  return segs.map(s => {
    const frac = s.value / total
    const dash = `${frac * DC_K} ${DC_K}`
    const off  = -acc * DC_K
    acc += frac
    return { ...s, dash, off }
  })
})
const avgColor = computed(() => avgScore.value >= 70 ? 'var(--danger)' : avgScore.value >= 40 ? 'var(--warn)' : 'var(--ok)')

/* ─── Fleet map tab counts ─── */
function tabCount(t) {
  if (t === '전체') return runningCount.value
  if (t === '위험')  return dangerCount.value
  if (t === '주의')  return cautionCount.value
  return normalCount.value
}

/* ─── VehicleRow helpers ─── */
const vFactors = [
  { key: 'CONT',  max: 300, thr: [90, 180] },
  { key: 'DAILY', max: 720, thr: [360, 600] },
  { key: 'NIGHT', max: 240, thr: [30, 120] },
]
function factorVal(v, key) {
  return key === 'CONT' ? (v.contMin || 0) : key === 'DAILY' ? (v.dailyMin || 0) : (v.nightMin || 0)
}
function vSparkPts(v) {
  const pts = []
  for (let i = 0; i < 12; i++) {
    const s = Math.max(0, Math.min(100, 5 + (v.score - 5) * (i / 11) + Math.sin(i * 0.7) * 3))
    pts.push({ x: (i / 11) * 86, y: 22 - (s / 100) * 18 })
  }
  return pts.map(p => `${p.x},${p.y}`).join(' ')
}
function vSparkColor(v) {
  return v.level === 'DANGER' ? 'var(--danger)' : v.level === 'CAUTION' ? 'var(--warn)' : 'var(--ok)'
}
function vSparkEnd(v) {
  const s = Math.max(0, Math.min(100, 5 + (v.score - 5) + Math.sin(11 * 0.7) * 3))
  return { cx: 86, cy: 22 - (s / 100) * 18 }
}
function vDelta(v) {
  const d = Math.round(v.score - 5)
  if (d > 0) return { txt: `▲ +${d}`, color: 'var(--danger)' }
  if (d < 0) return { txt: `▼ ${d}`,  color: 'var(--ok)' }
  return { txt: '→ 0', color: 'var(--text-4)' }
}
function mapVColor(v) {
  return v.level === 'DANGER' ? 'var(--danger)' : v.level === 'CAUTION' ? 'var(--warn)' : 'var(--accent)'
}

/* ─── EventStream ─── */
const liveEvents = computed(() =>
  runningVehicles.value
    .filter(v => v.level === 'DANGER' || v.level === 'CAUTION')
    .slice(0, 4)
    .map((v, i) => ({
      t: ['14:32','14:18','14:09','13:47'][i] ?? '13:21',
      kind: v.level === 'DANGER' ? 'danger' : 'warn',
      plate: v.plate, id: v.id,
      text: v.level === 'DANGER'
        ? `연속 ${((v.contMin||0)/60).toFixed(1)}h · 야간 ${((v.nightMin||0)/60).toFixed(1)}h · 점수 ${v.score} → DANGER`
        : `연속 ${((v.contMin||0)/60).toFixed(1)}h · 점수 ${v.score} → CAUTION 유지`,
    }))
)
const seedEvents = [
  { t:'12:55', kind:'info', plate:'경기 80바 1026', text:'충분 휴식(34분) · -20점 보정 · 점수 18 (NORMAL)' },
  { t:'12:14', kind:'info', plate:'경기 80바 1028', text:'DEPARTURE · OCR 0.99 · 차고지 출발' },
  { t:'11:42', kind:'info', plate:'경기 80바 1027', text:'연속 3h 도달 · CAUTION 근접' },
  { t:'10:33', kind:'info', plate:'경기 80바 1025', text:'REST_AREA_CCTV · 입장 휴게소 진입' },
  { t:'09:18', kind:'info', plate:'경기 80바 1029', text:'HIGHWAY_CCTV · 중부내륙 점촌 상행 · OCR 0.94' },
  { t:'08:50', kind:'info', plate:'경기 80바 1027', text:'DEPARTURE · 한라물류 출발 · 시나리오 A 시작' },
]
const allEvents = computed(() => [...liveEvents.value, ...seedEvents])
function eventVehicleId(plate) { return vehicles.value.find(v => v.plate === plate)?.id ?? null }

/* ─── AlertList ─── */
const alertList = computed(() =>
  runningVehicles.value
    .filter(v => v.level === 'DANGER' || v.level === 'CAUTION')
    .sort((a, b) => b.score - a.score)
    .map(v => ({
      id: v.id, plate: v.plate,
      sev: v.level === 'DANGER' ? 'danger' : 'warn',
      t: new Date().toTimeString().slice(0, 5),
      desc: `피로 점수 ${v.score}점 · ${v.level === 'DANGER' ? '위험 (전화 권고)' : '주의'} · 연속 ${Math.floor((v.contMin||0)/60)}h ${(v.contMin||0)%60}m`,
      action: v.level === 'DANGER' ? '즉시 전화 권고 필요' : '휴식 안내 권고',
    }))
)

/* ─── VehicleDrilldown ─── */
const timelinePoints = computed(() => {
  const v = selected.value
  if (!v) return []
  const pts = []
  for (let m = 0; m <= 720; m += 30) {
    const progress = Math.min(1, m / 600)
    let s = 5 + (v.score - 5) * progress
    s = Math.max(2, Math.min(98, s + Math.sin(m / 45) * 3))
    let event = null
    if (m === 0) event = '운행 시작'
    else if (m === 90)  event = '90분'
    else if (m === 240) event = '4h'
    else if (v.score >= 40 && Math.abs(s - 40) < 3 && !pts.some(p => p.event === '주의 진입')) event = '주의 진입'
    else if (v.score >= 70 && Math.abs(s - 70) < 3 && !pts.some(p => p.event === '위험 진입')) event = '위험 진입'
    pts.push({ t: m, score: Math.round(s), event })
  }
  return pts
})
const peakScore  = computed(() => Math.max(...timelinePoints.value.map(p => p.score), 0))
const tlPx = t  => 40 + (t / 720) * 740
const tlPy = s  => 20 + (1 - s / 100) * 180
const tlScorePts  = computed(() => timelinePoints.value.map(p => `${tlPx(p.t)},${tlPy(p.score)}`).join(' '))
const tlScoreArea = computed(() => {
  const pts = timelinePoints.value
  if (!pts.length) return ''
  return `${tlPx(0)},200 ${tlScorePts.value} ${tlPx(720)},200`
})
const tlEvents = computed(() => timelinePoints.value.filter(p => p.event))

const drillFactors = computed(() => {
  const v = selected.value
  if (!v) return []
  return [
    { label:'연속 운행',        val:(v.contMin||0)/60,  unit:'h', max:5,  thr:[1.5,3],  cLbl:'90분 +10', dLbl:'180분 +45' },
    { label:'일일 누적',        val:(v.dailyMin||0)/60, unit:'h', max:12, thr:[6,10],   cLbl:'6h +15',   dLbl:'10h +45' },
    { label:'야간 (22~06시)',   val:(v.nightMin||0)/60, unit:'h', max:4,  thr:[0.5,2],  cLbl:'30분 +10', dLbl:'2h +35' },
  ]
})
const restEvents = computed(() => {
  const v = selected.value
  if (!v) return []
  return [
    { label:'VALID',   count:v.restValid||0,   color:'var(--ok)',     hint:'15m+ · -10' },
    { label:'SUFF.',   count:v.restSuff||0,    color:'var(--accent)', hint:'30m+ · -20' },
    { label:'INVALID', count:v.restInvalid||0, color:'var(--text-4)', hint:'<15m · ±0' },
    { label:'MISSED',  count:v.restMiss||0,    color:'var(--danger)', hint:'2h+ · +10' },
  ]
})

/* ─── Heatmap ─── */
const heatmapDrivers = computed(() => {
  const cur = new Date().getHours()
  return runningVehicles.value.slice(0, 8).map(v => {
    const start = parseInt((v.startedAt || '06').split(':')[0], 10) || 6
    const arr = new Array(24).fill(null)
    let acc = 5
    for (let h = 0; h < 24; h++) {
      if (h < start || h > cur) continue
      acc += (h >= 22 || h <= 5) ? 8 : 5
      arr[h] = Math.min(v.score, Math.round(acc))
    }
    return { name: v.driver, plate: v.plate, hours: arr }
  })
})
function heatColor(s) {
  if (s === null || s === undefined) return 'var(--bg-2)'
  if (s >= 70) return '#B5544A'
  if (s >= 55) return '#C58A3A'
  if (s >= 40) return '#C5A56A'
  if (s >= 20) return '#92A08F'
  return '#5E8A6F'
}

/* ─── Ranking ─── */
const rankingItems = computed(() =>
  runningVehicles.value.slice().sort((a, b) => b.score - a.score).slice(0, 6).map(v => ({
    id: v.id, label: v.driver, sub: v.plate, value: v.score,
    color: v.level === 'DANGER' ? 'var(--danger)' : v.level === 'CAUTION' ? 'var(--warn)' : 'var(--ok)',
    tag:   v.level === 'DANGER' ? '위험' : v.level === 'CAUTION' ? '주의' : '정상',
  }))
)
</script>

<template>
  <div class="fade-up view">

    <!-- ── 페이지 헤더 ── -->
    <div style="display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:18px;gap:16px;flex-wrap:wrap;">
      <div>
        <div style="font:600 10.5px/1.4 var(--font-mono);letter-spacing:0.14em;text-transform:uppercase;color:var(--accent);margin-bottom:6px;">OPERATIONS / LIVE</div>
        <h1 style="font:800 24px/1.25 var(--font-sans);letter-spacing:-0.02em;color:var(--text-1);margin:0;">관제 대시보드</h1>
        <div style="font-size:12px;color:var(--text-3);margin-top:3px;">오늘 · 2026.05.04 (월) · 한라물류센터 · 가상 시뮬레이션 환경</div>
      </div>
      <div style="display:flex;align-items:center;gap:10px;">
        <span style="font-family:var(--font-mono);font-size:11.5px;color:var(--text-3);display:flex;align-items:center;gap:6px;">
          <span class="dot dot-ok blink"/>connected · {{ now }}
        </span>
        <div style="width:1px;height:18px;background:var(--line-2);"/>
        <button class="btn btn-ghost"><AppIcon name="filter" :size="13"/> 필터</button>
        <button class="btn btn-ghost"><AppIcon name="download" :size="13"/> CSV</button>
        <button class="btn btn-primary" @click="refresh"><AppIcon name="refresh" :size="13"/> 새로고침</button>
      </div>
    </div>

    <!-- ── KPI Strip (5 tiles) ── -->
    <div class="kpi-strip" style="margin-bottom:16px;">

      <!-- Tile 1: AVG FATIGUE -->
      <div class="kpi-tile" style="border-top:2px solid var(--accent);">
        <div class="tile-hdr">
          <span class="tile-label" style="color:var(--accent);">AVG FATIGUE</span>
          <span class="tile-meta">n={{ runningCount }} · σ={{ stdev.toFixed(1) }}</span>
        </div>
        <div class="tile-val">
          <span style="font:700 30px/1 var(--font-mono);letter-spacing:-0.02em;color:var(--text-1);font-variant-numeric:tabular-nums;">{{ avgScore.toFixed(1) }}</span>
          <span style="font-family:var(--font-mono);font-size:10.5px;color:var(--text-4);">/100</span>
          <span class="tile-delta bad">▲ +3.4 (vs 24h)</span>
        </div>
        <div style="margin-top:10px;">
          <svg viewBox="0 0 200 36" preserveAspectRatio="none" style="width:100%;height:34px;display:block;overflow:visible;">
            <rect x="0" y="0" :width="200*0.4" height="36" fill="rgba(94,138,111,0.10)"/>
            <rect :x="200*0.4" y="0" :width="200*0.3" height="36" fill="rgba(197,138,58,0.10)"/>
            <rect :x="200*0.7" y="0" :width="200*0.3" height="36" fill="rgba(181,84,74,0.10)"/>
            <rect v-for="(b,i) in distBars" :key="i" :x="b.x" :y="b.y" :width="b.w" :height="b.h" fill="var(--text-3)"/>
            <line :x1="meanX" y1="0" :x2="meanX" y2="36" stroke="var(--accent)" stroke-width="1.6"/>
            <polygon :points="`${meanX},-1 ${meanX-4},-5 ${meanX+4},-5`" fill="var(--accent)"/>
          </svg>
          <div style="display:flex;justify-content:space-between;font-family:var(--font-mono);font-size:9px;color:var(--text-4);margin-top:3px;">
            <span>0</span><span style="color:var(--warn);">40</span><span style="color:var(--danger);">70</span><span>100</span>
          </div>
        </div>
      </div>

      <!-- Tile 2: MAX CONT DRIVE -->
      <div class="kpi-tile">
        <div class="tile-hdr">
          <span class="tile-label">MAX CONT. DRIVE</span>
          <span class="tile-meta">limit 240m</span>
        </div>
        <div class="tile-val">
          <span :style="`font:700 30px/1 var(--font-mono);letter-spacing:-0.02em;color:${contColor};font-variant-numeric:tabular-nums;`">{{ maxContH }}:{{ String(maxContM).padStart(2,'0') }}</span>
          <span style="font-family:var(--font-mono);font-size:10.5px;color:var(--text-4);">h:m</span>
          <span class="tile-delta bad">+{{ contDelta }}점</span>
        </div>
        <div style="margin-top:10px;">
          <div style="position:relative;height:12px;background:var(--bg-3);border-radius:6px;overflow:hidden;">
            <div :style="`position:absolute;top:0;left:0;bottom:0;width:${contGaugePct}%;background:linear-gradient(90deg,var(--ok),var(--warn) 38%,var(--danger) 75%);`"/>
            <div style="position:absolute;top:-2px;bottom:-2px;left:30%;width:1.5px;background:var(--warn);"/>
            <div style="position:absolute;top:-2px;bottom:-2px;left:60%;width:1.5px;background:var(--danger);"/>
            <div style="position:absolute;top:-2px;bottom:-2px;left:80%;width:1.5px;background:var(--danger);"/>
            <div :style="`position:absolute;top:-3px;bottom:-3px;left:calc(${contGaugePct}% - 1px);width:2px;background:var(--text-1);`"/>
          </div>
          <div style="display:flex;justify-content:space-between;font-family:var(--font-mono);font-size:9px;color:var(--text-4);margin-top:3px;">
            <span>0</span><span style="color:var(--warn);">90m</span><span style="color:var(--danger);">180m</span><span style="color:var(--danger);">240m</span>
          </div>
        </div>
      </div>

      <!-- Tile 3: DANGER UNITS -->
      <div class="kpi-tile">
        <div class="tile-hdr">
          <span class="tile-label" style="color:var(--danger);">DANGER UNITS</span>
          <span class="tile-meta">7d μ=1.43</span>
        </div>
        <div class="tile-val">
          <span style="font:700 30px/1 var(--font-mono);letter-spacing:-0.02em;color:var(--danger);font-variant-numeric:tabular-nums;">{{ dangerCount }}</span>
          <span style="font-family:var(--font-mono);font-size:10.5px;color:var(--text-4);">/ {{ vehicles.length }}</span>
          <span class="tile-delta bad">▲ +1 (24h)</span>
        </div>
        <div style="margin-top:10px;">
          <svg viewBox="0 0 200 36" preserveAspectRatio="none" style="width:100%;height:34px;display:block;">
            <rect v-for="(b,i) in weeklyBars" :key="i" :x="b.x" :y="b.y" :width="b.w" :height="b.h"
              fill="var(--danger)" :opacity="b.today ? 1 : 0.42"/>
            <line x1="0" :y1="weeklyMeanY" x2="200" :y2="weeklyMeanY" stroke="var(--text-4)" stroke-dasharray="2 3" stroke-width="0.8"/>
            <text x="198" :y="weeklyMeanY-2" text-anchor="end" font-family="var(--font-mono)" font-size="7" fill="var(--text-4)">μ=1.43</text>
          </svg>
          <div style="display:flex;justify-content:space-between;font-family:var(--font-mono);font-size:8.5px;color:var(--text-4);margin-top:2px;">
            <span v-for="(d,i) in ['월','화','수','목','금','토','오늘']" :key="i"
              :style="i===6 ? 'color:var(--text-2);font-weight:700;' : ''">{{ d }}</span>
          </div>
        </div>
      </div>

      <!-- Tile 4: FLEET COMPOSITION -->
      <div class="kpi-tile">
        <div class="tile-hdr">
          <span class="tile-label">FLEET COMPOSITION</span>
          <span class="tile-meta">{{ runningCount }}/{{ vehicles.length }} run</span>
        </div>
        <div class="tile-val">
          <span style="font:700 30px/1 var(--font-mono);letter-spacing:-0.02em;color:var(--text-1);font-variant-numeric:tabular-nums;">{{ runningCount ? ((normalCount/runningCount)*100).toFixed(1) : '0.0' }}</span>
          <span style="font-family:var(--font-mono);font-size:10.5px;color:var(--text-4);">% NORMAL</span>
          <span class="tile-delta good">정상 비중</span>
        </div>
        <div style="margin-top:10px;">
          <div style="height:12px;border-radius:6px;overflow:hidden;display:flex;background:var(--bg-3);">
            <div :style="`width:${runningCount?(normalCount/vehicles.length*100):0}%;background:var(--ok);`"/>
            <div :style="`width:${runningCount?(cautionCount/vehicles.length*100):0}%;background:var(--warn);`"/>
            <div :style="`width:${runningCount?(dangerCount/vehicles.length*100):0}%;background:var(--danger);`"/>
            <div :style="`width:${(idleCount/vehicles.length*100)}%;background:var(--text-4);opacity:0.35;`"/>
          </div>
          <div style="display:flex;justify-content:space-between;margin-top:5px;font-family:var(--font-mono);font-size:9px;">
            <span style="color:var(--ok);">정상 {{ normalCount }}</span>
            <span style="color:var(--warn);">주의 {{ cautionCount }}</span>
            <span style="color:var(--danger);">위험 {{ dangerCount }}</span>
            <span style="color:var(--text-4);">대기 {{ idleCount }}</span>
          </div>
        </div>
      </div>

      <!-- Tile 5: FLEET HEALTH donut -->
      <div class="kpi-tile" style="border-right:none;">
        <div class="tile-hdr">
          <span class="tile-label" style="color:var(--accent);">FLEET HEALTH</span>
          <span style="display:inline-flex;align-items:center;gap:4px;font-family:var(--font-mono);font-size:9px;color:var(--text-4);">
            <span class="dot dot-brand blink"/>LIVE
          </span>
        </div>
        <div style="display:flex;align-items:center;gap:10px;margin-top:10px;">
          <div style="position:relative;width:78px;height:78px;flex-shrink:0;">
            <svg :width="DS" :height="DS" :viewBox="`0 0 ${DS} ${DS}`">
              <circle :cx="DS/2" :cy="DS/2" :r="DR_K" fill="none" stroke="var(--bg-3)" :stroke-width="DT"/>
              <g :transform="`rotate(-90 ${DS/2} ${DS/2})`">
                <circle v-for="(a,i) in donutArcsK" :key="i" :cx="DS/2" :cy="DS/2" :r="DR_K" fill="none"
                  :stroke="a.color" :stroke-width="DT" :stroke-dasharray="a.dash" :stroke-dashoffset="a.off"/>
              </g>
            </svg>
            <div style="position:absolute;inset:0;display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center;">
              <span :style="`font-family:var(--font-mono);font-size:18px;font-weight:800;color:${avgColor};line-height:1;letter-spacing:-0.01em;`">{{ avgScore.toFixed(1) }}</span>
              <span style="font-family:var(--font-mono);font-size:8px;color:var(--text-4);letter-spacing:0.1em;margin-top:2px;">AVG /100</span>
            </div>
          </div>
          <div style="display:flex;flex-direction:column;gap:4px;flex:1;min-width:0;">
            <div v-for="r in [{label:'정상',value:normalCount,color:'#5E8A6F'},{label:'주의',value:cautionCount,color:'#C58A3A'},{label:'위험',value:dangerCount,color:'#B5544A'}]"
              :key="r.label" style="display:grid;grid-template-columns:10px 1fr auto;gap:6px;align-items:center;">
              <span :style="`width:10px;height:10px;border-radius:2px;background:${r.color};display:block;`"/>
              <span style="font-size:11px;color:var(--text-2);">{{ r.label }}</span>
              <span :style="`font-family:var(--font-mono);font-size:13px;font-weight:800;color:${r.color};letter-spacing:-0.01em;`">{{ r.value }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Hero: FleetMap + EventStream/Alerts ── -->
    <div style="display:grid;grid-template-columns:minmax(0,1.65fr) minmax(0,1fr);gap:16px;margin-bottom:16px;">

      <!-- FleetMap -->
      <div class="card" style="padding:0;overflow:hidden;">
        <!-- header -->
        <div style="padding:16px 20px;border-bottom:1px solid var(--line-1);display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;">
          <div>
            <div class="label-sm" style="display:flex;align-items:center;gap:8px;">
              <span class="dot dot-brand blink"/>
              FLEET MAP · LIVE
            </div>
            <div style="font-size:14px;color:var(--text-2);margin-top:3px;">
              실시간 차량 위치 · 운행 중 <strong style="color:var(--text-1);">{{ runningCount }}</strong>대 / 전체 {{ vehicles.length }}대
            </div>
          </div>
          <div style="display:flex;gap:6px;">
            <button v-for="t in mapTabs" :key="t" @click="mapTab=t"
              :style="{padding:'6px 12px',fontSize:'11.5px',fontFamily:'var(--font-mono)',borderRadius:'var(--r-sm)',cursor:'pointer',
                background:mapTab===t?'var(--accent-soft)':'var(--bg-2)',color:mapTab===t?'var(--accent)':'var(--text-3)',
                border:'1px solid '+(mapTab===t?'var(--accent-line)':'var(--line-2)'),fontWeight:mapTab===t?700:500,
                display:'flex',alignItems:'center',gap:'6px'}">
              <span>{{ t }}</span><span style="opacity:0.65;font-weight:600;">{{ tabCount(t) }}</span>
            </button>
          </div>
        </div>

        <!-- body: 1.35fr map + 1fr list -->
        <div style="display:grid;grid-template-columns:1.35fr 1fr;gap:0;">
          <!-- Korea map SVG -->
          <div style="position:relative;background:var(--bg-2);border-right:1px solid var(--line-1);min-height:460px;">
            <svg width="100%" height="100%" viewBox="0 0 600 580" preserveAspectRatio="xMidYMid meet" style="display:block;min-height:460px;">
              <defs>
                <linearGradient id="kor-grad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="var(--bg-1)"/>
                  <stop offset="100%" stop-color="var(--bg-2)"/>
                </linearGradient>
                <radialGradient id="map-glow" cx="0.5" cy="0.5" r="0.5">
                  <stop offset="0%" stop-color="var(--accent)" stop-opacity="0.04"/>
                  <stop offset="100%" stop-color="var(--accent)" stop-opacity="0"/>
                </radialGradient>
              </defs>
              <ellipse cx="280" cy="290" rx="240" ry="220" fill="url(#map-glow)"/>
              <g opacity="0.18">
                <circle v-for="(d,di) in mapDots" :key="di" :cx="d.cx" :cy="d.cy" r="0.8" fill="var(--text-4)"/>
              </g>
              <!-- peninsula -->
              <path d="M268 38 L308 42 L348 56 L378 78 L388 108 L378 138 L398 162 L432 178 L458 198 L478 232 L488 270 L482 308 L470 348 L452 378 L424 410 L394 432 L362 450 L334 472 L304 500 L274 516 L240 506 L210 478 L188 444 L172 408 L160 372 L150 336 L138 298 L130 260 L132 222 L150 188 L172 154 L196 122 L222 90 L246 58 Z"
                fill="url(#kor-grad)" stroke="var(--bg-5)" stroke-width="1.6"/>
              <ellipse cx="208" cy="540" rx="44" ry="22" fill="url(#kor-grad)" stroke="var(--bg-5)" stroke-width="1.4"/>
              <!-- highways -->
              <g stroke="var(--line-2)" stroke-width="1.2" stroke-dasharray="3 5" fill="none" opacity="0.7">
                <path d="M250 90 Q270 200 280 320 Q300 410 350 470"/>
                <path d="M195 200 Q260 250 350 240 Q420 240 470 250"/>
                <path d="M260 350 Q330 360 420 360"/>
                <path d="M198 130 Q260 180 320 200 Q400 220 440 240"/>
              </g>
              <!-- cities -->
              <g font-family="var(--font-mono)" font-size="9.5" fill="var(--text-3)">
                <circle cx="252" cy="98"  r="2.5" fill="var(--text-2)"/><text x="260" y="102" font-weight="600">서울</text>
                <circle cx="232" cy="138" r="2"   fill="var(--text-3)"/><text x="240" y="141">인천</text>
                <circle cx="290" cy="200" r="2"   fill="var(--text-3)"/><text x="298" y="203">대전</text>
                <circle cx="358" cy="248" r="2"   fill="var(--text-3)"/><text x="366" y="251">대구</text>
                <circle cx="412" cy="338" r="2"   fill="var(--text-3)"/><text x="420" y="341">울산</text>
                <circle cx="378" cy="376" r="2.5" fill="var(--text-2)"/><text x="386" y="380" font-weight="600">부산</text>
                <circle cx="208" cy="328" r="2"   fill="var(--text-3)"/><text x="156" y="332" text-anchor="end">광주</text>
                <text x="208" y="555" text-anchor="middle" font-size="9">제주</text>
              </g>
              <!-- vehicle markers -->
              <g v-for="v in filteredVehicles.slice().sort((a,b)=>(a.level==='DANGER'?1:0)-(b.level==='DANGER'?1:0))" :key="v.id"
                :transform="`translate(${v.pos.x},${v.pos.y})`" style="cursor:pointer;"
                @click="selectedId=v.id" @mouseenter="hoveredId=v.id" @mouseleave="hoveredId=null">
                <circle r="14" :fill="mapVColor(v)" opacity="0.18">
                  <template v-if="v.level==='DANGER'">
                    <animate attributeName="r" from="10" to="22" dur="2s" repeatCount="indefinite"/>
                    <animate attributeName="opacity" from="0.4" to="0" dur="2s" repeatCount="indefinite"/>
                  </template>
                </circle>
                <circle v-if="selectedId===v.id || hoveredId===v.id" r="12" fill="none" :stroke="mapVColor(v)" stroke-width="1.8" opacity="0.7"/>
                <circle :r="selectedId===v.id?7:6" :fill="mapVColor(v)" stroke="var(--bg-1)" stroke-width="2"/>
                <g v-if="selectedId===v.id||hoveredId===v.id" transform="translate(10,-10)">
                  <rect x="0" y="-14" width="118" height="24" rx="4" fill="var(--bg-1)" :stroke="mapVColor(v)" stroke-width="1"/>
                  <text x="6" y="2" font-family="var(--font-mono)" font-size="10.5" font-weight="700" fill="var(--text-1)">{{ v.plate }}</text>
                  <text x="6" y="-4" font-family="var(--font-mono)" font-size="9" :fill="mapVColor(v)" font-weight="700">{{ v.score }}점</text>
                </g>
              </g>
            </svg>
            <!-- overlay chips -->
            <div style="position:absolute;top:14px;left:14px;display:flex;flex-direction:column;gap:6px;">
              <span class="chip chip-info"><span class="dot dot-brand"/>{{ runningCount }}대 운행중</span>
              <span v-if="dangerCount>0" class="chip chip-danger"><span class="dot dot-danger"/>{{ dangerCount }} 위험</span>
              <span v-if="cautionCount>0" class="chip chip-warn"><span class="dot dot-warn"/>{{ cautionCount }} 주의</span>
            </div>
            <!-- legend -->
            <div style="position:absolute;bottom:14px;right:14px;background:rgba(255,255,255,0.92);border:1px solid var(--line-2);border-radius:var(--r-md);padding:10px 14px;font-family:var(--font-mono);font-size:10.5px;display:flex;flex-direction:column;gap:6px;backdrop-filter:blur(4px);">
              <div style="font-size:9px;letter-spacing:0.12em;color:var(--text-4);font-weight:700;">LEGEND</div>
              <div style="display:flex;gap:6px;align-items:center;"><span style="width:8px;height:8px;border-radius:50%;background:var(--danger);display:inline-block;"/>위험 (70+)</div>
              <div style="display:flex;gap:6px;align-items:center;"><span style="width:8px;height:8px;border-radius:50%;background:var(--warn);display:inline-block;"/>주의 (40~69)</div>
              <div style="display:flex;gap:6px;align-items:center;"><span style="width:8px;height:8px;border-radius:50%;background:var(--ok);display:inline-block;"/>정상 (0~39)</div>
            </div>
            <!-- scale -->
            <div style="position:absolute;top:14px;right:14px;background:rgba(255,255,255,0.85);border:1px solid var(--line-2);border-radius:var(--r-sm);padding:5px 8px;font-family:var(--font-mono);font-size:9.5px;color:var(--text-4);backdrop-filter:blur(4px);">
              <div>SCALE 1 : 5M</div>
              <div style="display:flex;gap:4px;align-items:center;margin-top:3px;"><div style="width:40px;height:3px;background:var(--text-3);"/><span>100km</span></div>
            </div>
          </div>

          <!-- Running vehicle list -->
          <div style="padding:14px;display:flex;flex-direction:column;gap:8px;min-height:460px;max-height:460px;overflow-y:auto;">
            <div style="display:flex;justify-content:space-between;align-items:center;padding:0 2px;">
              <div class="label-sm">RUNNING VEHICLES · {{ filteredVehicles.length }}대</div>
              <span style="font-family:var(--font-mono);font-size:10.5px;color:var(--text-4);">{{ mapTab==='전체'?'점수 높은 순':mapTab }}</span>
            </div>
            <div v-if="sortedFiltered.length===0" style="padding:32px 14px;text-align:center;color:var(--text-4);font-size:12.5px;">해당 등급의 운행 차량이 없습니다</div>
            <!-- VehicleRow -->
            <div v-for="v in sortedFiltered" :key="v.id" @click="selectedId=v.id"
              @mouseenter="hoveredId=v.id" @mouseleave="hoveredId=null"
              :style="{display:'grid',gridTemplateColumns:'auto 1fr auto',gap:'10px',alignItems:'stretch',
                padding:'9px',cursor:'pointer',borderRadius:'var(--r-sm)',
                background:selectedId===v.id?'var(--accent-soft)':hoveredId===v.id?'var(--bg-3)':'var(--bg-2)',
                border:'1px solid '+(selectedId===v.id||hoveredId===v.id?'var(--accent-line)':'var(--line-2)'),
                transition:'background .12s,border-color .12s'}">
              <!-- plate placeholder (light) -->
              <div :style="{width:'76px',height:'56px',borderRadius:'4px',flexShrink:0,background:'linear-gradient(135deg,#DCDFE4,#B8BFC9)',border:'1px solid var(--line-3)',position:'relative',overflow:'hidden'}">
                <svg width="100%" height="100%" viewBox="0 0 200 130" preserveAspectRatio="xMidYMid slice" style="opacity:0.4;display:block;">
                  <rect width="200" height="130" fill="#E3E6EB"/>
                  <rect x="30" y="40" width="120" height="50" fill="#CCD2DA" stroke="#979EAE" stroke-width="1"/>
                  <circle cx="55" cy="98" r="8" fill="#747F95"/>
                  <circle cx="160" cy="98" r="8" fill="#747F95"/>
                </svg>
                <div style="position:absolute;left:50%;bottom:6px;transform:translateX(-50%);background:#fff;border:1px solid var(--accent-line);border-radius:2px;padding:2px 6px;font-family:var(--font-mono);font-size:10px;font-weight:700;color:var(--accent);white-space:nowrap;">{{ v.plate.split(' ').slice(-1)[0] }}</div>
              </div>
              <!-- middle: plate + chip + factor bars -->
              <div style="min-width:0;display:flex;flex-direction:column;gap:5px;justify-content:space-between;">
                <div style="display:flex;gap:6px;align-items:center;flex-wrap:wrap;">
                  <span style="font-family:var(--font-mono);font-size:12px;font-weight:700;">{{ v.plate }}</span>
                  <span :class="levelChipCls(v.level)">{{ levelLabel(v.level) }} {{ v.score }}</span>
                </div>
                <div style="font-size:10.5px;color:var(--text-3);display:flex;align-items:center;gap:5px;font-family:var(--font-mono);">
                  <AppIcon name="user" :size="9"/>{{ v.driver }}
                  <span style="color:var(--text-4);">·</span>
                  <AppIcon name="location" :size="9"/>
                  <span style="overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">{{ v.loc.replace(/^.*?· /,'') }}</span>
                </div>
                <!-- factor bars -->
                <div style="display:flex;flex-direction:column;gap:2px;margin-top:1px;">
                  <div v-for="f in vFactors" :key="f.key" style="display:grid;grid-template-columns:34px 1fr 30px;gap:6px;align-items:center;">
                    <span style="font-family:var(--font-mono);font-size:8.5px;color:var(--text-4);letter-spacing:0.06em;">{{ f.key }}</span>
                    <div style="position:relative;height:4px;background:var(--bg-3);border-radius:2px;overflow:hidden;">
                      <div :style="`position:absolute;top:0;left:0;height:100%;width:${fbPct(factorVal(v,f.key),f.max)}%;background:${fbColor(factorVal(v,f.key),f.thr)};transition:width .3s;`"/>
                      <div :style="`position:absolute;top:-1px;bottom:-1px;left:${(f.thr[0]/f.max)*100}%;width:1px;background:var(--warn);`"/>
                      <div :style="`position:absolute;top:-1px;bottom:-1px;left:${(f.thr[1]/f.max)*100}%;width:1px;background:var(--danger);`"/>
                    </div>
                    <span :style="`font-family:var(--font-mono);font-size:9px;color:${fbColor(factorVal(v,f.key),f.thr)};font-weight:700;text-align:right;`">{{ factorVal(v,f.key)===0?'—':fmtHM(factorVal(v,f.key)) }}</span>
                  </div>
                </div>
              </div>
              <!-- right: score + sparkline -->
              <div style="display:flex;flex-direction:column;align-items:flex-end;justify-content:space-between;min-width:92px;">
                <div style="display:flex;flex-direction:column;align-items:flex-end;">
                  <span :style="`font-family:var(--font-mono);font-size:22px;font-weight:800;color:${vSparkColor(v)};line-height:1;letter-spacing:-0.015em;`">{{ v.score }}</span>
                  <span style="font-size:9px;color:var(--text-4);font-family:var(--font-mono);">/100</span>
                </div>
                <svg width="86" height="24" style="display:block;">
                  <line x1="0" :y1="22-(40/100)*18" x2="86" :y2="22-(40/100)*18" stroke="var(--warn)" stroke-dasharray="2 3" stroke-width="0.6"/>
                  <line x1="0" :y1="22-(70/100)*18" x2="86" :y2="22-(70/100)*18" stroke="var(--danger)" stroke-dasharray="2 3" stroke-width="0.6"/>
                  <polyline :points="vSparkPts(v)" fill="none" :stroke="vSparkColor(v)" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle :cx="vSparkEnd(v).cx" :cy="vSparkEnd(v).cy" r="2" :fill="vSparkColor(v)" stroke="var(--bg-1)" stroke-width="1"/>
                </svg>
                <span :style="`font-family:var(--font-mono);font-size:9.5px;color:${vDelta(v).color};font-weight:700;`">{{ vDelta(v).txt }} · {{ v.startedAt }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- EventStream + AlertList -->
      <div style="display:grid;grid-template-rows:auto 1fr;gap:16px;min-width:0;">

        <!-- EventStream -->
        <div class="card" style="padding:0;overflow:hidden;">
          <div style="padding:14px 18px;border-bottom:1px solid var(--line-1);display:flex;justify-content:space-between;align-items:center;">
            <div>
              <div class="label-sm" style="display:flex;align-items:center;gap:8px;"><span class="dot dot-brand blink"/>EVENT STREAM</div>
              <div style="font-size:12.5px;color:var(--text-2);margin-top:2px;">시간 역순</div>
            </div>
            <span class="chip chip-info">{{ allEvents.length }} events</span>
          </div>
          <div style="padding:6px 6px 6px 4px;max-height:340px;overflow-y:auto;">
            <table style="width:100%;border-collapse:collapse;font-size:12.5px;">
              <tbody>
                <tr v-for="(e,i) in allEvents" :key="i"
                  @click="()=>{ const id=eventVehicleId(e.plate); if(id) selectedId=id; }"
                  :style="{cursor:eventVehicleId(e.plate)?'pointer':'default',borderBottom:i<allEvents.length-1?'1px solid var(--line-1)':'none'}"
                  @mouseenter="ev=>ev.currentTarget.style.background='var(--bg-2)'"
                  @mouseleave="ev=>ev.currentTarget.style.background='transparent'">
                  <td style="padding:8px 10px;white-space:nowrap;vertical-align:middle;width:78px;">
                    <div style="display:flex;align-items:center;gap:6px;">
                      <span :style="`width:6px;height:6px;border-radius:50%;background:${evColor(e.kind)};flex-shrink:0;display:inline-block;`"/>
                      <span style="font-family:var(--font-mono);font-size:11px;color:var(--text-3);">{{ e.t }}</span>
                    </div>
                  </td>
                  <td style="padding:8px 10px;white-space:nowrap;vertical-align:middle;width:132px;">
                    <span :style="`font-family:var(--font-mono);font-size:11.5px;font-weight:700;color:${evColor(e.kind)};`">{{ e.plate }}</span>
                  </td>
                  <td style="padding:8px 10px;vertical-align:middle;color:var(--text-2);font-size:12px;">{{ e.text }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- AlertList -->
        <div class="card" style="padding:0;overflow:hidden;display:flex;flex-direction:column;">
          <div style="padding:14px 18px;border-bottom:1px solid var(--line-1);display:flex;justify-content:space-between;align-items:center;">
            <div class="label-sm" style="display:flex;align-items:center;gap:8px;"><span class="dot dot-danger pulse-ring"/>ACTIVE ALERTS</div>
            <span class="chip chip-danger">{{ alertList.length }} active</span>
          </div>
          <div style="padding:10px;display:flex;flex-direction:column;gap:6px;max-height:320px;overflow-y:auto;">
            <div v-if="alertList.length===0" style="padding:32px 14px;text-align:center;color:var(--text-4);font-size:12.5px;">현재 활성 알림이 없습니다</div>
            <div v-for="a in alertList" :key="a.id" @click="selectedId=a.id"
              :style="{padding:'10px 12px',borderLeft:`3px solid var(--${a.sev})`,background:`var(--${a.sev}-soft)`,borderRadius:'0 4px 4px 0',cursor:'pointer',transition:'background .12s'}">
              <div style="display:flex;justify-content:space-between;align-items:center;">
                <span :style="`font-family:var(--font-mono);font-size:12px;font-weight:700;color:var(--${a.sev});`">{{ a.plate }}</span>
                <span style="font-family:var(--font-mono);font-size:11px;color:var(--text-4);">{{ a.t }}</span>
              </div>
              <div style="font-size:12.5px;color:var(--text-1);margin-top:5px;">{{ a.desc }}</div>
              <div style="font-size:11.5px;color:var(--text-3);margin-top:4px;display:flex;gap:6px;align-items:center;">
                <AppIcon name="check" :size="10"/> {{ a.action }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ── VehicleDrilldown ── -->
    <div v-if="selected" class="card" style="margin-bottom:16px;overflow:hidden;padding:0;">
      <!-- header -->
      <div style="padding:16px 18px;border-bottom:1px solid var(--line-1);display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;">
        <div style="display:flex;align-items:center;gap:16px;">
          <!-- plate placeholder 130x86 light -->
          <div style="width:130px;height:86px;border-radius:4px;flex-shrink:0;background:linear-gradient(135deg,#DCDFE4,#B8BFC9);border:1px solid var(--line-3);position:relative;overflow:hidden;">
            <svg width="100%" height="100%" viewBox="0 0 200 130" preserveAspectRatio="xMidYMid slice" style="opacity:0.4;display:block;">
              <rect width="200" height="130" fill="#E3E6EB"/>
              <rect x="30" y="40" width="120" height="50" fill="#CCD2DA" stroke="#979EAE" stroke-width="1"/>
              <circle cx="55" cy="98" r="8" fill="#747F95"/>
              <circle cx="160" cy="98" r="8" fill="#747F95"/>
            </svg>
            <div style="position:absolute;left:50%;bottom:6px;transform:translateX(-50%);background:#fff;border:1px solid var(--accent-line);border-radius:2px;padding:2px 6px;font-family:var(--font-mono);font-size:10px;font-weight:700;color:var(--accent);white-space:nowrap;">{{ selected.plate }}</div>
          </div>
          <div>
            <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap;">
              <span style="font-family:var(--font-mono);font-size:18px;font-weight:800;">{{ selected.plate }}</span>
              <span :class="levelChipCls(selected.level)">{{ levelLabel(selected.level) }} · {{ selected.score }}점</span>
              <span class="chip chip-mute"><AppIcon name="user" :size="10"/> {{ selected.driver }}</span>
              <span class="chip chip-info">{{ selected.driveLogId || '운행 ID 없음' }}</span>
            </div>
            <div style="font-family:var(--font-mono);font-size:11.5px;color:var(--text-3);margin-top:5px;display:flex;gap:8px;align-items:center;">
              <AppIcon name="location" :size="11"/>{{ selected.loc }}
              <span>·</span><span>시작 {{ selected.startedAt }}</span>
              <span>·</span><span>{{ selected.spd }} km/h</span>
              <span>·</span><span>{{ selected.type }}</span>
            </div>
          </div>
        </div>
        <div style="display:flex;gap:8px;">
          <button class="btn btn-ghost" :disabled="selected.level!=='DANGER'" :style="{opacity:selected.level==='DANGER'?1:0.45,cursor:selected.level==='DANGER'?'pointer':'not-allowed'}">
            <AppIcon name="phone" :size="13"/> 전화 권고
          </button>
          <button class="btn btn-ghost"><AppIcon name="coffee" :size="13"/> 휴게 안내</button>
          <button class="btn btn-primary" @click="selected.driveLogId && router.push('/drive-history/'+selected.driveLogId)">
            <AppIcon name="arrow" :size="13"/> 운행 상세
          </button>
        </div>
      </div>

      <!-- body: factor breakdown + timeline -->
      <div style="display:grid;grid-template-columns:1fr 1.7fr;">
        <!-- Factor breakdown -->
        <div style="padding:18px;border-right:1px solid var(--line-1);">
          <div class="label-sm" style="margin-bottom:12px;display:flex;justify-content:space-between;align-items:center;">
            <span>FATIGUE BREAKDOWN</span>
            <span style="font-family:var(--font-mono);font-size:9px;color:var(--text-4);letter-spacing:0.06em;">score = Σ(factor · weight)</span>
          </div>
          <div style="display:flex;flex-direction:column;gap:12px;">
            <div v-for="fb in drillFactors" :key="fb.label"
              :style="{padding:'10px 12px',background:'var(--bg-2)',border:'1px solid var(--line-2)',borderRadius:'var(--r-sm)',borderTop:`2px solid ${fbColor(fb.val,fb.thr)}`}">
              <div style="display:flex;justify-content:space-between;align-items:flex-end;">
                <div>
                  <div style="font-size:12px;color:var(--text-2);font-weight:600;">{{ fb.label }}</div>
                  <div style="font-family:var(--font-mono);font-size:9.5px;color:var(--text-4);letter-spacing:0.04em;margin-top:1px;">max {{ fb.max }}{{ fb.unit }} · 임계 {{ fb.thr[0] }} / {{ fb.thr[1] }}{{ fb.unit }}</div>
                </div>
                <div style="display:flex;align-items:baseline;gap:4px;">
                  <span :style="`font-family:var(--font-mono);font-size:20px;font-weight:800;color:${fbColor(fb.val,fb.thr)};letter-spacing:-0.01em;line-height:1;`">{{ (Math.round(fb.val*10)/10).toFixed(1) }}</span>
                  <span style="font-family:var(--font-mono);font-size:10px;color:var(--text-4);">{{ fb.unit }}</span>
                </div>
              </div>
              <div style="position:relative;height:10px;background:var(--bg-3);border-radius:5px;margin-top:8px;overflow:hidden;">
                <div :style="`position:absolute;top:0;left:0;height:100%;width:${fbPct(fb.val,fb.max)}%;background:linear-gradient(90deg,var(--ok),var(--warn) 50%,var(--danger) 85%);transition:width .4s;`"/>
                <div :style="`position:absolute;top:-3px;bottom:-3px;left:${(fb.thr[0]/fb.max)*100}%;width:1.5px;background:var(--warn);`"/>
                <div :style="`position:absolute;top:-3px;bottom:-3px;left:${(fb.thr[1]/fb.max)*100}%;width:1.5px;background:var(--danger);`"/>
                <div :style="`position:absolute;top:-3px;bottom:-3px;left:calc(${fbPct(fb.val,fb.max)}% - 1px);width:2px;background:var(--text-1);`"/>
              </div>
              <div style="display:flex;justify-content:space-between;margin-top:4px;font-family:var(--font-mono);font-size:9.5px;">
                <span :style="fb.val>fb.thr[0]?'color:var(--warn);font-weight:700;':'color:var(--text-4);'">{{ fb.val>fb.thr[0]?'▲ ':'○ ' }}{{ fb.cLbl }}</span>
                <span :style="fb.val>fb.thr[1]?'color:var(--danger);font-weight:700;':'color:var(--text-4);'">{{ fb.val>fb.thr[1]?'▲ ':'○ ' }}{{ fb.dLbl }}</span>
              </div>
            </div>
          </div>
          <div class="label-sm" style="margin:16px 0 8px;display:flex;justify-content:space-between;align-items:center;">
            <span>REST EVENTS</span>
            <span style="font-family:var(--font-mono);font-size:9px;color:var(--text-4);letter-spacing:0.06em;">4 stages · 점수 보정</span>
          </div>
          <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:6px;">
            <div v-for="r in restEvents" :key="r.label"
              :style="{padding:'10px',borderRadius:'4px',background:'var(--bg-2)',border:'1px solid var(--line-2)',borderTop:`2px solid ${r.color}`}">
              <div :style="`font-family:var(--font-mono);font-size:9px;letter-spacing:0.1em;font-weight:700;color:${r.color};`">{{ r.label }}</div>
              <div :style="`font-family:var(--font-mono);font-size:22px;font-weight:800;margin-top:3px;letter-spacing:-0.015em;color:${r.count>0?'var(--text-1)':'var(--text-4)'};`">{{ r.count }}</div>
              <div style="font-family:var(--font-mono);font-size:9px;color:var(--text-4);margin-top:2px;">{{ r.hint }}</div>
            </div>
          </div>
        </div>

        <!-- Drive timeline -->
        <div style="padding:18px;">
          <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:8px;">
            <div>
              <div class="label-sm">DRIVE TIMELINE · {{ selected.plate }}</div>
              <div style="font-size:12.5px;color:var(--text-2);margin-top:2px;">시간대별 누적 피로 점수 (12h)</div>
            </div>
            <div style="display:flex;align-items:baseline;gap:14px;">
              <div>
                <div class="label-sm">현재</div>
                <div style="display:flex;align-items:baseline;gap:4px;margin-top:3px;">
                  <span :style="`font-family:var(--font-mono);font-size:22px;font-weight:700;color:${levelLabel(selected.level)==='위험'?'var(--danger)':levelLabel(selected.level)==='주의'?'var(--warn)':'var(--ok)'};line-height:1;`">{{ selected.score }}</span>
                  <span style="font-family:var(--font-mono);font-size:10px;color:var(--text-4);">/100</span>
                </div>
              </div>
              <div style="border-left:1px solid var(--line-2);padding-left:14px;">
                <div class="label-sm">최고</div>
                <span :style="`font-family:var(--font-mono);font-size:22px;font-weight:800;color:${levelLabel(selected.level)==='위험'?'var(--danger)':levelLabel(selected.level)==='주의'?'var(--warn)':'var(--ok)'};letter-spacing:-0.01em;`">{{ peakScore }}</span>
              </div>
            </div>
          </div>
          <svg width="100%" height="220" viewBox="0 0 800 220" preserveAspectRatio="none">
            <rect x="40" y="20"  width="740" height="74" fill="rgba(181,84,74,0.07)"/>
            <rect x="40" y="94"  width="740" height="54" fill="rgba(197,138,58,0.07)"/>
            <rect x="40" y="148" width="740" height="52" fill="rgba(94,138,111,0.06)"/>
            <text x="780" y="34"  text-anchor="end" font-family="var(--font-mono)" font-size="9.5" fill="var(--danger)">위험 ≥ 70</text>
            <text x="780" y="108" text-anchor="end" font-family="var(--font-mono)" font-size="9.5" fill="var(--warn)">주의 40~69</text>
            <text x="780" y="162" text-anchor="end" font-family="var(--font-mono)" font-size="9.5" fill="var(--ok)">정상 ≤ 39</text>
            <line v-for="y in [0,0.25,0.5,0.75,1]" :key="y" x1="40" x2="780" :y1="20+y*180" :y2="20+y*180" stroke="var(--line-1)" stroke-dasharray="2 4"/>
            <polyline :points="tlScoreArea" fill="rgba(81,95,122,0.18)" stroke="none"/>
            <polyline :points="tlScorePts" fill="none" stroke="var(--accent)" stroke-width="2.2"/>
            <g v-for="(p,i) in tlEvents" :key="i">
              <line :x1="tlPx(p.t)" y1="20" :x2="tlPx(p.t)" y2="200" stroke="var(--accent)" stroke-dasharray="2 4" opacity="0.35"/>
              <circle :cx="tlPx(p.t)" :cy="tlPy(p.score)" r="4" :fill="tlEventColor(p.event)" stroke="var(--bg-1)" stroke-width="2"/>
              <text :x="tlPx(p.t)+6" :y="tlPy(p.score)-8" font-family="var(--font-mono)" font-size="9.5" fill="var(--text-2)">{{ p.event }}</text>
            </g>
            <text v-for="(h,i) in [0,2,4,6,8,10,12]" :key="'h'+i" :x="40+h*61.6" y="212" text-anchor="middle" font-family="var(--font-mono)" font-size="10" fill="var(--text-4)">+{{ h }}h</text>
          </svg>
        </div>
      </div>
    </div>

    <!-- ── Heatmap + Ranking ── -->
    <div style="display:grid;grid-template-columns:minmax(0,1.65fr) minmax(0,1fr);gap:16px;margin-bottom:24px;">
      <!-- HeatmapHours -->
      <div class="card" style="padding:18px;">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
          <div>
            <div class="label-sm">DRIVER × HOUR HEATMAP</div>
            <div style="font-size:12.5px;color:var(--text-2);margin-top:2px;">오늘 시간대별 누적 피로 점수</div>
          </div>
          <span class="chip chip-mute">2026.05.04</span>
        </div>
        <div style="display:flex;flex-direction:column;gap:6px;">
          <!-- hour axis -->
          <div style="display:grid;grid-template-columns:120px repeat(24,1fr);gap:2px;font-family:var(--font-mono);font-size:9px;color:var(--text-4);">
            <div/>
            <div v-for="h in 24" :key="h" style="text-align:center;">{{ String(h-1).padStart(2,'0') }}</div>
          </div>
          <!-- rows -->
          <div v-for="d in heatmapDrivers" :key="d.plate" style="display:grid;grid-template-columns:120px repeat(24,1fr);gap:2px;align-items:center;">
            <div style="font-size:11.5px;">
              <div style="font-weight:600;color:var(--text-1);">{{ d.name }}</div>
              <div style="font-family:var(--font-mono);color:var(--text-4);font-size:10px;margin-top:1px;">{{ d.plate }}</div>
            </div>
            <div v-for="(s,i) in d.hours" :key="i"
              :style="`height:22px;border-radius:2px;background:${heatColor(s)};`"
              :title="`${i}:00 · ${s===null?'운행 없음':s+'점'}`"/>
          </div>
        </div>
        <div style="margin-top:8px;display:flex;align-items:center;gap:8px;font-family:var(--font-mono);font-size:10px;color:var(--text-3);">
          <span>0 (안전)</span>
          <div style="display:flex;gap:2px;">
            <span v-for="c in ['var(--bg-2)','#5E8A6F','#92A08F','#C5A56A','#C58A3A','#B5544A']" :key="c"
              :style="`width:18px;height:14px;background:${c};${c==='var(--bg-2)'?'border:1px solid var(--line-2);':''}display:inline-block;`"/>
          </div>
          <span>100 (위험)</span>
        </div>
      </div>

      <!-- DriverRanking -->
      <div class="card" style="padding:0;overflow:hidden;">
        <div style="padding:14px 18px;border-bottom:1px solid var(--line-1);display:flex;justify-content:space-between;align-items:center;">
          <div>
            <div class="label-sm">DRIVER RANKING · TODAY</div>
            <div style="font-size:12.5px;color:var(--text-2);margin-top:2px;">현재 누적 피로 점수</div>
          </div>
          <span style="font-family:var(--font-mono);font-size:11px;color:var(--text-3);">상위 6명</span>
        </div>
        <div style="padding:16px 18px;">
          <div style="display:flex;flex-direction:column;gap:10px;">
            <div v-for="it in rankingItems" :key="it.id" @click="selectedId=it.id"
              style="display:grid;grid-template-columns:120px 1fr 60px;gap:14px;align-items:center;cursor:pointer;">
              <div>
                <div style="font-size:12.5px;font-weight:600;color:var(--text-1);">{{ it.label }}</div>
                <div style="font-family:var(--font-mono);font-size:10.5px;color:var(--text-4);">{{ it.sub }}</div>
              </div>
              <div style="height:16px;background:var(--bg-2);border-radius:3px;overflow:hidden;position:relative;">
                <div :style="`width:${(it.value/100)*100}%;height:100%;background:${it.color};transition:width 0.4s;`"/>
                <div style="position:absolute;top:50%;left:8px;transform:translateY(-50%);font-family:var(--font-mono);font-size:10px;color:var(--bg-1);font-weight:700;">{{ it.value }}</div>
              </div>
              <span :style="`font-family:var(--font-mono);font-size:11px;font-weight:700;color:${it.color};text-align:right;`">{{ it.tag }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.fade-up { animation: fadeUp 0.4s ease both; }
.view    { padding: 32px 32px 40px; }
@keyframes fadeUp { from { opacity:0; transform:translateY(8px); } to { opacity:1; transform:translateY(0); } }

/* KPI strip */
.kpi-strip {
  background: var(--bg-1);
  border: 1px solid var(--line-2);
  border-radius: 14px;
  overflow: hidden;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  box-shadow: var(--shadow-1);
}
.kpi-tile {
  padding: 14px 16px;
  position: relative;
  border-right: 1px solid var(--line-1);
  min-width: 180px;
}
.tile-hdr { display:flex; justify-content:space-between; align-items:center; }
.tile-label { font: 600 9.5px/1 var(--font-mono); letter-spacing: 0.14em; text-transform: uppercase; color: var(--text-4); }
.tile-meta  { font-family: var(--font-mono); font-size: 9px; color: var(--text-4); }
.tile-val   { display:flex; align-items:baseline; gap:4px; margin-top:8px; }
.tile-delta { margin-left:auto; font-family:var(--font-mono); font-size:10.5px; padding:2px 5px; border-radius:3px; }
.tile-delta.bad  { color:var(--danger); background:var(--danger-soft); }
.tile-delta.good { color:var(--ok);     background:var(--ok-soft); }

/* label-sm */
.label-sm {
  font-family: var(--font-mono);
  font-size: 10.5px; letter-spacing: 0.12em;
  text-transform: uppercase; color: var(--text-3); font-weight: 600;
}

/* chips */
.chip {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 3px 8px; border-radius: 999px;
  font-size: 11px; font-weight: 500; letter-spacing: 0.02em;
  border: 1px solid transparent; font-family: var(--font-mono);
}
.chip-ok     { background:var(--ok-soft);     color:var(--ok);     border-color:rgba(138,186,154,0.25); }
.chip-warn   { background:var(--warn-soft);   color:var(--warn);   border-color:rgba(214,181,106,0.25); }
.chip-danger { background:var(--danger-soft); color:var(--danger); border-color:rgba(209,139,126,0.25); }
.chip-info   { background:var(--info-soft);   color:var(--info);   border-color:var(--accent-line); }
.chip-mute   { background:var(--bg-3);        color:var(--text-3); border-color:var(--line-2); }

/* dots */
.dot         { width:6px; height:6px; border-radius:50%; display:inline-block; }
.dot-ok      { background:var(--ok);     box-shadow:0 0 0 3px var(--ok-soft); }
.dot-danger  { background:var(--danger); box-shadow:0 0 0 3px var(--danger-soft); }
.dot-warn    { background:var(--warn);   box-shadow:0 0 0 3px var(--warn-soft); }
.dot-brand   { background:var(--accent); box-shadow:0 0 0 3px var(--accent-soft); }

/* animations */
.blink { animation: blink-soft 2s infinite; }
@keyframes blink-soft { 0%,100% { opacity:1; } 50% { opacity:0.55; } }
.pulse-ring { animation: pulse-ring 2s infinite; }
@keyframes pulse-ring { 0%{box-shadow:0 0 0 0 rgba(209,139,126,0.45);} 70%{box-shadow:0 0 0 8px rgba(209,139,126,0);} 100%{box-shadow:0 0 0 0 rgba(209,139,126,0);} }

/* card */
.card { background:var(--bg-1); border:1px solid var(--line-2); border-radius:var(--r-md); }
</style>
