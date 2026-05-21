<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppIcon from '@/components/common/AppIcon.vue'
import { dashboardApi } from '@/api/dashboardApi'

const route = useRoute()
const router = useRouter()

/* ─── 시드 데이터 (lat/lng 추가) ─── */
const SEED_VEHICLES = [
  { id:1,  plate:'경기 80바 1024', driver:'김민수', type:'카고 5톤',    score:78, level:'DANGER',  status:'RUNNING', spd:87, contMin:384, dailyMin:672, nightMin:204, restValid:1, restSuff:0, restInvalid:1, restMiss:2, loc:'경부고속 · 안성IC',  startedAt:'03:18', scenario:'C', driveLogId:'DL-2026-0438', lat:37.0078, lng:127.2714 },
  { id:2,  plate:'경기 80바 1025', driver:'박정호', type:'윙바디 11톤', score:56, level:'CAUTION', status:'RUNNING', spd:78, contMin:270, dailyMin:480, nightMin:108, restValid:2, restSuff:0, restInvalid:0, restMiss:1, loc:'서해안 · 서산IC',    startedAt:'06:11', scenario:'B', driveLogId:'DL-2026-0437', lat:36.7842, lng:126.4502 },
  { id:3,  plate:'경기 80바 1026', driver:'이영준', type:'카고 4.5톤',  score:18, level:'NORMAL',  status:'RUNNING', spd:92, contMin:108, dailyMin:186, nightMin:0,   restValid:0, restSuff:1, restInvalid:0, restMiss:0, loc:'중부고속 · 음성IC',  startedAt:'11:02', scenario:'A', driveLogId:'DL-2026-0436', lat:36.9357, lng:127.6882 },
  { id:4,  plate:'경기 80바 1027', driver:'최성훈', type:'윙바디 8톤',  score:32, level:'NORMAL',  status:'RUNNING', spd:81, contMin:180, dailyMin:342, nightMin:30,  restValid:1, restSuff:1, restInvalid:0, restMiss:0, loc:'영동고속 · 여주IC',  startedAt:'08:50', scenario:'A', driveLogId:'DL-2026-0435', lat:37.2986, lng:127.6374 },
  { id:5,  plate:'경기 80바 1028', driver:'정우석', type:'카고 5톤',    score:12, level:'NORMAL',  status:'RUNNING', spd:76, contMin:60,  dailyMin:114, nightMin:0,   restValid:0, restSuff:0, restInvalid:0, restMiss:0, loc:'남해고속 · 진주IC',  startedAt:'12:39', scenario:'A', driveLogId:'DL-2026-0434', lat:35.1797, lng:128.1077 },
  { id:6,  plate:'경기 80바 1029', driver:'강지훈', type:'카고 5톤',    score:84, level:'DANGER',  status:'RUNNING', spd:64, contMin:342, dailyMin:618, nightMin:168, restValid:1, restSuff:0, restInvalid:1, restMiss:2, loc:'중부내륙 · 점촌IC',  startedAt:'04:02', scenario:'C', driveLogId:'DL-2026-0433', lat:36.3933, lng:128.1968 },
  { id:7,  plate:'경기 80바 1030', driver:'한승연', type:'카고 2.5톤',  score:48, level:'CAUTION', status:'RUNNING', spd:71, contMin:228, dailyMin:384, nightMin:72,  restValid:1, restSuff:0, restInvalid:0, restMiss:1, loc:'서울외곽 · 송내IC',  startedAt:'07:45', scenario:'B', driveLogId:'DL-2026-0432', lat:37.4875, lng:126.7657 },
  { id:8,  plate:'경기 80바 1031', driver:'조영민', type:'윙바디 11톤', score:41, level:'CAUTION', status:'RUNNING', spd:84, contMin:252, dailyMin:426, nightMin:48,  restValid:1, restSuff:0, restInvalid:0, restMiss:1, loc:'서해안 · 당진IC',    startedAt:'06:50', scenario:'B', driveLogId:'DL-2026-0431', lat:36.8897, lng:126.6453 },
  { id:9,  plate:'경기 80바 1032', driver:'윤태경', type:'카고 5톤',    score:0,  level:'NORMAL',  status:'IDLE',    spd:0,  contMin:0,   dailyMin:0,   nightMin:0,   restValid:0, restSuff:0, restInvalid:0, restMiss:0, loc:'한라물류 · 차고지',  startedAt:'—',     scenario:'—', driveLogId:null, lat:37.5665, lng:126.9780 },
  { id:10, plate:'경기 80바 1033', driver:'서동현', type:'윙바디 8톤',  score:0,  level:'NORMAL',  status:'IDLE',    spd:0,  contMin:0,   dailyMin:0,   nightMin:0,   restValid:0, restSuff:0, restInvalid:0, restMiss:0, loc:'한라물류 · 차고지',  startedAt:'—',     scenario:'—', driveLogId:null, lat:37.5665, lng:126.9780 },
]

const DEMO_RUNNING_PRESETS = SEED_VEHICLES.filter(v => v.status === 'RUNNING')

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
function evColor(k)       { return k === 'danger' ? 'var(--danger)' : k === 'warn' ? 'var(--warn)' : 'var(--accent)' }
function startedAtLabel(dt, fallback = '—') { return dt ? String(dt).slice(11, 16) : fallback }
function mapVColor(v)     { return v.level === 'DANGER' ? '#B5544A' : v.level === 'CAUTION' ? '#C58A3A' : '#515F7A' }

