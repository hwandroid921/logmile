<script setup>
const stats = [
  { v: '22%',    l: '화물차 사고 중 피로 관련 비율', s: '교통안전공단 2024' },
  { v: '11h',    l: '장거리 운전자 평균 1일 운행',   s: '국토교통부 2023' },
  { v: '₩4.2조', l: '연간 화물 사고 사회적 비용',     s: '도로교통공단 추산' },
]

const scenarios = [
  { code: 'A', label: '정상 운행',  desc: '규정 휴식 준수, 야간 운행 없음. 누적 점수가 39점 이하로 유지되는 모범 운행 케이스.', range: '0 ~ 39점',    color: '#5E8A6F' },
  { code: 'B', label: '경계 운행',  desc: '간헐적 장시간 운행, 야간 운행 일부 포함. 점수가 40~69점 구간을 오가며 주의 알림이 발생.', range: '40 ~ 69점',  color: '#C58A3A' },
  { code: 'C', label: '피로 누적',  desc: '연속 4시간 이상 운행 반복, 휴식 누락, 야간 운행 다수. 70점 이상 위험 등급 — 전화 권고 활성화.', range: '70 ~ 100점', color: '#B5544A' },
]

const stacks = [
  { role: 'FRONTEND',  t: 'Vue 3 · Vite',     tags: ['Vue 3.4', 'Pinia', 'Vue Router', 'Axios', 'SVG Charts'] },
  { role: 'BACKEND',   t: 'Spring Boot 3',     tags: ['Spring Boot 3.5', 'Spring Security · JWT', 'JPA', 'PostgreSQL 16'] },
  { role: 'DATA · ML', t: 'FastAPI · ANPR',    tags: ['Python 3.11', 'FastAPI', 'YOLO11n', 'EasyOCR', 'Docker'] },
]

const agileSteps = [
  { n: '01', t: '목업 · 단위 기능',  d: '빠르게 목업과 단위 기능 모듈을 제작해 즉시 시연 가능한 상태로 유지합니다.' },
  { n: '02', t: '시연 · 피드백',     d: '선도기업 현장 담당자에게 시연하고 실무 피드백을 수집합니다.' },
  { n: '03', t: '반영 · 개선',       d: '피드백을 다음 스프린트에 즉각 반영해 고객 니즈를 빠르게 충족시킵니다.' },
  { n: '04', t: '반복 · 완성',       d: '짧은 주기로 반복하며 실제 서비스에 가까운 완성도를 만들어 갑니다.' },
]

const team = [
  {
    n: '유환희', role: '팀장', r: 'Backend · AI · 팀장',
    b: '팀 전반 운영 및 방향 설정 담당. Spring Boot REST API 설계·구현, 피로도 점수 산출 엔진, JWT/Spring Security 권한 관리, YOLO11 + EasyOCR 번호판 인식 모델 통합.',
    tags: ['Spring Boot', 'JWT', 'PostgreSQL', 'YOLO11', 'EasyOCR'],
    g1: '#515F7A', g2: '#747F95', isLeader: true,
  },
  {
    n: '백경서', role: '팀원', r: 'Frontend · GPS · 문서',
    b: 'Vue 3 SPA 설계 및 실시간 관제 대시보드 구현. Python GPS 시나리오 시뮬레이터 제작. ERD·DB 설계, 요구사항 정의서·화면 설계서 등 프로젝트 산출물 전반 작성 담당.',
    tags: ['Vue 3', 'Pinia', 'SVG Charts', 'GPS Simulator', '산출물'],
    g1: '#747F95', g2: '#979EAE', isLeader: false,
  },
]
</script>

