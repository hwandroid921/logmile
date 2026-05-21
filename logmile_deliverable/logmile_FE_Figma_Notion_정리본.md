# Logmile Design System — Figma / Notion 정리본

> 버전: v1.0.0 | 테마: Light Slate | 기준 파일: `tokens.css`, `main.css`

---

## 📐 Figma Variables 설정 가이드

Figma에서 Local Variables 패널에 아래 그룹으로 등록하세요.
각 토큰은 `그룹/변수명` 형식으로 만들면 됩니다.

---

### 🎨 Color Variables

#### Brand / Slate Palette

| Figma 변수명 | CSS 토큰 | Hex 값 |
|---|---|---|
| `Brand/ink` | `--ink` | `#1F2630` |
| `Brand/ink-2` | `--ink-2` | `#3B4453` |
| `Brand/ink-3` | `--ink-3` | `#5A6371` |
| `Brand/slate-900` | `--slate-900` | `#515F7A` |
| `Brand/slate-700` | `--slate-700` | `#747F95` |
| `Brand/slate-500` | `--slate-500` | `#979EAE` |
| `Brand/slate-300` | `--slate-300` | `#B8BFC9` |
| `Brand/slate-100` | `--slate-100` | `#DCDFE4` |
| `Brand/slate-50` | `--slate-50` | `#E6E9EE` |

#### Surface / Background

| Figma 변수명 | CSS 토큰 | Hex 값 | 용도 |
|---|---|---|---|
| `Surface/bg-0` | `--bg-0` | `#DEE2E8` | 앱 최상위 배경, 인풋 배경 |
| `Surface/bg-1` | `--bg-1` | `#F1F3F6` | 카드, 사이드바, 폼 패널 |
| `Surface/bg-2` | `--bg-2` | `#E6E9EE` | 서브 배경, seed-box |
| `Surface/bg-3` | `--bg-3` | `#D4D9E0` | 호버 상태, role-tabs |
| `Surface/bg-4` | `--bg-4` | `#C9CFD8` | 강조 배경 |
| `Surface/bg-5` | `--bg-5` | `#BCC3CD` | 딥 배경 |
| `Border/line-1` | `--line-1` | `#C9CFD8` | 주요 구분선 (사이드바, 탑바) |
| `Border/line-2` | `--line-2` | `#BCC3CD` | 카드·인풋 테두리 |
| `Border/line-3` | `--line-3` | `#B8BFC9` | 버튼 hover 테두리 |

#### Text

| Figma 변수명 | CSS 토큰 | Hex 값 | 용도 |
|---|---|---|---|
| `Text/text-1` | `--text-1` | `#1F2630` | 제목, 기본 텍스트 |
| `Text/text-2` | `--text-2` | `#3B4453` | 보조 텍스트, 카드 내용 |
| `Text/text-3` | `--text-3` | `#5A6371` | 3차 텍스트, nav 기본 |
| `Text/text-4` | `--text-4` | `#8A93A1` | 플레이스홀더, 캡션, 라벨 |

#### Accent

| Figma 변수명 | CSS 토큰 | 값 | 용도 |
|---|---|---|---|
| `Accent/primary` | `--accent` | `#515F7A` | Primary 버튼, active 상태 |
| `Accent/hover` | `--accent-hover` | `#1F2630` | 버튼 hover |
| `Accent/soft` | `--accent-soft` | `rgba(81,95,122,0.10)` | active 배경, chip 배경 |
| `Accent/line` | `--accent-line` | `rgba(81,95,122,0.28)` | active 테두리, focus ring |
| `Accent/on-primary` | `--accent-ink` | `#FFFFFF` | primary 버튼 위 텍스트 |
| `Accent/secondary` | `--accent-2` | `#747F95` | 보조 강조 |

#### Status

| Figma 변수명 | CSS 토큰 | 값 | 용도 |
|---|---|---|---|
| `Status/ok` | `--ok` | `#5E8A6F` | NORMAL · 텍스트, dot, chip |
| `Status/ok-soft` | `--ok-soft` | `rgba(94,138,111,0.12)` | NORMAL · 배경 |
| `Status/warn` | `--warn` | `#C58A3A` | CAUTION · 텍스트, dot, chip |
| `Status/warn-soft` | `--warn-soft` | `rgba(197,138,58,0.12)` | CAUTION · 배경 |
| `Status/danger` | `--danger` | `#B5544A` | DANGER · 텍스트, dot, chip, 오류 |
| `Status/danger-soft` | `--danger-soft` | `rgba(181,84,74,0.12)` | DANGER · 배경, 오류 배경 |
| `Status/info` | `--info` | `#747F95` | INFO · 텍스트 |
| `Status/info-soft` | `--info-soft` | `rgba(116,127,149,0.14)` | INFO · 배경 |

---

### 📝 Text Style 설정

Figma Text Styles에 아래와 같이 등록하세요.
폰트: **Pretendard Variable** (기본) / **JetBrains Mono** (mono 계열)

