<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { storeToRefs } from 'pinia'
import AppIcon from '@/components/common/AppIcon.vue'
import { useSimulationStore } from '@/stores/simulationStore'
import { vehicleApi } from '@/api/vehicleApi'
import { driverApi } from '@/api/driverApi'
import { ocrApi } from '@/api/ocrApi'

const sim = useSimulationStore()
const {
  mode, scenarioType,
  vehicleId, vehiclePlateNo, driverId, driverName, ocrConfidence,
  isRunning, driveLogId, startedAtIso, endedAtIso,
  events, plannedEvents,
  apiError, apiInfo, nowIso,
  currentScore, maxScore, levelLabel, elapsedMinutes, scoreState,
} = storeToRefs(sim)

/* ─────────── 차량/운전자 목록 ─────────── */
const vehicles = ref([])
const drivers  = ref([])

async function loadVehiclesDrivers() {
  try {
    const [vRes, dRes] = await Promise.all([vehicleApi.getAll(), driverApi.getAll()])
    vehicles.value = vRes.data.filter(v => v.active)
    drivers.value  = dRes.data
  } catch (e) { /* 무시 */ }
}

// vehicleId 변경 → driver 자동 매칭
function autoSelectDriverFromVehicle() {
  const v = vehicles.value.find(x => x.id === vehicleId.value)
  if (!v) return
  // driverId 우선 매칭 (vehicle.driverId 있으면), 없으면 vehicleId 매칭
  const matched =
    (v.driverId && drivers.value.find(d => d.id === v.driverId)) ||
    drivers.value.find(d => d.vehicleId === v.id) ||
    null
  if (matched) sim.selectDriver(matched)
  else {
    driverId.value = null
    driverName.value = ''
  }
}

function pickVehicleById(id) {
  const v = vehicles.value.find(x => x.id === id)
  if (v) { sim.selectVehicle(v); autoSelectDriverFromVehicle() }
}

/* ─────────── 차량 등록 방식 ─────────── */
const vehiclePickMode = ref('LIST')      // 'LIST' | 'PHOTO'
const ocrFile = ref(null)
const ocrLoading = ref(false)
const ocrError = ref(null)
const ocrResult = ref(null)
const ocrPreviewUrl = ref('')
const ocrAbortController = ref(null)
const recognizedPlateNo = computed(() => ocrResult.value?.plate_no || '')
const ocrPreviewLabel = computed(() => {
  if (!ocrResult.value) return ''
  return recognizedPlateNo.value || '수동 확인 필요'
})

function onPhoto(e) {
  if (ocrLoading.value) return
  if (ocrPreviewUrl.value) URL.revokeObjectURL(ocrPreviewUrl.value)
  ocrFile.value = e.target.files?.[0] ?? null
  ocrPreviewUrl.value = ocrFile.value ? URL.createObjectURL(ocrFile.value) : ''
  ocrResult.value = null
  ocrError.value = null
}

function cancelRecognize() {
  ocrAbortController.value?.abort()
}

async function recognizePhoto() {
  if (!ocrFile.value) { ocrError.value = '사진을 먼저 선택해주세요.'; return }
  ocrLoading.value = true; ocrError.value = null
  ocrAbortController.value = new AbortController()
  try {
    const res = await ocrApi.recognize(ocrFile.value, { signal: ocrAbortController.value.signal })
    const r = res.data
    ocrResult.value = r
    ocrConfidence.value = r.confidence ?? 0.95
    // 인식된 plate 로 차량 목록에서 매칭
    const target = (r.plate_no || '').replace(/\s+/g, '')
    const found = vehicles.value.find(v => (v.plateNo || '').replace(/\s+/g, '') === target)
    if (found) {
      sim.selectVehicle(found)
      autoSelectDriverFromVehicle()
    } else {
      // 매칭 없으면 raw 번호만 저장
      vehiclePlateNo.value = r.plate_no || ''
      vehicleId.value = null
      ocrError.value = `인식된 번호판 [${r.plate_no}] 와 매칭되는 차량이 없습니다.`
    }
  } catch (e) {
    if (e.name === 'CanceledError' || e.code === 'ERR_CANCELED') {
      ocrError.value = '번호판 인식을 취소했습니다.'
    } else {
      ocrError.value = '번호판 인식 실패'
    }
  } finally {
    ocrLoading.value = false
    ocrAbortController.value = null
  }
}

/* ─────────── 시작/종료 시각 입력 ─────────── */
const startedAtInput = ref('')   // datetime-local
const endedAtInput   = ref('')

