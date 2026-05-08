<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '@/components/common/AppIcon.vue'
import { vehicles, alerts, days, plateTimeline, levelOf, fmtMin } from '@/data/mockData'

const router = useRouter()

/* ─── State ─── */
const now     = ref('2026.05.04 14:32:08')
const mapTab  = ref('전체')
const mapTabs = ['전체', '위험', '주의', '정상']
const selected = ref(vehicles[0])

function select(v) { selected.value = v }
function refresh()  { now.value = new Date().toLocaleString('ko-KR') }

/* ─── 차량 필터 ─── */
const runningVehicles = computed(() => {
  const r = vehicles.filter(v => v.status === 'RUNNING')
  if (mapTab.value === '전체') return r
  const need = mapTab.value === '위험' ? 'DANGER' : mapTab.value === '주의' ? 'CAUTION' : 'NORMAL'
  return r.filter(v => v.level === need)
})

/* ─── KPI ─── */
const runningCount = computed(() => vehicles.filter(v => v.status === 'RUNNING').length)
const dangerCount  = computed(() => vehicles.filter(v => v.status === 'RUNNING' && v.level === 'DANGER').length)
const cautionCount = computed(() => vehicles.filter(v => v.status === 'RUNNING' && v.level === 'CAUTION').length)
const normalCount  = computed(() => vehicles.filter(v => v.status === 'RUNNING' && v.level === 'NORMAL').length)
const idleCount    = computed(() => vehicles.filter(v => v.status === 'IDLE').length)
const avgScore     = computed(() => {
  const r = vehicles.filter(v => v.status === 'RUNNING')
  return r.length ? Math.round(r.reduce((a, v) => a + v.score, 0) / r.length * 10) / 10 : 0
})

const kpis = computed(() => [
  { label:'TOTAL FLEET', value:vehicles.length,     unit:'대', delta:'한라물류',    dir:'flat', color:'var(--text-1)',  sub:'전체 등록',        spark:[10,10,10,10,10,10,10] },
  { label:'RUNNING',     value:runningCount.value,  unit:'대', delta:'+2 last hr', dir:'up',   color:'var(--accent)',  sub:'RUNNING',          spark:[4,5,6,7,7,8,runningCount.value] },
  { label:'IDLE',        value:idleCount.value,     unit:'대', delta:'차고지',     dir:'flat', color:'var(--text-2)',  sub:'대기',             spark:[6,5,4,3,3,2,idleCount.value] },
  { label:'NORMAL',      value:normalCount.value,   unit:'대', delta:'0~39점',     dir:'flat', color:'var(--ok)',      sub:'정상',             spark:[3,3,3,3,3,3,normalCount.value] },
  { label:'CAUTION',     value:cautionCount.value,  unit:'대', delta:'40~69점',    dir:'flat', color:'var(--warn)',    sub:'주의',             spark:[1,2,2,2,3,3,cautionCount.value] },
  { label:'DANGER',      value:dangerCount.value,   unit:'대', delta:'70+점',      dir:dangerCount.value>0?'up':'flat', color:'var(--danger)', sub:'위험 (전화 권고)', spark:[0,0,1,1,1,2,dangerCount.value] },
])

/* ─── Donut ─── */
const donutSegs = computed(() => [
  { label:'정상', range:'0~39점',   value:normalCount.value,  color:'#5E8A6F' },
  { label:'주의', range:'40~69점',  value:cautionCount.value, color:'#C58A3A' },
  { label:'위험', range:'70~100점', value:dangerCount.value,  color:'#B5544A' },
])
const DSIZE = 180, DTHICK = 22
const DR = (DSIZE - DTHICK) / 2
const DC = 2 * Math.PI * DR
const donutArcs = computed(() => {
  const total = donutSegs.value.reduce((a, b) => a + b.value, 0) || 1
  let acc = 0
  return donutSegs.value.map(s => {
    const frac = s.value / total
    const dash = `${frac * DC} ${DC}`
    const offset = -acc * DC
    acc += frac
    return { color: s.color, dash, offset }
  })
})

/* ─── 알림 ─── */
const alertCount = computed(() => alerts.filter(a => a.sev !== 'info').length)
function alertBorderColor(sev) {
  return sev === 'danger' ? 'var(--danger)' : sev === 'warn' ? 'var(--warn)' : 'var(--info)'
}
function alertBg(sev) {
  return sev === 'danger' ? 'var(--danger-soft)' : sev === 'warn' ? 'var(--warn-soft)' : 'var(--info-soft)'
}
function alertTextColor(sev) {
  return sev === 'danger' ? 'var(--danger)' : sev === 'warn' ? 'var(--warn)' : 'var(--info)'
}

/* ─── Fleet Map ─── */
const POS = {
  '1024':{x:280,y:215}, '1025':{x:200,y:230}, '1026':{x:295,y:250}, '1027':{x:320,y:165},
  '1028':{x:360,y:350}, '1029':{x:320,y:230}, '1030':{x:245,y:130}, '1031':{x:220,y:175},
  '1032':{x:250,y:260}, '1033':{x:256,y:264},
}
const placedVehicles = computed(() =>
  runningVehicles.value.map(v => {
    const num = (v.plate.match(/(\d{4})\s*$/) || [])[1] || ''
    const pos = POS[num] || { x: 280, y: 250 }
    return {
      plate: v.plate, level: v.level, x: pos.x, y: pos.y,
      fill: v.level === 'DANGER' ? 'var(--danger)' : v.level === 'CAUTION' ? 'var(--warn)' : 'var(--accent)',
    }
  })
)

/* ─── 선택 차량 드릴다운 ─── */
const timelinePoints = computed(() => {
  const v = selected.value
  if (v.status !== 'RUNNING') return [{ t:0, score:0, speed:0, event:'운행 없음' }, { t:720, score:0, speed:0, event:null }]
  const pts = [], target = v.score
  for (let m = 0; m <= 720; m += 30) {
    const h = m / 60
    const progress = Math.min(1, m / 600)
    let s = 5 + (target - 5) * progress
    if (h > 3.5 && h < 3.9 && v.restValid + v.restSuff > 0) s = Math.max(5, s - 12)
    if (h > 9.4 && h < 9.7 && v.restSuff > 0) s = Math.max(5, s - 20)
    s = Math.max(2, Math.min(98, s + Math.sin(m / 45) * 3))
    let speed = m < 100 ? 60 : (h > 3.5 && h < 3.9) ? 0 : (h > 9.4 && h < 9.7) ? 0 : 80 + Math.sin(m / 60) * 8
    let event = null
    if (m === 0) event = '운행 시작'
    else if (m === 90) event = '90분'
    else if (m === 240) event = '4h · 야간진입'
    else if (m === 360) event = '휴식'
    else if (target >= 40 && Math.abs(s - 40) < 3 && !pts.some(p => p.event === '주의 진입')) event = '주의 진입'
    else if (target >= 70 && Math.abs(s - 70) < 3 && !pts.some(p => p.event === '위험 진입')) event = '위험 진입'
    pts.push({ t: m, score: Math.round(s), speed: Math.round(speed), event })
  }
  return pts
})
const peakScore = computed(() => Math.max(...timelinePoints.value.map(p => p.score)))

