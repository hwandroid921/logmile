<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppIcon from '@/components/common/AppIcon.vue'
import { driveLogs, plateTimeline, restEvents, levelOf } from '@/data/mockData'

const route  = useRoute()
const router = useRouter()

const logId = route.params.id
const log   = driveLogs.find(l => l.id === logId) ?? driveLogs[0]
const level = levelOf(log.peak)

// 번호판 인식 타임라인 (logId와 무관하게 공통 timeline 사용)
const timeline = plateTimeline

// 해당 운행의 휴식 이벤트
const rests = restEvents.filter(r => r.drive === log.id)

function fmtMin(m) {
  if (!m) return '—'
  const h = Math.floor(m / 60)
  const mn = m % 60
  return h > 0 ? `${h}h ${mn}m` : `${mn}m`
}

function restTypeStyle(t) {
  if (t === 'SUFFICIENT') return { color:'var(--ok)',     bg:'var(--ok-soft)',     border:'rgba(94,138,111,.3)' }
  if (t === 'VALID')      return { color:'#5B8FA8',       bg:'rgba(91,143,168,.12)', border:'rgba(91,143,168,.35)' }
  if (t === 'INVALID')    return { color:'var(--warn)',   bg:'var(--warn-soft)',   border:'rgba(197,138,58,.3)' }
  return                         { color:'var(--danger)', bg:'var(--danger-soft)', border:'rgba(181,84,74,.3)' }
}

function scenarioColor(s) {
  return s === 'C' ? 'var(--danger)' : s === 'B' ? 'var(--warn)' : 'var(--ok)'
}
function statusStyle(s) {
  if (s === 'RUNNING')   return { label:'운행중', color:'var(--accent)',  bg:'var(--accent-soft)' }
  if (s === 'COMPLETED') return { label:'완료',   color:'var(--ok)',     bg:'var(--ok-soft)'     }
  return                        { label:'중단',   color:'var(--danger)', bg:'var(--danger-soft)' }
}

const ss = statusStyle(log.status)
const confPct = Math.round(log.ocrConf * 100)
</script>

