<script setup>
import { useRouter } from 'vue-router'
import AppIcon from '@/components/common/AppIcon.vue'

const router = useRouter()

const features = [
  {
    num: '01',
    name: 'demoBoard',
    title: '관제 대시보드',
    desc: '차량별 실시간 피로도 점수, Fleet Map, 드릴다운 분석, 14일 트렌드, 히트맵을 한 화면에서 확인합니다. 위험(70점+) 차량은 전화 권고가 활성화됩니다.',
    tags: ['KPI 6종', 'Fleet Map', '드라이브 타임라인', '히트맵', 'Scatter'],
    color: '#515F7A',
    preview: [
      { l: 'RUNNING',  v: '8',  c: 'var(--accent)' },
      { l: 'CAUTION',  v: '3',  c: 'var(--warn)' },
      { l: 'DANGER',   v: '2',  c: 'var(--danger)' },
    ],
  },
  {
    num: '02',
    name: 'demoSim',
    title: '운행 시뮬레이터',
    desc: '출발 시간, 운행 시간, 야간 모드, 휴식 이벤트, 번호판 인식 이벤트를 직접 설정하고 1분 단위로 피로 점수가 어떻게 누적되는지 시뮬레이션합니다.',
    tags: ['파라미터 조정', '1분 단위 엔진', '이벤트 로그', 'OCR 인식 이벤트'],
    color: '#747F95',
    preview: [
      { l: 'PEAK',  v: '84', c: 'var(--danger)' },
      { l: 'FINAL', v: '61', c: 'var(--warn)' },
      { l: 'EVENTS',v: '9',  c: 'var(--accent)' },
    ],
  },
  {
    num: '03',
    name: 'demoThresholds',
    title: '임계값 설정',
    desc: '연속·일일·야간 운행 가산점, 휴식 차감점, 등급 컷오프를 관리자가 직접 조정합니다. 변경 사항은 즉시 점수 산정 엔진에 반영됩니다.',
    tags: ['연속 4단계', '일일 3단계', '야간 3단계', '휴식 보정', '등급 컷오프'],
    color: '#979EAE',
    preview: [
      { l: '90분',   v: '+10', c: 'var(--ok)' },
      { l: '240분',  v: '+65', c: 'var(--danger)' },
      { l: '30분 휴식', v: '-20', c: 'var(--ok)' },
    ],
  },
]
</script>

<template>
  <div class="fade-up">

    <!-- ══ HEADER ══ -->
    <section class="section-border" style="padding:72px 0 48px;">
      <div class="page-inner">
        <div class="eyebrow" style="margin-bottom:14px;">FEATURES · DEMO</div>
        <h1 style="font-size:48px;font-weight:800;margin:0;line-height:1.08;letter-spacing:-0.028em;max-width:820px;color:var(--text-1);">
          실제 화면으로<br />
          <span style="color:var(--text-3);">기능을 직접 확인</span>하세요.
        </h1>
        <p style="font-size:15px;color:var(--text-2);line-height:1.75;margin:24px 0 20px;max-width:680px;">
          로그인 없이 실제 관제 화면을 체험할 수 있습니다.
          대시보드·시뮬레이터·임계값 설정 모두 <strong style="color:var(--text-1);">mock 데이터로 완전히 동작</strong>합니다.
        </p>
        <div class="demo-notice">
          <AppIcon name="info" :size="13" />
          <span>아래 화면들은 실제 DB 없이 하드코딩된 mock 데이터로 구성됩니다. 실제 관제 이용은 관리자 로그인이 필요합니다.</span>
        </div>
      </div>
    </section>

    <!-- ══ 기능 목록 ══ -->
    <div v-for="(f, i) in features" :key="f.num"
      :class="i % 2 === 1 ? 'section-alt' : ''"
      class="section-pad section-border">
      <div class="page-inner">
        <div class="feature-row">

          <!-- 텍스트 -->
          <div class="feature-text">
            <div class="eyebrow" style="margin-bottom:10px;">{{ f.num }} — {{ f.title.toUpperCase() }}</div>
            <h2 class="section-title">{{ f.title }}</h2>
            <p class="body-text" style="margin-top:14px;">{{ f.desc }}</p>
            <div style="margin-top:18px;display:flex;gap:6px;flex-wrap:wrap;">
              <span v-for="t in f.tags" :key="t" class="chip chip-mute mono">{{ t }}</span>
            </div>
            <div style="margin-top:28px;">
              <button class="btn-demo" @click="router.push({ name: f.name })">
                {{ f.title }} 데모 열기 →
              </button>
            </div>
          </div>

          <!-- 미리보기 카드 -->
          <div class="feature-preview card" @click="router.push({ name: f.name })">
            <!-- 가짜 브라우저바 -->
            <div class="preview-bar">
              <div style="display:flex;gap:5px;">
                <span class="dot-btn" style="background:#FF5F57;" />
                <span class="dot-btn" style="background:#FEBC2E;" />
                <span class="dot-btn" style="background:#28C840;" />
              </div>
              <div class="mono" style="flex:1;text-align:center;font-size:10px;color:var(--text-4);">
                logmile / demo / {{ f.num === '01' ? 'dashboard' : f.num === '02' ? 'simulation' : 'thresholds' }}
              </div>
            </div>
            <!-- KPI 미리보기 -->
            <div class="preview-kpi">
              <div v-for="k in f.preview" :key="k.l" class="preview-kpi-item">
                <div class="mono" style="font-size:9.5px;color:var(--text-4);letter-spacing:0.08em;text-transform:uppercase;margin-bottom:4px;">{{ k.l }}</div>
                <div class="mono" style="font-size:24px;font-weight:800;letter-spacing:-0.015em;" :style="{ color: k.c }">{{ k.v }}</div>
              </div>
            </div>
            <!-- 더미 바 차트 -->
            <div class="preview-chart">
              <div v-for="h in [35,48,42,61,55,70,65,58,74,68,52,46]" :key="h"
                class="preview-bar-col"
                :style="{
                  height: h + '%',
                  background: h >= 70 ? 'var(--danger)' : h >= 40 ? 'var(--warn)' : 'var(--ok)',
                  opacity: 0.75
                }" />
            </div>
            <!-- 클릭 유도 -->
            <div class="preview-cta">
              <AppIcon name="play" :size="12" />
              <span>클릭하여 전체 화면 데모 열기</span>
            </div>
          </div>

        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.page-inner    { max-width: 1440px; margin: 0 auto; padding: 0 28px; }