// Timeline SVG helpers
const tlPx = t => 40 + (t / 720) * 740
const tlPy = s => 20 + (1 - s / 100) * 180
const tlScorePts  = computed(() => timelinePoints.value.map(p => `${tlPx(p.t)},${tlPy(p.score)}`).join(' '))
const tlScoreArea = computed(() => {
  const pts = timelinePoints.value
  return `${tlPx(pts[0].t)},200 ${tlScorePts.value} ${tlPx(pts[pts.length - 1].t)},200`
})
const tlSpeedPts = computed(() => timelinePoints.value.map(p => `${tlPx(p.t)},${20 + (1 - Math.min(120, p.speed) / 120) * 180}`).join(' '))
const tlEvents   = computed(() => timelinePoints.value.filter(p => p.event))
function tlEventColor(e) {
  if (e === '위험 진입') return 'var(--danger)'
  if (e === '주의 진입' || e === '야간 진입') return 'var(--warn)'
  return 'var(--accent)'
}

/* ─── Plate recognition ─── */
function sourceColor(s) {
  if (s === 'DEPARTURE' || s === 'ARRIVAL') return 'var(--accent)'
  if (s === 'REST_AREA_CCTV') return 'var(--ok)'
  return 'var(--text-2)'
}
const plateAvgConf = computed(() => (plateTimeline.reduce((a, p) => a + p.conf, 0) / plateTimeline.length).toFixed(2))

/* ─── Heatmap ─── */
const heatmapDrivers = computed(() => {
  const make = (start, end, dipAt, dipDepth) => {
    const arr = new Array(24).fill(null)
    let acc = 5
    for (let h = 0; h < 24; h++) {
      if (h < start || h > end) continue
      acc += (h >= 22 || h <= 5) ? 8 : 5
      if (h === dipAt) acc = Math.max(5, acc - dipDepth)
      arr[h] = Math.min(95, Math.round(acc))
    }
    return arr
  }
  return vehicles.filter(v => v.status === 'RUNNING').slice(0, 8).map(v => {
    const start = parseInt(v.startedAt.split(':')[0], 10) || 6
    const cur = 14, dipAt = start + Math.floor((cur - start) / 2)
    return { name: v.driver, plate: v.plate, hours: make(start, cur, dipAt, v.restValid * 10 + v.restSuff * 20) }
  })
})
function heatCellColor(s) {
  if (s === null || s === undefined) return 'var(--bg-2)'
  if (s >= 70) return '#B5544A'
  if (s >= 55) return '#C58A3A'
  if (s >= 40) return '#C5A56A'
  if (s >= 20) return '#92A08F'
  return '#5E8A6F'
}

/* ─── Ranking ─── */
const rankingItems = computed(() =>
  vehicles.filter(v => v.status === 'RUNNING').slice().sort((a, b) => b.score - a.score).slice(0, 6)
    .map(v => ({
      label: v.driver, sub: v.plate, value: v.score, max: 100,
      color: v.level === 'DANGER' ? 'var(--danger)' : v.level === 'CAUTION' ? 'var(--warn)' : 'var(--ok)',
      tag: v.level === 'DANGER' ? '위험' : v.level === 'CAUTION' ? '주의' : '정상',
    }))
)

/* ─── Trend chart ─── */
const trendAvg   = computed(() => Math.round(days.reduce((a, d) => a + d.avgScore, 0) / days.length * 10) / 10)
const trendDelta = computed(() => {
  const a = days.slice(0, 7).reduce((s, d) => s + d.avgScore, 0) / 7
  const b = days.slice(7).reduce((s, d) => s + d.avgScore, 0) / 7
  return Math.round((b - a) * 10) / 10
})
const dangerSum   = computed(() => days.reduce((a, d) => a + d.danger, 0))
const totalDriveH = computed(() => Math.round(days.reduce((a, d) => a + d.driveHours, 0) * 10) / 10)

const TC_H = 220
const tcBx = i => 40 + i * (740 / (days.length - 1))
const tcLy = s => 20 + (1 - s / 100) * (TC_H - 50)
const tcLinePts = computed(() => days.map((d, i) => `${tcBx(i)},${tcLy(d.avgScore)}`).join(' '))
const tcAreaPts = computed(() => `${tcBx(0)},${TC_H - 30} ${tcLinePts.value} ${tcBx(days.length - 1)},${TC_H - 30}`)

/* ─── Scatter ─── */
const scatterItems = computed(() =>
  vehicles.filter(v => v.status === 'RUNNING').map(v => ({
    x: v.dailyMin / 60, y: v.score, r: 5 + v.restMiss * 4,
    color: v.level === 'DANGER' ? '#B5544A' : v.level === 'CAUTION' ? '#C58A3A' : '#5E8A6F',
    label: v.plate.split(' ').pop().slice(0, 4),
  }))
)

/* ─── Event stream ─── */
const events = [
  { t:'14:32', v:'경기 80바 1024', e:'연속 6.4h · 야간 3.4h · 점수 78 → DANGER',        k:'danger' },
  { t:'14:18', v:'경기 80바 1029', e:'일일 10.3h · 휴식 누락 2회 · 점수 84 → DANGER',    k:'danger' },
  { t:'14:09', v:'경기 80바 1025', e:'연속 4.5h · 점수 56 → CAUTION 유지',               k:'warn'   },
  { t:'13:47', v:'경기 80바 1030', e:'연속 3.8h · 휴식 누락 1회 · 점수 48 → CAUTION',   k:'warn'   },
  { t:'13:21', v:'경기 80바 1031', e:'일일 7.1h · 야간 0.8h · 점수 41 → CAUTION 진입',  k:'warn'   },
  { t:'12:55', v:'경기 80바 1026', e:'충분 휴식(34분) · -20점 보정 · 점수 18 (NORMAL)', k:'info'   },
  { t:'12:14', v:'경기 80바 1028', e:'DEPARTURE · OCR 0.99 · 차고지 출발',               k:'info'   },
  { t:'11:42', v:'경기 80바 1027', e:'연속 3h 도달 · CAUTION 근접',                      k:'info'   },
  { t:'10:33', v:'경기 80바 1025', e:'REST_AREA_CCTV · 입장 휴게소 진입',                k:'info'   },
]
function evColor(k) { return k==='danger' ? 'var(--danger)' : k==='warn' ? 'var(--warn)' : 'var(--accent)' }

/* ─── Helpers ─── */
function levelLabel(l)     { return l==='DANGER' ? '위험' : l==='CAUTION' ? '주의' : '정상' }
function levelChipCls(l)   { return l==='DANGER' ? 'chip chip-danger' : l==='CAUTION' ? 'chip chip-warn' : 'chip chip-ok' }
function statusLabel(s)    { return s==='RUNNING' ? '운행중' : s==='COMPLETED' ? '완료' : s==='STOPPED' ? '중단' : '대기' }
function statusChipCls(s)  { return s==='RUNNING' ? 'chip chip-info' : s==='COMPLETED' ? 'chip chip-ok' : s==='STOPPED' ? 'chip chip-danger' : 'chip chip-mute' }
function scColor(c)        { return c==='C' ? 'var(--danger)' : c==='B' ? 'var(--warn)' : 'var(--ok)' }

// Sparkline helpers
function spkPts(data, w=64, h=18) {
  const max = Math.max(...data), min = Math.min(...data), span = max-min||1
  return data.map((d,i) => `${(i/(data.length-1))*w},${h-((d-min)/span)*(h-2)-1}`).join(' ')
}
function spkArea(data, w=64, h=18) { return `0,${h} ${spkPts(data,w,h)} ${w},${h}` }

// Factor bar
function fbPct(val, max) { return Math.min(100, val/max*100) }
function fbColor(val, thr) {
  if (thr[1] != null && val > thr[1]) return 'var(--danger)'
  if (thr[0] != null && val > thr[0]) return 'var(--warn)'
  return 'var(--ok)'
}
</script>

