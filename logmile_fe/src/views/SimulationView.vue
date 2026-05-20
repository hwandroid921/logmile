<script setup>
import { ref, computed, onMounted } from 'vue'
import AppIcon from '@/components/common/AppIcon.vue'
import { thresholdApi } from '@/api/thresholdApi'
import { simulationApi } from '@/api/simulationApi'
import { ocrApi } from '@/api/ocrApi'
import { vehicleApi } from '@/api/vehicleApi'
import { driverApi } from '@/api/driverApi'

/* ───────── 임계값 맵 ───────── */
const T = ref({})
const thresholdLoaded = ref(false)

// 임계값 키를 시뮬레이터에서 쓰는 snake_upper 형태로 변환
function normalizeKey(key) {
  return (key || '').toUpperCase()
}

async function loadThresholds() {
  try {
    const res = await thresholdApi.getAll()
    const map = {}
    res.data.forEach(t => {
      const k = normalizeKey(t.thresholdKey)
      map[k] = t.thresholdValue
    })
    T.value = map
    thresholdLoaded.value = true
  } catch (e) {
    // 임계값 로드 실패 시 기본값 사용
    T.value = {
      CONTINUOUS_DRIVING_90_DELTA: 10,
      CONTINUOUS_DRIVING_120_DELTA: 25,
      CONTINUOUS_DRIVING_180_DELTA: 45,
      CONTINUOUS_DRIVING_240_DELTA: 65,
      CONTINUOUS_DRIVING_90: 90,
      CONTINUOUS_DRIVING_120: 120,
      CONTINUOUS_DRIVING_180: 180,
      CONTINUOUS_DRIVING_240: 240,
      DAILY_DRIVING_360_DELTA: 10,
      DAILY_DRIVING_480_DELTA: 25,
      DAILY_DRIVING_600_DELTA: 40,
      DAILY_DRIVING_360: 360,
      DAILY_DRIVING_480: 480,
      DAILY_DRIVING_600: 600,
      NIGHT_DRIVING_30_DELTA: 10,
      NIGHT_DRIVING_60_DELTA: 25,
      NIGHT_DRIVING_120_DELTA: 40,
      NIGHT_DRIVING_30: 30,
      NIGHT_DRIVING_60: 60,
      NIGHT_DRIVING_120: 120,
      REST_VALID_MINUTES: 15,
      REST_SUFFICIENT_MINUTES: 30,
      REST_CORRECTION_VALID_SCORE_DELTA: -10,
      REST_CORRECTION_SUFFICIENT_SCORE_DELTA: -20,
    }
    thresholdLoaded.value = true
  }
}

/* ───────── 차량/운전자 목록 ───────── */
const vehicles = ref([])
const drivers  = ref([])
const selectedVehicleId = ref(null)
const selectedDriverId  = ref(null)
const driveLogId = ref(null)   // 시뮬레이션 시작 후 반환
const isRunning  = ref(false)
const apiError   = ref(null)
const apiInfo    = ref(null)
const savingEvents = ref(false)

async function loadVehiclesDrivers() {
  try {
    const [vRes, dRes] = await Promise.all([vehicleApi.getAll(), driverApi.getAll()])
    vehicles.value = vRes.data.filter(v => v.active)
    drivers.value  = dRes.data
    if (vehicles.value.length) selectedVehicleId.value = vehicles.value[0].id
    if (drivers.value.length)  selectedDriverId.value  = drivers.value[0].id
  } catch (e) {
    // 목록 로드 실패 시 무시 (입력란으로 대체)
  }
}

onMounted(async () => {
  await Promise.all([loadThresholds(), loadVehiclesDrivers()])
  applyScenarioPreset('A')
})

/* ───────── 파라미터 ───────── */
const simulationMode = ref('SCENARIO')
const selectedScenario = ref('A')
const startHour   = ref(4)
const durationMin = ref(360)
const nightMode   = ref(false)

const rests = ref([
  { atMin: 120, durMin: 20 },
  { atMin: 270, durMin: 35 },
])