function pad(n) { return String(n).padStart(2, '0') }
function nowLocalDt() {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function initStartInput() {
  if (!startedAtInput.value) startedAtInput.value = nowLocalDt()
}
function initEndInput() {
  if (!endedAtInput.value) endedAtInput.value = nowLocalDt()
}

/* ─────────── 운행 시작/종료 ─────────── */
const submitting = ref({ start: false, stop: false, event: false })

async function doStart() {
  submitting.value.start = true
  const ok = await sim.start({ startedAt: startedAtInput.value || nowLocalDt() })
  submitting.value.start = false
  if (ok) {
    // 종료 시각 입력란은 비워두고, 사용자가 클릭하면 그때 now 로 채움
    endedAtInput.value = ''
  }
}

async function doStop() {
  // 사용자가 endedAtInput 비워뒀으면 자동으로 지금 시각 기록
  const eIso = endedAtInput.value || nowLocalDt()
  submitting.value.stop = true
  await sim.stop({ endedAt: eIso })
  submitting.value.stop = false
}

function doReset() {
  if (!confirm('운행 기록을 초기화하시겠습니까?')) return
  sim.reset()
  startedAtInput.value = nowLocalDt()
  endedAtInput.value = ''
}

/* ─────────── 이벤트 등록 (직접 입력 모드) ─────────── */
const eventType = ref('REST')   // 'REST' | 'FATIGUE' | 'MARKER'

// REST 폼
const restForm = ref({ startIso: '', endIso: '' })
// FATIGUE 폼
const fatigueForm = ref({
  continuousDrivingMinutes: 0,
  dailyTotalDrivingMinutes: 0,
  nightDrivingMinutes: 0,
  restCount: 0,
  restViolationCount: 0,
  occurredAtIso: '',
  reason: '',
})
// MARKER 폼
const markerForm = ref({ tIso: '', label: '', locationType: 'HIGHWAY_GATE' })

function fillEventFormDefaults() {
  const n = nowLocalDt()
  if (!restForm.value.startIso)    restForm.value.startIso = n
  if (!restForm.value.endIso)      restForm.value.endIso   = n
  if (!fatigueForm.value.occurredAtIso) fatigueForm.value.occurredAtIso = n
  if (!markerForm.value.tIso)      markerForm.value.tIso = n
}

async function submitEvent() {
  submitting.value.event = true
  try {
    if (eventType.value === 'REST') {
      const ok = await sim.addRest({
        startIso: restForm.value.startIso,
        endIso: restForm.value.endIso,
      })
      if (ok) {
        const dur = sim.diffMinutes(restForm.value.startIso, restForm.value.endIso)
        // 다음 입력 편의: 종료시각을 시작시각으로 옮겨두기
        restForm.value.startIso = restForm.value.endIso
        restForm.value.endIso = sim.addMin(restForm.value.endIso, Math.max(15, dur))
      }
    } else if (eventType.value === 'FATIGUE') {
      const ok = await sim.addFatigue({ ...fatigueForm.value })
      if (ok) {
        fatigueForm.value.occurredAtIso = nowLocalDt()
        fatigueForm.value.reason = ''
      }
    } else if (eventType.value === 'MARKER') {
      sim.addMarker({
        tIso: markerForm.value.tIso,
        label: markerForm.value.label || '위치 마커',
        locationType: markerForm.value.locationType,
      })
      markerForm.value.label = ''
      markerForm.value.tIso = nowLocalDt()
    }
  } finally {
    submitting.value.event = false
  }
}

/* ─────────── 점 그래프 데이터 ─────────── */
// x축: startedAt ~ (현재 또는 endedAt)
const chart = computed(() => {
  if (!startedAtIso.value) return null
  const startMs = sim.parseLocalDt(startedAtIso.value).getTime()
  const endIso = isRunning.value ? nowIso.value : (endedAtIso.value || nowIso.value)
  const endMs = sim.parseLocalDt(endIso).getTime()
  const span = Math.max(endMs - startMs, 60 * 1000) // 최소 1분

  const W = 640, H = 200, padL = 36, padR = 12, padT = 14, padB = 22
  const xOf = ms => padL + ((ms - startMs) / span) * (W - padL - padR)
  const yOf = s  => padT + (1 - s / 100) * (H - padT - padB)

  // line path: start(0) → 각 trace 점들 → 끝점(현재 score)
  const points = []
  points.push({ ms: startMs, score: 0 })
  for (const tr of scoreState.value.traces.slice(1)) {
    points.push({ ms: sim.parseLocalDt(tr.tIso).getTime(), score: tr.score, kind: tr.kind, ref: tr.ref })
  }
  // 마지막 시점에 현재 점수 (선이 끝까지 이어지게)
  if (points[points.length - 1].ms < endMs - 1000) {
    points.push({ ms: endMs, score: currentScore.value })
  }

  const pathD = points
    .map((p, i) => `${i === 0 ? 'M' : 'L'} ${xOf(p.ms).toFixed(1)} ${yOf(p.score).toFixed(1)}`)
    .join(' ')

  // 채움 영역
  const areaD = pathD
    + ` L ${xOf(endMs).toFixed(1)} ${yOf(0).toFixed(1)}`
    + ` L ${xOf(startMs).toFixed(1)} ${yOf(0).toFixed(1)} Z`

  // x축 눈금 (시작/종료 + 1시간 간격 또는 4분할)
  const ticks = []
  const ticksN = 5
  for (let i = 0; i <= ticksN; i++) {
    const ms = startMs + (span * i) / ticksN
    ticks.push({ x: xOf(ms), label: formatHm(new Date(ms)) })
  }

  return {
    W, H, padL, padR, padT, padB,
    pathD, areaD,
    points: points.slice(1),  // 첫점(시작)은 별도로 안 그림
    ticks,
    yDanger: yOf(70), yCaution: yOf(40), yZero: yOf(0),
  }
})

function formatHm(d) { return `${pad(d.getHours())}:${pad(d.getMinutes())}` }

function eventColor(kind) {
  if (kind === 'rest')    return 'var(--ok)'
  if (kind === 'fatigue') return 'var(--danger)'
  if (kind === 'marker')  return 'var(--info)'
  return 'var(--accent)'
}
function eventIcon(kind) {
  if (kind === 'rest')    return '◎'
  if (kind === 'fatigue') return '▲'
  if (kind === 'marker')  return '◉'
  return '·'
}
function eventLabel(ev) {
  if (ev.kind === 'rest')    return `휴식 ${ev.durationMin}분`
  if (ev.kind === 'fatigue') return `피로도 입력 · 연속 ${ev.continuousDrivingMinutes}m · 일일 ${ev.dailyTotalDrivingMinutes}m`
  if (ev.kind === 'marker')  return ev.label
  return ''
}

const eventsSorted = computed(() =>
  [...events.value].sort((a, b) => sim.parseLocalDt(b.tIso) - sim.parseLocalDt(a.tIso))
)

const elapsedFmt = computed(() => {
  const m = elapsedMinutes.value
  const h = Math.floor(m / 60)
  return h > 0 ? `${h}h ${m % 60}m` : `${m}m`
})

const levelColor = computed(() =>
  currentScore.value >= 70 ? 'var(--danger)' :
  currentScore.value >= 40 ? 'var(--warn)' : 'var(--ok)'
)
const peakColor = computed(() =>
  maxScore.value >= 70 ? 'var(--danger)' :
  maxScore.value >= 40 ? 'var(--warn)' : 'var(--ok)'
)

/* ─────────── mount ─────────── */
onMounted(async () => {
  sim.hydrate()
  await sim.loadThresholds()
  await loadVehiclesDrivers()

  // hydrate 후 vehicleId 가 살아있는데 vehicles 가 늦게 도착했을 수 있음
  if (vehicleId.value && vehicles.value.length) {
    const v = vehicles.value.find(x => x.id === vehicleId.value)
    if (v) sim.selectVehicle(v)
  }
  // hydrate 된 운전자 값도 현재 차량 기준으로 다시 검증한다.
  if (vehicleId.value) autoSelectDriverFromVehicle()
  // 기본 차량/운전자 (둘 다 비어있을 때만)
  if (!vehicleId.value && vehicles.value.length) pickVehicleById(vehicles.value[0].id)

  if (!startedAtInput.value) startedAtInput.value = nowLocalDt()
  fillEventFormDefaults()
})
onBeforeUnmount(() => {
  if (ocrPreviewUrl.value) URL.revokeObjectURL(ocrPreviewUrl.value)
})

// vehicles 가 늦게 도착한 경우 driver 자동 매칭 재시도
watch(vehicles, () => {
  if (vehicleId.value) autoSelectDriverFromVehicle()
})
</script>

<template>
  <div class="view">
    <!-- ───── 헤더 ───── -->
    <div class="breadcrumb mono">ADMIN / SIMULATION · 시연용 운행 시뮬레이터</div>
    <div class="page-header">
      <div>
        <h2 class="page-title">운행 시뮬레이션</h2>
        <p class="page-sub">
          GPS·CCTV 없이 시연 현장에서 운행 / 휴식 / 피로도 이벤트를 직접 입력해 서버에 동일한 흐름을 만들어냅니다.
          다른 페이지로 이동해도 진행 상태는 유지됩니다.
        </p>
      </div>
      <div class="header-status">
        <div v-if="isRunning" class="status-running mono">
          <span class="status-dot" /> RUNNING · DriveLog #{{ driveLogId }}
        </div>
        <div v-else class="status-idle mono">
          <span class="status-dot status-dot-idle" /> IDLE
        </div>
        <div v-if="apiError" class="api-msg api-err">{{ apiError }}</div>
        <div v-if="apiInfo"  class="api-msg api-ok">{{ apiInfo }}</div>
      </div>
    </div>

    <!-- ───── KPI 스트립 ───── -->
    <div class="kpi-strip">
      <div class="kpi-card kpi-main">
        <div class="kpi-lbl mono">현재 피로도</div>
        <div class="kpi-num kpi-num-xl" :style="{ color: levelColor }">{{ currentScore }}</div>
        <div class="kpi-bar"><div class="kpi-bar-fill" :style="{ width: currentScore + '%', background: levelColor }" /></div>
      </div>
      <div class="kpi-card">
        <div class="kpi-lbl mono">최고 피로도</div>
        <div class="kpi-num" :style="{ color: peakColor }">{{ maxScore }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-lbl mono">피로도 레벨</div>
        <div class="kpi-level" :style="{ color: levelColor, borderColor: levelColor }">{{ levelLabel }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-lbl mono">경과 시간</div>
        <div class="kpi-num">{{ elapsedFmt }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-lbl mono">이벤트</div>
        <div class="kpi-num">{{ events.length }}</div>
      </div>
    </div>

    <div class="main-grid">
      <!-- ───── LEFT: 컨트롤 ───── -->
      <div class="left-col">

        <!-- 모드 토글 -->
        <div class="card panel">
          <div class="panel-title">
            <AppIcon name="layers" :size="13" />
            <span>시뮬레이션 모드</span>
          </div>
          <div class="mode-tabs">
            <button class="mode-btn mono" :class="{ active: mode === 'MANUAL' }"
                    :disabled="isRunning || ocrLoading"
                    @click="sim.setMode('MANUAL')">
              직접 입력
            </button>
            <button class="mode-btn mono" :class="{ active: mode === 'SCENARIO' }"
                    :disabled="isRunning || ocrLoading"
                    @click="sim.setMode('SCENARIO')">
              시나리오 선택
            </button>
          </div>
          <div v-if="mode === 'SCENARIO'" class="scenario-grid">
            <button v-for="s in ['A','B','C']" :key="s"
                    class="scenario-btn mono"
                    :class="{ active: scenarioType === s }"
                    :disabled="isRunning || ocrLoading"
                    @click="sim.setScenario(s)">
              {{ s }}
            </button>
          </div>
          <div v-if="isRunning" class="locked-hint mono">운행 중에는 모드를 바꿀 수 없습니다</div>
        </div>

        <!-- 차량 / 운전자 -->
        <div class="card panel">
          <div class="panel-title">
            <AppIcon name="truck" :size="13" />
            <span>차량 등록</span>
          </div>
          <div class="pick-tabs">
            <button class="pick-btn mono" :class="{ active: vehiclePickMode === 'LIST' }"
                    :disabled="isRunning || ocrLoading" @click="vehiclePickMode = 'LIST'">
              기존 차량
            </button>
            <button class="pick-btn mono" :class="{ active: vehiclePickMode === 'PHOTO' }"
                    :disabled="isRunning || ocrLoading" @click="vehiclePickMode = 'PHOTO'">
              사진 등록 (OCR)
            </button>
          </div>

          <div v-if="vehiclePickMode === 'LIST'" class="form-row">
            <label class="form-lbl mono">VEHICLE</label>
            <select :value="vehicleId" @change="e => pickVehicleById(Number(e.target.value))"
                    class="sel-inp mono" :disabled="isRunning || ocrLoading">
              <option v-if="!vehicles.length" :value="null">차량 없음</option>
              <option v-for="v in vehicles" :key="v.id" :value="v.id">{{ v.plateNo }}</option>
            </select>
          </div>

          <div v-else class="form-row">
            <label class="form-lbl mono">PHOTO</label>
            <input type="file" accept="image/*" class="txt-inp mono" :disabled="isRunning || ocrLoading" @change="onPhoto" />
            <div class="ocr-row">
              <button class="btn btn-ghost ocr-btn" :disabled="!ocrFile || ocrLoading || isRunning" @click="recognizePhoto">
                <AppIcon name="camera" :size="13" />
                {{ ocrLoading ? '인식 중…' : '번호판 인식' }}
              </button>
              <button v-if="ocrLoading" class="btn btn-ghost ocr-btn cancel" @click="cancelRecognize">
                취소
              </button>
              <div v-if="ocrResult" class="ocr-result mono">
                <b>{{ recognizedPlateNo || '인식 실패' }}</b>
                <span>· {{ Math.round((ocrResult.confidence || 0) * 100) }}%</span>
              </div>
            </div>
            <div v-if="ocrPreviewUrl" class="ocr-preview">
              <img :src="ocrPreviewUrl" alt="선택한 차량 번호판 사진 미리보기" />
              <div v-if="ocrResult" class="ocr-preview-label mono" :class="{ warn: !recognizedPlateNo }">
                <span class="ocr-label-title">인식 라벨</span>
                <strong>{{ ocrPreviewLabel }}</strong>
                <span class="ocr-label-meta">
                  OCR {{ Math.round((ocrResult.confidence || 0) * 100) }}%
                  <template v-if="ocrResult.detection_confidence !== null && ocrResult.detection_confidence !== undefined">
                    · DET {{ Math.round(ocrResult.detection_confidence * 100) }}%
                  </template>
                </span>
              </div>
            </div>
            <div v-if="ocrResult" class="recognized-plate">
              <span class="recognized-lbl mono">인식한 차량 번호</span>
              <span class="recognized-num mono">{{ recognizedPlateNo || '인식 실패' }}</span>
              <span class="recognized-meta mono">
                OCR {{ Math.round((ocrResult.confidence || 0) * 100) }}%
              </span>
            </div>
            <div v-if="ocrError" class="ocr-error mono">{{ ocrError }}</div>
          </div>

          <!-- 선택된 차량/운전자 요약 -->
          <div class="picked-card">
            <div class="picked-row">
              <span class="picked-lbl mono">차량</span>
              <span class="picked-val mono">{{ vehiclePlateNo || '— 미선택 —' }}</span>
            </div>
            <div class="picked-row">
              <span class="picked-lbl mono">운전자</span>
              <span class="picked-val mono">{{ driverName || '— 자동 매칭 대기 —' }}</span>
            </div>
          </div>
        </div>

        <!-- 운행 시작 / 종료 -->
        <div class="card panel">
          <div class="panel-title">
            <AppIcon name="play" :size="13" />
            <span>운행 제어</span>
          </div>

          <div v-if="!isRunning" class="form-row">
            <label class="form-lbl mono">
              STARTED_AT
              <button class="reset-mini mono" :disabled="ocrLoading" @click="startedAtInput = nowLocalDt()">↻ 지금</button>
            </label>
            <input type="datetime-local" v-model="startedAtInput" class="txt-inp mono" :disabled="ocrLoading" />
            <div class="form-hint mono">비워두면 시작 클릭 시각이 그대로 기록됩니다.</div>
          </div>

          <div v-else class="form-row">
            <label class="form-lbl mono">RUNNING SINCE</label>
            <input :value="startedAtIso" disabled class="txt-inp mono" />
          </div>

          <div v-if="isRunning" class="form-row">
            <label class="form-lbl mono">
              ENDED_AT
              <button class="reset-mini mono" @click="endedAtInput = nowLocalDt()">↻ 지금</button>
            </label>
            <input type="datetime-local" v-model="endedAtInput" class="txt-inp mono"
                   :placeholder="nowLocalDt()" />
            <div class="form-hint mono">비워두고 종료를 누르면 클릭 시각이 그대로 기록됩니다. 운행 시간은 자동 계산.</div>
          </div>

          <div class="control-actions">
            <button v-if="!isRunning"
                    class="btn btn-primary"
                    :disabled="submitting.start || !vehicleId || !driverId || ocrLoading"
                    @click="doStart">
              <AppIcon name="play" :size="13" />
              {{ submitting.start ? '시작 중…' : '운행 시작' }}
            </button>
            <button v-if="isRunning"
                    class="btn btn-danger"
                    :disabled="submitting.stop"
                    @click="doStop">
              <AppIcon name="stop" :size="13" />
              {{ submitting.stop ? '종료 중…' : '운행 종료' }}
            </button>
            <button v-if="!isRunning && (events.length || driveLogId)"
                    class="btn btn-ghost btn-sm"
                    @click="doReset">
              초기화
            </button>
          </div>
        </div>

        <!-- 이벤트 등록 (직접 입력 모드 + 운행 중) -->
        <div v-if="mode === 'MANUAL' && isRunning" class="card panel">
          <div class="panel-title">
            <AppIcon name="plus" :size="13" />
            <span>이벤트 등록</span>
          </div>

          <div class="form-row">
            <label class="form-lbl mono">이벤트 종류</label>
            <select v-model="eventType" class="sel-inp mono">
              <option value="REST">휴식 등록 (rest-events)</option>
              <option value="FATIGUE">피로도 수치 (fatigue-events)</option>
              <option value="MARKER">위치 마커 (로컬 표시용)</option>
            </select>
          </div>

          <!-- REST 폼 -->
          <div v-if="eventType === 'REST'" class="event-form">
            <div class="form-row">
              <label class="form-lbl mono">REST_STARTED_AT</label>
              <input type="datetime-local" v-model="restForm.startIso" class="txt-inp mono" />
            </div>
            <div class="form-row">
              <label class="form-lbl mono">REST_ENDED_AT</label>
              <input type="datetime-local" v-model="restForm.endIso" class="txt-inp mono" />
            </div>
            <div class="form-hint-strong mono"
                 :style="{ color: (sim.diffMinutes(restForm.startIso, restForm.endIso) >= 30) ? 'var(--ok)' :
                                  (sim.diffMinutes(restForm.startIso, restForm.endIso) >= 15) ? 'var(--info)' : 'var(--warn)' }">
              {{ sim.diffMinutes(restForm.startIso, restForm.endIso) }}분 ·
              <template v-if="sim.diffMinutes(restForm.startIso, restForm.endIso) >= 30">SUFFICIENT (-20pt)</template>
              <template v-else-if="sim.diffMinutes(restForm.startIso, restForm.endIso) >= 15">VALID (-10pt)</template>
              <template v-else>INVALID (보정 없음)</template>
            </div>
          </div>

          <!-- FATIGUE 폼 -->
          <div v-if="eventType === 'FATIGUE'" class="event-form fatigue-form">
            <div class="form-row">
              <label class="form-lbl mono">CONTINUOUS_MIN<span class="form-lbl-ko">연속 운행 (분)</span></label>
              <input type="number" min="0" v-model.number="fatigueForm.continuousDrivingMinutes" class="num-inp mono" />
            </div>
            <div class="form-row">
              <label class="form-lbl mono">DAILY_MIN<span class="form-lbl-ko">일일 총 운행 (분)</span></label>
              <input type="number" min="0" v-model.number="fatigueForm.dailyTotalDrivingMinutes" class="num-inp mono" />
            </div>
            <div class="form-row">
              <label class="form-lbl mono">NIGHT_MIN<span class="form-lbl-ko">야간 운행 (분)</span></label>
              <input type="number" min="0" v-model.number="fatigueForm.nightDrivingMinutes" class="num-inp mono" />
            </div>
            <div class="form-row">
              <label class="form-lbl mono">REST_COUNT<span class="form-lbl-ko">유효 휴식 횟수</span></label>
              <input type="number" min="0" v-model.number="fatigueForm.restCount" class="num-inp mono" />
            </div>
            <div class="form-row">
              <label class="form-lbl mono">REST_VIOLATION<span class="form-lbl-ko">휴식 위반 횟수</span></label>
              <input type="number" min="0" v-model.number="fatigueForm.restViolationCount" class="num-inp mono" />
            </div>
            <div class="form-row">
              <label class="form-lbl mono">OCCURRED_AT<span class="form-lbl-ko">발생 시각</span></label>
              <input type="datetime-local" v-model="fatigueForm.occurredAtIso" class="txt-inp mono" />
            </div>
            <div class="form-row form-row-wide">
              <label class="form-lbl mono">REASON<span class="form-lbl-ko">메모 / 사유</span></label>
              <input type="text" v-model="fatigueForm.reason" class="txt-inp mono" placeholder="(선택)" />
            </div>
          </div>

          <!-- MARKER 폼 -->
          <div v-if="eventType === 'MARKER'" class="event-form">
            <div class="form-row">
              <label class="form-lbl mono">LOCATION_TYPE</label>
              <select v-model="markerForm.locationType" class="sel-inp mono">
                <option value="HIGHWAY_GATE">고속도로 게이트 (입출차)</option>
                <option value="REST_AREA">휴게소</option>
                <option value="CCTV">CCTV 통과</option>
                <option value="DEPARTURE">출발지</option>
                <option value="ARRIVAL">도착지</option>
              </select>
            </div>
            <div class="form-row">
              <label class="form-lbl mono">LABEL</label>
              <input type="text" v-model="markerForm.label" class="txt-inp mono" placeholder="예: 경부고속 안성IC" />
            </div>
            <div class="form-row">
              <label class="form-lbl mono">OBSERVED_AT</label>
              <input type="datetime-local" v-model="markerForm.tIso" class="txt-inp mono" />
            </div>
            <div class="form-hint mono">※ 백엔드 API 없음 — 그래프와 이벤트 로그에 시각적 마커만 추가됩니다.</div>
          </div>

          <button class="btn btn-primary event-submit" :disabled="submitting.event" @click="submitEvent">
            <AppIcon name="check" :size="13" />
            {{ submitting.event ? '등록 중…' : '이벤트 등록' }}
          </button>
        </div>

        <!-- 예정 이벤트 (시나리오 모드 + 운행 중) -->
        <div v-if="mode === 'SCENARIO' && isRunning && plannedEvents.length" class="card panel">
          <div class="panel-title">
            <AppIcon name="list" :size="13" />
            <span>예정된 이벤트 · 시나리오 {{ scenarioType }}</span>
          </div>
          <div class="planned-list">
            <div v-for="p in plannedEvents" :key="p.id" class="planned-row">
              <span class="mono planned-time">+{{ p.offsetMin }}분</span>
              <span class="planned-label">{{ p.label }}</span>
            </div>
          </div>
          <div class="form-hint mono">시나리오 모드에서는 이벤트 등록이 자동 처리되며, 시연 중에는 종료 버튼만 누르면 됩니다.</div>
        </div>

      </div>

      <!-- ───── RIGHT: 라이브 차트 + 이벤트 로그 ───── -->
      <div class="right-col">
        <div class="card chart-card">
          <div class="chart-hdr">
            <div class="card-title">피로 점수 추이</div>
            <span class="mono chart-sub">{{ scoreState.traces.length }}개 데이터 포인트</span>
          </div>

          <div v-if="!chart" class="chart-empty">
            <span>운행을 시작하면 점수 추이가 표시됩니다.</span>
          </div>

          <svg v-else class="chart-svg" :viewBox="`0 0 ${chart.W} ${chart.H}`" preserveAspectRatio="none">
            <!-- 가이드라인 -->
            <line :x1="chart.padL" :x2="chart.W - chart.padR" :y1="chart.yDanger"  :y2="chart.yDanger"
                  stroke="rgba(181,84,74,.35)" stroke-dasharray="4 3" />
            <line :x1="chart.padL" :x2="chart.W - chart.padR" :y1="chart.yCaution" :y2="chart.yCaution"
                  stroke="rgba(197,138,58,.35)" stroke-dasharray="4 3" />
            <line :x1="chart.padL" :x2="chart.W - chart.padR" :y1="chart.yZero"    :y2="chart.yZero"
                  stroke="var(--line-2)" />

            <!-- 영역 채움 -->
            <path :d="chart.areaD" fill="var(--accent-soft)" />
            <!-- 선 -->
            <path :d="chart.pathD" fill="none" :stroke="levelColor" stroke-width="2" stroke-linejoin="round" />

            <!-- 점 -->
            <g v-for="(p, i) in chart.points" :key="i">
              <circle :cx="chart.padL + ((p.ms - sim.parseLocalDt(startedAtIso).getTime()) /
                            ((sim.parseLocalDt(isRunning ? nowIso : (endedAtIso || nowIso)).getTime()) - sim.parseLocalDt(startedAtIso).getTime() || 1)) * (chart.W - chart.padL - chart.padR)"
                      :cy="chart.padT + (1 - p.score / 100) * (chart.H - chart.padT - chart.padB)"
                      r="4.5"
                      :fill="eventColor(p.kind)"
                      stroke="var(--bg-1)" stroke-width="2">
                <title>{{ formatHm(new Date(p.ms)) }} · {{ p.score }}점</title>
              </circle>
            </g>

            <!-- 현재 점수 큰 점 (운행 중일 때) -->
            <circle v-if="isRunning && chart.points.length"
                    :cx="chart.W - chart.padR"
                    :cy="chart.padT + (1 - currentScore / 100) * (chart.H - chart.padT - chart.padB)"
                    r="6" :fill="levelColor" stroke="var(--bg-1)" stroke-width="2.5">
              <animate attributeName="r" values="6;8;6" dur="1.5s" repeatCount="indefinite" />
            </circle>

            <!-- y 축 라벨 -->
            <text :x="chart.padL - 6" :y="chart.yDanger + 3"  text-anchor="end" class="chart-y-lbl" fill="var(--danger)">70</text>
            <text :x="chart.padL - 6" :y="chart.yCaution + 3" text-anchor="end" class="chart-y-lbl" fill="var(--warn)">40</text>
            <text :x="chart.padL - 6" :y="chart.yZero + 3"    text-anchor="end" class="chart-y-lbl" fill="var(--text-4)">0</text>

            <!-- x 축 -->
            <g v-for="(t, i) in chart.ticks" :key="'t'+i">
              <line :x1="t.x" :x2="t.x" :y1="chart.yZero" :y2="chart.yZero + 4" stroke="var(--line-2)" />
              <text :x="t.x" :y="chart.yZero + 16" text-anchor="middle" class="chart-x-lbl" fill="var(--text-4)">{{ t.label }}</text>
            </g>
          </svg>
        </div>

        <div class="card event-card">
          <div class="chart-hdr">
            <div class="card-title">이벤트 로그</div>
            <span class="mono chart-sub">{{ events.length }}건</span>
          </div>
          <div class="event-list">
            <div v-if="!events.length" class="empty-hint">아직 등록된 이벤트가 없습니다.</div>
            <div v-for="ev in eventsSorted" :key="ev.id" class="event-row">
              <span class="ev-icon" :style="{ color: eventColor(ev.kind) }">{{ eventIcon(ev.kind) }}</span>
              <span class="mono ev-time">{{ ev.tIso.slice(11, 16) }}</span>
              <span class="ev-label">{{ eventLabel(ev) }}</span>
              <span v-if="ev.scoreAfter !== undefined" class="mono ev-score" :style="{
                color: ev.scoreAfter >= 70 ? 'var(--danger)' : ev.scoreAfter >= 40 ? 'var(--warn)' : 'var(--ok)'
              }">{{ ev.scoreAfter }}pt</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view { display:flex; flex-direction:column; gap:14px; padding:28px 32px 40px; }
.breadcrumb { font-size:11px; color:var(--text-4); letter-spacing:0.04em; }

.page-header { display:flex; align-items:flex-start; justify-content:space-between; gap:24px; }
.page-title  { font-size:24px; font-weight:700; color:var(--text-1); margin:0 0 4px; letter-spacing:-0.01em; }
.page-sub    { font-size:12.5px; color:var(--text-3); margin:0; max-width:680px; line-height:1.55; }

.header-status { display:flex; flex-direction:column; align-items:flex-end; gap:6px; min-width:240px; }
.status-running, .status-idle {
  display:flex; align-items:center; gap:8px;
  font-size:11px; padding:5px 10px; border-radius:999px;
  border:1px solid var(--line-2); background:var(--bg-2);
}
.status-running { color:var(--ok); border-color:rgba(94,138,111,.4); background:var(--ok-soft); }
.status-dot { width:7px; height:7px; border-radius:50%; background:var(--ok); animation:pulse-ring 1.6s infinite; }
.status-dot-idle { background:var(--text-4); animation:none; box-shadow:none; }
.status-idle { color:var(--text-3); }
.api-msg { font-size:11px; max-width:360px; text-align:right; }
.api-err { color:var(--danger); }
.api-ok  { color:var(--ok); }

/* ── KPI ───────────────────────── */
.kpi-strip {
  display:grid; grid-template-columns:1.6fr 1fr 1fr 1fr 1fr; gap:10px;
}
.kpi-card {
  background:var(--bg-1); border:1px solid var(--line-2); border-radius:var(--r-md);
  padding:14px 18px; display:flex; flex-direction:column; gap:6px;
}
.kpi-main { background:var(--bg-1); }
.kpi-lbl  { font-size:10px; letter-spacing:0.07em; color:var(--text-4); }
.kpi-num  { font-size:28px; font-weight:800; color:var(--text-1); letter-spacing:-0.02em; line-height:1; }
.kpi-num-xl { font-size:42px; }
.kpi-bar  { height:4px; background:var(--bg-3); border-radius:2px; overflow:hidden; margin-top:4px; }
.kpi-bar-fill { height:100%; transition:width .4s, background .4s; }
.kpi-level {
  font-size:18px; font-weight:800; letter-spacing:0.04em;
  padding:6px 12px; border:1.5px solid; border-radius:var(--r-sm);
  align-self:flex-start; font-family:var(--font-mono);
}

/* ── 메인 그리드 ───────────────── */
.main-grid { display:grid; grid-template-columns:420px 1fr; gap:14px; align-items:start; }
.left-col, .right-col { display:flex; flex-direction:column; gap:12px; }

.panel { padding:14px 16px; display:flex; flex-direction:column; gap:12px; }
.panel-title {
  font-size:12.5px; font-weight:700; color:var(--text-1);
  display:flex; align-items:center; gap:7px;
}

/* mode toggles */
.mode-tabs, .pick-tabs { display:grid; grid-template-columns:1fr 1fr; gap:6px; }
.mode-btn, .pick-btn, .scenario-btn {
  padding:8px 10px; border:1px solid var(--line-2); border-radius:var(--r-sm);
  background:var(--bg-1); color:var(--text-3); cursor:pointer;
  font-size:11.5px; font-weight:700; letter-spacing:0.02em;
  transition:background .15s, border-color .15s, color .15s;
}
.mode-btn:hover:not(:disabled), .pick-btn:hover:not(:disabled), .scenario-btn:hover:not(:disabled) {
  background:var(--bg-2);
}
.mode-btn.active, .pick-btn.active, .scenario-btn.active {
  color:var(--accent); background:var(--accent-soft); border-color:var(--accent-line);
}
.mode-btn:disabled, .pick-btn:disabled, .scenario-btn:disabled { opacity:.5; cursor:not-allowed; }
.scenario-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:6px; }
.locked-hint { font-size:10px; color:var(--text-4); }

