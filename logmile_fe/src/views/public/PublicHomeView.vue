<script setup>
import { useRouter } from 'vue-router'
import AppIcon from '@/components/common/AppIcon.vue'

const router = useRouter()

const scale = [
  { range: '0~39점',   label: '정상', code: '정상 최대 39점', bg: '#5E8A6F', text: '#FFFFFF' },
  { range: '40~69점',  label: '주의', code: '주의 40점부터', bg: '#C58A3A', text: '#FFFFFF' },
  { range: '70~100점', label: '위험', code: '위험 70점부터', bg: '#B5544A', text: '#FFFFFF' },
]

const heroStats = [
  { v: '10',   l: '시뮬레이션 차량',  s: '한라물류센터 단일 운영' },
  { v: '3',    l: '운행 시나리오',    s: 'A(정상) · B(경계) · C(피로)' },
  { v: '21건', l: '피로 임계값 시드', s: 'fatigue_threshold 테이블' },
  { v: '< 5s', l: 'GPS 수집 주기',   s: 'gps_data 30일 보존' },
]

const features = [
  { num: '01', icon: 'clock',    title: '연속/일일/야간 운행',  desc: '90/120/180/240분 4단계 가산. 일일 6h부터 +15, 10h에서 +45. 야간(22~06시)도 별도 가산.', tags: ['+10 / +25 / +45 / +65', '단계별 누적'], color: '#515F7A' },
  { num: '02', icon: 'activity', title: '누적 피로 점수',        desc: '0~100점 — 시간이 지날수록 가산되고 휴식할수록 차감. 70점 이상은 위험 등급으로 자동 분류.', tags: ['0=안전, 100=위험', 'rest로 -10/-20'], color: '#747F95' },
  { num: '03', icon: 'eye',      title: '번호판 인식 매칭',     desc: '출발·고속도로 CCTV·휴게소 진출입·도착 — 4개 source_type으로 운행 로그를 자동 보강합니다.', tags: ['출발 / 도착', '고속도로 / 휴게소'], color: '#979EAE' },
  { num: '04', icon: 'bell',     title: '관리자 알림',          desc: '주의(40+) 시 운전자 알림, 위험(70+) 시 관리자 콘솔에 전화 권고가 활성화됩니다.', tags: ['주의 → 알림', '위험 → 전화'], color: '#B8BFC9' },
]

const steps = [
  { n: '01', icon: 'truck',  t: 'GPS · 차속 수집',  d: '차량 단말이 5초 간격으로 좌표·속도를 전송 (gps_data 테이블, 30일 보존).', bg: '#515F7A' },
  { n: '02', icon: 'eye',    t: '번호판 매칭',       d: '출발·고속 CCTV·휴게소·도착 4종 인식. OCR 신뢰도 0.85 미만은 수동입력 플래그.', bg: '#747F95' },
  { n: '03', icon: 'cpu',    t: '점수 가산 / 차감',  d: '연속·일일·야간 임계값 도달 시 +10~+65, 유효/충분 휴식 시 -10/-20 보정.', bg: '#979EAE' },
  { n: '04', icon: 'bell',   t: '등급 판정 · 알림',  d: '0~39 정상, 40~69 주의 알림, 70+ 위험 — 관리자 콘솔 전화 권고 활성화.', bg: '#DCDFE4' },
]

const heroKpi = [
  { l: '운행중', v: '8', d: '/10',   c: 'var(--accent)' },
  { l: '주의',   v: '3', d: '40~69', c: 'var(--warn)' },
  { l: '위험',   v: '2', d: '70+',   c: 'var(--danger)' },
]

const heroRows = [
  { plate: '경기 80바 1024', driver: '김민수', loc: '경부 안성IC',   score: 78 },
  { plate: '경기 80바 1025', driver: '박정호', loc: '서해안 서산IC', score: 56 },
  { plate: '경기 80바 1026', driver: '이영준', loc: '중부 음성IC',   score: 18 },
]

const wideRows = [
  { plate: '경기 80바 1024', driver: '김민수', loc: '경부고속 · 안성IC',  score: 78, scenario: 'C' },
  { plate: '경기 80바 1029', driver: '강지훈', loc: '중부내륙 · 점촌IC',  score: 84, scenario: 'C' },
  { plate: '경기 80바 1025', driver: '박정호', loc: '서해안 · 서산IC',    score: 56, scenario: 'B' },
  { plate: '경기 80바 1027', driver: '최성훈', loc: '영동고속 · 여주IC',  score: 32, scenario: 'A' },
  { plate: '경기 80바 1026', driver: '이영준', loc: '중부고속 · 음성IC',  score: 18, scenario: 'A' },
]