<template>
  <div class="fade-up" style="padding:32px 0 0;">
    <div class="container">

      <!-- ── 페이지 헤더 ── -->
      <div class="pg-header">
        <div>
          <div class="label-sm" style="color:var(--accent);margin-bottom:10px;">OPERATIONS / LIVE</div>
          <h1 class="pg-title">관제 대시보드</h1>
          <p class="pg-sub">오늘 · 2026.05.04 (월) · 한라물류센터 · 가상 시뮬레이션 환경</p>
        </div>
        <div style="display:flex;gap:8px;align-items:center;">
          <div class="mono" style="display:flex;gap:14px;font-size:11.5px;color:var(--text-3);align-items:center;">
            <span style="display:flex;align-items:center;gap:6px;"><span class="dot dot-ok blink"></span> connected</span>
            <span>{{ now }}</span>
          </div>
          <button class="btn btn-ghost"><AppIcon name="filter" :size="13"/> 필터</button>
          <button class="btn btn-ghost"><AppIcon name="download" :size="13"/> CSV</button>
          <button class="btn btn-primary" @click="refresh"><AppIcon name="refresh" :size="13"/> 새로고침</button>
        </div>
      </div>

      <!-- ── KPI Strip ── -->
      <div class="card kpi-strip" style="margin-bottom:16px;">
        <div v-for="(k, i) in kpis" :key="k.label" class="stat-tile"
          :style="{ borderRight: i < 5 ? '1px solid var(--line-1)' : 'none' }">
          <div v-if="i===0" :style="{ position:'absolute', top:0, left:0, right:0, height:'2px', background:k.color }"></div>
          <div class="label-sm" style="margin-bottom:10px;">{{ k.label }}</div>
          <div style="display:flex;align-items:baseline;gap:6px;">
            <span class="mono" :style="{ fontSize:'30px', fontWeight:700, color:k.color, lineHeight:1, letterSpacing:'-0.015em' }">{{ k.value }}</span>
            <span class="mono" style="font-size:12px;color:var(--text-3);">{{ k.unit }}</span>
          </div>
          <div style="margin-top:10px;display:flex;align-items:center;gap:8px;justify-content:space-between;">
            <div>
              <span class="mono" :style="{ fontSize:'11.5px', color: k.dir==='up' ? 'var(--ok)' : k.dir==='down' ? 'var(--danger)' : 'var(--text-3)' }">
                {{ k.dir==='up' ? '▲' : k.dir==='down' ? '▼' : '→' }} {{ k.delta }}
              </span>
              <span style="color:var(--text-4);font-size:11px;margin-left:6px;">{{ k.sub }}</span>
            </div>
            <svg width="64" height="18" style="display:block;">
              <defs>
                <linearGradient :id="'spk'+i" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" :stop-color="k.color" stop-opacity="0.28"/>
                  <stop offset="100%" :stop-color="k.color" stop-opacity="0"/>
                </linearGradient>
              </defs>
              <polyline :points="spkArea(k.spark)" :fill="'url(#spk'+i+')'" stroke="none"/>
              <polyline :points="spkPts(k.spark)" fill="none" :stroke="k.color" stroke-width="1.6" stroke-linejoin="round" stroke-linecap="round"/>
            </svg>
          </div>
        </div>
      </div>

      <!-- ── Hero: Fleet map + Donut/Alerts ── -->
      <div style="display:grid;grid-template-columns:1.6fr 1fr;gap:16px;margin-bottom:16px;">

        <!-- Fleet map -->
        <div class="card" style="display:grid;grid-template-rows:auto 1fr;overflow:hidden;">
          <div style="padding:18px 20px;border-bottom:1px solid var(--line-1);display:flex;justify-content:space-between;align-items:center;">
            <div>
              <div class="label-sm">FLEET MAP · LIVE</div>
              <div style="font-size:14px;color:var(--text-2);margin-top:2px;">실시간 차량 위치 · 운행 중 {{ runningCount }}대 / 전체 {{ vehicles.length }}대</div>
            </div>
            <div style="display:flex;gap:6px;">
              <button v-for="t in mapTabs" :key="t" @click="mapTab=t"
                :style="{ padding:'6px 10px', fontSize:'11.5px', fontFamily:'var(--font-mono)',
                          borderRadius:'4px', cursor:'pointer',
                          background: mapTab===t ? 'var(--accent-soft)' : 'var(--bg-2)',
                          color: mapTab===t ? 'var(--accent)' : 'var(--text-3)',
                          border: '1px solid '+(mapTab===t ? 'var(--accent-line)' : 'var(--line-2)'),
                          fontWeight: mapTab===t ? 700 : 500 }">
                {{ t }}
              </button>
            </div>
          </div>
          <div style="padding:14px;display:grid;grid-template-columns:1.05fr 1fr;gap:14px;">
            <!-- SVG 지도 -->
            <div style="position:relative;width:100%;aspect-ratio:1.05/1;background:var(--bg-2);border-radius:6px;overflow:hidden;border:1px solid var(--line-2);">
              <svg width="100%" height="100%" viewBox="0 0 600 580" preserveAspectRatio="xMidYMid meet">
                <path d="M270 30 L320 35 L360 50 L380 75 L370 110 L390 140 L420 170 L455 180 L470 215 L490 240 L500 280 L495 320 L480 360 L470 400 L450 430 L420 455 L380 475 L340 500 L300 520 L260 510 L220 480 L195 440 L180 400 L170 360 L155 320 L140 285 L130 245 L135 205 L155 170 L180 135 L210 100 L240 60 Z"
                  fill="var(--bg-1)" stroke="var(--slate-300)" stroke-width="1.4"/>
                <ellipse cx="200" cy="540" rx="40" ry="20" fill="var(--bg-1)" stroke="var(--slate-300)" stroke-width="1.2"/>
                <g stroke="var(--slate-300)" stroke-width="1.2" stroke-dasharray="3 4" fill="none">
                  <path d="M250 90 Q270 200 280 320 Q300 410 350 470"/>
                  <path d="M195 200 Q260 250 350 240 Q420 240 470 250"/>
                  <path d="M260 350 Q330 360 420 360"/>
                </g>
                <g font-family="var(--font-mono)" font-size="10" fill="var(--text-4)">
                  <circle cx="250" cy="100" r="2.5" fill="var(--text-3)"/><text x="258" y="103">서울</text>
                  <circle cx="290" cy="200" r="2"   fill="var(--text-3)"/><text x="298" y="203">대전</text>
                  <circle cx="380" cy="370" r="2"   fill="var(--text-3)"/><text x="388" y="373">부산</text>
                  <circle cx="200" cy="320" r="2"   fill="var(--text-3)"/><text x="158" y="323" text-anchor="end">광주</text>
                </g>
                <g v-for="v in placedVehicles" :key="v.plate">
                  <g :transform="'translate('+v.x+' '+v.y+')'">
                    <circle r="14" :fill="v.fill" opacity="0.18">
                      <animate v-if="v.level==='DANGER'" attributeName="r" from="10" to="22" dur="2s" repeatCount="indefinite"/>
                      <animate v-if="v.level==='DANGER'" attributeName="opacity" from="0.4" to="0" dur="2s" repeatCount="indefinite"/>
                    </circle>
                    <circle r="6" :fill="v.fill" stroke="var(--bg-1)" stroke-width="2"/>
                  </g>
                </g>
              </svg>
              <div style="position:absolute;top:14px;left:14px;display:flex;flex-direction:column;gap:6px;">
                <div class="chip chip-info"><span class="dot dot-brand"></span> {{ runningCount }}대 운행중</div>
                <div v-if="dangerCount" class="chip chip-danger"><span class="dot dot-danger"></span> {{ dangerCount }} 위험</div>
                <div v-if="cautionCount" class="chip chip-warn"><span class="dot dot-warn"></span> {{ cautionCount }} 주의</div>
              </div>
              <div style="position:absolute;bottom:14px;right:14px;background:rgba(255,255,255,0.92);border:1px solid var(--line-2);border-radius:6px;padding:8px 12px;font-family:var(--font-mono);font-size:10px;display:flex;flex-direction:column;gap:4px;">
                <div style="display:flex;gap:6px;align-items:center;"><span style="width:8px;height:8px;border-radius:50%;background:var(--danger);"></span> 위험</div>
                <div style="display:flex;gap:6px;align-items:center;"><span style="width:8px;height:8px;border-radius:50%;background:var(--warn);"></span> 주의</div>
                <div style="display:flex;gap:6px;align-items:center;"><span style="width:8px;height:8px;border-radius:50%;background:var(--accent);"></span> 정상</div>
              </div>
            </div>
            <!-- Running vehicles list -->
            <div style="display:flex;flex-direction:column;gap:8px;">
              <div class="label-sm" style="display:flex;justify-content:space-between;">
                <span>RUNNING VEHICLES</span>
                <span class="mono" style="color:var(--accent);">● {{ runningCount }}대 운행중</span>
              </div>
              <div v-for="v in vehicles.filter(v=>v.status==='RUNNING').slice(0,5)" :key="v.plate"
                @click="select(v)"
                :style="{ display:'grid', gridTemplateColumns:'auto 1fr auto', gap:'10px', alignItems:'center',
                          padding:'10px', cursor:'pointer', borderRadius:'4px',
                          background: selected.plate===v.plate ? 'var(--accent-soft)' : 'var(--bg-2)',
                          border:'1px solid '+(selected.plate===v.plate ? 'var(--accent-line)' : 'var(--line-2)') }">
                <!-- plate photo placeholder -->
                <div :style="{ width:'96px', height:'64px', borderRadius:'4px', flexShrink:0,
                               background:'linear-gradient(135deg,#DCDFE4,#B8BFC9)',
                               border:'1px solid var(--line-3)', position:'relative', overflow:'hidden' }">
                  <svg width="100%" height="100%" viewBox="0 0 200 130" preserveAspectRatio="xMidYMid slice" style="opacity:0.4">
                    <rect width="200" height="130" fill="#E3E6EB"/>
                    <rect x="30" y="40" width="120" height="50" fill="#CCD2DA" stroke="#979EAE" stroke-width="1"/>
                    <circle cx="55" cy="98" r="8" fill="#747F95"/><circle cx="160" cy="98" r="8" fill="#747F95"/>
                  </svg>
                  <div :style="{ position:'absolute', left:'50%', bottom:'6px', transform:'translateX(-50%)',
                                 background:'#fff', border:'1px solid var(--accent-line)', borderRadius:'2px',
                                 padding:'2px 6px', fontFamily:'var(--font-mono)', fontSize:'10px',
                                 fontWeight:700, color:'var(--accent)', whiteSpace:'nowrap' }">{{ v.plate }}</div>
                </div>
                <div>
                  <div style="display:flex;align-items:center;gap:6px;flex-wrap:wrap;">
                    <span class="mono" style="font-size:12.5px;font-weight:700;">{{ v.plate }}</span>
                    <span :class="levelChipCls(v.level)">{{ levelLabel(v.level) }} {{ v.score }}</span>
                    <span class="chip chip-mute" :style="{ borderColor:scColor(v.scenario), color:scColor(v.scenario) }">시나리오 {{ v.scenario }}</span>
                  </div>
                  <div style="font-size:11px;color:var(--text-3);margin-top:3px;">{{ v.driver }} · {{ v.loc }}</div>
                </div>
                <div style="display:flex;flex-direction:column;gap:4px;align-items:flex-end;">
                  <span class="mono" :style="{ fontSize:'20px', fontWeight:700, color:levelOf(v.score).color, lineHeight:1 }">{{ v.score }}</span>
                  <span style="font-size:9px;color:var(--text-4);">/100</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Donut + Alerts -->
        <div style="display:grid;grid-template-rows:auto 1fr;gap:16px;">
          <!-- Donut -->
          <div class="card" style="padding:20px;">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;">
              <div>
                <div class="label-sm">FLEET HEALTH · NOW</div>
                <div style="font-size:14px;color:var(--text-2);margin-top:2px;">운행 중 차량 등급 분포</div>
              </div>
              <span class="chip chip-info"><span class="dot dot-brand blink"></span> LIVE</span>
            </div>
            <div style="display:grid;grid-template-columns:auto 1fr;gap:24px;align-items:center;margin-top:14px;">
              <!-- DonutGauge SVG -->
              <div :style="{ position:'relative', width:DSIZE+'px', height:DSIZE+'px' }">
                <svg :width="DSIZE" :height="DSIZE" :viewBox="'0 0 '+DSIZE+' '+DSIZE">
                  <circle :cx="DSIZE/2" :cy="DSIZE/2" :r="DR" fill="none" stroke="var(--bg-3)" :stroke-width="DTHICK"/>
                  <g :transform="'rotate(-90 '+DSIZE/2+' '+DSIZE/2+')'">
                    <circle v-for="(a, i) in donutArcs" :key="i"
                      :cx="DSIZE/2" :cy="DSIZE/2" :r="DR" fill="none"
                      :stroke="a.color" :stroke-width="DTHICK"
                      :stroke-dasharray="a.dash" :stroke-dashoffset="a.offset"
                      stroke-linecap="butt"/>
                  </g>
                </svg>
                <div style="position:absolute;inset:0;display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center;">
                  <div class="label-sm">평균 점수</div>
                  <div class="mono" style="font-size:34px;font-weight:800;color:var(--text-1);line-height:1.1;letter-spacing:-0.02em;margin-top:4px;">{{ avgScore }}</div>
                  <div style="font-size:11.5px;color:var(--text-3);margin-top:6px;">of 100 (낮을수록 안전)</div>
                </div>
              </div>
              <div style="display:flex;flex-direction:column;gap:10px;">
                <div v-for="s in donutSegs" :key="s.label" style="display:grid;grid-template-columns:14px 1fr auto;gap:10px;align-items:center;">
                  <span :style="{ width:'14px', height:'14px', borderRadius:'3px', background:s.color, display:'block' }"></span>
                  <div>
                    <div style="font-size:13px;font-weight:600;">{{ s.label }}</div>
                    <div style="font-size:11px;color:var(--text-4);font-family:var(--font-mono);">{{ s.range }}</div>
                  </div>
                  <span class="mono" style="font-size:18px;font-weight:800;letter-spacing:-0.01em;">{{ s.value }}</span>
                </div>
              </div>
            </div>
          </div>
          <!-- Alerts -->
          <div class="card" style="overflow:hidden;">
            <div style="padding:16px 20px;border-bottom:1px solid var(--line-1);display:flex;justify-content:space-between;align-items:center;">
              <div class="label-sm" style="display:flex;align-items:center;gap:6px;">
                <span class="dot dot-danger pulse-ring"></span>
                ACTIVE ALERTS
              </div>
              <span class="chip chip-danger">{{ alertCount }} active</span>
            </div>
            <div style="padding:10px;display:flex;flex-direction:column;gap:6px;max-height:330px;overflow-y:auto;">
              <div v-for="a in alerts" :key="a.t+a.v"
                :style="{ padding:'10px 12px',
                          borderLeft:'3px solid '+alertBorderColor(a.sev),
                          background:alertBg(a.sev),
                          borderRadius:'0 4px 4px 0' }">
                <div style="display:flex;justify-content:space-between;align-items:center;">
                  <span class="mono" :style="{ fontSize:'12px', fontWeight:700, color:alertTextColor(a.sev) }">{{ a.v }}</span>
                  <span class="mono" style="font-size:11px;color:var(--text-4);">{{ a.t }}</span>
                </div>
                <div style="font-size:12.5px;color:var(--text-1);margin-top:5px;">{{ a.d }}</div>
                <div style="font-size:11.5px;color:var(--text-3);margin-top:4px;display:flex;gap:6px;align-items:center;">
                  <AppIcon name="check" :size="10"/> {{ a.action }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ── 차량 드릴다운 ── -->
      <div class="card" style="margin-bottom:16px;overflow:hidden;">
        <div style="padding:18px 20px;border-bottom:1px solid var(--line-1);display:flex;justify-content:space-between;align-items:center;">
          <div style="display:flex;gap:18px;align-items:center;">
            <!-- plate photo md -->
            <div style="width:140px;height:92px;border-radius:4px;flex-shrink:0;background:linear-gradient(135deg,#DCDFE4,#B8BFC9);border:1px solid var(--line-3);position:relative;overflow:hidden;">
              <svg width="100%" height="100%" viewBox="0 0 200 130" preserveAspectRatio="xMidYMid slice" style="opacity:0.4">
                <rect width="200" height="130" fill="#E3E6EB"/>
                <rect x="30" y="40" width="120" height="50" fill="#CCD2DA" stroke="#979EAE" stroke-width="1"/>
                <circle cx="55" cy="98" r="8" fill="#747F95"/><circle cx="160" cy="98" r="8" fill="#747F95"/>
              </svg>
              <div style="position:absolute;left:50%;bottom:10px;transform:translateX(-50%);background:#fff;border:1px solid var(--accent-line);border-radius:2px;padding:3px 8px;font-family:var(--font-mono);font-size:12px;font-weight:700;color:var(--accent);white-space:nowrap;">{{ selected.plate }}</div>
              <div style="position:absolute;top:6px;left:6px;font-family:var(--font-mono);font-size:9.5px;color:#fff;background:rgba(31,38,48,.55);padding:1px 5px;border-radius:2px;">{{ now.split(' ')[1] }}</div>
            </div>
            <div>
              <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap;">
                <span class="mono" style="font-size:18px;font-weight:800;">{{ selected.plate }}</span>
                <span :class="levelChipCls(selected.level)">{{ levelLabel(selected.level) }} · {{ selected.score }}점</span>
                <span class="chip chip-mute"><AppIcon name="user" :size="10"/> {{ selected.driver }}</span>
                <span class="chip chip-mute" :style="{ borderColor:scColor(selected.scenario), color:scColor(selected.scenario) }">시나리오 {{ selected.scenario }}</span>
                <span class="chip chip-info">{{ selected.driveLogId || '운행 ID 없음' }}</span>
                <span :class="statusChipCls(selected.status)">{{ statusLabel(selected.status) }}</span>
              </div>
              <div class="mono" style="font-size:11.5px;color:var(--text-3);margin-top:4px;">
                <AppIcon name="location" :size="10"/> {{ selected.loc }} · 운행 시작 {{ selected.startedAt }} · {{ selected.spd }}km/h · {{ selected.type }}
              </div>
            </div>
          </div>
          <div style="display:flex;gap:8px;">
            <button class="btn btn-ghost" :style="{ opacity: selected.level==='DANGER' ? 1 : 0.5 }" :disabled="selected.level!=='DANGER'"><AppIcon name="phone" :size="13"/> 전화 권고</button>
            <button class="btn btn-ghost"><AppIcon name="coffee" :size="13"/> 휴게 안내</button>
            <button class="btn btn-primary" @click="router.push('/drive-history/'+selected.driveLogId)"><AppIcon name="arrow" :size="13"/> 운행 상세</button>
          </div>
        </div>

        <div style="display:grid;grid-template-columns:1fr 1.7fr;">
          <!-- 피로 팩터 -->
          <div style="padding:20px;border-right:1px solid var(--line-1);">
            <div class="label-sm" style="margin-bottom:14px;">FATIGUE BREAKDOWN</div>
            <div style="display:flex;flex-direction:column;gap:14px;">
              <!-- FactorBar: 연속 운행 -->
              <div v-for="fb in [
                { label:'연속 운행',       val:selected.contMin/60,  unit:'h', max:5,  thr:[1.5,3],  cLbl:'90분 +10', dLbl:'180분 +45' },
                { label:'일일 누적 운행',   val:selected.dailyMin/60, unit:'h', max:12, thr:[6,10],   cLbl:'6h +15',   dLbl:'10h +45'   },
                { label:'야간 운행 (22-06시)', val:selected.nightMin/60, unit:'h', max:4, thr:[0.5,2], cLbl:'30분 +10', dLbl:'2h +35'    },
              ]" :key="fb.label">
                <div style="display:flex;justify-content:space-between;align-items:baseline;">
                  <div style="font-size:12.5px;color:var(--text-2);font-weight:600;">{{ fb.label }}</div>
                  <div style="display:flex;align-items:baseline;gap:4px;">
                    <span class="mono" :style="{ fontSize:'18px', fontWeight:800, color:fbColor(fb.val,fb.thr), letterSpacing:'-0.01em' }">{{ Math.round(fb.val*10)/10 }}</span>
                    <span class="mono" style="font-size:11px;color:var(--text-4);">{{ fb.unit }}</span>
                  </div>
                </div>
                <div style="position:relative;height:10px;background:var(--bg-3);border-radius:5px;margin-top:8px;overflow:hidden;">
                  <div :style="{ position:'absolute',top:0,left:0,height:'100%',width:fbPct(fb.val,fb.max)+'%',background:fbColor(fb.val,fb.thr),transition:'width 0.4s' }"></div>
                  <div v-if="fb.thr[0]!=null" :style="{ position:'absolute',top:'-3px',bottom:'-3px',left:(fb.thr[0]/fb.max*100)+'%',width:'1.4px',background:'var(--warn)' }"></div>
                  <div v-if="fb.thr[1]!=null" :style="{ position:'absolute',top:'-3px',bottom:'-3px',left:(fb.thr[1]/fb.max*100)+'%',width:'1.4px',background:'var(--danger)' }"></div>
                </div>
                <div style="display:flex;justify-content:space-between;margin-top:5px;font-family:var(--font-mono);font-size:10px;">
                  <span :style="{ color: fb.val>fb.thr[0] ? 'var(--warn)' : 'var(--text-4)' }">{{ fb.cLbl }}</span>
                  <span :style="{ color: fb.val>fb.thr[1] ? 'var(--danger)' : 'var(--text-4)' }">{{ fb.dLbl }}</span>
                </div>
              </div>
            </div>

            <div class="label-sm" style="margin:18px 0 10px;">REST EVENTS · 4단계</div>
            <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:6px;">
              <div v-for="rt in [
                { label:'VALID',      count:selected.restValid,   color:'var(--ok)',     hint:'15분+ · -10점' },
                { label:'SUFFICIENT', count:selected.restSuff,    color:'var(--accent)', hint:'30분+ · -20점' },
                { label:'INVALID',    count:selected.restInvalid, color:'var(--text-4)', hint:'15분 미만' },
                { label:'누락',        count:selected.restMiss,    color:'var(--danger)', hint:'2h+ 미휴식' },
              ]" :key="rt.label"
                :style="{ padding:'10px', borderRadius:'4px', background:'var(--bg-2)', border:'1px solid var(--line-2)', borderTop:'2px solid '+rt.color }">
                <div class="mono" :style="{ fontSize:'9.5px', letterSpacing:'0.08em', fontWeight:700, color:rt.color }">{{ rt.label }}</div>
                <div class="mono" style="font-size:22px;font-weight:800;margin-top:4px;letter-spacing:-0.01em;">{{ rt.count }}</div>
                <div style="font-size:10px;color:var(--text-4);margin-top:2px;">{{ rt.hint }}</div>
              </div>
            </div>
          </div>

          <!-- Drive timeline -->
          <div style="padding:20px;">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:8px;">
              <div>
                <div class="label-sm">DRIVE TIMELINE · {{ selected.plate }}</div>
                <div style="font-size:13px;color:var(--text-2);margin-top:2px;">시간대별 피로 점수(누적) · 차속 · 이벤트</div>
              </div>
              <div style="display:flex;align-items:baseline;gap:14px;">
                <div>
                  <div class="label-sm">현재 점수</div>
                  <div style="display:flex;align-items:baseline;gap:4px;margin-top:4px;">
                    <span class="mono" :style="{ fontSize:'22px', fontWeight:700, color:levelOf(selected.score).color, lineHeight:1 }">{{ selected.score }}</span>
                    <span class="mono" style="font-size:10px;color:var(--text-4);">/100</span>
                  </div>
                </div>
                <div style="border-left:1px solid var(--line-2);padding-left:14px;">
                  <div class="label-sm">최고 점수</div>
                  <span class="mono" :style="{ fontSize:'22px', fontWeight:800, color:levelOf(selected.score).color, letterSpacing:'-0.01em' }">{{ peakScore }}</span>
                </div>
              </div>
            </div>
            <!-- Drive Timeline SVG -->
            <svg width="100%" height="240" viewBox="0 0 800 240" preserveAspectRatio="none">
              <rect x="40" y="20"  width="740" height="74" fill="rgba(181,84,74,0.07)"/>
              <rect x="40" y="94"  width="740" height="54" fill="rgba(197,138,58,0.07)"/>
              <rect x="40" y="148" width="740" height="52" fill="rgba(94,138,111,0.06)"/>
              <text x="780" y="34"  text-anchor="end" font-family="var(--font-mono)" font-size="9.5" fill="var(--danger)">위험 ≥ 70</text>
              <text x="780" y="108" text-anchor="end" font-family="var(--font-mono)" font-size="9.5" fill="var(--warn)">주의 40~69</text>
              <text x="780" y="162" text-anchor="end" font-family="var(--font-mono)" font-size="9.5" fill="var(--ok)">정상 ≤ 39</text>
              <line v-for="y in [0,0.25,0.5,0.75,1]" :key="y" x1="40" x2="780" :y1="20+y*180" :y2="20+y*180" stroke="var(--line-1)" stroke-dasharray="2 4"/>
              <polyline :points="tlSpeedPts" fill="none" stroke="var(--slate-300)" stroke-width="1.6" stroke-dasharray="3 3"/>
              <polyline :points="tlScoreArea" fill="rgba(81,95,122,0.18)" stroke="none"/>
              <polyline :points="tlScorePts" fill="none" stroke="var(--accent)" stroke-width="2.4"/>
              <g v-for="(p, i) in tlEvents" :key="i">
                <line :x1="tlPx(p.t)" y1="20" :x2="tlPx(p.t)" y2="200" stroke="var(--accent)" stroke-dasharray="2 4" opacity="0.4"/>
                <circle :cx="tlPx(p.t)" :cy="tlPy(p.score)" r="4" :fill="tlEventColor(p.event)" stroke="var(--bg-1)" stroke-width="2"/>
                <text :x="tlPx(p.t)+6" :y="tlPy(p.score)-8" font-family="var(--font-mono)" font-size="9.5" fill="var(--text-2)">{{ p.event }}</text>
              </g>
              <text v-for="(h, i) in [0,2,4,6,8,10,12]" :key="'h'+i" :x="40+h*61.6" y="232" text-anchor="middle" font-family="var(--font-mono)" font-size="10" fill="var(--text-4)">+{{ h }}h</text>
            </svg>
            <div style="display:flex;gap:14px;margin-top:6px;font-family:var(--font-mono);font-size:10.5px;color:var(--text-3);">
              <span><span style="display:inline-block;width:14px;height:2px;background:var(--accent);vertical-align:middle;"></span> 누적 피로 점수</span>
              <span><span style="display:inline-block;width:14px;height:0;border-top:1.4px dashed var(--slate-300);vertical-align:middle;"></span> 차속</span>
            </div>
          </div>
        </div>

        <!-- Plate recognition events -->
        <div style="padding:18px 20px;border-top:1px solid var(--line-1);">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
            <div>
              <div class="label-sm">PLATE RECOGNITION EVENTS · {{ selected.plate }}</div>
              <div style="font-size:12.5px;color:var(--text-3);margin-top:2px;">출발 · 고속도로 CCTV · 휴게소 진출입 · 도착 번호판 매칭</div>
            </div>
            <div class="mono" style="font-size:11px;color:var(--text-3);display:flex;gap:12px;">
              <span><span class="dot" style="background:var(--ok);"></span> 매칭 {{ plateTimeline.length }}건</span>
              <span>OCR 평균 {{ plateAvgConf }}</span>
            </div>
          </div>
          <div style="display:grid;grid-template-columns:repeat(7,1fr);gap:8px;">
            <div v-for="(p, i) in plateTimeline" :key="i"
              style="border:1px solid var(--line-2);background:var(--bg-2);border-radius:4px;padding:10px;display:flex;flex-direction:column;gap:6px;">
              <div style="display:flex;justify-content:space-between;align-items:center;">
                <span class="mono" :style="{ fontSize:'9.5px', letterSpacing:'0.06em', fontWeight:700, color:sourceColor(p.source) }">{{ p.source }}</span>
                <span class="mono" style="font-size:10px;color:var(--text-4);">{{ p.t }}</span>
              </div>
              <div style="font-size:11px;color:var(--text-2);line-height:1.4;">{{ p.loc }}</div>
              <div style="display:flex;justify-content:space-between;align-items:center;margin-top:auto;">
                <span class="mono" style="font-size:9.5px;color:var(--text-4);">conf {{ p.conf.toFixed(2) }}</span>
                <span :class="p.matched ? 'chip chip-ok' : 'chip chip-warn'" style="font-size:9.5px;padding:1px 6px;">{{ p.matched ? '매칭' : '미매칭' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ── Heatmap + Ranking ── -->
      <div style="display:grid;grid-template-columns:1.7fr 1fr;gap:16px;margin-bottom:16px;">
        <!-- Hour Heatmap -->
        <div class="card" style="padding:20px;">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:14px;">
            <div>
              <div class="label-sm">DRIVER × HOUR HEATMAP</div>
              <div style="font-size:13px;color:var(--text-2);margin-top:2px;">오늘 시간대별 누적 피로 점수 (낮을수록 안전)</div>
            </div>
            <span class="chip chip-mute">2026.05.04</span>
          </div>
          <div style="display:flex;flex-direction:column;gap:8px;">
            <!-- hour axis -->
            <div style="display:grid;grid-template-columns:120px repeat(24,1fr);gap:2px;font-family:var(--font-mono);font-size:9.5px;color:var(--text-4);">
              <div></div>
              <div v-for="h in 24" :key="h" style="text-align:center;">{{ (h-1).toString().padStart(2,'0') }}</div>
            </div>
            <div v-for="d in heatmapDrivers" :key="d.plate"
              style="display:grid;grid-template-columns:120px repeat(24,1fr);gap:2px;align-items:center;">
              <div style="font-size:11.5px;">
                <div style="font-weight:600;color:var(--text-1);">{{ d.name }}</div>
                <div class="mono" style="color:var(--text-4);font-size:10px;margin-top:2px;">{{ d.plate }}</div>
              </div>
              <div v-for="(s, i) in d.hours" :key="i"
                :style="{ height:'22px', borderRadius:'2px', background:heatCellColor(s) }"
                :title="i+':00 score='+(s===null?'no-drive':s)"></div>
            </div>
            <div style="margin-top:8px;display:flex;align-items:center;gap:8px;font-family:var(--font-mono);font-size:10.5px;color:var(--text-3);">
              <span>운행 없음</span>
              <div style="display:flex;gap:2px;">
                <span style="width:18px;height:14px;background:var(--bg-2);border:1px solid var(--line-2);display:inline-block;"></span>
                <span style="width:18px;height:14px;background:#5E8A6F;display:inline-block;"></span>
                <span style="width:18px;height:14px;background:#92A08F;display:inline-block;"></span>
                <span style="width:18px;height:14px;background:#C5A56A;display:inline-block;"></span>
                <span style="width:18px;height:14px;background:#C58A3A;display:inline-block;"></span>
                <span style="width:18px;height:14px;background:#B5544A;display:inline-block;"></span>
              </div>
              <span>점수 0 (정상) → 100 (위험)</span>
            </div>
          </div>
        </div>

        <!-- Ranking -->
        <div class="card" style="overflow:hidden;">
          <div style="padding:16px 20px;border-bottom:1px solid var(--line-1);display:flex;justify-content:space-between;align-items:center;">
            <div>
              <div class="label-sm">DRIVER RANKING · TODAY</div>
              <div style="font-size:13px;color:var(--text-2);margin-top:2px;">현재 누적 피로 점수 (높을수록 위험)</div>
            </div>
            <span class="mono" style="font-size:11px;color:var(--text-3);">상위 6명</span>
          </div>
          <div style="padding:18px 20px;">
            <div style="display:flex;flex-direction:column;gap:10px;">
              <div v-for="it in rankingItems" :key="it.label"
                style="display:grid;grid-template-columns:140px 1fr 70px;gap:14px;align-items:center;">
                <div>
                  <div style="font-size:12.5px;font-weight:600;color:var(--text-1);">{{ it.label }}</div>
                  <div class="mono" style="font-size:10.5px;color:var(--text-4);">{{ it.sub }}</div>
                </div>
                <div style="height:18px;background:var(--bg-2);border-radius:3px;overflow:hidden;position:relative;">
                  <div :style="{ width:(it.value/it.max*100)+'%', height:'100%', background:it.color, transition:'width 0.4s' }"></div>
                  <div style="position:absolute;top:50%;left:8px;transform:translateY(-50%);font-family:var(--font-mono);font-size:10px;color:var(--bg-1);font-weight:600;">{{ it.value }}</div>
                </div>
                <span :style="{ fontFamily:'var(--font-mono)', fontSize:'11px', fontWeight:700, color:it.color, textAlign:'right' }">{{ it.tag }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ── Trend + Event stream ── -->
      <div style="display:grid;grid-template-columns:1.7fr 1fr;gap:16px;">
        <!-- Trend chart -->
        <div class="card" style="padding:20px;">
          <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:18px;">
            <div>
              <div class="label-sm">14-DAY TREND</div>
              <div style="font-size:13px;color:var(--text-2);margin-top:2px;">전체 차량 평균 피로 점수 + 위험 진입 횟수</div>
            </div>
            <div style="display:flex;gap:24px;">
              <div>
                <div class="label-sm">평균 점수</div>
                <div style="display:flex;align-items:baseline;gap:6px;margin-top:4px;">
                  <span class="mono" style="font-size:22px;font-weight:800;">{{ trendAvg }}</span>
                  <span class="mono" :style="{ fontSize:'11px', color: trendDelta>0 ? 'var(--danger)' : 'var(--ok)' }">
                    {{ trendDelta>0 ? '▲' : '▼' }} {{ Math.abs(trendDelta) }}
                  </span>
                </div>
              </div>
              <div>
                <div class="label-sm">위험 진입 누적</div>
                <div style="display:flex;align-items:baseline;gap:6px;margin-top:4px;">
                  <span class="mono" style="font-size:22px;font-weight:800;color:var(--danger);">{{ dangerSum }}</span>
                  <span class="mono" style="font-size:11px;color:var(--text-3);">/ 14일</span>
                </div>
              </div>
              <div>
                <div class="label-sm">총 운행시간</div>
                <div class="mono" style="font-size:22px;font-weight:800;margin-top:4px;">{{ totalDriveH }}h</div>
              </div>
            </div>
          </div>
          <!-- TrendChart SVG -->
          <div :style="{ height:TC_H+'px', position:'relative' }">
            <svg width="100%" :height="TC_H" :viewBox="'0 0 800 '+TC_H" preserveAspectRatio="none" style="display:block;">
              <defs>
                <linearGradient id="tcgrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%"   stop-color="#515F7A" stop-opacity="0.30"/>
                  <stop offset="100%" stop-color="#515F7A" stop-opacity="0.02"/>
                </linearGradient>
              </defs>
              <line v-for="y in [0,0.25,0.5,0.75,1]" :key="y" x1="40" x2="780" :y1="20+y*(TC_H-50)" :y2="20+y*(TC_H-50)" stroke="var(--line-1)" stroke-dasharray="2 4"/>
              <text v-for="(lv, i) in [100,75,50,25,0]" :key="'l'+i" x="32" :y="24+i*((TC_H-50)/4)" font-family="var(--font-mono)" font-size="9.5" fill="var(--text-4)" text-anchor="end">{{ lv }}</text>
              <g v-for="(d, i) in days" :key="'b'+i">
                <rect :x="tcBx(i)-7" :y="(TC_H-30) - d.danger*8" width="14" :height="d.danger*8"
                  fill="rgba(181,84,74,0.20)" stroke="var(--danger)" stroke-width="1"/>
              </g>
              <polyline :points="tcAreaPts" fill="url(#tcgrad)" stroke="none"/>
              <polyline :points="tcLinePts" fill="none" stroke="var(--accent)" stroke-width="2"/>
              <g v-for="(d, i) in days" :key="'p'+i">
                <circle :cx="tcBx(i)" :cy="tcLy(d.avgScore)" r="3" fill="var(--bg-1)" stroke="var(--accent)" stroke-width="2"/>
              </g>
              <g v-for="(d, i) in days" :key="'x'+i">
                <text :x="tcBx(i)" :y="TC_H-8" text-anchor="middle" font-family="var(--font-mono)" font-size="10" fill="var(--text-4)">{{ d.date }}</text>
              </g>
            </svg>
            <div style="position:absolute;top:4px;right:8px;display:flex;gap:14px;font-family:var(--font-mono);font-size:10.5px;">
              <span style="color:var(--accent);display:flex;gap:5px;align-items:center;">
                <span style="width:14px;height:2px;background:var(--accent);display:inline-block;"></span> 평균 피로도
              </span>
              <span style="color:var(--danger);display:flex;gap:5px;align-items:center;">
                <span style="width:8px;height:8px;background:rgba(181,84,74,0.25);border:1px solid var(--danger);display:inline-block;"></span> 위험 진입
              </span>
            </div>
          </div>
        </div>

        <!-- Event stream -->
        <div class="card" style="overflow:hidden;">
          <div style="padding:16px 20px;border-bottom:1px solid var(--line-1);display:flex;justify-content:space-between;align-items:center;">
            <div class="label-sm">EVENT STREAM</div>
            <span class="chip chip-mute">최근 24h</span>
          </div>
          <div style="padding:14px 18px;font-family:var(--font-mono);font-size:12px;display:flex;flex-direction:column;gap:0;max-height:240px;overflow-y:auto;">
            <div v-for="(e, i) in events" :key="i"
              :style="{ display:'grid', gridTemplateColumns:'68px 70px 1fr', gap:'10px', padding:'7px 0',
                        borderBottom: i<events.length-1 ? '1px solid var(--line-1)':'none', alignItems:'baseline' }">
              <span style="color:var(--text-4);">{{ e.t }}</span>
              <span :style="{ color:evColor(e.k), fontSize:'10px', letterSpacing:'0.1em', fontWeight:700 }">[{{ e.k.toUpperCase() }}]</span>
              <span style="color:var(--text-2);">
                <strong style="color:var(--text-1);">{{ e.v }}</strong> · {{ e.e }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- ── Scatter ── -->
      <div class="card" style="padding:22px;margin-top:16px;">
        <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:14px;">
          <div>
            <div class="label-sm">DAILY HOURS × FATIGUE SCATTER</div>
            <div style="font-size:13px;color:var(--text-2);margin-top:2px;">일일 운행시간(가로) × 누적 피로 점수(세로) · 점 크기 = 휴식 누락 횟수</div>
          </div>
          <div style="display:flex;gap:16px;font-family:var(--font-mono);font-size:11px;color:var(--text-3);">
            <span style="display:flex;gap:5px;align-items:center;"><span style="width:12px;height:12px;background:rgba(94,138,111,0.3);border:1px solid var(--ok);border-radius:50%;display:inline-block;"></span>정상</span>
            <span style="display:flex;gap:5px;align-items:center;"><span style="width:12px;height:12px;background:rgba(197,138,58,0.3);border:1px solid var(--warn);border-radius:50%;display:inline-block;"></span>주의</span>
            <span style="display:flex;gap:5px;align-items:center;"><span style="width:12px;height:12px;background:rgba(181,84,74,0.3);border:1px solid var(--danger);border-radius:50%;display:inline-block;"></span>위험</span>
          </div>
        </div>
        <svg width="100%" height="220" viewBox="0 0 600 220" preserveAspectRatio="none">
          <line v-for="y in [0,0.25,0.5,0.75,1]" :key="y" x1="40" x2="580" :y1="10+y*180" :y2="10+y*180" stroke="var(--line-1)" stroke-dasharray="2 4"/>
          <text v-for="(t, i) in [100,75,50,25,0]" :key="'l'+i" x="32" :y="14+i*45" text-anchor="end" font-family="var(--font-mono)" font-size="10" fill="var(--text-4)">{{ t }}</text>
          <line x1="40" y1="10" x2="40" y2="190" stroke="var(--line-2)"/>
          <line x1="40" y1="190" x2="580" y2="190" stroke="var(--line-2)"/>
          <rect x="40" y="64"  width="540" height="54" fill="rgba(197,138,58,0.07)"/>
          <rect x="40" y="118" width="540" height="72" fill="rgba(181,84,74,0.07)"/>
          <g v-for="(it, i) in scatterItems" :key="i">
            <circle :cx="40 + it.x*54" :cy="10 + (1-it.y/100)*180" :r="it.r" :fill="it.color" opacity="0.7" :stroke="it.color" stroke-width="1"/>
            <text :x="40 + it.x*54" :y="14 + (1-it.y/100)*180 - it.r - 4" text-anchor="middle" font-family="var(--font-mono)" font-size="9.5" fill="var(--text-3)">{{ it.label }}</text>
          </g>
          <text v-for="(h, i) in [0,2,4,6,8,10,12]" :key="'h'+i" :x="40+h*45" y="208" text-anchor="middle" font-family="var(--font-mono)" font-size="10" fill="var(--text-4)">{{ h }}h</text>
        </svg>
      </div>

    </div>
  </div>
</template>

<style scoped>
.fade-up { animation: fadeUp 0.4s ease both; }
@keyframes fadeUp { from { opacity:0; transform:translateY(8px); } to { opacity:1; transform:translateY(0); } }

.container { max-width:1320px; margin:0 auto; padding:0 32px 40px; }

/* Page header */
.pg-header { display:flex; justify-content:space-between; align-items:flex-end; margin-bottom:28px; gap:24px; }
.pg-title  { font-size:30px; font-weight:800; margin:0; letter-spacing:-0.022em; color:var(--text-1); }
.pg-sub    { font-size:14px; color:var(--text-3); margin:8px 0 0; }

/* KPI strip */
.kpi-strip { display:grid; grid-template-columns:repeat(6,1fr); }
.stat-tile { padding:18px 20px; position:relative; }

/* Chip & dot */
:deep(.chip) {
  display:inline-flex; align-items:center; gap:6px; padding:3px 9px; border-radius:999px;
  font-size:11px; font-weight:600; letter-spacing:0.02em;
  border:1px solid transparent; font-family:var(--font-mono);
}
:deep(.chip-ok)     { background:var(--ok-soft);     color:var(--ok);     border-color:rgba(94,138,111,0.25); }
:deep(.chip-warn)   { background:var(--warn-soft);   color:var(--warn);   border-color:rgba(197,138,58,0.25); }
:deep(.chip-danger) { background:var(--danger-soft); color:var(--danger); border-color:rgba(181,84,74,0.25); }
:deep(.chip-info)   { background:var(--info-soft);   color:var(--info);   border-color:var(--accent-line); }
:deep(.chip-mute)   { background:var(--bg-2);        color:var(--text-3); border-color:var(--line-2); }

.dot { width:6px; height:6px; border-radius:50%; display:inline-block; }
.dot-ok     { background:var(--ok);     box-shadow:0 0 0 3px var(--ok-soft); }
.dot-danger { background:var(--danger); box-shadow:0 0 0 3px var(--danger-soft); }
.dot-warn   { background:var(--warn);   box-shadow:0 0 0 3px var(--warn-soft); }
.dot-brand  { background:var(--accent); box-shadow:0 0 0 3px var(--accent-soft); }

.blink { animation:blink-soft 2s infinite; }
@keyframes blink-soft { 0%,100% { opacity:1; } 50% { opacity:0.55; } }

.pulse-ring { animation:pulse-ring 2s infinite; }
@keyframes pulse-ring {
  0%   { box-shadow:0 0 0 0 rgba(181,84,74,0.45); }
  70%  { box-shadow:0 0 0 10px rgba(181,84,74,0); }
  100% { box-shadow:0 0 0 0 rgba(181,84,74,0); }
}

.label-sm {
  font-family:var(--font-mono);
  font-size:10.5px; letter-spacing:0.14em;
  text-transform:uppercase; color:var(--text-3); font-weight:600;
}

/* --info 가 없을 경우 대비 */
.card { background:var(--bg-1); border:1px solid var(--line-2); border-radius:var(--r-md); }
</style>