/* form */
.form-row { display:flex; flex-direction:column; gap:5px; }
.form-row-wide { grid-column:1 / -1; }
.form-lbl {
  font-size:9.5px; letter-spacing:0.06em; color:var(--text-4);
  display:flex; align-items:center; gap:8px; flex-wrap:wrap;
}
.form-lbl-ko {
  font-family:var(--font-sans); letter-spacing:0; color:var(--text-3);
  font-size:11px; text-transform:none; font-weight:500;
}
.form-hint { font-size:10.5px; color:var(--text-4); margin-top:2px; }
.form-hint-strong { font-size:11px; font-weight:700; letter-spacing:0.03em; }

.txt-inp, .num-inp, .sel-inp {
  padding:8px 10px; border:1px solid var(--line-2); border-radius:var(--r-sm);
  background:var(--bg-1); color:var(--text-1); font-size:12.5px; outline:none;
  width:100%; box-sizing:border-box; transition:border-color .15s;
  font-family:var(--font-mono);
}
.txt-inp:focus, .num-inp:focus, .sel-inp:focus { border-color:var(--accent-line); }
.txt-inp:disabled { background:var(--bg-2); color:var(--text-3); }
.sel-inp { cursor:pointer; }

.reset-mini {
  font-size:9.5px; padding:1px 6px; border-radius:var(--r-sm);
  background:transparent; color:var(--text-4); border:1px solid var(--line-2);
  cursor:pointer; letter-spacing:0;
}
.reset-mini:hover { color:var(--accent); border-color:var(--accent-line); }