function buildRuntimeVehicle(apiVehicle, index) {
  const preset = DEMO_RUNNING_PRESETS[index % DEMO_RUNNING_PRESETS.length] || DEMO_RUNNING_PRESETS[0]
  const score = apiVehicle.fatigueScore ?? preset.score
  const level = apiVehicle.fatigueLevel ?? preset.level
  const contMin = level === 'DANGER' ? Math.max(240, 180 + score * 2) :
    level === 'CAUTION' ? Math.max(120, 90 + score * 2) :
    Math.max(30, score * 3)
  const dailyMin = Math.max(contMin, contMin + 120 + index * 18)
  const nightMin = level === 'DANGER' ? Math.max(30, Math.round(contMin * 0.35)) :
    level === 'CAUTION' ? Math.round(contMin * 0.2) : Math.round(contMin * 0.08)

  return {
    ...preset,
    id: apiVehicle.vehicleId ?? apiVehicle.driveLogId ?? preset.id,
    plate: apiVehicle.plateNo ?? preset.plate,
    driver: apiVehicle.driverName ?? preset.driver,
    type: apiVehicle.vehicleType ?? preset.type,
    score, level, status: 'RUNNING',
    contMin, dailyMin, nightMin,
    restValid: level === 'DANGER' ? 1 : 1 + (index % 2),
    restSuff:  level === 'NORMAL' ? 1 : 0,
    restInvalid: level === 'DANGER' ? 1 : 0,
    restMiss: level === 'DANGER' ? 2 : level === 'CAUTION' ? 1 : 0,
    startedAt: startedAtLabel(apiVehicle.startedAt, preset.startedAt),
    driveLogId: apiVehicle.driveLogId ?? null,
    lat: preset.lat, lng: preset.lng,
  }
}

/* ─── State ─── */
const now        = ref(new Date().toLocaleString('ko-KR', { hour12: false }))
const mapTab     = ref('전체')
const mapTabs    = ['전체', '위험', '주의', '정상']
const vehicles   = ref([])
const selectedId = ref(null)
const isDemoBoard = computed(() => route.name === 'demoBoard')

/* ─── Kakao Maps ─── */
const mapContainer = ref(null)
let kakaoMap       = null
let kakaoOverlays  = []

function makeMarkerEl(v) {
  const c    = mapVColor(v)
  const isSel = v.id === selectedId.value
  const size = isSel ? 18 : 13
  const pulse   = isSel ? `<div style="position:absolute;inset:-6px;border-radius:50%;border:2px solid ${c};opacity:.55;animation:kakao-pulse 1.4s ease-out infinite;pointer-events:none"></div>` : ''
  const tooltip = isSel ? `<div style="position:absolute;left:50%;bottom:calc(100%+6px);transform:translateX(-50%);background:#F1F3F6;border:1px solid ${c};border-radius:4px;padding:3px 7px;white-space:nowrap;font-family:'JetBrains Mono',monospace;font-size:10px;font-weight:700;color:#1F2630;box-shadow:0 2px 6px rgba(0,0,0,.15);pointer-events:none"><span style="color:${c}">${v.score}점</span> ${v.plate}</div>` : ''
  const el = document.createElement('div')
  el.style.cssText = `position:relative;width:${size*2}px;height:${size*2}px;cursor:pointer`
  el.innerHTML = `${pulse}<div style="position:absolute;inset:0;border-radius:50%;background:${c};opacity:.18"></div><div style="position:absolute;inset:${size-6}px;border-radius:50%;background:${c};border:2px solid #F1F3F6;box-shadow:0 1px 4px rgba(0,0,0,.25)"></div>${tooltip}`
  el.addEventListener('click', () => { selectedId.value = v.id })
  return el
}

function renderKakaoMarkers() {
  kakaoOverlays.forEach(o => o.setMap(null))
  kakaoOverlays = []
  if (!kakaoMap) return

  const running  = vehicles.value.filter(v => v.status === 'RUNNING')
  const filtered = mapTab.value === '전체' ? running
    : running.filter(v => v.level === (mapTab.value === '위험' ? 'DANGER' : mapTab.value === '주의' ? 'CAUTION' : 'NORMAL'))

  filtered.forEach(v => {
    const overlay = new window.kakao.maps.CustomOverlay({
      position: new window.kakao.maps.LatLng(v.lat, v.lng),
      content:  makeMarkerEl(v),
      yAnchor: 0.5, xAnchor: 0.5,
      zIndex: v.id === selectedId.value ? 10 : 1,
    })
    overlay.setMap(kakaoMap)
    kakaoOverlays.push(overlay)
  })

  if (selectedId.value) {
    const sel = vehicles.value.find(v => v.id === selectedId.value)
    if (sel) kakaoMap.panTo(new window.kakao.maps.LatLng(sel.lat, sel.lng))
  }
}

function initKakaoMap() {
  if (typeof window.kakao === 'undefined') return
  window.kakao.maps.load(() => {
    const container = mapContainer.value
    if (!container) return
    if (!container.offsetHeight) container.style.height = '360px'
    kakaoMap = new window.kakao.maps.Map(container, {
      center: new window.kakao.maps.LatLng(36.5, 127.8),
      level: 9,
    })
    kakaoMap.addControl(new window.kakao.maps.ZoomControl(), window.kakao.maps.ControlPosition.RIGHT)
    setTimeout(() => { kakaoMap.relayout(); renderKakaoMarkers() }, 100)
  })
}

/* ─── API 로드 ─── */
async function fetchData() {
  if (isDemoBoard.value) {
    vehicles.value = SEED_VEHICLES
    if (!vehicles.value.some(v => v.id === selectedId.value)) {
      selectedId.value = vehicles.value[0]?.id ?? null
    }
    return
  }
  try {
    const [, vRes] = await Promise.all([dashboardApi.getSummary(), dashboardApi.getVehicles()])
    const runtimeVehicles = Array.isArray(vRes.data)
      ? vRes.data.map((v, index) => buildRuntimeVehicle(v, index))
      : []
    vehicles.value = runtimeVehicles
    if (!vehicles.value.some(v => v.id === selectedId.value)) {
      selectedId.value = vehicles.value[0]?.id ?? null
    }
  } catch {
    vehicles.value = []
    selectedId.value = null
  }
}

async function refresh() {
  now.value = new Date().toLocaleString('ko-KR', { hour12: false })
  await fetchData()
}

let timer = null
onMounted(async () => {
  await fetchData()
  await nextTick()
  initKakaoMap()
  timer = setInterval(fetchData, 5000)
})
onUnmounted(() => clearInterval(timer))

/* 마커 재렌더 watch */
watch(selectedId, () => renderKakaoMarkers())
watch(mapTab,     () => renderKakaoMarkers())
watch(vehicles,   () => renderKakaoMarkers())

/* ─── 파생값 ─── */
const selected         = computed(() => vehicles.value.find(v => v.id === selectedId.value) || null)
const selectedDetailId = computed(() => {
  const raw = selected.value?.driveLogId
  if (raw == null || raw === '') return null
  const parsed = Number(raw)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null
})
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

