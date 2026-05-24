<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppIcon from '@/components/common/AppIcon.vue'
import { driveHistoryApi } from '@/api/driveHistoryApi'

const route  = useRoute()
const router = useRouter()

const log        = ref(null)
const loading    = ref(true)
const error      = ref(null)
const gpsPoints  = ref([])
const mapContainer = ref(null)
let kakaoMap     = null
let kakaoLine    = null

onMounted(async () => {
  try {
    const [detailRes, gpsRes] = await Promise.all([
      driveHistoryApi.getDetail(route.params.id),
      driveHistoryApi.getGps(route.params.id).catch(() => ({ data: [] })),
    ])
    log.value       = detailRes.data
    gpsPoints.value = Array.isArray(gpsRes.data) ? gpsRes.data : []
  } catch (e) {
    error.value = '운행 상세 정보를 불러오는 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
    await nextTick()
    initGpsMap()
  }
})

function initGpsMap() {
  if (!gpsPoints.value.length || typeof window.kakao === 'undefined') return
  window.kakao.maps.load(() => {
    const container = mapContainer.value
    if (!container) return
    const first = gpsPoints.value[0]
    kakaoMap = new window.kakao.maps.Map(container, {
      center: new window.kakao.maps.LatLng(first.latitude, first.longitude),
      level: 7,
    })
    kakaoMap.addControl(new window.kakao.maps.ZoomControl(), window.kakao.maps.ControlPosition.RIGHT)

    // 경로 폴리라인
    const path = gpsPoints.value.map(p => new window.kakao.maps.LatLng(p.latitude, p.longitude))
    kakaoLine = new window.kakao.maps.Polyline({
      path,
      strokeWeight: 3,
      strokeColor: '#4A8FC0',
      strokeOpacity: 0.85,
      strokeStyle: 'solid',
    })
    kakaoLine.setMap(kakaoMap)

    // 시작 마커
    new window.kakao.maps.CustomOverlay({
      position: path[0],
      content: '<div style="background:#5E8A6F;color:#fff;font-size:10px;font-family:monospace;padding:3px 7px;border-radius:10px;border:2px solid #fff;white-space:nowrap;box-shadow:0 1px 4px rgba(0,0,0,.2)">출발</div>',
      yAnchor: 1.6,
    }).setMap(kakaoMap)

    // 종료 마커
    if (path.length > 1) {
      const lvlColor = log.value?.maxFatigueLevel === 'DANGER' ? '#B5544A' : log.value?.maxFatigueLevel === 'CAUTION' ? '#C58A3A' : '#515F7A'
      new window.kakao.maps.CustomOverlay({
        position: path[path.length - 1],
        content: `<div style="background:${lvlColor};color:#fff;font-size:10px;font-family:monospace;padding:3px 7px;border-radius:10px;border:2px solid #fff;white-space:nowrap;box-shadow:0 1px 4px rgba(0,0,0,.2)">도착</div>`,
        yAnchor: 1.6,
      }).setMap(kakaoMap)
    }

    // 지도 범위를 경로에 맞게 조정
    const bounds = new window.kakao.maps.LatLngBounds()
    path.forEach(p => bounds.extend(p))
    kakaoMap.setBounds(bounds)
  })
}

function fmtMin(m) {
  if (!m && m !== 0) return '—'
  const h = Math.floor(m / 60), mn = m % 60
  return h > 0 ? `${h}h ${mn}m` : `${mn}m`
}
function fmtDate(dt) {
  if (!dt) return '운행중'
  return String(dt).slice(0, 16).replace('T', ' ')
}
function levelOf(l) {
  if (l === 'DANGER')  return { label: 'DANGER',  color: 'var(--danger)', bg: 'var(--danger-soft)' }
  if (l === 'CAUTION') return { label: 'CAUTION', color: 'var(--warn)',   bg: 'var(--warn-soft)' }
  return                      { label: 'NORMAL',  color: 'var(--ok)',     bg: 'var(--ok-soft)' }
}

const level    = computed(() => levelOf(log.value?.maxFatigueLevel))
const confPct  = computed(() => Math.round((log.value?.ocrConfidence ?? 0) * 100))
const scorePct = computed(() => log.value?.maxFatigueScore ?? 0)

