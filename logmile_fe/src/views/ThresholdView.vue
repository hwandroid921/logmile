<script setup>
import { ref, computed } from 'vue'
import { thresholds } from '@/data/mockData'

const groups = [
  { id:'continuous', title:'연속 운행',       unit:'분', tone:'danger', desc:'무휴식 연속 운행 시간 (4단계 가산점)' },
  { id:'daily',      title:'일일 누적 운행',   unit:'분', tone:'warn',   desc:'당일 누적 운행 시간 (3단계 가산점)' },
  { id:'night',      title:'야간 운행',        unit:'분', tone:'warn',   desc:'22:00 ~ 06:00 누적 (3단계 가산점)' },
  { id:'rest',       title:'휴식 기준 / 보정', unit:'',   tone:'ok',     desc:'유효 / 충분 / 누락 휴식 가중치 (7건)' },
  { id:'level',      title:'등급 컷오프',      unit:'점', tone:'accent', desc:'NORMAL / CAUTION / DANGER 경계 (4건)' },
]

const list    = ref(thresholds.map(t => ({ ...t })))
const editing = ref(null)
const draft   = ref(0)
const savedAt = ref('2026.04.21 14:08')
const savedBy = '시스템 관리자 (super@logmile.io)'

const grouped = computed(() => {
  const map = {}
  groups.forEach(g => { map[g.id] = [] })
  list.value.forEach(t => { if (map[t.group]) map[t.group].push(t) })
  return map
})

const toneMap = { danger:'var(--danger)', warn:'var(--warn)', ok:'var(--ok)', accent:'var(--accent)' }

function startEdit(t) { editing.value = t.key; draft.value = t.value }
function cancelEdit()  { editing.value = null }
function commitEdit(t) { t.value = draft.value; editing.value = null; savedAt.value = '방금' }
function deltaText(t) { return t.delta === 0 ? '—' : (t.delta > 0 ? '+' : '') + t.delta + '점' }
function deltaColor(t) { return t.delta > 0 ? 'var(--danger)' : t.delta < 0 ? 'var(--ok)' : 'var(--text-3)' }

const simSteps = [
  {label:'시작',      val:0,  tag:'NORMAL'},
  {label:'연속 90m',  val:10, tag:'NORMAL'},
  {label:'연속 120m', val:35, tag:'NORMAL'},
  {label:'연속 180m', val:55, tag:'CAUTION'},
  {label:'야간 30m',  val:65, tag:'CAUTION'},
  {label:'연속 240m', val:85, tag:'DANGER'},
  {label:'유효휴식',  val:75, tag:'DANGER'},
  {label:'야간 60m',  val:80, tag:'DANGER'},
]
function tagColor(tag) {
  return tag === 'DANGER' ? 'var(--danger)' : tag === 'CAUTION' ? 'var(--warn)' : 'var(--ok)'
}
</script>