/* ─── KPI Tile 1 ─── */
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

/* ─── KPI Tile 2 ─── */
const maxCont      = computed(() => runningVehicles.value.reduce((m, v) => Math.max(m, v.contMin || 0), 0))
const maxContH     = computed(() => Math.floor(maxCont.value / 60))
const maxContM     = computed(() => maxCont.value % 60)
const contGaugePct = computed(() => Math.min(100, (maxCont.value / 300) * 100))
const contColor    = computed(() => maxCont.value >= 180 ? 'var(--danger)' : maxCont.value >= 90 ? 'var(--warn)' : 'var(--ok)')
const contDelta    = computed(() => maxCont.value >= 180 ? 45 : maxCont.value >= 120 ? 25 : 10)

/* ─── KPI Tile 3 ─── */
const weeklyBase = [1, 2, 0, 2, 3, 1]
const weeklyFull = computed(() => [...weeklyBase, dangerCount.value])
const weeklyMax  = computed(() => Math.max(...weeklyFull.value, 1))
const weeklyMean = 1.43
const weeklyBars = computed(() => {
  const h = 36
  return weeklyFull.value.map((d, i) => {
    const bh = (d / weeklyMax.value) * (h - 4)
    return { x: i * (200 / 7) + 1, y: h - bh, w: (200 / 7) - 4, h: bh, today: i === 6 }
  })
})
const weeklyMeanY = computed(() => 36 - (weeklyMean / weeklyMax.value) * (36 - 4))

/* ─── KPI Tile 5 ─── */
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

/* ─── Map tab counts ─── */
function tabCount(t) {
  if (t === '전체') return runningCount.value
  if (t === '위험')  return dangerCount.value
  if (t === '주의')  return cautionCount.value
  return normalCount.value
}

/* ─── Vehicle row helpers ─── */
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

function goToDriveHistoryDetail() {
  if (!selectedDetailId.value) return
  router.push({ name: 'driveHistoryDetail', params: { id: selectedDetailId.value } })
}

