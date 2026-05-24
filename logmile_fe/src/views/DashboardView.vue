<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppIcon from '@/components/common/AppIcon.vue'
import { dashboardApi } from '@/api/dashboardApi'
import { fatigueStatsApi } from '@/api/fatigueStatsApi'
import { driveHistoryApi } from '@/api/driveHistoryApi'

const route = useRoute()
const router = useRouter()

/* ─── 시드 데이터 (lat/lng 추가) ─── */
const SEED_VEHICLES = [
  { id:1,  plate:'경기 80바 1024', driver:'김민수', phone:'010-3821-4507', type:'카고 5톤',    score:78, level:'DANGER',  status:'RUNNING', spd:87, contMin:384, dailyMin:672, nightMin:204, restValid:1, restSuff:0, restInvalid:1, restMiss:2, loc:'경부고속 · 안성IC',  startedAt:'03:18', scenario:'C', driveLogId:1,  recognizedPlate:'01누 5936', lat:37.0078, lng:127.2714 },
  { id:2,  plate:'경기 80바 1025', driver:'박정호', phone:'010-5512-8834', type:'윙바디 11톤', score:56, level:'CAUTION', status:'RUNNING', spd:78, contMin:270, dailyMin:480, nightMin:108, restValid:2, restSuff:0, restInvalid:0, restMiss:1, loc:'서해안 · 서산IC',    startedAt:'06:11', scenario:'B', driveLogId:10, recognizedPlate:'01다 0673', lat:36.7842, lng:126.4502 },
  { id:3,  plate:'경기 80바 1026', driver:'이영준', phone:'010-7743-2291', type:'카고 4.5톤',  score:18, level:'NORMAL',  status:'RUNNING', spd:92, contMin:108, dailyMin:186, nightMin:0,   restValid:0, restSuff:1, restInvalid:0, restMiss:0, loc:'중부고속 · 음성IC',  startedAt:'11:02', scenario:'A', driveLogId:11, recognizedPlate:null,        lat:36.9357, lng:127.6882 },
  { id:4,  plate:'경기 80바 1027', driver:'최성훈', phone:'010-2267-9104', type:'윙바디 8톤',  score:32, level:'NORMAL',  status:'RUNNING', spd:81, contMin:180, dailyMin:342, nightMin:30,  restValid:1, restSuff:1, restInvalid:0, restMiss:0, loc:'영동고속 · 여주IC',  startedAt:'08:50', scenario:'A', driveLogId:12, recognizedPlate:'01도 4663', lat:37.2986, lng:127.6374 },
  { id:5,  plate:'경기 80바 1028', driver:'정우석', phone:'010-9934-6612', type:'카고 5톤',    score:12, level:'NORMAL',  status:'RUNNING', spd:76, contMin:60,  dailyMin:114, nightMin:0,   restValid:0, restSuff:0, restInvalid:0, restMiss:0, loc:'남해고속 · 진주IC',  startedAt:'12:39', scenario:'A', driveLogId:13, recognizedPlate:'01스 4684', lat:35.1797, lng:128.1077 },
  { id:6,  plate:'경기 80바 1029', driver:'강지훈', phone:'010-4418-7753', type:'카고 5톤',    score:84, level:'DANGER',  status:'RUNNING', spd:64, contMin:342, dailyMin:618, nightMin:168, restValid:1, restSuff:0, restInvalid:1, restMiss:2, loc:'중부내륙 · 점촌IC',  startedAt:'04:02', scenario:'C', driveLogId:null, recognizedPlate:null,       lat:36.3933, lng:128.1968 },
  { id:7,  plate:'경기 80바 1030', driver:'한승연', phone:'010-6629-3380', type:'카고 2.5톤',  score:48, level:'CAUTION', status:'RUNNING', spd:71, contMin:228, dailyMin:384, nightMin:72,  restValid:1, restSuff:0, restInvalid:0, restMiss:1, loc:'서울외곽 · 송내IC',  startedAt:'07:45', scenario:'B', driveLogId:null, recognizedPlate:null,       lat:37.4875, lng:126.7657 },
  { id:8,  plate:'경기 80바 1031', driver:'조영민', phone:'010-8801-5547', type:'윙바디 11톤', score:41, level:'CAUTION', status:'RUNNING', spd:84, contMin:252, dailyMin:426, nightMin:48,  restValid:1, restSuff:0, restInvalid:0, restMiss:1, loc:'서해안 · 당진IC',    startedAt:'06:50', scenario:'B', driveLogId:null, recognizedPlate:null,       lat:36.8897, lng:126.6453 },
  { id:9,  plate:'경기 80바 1032', driver:'윤태경', phone:'010-1193-4428', type:'카고 5톤',    score:0,  level:'NORMAL',  status:'IDLE',    spd:0,  contMin:0,   dailyMin:0,   nightMin:0,   restValid:0, restSuff:0, restInvalid:0, restMiss:0, loc:'한라물류 · 차고지',  startedAt:'—',     scenario:'—', driveLogId:null, lat:37.5665, lng:126.9780 },
  { id:10, plate:'경기 80바 1033', driver:'서동현', phone:'010-3375-8819', type:'윙바디 8톤',  score:0,  level:'NORMAL',  status:'IDLE',    spd:0,  contMin:0,   dailyMin:0,   nightMin:0,   restValid:0, restSuff:0, restInvalid:0, restMiss:0, loc:'한라물류 · 차고지',  startedAt:'—',     scenario:'—', driveLogId:null, lat:37.5665, lng:126.9780 },
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
function todayDateValue() { return new Date().toLocaleDateString('en-CA', { timeZone: 'Asia/Seoul' }) }
function formatSelectedDateLabel(value) {
  if (!value) return '날짜 미선택'
  const [year, month, day] = value.split('-')
  return `${year}.${month}.${day}`
}
function formatCsvDateTime(value) {
  if (!value) return '—'
  const text = String(value)
  if (text.includes('T')) return text.slice(0, 16).replace('T', ' ')
  return text
}
function actionTimeLabel(value) {
  if (!value) return ''
  return formatCsvDateTime(value)
}
function escapeCsvCell(value) {
  const text = value == null ? '' : String(value)
  return `"${text.replace(/"/g, '""')}"`
}
function csvTabSuffix(tab) {
  if (tab === '위험') return 'danger'
  if (tab === '주의') return 'caution'
  if (tab === '정상') return 'normal'
  return 'all'
}

function buildRuntimeVehicle(apiVehicle, index) {
  const preset = DEMO_RUNNING_PRESETS[index % DEMO_RUNNING_PRESETS.length] || DEMO_RUNNING_PRESETS[0]
  const score = apiVehicle.fatigueScore ?? preset.score
  const level = apiVehicle.fatigueLevel ?? preset.level

  // #2: 실제 운행 시간 사용, 없으면 점수 기반 추정
  const contMin  = apiVehicle.continuousDrivingMinutes  ??
    (level === 'DANGER' ? Math.max(240, 180 + score * 2) :
     level === 'CAUTION' ? Math.max(120, 90 + score * 2) :
     Math.max(30, score * 3))
  const dailyMin = apiVehicle.dailyTotalDrivingMinutes  ??
    Math.max(contMin, contMin + 120 + index * 18)
  const nightMin = apiVehicle.nightDrivingMinutes       ??
    (level === 'DANGER' ? Math.max(30, Math.round(contMin * 0.35)) :
     level === 'CAUTION' ? Math.round(contMin * 0.2) : Math.round(contMin * 0.08))

  return {
    ...preset,
    id: apiVehicle.vehicleId ?? apiVehicle.driveLogId ?? preset.id,
    vehicleId: apiVehicle.vehicleId ?? preset.id,
    plate: apiVehicle.plateNo ?? preset.plate,
    driver: apiVehicle.driverName ?? preset.driver,
    type: apiVehicle.vehicleType ?? preset.type,
    score,
    level,
    status: apiVehicle.status ?? 'RUNNING',
    contMin, dailyMin, nightMin,
    // #4: restEvents는 selectedDetail에서 동적으로 계산 (buildRuntimeVehicle에서 제거)
    restValid: 0, restSuff: 0, restInvalid: 0, restMiss: 0,
    startedAt: startedAtLabel(apiVehicle.startedAt, preset.startedAt),
    startedAtRaw: apiVehicle.startedAt ?? null,
    driveLogId: apiVehicle.driveLogId ?? null,
    driverPhone: apiVehicle.driverPhone ?? preset.phone ?? null,
    restGuideCount: apiVehicle.restGuideCount ?? 0,
    lastRestGuideAt: apiVehicle.lastRestGuideAt ?? null,
    lastPhoneRecommendationAt: apiVehicle.lastPhoneRecommendationAt ?? null,
    lat: preset.lat, lng: preset.lng,
    recognizedPlate: apiVehicle.recognizedPlateNo ?? null,
  }
}

/* ─── State ─── */
const now        = ref(new Date().toLocaleString('ko-KR', { hour12: false }))
const selectedDate = ref(todayDateValue())
const mapTab     = ref('전체')
const mapTabs    = ['전체', '위험', '주의', '정상']
const vehicles   = ref([])
const selectedId = ref(null)
const actionSubmitting = ref({ rest: false, phone: false })
const phoneModal       = ref(null)   // null | { driver, phone, plate }
const smsToast         = ref(null)   // null | { driver, phone, message, count }
const localEvents      = ref([])     // SMS 시뮬레이션 이벤트 누적
const isDemoBoard = computed(() => route.name === 'demoBoard')

// 요약 API 실데이터 (#7)
const summaryData = ref(null)
// 주간 DANGER 통계 (#1)
const weeklyStats = ref([])
// 선택 차량 상세 (fatigueEvents + restEvents) (#4, #5)
const selectedDetail = ref(null)
// EVENT STREAM 실데이터 (#3)
const apiEvents = ref([])

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

  filteredVehicles.value.forEach(v => {
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
      center: new window.kakao.maps.LatLng(36.0, 127.7),
      level: 12,
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
    const params = { date: selectedDate.value }
    const [sRes, vRes, eRes] = await Promise.all([
      dashboardApi.getSummary(params),
      dashboardApi.getVehicles(params),
      dashboardApi.getEvents(params),
    ])
    // #7: summary 실데이터 저장
    summaryData.value = sRes.data ?? null
    // vehicles
    const runtimeVehicles = Array.isArray(vRes.data)
      ? vRes.data.map((v, index) => buildRuntimeVehicle(v, index))
      : []
    vehicles.value = runtimeVehicles
    if (!vehicles.value.some(v => v.id === selectedId.value)) {
      selectedId.value = vehicles.value[0]?.id ?? null
    }
    // #3: 이벤트 스트림 실데이터
    apiEvents.value = Array.isArray(eRes.data) ? eRes.data : []
  } catch {
    vehicles.value = []
    selectedId.value = null
    summaryData.value = null
    apiEvents.value = []
  }
}

async function fetchWeeklyStats() {
  if (isDemoBoard.value) return
  try {
    const res = await fatigueStatsApi.getStats({ days: 7 })
    weeklyStats.value = Array.isArray(res.data) ? res.data : []
  } catch {
    weeklyStats.value = []
  }
}

async function refresh() {
  now.value = new Date().toLocaleString('ko-KR', { hour12: false })
  await fetchData()
}

let timer = null
onMounted(async () => {
  await Promise.all([fetchData(), fetchWeeklyStats()])
  await nextTick()
  initKakaoMap()
  timer = setInterval(fetchData, 5000)
})
onUnmounted(() => clearInterval(timer))

/* 마커 재렌더 watch */
watch(selectedId, () => renderKakaoMarkers())
watch(mapTab,     () => renderKakaoMarkers())
watch(vehicles,   () => renderKakaoMarkers())

watch(selectedDate, async () => {
  selectedId.value = null
  selectedDetail.value = null
  await Promise.all([refresh(), fetchWeeklyStats()])
})

/* ─── 파생값 ─── */
const isTodaySelected = computed(() => selectedDate.value === todayDateValue())
const dashboardVehicles = computed(() =>
  vehicles.value.filter(v => isTodaySelected.value ? v.status === 'RUNNING' : v.status !== 'IDLE'))
const selected         = computed(() => vehicles.value.find(v => v.id === selectedId.value) || null)
const selectedDetailId = computed(() => {
  const raw = selected.value?.driveLogId
  if (raw == null || raw === '') return null
  const parsed = Number(raw)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null
})
const restGuideCount            = computed(() => selected.value?.restGuideCount ?? 0)
const phoneRecommendationIssued = computed(() => Boolean(selected.value?.lastPhoneRecommendationAt))
const filteredVehicles = computed(() => {
  const r = dashboardVehicles.value
  if (mapTab.value === '전체') return r
  const need = mapTab.value === '위험' ? 'DANGER' : mapTab.value === '주의' ? 'CAUTION' : 'NORMAL'
  return r.filter(v => v.level === need)
})
const sortedFiltered = computed(() => filteredVehicles.value.slice().sort((a, b) => b.score - a.score))

/* #4 #5: 선택 차량 상세 자동 로드 (selectedDetailId 선언 이후에 위치해야 함) */
watch(selectedDetailId, async (id) => {
  if (!id || isDemoBoard.value) { selectedDetail.value = null; return }
  try {
    const res = await driveHistoryApi.getDetail(id)
    selectedDetail.value = res.data ?? null
  } catch {
    selectedDetail.value = null
  }
})

const runningCount = computed(() => dashboardVehicles.value.length)
const dangerCount  = computed(() => dashboardVehicles.value.filter(v => v.level === 'DANGER').length)
const cautionCount = computed(() => dashboardVehicles.value.filter(v => v.level === 'CAUTION').length)
const normalCount  = computed(() => dashboardVehicles.value.filter(v => v.level === 'NORMAL').length)
// #7: summary API의 todayCompleted 실데이터 사용
const todayCompleted = computed(() => summaryData.value?.todayCompleted ?? 0)
const idleCount    = computed(() => isTodaySelected.value ? vehicles.value.length - runningCount.value : 0)

const scores   = computed(() => dashboardVehicles.value.map(v => v.score))
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
const maxCont      = computed(() => dashboardVehicles.value.reduce((m, v) => Math.max(m, v.contMin || 0), 0))
const maxContH     = computed(() => Math.floor(maxCont.value / 60))
const maxContM     = computed(() => maxCont.value % 60)
const contGaugePct = computed(() => Math.min(100, (maxCont.value / 300) * 100))
const contColor    = computed(() => maxCont.value >= 180 ? 'var(--danger)' : maxCont.value >= 90 ? 'var(--warn)' : 'var(--ok)')
const contDelta    = computed(() => maxCont.value >= 180 ? 45 : maxCont.value >= 120 ? 25 : 10)

/* ─── KPI Tile 3 (#1 주간 DANGER 실데이터) ─── */
const weeklyFull = computed(() => {
  if (weeklyStats.value.length >= 7) {
    return weeklyStats.value.slice(-7).map(s => s.dangerEventCount ?? 0)
  }
  // 실데이터 부족 시 마지막 값을 오늘 dangerCount로 대체
  const base = [1, 2, 0, 2, 3, 1]
  return [...base, dangerCount.value]
})
const weeklyMax  = computed(() => Math.max(...weeklyFull.value, 1))
const weeklyMean = computed(() => {
  const arr = weeklyFull.value
  return arr.length ? arr.reduce((a, b) => a + b, 0) / arr.length : 0
})
const weeklyMeanY = computed(() => 36 - (weeklyMean.value / weeklyMax.value) * (36 - 4))
const weeklyBars = computed(() => {
  const h = 36
  return weeklyFull.value.map((d, i) => {
    const bh = (d / weeklyMax.value) * (h - 4)
    return { x: i * (200 / 7) + 1, y: h - bh, w: (200 / 7) - 4, h: bh, today: i === weeklyFull.value.length - 1 }
  })
})

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

function downloadCsv() {
  if (!sortedFiltered.value.length) {
    alert('다운로드할 차량 데이터가 없습니다.')
    return
  }

  const header = ['조회일자', '운행 ID', '차량 ID', '번호판', '운전자명', '차량유형', '피로점수', '피로등급', '운행시작시각', '상태']
  const rows = sortedFiltered.value.map(v => [
    selectedDate.value,
    v.driveLogId ?? '',
    v.vehicleId ?? '',
    v.plate,
    v.driver,
    v.type,
    v.score,
    v.level,
    formatCsvDateTime(v.startedAtRaw ?? v.startedAt),
    v.status,
  ])
  const csv = [header, ...rows].map(row => row.map(escapeCsvCell).join(',')).join('\n')
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  const suffix = csvTabSuffix(mapTab.value)
  link.href = url
  link.download = suffix === 'all'
    ? `dashboard_${selectedDate.value}.csv`
    : `dashboard_${selectedDate.value}_${suffix}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

async function sendRestGuide() {
  if (!selected.value) return
  if (!confirm(`${selected.value?.plate ?? '선택 차량'}에 휴게 안내를 기록할까요?`)) return
  actionSubmitting.value.rest = true

  // #8: 데모 모드 로컬 시뮬레이션
  if (isDemoBoard.value || !selectedDetailId.value) {
    const v = selected.value
    v.restGuideCount = (v.restGuideCount ?? 0) + 1
    const driver = v.driver ?? ''
    const phone  = v.driverPhone ?? ''
    const msg    = `[한라물류 관제센터] ${driver} 기사님, 안전 운행을 위해 즉시 휴식을 취해주시기 바랍니다.`
    const count  = v.restGuideCount
    smsToast.value = { driver, phone, message: msg, count }
    localEvents.value.unshift({ t: new Date().toTimeString().slice(0,5), kind:'info', plate: v.plate, text:`📱 SMS 발송 완료 · ${phone} · 휴게 안내 ${count}회차` })
    setTimeout(() => { smsToast.value = null }, 5000)
    actionSubmitting.value.rest = false
    return
  }

  try {
    const res = await dashboardApi.createAction({
      driveLogId: selectedDetailId.value,
      actionType: 'REST_GUIDE',
    })
    await refresh()
    // SMS 시뮬레이션: 토스트 + 이벤트 스트림 기록
    const data = res?.data ?? {}
    const driver = data.driverName  ?? selected.value?.driver ?? ''
    const phone  = data.driverPhone ?? selected.value?.driverPhone ?? ''
    const msg    = data.smsMessage  ?? `[관제센터] ${driver} 기사님, 즉시 휴식을 취해주시기 바랍니다.`
    const count  = restGuideCount.value  // refresh() 후 갱신된 값
    smsToast.value = { driver, phone, message: msg, count }
    const nowStr = new Date().toTimeString().slice(0, 5)
    localEvents.value.unshift({
      t: nowStr,
      kind: 'info',
      plate: selected.value?.plate ?? '',
      text: `📱 SMS 발송 완료 · ${phone} · 휴게 안내 ${count}회차`,
    })
    // 5초 후 토스트 자동 닫기
    setTimeout(() => { smsToast.value = null }, 5000)
  } catch (e) {
    alert(e?.response?.data?.message || '휴게 안내 기록 중 오류가 발생했습니다.')
  } finally {
    actionSubmitting.value.rest = false
  }
}

async function sendPhoneRecommendation() {
  if (!selected.value) return
  if (!confirm(`${selected.value?.plate ?? '선택 차량'}에 전화 권고를 기록할까요?`)) return
  actionSubmitting.value.phone = true

  // #8: 데모 모드 로컬 시뮬레이션
  if (isDemoBoard.value || !selectedDetailId.value) {
    const v = selected.value
    phoneModal.value = { driver: v.driver ?? '', phone: v.driverPhone ?? '', plate: v.plate ?? '' }
    actionSubmitting.value.phone = false
    return
  }

  try {
    await dashboardApi.createAction({
      driveLogId: selectedDetailId.value,
      actionType: 'PHONE_RECOMMENDATION',
    })
    await refresh()
    phoneModal.value = {
      driver: selected.value?.driver ?? '',
      phone:  selected.value?.driverPhone ?? '',
      plate:  selected.value?.plate ?? '',
    }
  } catch (e) {
    alert(e?.response?.data?.message || '전화 권고 기록 중 오류가 발생했습니다.')
  } finally {
    actionSubmitting.value.phone = false
  }
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
  // #4: selectedDetail의 실 restEvents 사용
  if (selectedDetail.value?.restEvents?.length) {
    const evs = selectedDetail.value.restEvents
    const valid   = evs.filter(e => e.restType === 'VALID').length
    const suff    = evs.filter(e => e.restType === 'SUFFICIENT').length
    const invalid = evs.filter(e => e.restType === 'INVALID').length
    const pending = evs.filter(e => e.restType === 'PENDING').length
    // MISSED는 FatigueEvent의 restViolationCount 합산
    const missed = selectedDetail.value.fatigueEvents
      ? Math.max(0, ...selectedDetail.value.fatigueEvents.map(e => e.continuousDrivingMinutes ?? 0).filter(m => m >= 180).map(() => 1))
      : 0
    return [
      { label:'VALID',   count: valid,   color:'var(--ok)',     hint:'15m+ · -10' },
      { label:'SUFF.',   count: suff,    color:'var(--accent)', hint:'30m+ · -20' },
      { label:'INVALID', count: invalid + pending, color:'var(--text-4)', hint:'<15m · ±0' },
      { label:'MISSED',  count: missed,  color:'var(--danger)', hint:'2h+ · +10' },
    ]
  }
  // 데모 fallback
  const v = selected.value
  if (!v) return []
  return [
    { label:'VALID',   count:v.restValid||0,   color:'var(--ok)',     hint:'15m+ · -10' },
    { label:'SUFF.',   count:v.restSuff||0,    color:'var(--accent)', hint:'30m+ · -20' },
    { label:'INVALID', count:v.restInvalid||0, color:'var(--text-4)', hint:'<15m · ±0' },
    { label:'MISSED',  count:v.restMiss||0,    color:'var(--danger)', hint:'2h+ · +10' },
  ]
})

/* ─── Drive Timeline (#5 실데이터 연동) ─── */
const timelinePoints = computed(() => {
  const v = selected.value
  if (!v) return []

  // 실 FatigueEvent 데이터가 있을 때
  const fEvents = selectedDetail.value?.fatigueEvents
  if (fEvents?.length && v.startedAtRaw) {
    const startMs = new Date(v.startedAtRaw).getTime()
    return fEvents.map(e => {
      const tMs  = new Date(e.occurredAt).getTime()
      const tMin = Math.round((tMs - startMs) / 60000)
      const lvl  = e.fatigueLevel
      let evt = null
      if (lvl === 'DANGER'  && !fEvents.slice(0, fEvents.indexOf(e)).some(x => x.fatigueLevel === 'DANGER'))  evt = '위험 진입'
      if (lvl === 'CAUTION' && !fEvents.slice(0, fEvents.indexOf(e)).some(x => x.fatigueLevel === 'CAUTION')) evt = '주의 진입'
      return { t: Math.max(0, tMin), score: e.fatigueScore ?? 0, event: evt }
    })
  }

  // 시뮬레이션 fallback
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

/* ─── EventStream (#3 실데이터 연동) ─── */
function apiEventKind(level) {
  return level === 'DANGER' ? 'danger' : level === 'CAUTION' ? 'warn' : 'info'
}
function apiEventText(ev) {
  const cont  = ev.continuousDrivingMinutes != null ? `연속 ${(ev.continuousDrivingMinutes/60).toFixed(1)}h` : null
  const daily = ev.dailyTotalDrivingMinutes != null ? `누적 ${(ev.dailyTotalDrivingMinutes/60).toFixed(1)}h` : null
  const score = `점수 ${ev.fatigueScore}`
  const level = ev.fatigueLevel === 'DANGER' ? '→ DANGER' : ev.fatigueLevel === 'CAUTION' ? '→ CAUTION' : '→ NORMAL'
  const parts = [cont, daily, score, level].filter(Boolean)
  return parts.join(' · ')
}
const liveEvents = computed(() => {
  // 실데이터가 있으면 API 이벤트, 없으면 데모 차량 기반 fallback
  if (apiEvents.value.length > 0) {
    return apiEvents.value.map(ev => ({
      t: ev.occurredAt ? String(ev.occurredAt).slice(11, 16) : '--:--',
      kind: apiEventKind(ev.fatigueLevel),
      plate: ev.plateNo,
      id: null,
      text: apiEventText(ev),
    }))
  }
  // 데모 fallback
  return dashboardVehicles.value
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
})
const seedEvents = [
  { t:'12:55', kind:'info', plate:'경기 80바 1026', text:'충분 휴식(34분) · -20점 보정 · 점수 18 (NORMAL)' },
  { t:'12:14', kind:'info', plate:'경기 80바 1028', text:'DEPARTURE · OCR 0.99 · 차고지 출발' },
  { t:'11:42', kind:'info', plate:'경기 80바 1027', text:'연속 3h 도달 · CAUTION 근접' },
  { t:'10:33', kind:'info', plate:'경기 80바 1025', text:'REST_AREA_CCTV · 입장 휴게소 진입' },
  { t:'09:18', kind:'info', plate:'경기 80바 1029', text:'HIGHWAY_CCTV · 중부내륙 점촌 상행 · OCR 0.94' },
  { t:'08:50', kind:'info', plate:'경기 80바 1027', text:'DEPARTURE · 한라물류 출발 · 시나리오 A 시작' },
]
// 실데이터가 있으면 seed 제거, 없으면 fallback 유지
const allEvents = computed(() =>
  apiEvents.value.length > 0
    ? [...localEvents.value, ...liveEvents.value]
    : [...localEvents.value, ...liveEvents.value, ...seedEvents]
)
function eventVehicleId(plate) { return vehicles.value.find(v => v.plate === plate)?.id ?? null }

/* ─── AlertList ─── */
const alertList = computed(() =>
  dashboardVehicles.value
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
  return dashboardVehicles.value.slice(0, 8).map(v => {
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
  dashboardVehicles.value.slice().sort((a, b) => b.score - a.score).slice(0, 6).map(v => ({
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
        <div style="font-size:12px;color:var(--text-3);margin-top:3px;">조회일 · {{ formatSelectedDateLabel(selectedDate) }} · 한라물류센터 · 가상 시뮬레이션 환경</div>
      </div>
      <div style="display:flex;align-items:center;gap:10px;">
        <span style="font-family:var(--font-mono);font-size:11.5px;color:var(--text-3);display:flex;align-items:center;gap:6px;">
          <span class="dot dot-ok blink"/>connected · {{ now }}
        </span>
        <div style="width:1px;height:18px;background:var(--line-2);"/>
        <label class="date-filter">
          <span class="date-filter__label"><AppIcon name="filter" :size="13"/> 날짜</span>
          <input v-model="selectedDate" class="date-filter__input mono" type="date" />
        </label>
        <button class="btn btn-ghost" :disabled="!sortedFiltered.length" @click="downloadCsv">
          <AppIcon name="download" :size="13"/> CSV
        </button>
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
          <span class="tile-meta">7d μ={{ weeklyMean.toFixed(1) }}</span>
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
            <text x="198" :y="weeklyMeanY-2" text-anchor="end" font-family="var(--font-mono)" font-size="7" fill="var(--text-4)">μ={{ weeklyMean.toFixed(1) }}</text>
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
            <span style="color:var(--accent);">완료 {{ todayCompleted }}</span>
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
          <div style="border-bottom:1px solid var(--line-1);display:flex;align-items:stretch;">
            <!-- 번호판 썸네일 (왼쪽 끝, 헤더 전체 높이, max-width 제한) -->
            <div style="flex-shrink:0;width:120px;align-self:stretch;background:linear-gradient(135deg,#DCDFE4,#B8BFC9);border-right:1px solid var(--line-3);position:relative;overflow:hidden;">
              <img
                v-if="selected?.driveLogId"
                :src="`/demo-plates/dl${selected.driveLogId}.jpg`"
                style="position:absolute;top:0;left:0;width:100%;height:100%;object-fit:cover;display:block;"
                @error="(e) => e.target.style.display='none'"
              />
              <svg width="100%" height="100%" viewBox="0 0 200 130" preserveAspectRatio="xMidYMid slice"
                :style="`opacity:.4;display:${selected?.driveLogId ? 'none' : 'block'};`">
                <rect width="200" height="130" fill="#E3E6EB"/>
                <rect x="30" y="40" width="120" height="50" fill="#CCD2DA" stroke="#979EAE" stroke-width="1"/>
                <circle cx="55" cy="98" r="8" fill="#747F95"/>
                <circle cx="160" cy="98" r="8" fill="#747F95"/>
              </svg>
              <div style="position:absolute;left:50%;bottom:8px;transform:translateX(-50%);background:#fff;border:1px solid rgba(81,95,122,.28);border-radius:2px;padding:2px 7px;font-family:var(--font-mono);font-size:10px;font-weight:700;color:#515F7A;white-space:nowrap;">
                {{ selected ? (selected.recognizedPlate || selected.plate.split(' ').slice(-1)[0]) : '—' }}
              </div>
            </div>
            <!-- 텍스트 + 버튼 -->
            <div style="display:flex;justify-content:space-between;align-items:center;gap:10px;flex:1;padding:14px 16px;flex-wrap:wrap;min-width:0;">
              <div style="min-width:0;">
                <div style="display:flex;align-items:center;gap:6px;flex-wrap:wrap;margin-bottom:4px;">
                  <span style="font-family:var(--font-mono);font-size:15px;font-weight:800;color:var(--text-1);">{{ selected ? selected.plate : '—' }}</span>
                  <span v-if="selected" :class="levelChipCls(selected.level)">{{ levelLabel(selected.level) }} · {{ selected.score }}점</span>
                  <span v-if="selected" class="chip chip-mute"><AppIcon name="user" :size="10"/> {{ selected.driver }}</span>
                  <span v-if="selected" class="chip chip-info">{{ selectedDetailId ?? '운행 ID 없음' }}</span>
                </div>
                <div style="font-family:var(--font-mono);font-size:10.5px;color:var(--text-3);">
                  {{ selected ? `${selected.loc} · 시작 ${selected.startedAt} · ${selected.spd}km/h · ${selected.type}` : '차량을 선택하세요' }}
                </div>
                <div v-if="selected && (restGuideCount > 0 || phoneRecommendationIssued)"
                  style="font-family:var(--font-mono);font-size:9.5px;color:var(--text-4);margin-top:6px;display:flex;gap:10px;flex-wrap:wrap;align-items:center;">
                  <span v-if="restGuideCount > 0" style="display:flex;align-items:center;gap:4px;">
                    ☕ 휴게 안내 <strong style="color:var(--warn);">{{ restGuideCount }}회</strong> · 마지막 {{ actionTimeLabel(selected.lastRestGuideAt) }}
                  </span>
                  <span v-if="phoneRecommendationIssued" style="display:flex;align-items:center;gap:4px;">
                    📞 전화 권고 완료 · {{ actionTimeLabel(selected.lastPhoneRecommendationAt) }}
                    <a v-if="selected.driverPhone" :href="`tel:${selected.driverPhone.replace(/-/g,'')}`"
                      style="color:var(--accent);text-decoration:none;font-weight:700;">{{ selected.driverPhone }}</a>
                  </span>
                </div>
              </div>
              <div style="display:flex;gap:6px;flex-wrap:wrap;flex-shrink:0;">
                <button class="btn btn-ghost" style="font-size:11.5px;"
                  :disabled="!selected || selected.level!=='DANGER' || phoneRecommendationIssued || actionSubmitting.phone"
                  :style="{opacity:selected?.level==='DANGER' && !phoneRecommendationIssued ? 1 : 0.45,cursor:selected?.level==='DANGER' && !phoneRecommendationIssued ? 'pointer' : 'not-allowed'}"
                  @click="sendPhoneRecommendation">
                  <AppIcon name="phone" :size="12"/>
                  {{ phoneRecommendationIssued ? '전화 권고 완료' : actionSubmitting.phone ? '기록 중...' : '전화 권고' }}
                </button>
                <button class="btn btn-ghost" style="font-size:11.5px;"
                  :disabled="!selected || actionSubmitting.rest"
                  :style="{opacity:selected ? 1 : 0.45,cursor:selected ? 'pointer' : 'not-allowed'}"
                  @click="sendRestGuide">
                  <AppIcon name="coffee" :size="12"/>
                  {{ actionSubmitting.rest ? '기록 중...' : restGuideCount > 0 ? `휴게 안내 (${restGuideCount}회)` : '휴게 안내' }}
                </button>
                <button class="btn btn-primary" style="font-size:11.5px;"
                  :disabled="!selectedDetailId"
                  :style="{opacity:selectedDetailId?1:0.45,cursor:selectedDetailId?'pointer':'not-allowed'}"
                  @click="goToDriveHistoryDetail">
                  <AppIcon name="arrow" :size="12"/> 운행 상세
                </button>
              </div>
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
                <img
                  v-if="v.driveLogId"
                  :src="`/demo-plates/dl${v.driveLogId}.jpg`"
                  :alt="v.recognizedPlate || v.plate"
                  style="width:100%;height:100%;object-fit:cover;display:block;"
                  @error="(e) => { e.target.style.display='none'; e.target.nextElementSibling.style.display='block'; }"
                />
                <svg width="100%" height="100%" viewBox="0 0 200 130" preserveAspectRatio="xMidYMid slice"
                  :style="`opacity:.4;display:${v.driveLogId ? 'none' : 'block'};`">
                  <rect width="200" height="130" fill="#E3E6EB"/>
                  <rect x="30" y="40" width="120" height="50" fill="#CCD2DA" stroke="#979EAE" stroke-width="1"/>
                  <circle cx="55" cy="98" r="8" fill="#747F95"/>
                  <circle cx="160" cy="98" r="8" fill="#747F95"/>
                </svg>
                <div style="position:absolute;left:50%;bottom:5px;transform:translateX(-50%);background:#fff;border:1px solid rgba(81,95,122,.28);border-radius:2px;padding:2px 5px;font-family:var(--font-mono);font-size:9px;font-weight:700;color:#515F7A;white-space:nowrap;">{{ v.recognizedPlate || v.plate.split(' ').slice(-1)[0] }}</div>
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
          <span class="chip chip-mute">{{ formatSelectedDateLabel(selectedDate) }}</span>
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

  <!-- ── 전화 권고 모달 ── -->
  <Teleport to="body">
    <div v-if="phoneModal" class="phone-modal-backdrop" @click.self="phoneModal=null">
      <div class="phone-modal">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:14px;">
          <div>
            <div style="font:600 10px/1 var(--font-mono);letter-spacing:.14em;text-transform:uppercase;color:var(--danger);margin-bottom:4px;">PHONE RECOMMENDATION</div>
            <div style="font:800 16px/1.2 var(--font-sans);color:var(--text-1);">전화 권고 기록 완료</div>
          </div>
          <button class="btn btn-ghost" style="padding:4px 8px;" @click="phoneModal=null">✕</button>
        </div>

        <div style="background:var(--bg-2);border:1px solid var(--line-2);border-radius:var(--r-sm);padding:14px 16px;margin-bottom:14px;">
          <div style="font-size:11px;color:var(--text-4);font-family:var(--font-mono);margin-bottom:6px;">대상 운전자</div>
          <div style="font:700 15px/1.3 var(--font-mono);color:var(--text-1);">{{ phoneModal.driver }}</div>
          <div style="font-size:11.5px;color:var(--text-3);margin-top:2px;">{{ phoneModal.plate }}</div>
        </div>

        <div style="margin-bottom:16px;">
          <div style="font-size:11px;color:var(--text-4);font-family:var(--font-mono);letter-spacing:.1em;margin-bottom:8px;">DRIVER CONTACT</div>
          <div style="font:800 22px/1 var(--font-mono);color:var(--text-1);letter-spacing:.04em;margin-bottom:12px;">
            {{ phoneModal.phone || '번호 없음' }}
          </div>
          <div v-if="phoneModal.phone" style="display:flex;gap:8px;">
            <a :href="`tel:${phoneModal.phone.replace(/-/g,'')}`" class="btn btn-primary" style="text-decoration:none;flex:1;justify-content:center;">
              📞 전화 걸기
            </a>
            <a :href="`sms:${phoneModal.phone.replace(/-/g,'')}?body=${encodeURIComponent('안전 운행을 위해 즉시 휴식을 취해주시기 바랍니다. (한라물류 관제센터)')}`"
              class="btn btn-ghost" style="text-decoration:none;flex:1;justify-content:center;">
              💬 문자 보내기
            </a>
          </div>
        </div>

        <div style="font-family:var(--font-mono);font-size:10px;color:var(--text-4);text-align:center;border-top:1px solid var(--line-1);padding-top:10px;">
          전화 권고 이력이 시스템에 기록되었습니다
        </div>
      </div>
    </div>
  </Teleport>

  <!-- ── SMS 시뮬레이션 토스트 ── -->
  <Teleport to="body">
    <Transition name="sms-toast">
      <div v-if="smsToast" class="sms-toast">
        <div class="sms-toast__header">
          <span style="font:600 10px/1 var(--font-mono);letter-spacing:.14em;text-transform:uppercase;color:var(--ok);">📱 SMS 발송 시뮬레이션</span>
          <button style="background:none;border:none;cursor:pointer;color:var(--text-3);font-size:14px;line-height:1;padding:0;" @click="smsToast=null">✕</button>
        </div>
        <div class="sms-toast__to">
          <span style="font-size:10px;color:var(--text-4);font-family:var(--font-mono);">TO</span>
          <span style="font:700 13px/1 var(--font-mono);color:var(--text-1);">{{ smsToast.driver }}</span>
          <span style="font-family:var(--font-mono);font-size:11px;color:var(--ok);">{{ smsToast.phone }}</span>
          <span style="font-size:10px;color:var(--text-4);font-family:var(--font-mono);">· {{ smsToast.count }}회차</span>
        </div>
        <div class="sms-toast__bubble">{{ smsToast.message }}</div>
        <div style="font-size:10px;color:var(--text-4);font-family:var(--font-mono);margin-top:6px;">포트폴리오 시뮬레이션 · 실제 전송 아님</div>
      </div>
    </Transition>
  </Teleport>

</template>

<style scoped>
.fade-up { animation: fadeUp 0.4s ease both; }
@keyframes fadeUp { from { opacity:0; transform:translateY(8px); } to { opacity:1; transform:translateY(0); } }

/* 페이지 */
.view { padding: 20px 8px 32px; max-width:100%; }

/* 페이지 헤더 */
.page-hdr { display:flex; justify-content:space-between; align-items:flex-end; margin-bottom:14px; gap:16px; flex-wrap:wrap; }
.date-filter {
  display:flex;
  align-items:center;
  gap:8px;
  padding:7px 10px;
  border:1px solid var(--line-2);
  border-radius:var(--r-md);
  background:var(--bg-1);
}
.date-filter__label {
  display:inline-flex;
  align-items:center;
  gap:6px;
  font-size:11px;
  color:var(--text-3);
  white-space:nowrap;
}
.date-filter__input {
  border:none;
  background:transparent;
  color:var(--text-1);
  font-size:12px;
  outline:none;
}

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

/* 전화 권고 모달 */
.phone-modal-backdrop {
  position: fixed; inset: 0; z-index: 1000;
  background: rgba(15,20,30,.55);
  display: flex; align-items: center; justify-content: center;
  backdrop-filter: blur(3px);
  animation: fadeUp .18s ease both;
}
.phone-modal {
  background: var(--bg-1);
  border: 1px solid var(--danger-soft);
  border-top: 3px solid var(--danger);
  border-radius: var(--r-md);
  box-shadow: 0 8px 32px rgba(0,0,0,.22);
  padding: 20px 22px;
  width: 360px;
  max-width: calc(100vw - 32px);
}

/* 버튼 */
.btn { display:inline-flex; align-items:center; gap:5px; padding:6px 12px; border-radius:var(--r-sm); font-size:12.5px; font-weight:500; cursor:pointer; border:none; font-family:var(--font-sans); transition:background .12s; }
.btn-primary { background:var(--accent); color:#fff; }
.btn-primary:hover { background:var(--accent-hover); }
.btn-ghost { background:var(--bg-2); color:var(--text-2); border:1px solid var(--line-2); }
.btn-ghost:hover { background:var(--bg-3); }

/* SMS 시뮬레이션 토스트 */
.sms-toast {
  position: fixed; top: 24px; left: 50%; transform: translateX(-50%); z-index: 1100;
  width: 360px;
  background: var(--bg-1);
  border: 1px solid var(--ok-soft);
  border-top: 3px solid var(--ok);
  border-radius: var(--r-md);
  box-shadow: 0 8px 32px rgba(0,0,0,.22);
  padding: 14px 16px;
}
.sms-toast__header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 10px;
}
.sms-toast__to {
  display: flex; align-items: center; gap: 6px; flex-wrap: wrap;
  margin-bottom: 10px;
}
.sms-toast__bubble {
  background: var(--bg-2);
  border: 1px solid var(--line-2);
  border-radius: var(--r-sm);
  padding: 10px 12px;
  font-size: 12px;
  color: var(--text-2);
  line-height: 1.5;
  font-family: var(--font-sans);
  word-break: keep-all;
}
/* 토스트 Transition */
.sms-toast-enter-active { animation: sms-slide-in .22s ease both; }
.sms-toast-leave-active { animation: sms-slide-in .18s ease reverse both; }
@keyframes sms-slide-in {
  from { opacity:0; transform:translateX(-50%) translateY(-16px); }
  to   { opacity:1; transform:translateX(-50%) translateY(0); }
}
</style>