| Style 이름 | 폰트 | 크기 | 굵기 | 자간 | 줄간 | 용도 |
|---|---|---|---|---|---|---|
| `Brand/Title` | Pretendard Variable | 32 | 800 | -2% | 125% | 로그인 브랜드 패널 제목 |
| `Section/Title` | Pretendard Variable | 22 | 800 | -2% | auto | 페이지 섹션 제목 |
| `Form/Title` | Pretendard Variable | 22 | 700 | -1% | auto | 폼 제목 |
| `Body/Default` | Pretendard Variable | 14 | 400 | 0 | 155% | 기본 본문 |
| `Nav/Item` | Pretendard Variable | 13 | 500 | 0 | auto | 사이드바 메뉴 |
| `Nav/Item-Topbar` | Pretendard Variable | 13.5 | 600 | 0 | auto | 탑바 nav |
| `Button/Default` | Pretendard Variable | 13 | 500 | 0 | auto | 기본 버튼 |
| `Button/Large` | Pretendard Variable | 14 | 600 | 0 | auto | submit 버튼 |
| `Caption/Default` | Pretendard Variable | 12 | 400 | 0 | auto | 서브 텍스트 |
| `Mono/KPI-Value` | JetBrains Mono | 28 | 800 | 0 | 100% | 대시보드 KPI 숫자 |
| `Mono/Chip` | JetBrains Mono | 11 | 600 | 2% | auto | chip 텍스트 |
| `Mono/Label-SM` | JetBrains Mono | 10.5 | 600 | 14% | auto | 섹션 레이블 (UPPERCASE) |
| `Mono/Nav-Section` | JetBrains Mono | 9.5 | 600 | 14% | auto | 사이드바 섹션 라벨 |
| `Mono/Field-Label` | Pretendard Variable | 10.5 | 600 | 8% | auto | 폼 필드 라벨 (UPPERCASE) |
| `Mono/Caption` | JetBrains Mono | 10–12 | 400 | 4–6% | auto | 코드/수치 캡션 |

---

### 📏 Spacing & Border Radius

Figma Number Variables 또는 Effect Styles로 등록:

**Border Radius**

| Figma 변수명 | CSS 토큰 | 값 | 적용 컴포넌트 |
|---|---|---|---|
| `Radius/sm` | `--r-sm` | 4 | topbar 배지, role-tab |
| `Radius/md` | `--r-md` | 8 | 버튼, 카드, 인풋, nav-item |
| `Radius/lg` | `--r-lg` | 14 | KPI 카드, 큰 패널 |
| `Radius/xl` | `--r-xl` | 20 | 모달, 브랜드 패널 |
| `Radius/pill` | `—` | 999 | chip, user-chip, 뱃지 |
| `Radius/circle` | `—` | 50% | dot, avatar |

**Layout Constants**

| Figma 변수명 | CSS 토큰 | 값 | 비고 |
|---|---|---|---|
| `Layout/topbar-height` | `--topbar-height` | 64 | 실제 사용 · 유일한 네비게이션 |
| `Layout/sidebar-width` | `--sidebar-width` | 220 | tokens.css에 정의돼 있으나 미사용 |

---

### 🌑 Effect Styles (Shadows)

| Style 이름 | CSS 토큰 | 값 | 용도 |
|---|---|---|---|
| `Shadow/1-Subtle` | `--shadow-1` | `inset 0 1px 0 rgba(255,255,255,0.6), 0 1px 2px rgba(31,38,48,0.06)` | 기본 카드 |
| `Shadow/2-Card` | `--shadow-2` | `inset 0 1px 0 rgba(255,255,255,0.7), 0 8px 30px rgba(81,95,122,0.10)` | 떠있는 카드 |
| `Shadow/3-Float` | `--shadow-3` | `0 24px 60px rgba(81,95,122,0.18)` | 모달/플로팅 패널 |

---

## 📋 컴포넌트 패턴 정리

### Buttons

| 종류 | 배경 | 글자색 | 테두리 | radius | 용도 |
|---|---|---|---|---|---|
| `.btn-primary` | `--accent` | `#FFFFFF` | none | `--r-md` | 주요 액션 |
| `.btn-ghost` | transparent | `--text-2` | `--line-2` | `--r-md` | 보조 액션 |
| `.btn-link` | none | `--accent` | none | 0 | 인라인 링크형 |
| `.btn-danger` | `--danger-soft` | `--danger` | `rgba(181,84,74,0.25)` | `--r-md` | 위험/삭제 액션 |
| `btn-login` | `--accent-soft` | `--accent` | `--accent-line` | `--r-md` | 탑바 로그인 버튼 |

**크기:** `btn-sm` (5/10px padding, 12px) · 기본 (8/14px, 13px) · `btn-lg` (11/22px, 14px, weight 600)

---

### Chips

> 모두 `border-radius: 999px`, `font-family: JetBrains Mono`, `font-size: 11px`, `font-weight: 600`