<template>
  <div class="view">
    <div class="breadcrumb mono">
      ADMIN / FATIGUE_THRESHOLD · 시드 {{ list.length }}건 · 최종 저장 {{ savedAt }}
    </div>

    <div class="page-header">
      <div>
        <h2 class="page-title">피로 임계값 정책</h2>
        <p class="page-desc">
          누적 피로 점수 모델 — 점수 <b style="color:var(--ok)">0점 (정상)</b>에서 시작해
          <b style="color:var(--warn)">+10 ~ +65점</b> 가산,
          <b style="color:var(--ok)">유효 휴식 시 -10 ~ -20점</b> 보정.
          총 합산이 <b style="color:var(--danger)">70점 이상</b>이면 DANGER로 전화 권고가 발송됩니다.
        </p>
      </div>
      <div class="hdr-actions">
        <span class="mono" style="font-size:11px;color:var(--text-4);white-space:nowrap">{{ savedBy }}</span>
        <button class="btn btn-ghost">변경 이력</button>
        <button class="btn btn-primary" @click="savedAt = '방금'">정책 저장</button>
      </div>
    </div>

    <!-- 점수 밴드 -->
    <div class="card band-card">
      <div style="display:flex;justify-content:space-between;margin-bottom:10px;">
        <span class="mono" style="font-size:11.5px;color:var(--text-3)">SCORE_BAND · fatigue_event.fatigue_level</span>
        <span class="mono" style="font-size:11px;color:var(--text-4)">정상 → 위험 (점수가 클수록 위험)</span>
      </div>
      <div class="band-bar">
        <div class="band-normal" />
        <div class="band-caution" />
        <div class="band-danger" />
        <div class="band-tick" style="left:40%" />
        <div class="band-tick" style="left:70%" />
        <span class="band-lbl" style="left:8%;color:var(--ok)">NORMAL · 0~39</span>
        <span class="band-lbl" style="left:44%;color:var(--warn)">CAUTION · 40~69</span>
        <span class="band-lbl" style="left:74%;color:var(--danger)">DANGER · ≥70</span>
      </div>
      <div class="band-nums mono">
        <span>0</span><span>20</span><span>40</span><span>60</span><span>80</span><span>100</span>
      </div>
    </div>

    <!-- 임계값 그룹 -->
    <div v-for="g in groups" :key="g.id" class="card group-card">
      <div class="group-hdr">
        <div style="display:flex;align-items:center;gap:12px;">
          <div class="g-accent" :style="{ background: toneMap[g.tone] }" />
          <div>
            <div class="g-title">{{ g.title }}</div>
            <div class="g-desc">{{ g.desc }}</div>
          </div>
        </div>
        <span class="mono" style="font-size:11px;color:var(--text-4)">{{ grouped[g.id].length }} threshold(s)</span>
      </div>
      <table class="tbl">
        <thead>
          <tr>
            <th style="width:32%">KEY</th>
            <th style="width:15%">VALUE</th>
            <th style="width:13%">SCORE_DELTA</th>
            <th>DESCRIPTION</th>
            <th style="width:10%;text-align:right">UPDATED</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in grouped[g.id]" :key="t.key">
            <td>
              <div class="mono" style="font-size:11.5px;font-weight:600;color:var(--text-1)">{{ t.key }}</div>
              <div style="font-size:11px;color:var(--text-4);margin-top:2px">{{ t.label }}</div>
            </td>
            <td>
              <template v-if="editing === t.key">
                <div style="display:flex;align-items:center;gap:6px">
                  <input v-model.number="draft" type="number" class="edit-inp mono" />
                  <button class="btn btn-primary" style="font-size:11px;padding:4px 9px" @click="commitEdit(t)">저장</button>
                  <button class="btn btn-ghost"   style="font-size:11px;padding:4px 9px" @click="cancelEdit">취소</button>
                </div>
              </template>
              <template v-else>
                <span class="mono" style="font-size:14px;font-weight:700;color:var(--text-1)">{{ t.value }}</span>
                <span v-if="g.unit" style="font-size:11px;color:var(--text-4);margin-left:3px">{{ g.unit }}</span>
                <button class="mono edit-btn" @click="startEdit(t)">편집</button>
              </template>
            </td>
            <td>
              <span class="mono" style="font-size:13px;font-weight:700" :style="{ color: deltaColor(t) }">{{ deltaText(t) }}</span>
            </td>
            <td style="font-size:12px;color:var(--text-2);line-height:1.5">{{ t.description }}</td>
            <td class="mono" style="text-align:right;font-size:10.5px;color:var(--text-4)">{{ t.updatedAt }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 시뮬레이션 카드 -->
    <div class="card sim-card">
      <div style="font-size:13px;font-weight:700;color:var(--text-1);margin-bottom:4px">시나리오 C · 점수 누적 시뮬레이션</div>
      <div style="font-size:11.5px;color:var(--text-3);margin-bottom:18px">연속 4시간 + 야간 2시간 + 휴식 누락 2회 → 위험 진입까지의 점수 흐름</div>
      <div class="sim-steps">
        <div v-for="s in simSteps" :key="s.label" class="sim-step">
          <div class="sim-bar-wrap">
            <div class="sim-bar-fill" :style="{ height: s.val+'%', background: tagColor(s.tag) }" />
            <div class="sim-bar-num">{{ s.val }}</div>
          </div>
          <div style="font-size:10.5px;color:var(--text-2);text-align:center">{{ s.label }}</div>
          <div class="mono" style="font-size:9.5px" :style="{ color: tagColor(s.tag) }">{{ s.tag }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view { display:flex; flex-direction:column; gap:16px; padding:32px 32px 40px; }
.breadcrumb { font-size:11px; color:var(--text-4); letter-spacing:0.04em; }

.page-header {
  display:flex; align-items:flex-start; justify-content:space-between;
  border-bottom:1px solid var(--line-2); padding-bottom:18px; gap:24px;
}
.page-title { font-size:24px; font-weight:700; color:var(--text-1); margin:0 0 6px; letter-spacing:-0.01em; }
.page-desc  { font-size:13px; color:var(--text-3); line-height:1.55; margin:0; max-width:680px; }
.hdr-actions { display:flex; align-items:center; gap:10px; flex-shrink:0; }

.band-card { padding:16px 20px; }
.band-bar {
  position:relative; height:28px; border-radius:3px; overflow:hidden; display:flex;
}
.band-normal  { flex:40; background:rgba(94,138,111,.22); }
.band-caution { flex:30; background:rgba(197,138,58,.3);  }
.band-danger  { flex:30; background:rgba(181,84,74,.34);  }
.band-tick  { position:absolute; top:0; bottom:0; width:1px; background:var(--text-2); }
.band-lbl {
  position:absolute; top:50%; transform:translateY(-50%);
  font-family:var(--font-mono); font-size:11px; font-weight:700;
}
.band-nums { display:flex; justify-content:space-between; margin-top:5px; font-size:10px; color:var(--text-4); }

.group-card { padding:0; overflow:hidden; }
.group-hdr {
  display:flex; align-items:center; justify-content:space-between;
  padding:12px 20px; background:var(--bg-2); border-bottom:1px solid var(--line-1);
}
.g-accent { width:4px; height:18px; border-radius:2px; flex-shrink:0; }
.g-title  { font-size:14px; font-weight:700; color:var(--text-1); }
.g-desc   { font-size:11.5px; color:var(--text-3); margin-top:2px; }

.tbl { width:100%; border-collapse:collapse; font-size:12.5px; }
.tbl th {
  padding:10px 20px; text-align:left;
  font-family:var(--font-mono); font-size:10px; font-weight:500; letter-spacing:0.04em;
  color:var(--text-4); background:var(--bg-3); border-bottom:1px solid var(--line-1);
}
.tbl td {
  padding:13px 20px; color:var(--text-2);
  border-top:1px solid var(--line-1); vertical-align:middle;
}
.edit-inp {
  width:80px; padding:4px 8px;
  border:1px solid var(--accent-line); border-radius:var(--r-sm);
  background:var(--bg-2); color:var(--text-1); font-size:13px; outline:none;
}
.edit-btn {
  margin-left:10px; font-size:10px; padding:2px 8px;
  background:transparent; color:var(--text-3);
  border:1px solid var(--line-2); border-radius:var(--r-sm); cursor:pointer;
  transition:color .12s; font-family:var(--font-mono);
}
.edit-btn:hover { color:var(--accent); border-color:var(--accent-line); }

.sim-card  { padding:20px; }
.sim-steps { display:grid; grid-template-columns:repeat(8,1fr); gap:14px; font-family:var(--font-mono); }
.sim-step  { display:flex; flex-direction:column; align-items:center; gap:8px; }
.sim-bar-wrap {
  position:relative; width:100%; height:96px;
  background:var(--bg-2); border:1px solid var(--line-1); border-radius:3px; overflow:hidden;
}
.sim-bar-fill { position:absolute; left:0; right:0; bottom:0; transition:height .3s; }
.sim-bar-num  { position:absolute; top:6px; left:0; right:0; text-align:center; font-weight:700; color:var(--text-1); font-size:11px; }
</style>