const entries = ref([
  { atMin: 0,   kind: 'DEPARTURE',    plate: '경기 80바 1024', location: '한라물류 차고지', ocrConf: 0.99, detectionConfidence: 0.99, sourceType: 'SIMULATOR', manualRequired: false },
  { atMin: 180, kind: 'HIGHWAY_CCTV', plate: '경기 80바 1024', location: '경부고속 안성IC', ocrConf: 0.95, detectionConfidence: 0.95, sourceType: 'SIMULATOR', manualRequired: false },
  { atMin: 360, kind: 'ARRIVAL',      plate: '경기 80바 1024', location: '수원 물류센터',   ocrConf: 0.98, detectionConfidence: 0.98, sourceType: 'SIMULATOR', manualRequired: false },
])

const scenarioPresets = {
  A: {
    startHour: 9,
    durationMin: 160,
    nightMode: false,
    rests: [{ atMin: 90, durMin: 35 }],
    entries: [
      { atMin: 0, kind: 'DEPARTURE', plate: '경기 80바 1026', location: '한라물류 차고지', ocrConf: 0.99 },
      { atMin: 160, kind: 'ARRIVAL', plate: '경기 80바 1026', location: '수원 물류센터', ocrConf: 0.98 },
    ],
  },
  B: {
    startHour: 6,
    durationMin: 390,
    nightMode: false,
    rests: [{ atMin: 180, durMin: 20 }],
    entries: [
      { atMin: 0, kind: 'DEPARTURE', plate: '경기 80바 1025', location: '인천항 3게이트', ocrConf: 0.96 },
      { atMin: 150, kind: 'HIGHWAY_CCTV', plate: '경기 80바 1025', location: '서해안고속 서산IC', ocrConf: 0.94 },
      { atMin: 390, kind: 'ARRIVAL', plate: '경기 80바 1025', location: '대전 허브 터미널', ocrConf: 0.95 },
    ],
  },
  C: {
    startHour: 3,
    durationMin: 620,
    nightMode: true,
    rests: [{ atMin: 260, durMin: 10 }],
    entries: [
      { atMin: 0, kind: 'DEPARTURE', plate: '경기 80바 1024', location: '한라물류 차고지', ocrConf: 0.97 },
      { atMin: 210, kind: 'HIGHWAY_CCTV', plate: '경기 80바 1024', location: '경부고속 안성IC', ocrConf: 0.93 },
      { atMin: 330, kind: 'REST_AREA_CCTV', plate: '경기 80바 1024', location: '안성휴게소', ocrConf: 0.9 },
      { atMin: 620, kind: 'ARRIVAL', plate: '경기 80바 1024', location: '부산항 물류센터', ocrConf: 0.94 },
    ],
  },
}

function normalizeEntry(entry) {
  return {
    ...entry,
    detectionConfidence: entry.detectionConfidence ?? entry.ocrConf ?? 0.95,
    sourceType: entry.sourceType ?? 'SIMULATOR',
    manualRequired: entry.manualRequired ?? false,
    ocrLoading: false,
    ocrError: null,
    ocrResult: null,
    imageFile: null,
  }
}

function applyScenarioPreset(scenario = selectedScenario.value) {
  const preset = scenarioPresets[scenario]
  if (!preset) return
  selectedScenario.value = scenario
  startHour.value = preset.startHour
  durationMin.value = preset.durationMin
  nightMode.value = preset.nightMode
  rests.value = preset.rests.map(r => ({ ...r }))
  entries.value = preset.entries.map(normalizeEntry)
  runSim()
}

/* ───────── 시뮬레이션 엔진 ───────── */
const NIGHT_H = new Set([22, 23, 0, 1, 2, 3, 4, 5])
const isNight = h => NIGHT_H.has(h % 24)