<template>
  <div class="fade-up">

    <!-- ══ HEADER ══ -->
    <section class="section-border" style="padding:72px 0 48px;">
      <div class="page-inner">
        <div class="eyebrow" style="margin-bottom:14px;">PROJECT OVERVIEW</div>
        <h1 style="font-size:48px;font-weight:800;margin:0;line-height:1.08;letter-spacing:-0.028em;max-width:820px;color:var(--text-1);">
          화물차 운전자 피로도를<br />
          <span style="color:var(--text-3);">데이터로 정량화</span>합니다.
        </h1>
        <p style="font-size:15px;color:var(--text-2);line-height:1.75;margin:24px 0 0;max-width:720px;">
          logmile은 <strong style="color:var(--text-1);">에스트래픽 스마트 모빌리티 DX Academy</strong> 훈련과정의
          첫 번째 본 프로젝트입니다.
          웹 기반 모빌리티 서비스 설계·개발, ChatGPT 활용 차량번호 인식 시스템,
          Python/FastAPI(YOLO+OCR) + Spring Boot + Vue.js를 결합한 실시간 차량 피로도 모니터링 플랫폼입니다.
        </p>
        <div style="margin-top:28px;display:flex;gap:8px;flex-wrap:wrap;">
          <span class="chip chip-brand">에스트래픽 · 스마트 모빌리티 DX Academy</span>
          <span class="chip chip-mute">국비 선도기업 훈련과정</span>
          <span class="chip chip-mute">팀원 2명</span>
          <span class="chip chip-mute">애자일 방법론</span>
        </div>
      </div>
    </section>

    <!-- ══ 01 BACKGROUND ══ -->
    <section class="section-pad section-border">
      <div class="page-inner">
        <div class="two-col">
          <div>
            <div class="eyebrow">01 — BACKGROUND</div>
            <h2 class="section-title" style="margin-top:12px;">왜 만들었나</h2>
          </div>
          <div>
            <p class="body-text">
              에스트래픽 스마트 모빌리티 DX Academy 훈련과정의 선도기업 현장 과제로 시작된 프로젝트입니다.
              국내 화물차 사고의 22%는 운전자 피로와 직결된 것으로 보고되지만,
              현장에서는 여전히 운행일지·자가신고에 의존하고 위험 신호는 사고 이후에야 데이터로 정리됩니다.
            </p>
            <p class="body-text" style="margin-top:16px;">
              logmile은 차량 단말이 만들어내는 운행 로그를 결합해
              <strong style="color:var(--text-1);">사람의 직감 대신 누적 피로 점수로 판정</strong>합니다.
              관리자는 0~100점 한 줄만 보면 됩니다 — 70점부터는 즉시 전화 권고.
            </p>
            <div class="stats-row">
              <div v-for="(s,i) in stats" :key="s.l"
                :style="{padding:'22px',borderRight:i<2?'1px solid var(--line-2)':'none',background:'var(--bg-1)'}">
                <div class="mono" style="font-size:32px;font-weight:800;color:var(--accent);line-height:1;letter-spacing:-0.02em;">{{ s.v }}</div>
                <div style="font-size:13px;color:var(--text-2);margin-top:12px;line-height:1.5;">{{ s.l }}</div>
                <div class="mono" style="font-size:10.5px;color:var(--text-4);margin-top:8px;">{{ s.s }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ══ 02 SCENARIOS ══ -->
    <section class="section-pad section-border" style="background:var(--bg-0);">
      <div class="page-inner">
        <div class="two-col">
          <div style="position:sticky;top:80px;align-self:start;">
            <div class="eyebrow">02 — SCENARIOS</div>
            <h2 class="section-title" style="margin-top:12px;">3가지 운행 시나리오</h2>
            <p class="body-text" style="font-size:13px;margin-top:8px;">10대 차량을 A·B·C로 분류해 점수 모델의 동작을 검증합니다.</p>
          </div>
          <div style="display:flex;flex-direction:column;gap:10px;">
            <div v-for="s in scenarios" :key="s.code" class="card"
              style="padding:22px;display:grid;grid-template-columns:60px 1fr auto;gap:18px;align-items:center;">
              <div :style="{width:'56px',height:'56px',borderRadius:'8px',background:s.color,color:'#FFF',display:'flex',alignItems:'center',justifyContent:'center',fontFamily:'var(--font-mono)',fontSize:'26px',fontWeight:800}">
                {{ s.code }}
              </div>
              <div>
                <div style="font-size:15px;font-weight:700;">시나리오 {{ s.code }} · {{ s.label }}</div>
                <div style="font-size:12.5px;color:var(--text-3);margin-top:4px;line-height:1.6;">{{ s.desc }}</div>
              </div>
              <div class="mono" :style="{fontSize:'16px',fontWeight:800,color:s.color,minWidth:'110px',textAlign:'right',letterSpacing:'-0.015em'}">{{ s.range }}</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ══ 03 AGILE ══ -->
    <section class="section-pad section-border">
      <div class="page-inner">
        <div class="two-col">
          <div style="position:sticky;top:80px;align-self:start;">
            <div class="eyebrow">03 — AGILE</div>
            <h2 class="section-title" style="margin-top:12px;">애자일 방법론</h2>
            <p class="body-text" style="font-size:13px;margin-top:8px;">
              짧은 스프린트 주기로 빠르게 만들고, 현장 피드백을 즉시 반영합니다.
            </p>
          </div>
          <div style="display:flex;flex-direction:column;gap:10px;">
            <div v-for="step in agileSteps" :key="step.n" class="card agile-step">
              <div class="agile-num mono">{{ step.n }}</div>
              <div>
                <div style="font-size:15px;font-weight:700;">{{ step.t }}</div>
                <div style="font-size:12.5px;color:var(--text-3);margin-top:6px;line-height:1.65;">{{ step.d }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ══ 04 STACK ══ -->
    <section class="section-pad section-border">
      <div class="page-inner">
        <div class="eyebrow">04 — STACK</div>
        <h2 class="section-title" style="margin:12px 0 28px;">기술 구성</h2>
        <div class="stack-grid">
          <div v-for="s in stacks" :key="s.t" class="card" style="padding:22px;">
            <div class="eyebrow" style="margin-bottom:6px;">{{ s.role }}</div>
            <div style="font-size:17px;font-weight:700;margin-bottom:12px;">{{ s.t }}</div>
            <div style="display:flex;gap:6px;flex-wrap:wrap;">
              <span v-for="t in s.tags" :key="t" class="chip chip-mute mono">{{ t }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ══ 05 CONTACT CTA ══ -->
    <section class="section-pad">
      <div class="page-inner">
        <div class="contact-cta">
          <div>
            <div class="eyebrow" style="margin-bottom:10px;">05 — TEAM &amp; CONTACT</div>
            <h2 class="section-title">팀 소개 및 문의</h2>
            <p class="body-text" style="margin-top:10px;font-size:13.5px;">
              프로젝트를 만든 팀원 소개와 문의처를 확인하세요.
            </p>
          </div>
          <div style="display:flex;gap:12px;flex-shrink:0;">
            <router-link :to="{ name: 'publicContact' }" class="btn-cta-primary">
              팀 소개 보기 →
            </router-link>
            <router-link :to="{ name: 'publicBoard' }" class="btn-cta-ghost">
              문의 게시판
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
.label-sm { font-family: var(--font-mono); font-size: 10.5px; letter-spacing: 0.12em; text-transform: uppercase; color: var(--text-4); }
.mono { font-family: var(--font-mono); }
.eyebrow { font-family: var(--font-mono); font-size: 10.5px; letter-spacing: 0.12em; text-transform: uppercase; color: var(--accent); }
.section-title { font-size: 28px; font-weight: 800; margin: 0; letter-spacing: -0.022em; line-height: 1.2; color: var(--text-1); }
.body-text { font-size: 15px; color: var(--text-2); line-height: 1.8; margin: 0; }
.two-col { display: grid; grid-template-columns: 280px 1fr; gap: 56px; }
.stats-row { display: grid; grid-template-columns: repeat(3,1fr); margin-top: 32px; border: 1px solid var(--line-2); border-radius: 6px; overflow: hidden; }
.stack-grid { display: grid; grid-template-columns: repeat(3,1fr); gap: 14px; }
.team-grid { display: grid; grid-template-columns: repeat(2,1fr); gap: 16px; }
.chip { display: inline-flex; align-items: center; padding: 3px 9px; border-radius: 999px; font-size: 11.5px; font-weight: 500; }
.chip-brand { background: var(--accent-soft); color: var(--accent); border: 1px solid var(--accent-line); }
.chip-mute { background: var(--bg-3); color: var(--text-3); border: 1px solid var(--line-2); }
.card { background: var(--bg-2); border: 1px solid var(--line-2); border-radius: var(--r-lg); }
.agile-step { padding: 20px 22px; display: grid; grid-template-columns: 40px 1fr; gap: 16px; align-items: flex-start; }
.agile-num { font-size: 22px; font-weight: 800; color: var(--accent); line-height: 1.4; letter-spacing: -0.02em; }
.leader-badge {
  position: absolute; bottom: 2px; right: 2px;
  font-size: 9px; font-weight: 700; letter-spacing: 0.04em;
  background: var(--accent); color: var(--accent-ink);
  padding: 1px 6px; border-radius: 999px;
}
.contact-cta { display: flex; justify-content: space-between; align-items: center; gap: 32px; padding: 36px 44px; background: linear-gradient(135deg, var(--bg-2) 0%, var(--bg-3) 100%); border: 1px solid var(--line-3); border-radius: var(--r-lg); }
.btn-cta-primary { display: inline-flex; align-items: center; padding: 9px 20px; background: var(--accent); color: var(--accent-ink); border: none; border-radius: var(--r-md); font-size: 13.5px; font-weight: 600; text-decoration: none; transition: background 0.15s; }
.btn-cta-primary:hover { background: var(--accent-hover); }
.btn-cta-ghost { display: inline-flex; align-items: center; padding: 9px 20px; background: transparent; color: var(--text-2); border: 1px solid var(--line-2); border-radius: var(--r-md); font-size: 13.5px; font-weight: 600; text-decoration: none; transition: background 0.15s, border-color 0.15s; }
.btn-cta-ghost:hover { background: var(--bg-3); border-color: var(--line-3); color: var(--text-1); }
.fade-up { animation: fade-up 0.4s ease both; }
@keyframes fade-up { from{opacity:0;transform:translateY(16px)} to{opacity:1;transform:translateY(0)} }
</style>