/* OCR */
.ocr-row { display:flex; align-items:center; gap:10px; margin-top:6px; flex-wrap:wrap; }
.ocr-btn { font-size:11.5px; padding:6px 12px; }
.ocr-btn.cancel { color:var(--danger); border-color:rgba(181,84,74,.35); }
.ocr-result { font-size:11.5px; color:var(--ok); display:flex; gap:6px; }
.ocr-error  { font-size:11px; color:var(--danger); margin-top:2px; }
.ocr-preview {
  margin-top:8px; border:1px solid var(--line-1); border-radius:var(--r-md);
  background:var(--bg-2); overflow:hidden; aspect-ratio:16 / 9;
  display:flex; align-items:center; justify-content:center; position:relative;
}
.ocr-preview img {
  width:100%; height:100%; object-fit:contain; display:block;
}
.ocr-preview-label {
  position:absolute; left:12px; bottom:12px; max-width:calc(100% - 24px);
  padding:8px 10px; border-radius:var(--r-md);
  background:rgba(18,24,22,.88); border:1px solid rgba(255,255,255,.18);
  color:#fff; display:grid; gap:2px; box-shadow:0 10px 24px rgba(0,0,0,.22);
}
.ocr-preview-label.warn { border-color:rgba(197,138,58,.7); }
.ocr-label-title { font-size:9px; color:rgba(255,255,255,.68); letter-spacing:0.06em; }
.ocr-preview-label strong { font-size:18px; line-height:1.05; letter-spacing:0; }
.ocr-label-meta { font-size:10px; color:rgba(255,255,255,.72); }
.recognized-plate {
  margin-top:8px; padding:10px 12px; border-radius:var(--r-md);
  border:1px solid var(--accent-line); background:var(--accent-soft);
  display:grid; grid-template-columns:1fr auto; gap:4px 10px; align-items:center;
}
.recognized-lbl { font-size:9.5px; letter-spacing:0.06em; color:var(--text-3); }
.recognized-num { font-size:18px; font-weight:800; color:var(--accent); letter-spacing:0; }
.recognized-meta { grid-column:1 / -1; font-size:10.5px; color:var(--text-4); }