const continuousSteps = computed(() => [
  { min: T.value.CONTINUOUS_DRIVING_240 ?? 240, delta: T.value.CONTINUOUS_DRIVING_240_DELTA ?? 65 },
  { min: T.value.CONTINUOUS_DRIVING_180 ?? 180, delta: T.value.CONTINUOUS_DRIVING_180_DELTA ?? 45 },
  { min: T.value.CONTINUOUS_DRIVING_120 ?? 120, delta: T.value.CONTINUOUS_DRIVING_120_DELTA ?? 25 },
  { min: T.value.CONTINUOUS_DRIVING_90  ?? 90,  delta: T.value.CONTINUOUS_DRIVING_90_DELTA  ?? 10 },
])
const dailySteps = computed(() => [
  { min: T.value.DAILY_DRIVING_600 ?? 600, delta: T.value.DAILY_DRIVING_600_DELTA ?? 40 },
  { min: T.value.DAILY_DRIVING_480 ?? 480, delta: T.value.DAILY_DRIVING_480_DELTA ?? 25 },
  { min: T.value.DAILY_DRIVING_360 ?? 360, delta: T.value.DAILY_DRIVING_360_DELTA ?? 10 },
])
const nightSteps = computed(() => [
  { min: T.value.NIGHT_DRIVING_120 ?? 120, delta: T.value.NIGHT_DRIVING_120_DELTA ?? 40 },
  { min: T.value.NIGHT_DRIVING_60  ?? 60,  delta: T.value.NIGHT_DRIVING_60_DELTA  ?? 25 },
  { min: T.value.NIGHT_DRIVING_30  ?? 30,  delta: T.value.NIGHT_DRIVING_30_DELTA  ?? 10 },
])

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
      const REST_SUFFICIENT = T.value.REST_SUFFICIENT_MINUTES ?? 30
      const REST_VALID      = T.value.REST_VALID_MINUTES ?? 15
      if (dur >= REST_SUFFICIENT) {
        const d = T.value.REST_CORRECTION_SUFFICIENT_SCORE_DELTA ?? -20
        score = Math.max(0, score + d)
        events.push({ t: m, label: `충분 휴식 ${dur}분`, kind: 'rest_sufficient', delta: d })
      } else if (dur >= REST_VALID) {
        const d = T.value.REST_CORRECTION_VALID_SCORE_DELTA ?? -10
        score = Math.max(0, score + d)
        events.push({ t: m, label: `유효 휴식 ${dur}분`, kind: 'rest_valid', delta: d })
      } else {
        events.push({ t: m, label: `무효 휴식 ${dur}분 (<${REST_VALID}m)`, kind: 'rest_invalid', delta: 0 })
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
      for (const s of continuousSteps.value) {
        if (contMin >= s.min && !firedC.has(s.min)) {
          firedC.add(s.min); score += s.delta
          events.push({ t: m, label: `연속 ${s.min}분 초과`, kind: 'continuous', delta: s.delta })
          break
        }
      }
      // 일일 누적
      for (const s of dailySteps.value) {
        if (dailyMin >= s.min && !firedD.has(s.min)) {
          firedD.add(s.min); score += s.delta
          events.push({ t: m, label: `일일 누적 ${s.min}분 초과`, kind: 'daily', delta: s.delta })
          break
        }
      }
      // 야간
      if (nightMode.value || isNight(hour)) {
        for (const s of nightSteps.value) {
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

const autoScenarioType = computed(() => {
  if (maxScore.value >= 70) return 'C'
  if (maxScore.value >= 40) return 'B'
  return 'A'
})
const scenarioType = computed(() => simulationMode.value === 'SCENARIO' ? selectedScenario.value : autoScenarioType.value)

function toLocalDateTime(atMin = 0) {
  const d = new Date()
  d.setHours(startHour.value, 0, 0, 0)
  d.setMinutes(d.getMinutes() + Number(atMin || 0))
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  const ss = String(d.getSeconds()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}T${hh}:${mi}:${ss}`
}

function mapPlateEventType(kind) {
  return kind === 'ARRIVAL' ? 'EXIT' : 'ENTRY'
}

function mapPlateLocationType(kind) {
  if (kind === 'HIGHWAY_CCTV') return 'HIGHWAY_GATE'
  if (kind === 'REST_AREA_CCTV') return 'REST_AREA'
  return 'CCTV'
}

function validRestCount() {
  const validMinutes = T.value.REST_VALID_MINUTES ?? 15
  return rests.value.filter(r => Number(r.durMin || 0) >= validMinutes).length
}

function restViolationCount() {
  return Math.max(0, Math.floor(Number(durationMin.value || 0) / 120) - validRestCount())
}

function isRestMinute(minute) {
  return rests.value.some(r => {
    const start = Number(r.atMin || 0)
    const end = start + Number(r.durMin || 0)
    return minute >= start && minute < end
  })
}

function drivingMinutes() {
  let count = 0
  for (let m = 0; m <= durationMin.value; m++) {
    if (!isRestMinute(m)) count++
  }
  return count
}

function maxContinuousDrivingMinutes() {
  let current = 0
  let max = 0
  for (let m = 0; m <= durationMin.value; m++) {
    if (isRestMinute(m)) {
      current = 0
    } else {
      current++
      max = Math.max(max, current)
    }
  }
  return max
}

function nightDrivingMinutes() {
  let count = 0
  for (let m = 0; m <= durationMin.value; m++) {
    const hour = Math.floor((startHour.value * 60 + m) / 60) % 24
    if (!isRestMinute(m) && (nightMode.value || isNight(hour))) count++
  }
  return count
}

function primaryEntry() {
  return entries.value[0] ?? null
}

async function startSimulation() {
  const entry = primaryEntry()
  if (!selectedVehicleId.value || !selectedDriverId.value) {
    apiError.value = '차량과 운전자를 선택해주세요.'
    return
  }
  if (!entry?.plate) {
    apiError.value = '시작 번호판을 입력하거나 OCR로 인식해주세요.'
    return
  }
  apiError.value = null
  apiInfo.value = null
  try {
    const res = await simulationApi.start({
      vehicleId: selectedVehicleId.value,
      driverId: selectedDriverId.value,
      scenarioType: scenarioType.value,
      recognizedPlateNo: entry.plate,
      ocrConfidence: entry.ocrConf,
      manualInput: entry.sourceType === 'MANUAL' || !!entry.manualRequired,
      startedAt: toLocalDateTime(0),
    })
    driveLogId.value = res.data.driveLogId
    isRunning.value = true
    apiInfo.value = `DriveLog #${driveLogId.value} 운행 시작`
  } catch (e) {
    apiError.value = '시뮬레이션 시작 중 오류가 발생했습니다.'
  }
}

async function applySimulationEvents() {
  if (!driveLogId.value) {
    apiError.value = '먼저 서버 시뮬레이션을 시작해주세요.'
    return
  }
  savingEvents.value = true
  apiError.value = null
  apiInfo.value = null
  try {
    for (const entry of entries.value) {
      await simulationApi.createPlateEvent({
        plateNo: entry.plate,
        eventType: mapPlateEventType(entry.kind),
        locationType: mapPlateLocationType(entry.kind),
        sourceType: entry.sourceType ?? 'SIMULATOR',
        observedAt: toLocalDateTime(entry.atMin),
        confidence: entry.ocrConf,
        detectionConfidence: entry.detectionConfidence ?? entry.ocrConf,
        isManualRequired: !!entry.manualRequired,
        memo: entry.location,
      })
    }

    for (const rest of rests.value) {
      await simulationApi.createRestEvent({
        driveLogId: driveLogId.value,
        restStartedAt: toLocalDateTime(rest.atMin),
        restEndedAt: toLocalDateTime(Number(rest.atMin || 0) + Number(rest.durMin || 0)),
      })
    }

    await simulationApi.createFatigueEvent({
      driveLogId: driveLogId.value,
      continuousDrivingMinutes: maxContinuousDrivingMinutes(),
      dailyTotalDrivingMinutes: drivingMinutes(),
      nightDrivingMinutes: nightDrivingMinutes(),
      restCount: validRestCount(),
      restViolationCount: restViolationCount(),
      occurredAt: toLocalDateTime(durationMin.value),
      reason: `시연용 ${scenarioType.value} 시나리오 피로도 입력`,
    })

    apiInfo.value = '번호판/휴식/피로도 이벤트가 서버에 반영되었습니다.'
  } catch (e) {
    apiError.value = '시뮬레이션 이벤트 저장 중 오류가 발생했습니다.'
  } finally {
    savingEvents.value = false
  }
}

async function stopSimulation() {
  if (!driveLogId.value) return
  apiError.value = null
  apiInfo.value = null
  try {
    await simulationApi.stop(driveLogId.value, {
      endedAt: toLocalDateTime(durationMin.value),
    })
    isRunning.value = false
    driveLogId.value = null
    apiInfo.value = '시뮬레이션 운행이 종료되었습니다.'
  } catch (e) {
    apiError.value = '시뮬레이션 중지 중 오류가 발생했습니다.'
  }
}

function onPlateImageChange(index, event) {
  const entry = entries.value[index]
  if (!entry) return
  entry.imageFile = event.target.files?.[0] ?? null
  entry.ocrResult = null
  entry.ocrError = null
}

async function recognizePlate(index) {
  const entry = entries.value[index]
  if (!entry?.imageFile) {
    entry.ocrError = '번호판 이미지를 선택해주세요.'
    return
  }
  entry.ocrLoading = true
  entry.ocrError = null
  try {
    const res = await ocrApi.recognize(entry.imageFile)
    const result = res.data
    entry.ocrResult = result
    entry.plate = result.plate_no || entry.plate
    entry.ocrConf = result.confidence ?? entry.ocrConf
    entry.detectionConfidence = result.detection_confidence ?? entry.ocrConf
    entry.manualRequired = !!result.is_manual_required
    entry.sourceType = result.is_manual_required ? 'MANUAL' : 'OCR'
    runSim()
  } catch (e) {
    entry.ocrError = '번호판 인식 중 오류가 발생했습니다.'
  } finally {
    entry.ocrLoading = false
  }
}

function addRest()  { rests.value.push({ atMin: 60, durMin: 20 }) }
function delRest(i) { rests.value.splice(i, 1) }
function addEntry() { entries.value.push(normalizeEntry({ atMin: 0, kind: 'HIGHWAY_CCTV', plate: '경기 80바 1024', location: '', ocrConf: 0.95 })) }
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
        <p class="page-sub">A/B/C 프리셋 또는 직접 입력으로 번호판, 휴식, 피로도 이벤트를 구성하고 실제 API 흐름에 반영합니다.</p>
      </div>
      <div style="display:flex;flex-direction:column;align-items:flex-end;gap:8px;">
        <div style="display:flex;gap:8px;align-items:center;">
          <select v-model.number="selectedVehicleId" class="sel-inp mono">
            <option v-if="!vehicles.length" :value="null">차량 없음</option>
            <option v-for="v in vehicles" :key="v.id" :value="v.id">{{ v.plateNo }}</option>
          </select>
          <select v-model.number="selectedDriverId" class="sel-inp mono">
            <option v-if="!drivers.length" :value="null">운전자 없음</option>
            <option v-for="d in drivers" :key="d.id" :value="d.id">{{ d.name }}</option>
          </select>
          <button class="btn btn-primary run-btn" @click="runSim">
            <AppIcon name="refresh" :size="13" />시뮬레이션 실행
          </button>
          <button v-if="!isRunning" class="btn btn-ghost run-btn" @click="startSimulation" :disabled="!thresholdLoaded">
            서버 시작
          </button>
          <button v-if="isRunning" class="btn btn-ghost run-btn" @click="applySimulationEvents" :disabled="savingEvents">
            {{ savingEvents ? '반영 중' : '이벤트 반영' }}
          </button>
          <button v-if="isRunning" class="btn btn-danger run-btn" @click="stopSimulation">
            서버 중지
          </button>
        </div>
        <div v-if="isRunning" class="mono" style="font-size:11px;color:var(--ok)">
          ● 운행 중 · DriveLog #{{ driveLogId }}
        </div>
        <div v-if="apiError" style="font-size:11px;color:var(--danger)">{{ apiError }}</div>
        <div v-if="apiInfo" style="font-size:11px;color:var(--ok)">{{ apiInfo }}</div>
      </div>
    </div>

    <div class="main-grid">
      <!-- ── 파라미터 패널 ── -->
      <div class="param-col">
        <!-- 기본 설정 -->
        <div class="card param-card">
          <div class="param-title">기본 파라미터</div>

          <div class="mode-tabs">
            <button class="mode-btn mono" :class="{ active: simulationMode === 'SCENARIO' }" @click="simulationMode = 'SCENARIO'; applyScenarioPreset(selectedScenario)">
              A/B/C
            </button>
            <button class="mode-btn mono" :class="{ active: simulationMode === 'MANUAL' }" @click="simulationMode = 'MANUAL'">
              직접 입력
            </button>
          </div>

          <div v-if="simulationMode === 'SCENARIO'" class="scenario-grid">
            <button v-for="s in ['A','B','C']" :key="s" class="scenario-btn mono"
              :class="{ active: selectedScenario === s }" @click="applyScenarioPreset(s)">
              {{ s }}
            </button>
          </div>

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
              :style="{ color: r.durMin >= (T.REST_SUFFICIENT_MINUTES ?? 30) ? 'var(--ok)' : r.durMin >= (T.REST_VALID_MINUTES ?? 15) ? '#5B8FA8' : 'var(--warn)' }">
              {{ r.durMin >= (T.REST_SUFFICIENT_MINUTES ?? 30) ? 'SUFFICIENT (-20pt)' : r.durMin >= (T.REST_VALID_MINUTES ?? 15) ? 'VALID (-10pt)' : 'INVALID (보정없음)' }}
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
                <span class="input-lbl mono">PLATE</span>
                <input v-model="e.plate" type="text" class="num-inp mono loc-inp" placeholder="번호판" />
              </div>
              <div class="input-group" style="grid-column:1/-1">
                <span class="input-lbl mono">LOCATION</span>
                <input v-model="e.location" type="text" class="num-inp mono loc-inp" placeholder="위치" />
              </div>
              <div class="input-group">
                <span class="input-lbl mono">OCR_CONF</span>
                <input v-model.number="e.ocrConf" type="number" min="0" max="1" step="0.01" class="num-inp mono" />
              </div>
              <div class="input-group">
                <span class="input-lbl mono">SOURCE</span>
                <select v-model="e.sourceType" class="num-inp mono sel-inp">
                  <option>OCR</option>
                  <option>SIMULATOR</option>
                  <option>MANUAL</option>
                  <option>DUMMY</option>
                </select>
              </div>
              <div class="input-group" style="grid-column:1/-1">
                <span class="input-lbl mono">PLATE IMAGE</span>
                <input type="file" accept="image/*" class="num-inp mono" @change="onPlateImageChange(i, $event)" />
              </div>
              <button class="add-btn mono" type="button" @click="recognizePlate(i)" :disabled="e.ocrLoading">
                {{ e.ocrLoading ? 'OCR 처리 중' : 'FastAPI OCR' }}
              </button>
              <div v-if="e.ocrResult" class="ocr-result mono">
                {{ e.ocrResult.plate_no || '수동 입력 필요' }} · {{ Math.round((e.ocrResult.confidence || 0) * 100) }}%
              </div>
              <div v-if="e.ocrError" class="ocr-error">{{ e.ocrError }}</div>
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
.mode-tabs { display:grid; grid-template-columns:1fr 1fr; gap:6px; }
.mode-btn, .scenario-btn {
  padding:7px 8px; border:1px solid var(--line-2); border-radius:var(--r-sm);
  background:var(--bg-1); color:var(--text-3); cursor:pointer; font-size:11px; font-weight:700;
}
.mode-btn.active, .scenario-btn.active {
  color:var(--accent); background:var(--accent-soft); border-color:var(--accent-line);
}
.scenario-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:6px; }

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
.sel-inp {
  padding:6px 10px; border:1px solid var(--line-2); border-radius:var(--r-md);
  background:var(--bg-2); color:var(--text-1); font-size:12px; cursor:pointer;
  font-family:var(--font-mono); outline:none;
}
.btn-danger {
  background:var(--danger); color:#fff; border:none; border-radius:var(--r-md);
  cursor:pointer; font-weight:600; transition:opacity .15s;
}
.btn-danger:hover { opacity:0.85; }
.loc-inp  { width:100%; }
.empty-hint { font-size:11.5px; color:var(--text-4); padding:8px 0; }
.ocr-result { grid-column:1/-1; font-size:10.5px; color:var(--ok); }
.ocr-error  { grid-column:1/-1; font-size:11px; color:var(--danger); }

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