const trendDays = [
  42, 38, 51, 45, 60, 58, 55, 48, 61, 72, 65, 58, 50, 44,
]

function scoreColor(s) {
  if (s >= 70) return 'var(--danger)'
  if (s >= 40) return 'var(--warn)'
  return 'var(--ok)'
}

function trendLinePath(h) {
  const w = 340; const pad = 12
  const pts = trendDays.map((s, i) => {
    const x = pad + (i / (trendDays.length - 1)) * (w - pad * 2)
    const y = h - pad - (s / 100) * (h - pad * 2)
    return `${x},${y}`
  })
  return 'M ' + pts.join(' L ')
}

function trendAreaPath(h) {
  const w = 340; const pad = 12
  const pts = trendDays.map((s, i) => {
    const x = pad + (i / (trendDays.length - 1)) * (w - pad * 2)
    const y = h - pad - (s / 100) * (h - pad * 2)
    return `${x},${y}`
  })
  const x0 = pad; const xN = pad + (w - pad * 2)
  return `M ${x0},${h - pad} L ${pts.join(' L ')} L ${xN},${h - pad} Z`
}
</script>

<template>
  <div class="fade-up">

    <!-- ══ HERO ══ -->
    <section class="hero-section">
      <div class="bg-grid hero-grid-bg" />
      <div class="page-inner hero-body">
        <!-- 좌측 텍스트 -->
        <div class="hero-left">
          <div class="chip chip-info" style="margin-bottom:22px;display:inline-flex;align-items:center;gap:7px;">
            <span class="dot dot-brand" style="animation:blink-soft 2s infinite" />
            화물차 운수업 피로도 모니터링 · 졸업작품 시뮬레이션
          </div>
          <h1 class="hero-title">
            쌓인 피로를<br />
            <span style="color: var(--accent);">점수 한 줄</span>로<br />
            먼저 잡아냅니다.
          </h1>
          <p class="hero-desc">
            logmile은 화물차의 GPS·차속·번호판 인식 신호를 결합해
            <strong style="color: var(--text-1);">연속 운행·일일 누적·야간 운행·휴식 누락</strong>을
            0~100점으로 누적합니다. 점수가 높을수록 위험.
            70점부터는 운전자에게 전화 권고가 자동 발송됩니다.
          </p>
          <div style="display:flex;gap:12px;margin-bottom:40px;">
            <router-link :to="{ name: 'publicFeatures' }" class="btn btn-primary">
              <AppIcon name="play" :size="13" /> 기능 미리보기
            </router-link>
            <router-link :to="{ name: 'publicIntro' }" class="btn btn-ghost">
              프로젝트 소개 <AppIcon name="arrow" :size="13" />
            </router-link>
          </div>
          <!-- 피로 등급 -->
          <div>
            <div class="label-sm" style="margin-bottom:8px;">피로도 등급 · 0=안전 → 100=위험</div>
            <div class="scale-grid">
              <div v-for="(s,i) in scale" :key="i" :style="{padding:'14px',background:s.bg,color:s.text}">
                <div class="mono" style="font-size: 14px;letter-spacing:0.12em;opacity:0.85;">{{ s.range }}</div>
                <div style="font-weight:700;font-size: 14px;margin-top:6px;">{{ s.label }}</div>
                <div class="mono" style="font-size: 14px;opacity:0.7;margin-top:4px;">{{ s.code }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 우측 프리뷰 카드 -->
        <div style="position:relative;">
          <div class="card" style="padding:0;overflow:hidden;box-shadow:0 24px 60px rgba(81,95,122,0.18);">
            <!-- 가짜 브라우저바 -->
            <div class="preview-bar">
              <div style="display:flex;gap:6px;">
                <span style="width:10px;height:10px;border-radius:5px;background:var(--bg-5);" />
                <span style="width:10px;height:10px;border-radius:5px;background:var(--bg-5);" />
                <span style="width:10px;height:10px;border-radius:5px;background:var(--bg-5);" />
              </div>
              <div class="mono" style="flex:1;text-align:center;font-size: 14px;color: var(--text-3);">logmile.io / dashboard / live</div>
              <AppIcon name="refresh" :size="12" style="color: var(--text-3);" />
            </div>
            <!-- KPI -->
            <div style="display:grid;grid-template-columns:repeat(3,1fr);border-bottom:1px solid var(--line-1);">
              <div v-for="(k,i) in heroKpi" :key="k.l" :style="{padding:'14px 16px',borderRight:i<2?'1px solid var(--line-1)':'none'}">
                <div class="label-sm">{{ k.l }}</div>
                <div style="display:flex;align-items:baseline;gap:8px;margin-top:6px;">
                  <span class="mono" :style="{fontSize:'24px',fontWeight:800,color:k.c}">{{ k.v }}</span>
                  <span class="mono" style="font-size: 14px;color: var(--text-3);">{{ k.d }}</span>
                </div>
              </div>
            </div>
            <!-- 차량 목록 -->
            <div style="padding:14px;">
              <div class="label-sm" style="margin-bottom:10px;display:flex;justify-content:space-between;">
                <span>운행 중 차량</span>
                <span style="color: var(--danger);">● 실시간</span>
              </div>
              <div style="display:flex;flex-direction:column;gap:8px;">
                <div v-for="r in heroRows" :key="r.plate"
                  style="display:grid;grid-template-columns:36px 1fr auto;gap:12px;align-items:center;padding:8px;background:var(--bg-2);border-radius:4px;">
                  <div style="width:36px;height:24px;background:var(--bg-3);border:1px solid var(--line-2);border-radius:3px;display:flex;align-items:center;justify-content:center;">
                    <span class="mono" style="font-size: 14px;color: var(--text-3);">KR</span>
                  </div>
                  <div>
                    <div class="mono" style="font-size: 14px;font-weight:700;">{{ r.plate }}</div>
                    <div style="font-size: 14px;color: var(--text-3);margin-top:2px;">{{ r.driver }} · {{ r.loc }}</div>
                  </div>
                  <span class="mono" :style="{fontWeight:800,fontSize:'15px',color:scoreColor(r.score)}">{{ r.score }}</span>
                </div>
              </div>
            </div>
          </div>
          <!-- 위험 배지 -->
          <div style="position:absolute;top:-14px;right:-14px;background:var(--bg-1);border:1px solid var(--accent-line);padding:8px 12px;border-radius:6px;display:flex;align-items:center;gap:8px;box-shadow:0 8px 20px rgba(81,95,122,0.18);">
            <span class="dot dot-danger" style="animation:pulse-ring 2s infinite;" />
            <span class="mono" style="font-size: 14px;color: var(--text-1);font-weight:700;">2건 위험 (70+)</span>
          </div>
        </div>
      </div>

      <!-- 하단 통계 바 -->
      <div class="page-inner">
        <div class="hero-stats-bar">
          <div v-for="(s,i) in heroStats" :key="s.l"
            :style="{paddingRight:'24px',borderRight:i<3?'1px solid var(--line-1)':'none',paddingLeft:i>0?'24px':'0'}">
            <div class="mono" style="font-size:28px;font-weight:800;color: var(--accent);letter-spacing: 0;">{{ s.v }}</div>
            <div class="label-sm" style="margin-top:6px;">{{ s.l }}</div>
            <div style="font-size: 14px;color: var(--text-3);margin-top:4px;">{{ s.s }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- ══ CORE FEATURES ══ -->
    <section class="section-pad section-border">
      <div class="page-inner">
        <div class="section-hd">
          <div class="eyebrow">핵심 기능</div>
          <h2 class="section-title">네 가지 위험 신호를 동시에 본다.</h2>
          <p class="section-sub">단일 변수에 의존하지 않고 운행 시간·휴식·야간 운행·번호판 매칭을 통합해 누적 피로 점수로 환산합니다.</p>
        </div>
        <div class="feat-grid">
          <div v-for="f in features" :key="f.num" class="card feat-card">
            <div class="feat-topbar" :style="{background:f.color}" />
            <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:16px;">
              <div class="feat-icon"><AppIcon :name="f.icon" :size="20" /></div>
              <span class="mono" style="font-size: 14px;color: var(--text-3);">{{ f.num }}</span>
            </div>
            <h3 style="font-size:15.5px;margin:0 0 8px;font-weight:700;">{{ f.title }}</h3>
            <p style="font-size: 14px;color: var(--text-3);line-height:1.65;margin:0;">{{ f.desc }}</p>
            <div class="feat-tags">
              <span v-for="t in f.tags" :key="t" class="chip chip-mute">{{ t }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ══ LIVE PREVIEW ══ -->
    <section class="section-pad section-border" style="background:var(--bg-0);">
      <div class="page-inner">
        <div class="section-hd" style="display:flex;justify-content:space-between;align-items:flex-start;">
          <div>
            <div class="eyebrow">실시간 관제</div>
            <h2 class="section-title">가상 물류센터 차량 10대가 한 화면에 흐른다.</h2>
            <p class="section-sub">시나리오 A(정상) · B(경계) · C(피로 누적)로 시드된 운행 데이터가 실시간으로 점수화됩니다.</p>
          </div>
          <router-link :to="{ name: 'publicFeatures' }" class="btn btn-ghost" style="flex-shrink:0;margin-top:4px;">
            <AppIcon name="arrow" :size="13" /> 기능 미리보기
          </router-link>
        </div>
        <div class="card" style="overflow:hidden;">
          <div class="wide-grid">
            <!-- 차량 목록 -->
            <div style="padding:20px;border-right:1px solid var(--line-1);">
              <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:14px;">
                <div>
                  <div class="label-sm">운행 차량 · 최근 5대</div>
                  <div style="font-size: 14px;color: var(--text-2);margin-top:2px;">한라물류센터 운행중 차량</div>
                </div>
                <div class="chip chip-info" style="display:inline-flex;align-items:center;gap:6px;">
                  <span class="dot dot-brand" style="animation:blink-soft 2s infinite;" />
                  실시간 · 1.2초 전
                </div>
              </div>
              <div style="display:flex;flex-direction:column;gap:6px;">
                <div v-for="(r,i) in wideRows" :key="r.plate"
                  :style="{display:'grid',gridTemplateColumns:'1fr 90px 80px',gap:'14px',alignItems:'center',padding:'10px',background:i===0?'var(--accent-soft)':'var(--bg-2)',borderRadius:'4px',border:i===0?'1px solid var(--accent-line)':'1px solid transparent'}">
                  <div>
                    <div style="display:flex;align-items:center;gap:6px;">
                      <span class="mono" style="font-size: 14px;font-weight:700;">{{ r.plate }}</span>
                      <span class="chip chip-mute" style="font-size: 14px;padding:1px 5px;">{{ r.scenario }}</span>
                    </div>
                    <div style="font-size: 14px;color: var(--text-3);margin-top:2px;">
                      <AppIcon name="location" :size="10" /> {{ r.loc }} · {{ r.driver }}
                    </div>
                  </div>
                  <span class="mono" style="font-size:18px;font-weight:800;text-align:right;" :style="{color:scoreColor(r.score)}">{{ r.score }}</span>
                  <span class="mono" style="font-size: 14px;color: var(--text-3);text-align:right;">실시간</span>
                </div>
              </div>
            </div>
            <!-- 트렌드 차트 -->
            <div style="padding:20px;">
              <div class="label-sm" style="margin-bottom:6px;">피로도 추이 · 최근 14일</div>
              <div style="display:flex;align-items:baseline;gap:8px;margin-bottom:16px;">
                <span class="mono" style="font-size:30px;font-weight:800;letter-spacing: 0;">53.7</span>
                <span class="mono" style="font-size: 14px;color: var(--danger);">▲ 5.2</span>
                <span style="font-size: 14px;color: var(--text-3);">vs 지난주</span>
              </div>
              <svg width="100%" viewBox="0 0 340 180" style="display:block;">
                <defs>
                  <linearGradient id="phGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stop-color="var(--accent)" stop-opacity="0.25" />
                    <stop offset="100%" stop-color="var(--accent)" stop-opacity="0" />
                  </linearGradient>
                </defs>
                <rect x="12" y="12" width="316" :height="(30/100)*(180-24)" fill="var(--danger)" opacity="0.06" />
                <path :d="trendAreaPath(180)" fill="url(#phGrad)" />
                <path :d="trendLinePath(180)" fill="none" stroke="var(--accent)" stroke-width="1.8" stroke-linejoin="round" />
              </svg>
            </div>
          </div>
          <div style="padding:10px 20px;background:var(--bg-2);display:flex;justify-content:space-between;font-size: 14px;font-family:var(--font-mono);color: var(--text-3);">
            <span><span class="dot dot-ok" /> connected · 8 vehicles streaming</span>
            <span>refreshed 1.2s ago · auto-refresh ON</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ══ HOW IT WORKS ══ -->
    <section class="section-pad section-border">
      <div class="page-inner">
        <div class="section-hd">
          <div class="eyebrow">동작 흐름</div>
          <h2 class="section-title">신호 수집 → 점수 가산 → 관리자 알림</h2>
          <p class="section-sub">화물차 단말의 GPS·차속·번호판 데이터를 5초 간격으로 수집해 누적 피로 점수에 가산합니다.</p>
        </div>
        <div class="steps-grid">
          <div v-for="(s,i) in steps" :key="s.n" class="card" style="padding:24px;position:relative;">
            <div class="mono" style="font-size: 14px;color: var(--accent);letter-spacing:0.14em;">단계 {{ s.n }}</div>
            <div :style="{width:'44px',height:'44px',borderRadius:'6px',background:s.bg,color:i<2?'#FFF':'var(--text-1)',display:'flex',alignItems:'center',justifyContent:'center',margin:'10px 0 14px'}">
              <AppIcon :name="s.icon" :size="20" />
            </div>
            <div style="font-size:15px;font-weight:700;margin-bottom:6px;">{{ s.t }}</div>
            <div style="font-size: 14px;color: var(--text-3);line-height:1.65;">{{ s.d }}</div>
            <div v-if="i<3" style="position:absolute;right:-9px;top:50%;transform:translateY(-50%);background:var(--bg-1);padding:0 4px;color: var(--text-3);">
              <AppIcon name="chevronR" :size="14" />
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ══ CTA ══ -->
    <section class="section-pad">
      <div class="page-inner">
        <div class="card cta-card">
          <div>
            <div class="label-sm" style="color: var(--accent);margin-bottom:10px;">기능 데모</div>
            <h2 style="font-size:30px;margin:0 0 8px;font-weight:700;letter-spacing: 0;">기능 미리보기</h2>
            <p style="color: var(--text-2);margin:0;font-size: 14px;max-width:600px;line-height:1.7;">
              로그인 없이 더미 데이터로 대시보드·시뮬레이터·임계값 설정 화면을 미리 확인해보세요.
              실제 관제 기능은 관리자 로그인 후 이용 가능합니다.
            </p>
          </div>
          <div style="display:flex;gap:12px;flex-shrink:0;">
            <router-link :to="{ name: 'publicFeatures' }" class="btn btn-primary">
              <AppIcon name="play" :size="13" /> 기능 미리보기
            </router-link>
            <router-link :to="{ name: 'publicIntro' }" class="btn btn-ghost">
              <AppIcon name="arrow" :size="13" /> 프로젝트 소개
            </router-link>
          </div>
        </div>
      </div>
    </section>

  </div>
</template>

<style scoped>
.page-inner { max-width: 1280px; margin: 0 auto; padding: 0 28px; }
.section-pad { padding: 80px 0; }
.section-border { border-bottom: 1px solid var(--line-1); }

.label-sm {
  font-family: var(--font-mono);
  font-size: 14px; letter-spacing: 0.12em; text-transform: uppercase; color: var(--text-3);
}
.mono { font-family: var(--font-mono); }

/* ── 섹션 헤더 ── */
.section-hd { margin-bottom: 36px; }
.eyebrow { font-family: var(--font-mono); font-size: 14px; letter-spacing: 0.12em; text-transform: uppercase; color: var(--accent); margin-bottom: 8px; }
.section-title { font-size: 28px; font-weight: 700; margin: 0 0 10px; letter-spacing: 0; color: var(--text-1); }
.section-sub { font-size: 14px; color: var(--text-3); line-height: 1.7; margin: 0; max-width: 640px; }

/* ── Hero ── */
.hero-section { position: relative; border-bottom: 1px solid var(--line-1); overflow: hidden; padding: 72px 0 0; }
.hero-grid-bg {
  position: absolute; inset: 0; opacity: 0.4;
  background-image: linear-gradient(var(--line-1) 1px, transparent 1px), linear-gradient(90deg, var(--line-1) 1px, transparent 1px);
  background-size: 40px 40px;
  mask-image: radial-gradient(ellipse at 30% 50%, black 30%, transparent 75%);
  -webkit-mask-image: radial-gradient(ellipse at 30% 50%, black 30%, transparent 75%);
}
.hero-body { position: relative; padding-bottom: 60px; display: grid; grid-template-columns: 1.05fr 1fr; gap: 56px; align-items: center; }
.hero-title { font-size: 48px; font-weight: 700; line-height: 1.1; letter-spacing: 0; margin: 0; color: var(--text-1); }
.hero-desc { font-size: 15px; color: var(--text-2); line-height: 1.7; margin: 22px 0 32px; max-width: 480px; }
.scale-grid { display: grid; grid-template-columns: repeat(3,1fr); border: 1px solid var(--line-2); border-radius: 6px; overflow: hidden; }
.hero-stats-bar { display: grid; grid-template-columns: repeat(4,1fr); border-top: 1px solid var(--line-1); padding: 24px 0; margin-top: 40px; }

/* ── Preview bar ── */
.preview-bar { padding: 10px 14px; border-bottom: 1px solid var(--line-1); display: flex; align-items: center; gap: 10px; background: var(--bg-2); }

/* ── Features ── */
.feat-grid { display: grid; grid-template-columns: repeat(4,1fr); gap: 14px; }
.feat-card { padding: 24px; position: relative; overflow: hidden; }
.feat-topbar { position: absolute; top: 0; left: 0; right: 0; height: 2px; }
.feat-icon { width: 40px; height: 40px; border-radius: 6px; background: var(--accent-soft); color: var(--accent); display: flex; align-items: center; justify-content: center; border: 1px solid var(--accent-line); }
.feat-tags { margin-top: 18px; padding-top: 14px; border-top: 1px solid var(--line-1); display: flex; gap: 6px; flex-wrap: wrap; }

/* ── Wide preview ── */
.wide-grid { display: grid; grid-template-columns: 1.6fr 1fr; border-bottom: 1px solid var(--line-1); }

/* ── Steps ── */
.steps-grid { display: grid; grid-template-columns: repeat(4,1fr); gap: 14px; }

/* ── CTA ── */
.cta-card { padding: 36px 44px; background: linear-gradient(135deg, var(--bg-2) 0%, var(--bg-3) 100%); border: 1px solid var(--line-3); display: flex; justify-content: space-between; align-items: center; gap: 32px; }

/* ── Buttons ── */
.btn { display: inline-flex; align-items: center; gap: 7px; padding: 9px 18px; border-radius: var(--r-md); font-size: 14px; font-weight: 600; text-decoration: none; cursor: pointer; transition: background 0.15s, border-color 0.15s, color 0.15s; }
.btn-primary { background: var(--accent); color: var(--accent-ink); border: none; }
.btn-primary:hover { background: var(--accent-hover); }
.btn-ghost { background: transparent; border: 1px solid var(--line-2); color: var(--text-2); }
.btn-ghost:hover { background: var(--bg-3); border-color: var(--line-3); color: var(--text-1); }

/* ── Chips / Dots ── */
.chip { display: inline-flex; align-items: center; gap: 5px; padding: 3px 9px; border-radius: 999px; font-size: 14px; font-weight: 700; }
.chip-info  { background: var(--info-soft); color: var(--info); border: 1px solid rgba(125,179,199,0.25); }
.chip-mute  { background: var(--bg-3); color: var(--text-3); border: 1px solid var(--line-2); }
.dot { width: 7px; height: 7px; border-radius: 50%; display: inline-block; flex-shrink: 0; }
.dot-brand  { background: var(--accent); }
.dot-ok     { background: var(--ok); }
.dot-danger { background: var(--danger); }

/* ── Card ── */
.card { background: var(--bg-2); border: 1px solid var(--line-2); border-radius: var(--r-lg); }

/* ── Animations ── */
@keyframes blink-soft { 0%,100%{opacity:1} 50%{opacity:0.3} }
@keyframes pulse-ring {
  0%   { box-shadow: 0 0 0 0 rgba(209,139,126,0.5); }
  70%  { box-shadow: 0 0 0 8px rgba(209,139,126,0); }
  100% { box-shadow: 0 0 0 0 rgba(209,139,126,0); }
}
.fade-up { animation: fade-up 0.4s ease both; }
@keyframes fade-up { from{opacity:0;transform:translateY(16px)} to{opacity:1;transform:translateY(0)} }
</style>