function restTypeStyle(t) {
  if (t === 'SUFFICIENT') return { color:'var(--ok)',     bg:'var(--ok-soft)',     border:'rgba(94,138,111,.3)' }
  if (t === 'VALID')      return { color:'#5B8FA8',       bg:'rgba(91,143,168,.12)', border:'rgba(91,143,168,.35)' }
  return                         { color:'var(--warn)',   bg:'var(--warn-soft)',   border:'rgba(197,138,58,.3)' }
}
function scenarioColor(s) {
  return s === 'C' ? 'var(--danger)' : s === 'B' ? 'var(--warn)' : 'var(--ok)'
}
function statusStyle(s) {
  if (s === 'RUNNING')   return { label:'운행중', color:'var(--accent)',  bg:'var(--accent-soft)' }
  if (s === 'COMPLETED') return { label:'완료',   color:'var(--ok)',     bg:'var(--ok-soft)'     }
  return                        { label:'중단',   color:'var(--danger)', bg:'var(--danger-soft)' }
}
function fatigueColor(score) {
  if (score >= 70) return 'var(--danger)'
  if (score >= 40) return 'var(--warn)'
  return 'var(--ok)'
}
</script>

<template>
  <div class="view">

    <!-- 로딩 / 에러 -->
    <div v-if="loading" class="state-row mono">운행 상세 정보 로드 중...</div>
    <div v-else-if="error" class="state-row" style="color:var(--danger)">{{ error }}</div>

    <template v-else-if="log">
      <!-- 헤더 -->
      <div class="breadcrumb mono">
        ADMIN / DRIVE_HISTORY / #{{ log.driveLogId }} · {{ log.plateNo }} · {{ log.driverName }}
      </div>

      <div class="page-header">
        <div style="display:flex;align-items:center;gap:12px;">
          <button class="btn btn-ghost back-btn" @click="router.back()">
            <AppIcon name="chevron-left" :size="13" />이전
          </button>
          <div>
            <h2 class="page-title">#{{ log.driveLogId }}</h2>
            <div class="plate-sub">{{ log.plateNo }} · {{ log.driverName }}</div>
          </div>
        </div>
        <div style="display:flex;align-items:center;gap:8px;">
          <span class="status-chip mono"
            :style="{ color: statusStyle(log.status).color, background: statusStyle(log.status).bg }">
            {{ statusStyle(log.status).label }}
          </span>
          <span v-if="log.scenarioType" class="scenario-chip mono" :style="{ color: scenarioColor(log.scenarioType) }">
            SCENARIO {{ log.scenarioType }}
          </span>
        </div>
      </div>

      <!-- 메타 그리드 -->
      <div class="card meta-card">
        <div class="meta-grid">
          <div class="meta-item">
            <div class="meta-lbl mono">STARTED_AT</div>
            <div class="meta-val">{{ fmtDate(log.startedAt) }}</div>
          </div>
          <div class="meta-item">
            <div class="meta-lbl mono">ENDED_AT</div>
            <div class="meta-val">{{ fmtDate(log.endedAt) }}</div>
          </div>
          <div class="meta-item">
            <div class="meta-lbl mono">TOTAL_DRIVE</div>
            <div class="meta-val">{{ fmtMin(log.totalDrivingMinutes) }}</div>
          </div>
          <div class="meta-item">
            <div class="meta-lbl mono">NIGHT_DRIVE</div>
            <div class="meta-val">{{ fmtMin(log.nightDrivingMinutes) }}</div>
          </div>
          <div class="meta-item">
            <div class="meta-lbl mono">REST_COUNT</div>
            <div class="meta-val">{{ log.totalRestCount ?? 0 }}회</div>
          </div>
          <div class="meta-item">
            <div class="meta-lbl mono">PEAK_SCORE</div>
            <div class="meta-val" :style="{ color: level.color }">{{ log.maxFatigueScore ?? 0 }}</div>
          </div>
          <div class="meta-item">
            <div class="meta-lbl mono">LEVEL</div>
            <div class="meta-val">
              <span class="level-chip mono" :style="{ color: level.color }">{{ level.label }}</span>
            </div>
          </div>
          <div class="meta-item">
            <div class="meta-lbl mono">OCR_CONF</div>
            <div class="meta-val">{{ confPct }}%
              <span v-if="log.manualInput" class="manual-badge mono">MANUAL</span>
              <span v-else-if="confPct < 85" class="manual-badge mono" style="background:var(--warn-soft);color:var(--warn);border-color:rgba(197,138,58,.3)">low</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 점수 바 -->
      <div class="card score-card">
        <div class="score-hdr">
          <span class="card-title">최고 피로 점수</span>
          <span class="mono score-num" :style="{ color: level.color }">{{ log.maxFatigueScore ?? 0 }}</span>
        </div>
        <div class="score-bar-bg">
          <div class="score-band band-n" /><div class="score-band band-c" /><div class="score-band band-d" />
          <div class="score-needle" :style="{ left: scorePct + '%' }" />
          <div class="score-tick" style="left:40%" /><div class="score-tick" style="left:70%" />
          <span class="band-lbl" style="left:5%;color:var(--ok)">NORMAL</span>
          <span class="band-lbl" style="left:42%;color:var(--warn)">CAUTION</span>
          <span class="band-lbl" style="left:72%;color:var(--danger)">DANGER</span>
        </div>
        <div class="band-nums mono">
          <span>0</span><span>20</span><span>40</span><span>60</span><span>80</span><span>100</span>
        </div>
      </div>

      <!-- GPS 경로 지도 -->
      <div v-if="gpsPoints.length" class="card" style="padding:0;overflow:hidden;margin-bottom:16px;">
        <div style="padding:14px 18px 10px;border-bottom:1px solid var(--line-2);display:flex;align-items:center;justify-content:space-between;">
          <div class="card-title">GPS 운행 경로</div>
          <span class="mono" style="font-size:10px;color:var(--text-4)">{{ gpsPoints.length }}포인트</span>
        </div>
        <div ref="mapContainer" style="width:100%;height:320px;"></div>
      </div>
      <div v-else-if="!loading" class="card" style="padding:16px 18px;margin-bottom:16px;display:flex;align-items:center;gap:8px;">
        <span style="font-size:12px;color:var(--text-4);font-family:var(--font-mono);">GPS ROUTE · 기록된 GPS 데이터가 없습니다</span>
      </div>

      <div class="two-col">
        <!-- 피로도 이벤트 타임라인 -->
        <div class="card timeline-card">
          <div class="card-hdr">
            <div class="card-title">피로도 이벤트</div>
            <span class="mono" style="font-size:11px;color:var(--text-4)">{{ log.fatigueEvents?.length ?? 0 }}건</span>
          </div>
          <div v-if="!log.fatigueEvents?.length" class="empty-txt">피로도 이벤트가 없습니다.</div>
          <div v-else class="timeline-list">
            <div v-for="(ev, i) in log.fatigueEvents" :key="ev.id" class="tl-item">
              <div class="tl-line-wrap">
                <div class="tl-dot" :class="{ matched: ev.fatigueScore >= 40 }" />
                <div v-if="i < log.fatigueEvents.length - 1" class="tl-line" />
              </div>
              <div class="tl-body">
                <div class="tl-top">
                  <span class="mono tl-time">{{ fmtDate(ev.occurredAt).slice(11) }}</span>
                  <span class="mono" style="font-size:12px;font-weight:700"
                    :style="{ color: fatigueColor(ev.fatigueScore) }">{{ ev.fatigueScore }}점</span>
                  <span class="mono" style="font-size:10px;color:var(--text-4)">{{ ev.fatigueLevel }}</span>
                </div>
                <div v-if="ev.reason" class="tl-loc">{{ ev.reason }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 휴식 이벤트 -->
        <div class="card rest-card">
          <div class="card-hdr">
            <div class="card-title">휴식 이벤트</div>
            <span class="mono" style="font-size:11px;color:var(--text-4)">{{ log.restEvents?.length ?? 0 }}건</span>
          </div>
          <div v-if="!log.restEvents?.length" class="empty-txt">해당 운행의 휴식 이벤트가 없습니다.</div>
          <div v-else class="rest-list">
            <div v-for="r in log.restEvents" :key="r.id" class="rest-item">
              <div class="rest-top">
                <span class="rest-type-chip mono"
                  :style="{ color: restTypeStyle(r.restType).color, background: restTypeStyle(r.restType).bg, borderColor: restTypeStyle(r.restType).border }">
                  {{ r.restType }}
                </span>
                <span class="mono rest-dur">{{ r.restMinutes }}분</span>
              </div>
              <div class="rest-time mono">{{ fmtDate(r.restStartedAt) }} → {{ fmtDate(r.restEndedAt) }}</div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.view { display:flex; flex-direction:column; gap:16px; padding:32px 32px 40px; }
.breadcrumb { font-size:11px; color:var(--text-4); letter-spacing:0.04em; }
.state-row  { padding:40px; text-align:center; font-size:13px; color:var(--text-4); }

.page-header { display:flex; align-items:center; justify-content:space-between; }
.page-title  { font-size:22px; font-weight:700; color:var(--text-1); margin:0 0 2px; letter-spacing:-0.01em; }
.plate-sub   { font-size:12px; color:var(--text-3); }
.back-btn    { display:flex; align-items:center; gap:5px; font-size:12.5px; padding:7px 13px; }

.status-chip {
  display:inline-block; padding:3px 10px; border-radius:var(--r-sm);
  font-size:10.5px; font-weight:700; letter-spacing:0.06em;
}
.scenario-chip { font-size:12px; font-weight:800; }
.level-chip { font-size:12px; font-weight:700; }
.manual-badge {
  display:inline-block; font-size:9px; padding:1px 6px;
  background:var(--warn-soft); color:var(--warn); border:1px solid rgba(197,138,58,.3);
  border-radius:var(--r-sm); margin-left:6px; vertical-align:middle;
}

.meta-card  { padding:20px; }
.meta-grid  { display:grid; grid-template-columns:repeat(4,1fr); gap:16px; }
.meta-item  { display:flex; flex-direction:column; gap:4px; }
.meta-lbl   { font-size:9.5px; letter-spacing:0.07em; color:var(--text-4); }
.meta-val   { font-size:14px; font-weight:600; color:var(--text-1); }

.score-card { padding:20px; }
.score-hdr  { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.card-title { font-size:14px; font-weight:700; color:var(--text-1); }
.score-num  { font-size:28px; font-weight:800; letter-spacing:-0.02em; }
.score-bar-bg {
  position:relative; height:28px; border-radius:3px; overflow:visible; display:flex;
}
.score-band { height:100%; }
.band-n { flex:40; background:rgba(94,138,111,.2); border-radius:3px 0 0 3px; }
.band-c { flex:30; background:rgba(197,138,58,.28); }
.band-d { flex:30; background:rgba(181,84,74,.32); border-radius:0 3px 3px 0; }
.score-tick { position:absolute; top:0; bottom:0; width:1px; background:var(--text-3); }
.score-needle {
  position:absolute; top:-4px; bottom:-4px; width:2px;
  background:var(--text-1); border-radius:1px;
  box-shadow:0 0 0 3px rgba(255,255,255,.6);
  transform:translateX(-50%);
}
.band-lbl {
  position:absolute; top:50%; transform:translateY(-50%);
  font-family:var(--font-mono); font-size:10.5px; font-weight:700;
}
.band-nums {
  display:flex; justify-content:space-between;
  margin-top:6px; font-size:10px; color:var(--text-4);
}

.two-col { display:grid; grid-template-columns:1fr 1fr; gap:10px; }

.timeline-card { padding:20px; }
.card-hdr { display:flex; align-items:center; justify-content:space-between; margin-bottom:14px; }
.timeline-list { display:flex; flex-direction:column; max-height:360px; overflow-y:auto; }
.tl-item { display:flex; gap:12px; }
.tl-line-wrap { display:flex; flex-direction:column; align-items:center; flex-shrink:0; }
.tl-dot {
  width:10px; height:10px; border-radius:50%; flex-shrink:0;
  background:var(--bg-3); border:2px solid var(--line-2);
}
.tl-dot.matched { background:var(--warn); border-color:var(--warn); }
.tl-line { flex:1; width:1px; background:var(--line-2); min-height:12px; }
.tl-body { padding-bottom:16px; flex:1; }
.tl-top  { display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.tl-time { font-size:12px; font-weight:700; color:var(--text-1); }
.tl-loc  { font-size:12px; color:var(--text-3); margin-top:3px; }

.rest-card { padding:20px; }
.rest-list { display:flex; flex-direction:column; gap:10px; }
.rest-item { padding:12px; background:var(--bg-2); border:1px solid var(--line-1); border-radius:var(--r-md); }
.rest-top  { display:flex; align-items:center; justify-content:space-between; margin-bottom:6px; }
.rest-type-chip {
  display:inline-block; padding:2px 8px; border-radius:var(--r-sm);
  font-size:10px; font-weight:700; letter-spacing:0.05em; border:1px solid;
}
.rest-dur  { font-size:13px; font-weight:700; color:var(--text-1); }
.rest-time { font-size:11.5px; color:var(--text-3); }
.empty-txt { font-size:12.5px; color:var(--text-4); padding:20px 0; text-align:center; }
</style>
