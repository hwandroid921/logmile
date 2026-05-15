<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const team = [
  {
    n: '유환희', role: '팀장', r: 'Backend · AI · 팀장',
    b: '팀 전반 운영 및 방향 설정 담당. Spring Boot REST API 설계·구현, 피로도 점수 산출 엔진, JWT/Spring Security 권한 관리, YOLO11n + EasyOCR 번호판 인식 모델 통합.',
    tags: ['Spring Boot', 'JWT', 'PostgreSQL', 'YOLO11n', 'EasyOCR'],
    g1: '#515F7A', g2: '#747F95', isLeader: true,
  },
  {
    n: '백경서', role: '팀원', r: 'Frontend · GPS · 문서',
    b: 'Vue 3 SPA 설계 및 실시간 관제 대시보드 구현. Python GPS 시나리오 시뮬레이터 제작. ERD·DB 설계, 요구사항 정의서·화면 설계서 등 프로젝트 산출물 전반 작성 담당.',
    tags: ['Vue 3', 'Pinia', 'SVG Charts', 'GPS Simulator', '산출물'],
    g1: '#747F95', g2: '#979EAE', isLeader: false,
  },
]

const contactItems = [
  { icon: 'info',   label: '교육기관', value: 'MBC 컴퓨터 아카데미 부평점' },
  { icon: 'list',   label: '과정',     value: '에스트래픽 스마트 모빌리티 DX Academy 3기 3조' },
  { icon: 'bell',   label: '문의',     value: '게시판을 통해 문의해주세요' },
]
</script>

<template>
  <div class="fade-up">

    <!-- ══ HEADER ══ -->
    <section class="section-border" style="padding:72px 0 48px;">
      <div class="page-inner">
        <div class="eyebrow" style="margin-bottom:14px;">CONTACT US</div>
        <h1 style="font-size:48px;font-weight:800;margin:0;line-height:1.08;letter-spacing:-0.028em;color:var(--text-1);">
          만드는 사람들
        </h1>
        <p style="font-size:15px;color:var(--text-2);line-height:1.75;margin:24px 0 0;max-width:640px;">
          MBC 컴퓨터 아카데미 부평점 · 에스트래픽 스마트 모빌리티 DX Academy 3기 3조에서
          logmile을 함께 만들고 있는 팀을 소개합니다.
        </p>
      </div>
    </section>

    <!-- ══ TEAM ══ -->
    <section class="section-pad section-border">
      <div class="page-inner">
        <div class="eyebrow" style="margin-bottom:8px;">01 — TEAM</div>
        <h2 class="section-title" style="margin:0 0 28px;">팀원 소개</h2>
        <div class="team-grid">
          <div v-for="p in team" :key="p.n" class="card member-card">
            <div class="member-top">
              <div style="position:relative;flex-shrink:0;">
                <div :style="{ width:'80px', height:'80px', borderRadius:'50%', background:`linear-gradient(135deg,${p.g1},${p.g2})`, display:'flex', alignItems:'center', justifyContent:'center' }">
                  <span class="mono" style="font-size:20px;font-weight:800;color:#FFF;">{{ p.n[0] }}</span>
                </div>
                <span v-if="p.isLeader" class="leader-badge">팀장</span>
              </div>
              <div>
                <div style="display:flex;align-items:center;gap:8px;">
                  <span style="font-size:20px;font-weight:800;color:var(--text-1);">{{ p.n }}</span>
                  <span v-if="p.isLeader" class="chip chip-brand" style="font-size:10.5px;padding:2px 9px;">LEAD</span>
                </div>
                <div class="eyebrow" style="margin-top:6px;">{{ p.r }}</div>
              </div>
            </div>
            <p style="font-size:13px;color:var(--text-3);line-height:1.7;margin:0;">{{ p.b }}</p>
            <div style="margin-top:14px;display:flex;gap:5px;flex-wrap:wrap;">
              <span v-for="t in p.tags" :key="t" class="chip chip-mute mono">{{ t }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ══ CONTACT ══ -->
    <section class="section-pad section-border">
      <div class="page-inner">
        <div class="two-col">
          <div>
            <div class="eyebrow" style="margin-bottom:8px;">02 — CONTACT</div>
            <h2 class="section-title" style="margin:0 0 16px;">프로젝트 문의</h2>
            <p class="body-text">
              logmile에 관한 기술 문의, 피드백, 협업 제안은
              게시판을 통해 남겨주세요. 담당자가 확인 후 답변드립니다.
            </p>
          </div>
          <div style="display:flex;flex-direction:column;gap:10px;">
            <div v-for="c in contactItems" :key="c.label" class="card contact-item">
              <div class="eyebrow" style="margin-bottom:4px;">{{ c.label }}</div>
              <div style="font-size:14px;font-weight:600;color:var(--text-1);">{{ c.value }}</div>
            </div>
            <button class="btn-board" @click="router.push({ name: 'publicBoard' })">
              게시판 바로가기 →
            </button>
          </div>
        </div>
      </div>
    </section>

  </div>
</template>

<style scoped>
.page-inner   { max-width: 1440px; margin: 0 auto; padding: 0 28px; }
.section-pad  { padding: 80px 0; }
.section-border { border-bottom: 1px solid var(--line-1); }
.mono         { font-family: var(--font-mono); }
.eyebrow      { font-family: var(--font-mono); font-size: 10.5px; letter-spacing: 0.12em; text-transform: uppercase; color: var(--accent); }
.section-title { font-size: 28px; font-weight: 800; margin: 0; letter-spacing: -0.022em; line-height: 1.2; color: var(--text-1); }
.body-text    { font-size: 15px; color: var(--text-2); line-height: 1.8; margin: 0; }
.two-col      { display: grid; grid-template-columns: 280px 1fr; gap: 56px; }

/* 팀 그리드 */
.team-grid { display: grid; grid-template-columns: repeat(2,1fr); gap: 20px; }
.card { background: var(--bg-2); border: 1px solid var(--line-2); border-radius: var(--r-lg); }
.member-card { padding: 28px; display: flex; flex-direction: column; gap: 16px; }
.member-top { display: flex; align-items: flex-start; gap: 18px; }

/* 팀장 배지 */
.leader-badge {
  position: absolute; bottom: 2px; right: 2px;
  font-size: 9px; font-weight: 700; letter-spacing: 0.04em;
  background: var(--accent); color: var(--accent-ink);
  padding: 1px 6px; border-radius: 999px;
}

/* Chips */
.chip { display: inline-flex; align-items: center; padding: 3px 9px; border-radius: 999px; font-size: 11.5px; font-weight: 500; }
.chip-brand { background: var(--accent-soft); color: var(--accent); border: 1px solid var(--accent-line); }
.chip-mute  { background: var(--bg-3); color: var(--text-3); border: 1px solid var(--line-2); }

/* Contact */
.contact-item { padding: 16px 20px; }
.btn-board {
  display: inline-flex; align-items: center; padding: 10px 20px;
  background: var(--accent); color: var(--accent-ink);
  border: none; border-radius: var(--r-md);
  font-size: 13.5px; font-weight: 600; cursor: pointer;
  transition: background 0.15s; text-align: left;
}
.btn-board:hover { background: var(--accent-hover); }

.fade-up { animation: fade-up 0.4s ease both; }
@keyframes fade-up { from{opacity:0;transform:translateY(16px)} to{opacity:1;transform:translateY(0)} }
</style>