.picked-card {
  background:var(--bg-2); border:1px solid var(--line-1); border-radius:var(--r-md);
  padding:10px 12px; display:flex; flex-direction:column; gap:4px;
}
.picked-row { display:flex; justify-content:space-between; align-items:center; gap:10px; }
.picked-lbl { font-size:9.5px; letter-spacing:0.06em; color:var(--text-4); }
.picked-val { font-size:13px; font-weight:600; color:var(--text-1); }

/* control */
.control-actions { display:flex; gap:8px; align-items:center; }
.control-actions .btn { font-size:13px; padding:10px 18px; display:flex; align-items:center; gap:6px; }
.btn-sm { font-size:11.5px !important; padding:6px 12px !important; }
.btn-danger {
  background:var(--danger); color:#fff; border-radius:var(--r-md);
  cursor:pointer; font-weight:600; border:none; transition:opacity .15s;
}
.btn-danger:hover:not(:disabled) { opacity:.85; }
.btn:disabled { opacity:.45; cursor:not-allowed; }

/* event 등록 */
.event-form { display:flex; flex-direction:column; gap:10px; }
.fatigue-form {
  display:grid; grid-template-columns:1fr 1fr; gap:10px;
}
.fatigue-form .form-row-wide { grid-column:1 / -1; }
@media (max-width: 1400px) {
  .fatigue-form { grid-template-columns:1fr; }
}
.event-submit { font-size:13px; padding:10px 18px; display:inline-flex; align-items:center; gap:6px; align-self:flex-start; }