/* ─── Drilldown ─── */
const drillFactors = computed(() => {
  const v = selected.value
  if (!v) return []
  return [
    { label:'연속 운행',       val:(v.contMin||0)/60,  unit:'h', max:5,  thr:[1.5,3],  cLbl:'90분 +10', dLbl:'180분 +45' },
    { label:'일일 누적',       val:(v.dailyMin||0)/60, unit:'h', max:12, thr:[6,10],   cLbl:'6h +15',   dLbl:'10h +45' },
    { label:'야간 (22~06시)', val:(v.nightMin||0)/60, unit:'h', max:4,  thr:[0.5,2],  cLbl:'30분 +10', dLbl:'2h +35' },
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

/* ─── Drive Timeline ─── */
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
const tlPx = t  => 30 + (t / 720) * 700
const tlPy = s  => 10 + (1 - s / 100) * 170
const tlScorePts  = computed(() => timelinePoints.value.map(p => `${tlPx(p.t)},${tlPy(p.score)}`).join(' '))
const tlScoreArea = computed(() => {
  const pts = timelinePoints.value
  if (!pts.length) return ''
  return `${tlPx(0)},182 ${tlScorePts.value} ${tlPx(720)},182`
})
const tlEvents = computed(() => timelinePoints.value.filter(p => p.event))
function tlEventColor(e) { return e === '위험 진입' ? 'var(--danger)' : e === '주의 진입' ? 'var(--warn)' : 'var(--accent)' }

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
    <div class="page-hdr">
      <div>
        <div style="font:600 10.5px/1.4 var(--font-mono);letter-spacing:.14em;text-transform:uppercase;color:var(--accent);margin-bottom:6px;">OPERATIONS / LIVE</div>
        <h1 style="font:800 24px/1.25 var(--font-sans);letter-spacing:-0.02em;color:var(--text-1);margin:0;">관제 대시보드</h1>
        <div style="font-size:12px;color:var(--text-3);margin-top:3px;">오늘 · 2026.05.21 (수) · 한라물류센터 · 가상 시뮬레이션 환경</div>
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

    <!-- ── KPI Strip ── -->
    <div class="kpi-strip">

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

      <!-- Tile 2: MAX CONT. DRIVE -->
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

      <!-- Tile 5: FLEET HEALTH -->
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
              <span :style="`font-family:var(--font-mono);font-size:18px;font-weight:800;color:${avgColor};line-height:1;`">{{ avgScore.toFixed(1) }}</span>
              <span style="font-family:var(--font-mono);font-size:8px;color:var(--text-4);letter-spacing:.1em;margin-top:2px;">AVG /100</span>
            </div>
          </div>
          <div style="display:flex;flex-direction:column;gap:4px;flex:1;min-width:0;">
            <div v-for="r in [{label:'정상',value:normalCount,color:'#5E8A6F'},{label:'주의',value:cautionCount,color:'#C58A3A'},{label:'위험',value:dangerCount,color:'#B5544A'}]"
              :key="r.label" style="display:grid;grid-template-columns:10px 1fr auto;gap:6px;align-items:center;">
              <span :style="`width:10px;height:10px;border-radius:2px;background:${r.color};display:block;`"/>
              <span style="font-size:11px;color:var(--text-2);">{{ r.label }}</span>
              <span :style="`font-family:var(--font-mono);font-size:13px;font-weight:800;color:${r.color};`">{{ r.value }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ── 메인 3컬럼 그리드 ── -->
    <div class="main-grid">

      <!-- ── 좌: FATIGUE BREAKDOWN + DRIVE TIMELINE ── -->
      <div class="left-col">

        <!-- FATIGUE BREAKDOWN 카드 -->
        <div class="card" style="padding:0;overflow:hidden;flex:1;display:flex;flex-direction:column;">

          <!-- 드릴다운 헤더 -->
          <div style="padding:14px 16px;border-bottom:1px solid var(--line-1);display:flex;justify-content:space-between;align-items:center;gap:10px;flex-wrap:wrap;">
            <div style="display:flex;align-items:center;gap:12px;">
              <!-- 번호판 플레이스홀더 -->
              <div style="width:100px;height:66px;border-radius:4px;flex-shrink:0;background:linear-gradient(135deg,#DCDFE4,#B8BFC9);border:1px solid var(--line-3);position:relative;overflow:hidden;">
                <svg width="100%" height="100%" viewBox="0 0 200 130" preserveAspectRatio="xMidYMid slice" style="opacity:.4;display:block;">
                  <rect width="200" height="130" fill="#E3E6EB"/>
                  <rect x="30" y="40" width="120" height="50" fill="#CCD2DA" stroke="#979EAE" stroke-width="1"/>
                  <circle cx="55" cy="98" r="8" fill="#747F95"/>
                  <circle cx="160" cy="98" r="8" fill="#747F95"/>
                </svg>
                <div style="position:absolute;left:50%;bottom:5px;transform:translateX(-50%);background:#fff;border:1px solid rgba(81,95,122,.28);border-radius:2px;padding:2px 5px;font-family:var(--font-mono);font-size:9px;font-weight:700;color:#515F7A;white-space:nowrap;">
                  {{ selected ? selected.plate.split(' ').slice(-1)[0] : '—' }}
                </div>
              </div>
              <div>
                <div style="display:flex;align-items:center;gap:6px;flex-wrap:wrap;margin-bottom:4px;">
                  <span style="font-family:var(--font-mono);font-size:15px;font-weight:800;color:var(--text-1);">{{ selected ? selected.plate : '—' }}</span>
                  <span v-if="selected" :class="levelChipCls(selected.level)">{{ levelLabel(selected.level) }} · {{ selected.score }}점</span>
                  <span v-if="selected" class="chip chip-mute"><AppIcon name="user" :size="10"/> {{ selected.driver }}</span>
                  <span v-if="selected" class="chip chip-info">{{ selectedDetailId ?? '운행 ID 없음' }}</span>
                </div>
                <div style="font-family:var(--font-mono);font-size:10.5px;color:var(--text-3);">
                  {{ selected ? `${selected.loc} · 시작 ${selected.startedAt} · ${selected.spd}km/h · ${selected.type}` : '차량을 선택하세요' }}
                </div>
              </div>
            </div>
            <div style="display:flex;gap:6px;flex-wrap:wrap;">
              <button class="btn btn-ghost" style="font-size:11.5px;"
                :disabled="!selected || selected.level!=='DANGER'"
                :style="{opacity:selected?.level==='DANGER'?1:0.45,cursor:selected?.level==='DANGER'?'pointer':'not-allowed'}">
                <AppIcon name="phone" :size="12"/> 전화 권고
              </button>
              <button class="btn btn-ghost" style="font-size:11.5px;" :disabled="!selected">
                <AppIcon name="coffee" :size="12"/> 휴게 안내
              </button>
              <button class="btn btn-primary" style="font-size:11.5px;"
                :disabled="!selectedDetailId"
                :style="{opacity:selectedDetailId?1:0.45,cursor:selectedDetailId?'pointer':'not-allowed'}"
                @click="goToDriveHistoryDetail">
                <AppIcon name="arrow" :size="12"/> 운행 상세
              </button>
            </div>
          </div>

          <!-- FATIGUE BREAKDOWN 레이블 -->
          <div style="padding:10px 16px 0;display:flex;justify-content:space-between;align-items:center;">
            <div class="label-sm" style="display:flex;align-items:center;gap:8px;"><span class="dot dot-brand"/>FATIGUE BREAKDOWN</div>
            <span style="font-family:var(--font-mono);font-size:9px;color:var(--text-4);letter-spacing:.06em;">score = Σ(factor · weight)</span>
          </div>

          <!-- 피로 요인 목록 -->
          <div style="padding:14px;display:flex;flex-direction:column;gap:10px;flex:1;overflow-y:auto;">
            <div v-if="!selected" style="padding:20px;text-align:center;color:var(--text-4);font-size:12.5px;">차량을 선택하세요</div>
            <div v-for="fb in drillFactors" :key="fb.label"
              :style="`padding:10px 12px;background:var(--bg-2);border:1px solid var(--line-2);border-radius:var(--r-sm);border-top:2px solid ${fbColor(fb.val,fb.thr)};`">
              <div style="display:flex;justify-content:space-between;align-items:flex-end;">
                <div>
                  <div style="font-size:12px;color:var(--text-2);font-weight:600;">{{ fb.label }}</div>
                  <div style="font-family:var(--font-mono);font-size:9.5px;color:var(--text-4);margin-top:1px;">max {{ fb.max }}{{ fb.unit }} · 임계 {{ fb.thr[0] }} / {{ fb.thr[1] }}{{ fb.unit }}</div>
                </div>
                <div style="display:flex;align-items:baseline;gap:4px;">
                  <span :style="`font-family:var(--font-mono);font-size:20px;font-weight:800;color:${fbColor(fb.val,fb.thr)};line-height:1;`">{{ (Math.round(fb.val*10)/10).toFixed(1) }}</span>
                  <span style="font-family:var(--font-mono);font-size:10px;color:var(--text-4);">{{ fb.unit }}</span>
                </div>
              </div>
              <div class="fb-bar-wrap">
                <div class="fb-bar-fill" :style="`width:${fbPct(fb.val,fb.max)}%;`"/>
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

          <!-- REST EVENTS -->
          <div style="padding:12px 16px;border-top:1px solid var(--line-1);">
            <div class="label-sm" style="margin-bottom:8px;">REST EVENTS</div>
            <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:5px;">
              <div v-for="r in restEvents" :key="r.label"
                :style="`padding:8px;border-radius:4px;background:var(--bg-2);border:1px solid var(--line-2);border-top:2px solid ${r.color};`">
                <div :style="`font-family:var(--font-mono);font-size:9px;letter-spacing:.1em;font-weight:700;color:${r.color};`">{{ r.label }}</div>
                <div :style="`font-family:var(--font-mono);font-size:22px;font-weight:800;margin-top:2px;letter-spacing:-0.015em;color:${r.count>0?'var(--text-1)':'var(--text-4)'};`">{{ r.count }}</div>
                <div style="font-family:var(--font-mono);font-size:9px;color:var(--text-4);margin-top:1px;">{{ r.hint }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- DRIVE TIMELINE 카드 -->
        <div class="card" style="padding:0;overflow:hidden;">
          <div style="padding:14px 16px;border-bottom:1px solid var(--line-1);display:flex;justify-content:space-between;align-items:center;">
            <div>
              <div class="label-sm">DRIVE TIMELINE</div>
              <div style="font-size:11.5px;color:var(--text-2);margin-top:2px;">시간대별 누적 피로 점수</div>
            </div>
            <div style="display:flex;align-items:baseline;gap:10px;">
              <div style="text-align:right;">
                <div class="label-sm">현재</div>
                <div style="font-family:var(--font-mono);font-size:22px;font-weight:700;color:var(--text-1);line-height:1;">{{ selected ? selected.score : '—' }}</div>
              </div>
              <div style="border-left:1px solid var(--line-2);padding-left:10px;text-align:right;">
                <div class="label-sm">최고</div>
                <div style="font-family:var(--font-mono);font-size:22px;font-weight:800;color:var(--text-1);line-height:1;">{{ selected ? peakScore : '—' }}</div>
              </div>
            </div>
          </div>
          <div style="padding:12px 14px;">
            <svg width="100%" height="160" viewBox="0 0 760 200" preserveAspectRatio="none">
              <rect x="30" y="10" width="720" height="66" fill="rgba(181,84,74,.07)"/>
              <rect x="30" y="76" width="720" height="54" fill="rgba(197,138,58,.07)"/>
              <rect x="30" y="130" width="720" height="52" fill="rgba(94,138,111,.06)"/>
              <text x="748" y="22"  text-anchor="end" font-family="var(--font-mono)" font-size="9" fill="var(--danger)">위험≥70</text>
              <text x="748" y="96"  text-anchor="end" font-family="var(--font-mono)" font-size="9" fill="var(--warn)">주의40~69</text>
              <text x="748" y="144" text-anchor="end" font-family="var(--font-mono)" font-size="9" fill="var(--ok)">정상≤39</text>
              <line x1="30" y1="10"  x2="750" y2="10"  stroke="var(--line-1)" stroke-dasharray="2 4"/>
              <line x1="30" y1="55"  x2="750" y2="55"  stroke="var(--line-1)" stroke-dasharray="2 4"/>
              <line x1="30" y1="100" x2="750" y2="100" stroke="var(--line-1)" stroke-dasharray="2 4"/>
              <line x1="30" y1="145" x2="750" y2="145" stroke="var(--line-1)" stroke-dasharray="2 4"/>
              <line x1="30" y1="182" x2="750" y2="182" stroke="var(--line-1)" stroke-dasharray="2 4"/>
              <polyline v-if="selected" :points="tlScoreArea" fill="rgba(81,95,122,.18)" stroke="none"/>
              <polyline v-if="selected" :points="tlScorePts" fill="none" stroke="var(--accent)" stroke-width="2"/>
              <g v-for="(p,i) in tlEvents" :key="i">
                <line :x1="tlPx(p.t)" y1="10" :x2="tlPx(p.t)" y2="182" stroke="var(--accent)" stroke-dasharray="2 4" opacity=".35"/>
                <circle :cx="tlPx(p.t)" :cy="tlPy(p.score)" r="4" :fill="tlEventColor(p.event)" stroke="var(--bg-1)" stroke-width="2"/>
                <text :x="tlPx(p.t)+6" :y="tlPy(p.score)-8" font-family="var(--font-mono)" font-size="9" fill="var(--text-2)">{{ p.event }}</text>
              </g>
              <text x="30"  y="196" text-anchor="middle" font-family="var(--font-mono)" font-size="9" fill="var(--text-4)">0h</text>
              <text x="150" y="196" text-anchor="middle" font-family="var(--font-mono)" font-size="9" fill="var(--text-4)">+2h</text>
              <text x="270" y="196" text-anchor="middle" font-family="var(--font-mono)" font-size="9" fill="var(--text-4)">+4h</text>
              <text x="390" y="196" text-anchor="middle" font-family="var(--font-mono)" font-size="9" fill="var(--text-4)">+6h</text>
              <text x="510" y="196" text-anchor="middle" font-family="var(--font-mono)" font-size="9" fill="var(--text-4)">+8h</text>
              <text x="630" y="196" text-anchor="middle" font-family="var(--font-mono)" font-size="9" fill="var(--text-4)">+10h</text>
              <text x="750" y="196" text-anchor="middle" font-family="var(--font-mono)" font-size="9" fill="var(--text-4)">+12h</text>
            </svg>
          </div>
        </div>

      </div><!-- /left-col -->

      <!-- ── 중: FLEET MAP + RUNNING VEHICLES ── -->
      <div class="center-col">

        <!-- Fleet Map 카드 -->
        <div class="card" style="padding:0;overflow:hidden;flex:1;display:flex;flex-direction:column;">
          <div style="padding:12px 14px;border-bottom:1px solid var(--line-1);display:flex;justify-content:space-between;align-items:center;gap:8px;flex-wrap:wrap;">
            <div>
              <div class="label-sm" style="display:flex;align-items:center;gap:6px;"><span class="dot dot-brand blink"/>FLEET MAP · LIVE</div>
              <div style="font-size:11.5px;color:var(--text-2);margin-top:2px;">운행 <strong style="color:var(--text-1);">{{ runningCount }}</strong>대 / 전체 {{ vehicles.length }}대</div>
            </div>
            <div class="map-tabs">
              <button v-for="t in mapTabs" :key="t" class="map-tab" :class="{active: mapTab===t}" @click="mapTab=t">
                {{ t }} <span>{{ tabCount(t) }}</span>
              </button>
            </div>
          </div>

          <!-- Kakao Maps 컨테이너 -->
          <div style="position:relative;flex:1;display:flex;flex-direction:column;min-height:300px;">
            <div ref="mapContainer" style="width:100%;flex:1;min-height:320px;overflow:hidden;"></div>
            <!-- 운행 현황 오버레이 -->
            <div style="position:absolute;top:12px;left:12px;display:flex;flex-direction:column;gap:5px;z-index:10;pointer-events:none;">
              <span class="chip chip-info"><span class="dot dot-brand"/>{{ runningCount }}대 운행중</span>
              <span v-if="dangerCount>0" class="chip chip-danger"><span class="dot dot-danger"/>{{ dangerCount }} 위험</span>
              <span v-if="cautionCount>0" class="chip chip-warn"><span class="dot dot-warn"/>{{ cautionCount }} 주의</span>
            </div>
            <!-- 범례 -->
            <div style="position:absolute;bottom:20px;right:10px;background:rgba(241,243,246,.92);border:1px solid var(--line-2);border-radius:var(--r-md);padding:7px 10px;font-family:var(--font-mono);font-size:9.5px;display:flex;flex-direction:column;gap:4px;backdrop-filter:blur(4px);z-index:10;pointer-events:none;">
              <div style="font-size:8px;letter-spacing:.12em;color:var(--text-4);font-weight:700;">LEGEND</div>
              <div style="display:flex;gap:5px;align-items:center;"><span style="width:7px;height:7px;border-radius:50%;background:#B5544A;display:inline-block;"/>위험 (70+)</div>
              <div style="display:flex;gap:5px;align-items:center;"><span style="width:7px;height:7px;border-radius:50%;background:#C58A3A;display:inline-block;"/>주의 (40~69)</div>
              <div style="display:flex;gap:5px;align-items:center;"><span style="width:7px;height:7px;border-radius:50%;background:#515F7A;display:inline-block;"/>정상 (0~39)</div>
            </div>
          </div>
        </div>

        <!-- Running Vehicles 카드 -->
        <div class="card" style="padding:0;overflow:hidden;">
          <div style="padding:10px 14px;border-bottom:1px solid var(--line-1);display:flex;justify-content:space-between;align-items:center;">
            <div class="label-sm">RUNNING VEHICLES · {{ sortedFiltered.length }}대</div>
            <span style="font-family:var(--font-mono);font-size:10.5px;color:var(--text-4);">점수 높은 순</span>
          </div>
          <div style="padding:10px;display:flex;flex-direction:column;gap:6px;max-height:300px;overflow-y:auto;">
            <div v-if="sortedFiltered.length===0" style="padding:20px;text-align:center;color:var(--text-4);font-size:12px;">해당 등급의 차량이 없습니다</div>
            <div v-for="v in sortedFiltered" :key="v.id" class="vrow" :class="{selected: selectedId===v.id}" @click="selectedId=v.id">
              <!-- 번호판 -->
              <div class="vrow-plate">
                <svg width="100%" height="100%" viewBox="0 0 200 130" preserveAspectRatio="xMidYMid slice" style="opacity:.4;display:block;">
                  <rect width="200" height="130" fill="#E3E6EB"/>
                  <rect x="30" y="40" width="120" height="50" fill="#CCD2DA" stroke="#979EAE" stroke-width="1"/>
                  <circle cx="55" cy="98" r="8" fill="#747F95"/>
                  <circle cx="160" cy="98" r="8" fill="#747F95"/>
                </svg>
                <div style="position:absolute;left:50%;bottom:5px;transform:translateX(-50%);background:#fff;border:1px solid rgba(81,95,122,.28);border-radius:2px;padding:2px 5px;font-family:var(--font-mono);font-size:9px;font-weight:700;color:#515F7A;white-space:nowrap;">{{ v.plate.split(' ').slice(-1)[0] }}</div>
              </div>
              <!-- 중앙 정보 -->
              <div style="min-width:0;display:flex;flex-direction:column;gap:4px;justify-content:space-between;">
                <div style="display:flex;gap:5px;align-items:center;flex-wrap:wrap;">
                  <span style="font-family:var(--font-mono);font-size:11px;font-weight:700;">{{ v.plate }}</span>
                  <span :class="levelChipCls(v.level)">{{ levelLabel(v.level) }} {{ v.score }}</span>
                </div>
                <div style="font-size:10px;color:var(--text-3);font-family:var(--font-mono);">{{ v.driver }} · {{ v.loc.replace(/^.*?· /,'') }}</div>
                <div style="display:flex;flex-direction:column;gap:2px;">
                  <div v-for="f in vFactors" :key="f.key" style="display:grid;grid-template-columns:30px 1fr 26px;gap:4px;align-items:center;">
                    <span style="font-family:var(--font-mono);font-size:8px;color:var(--text-4);">{{ f.key }}</span>
                    <div style="position:relative;height:4px;background:var(--bg-3);border-radius:2px;overflow:hidden;">
                      <div :style="`position:absolute;top:0;left:0;height:100%;width:${fbPct(factorVal(v,f.key),f.max)}%;background:${fbColor(factorVal(v,f.key),f.thr)};`"/>
                      <div :style="`position:absolute;top:-1px;bottom:-1px;left:${(f.thr[0]/f.max)*100}%;width:1px;background:var(--warn);`"/>
                      <div :style="`position:absolute;top:-1px;bottom:-1px;left:${(f.thr[1]/f.max)*100}%;width:1px;background:var(--danger);`"/>
                    </div>
                    <span :style="`font-family:var(--font-mono);font-size:8px;color:${fbColor(factorVal(v,f.key),f.thr)};font-weight:700;text-align:right;`">{{ factorVal(v,f.key)===0?'—':fmtHM(factorVal(v,f.key)) }}</span>
                  </div>
                </div>
              </div>
              <!-- 우측 점수 -->
              <div style="display:flex;flex-direction:column;align-items:flex-end;justify-content:space-between;min-width:70px;">
                <div>
                  <span :style="`font-family:var(--font-mono);font-size:20px;font-weight:800;color:${vSparkColor(v)};line-height:1;`">{{ v.score }}</span>
                  <span style="font-size:9px;color:var(--text-4);font-family:var(--font-mono);">/100</span>
                </div>
                <svg width="70" height="22" style="display:block;">
                  <polyline :points="vSparkPts(v)" fill="none" :stroke="vSparkColor(v)" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle :cx="vSparkEnd(v).cx" :cy="vSparkEnd(v).cy" r="2" :fill="vSparkColor(v)"/>
                </svg>
                <span :style="`font-family:var(--font-mono);font-size:9px;color:${vDelta(v).color};font-weight:700;`">{{ vDelta(v).txt }}</span>
              </div>
            </div>
          </div>
        </div>

      </div><!-- /center-col -->

      <!-- ── 우: ACTIVE ALERTS + EVENT STREAM ── -->
      <div class="right-col">

        <!-- ACTIVE ALERTS -->
        <div class="card" style="padding:0;overflow:hidden;display:flex;flex-direction:column;flex:1;">
          <div style="padding:12px 16px;border-bottom:1px solid var(--line-1);display:flex;justify-content:space-between;align-items:center;">
            <div class="label-sm" style="display:flex;align-items:center;gap:8px;"><span class="dot dot-danger pulse-ring"/>ACTIVE ALERTS</div>
            <span class="chip chip-danger">{{ alertList.length }} active</span>
          </div>
          <div style="padding:10px;display:flex;flex-direction:column;gap:6px;flex:1;overflow-y:auto;">
            <div v-if="alertList.length===0" style="padding:20px;text-align:center;color:var(--text-4);font-size:12.5px;">현재 활성 알림이 없습니다</div>
            <div v-for="a in alertList" :key="a.id" @click="selectedId=a.id"
              :style="`padding:10px 12px;border-left:3px solid var(--${a.sev});background:var(--${a.sev}-soft);border-radius:0 4px 4px 0;cursor:pointer;`">
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

        <!-- EVENT STREAM -->
        <div class="card" style="padding:0;overflow:hidden;display:flex;flex-direction:column;flex:1;">
          <div style="padding:12px 16px;border-bottom:1px solid var(--line-1);display:flex;justify-content:space-between;align-items:center;">
            <div>
              <div class="label-sm" style="display:flex;align-items:center;gap:8px;"><span class="dot dot-brand blink"/>EVENT STREAM</div>
              <div style="font-size:11.5px;color:var(--text-2);margin-top:2px;">시간 역순</div>
            </div>
            <span class="chip chip-info">{{ allEvents.length }} events</span>
          </div>
          <div style="flex:1;overflow-y:auto;">
            <table style="width:100%;border-collapse:collapse;font-size:12px;">
              <tbody>
                <tr v-for="(e,i) in allEvents" :key="i"
                  @click="()=>{ const id=eventVehicleId(e.plate); if(id) selectedId=id; }"
                  :style="{cursor:eventVehicleId(e.plate)?'pointer':'default',borderBottom:i<allEvents.length-1?'1px solid var(--line-1)':'none'}"
                  @mouseenter="ev=>ev.currentTarget.style.background='var(--bg-2)'"
                  @mouseleave="ev=>ev.currentTarget.style.background='transparent'">
                  <td style="padding:8px 10px;white-space:nowrap;vertical-align:middle;width:60px;">
                    <div style="display:flex;align-items:center;gap:5px;">
                      <span :style="`width:6px;height:6px;border-radius:50%;background:${evColor(e.kind)};flex-shrink:0;display:inline-block;`"/>
                      <span style="font-family:var(--font-mono);font-size:10px;color:var(--text-3);">{{ e.t }}</span>
                    </div>
                  </td>
                  <td style="padding:8px 10px;white-space:nowrap;vertical-align:middle;">
                    <span :style="`font-family:var(--font-mono);font-size:11px;font-weight:700;color:${evColor(e.kind)};`">{{ e.plate }}</span>
                  </td>
                  <td style="padding:8px 10px;vertical-align:middle;color:var(--text-2);font-size:11px;">{{ e.text }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

      </div><!-- /right-col -->
    </div><!-- /main-grid -->

    <!-- ── 하단: DRIVER RANKING + HEATMAP ── -->
    <div class="bottom-grid">

      <!-- Driver Ranking -->
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
                <div :style="`width:${it.value}%;height:100%;background:${it.color};transition:width .4s;`"/>
                <div style="position:absolute;top:50%;left:8px;transform:translateY(-50%);font-family:var(--font-mono);font-size:10px;color:var(--bg-1);font-weight:700;">{{ it.value }}</div>
              </div>
              <span :style="`font-family:var(--font-mono);font-size:11px;font-weight:700;color:${it.color};text-align:right;`">{{ it.tag }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Heatmap -->
      <div class="card" style="padding:18px;">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
          <div>
            <div class="label-sm">DRIVER × HOUR HEATMAP</div>
            <div style="font-size:12.5px;color:var(--text-2);margin-top:2px;">오늘 시간대별 누적 피로 점수</div>
          </div>
          <span class="chip chip-mute">2026.05.21</span>
        </div>
        <div style="display:flex;flex-direction:column;gap:6px;">
          <div style="display:grid;grid-template-columns:100px repeat(24,1fr);gap:2px;font-family:var(--font-mono);font-size:9px;color:var(--text-4);">
            <div/>
            <div v-for="h in 24" :key="h" style="text-align:center;">{{ String(h-1).padStart(2,'0') }}</div>
          </div>
          <div v-for="d in heatmapDrivers" :key="d.plate" style="display:grid;grid-template-columns:100px repeat(24,1fr);gap:2px;align-items:center;">
            <div style="font-size:11px;">
              <div style="font-weight:600;color:var(--text-1);">{{ d.name }}</div>
              <div style="font-family:var(--font-mono);color:var(--text-4);font-size:9px;margin-top:1px;">{{ d.plate }}</div>
            </div>
            <div v-for="(s,i) in d.hours" :key="i"
              :style="`height:20px;border-radius:2px;background:${heatColor(s)};`"
              :title="`${i}:00 · ${s===null?'운행 없음':s+'점'}`"/>
          </div>
        </div>
        <div style="margin-top:8px;display:flex;align-items:center;gap:8px;font-family:var(--font-mono);font-size:10px;color:var(--text-3);">
          <span>0 (안전)</span>
          <div style="display:flex;gap:2px;">
            <span v-for="c in ['var(--bg-2)','#5E8A6F','#92A08F','#C5A56A','#C58A3A','#B5544A']" :key="c"
              :style="`width:16px;height:12px;background:${c};${c==='var(--bg-2)'?'border:1px solid var(--line-2);':''}display:inline-block;`"/>
          </div>
          <span>100 (위험)</span>
        </div>
      </div>

    </div><!-- /bottom-grid -->

  </div>
</template>

<style scoped>
.fade-up { animation: fadeUp 0.4s ease both; }
@keyframes fadeUp { from { opacity:0; transform:translateY(8px); } to { opacity:1; transform:translateY(0); } }

/* 페이지 */
.view { padding: 20px 8px 32px; max-width:100%; }

/* 페이지 헤더 */
.page-hdr { display:flex; justify-content:space-between; align-items:flex-end; margin-bottom:14px; gap:16px; flex-wrap:wrap; }

/* KPI Strip */
.kpi-strip {
  background: var(--bg-1);
  border: 1px solid var(--line-2);
  border-radius: 14px;
  overflow: hidden;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  box-shadow: var(--shadow-1);
  margin-bottom: 14px;
}
.kpi-tile { padding: 14px 16px; border-right: 1px solid var(--line-1); }
.kpi-tile:last-child { border-right: none; }
.tile-hdr   { display:flex; justify-content:space-between; align-items:center; }
.tile-label { font: 600 9.5px/1 var(--font-mono); letter-spacing: .14em; text-transform: uppercase; color: var(--text-4); }
.tile-meta  { font-family: var(--font-mono); font-size: 9px; color: var(--text-4); }
.tile-val   { display:flex; align-items:baseline; gap:4px; margin-top:8px; }
.tile-delta { margin-left:auto; font-family:var(--font-mono); font-size:10.5px; padding:2px 5px; border-radius:3px; }
.tile-delta.bad  { color:var(--danger); background:var(--danger-soft); }
.tile-delta.good { color:var(--ok);     background:var(--ok-soft); }

/* 메인 3컬럼 그리드 */
.main-grid {
  display: grid;
  grid-template-columns: 35fr 25fr 35fr;
  gap: 14px;
  margin-bottom: 14px;
  align-items: stretch;
}
.left-col   { display:flex; flex-direction:column; gap:12px; height:100%; }
.center-col { display:flex; flex-direction:column; gap:12px; height:100%; }
.right-col  { display:flex; flex-direction:column; gap:12px; height:100%; }

/* 하단 그리드 */
.bottom-grid { display:grid; grid-template-columns:1fr 1fr; gap:14px; margin-bottom:24px; }

/* 지도 탭 */
.map-tabs { display:flex; gap:5px; }
.map-tab {
  padding: 5px 10px;
  font-size: 11px;
  font-family: var(--font-mono);
  border-radius: var(--r-sm);
  cursor: pointer;
  border: 1px solid var(--line-2);
  background: var(--bg-2);
  color: var(--text-3);
  font-weight: 500;
  transition: all .12s;
}
.map-tab.active {
  background: var(--accent-soft);
  color: var(--accent);
  border-color: var(--accent-line);
  font-weight: 700;
}

/* 차량 행 */
.vrow {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 10px;
  align-items: stretch;
  padding: 9px;
  cursor: pointer;
  border-radius: var(--r-sm);
  background: var(--bg-2);
  border: 1px solid var(--line-2);
  transition: background .12s, border-color .12s;
}
.vrow:hover, .vrow.selected {
  background: var(--accent-soft);
  border-color: var(--accent-line);
}
.vrow-plate {
  width: 70px; height: 52px;
  border-radius: 4px; flex-shrink: 0;
  background: linear-gradient(135deg, #DCDFE4, #B8BFC9);
  border: 1px solid var(--line-3);
  position: relative; overflow: hidden;
}

/* 피로 바 */
.fb-bar-wrap { position:relative; height:10px; background:var(--bg-3); border-radius:5px; margin-top:8px; overflow:hidden; }
.fb-bar-fill { position:absolute; top:0; left:0; height:100%; background:linear-gradient(90deg,var(--ok),var(--warn) 50%,var(--danger) 85%); transition:width .4s; }

/* label-sm */
.label-sm { font-family:var(--font-mono); font-size:10.5px; letter-spacing:.12em; text-transform:uppercase; color:var(--text-3); font-weight:600; }

/* chips */
.chip { display:inline-flex; align-items:center; gap:5px; padding:3px 8px; border-radius:999px; font-size:11px; font-weight:500; border:1px solid transparent; font-family:var(--font-mono); }
.chip-ok     { background:var(--ok-soft);     color:var(--ok);     border-color:rgba(138,186,154,.25); }
.chip-warn   { background:var(--warn-soft);   color:var(--warn);   border-color:rgba(214,181,106,.25); }
.chip-danger { background:var(--danger-soft); color:var(--danger); border-color:rgba(209,139,126,.25); }
.chip-info   { background:var(--info-soft);   color:var(--info);   border-color:var(--accent-line); }
.chip-mute   { background:var(--bg-3);        color:var(--text-3); border-color:var(--line-2); }

/* dots */
.dot        { width:6px; height:6px; border-radius:50%; display:inline-block; flex-shrink:0; }
.dot-ok     { background:var(--ok);     box-shadow:0 0 0 3px var(--ok-soft); }
.dot-danger { background:var(--danger); box-shadow:0 0 0 3px var(--danger-soft); }
.dot-warn   { background:var(--warn);   box-shadow:0 0 0 3px var(--warn-soft); }
.dot-brand  { background:var(--accent); box-shadow:0 0 0 3px var(--accent-soft); }

/* 애니메이션 */
.blink { animation: blink-soft 2s infinite; }
@keyframes blink-soft { 0%,100%{ opacity:1; } 50%{ opacity:.55; } }
.pulse-ring { animation: pulse-ring 2s infinite; }
@keyframes pulse-ring { 0%{box-shadow:0 0 0 0 rgba(209,139,126,.45);} 70%{box-shadow:0 0 0 8px rgba(209,139,126,0);} 100%{box-shadow:0 0 0 0 rgba(209,139,126,0);} }
@keyframes kakao-pulse { 0%{opacity:.55;} 70%{opacity:0;} 100%{opacity:0;} }

/* card */
.card { background:var(--bg-1); border:1px solid var(--line-2); border-radius:var(--r-md); box-shadow:var(--shadow-1); }

/* 버튼 */
.btn { display:inline-flex; align-items:center; gap:5px; padding:6px 12px; border-radius:var(--r-sm); font-size:12.5px; font-weight:500; cursor:pointer; border:none; font-family:var(--font-sans); transition:background .12s; }
.btn-primary { background:var(--accent); color:#fff; }
.btn-primary:hover { background:var(--accent-hover); }
.btn-ghost { background:var(--bg-2); color:var(--text-2); border:1px solid var(--line-2); }
.btn-ghost:hover { background:var(--bg-3); }
</style>