<template>
  <div class="view">
    <!-- 헤더 -->
    <div class="breadcrumb mono">
      ADMIN / DRIVE_HISTORY / {{ log.id }} · {{ log.plate }} · {{ log.driver }}
    </div>

    <div class="page-header">
      <div style="display:flex;align-items:center;gap:12px;">
        <button class="btn btn-ghost back-btn" @click="router.back()">
          <AppIcon name="chevron-left" :size="13" />이전
        </button>
        <div>
          <h2 class="page-title">{{ log.id }}</h2>
          <div class="plate-sub">{{ log.plate }} · {{ log.driver }}</div>
        </div>
      </div>
      <div style="display:flex;align-items:center;gap:8px;">
        <span class="status-chip mono" :style="{ color: ss.color, background: ss.bg }">{{ ss.label }}</span>
        <span class="scenario-chip mono" :style="{ color: scenarioColor(log.scenario) }">
          SCENARIO {{ log.scenario }}
        </span>
      </div>
    </div>

    <!-- 메타 그리드 -->
    <div class="card meta-card">
      <div class="meta-grid">
        <div class="meta-item">
          <div class="meta-lbl mono">STARTED_AT</div>
          <div class="meta-val">{{ log.startedAt }}</div>
        </div>
        <div class="meta-item">
          <div class="meta-lbl mono">ENDED_AT</div>
          <div class="meta-val">{{ log.endedAt ?? '운행중' }}</div>
        </div>
        <div class="meta-item">
          <div class="meta-lbl mono">TOTAL_DRIVE</div>
          <div class="meta-val">{{ fmtMin(log.totalMin) }}</div>
        </div>
        <div class="meta-item">
          <div class="meta-lbl mono">NIGHT_DRIVE</div>
          <div class="meta-val">{{ fmtMin(log.nightMin) }}</div>
        </div>
        <div class="meta-item">
          <div class="meta-lbl mono">REST_COUNT</div>
          <div class="meta-val">{{ log.restCount }}회</div>
        </div>
        <div class="meta-item">
          <div class="meta-lbl mono">PEAK_SCORE</div>
          <div class="meta-val" :style="{ color: level.color }">{{ log.peak }}</div>
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
            <span v-if="log.manual" class="manual-badge mono">MANUAL</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 점수 바 -->
    <div class="card score-card">
      <div class="score-hdr">
        <span class="card-title">최고 피로 점수</span>
        <span class="mono score-num" :style="{ color: level.color }">{{ log.peak }}</span>
      </div>
      <div class="score-bar-bg">
        <div class="score-band band-n" /><div class="score-band band-c" /><div class="score-band band-d" />
        <div class="score-needle" :style="{ left: log.peak + '%' }" />
        <div class="score-tick" style="left:40%" /><div class="score-tick" style="left:70%" />
        <span class="band-lbl" style="left:5%;color:var(--ok)">NORMAL</span>
        <span class="band-lbl" style="left:42%;color:var(--warn)">CAUTION</span>
        <span class="band-lbl" style="left:72%;color:var(--danger)">DANGER</span>
      </div>
      <div class="band-nums mono">
        <span>0</span><span>20</span><span>40</span><span>60</span><span>80</span><span>100</span>
      </div>
    </div>

    <div class="two-col">
      <!-- 번호판 인식 타임라인 -->
      <div class="card timeline-card">
        <div class="card-hdr">
          <div class="card-title">번호판 인식 타임라인</div>
          <span class="mono" style="font-size:11px;color:var(--text-4)">{{ timeline.length }}건</span>
        </div>
        <div class="timeline-list">
          <div v-for="(ev, i) in timeline" :key="i" class="tl-item">
            <div class="tl-line-wrap">
              <div class="tl-dot" :class="{ matched: ev.matched }" />
              <div v-if="i < timeline.length - 1" class="tl-line" />
            </div>
            <div class="tl-body">
              <div class="tl-top">
                <span class="mono tl-time">{{ ev.t }}</span>
                <span class="tl-src mono">{{ ev.source }}</span>
                <span class="tl-conf mono" :style="{ color: ev.conf >= 0.95 ? 'var(--ok)' : ev.conf >= 0.9 ? 'var(--warn)' : 'var(--danger)' }">
                  {{ Math.round(ev.conf * 100) }}%
                </span>
              </div>
              <div class="tl-loc">{{ ev.loc }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 휴식 이벤트 -->
      <div class="card rest-card">
        <div class="card-hdr">
          <div class="card-title">휴식 이벤트</div>
          <span class="mono" style="font-size:11px;color:var(--text-4)">{{ rests.length }}건</span>
        </div>
        <div v-if="rests.length === 0" class="empty-txt">해당 운행의 휴식 이벤트가 없습니다.</div>
        <div v-else class="rest-list">
          <div v-for="r in rests" :key="r.id" class="rest-item">
            <div class="rest-top">
              <span class="rest-type-chip mono"
                :style="{ color: restTypeStyle(r.type).color, background: restTypeStyle(r.type).bg, borderColor: restTypeStyle(r.type).border }">
                {{ r.type }}
              </span>
              <span class="mono rest-dur">{{ r.minutes }}분</span>
            </div>
            <div class="rest-time mono">{{ r.startAt }} → {{ r.endAt }}</div>
          </div>
        </div>
        <div v-if="rests.length === 0 && log.restCount > 0" class="rest-note">
          ※ 타 운행 기록과 연동된 휴식 데이터입니다.
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view { display:flex; flex-direction:column; gap:16px; padding:32px 32px 40px; }
.breadcrumb { font-size:11px; color:var(--text-4); letter-spacing:0.04em; }

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

/* 메타 카드 */
.meta-card  { padding:20px; }
.meta-grid  { display:grid; grid-template-columns:repeat(4,1fr); gap:16px; }
.meta-item  { display:flex; flex-direction:column; gap:4px; }
.meta-lbl   { font-size:9.5px; letter-spacing:0.07em; color:var(--text-4); }
.meta-val   { font-size:14px; font-weight:600; color:var(--text-1); }

/* 점수 바 */
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

/* 2열 */
.two-col { display:grid; grid-template-columns:1fr 1fr; gap:10px; }

/* 타임라인 */
.timeline-card { padding:20px; }
.card-hdr { display:flex; align-items:center; justify-content:space-between; margin-bottom:14px; }
.timeline-list { display:flex; flex-direction:column; }
.tl-item { display:flex; gap:12px; }
.tl-line-wrap { display:flex; flex-direction:column; align-items:center; flex-shrink:0; }
.tl-dot {
  width:10px; height:10px; border-radius:50%; flex-shrink:0;
  background:var(--bg-3); border:2px solid var(--line-2);
}
.tl-dot.matched { background:var(--accent); border-color:var(--accent); }
.tl-line { flex:1; width:1px; background:var(--line-2); min-height:12px; }
.tl-body { padding-bottom:16px; flex:1; }
.tl-top  { display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.tl-time { font-size:12px; font-weight:700; color:var(--text-1); }
.tl-src  { font-size:10px; color:var(--text-4); letter-spacing:0.04em; }
.tl-conf { font-size:10.5px; font-weight:600; }
.tl-loc  { font-size:12px; color:var(--text-3); margin-top:3px; }

/* 휴식 */
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
.rest-note { font-size:11px; color:var(--text-4); margin-top:10px; }
.empty-txt { font-size:12.5px; color:var(--text-4); padding:20px 0; text-align:center; }
</style>