/* planned */
.planned-list { display:flex; flex-direction:column; gap:6px; }
.planned-row {
  display:flex; align-items:center; gap:10px; padding:7px 10px;
  background:var(--bg-2); border-radius:var(--r-sm); font-size:12px;
}
.planned-time { color:var(--accent); font-weight:700; min-width:60px; }
.planned-label { color:var(--text-2); }

/* ── 차트 ───────────────────────── */
.chart-card { padding:18px; }
.chart-hdr  { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.card-title { font-size:14px; font-weight:700; color:var(--text-1); }
.chart-sub  { font-size:11px; color:var(--text-4); }
.chart-svg  { width:100%; height:260px; display:block; }
.chart-empty {
  height:260px; display:flex; align-items:center; justify-content:center;
  color:var(--text-4); font-size:12.5px;
  border:1px dashed var(--line-2); border-radius:var(--r-sm);
}
.chart-y-lbl, .chart-x-lbl { font-family:var(--font-mono); font-size:9.5px; }

/* event log */
.event-card { padding:18px; }
.event-list { display:flex; flex-direction:column; gap:1px; max-height:320px; overflow-y:auto; }
.event-row {
  display:flex; align-items:center; gap:10px; padding:8px 10px;
  border-radius:var(--r-sm); font-size:12px;
}
.event-row:hover { background:var(--bg-2); }
.ev-icon  { font-size:11px; flex-shrink:0; width:14px; text-align:center; }
.ev-time  { font-size:11px; color:var(--text-3); flex-shrink:0; min-width:42px; }
.ev-label { flex:1; color:var(--text-2); }
.ev-score { font-size:11px; font-weight:700; flex-shrink:0; }
.empty-hint { font-size:11.5px; color:var(--text-4); padding:14px 0; text-align:center; }

@keyframes pulse-ring {
  0%   { box-shadow: 0 0 0 0 rgba(94, 138, 111, 0.5); }
  70%  { box-shadow: 0 0 0 8px rgba(94, 138, 111, 0); }
  100% { box-shadow: 0 0 0 0 rgba(94, 138, 111, 0); }
}
</style>
