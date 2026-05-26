import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { simulationApi } from '@/api/simulationApi'
import { thresholdApi } from '@/api/thresholdApi'

const LS_KEY = 'logmile.simulation.v2'

/**
 * 시뮬레이션 스토어 (v2)
 * - 운행이 시작되면 페이지를 떠나거나 새로고침해도 진행 상태가 유지된다.
 * - 점수/이벤트 누적 계산은 클라이언트에서 (백엔드 점수 공식과 동일한 임계값 사용),
 *   서버에는 4개 엔드포인트(start/rest-events/fatigue-events/stop)만 호출한다.
 */
export const useSimulationStore = defineStore('simulation', () => {
  /* ─── 임계값 ─── */
  const T = ref({})
  const thresholdsLoaded = ref(false)

  async function loadThresholds() {
    if (thresholdsLoaded.value) return
    try {
      const res = await thresholdApi.getAll()
      const map = {}
      res.data.forEach(t => { map[(t.thresholdKey || '').toUpperCase()] = t.thresholdValue })
      T.value = map
    } catch (e) {
      T.value = {
        CONTINUOUS_DRIVING_90: 90, CONTINUOUS_DRIVING_90_DELTA: 10,
        CONTINUOUS_DRIVING_120: 120, CONTINUOUS_DRIVING_120_DELTA: 25,
        CONTINUOUS_DRIVING_180: 180, CONTINUOUS_DRIVING_180_DELTA: 45,
        CONTINUOUS_DRIVING_240: 240, CONTINUOUS_DRIVING_240_DELTA: 65,
        DAILY_DRIVING_360: 360, DAILY_DRIVING_360_DELTA: 10,
        DAILY_DRIVING_480: 480, DAILY_DRIVING_480_DELTA: 25,
        DAILY_DRIVING_600: 600, DAILY_DRIVING_600_DELTA: 40,
        NIGHT_DRIVING_30: 30, NIGHT_DRIVING_30_DELTA: 10,
        NIGHT_DRIVING_60: 60, NIGHT_DRIVING_60_DELTA: 25,
        NIGHT_DRIVING_120: 120, NIGHT_DRIVING_120_DELTA: 40,
        REST_VALID_MINUTES: 15, REST_SUFFICIENT_MINUTES: 30,
        REST_CORRECTION_VALID_SCORE_DELTA: -10,
        REST_CORRECTION_SUFFICIENT_SCORE_DELTA: -20,
      }
    }
    thresholdsLoaded.value = true
  }

  /* ─── 상태 ─── */
  const mode = ref('MANUAL')               // 'MANUAL' | 'SCENARIO'
  const scenarioType = ref('A')             // 'A' | 'B' | 'C'
  const vehicleId = ref(null)
  const vehiclePlateNo = ref('')
  const driverId = ref(null)
  const driverName = ref('')
  const ocrConfidence = ref(0.95)

  const isRunning = ref(false)
  const driveLogId = ref(null)
  const startedAtIso = ref('')             // ISO local 'YYYY-MM-DDTHH:mm'
  const endedAtIso = ref('')
  const apiError = ref(null)
  const apiInfo = ref(null)

  // 이벤트: 휴식 / 피로도 / 위치 마커
  // { id, kind: 'rest'|'fatigue'|'marker', tIso, ...payload, scoreAfter }
  const events = ref([])

  // 시나리오 모드에서 예정된 이벤트 (start 후 사용자에게 보여주기만)
  const plannedEvents = ref([])

  // 다중 시뮬레이션 이력 관리 추가
  const simulations = ref([])
  const currentSimulationId = ref(null)

  // 클럭 (1초마다 갱신, 경과시간 표시용)
  const nowIso = ref(formatLocalDt(new Date()))
  let clockTimer = null

  function startClock() {
    if (clockTimer) return
    clockTimer = setInterval(() => { nowIso.value = formatLocalDt(new Date()) }, 1000)
  }
  function stopClock() {
    if (clockTimer) { clearInterval(clockTimer); clockTimer = null }
  }

  /* ─── 유틸 ─── */
  function pad(n) { return String(n).padStart(2, '0') }
  function formatLocalDt(d) {
    return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
  }
  function parseLocalDt(s) { return s ? new Date(s) : new Date() }
  function toServerLdt(iso) { return iso ? (iso.length === 16 ? `${iso}:00` : iso) : null }
  function diffMinutes(aIso, bIso) {
    return Math.max(0, Math.round((parseLocalDt(bIso) - parseLocalDt(aIso)) / 60000))
  }
  function addMin(iso, mins) {
    const d = parseLocalDt(iso)
    d.setMinutes(d.getMinutes() + Number(mins || 0))
    return formatLocalDt(d)
  }
  function isNightHour(h) { return h >= 22 || h < 6 }

  /* ─── 점수 누적 계산 (events 기반) ─── */
  const scoreState = computed(() => {
    if (!startedAtIso.value) return { current: 0, max: 0, traces: [] }
    let score = 0, maxScore = 0
    const traces = [{ tIso: startedAtIso.value, score: 0, kind: 'start' }]

    // events 시간 순 정렬
    const sorted = [...events.value].sort((a, b) => parseLocalDt(a.tIso) - parseLocalDt(b.tIso))

    for (const ev of sorted) {
      if (ev.kind === 'rest') {
        const dur = ev.durationMin ?? diffMinutes(ev.startIso, ev.endIso)
        const VALID = T.value.REST_VALID_MINUTES ?? 15
        const SUFFICIENT = T.value.REST_SUFFICIENT_MINUTES ?? 30
        if (dur >= SUFFICIENT) score = Math.max(0, score + (T.value.REST_CORRECTION_SUFFICIENT_SCORE_DELTA ?? -20))
        else if (dur >= VALID) score = Math.max(0, score + (T.value.REST_CORRECTION_VALID_SCORE_DELTA ?? -10))
      } else if (ev.kind === 'fatigue') {
        // 직접 입력된 수치로 임계값 비교 → delta 누적
        const cont = ev.continuousDrivingMinutes || 0
        const daily = ev.dailyTotalDrivingMinutes || 0
        const night = ev.nightDrivingMinutes || 0
        const contSteps = [
          { min: T.value.CONTINUOUS_DRIVING_240 ?? 240, d: T.value.CONTINUOUS_DRIVING_240_DELTA ?? 65 },
          { min: T.value.CONTINUOUS_DRIVING_180 ?? 180, d: T.value.CONTINUOUS_DRIVING_180_DELTA ?? 45 },
          { min: T.value.CONTINUOUS_DRIVING_120 ?? 120, d: T.value.CONTINUOUS_DRIVING_120_DELTA ?? 25 },
          { min: T.value.CONTINUOUS_DRIVING_90  ?? 90,  d: T.value.CONTINUOUS_DRIVING_90_DELTA  ?? 10 },
        ]
        const dailySteps = [
          { min: T.value.DAILY_DRIVING_600 ?? 600, d: T.value.DAILY_DRIVING_600_DELTA ?? 40 },
          { min: T.value.DAILY_DRIVING_480 ?? 480, d: T.value.DAILY_DRIVING_480_DELTA ?? 25 },
          { min: T.value.DAILY_DRIVING_360 ?? 360, d: T.value.DAILY_DRIVING_360_DELTA ?? 10 },
        ]
        const nightSteps = [
          { min: T.value.NIGHT_DRIVING_120 ?? 120, d: T.value.NIGHT_DRIVING_120_DELTA ?? 40 },
          { min: T.value.NIGHT_DRIVING_60  ?? 60,  d: T.value.NIGHT_DRIVING_60_DELTA  ?? 25 },
          { min: T.value.NIGHT_DRIVING_30  ?? 30,  d: T.value.NIGHT_DRIVING_30_DELTA  ?? 10 },
        ]
        const top = (val, steps) => steps.find(s => val >= s.min)?.d || 0
        score += top(cont, contSteps) + top(daily, dailySteps) + top(night, nightSteps)
      }
      score = Math.max(0, Math.min(100, score))
      ev.scoreAfter = score
      traces.push({ tIso: ev.tIso, score, kind: ev.kind, ref: ev })
      if (score > maxScore) maxScore = score
    }

    return { current: score, max: maxScore, traces }
  })

  const currentScore = computed(() => scoreState.value.current)
  const maxScore = computed(() => scoreState.value.max)
  const levelLabel = computed(() => currentScore.value >= 70 ? 'DANGER' : currentScore.value >= 40 ? 'CAUTION' : 'NORMAL')

  const elapsedMinutes = computed(() => {
    if (!startedAtIso.value) return 0
    const endIso = isRunning.value ? nowIso.value : (endedAtIso.value || nowIso.value)
    return diffMinutes(startedAtIso.value, endIso)
  })

  const currentSimulationNo = computed(() => {
    if (!currentSimulationId.value) return ''
    const idx = simulations.value.findIndex(s => s.id === currentSimulationId.value)
    return idx > -1 ? `#${idx + 1}` : ''
  })

  /* ─── 액션 ─── */
  function selectVehicle(v) {
    if (!v) return
    vehicleId.value = v.id ?? null
    vehiclePlateNo.value = v.plateNo ?? ''
    driverId.value = null
    driverName.value = ''
  }
  function selectDriver(d) {
    if (!d) return
    driverId.value = d.id ?? null
    driverName.value = d.name ?? ''
  }
  function setMode(m) { 
    mode.value = m 
    plannedEvents.value = m === 'SCENARIO' ? buildPlannedEvents() : []
  }
  function setScenario(s) { 
    scenarioType.value = s 
    plannedEvents.value = mode.value === 'SCENARIO' ? buildPlannedEvents() : []
  }

  function buildPlannedEvents() {
    // 시나리오 모드에서 표시할 정해진 이벤트 시퀀스 세부 목록
    const presets = {
      A: [
        { offsetMin: 90, label: '광주 휴게소 진입 🚚' },
        { offsetMin: 90, label: '휴식 시작 ☕ (35분)' },
        { offsetMin: 125, label: '휴식 종료 🟢 (보정 -20pt)' },
        { offsetMin: 125, label: '광주 휴게소 진출 🚚' },
        { offsetMin: 160, label: '운행 종료 🏁 (최종 정상)' },
      ],
      B: [
        { offsetMin: 150, label: '여주 졸음쉼터 진입 🚚' },
        { offsetMin: 150, label: '휴식 시작 ☕ (10분, 무효)' },
        { offsetMin: 160, label: '휴식 종료 🟡 (보정 없음)' },
        { offsetMin: 160, label: '여주 졸음쉼터 진출 🚚' },
        { offsetMin: 230, label: '운행 종료 🏁 (최종 주의)' },
      ],
      C: [
        { offsetMin: 260, label: '충주 휴게소 진입 🚚' },
        { offsetMin: 260, label: '휴식 시작 ☕ (5분, 무효)' },
        { offsetMin: 265, label: '휴식 종료 🔴 (보정 없음)' },
        { offsetMin: 265, label: '충주 휴게소 진출 🚚' },
        { offsetMin: 385, label: '운행 종료 🏁 (최종 위험)' },
      ],
    }
    return (presets[scenarioType.value] || []).map((p, i) => ({ ...p, id: `pl-${i}` }))
  }

  /* ─── 다중 시뮬레이션 제어 ─── */
  function buildSimulationId(dlId) {
    return dlId ? `drive-${dlId}` : `sim-${Date.now()}`
  }

  function upsertCurrentSimulation() {
    if (!startedAtIso.value) return
    if (!currentSimulationId.value) {
      currentSimulationId.value = buildSimulationId(driveLogId.value)
    }

    const snap = {
      id: currentSimulationId.value,
      mode: mode.value,
      scenarioType: scenarioType.value,
      vehicleId: vehicleId.value,
      vehiclePlateNo: vehiclePlateNo.value,
      driverId: driverId.value,
      driverName: driverName.value,
      isRunning: isRunning.value,
      driveLogId: driveLogId.value,
      startedAtIso: startedAtIso.value,
      endedAtIso: endedAtIso.value,
      events: JSON.parse(JSON.stringify(events.value)),
      plannedEvents: JSON.parse(JSON.stringify(plannedEvents.value)),
      scoreState: {
        current: currentScore.value,
        max: maxScore.value,
        traces: JSON.parse(JSON.stringify(scoreState.value.traces))
      },
      updatedAt: new Date().toISOString()
    }

    const idx = simulations.value.findIndex(s => s.id === snap.id)
    if (idx > -1) {
      simulations.value[idx] = snap
    } else {
      simulations.value.push(snap)
    }
    persist()
  }

  function selectSimulation(id) {
    const snap = simulations.value.find(s => s.id === id)
    if (!snap) return

    currentSimulationId.value = snap.id
    mode.value = snap.mode
    scenarioType.value = snap.scenarioType
    vehicleId.value = snap.vehicleId
    vehiclePlateNo.value = snap.vehiclePlateNo
    driverId.value = snap.driverId
    driverName.value = snap.driverName
    isRunning.value = snap.isRunning
    driveLogId.value = snap.driveLogId
    startedAtIso.value = snap.startedAtIso
    endedAtIso.value = snap.endedAtIso
    events.value = JSON.parse(JSON.stringify(snap.events || []))
    plannedEvents.value = JSON.parse(JSON.stringify(snap.plannedEvents || []))

    if (isRunning.value) {
      startClock()
    } else {
      stopClock()
    }
    persist()
  }

  function deleteSimulation(id) {
    simulations.value = simulations.value.filter(s => s.id !== id)
    if (currentSimulationId.value === id) {
      reset()
    } else {
      persist()
    }
  }

  async function start({ startedAt } = {}) {
    apiError.value = null; apiInfo.value = null
    if (!vehicleId.value || !driverId.value) {
      apiError.value = '차량과 운전자를 선택해주세요.'
      return false
    }
    if (!vehiclePlateNo.value) {
      apiError.value = '차량 번호판이 비어 있습니다.'
      return false
    }
    const sIso = startedAt || formatLocalDt(new Date())
    startedAtIso.value = sIso
    try {
      const res = await simulationApi.start({
        vehicleId: vehicleId.value,
        driverId: driverId.value,
        scenarioType: scenarioType.value || 'A',
        recognizedPlateNo: vehiclePlateNo.value,
        ocrConfidence: ocrConfidence.value,
        manualInput: false,
        startedAt: toServerLdt(sIso),
      })
      driveLogId.value = res.data.driveLogId
      isRunning.value = true
      events.value = []
      endedAtIso.value = ''
      plannedEvents.value = mode.value === 'SCENARIO' ? buildPlannedEvents() : []
      currentSimulationId.value = buildSimulationId(res.data.driveLogId)
      startClock()
      apiInfo.value = `운행 시작 · DriveLog #${driveLogId.value}`
      upsertCurrentSimulation()
      return true
    } catch (e) {
      apiError.value = `시작 실패: ${e?.response?.data?.message || e.message || '서버 오류'}`
      return false
    }
  }

  async function addRest({ startIso, endIso }) {
    if (!isRunning.value) { apiError.value = '먼저 운행을 시작해주세요.'; return false }
    apiError.value = null; apiInfo.value = null
    try {
      await simulationApi.createRestEvent({
        driveLogId: driveLogId.value,
        restStartedAt: toServerLdt(startIso),
        restEndedAt: toServerLdt(endIso),
      })
      const dur = diffMinutes(startIso, endIso)
      events.value.push({
        id: `e-${Date.now()}`,
        kind: 'rest',
        tIso: endIso,                    // 휴식 종료 시점에 점수 반영
        startIso, endIso, durationMin: dur,
      })
      apiInfo.value = `휴식 이벤트 등록 (${dur}분)`
      upsertCurrentSimulation()
      return true
    } catch (e) {
      apiError.value = `휴식 등록 실패: ${e?.response?.data?.message || e.message}`
      return false
    }
  }

  async function addFatigue(payload) {
    if (!isRunning.value) { apiError.value = '먼저 운행을 시작해주세요.'; return false }
    apiError.value = null; apiInfo.value = null
    try {
      await simulationApi.createFatigueEvent({
        driveLogId: driveLogId.value,
        continuousDrivingMinutes: Number(payload.continuousDrivingMinutes || 0),
        dailyTotalDrivingMinutes: Number(payload.dailyTotalDrivingMinutes || 0),
        nightDrivingMinutes: Number(payload.nightDrivingMinutes || 0),
        restCount: Number(payload.restCount || 0),
        restViolationCount: Number(payload.restViolationCount || 0),
        occurredAt: toServerLdt(payload.occurredAtIso),
        reason: payload.reason || null,
      })
      events.value.push({
        id: `e-${Date.now()}`,
        kind: 'fatigue',
        tIso: payload.occurredAtIso,
        ...payload,
      })
      apiInfo.value = '피로도 이벤트 등록'
      upsertCurrentSimulation()
      return true
    } catch (e) {
      apiError.value = `피로도 등록 실패: ${e?.response?.data?.message || e.message}`
      return false
    }
  }

  function addMarker({ tIso, label, locationType }) {
    if (!isRunning.value) return false
    events.value.push({
      id: `e-${Date.now()}`,
      kind: 'marker',
      tIso, label, locationType,
    })
    apiInfo.value = `위치 마커 추가 · ${label}`
    upsertCurrentSimulation()
    return true
  }

  async function stop({ endedAt } = {}) {
    if (!isRunning.value || !driveLogId.value) return false
    apiError.value = null; apiInfo.value = null
    const eIso = endedAt || formatLocalDt(new Date())
    try {
      await simulationApi.stop(driveLogId.value, { endedAt: toServerLdt(eIso) })
      endedAtIso.value = eIso
      isRunning.value = false
      stopClock()
      apiInfo.value = `운행 종료 · DriveLog #${driveLogId.value}`
      upsertCurrentSimulation()
      return true
    } catch (e) {
      apiError.value = `종료 실패: ${e?.response?.data?.message || e.message}`
      return false
    }
  }

  function reset() {
    isRunning.value = false
    driveLogId.value = null
    currentSimulationId.value = null
    startedAtIso.value = ''
    endedAtIso.value = ''
    events.value = []
    plannedEvents.value = []
    apiError.value = null
    apiInfo.value = null
    isScenarioRunning.value = false
    stopClock()
    persist()
  }

  const isScenarioRunning = ref(false)

  async function runScenarioSequence() {
    if (isScenarioRunning.value) return
    if (!vehicleId.value || !driverId.value) {
      apiError.value = '차량과 운전자를 선택해주세요.'
      return false
    }
    if (!vehiclePlateNo.value) {
      apiError.value = '차량 번호판이 비어 있습니다.'
      return false
    }
    isScenarioRunning.value = true
    apiError.value = null
    apiInfo.value = `시나리오 ${scenarioType.value} 자동 시뮬레이션을 시작합니다...`

    try {
      // 1. 당일 날짜 기반 시작점 설정
      const today = new Date()
      let startHour = 9, startMin = 0
      if (scenarioType.value === 'B') { startHour = 21; startMin = 30 }
      else if (scenarioType.value === 'C') { startHour = 22; startMin = 0 }
      
      const startDate = new Date(today.getFullYear(), today.getMonth(), today.getDate(), startHour, startMin, 0)
      const baseIso = formatLocalDt(startDate)

      // 2. 운행 시작 API 전송
      const started = await start({ startedAt: baseIso })
      if (!started) throw new Error('운행 시작에 실패했습니다.')

      const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms))
      await delay(600) // API 간 가시적 시간차(0.6초) 부여

      // 3. 시나리오별 일괄 자동 이벤트 순차 연동
      if (scenarioType.value === 'A') {
        // [시나리오 A] 90분 운행 -> 35분 휴식 -> 160분 운행 종료
        await addMarker({ tIso: addMin(baseIso, 90), label: '광주 휴게소 진입', locationType: 'REST_AREA' })
        await delay(600)

        await addRest({ startIso: addMin(baseIso, 90), endIso: addMin(baseIso, 125) })
        await delay(600)

        await addMarker({ tIso: addMin(baseIso, 125), label: '광주 휴게소 진출', locationType: 'REST_AREA' })
        await delay(600)

        await stop({ endedAt: addMin(baseIso, 160) })

      } else if (scenarioType.value === 'B') {
        // [시나리오 B] 150분 운행 -> 10분 무효 휴식 -> 230분 운행 종료
        await addMarker({ tIso: addMin(baseIso, 150), label: '여주 졸음쉼터 진입', locationType: 'REST_AREA' })
        await delay(600)

        await addRest({ startIso: addMin(baseIso, 150), endIso: addMin(baseIso, 160) })
        await delay(600)

        await addMarker({ tIso: addMin(baseIso, 160), label: '여주 졸음쉼터 진출', locationType: 'REST_AREA' })
        await delay(600)

        await stop({ endedAt: addMin(baseIso, 230) })

      } else if (scenarioType.value === 'C') {
        // [시나리오 C] 260분 연속 주행 -> 5분 무효 휴식 -> 385분 운행 종료
        await addMarker({ tIso: addMin(baseIso, 260), label: '충주 휴게소 진입', locationType: 'REST_AREA' })
        await delay(600)

        await addRest({ startIso: addMin(baseIso, 260), endIso: addMin(baseIso, 265) })
        await delay(600)

        await addMarker({ tIso: addMin(baseIso, 265), label: '충주 휴게소 진출', locationType: 'REST_AREA' })
        await delay(600)

        await stop({ endedAt: addMin(baseIso, 385) })
      }

      apiInfo.value = `시나리오 ${scenarioType.value} 자동 시뮬레이션이 성공적으로 완수되었습니다!`
      return true
    } catch (e) {
      apiError.value = `시뮬레이션 구동 오류: ${e.message}`
      return false
    } finally {
      isScenarioRunning.value = false
    }
  }

  /* ─── persistence (localStorage) ─── */
  function persist() {
    try {
      const snap = {
        mode: mode.value, scenarioType: scenarioType.value,
        vehicleId: vehicleId.value, vehiclePlateNo: vehiclePlateNo.value,
        driverId: driverId.value, driverName: driverName.value,
        ocrConfidence: ocrConfidence.value,
        isRunning: isRunning.value, driveLogId: driveLogId.value,
        startedAtIso: startedAtIso.value, endedAtIso: endedAtIso.value,
        events: events.value, plannedEvents: plannedEvents.value,
        simulations: simulations.value,
        currentSimulationId: currentSimulationId.value,
      }
      localStorage.setItem(LS_KEY, JSON.stringify(snap))
    } catch (e) { /* quota / privacy mode 무시 */ }
  }
  function hydrate() {
    try {
      const raw = localStorage.getItem(LS_KEY)
      if (!raw) return
      const s = JSON.parse(raw)
      mode.value = s.mode ?? 'MANUAL'
      scenarioType.value = s.scenarioType ?? 'A'
      vehicleId.value = s.vehicleId ?? null
      vehiclePlateNo.value = s.vehiclePlateNo ?? ''
      driverId.value = s.driverId ?? null
      driverName.value = s.driverName ?? ''
      ocrConfidence.value = s.ocrConfidence ?? 0.95
      isRunning.value = !!s.isRunning
      driveLogId.value = s.driveLogId ?? null
      startedAtIso.value = s.startedAtIso ?? ''
      endedAtIso.value = s.endedAtIso ?? ''
      events.value = Array.isArray(s.events) ? s.events : []
      plannedEvents.value = Array.isArray(s.plannedEvents) ? s.plannedEvents : []
      simulations.value = Array.isArray(s.simulations) ? s.simulations : []
      currentSimulationId.value = s.currentSimulationId ?? null
      if (isRunning.value) startClock()
    } catch (e) { /* 손상된 데이터 무시 */ }
  }

  // 변경시 자동 저장 (디바운스 없이 가볍게)
  watch([mode, scenarioType, vehicleId, vehiclePlateNo, driverId, driverName, ocrConfidence], persist)

  return {
    // state
    T, thresholdsLoaded,
    mode, scenarioType,
    vehicleId, vehiclePlateNo, driverId, driverName, ocrConfidence,
    isRunning, driveLogId, startedAtIso, endedAtIso,
    events, plannedEvents, isScenarioRunning,
    apiError, apiInfo, nowIso,
    // 다중 이력 추가
    simulations, currentSimulationId,
    // derived
    currentScore, maxScore, levelLabel, elapsedMinutes, scoreState, currentSimulationNo,
    // actions
    loadThresholds,
    selectVehicle, selectDriver, setMode, setScenario,
    start, addRest, addFatigue, addMarker, stop, reset, runScenarioSequence,
    hydrate, persist,
    // 다중 이력 액션 추가
    buildSimulationId, upsertCurrentSimulation, selectSimulation, deleteSimulation,
    // utils (view에서 재사용)
    formatLocalDt, parseLocalDt, diffMinutes, addMin, toServerLdt,
  }
})