| 클래스 | 배경 | 글자색 | 테두리 |
|---|---|---|---|
| `.chip-ok` | `--ok-soft` | `--ok` | `rgba(94,138,111,0.25)` |
| `.chip-warn` | `--warn-soft` | `--warn` | `rgba(197,138,58,0.25)` |
| `.chip-danger` | `--danger-soft` | `--danger` | `rgba(181,84,74,0.25)` |
| `.chip-info` | `--info-soft` | `--info` | `--accent-line` |
| `.chip-mute` | `--bg-2` | `--text-3` | `--line-2` |
| `.chip-brand` | `--accent-soft` | `--accent` | `--accent-line` |

---

### Status Dots

> `6×6px`, `border-radius: 50%`, ring은 `box-shadow` 사용

| 클래스 | 색상 | Ring |
|---|---|---|
| `.dot-ok` | `--ok` `#5E8A6F` | `0 0 0 3px var(--ok-soft)` |
| `.dot-warn` | `--warn` `#C58A3A` | `0 0 0 3px var(--warn-soft)` |
| `.dot-danger` | `--danger` `#B5544A` | `0 0 0 3px var(--danger-soft)` |

`.blink` 클래스 추가 시 `blink-soft 2s infinite` 애니메이션 적용 (SYSTEM ONLINE 등)

---

### Navigation (Topbar 전용)

> AppLayout = AppTopbar + main. 사이드바 없이 **탑바 헤더 바만** 네비게이션으로 사용합니다.

**탑바 nav 아이템**

- 기본: `color: --text-3`, `padding: 7px 13px`, `font-size: 13.5px`, `font-weight: 600`, `border-radius: --r-sm`
- Hover: `bg: --bg-3`, `color: --text-1`
- Active: `bg: --accent-soft`, `border: 1px solid --accent-line`, `color: --accent`

**비로그인 공개 nav**: 소개 / 기능 / 게시판 / 팀 소개

**로그인(Admin) nav**: 대시보드 / 시뮬레이션 / 차량 관리 / 운전자 관리 / 운행 이력 / 임계값 설정 / 피로도 통계

**로그인(Super Admin) nav**: 대시보드 / 가입 승인 / 업체 관리

---

### Form Inputs

- 배경: `--bg-0`, 테두리: `1px solid --line-2`, radius: `--r-md`
- 폰트: `JetBrains Mono`, `font-size: 13px`, `color: --text-1`
- 플레이스홀더: `color: --text-4`
- Focus: `border-color: --accent-line`
- 오류: `border-color: --danger`, `background: --danger-soft`
- 라벨: Pretendard, 10.5px, weight 600, uppercase, `letter-spacing: 0.08em`, `color: --text-3`

---

### Topbar

```
배경: rgba(#F1F3F6, 0.92) = rgba(241, 243, 246, 0.92)
backdrop-filter: blur(10px)
border-bottom: 1px solid --line-1
height: --topbar-height (64px)
position: sticky top:0 z-index:50
```

---

### Brand Dark Panel (Login 좌측)

```
background: linear-gradient(135deg, #1A2230 0%, #2C3851 55%, #515F7A 100%)
데코 원: border: 1px solid rgba(255,255,255,0.06), border-radius: 50%
제목: 32px, weight 800, color: #FFFFFF
서브: 14px, color: rgba(255,255,255,0.55)
통계 숫자: 28px, weight 800, font-mono, color: #FFFFFF
```

---

## 🔑 핵심 설계 원칙

1. **Surface 계층**: bg-0(어두움)이 앱 배경 → bg-1(밝음)이 카드/콘텐츠. 보통의 라이트 테마와 반대 방향
2. **Mono vs Sans 역할 분리**: 숫자/상태/코드/라벨은 JetBrains Mono, 나머지 UI 텍스트는 Pretendard
3. **상태 색상 쌍**: solid(강조 텍스트·도트) + soft(배경) 항상 쌍으로 사용
4. **Accent 파생 토큰**: accent-soft(10% opacity), accent-line(28% opacity)으로 활성 상태 표현
5. **피로도 기준**: NORMAL(0~39점, ok), CAUTION(40~69점, warn), DANGER(70점 이상, danger)

---

## 📁 파일 구조 참고

```
src/
├── assets/
│   ├── tokens.css    ← 모든 CSS 변수 정의
│   ├── main.css      ← 전역 컴포넌트 스타일 (.btn, .chip, .card, ...)
│   └── base.css      ← reset (현재 비어 있음)
├── components/
│   ├── common/AppIcon.vue
│   └── layout/
│       ├── AppLayout.vue
│       ├── AppLogo.vue
│       └── AppTopbar.vue     ← 탑바 패턴, user-chip, live-badge
└── views/
    ├── LoginView.vue          ← brand-panel, form-card, role-tabs, seed-box
    ├── DashboardView.vue      ← kpi-card, alert-items, donut chart
    └── ...
```