.section-pad   { padding: 80px 0; }
.section-alt   { background: var(--bg-0); }
.section-border { border-bottom: 1px solid var(--line-1); }
.mono          { font-family: var(--font-mono); }
.eyebrow       { font-family: var(--font-mono); font-size: 10.5px; letter-spacing: 0.12em; text-transform: uppercase; color: var(--accent); }
.section-title { font-size: 28px; font-weight: 800; margin: 0; letter-spacing: -0.022em; line-height: 1.2; color: var(--text-1); }
.body-text     { font-size: 14.5px; color: var(--text-2); line-height: 1.8; margin: 0; }

/* 데모 안내 */
.demo-notice {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 8px 14px; background: var(--info-soft);
  border: 1px solid rgba(125,179,199,0.3); border-radius: var(--r-md);
  font-size: 12.5px; color: var(--info);
}

/* 기능 row */
.feature-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 56px;
  align-items: center;
}

/* 텍스트 */
.feature-text {}

.btn-demo {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 10px 22px; background: var(--accent);
  color: var(--accent-ink); border: none; border-radius: var(--r-md);
  font-size: 14px; font-weight: 700; cursor: pointer;
  transition: background 0.15s;
}
.btn-demo:hover { background: var(--accent-hover); }

/* 미리보기 카드 */
.card { background: var(--bg-2); border: 1px solid var(--line-2); border-radius: var(--r-lg); }
.feature-preview {
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.18s, box-shadow 0.18s;
  box-shadow: 0 8px 32px rgba(81,95,122,0.12);
}
.feature-preview:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 48px rgba(81,95,122,0.2);
}

/* 가짜 브라우저바 */
.preview-bar {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 12px; background: var(--bg-3);
  border-bottom: 1px solid var(--line-1);
}
.dot-btn { width: 9px; height: 9px; border-radius: 50%; display: inline-block; }

/* KPI 미리보기 */
.preview-kpi {
  display: grid; grid-template-columns: repeat(3,1fr);
  border-bottom: 1px solid var(--line-1);
}
.preview-kpi-item {
  padding: 14px 16px;
  border-right: 1px solid var(--line-1);
}
.preview-kpi-item:last-child { border-right: none; }

/* 더미 바 차트 */
.preview-chart {
  display: flex; align-items: flex-end; gap: 3px;
  height: 80px; padding: 12px 16px 0;
  background: var(--bg-1);
  border-bottom: 1px solid var(--line-1);
}
.preview-bar-col {
  flex: 1; border-radius: 2px 2px 0 0; min-height: 4px;
}

/* 클릭 유도 */
.preview-cta {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 10px; font-size: 12px; color: var(--accent);
  font-weight: 600; background: var(--accent-soft);
  font-family: var(--font-mono); letter-spacing: 0.02em;
}

/* Chips */
.chip { display: inline-flex; align-items: center; padding: 3px 9px; border-radius: 999px; font-size: 11px; font-weight: 500; }
.chip-mute { background: var(--bg-3); color: var(--text-3); border: 1px solid var(--line-2); }

.fade-up { animation: fade-up 0.4s ease both; }
@keyframes fade-up { from{opacity:0;transform:translateY(16px)} to{opacity:1;transform:translateY(0)} }
</style>
