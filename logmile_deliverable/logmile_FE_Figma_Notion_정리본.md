# Logmile Design System — Figma / Notion 정리본

> 버전: v1.1.7 | 테마: Light Slate | 기준 파일: `tokens.css`, `main.css`
> 변경 기준: 스타일 가이드 전용 한글 UI/타이틀 폰트를 LINE Seed Sans KR로 변경

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
| `Text/text-4` | `--text-4` | `#6F7A8A` | 플레이스홀더, 짧은 meta label, 비활성 보조 라벨 |

#### Text Color Usage Rules

| 토큰 | 권장 사용처 | 제한 기준 |
|---|---|---|
| `--text-1` | 페이지 제목, 카드 제목, 핵심 수치, 주요 값 | 긴 설명 문단에 과사용하지 않음 |
| `--text-2` | 카드 본문, 리스트 주요 텍스트, 테이블 주요 셀 | 제목 위계에는 `--text-1` 우선 |
| `--text-3` | 보조 설명, 테이블 일반 셀, 차트 축/범례, KPI 보조 문구 | placeholder나 비활성 라벨에는 `--text-4` 사용 |
| `--text-4` | breadcrumb, placeholder, 짧은 meta label, 비활성 보조 라벨 | 긴 문장, 중요한 설명, 표 셀 값에는 사용 금지 |

**가독성 기준**

- 설명 문구는 기본적으로 `--text-3` 이상을 사용한다.
- `--text-4`는 14px 이상의 짧은 라벨 또는 placeholder 성격에 한정한다.
- 차트 축, 차트 범례, 테이블 셀, 리스트 값은 `--text-3` 이상을 사용한다.
- 숫자 값은 `--text-1` 또는 상태 색상(`--ok`, `--warn`, `--danger`)으로 표시한다.

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

#### Status Color Usage Rules

| 상태 | Text | Soft BG | Border | 권장 표현 |
|---|---|---|---|---|
| NORMAL | `--ok` | `--ok-soft` | `rgba(94,138,111,0.32)` | dot + chip, 정상 수치 |
| CAUTION | `--warn` | `--warn-soft` | `rgba(197,138,58,0.32)` | dot + chip, 주의 수치 |
| DANGER | `--danger` | `--danger-soft` | `rgba(181,84,74,0.32)` | dot + chip, 위험 수치, 오류 |
| INFO | `--info` | `--info-soft` | `--accent-line` | 일반 상태, 보조 정보 |

- 상태 구분은 soft background만으로 표현하지 않고, 텍스트 색상 또는 dot을 함께 사용한다.
- `warn`, `danger` 계열은 작은 chip에서도 읽히도록 `font-weight: 700`을 기본 권장한다.
- 상태값이 숫자와 결합될 때는 숫자 자체에도 상태 색상을 적용한다.

---

### 📝 Text Style 설정

Figma Text Styles에 아래와 같이 등록하세요.
폰트: **LINE Seed Sans KR** (기본/타이틀 계열) / **IBM Plex Mono** (mono 계열)

#### Font Family Usage

| 폰트 | CSS 토큰 | 권장 사용처 | 제한 기준 |
|---|---|---|---|
| LINE Seed Sans KR | `--font-sans`, `--font-point` | 브랜드 타이틀, 페이지 제목, 카드 제목, 버튼, 폼 라벨, 본문 UI | 긴 숫자열, 차량번호, 코드성 라벨에는 mono 우선 |
| IBM Plex Mono | `--font-mono` | KPI 숫자, 차량번호, 상태 chip, 테이블 헤더, 코드성 라벨 | 긴 한글 문장 본문에는 사용하지 않음 |

- Readability Rules는 Typography보다 먼저 확인하는 기준 섹션으로 둔다.
- 스타일 가이드 페이지는 동일한 본문 폭을 기준으로 preview box를 100% 정렬한다.
- 좁은 form, alert 예시는 별도 max-width를 두지 않고 같은 가이드 폭 안에서 정렬한다.

| Style 이름 | 폰트 | 크기 | 굵기 | 자간 | 줄간 | 용도 |
|---|---|---|---|---|---|---|
| `Brand/Title` | LINE Seed Sans KR | 32 | 700 | 0 | 125% | 로그인 브랜드 패널 제목 |
| `Page/Title` | LINE Seed Sans KR | 24 | 700 | 0 | 125% | 앱 주요 페이지 제목 |
| `Section/Title` | LINE Seed Sans KR | 18 | 700 | 0 | 130% | 페이지 섹션 제목 |
| `Form/Title` | LINE Seed Sans KR | 22 | 700 | 0 | auto | 폼 제목 |
| `Card/Title` | LINE Seed Sans KR | 16 | 700 | 0 | 135% | 카드, 패널 제목 |
| `Body/Default` | LINE Seed Sans KR | 14 | 400 | 0 | 155% | 기본 본문 |
| `Body/Small` | LINE Seed Sans KR | 14 | 400 | 0 | 150% | 보조 본문, 리스트 보조 텍스트 |
| `Nav/Item` | LINE Seed Sans KR | 14 | 600 | 0 | auto | 사이드바 메뉴 |
| `Nav/Item-Topbar` | LINE Seed Sans KR | 14 | 600 | 0 | auto | 탑바 nav |
| `Button/Default` | LINE Seed Sans KR | 14 | 600 | 0 | auto | 기본 버튼 |
| `Button/Large` | LINE Seed Sans KR | 14 | 600 | 0 | auto | submit 버튼 |
| `Caption/Default` | LINE Seed Sans KR | 14 | 400 | 0 | auto | 서브 텍스트 |
| `Mono/KPI-Value` | IBM Plex Mono | 28–32 | 800 | 0 | 100% | 대시보드 KPI 숫자 |
| `Mono/Table-Header` | IBM Plex Mono | 16 | 700 | 4% | auto | 테이블 헤더 |
| `Mono/Chip` | IBM Plex Mono | 14 | 700 | 2% | auto | chip 텍스트 |
| `Mono/Label-SM` | IBM Plex Mono | 14 | 700 | 10% | auto | 섹션 레이블 (UPPERCASE) |
| `Mono/Nav-Section` | IBM Plex Mono | 14 | 700 | 10% | auto | 사이드바 섹션 라벨 |
| `Mono/Field-Label` | LINE Seed Sans KR | 14 | 700 | 8% | auto | 폼 필드 라벨 (UPPERCASE) |
| `Mono/Caption` | IBM Plex Mono | 14 | 400 | 4–6% | auto | 코드/수치 캡션 |

#### Typography Usage Rules

- 스타일 가이드와 실제 앱 UI의 상세 항목 최소 폰트 크기는 `14px`로 둔다.
- 항목 제목, 카드 제목, 핵심 텍스트는 `16px` 이상을 기준으로 한다.
- 일반 본문, 보조 본문, 캡션은 모두 `14px` 이상을 기준으로 한다.
- 브랜드/페이지/섹션/카드 제목은 `LINE Seed Sans KR`을 적용하여 정돈된 한글 UI 인상을 유지한다.
- 카드 제목은 `16px / 700`을 기준으로 하며, 카드 내부에서 18px 이상의 제목은 사용하지 않는다.
- KPI 값은 `28~32px / 800 / IBM Plex Mono`를 사용한다.
- 버튼 텍스트는 `14px / 600`을 기본으로 한다.
- 숫자, 상태값, 코드성 라벨, 테이블 헤더, chip은 `IBM Plex Mono`를 우선 사용한다.

#### Data UI Typography

| UI 요소 | 크기/굵기 | 색상 | 비고 |
|---|---|---|---|
| KPI label | 14px / 700 / mono | `--text-2` | 짧은 영문 meta 라벨 |
| KPI value | 28~32px / 800 / mono | `--text-1` 또는 상태 색상 | 핵심 수치 |
| KPI sub | 14px / 400 | `--text-2` 또는 `--text-3` | `--text-4` 사용 지양 |
| Chart axis | 14px / 400 | `--text-2` 또는 `--text-3` | 차트 축 기본 |
| Chart legend | 14px / 600 / mono | `--text-2` 또는 `--text-3` | 상태 범례는 chip + dot 권장 |
| Table header | 16px / 700 / mono | `--text-2` | 항목 제목으로 취급 |
| Table cell | 14px / 400 | `--text-2` 또는 `--text-3` | 값은 `--text-2` 이상 |
| List primary | 16px / 600 | `--text-1` 또는 `--text-2` | 차량번호, 이름 등 핵심 정보 |
| List secondary | 14px / 400 | `--text-2` 또는 `--text-3` | 보조 설명 |
| Empty state | 14px / 400 | `--text-3` | 빈 상태 안내 |

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
| `.btn-danger` | `--danger-soft` | `--danger` | `rgba(181,84,74,0.32)` | `--r-md` | 위험/삭제 액션 |
| `btn-login` | `--accent-soft` | `--accent` | `--accent-line` | `--r-md` | 탑바 로그인 버튼 |

**크기:** `btn-sm` (5/10px padding, 14px) · 기본 (8/14px, 14px) · `btn-lg` (11/22px, 14px, weight 600)

---

### Chips

> 모두 `border-radius: 999px`, `font-family: IBM Plex Mono`, `font-size: 14px`, `font-weight: 700`

| 클래스 | 배경 | 글자색 | 테두리 |
|---|---|---|---|
| `.chip-ok` | `--ok-soft` | `--ok` | `rgba(94,138,111,0.32)` |
| `.chip-warn` | `--warn-soft` | `--warn` | `rgba(197,138,58,0.32)` |
| `.chip-danger` | `--danger-soft` | `--danger` | `rgba(181,84,74,0.32)` |
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

- 기본: `color: --text-2`, `padding: 7px 13px`, `font-size: 14px`, `font-weight: 600`, `border-radius: --r-sm`
- Hover: `bg: --bg-3`, `color: --text-1`
- Active: `bg: --accent-soft`, `border: 1px solid --accent-line`, `color: --accent`

**비로그인 공개 nav**: 소개 / 기능 / 게시판 / 팀 소개

**로그인(Admin) nav**: 대시보드 / 시뮬레이션 / 차량 관리 / 운전자 관리 / 운행 이력 / 임계값 설정 / 피로도 통계

**로그인(Super Admin) nav**: 대시보드 / 가입 승인 / 업체 관리

---

### Form Inputs

- 배경: `--bg-0`, 테두리: `1px solid --line-2`, radius: `--r-md`
- 폰트: `IBM Plex Mono`, `font-size: 14px`, `color: --text-1`
- 플레이스홀더: `color: --text-4`
- Focus: `border-color: --accent-line`
- 오류: `border-color: --danger`, `background: --danger-soft`
- 라벨: LINE Seed Sans KR, 14px, weight 700, uppercase, `letter-spacing: 0.08em`, `color: --text-2`

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
제목: 32px, weight 700, font-sans, color: #FFFFFF
서브: 14px, color: rgba(255,255,255,0.76)
통계 숫자: 28px, weight 800, font-mono, color: #FFFFFF
```

---

### 🛰️ Simulation 3-Column Layout & Sim-Log Card

> 3열 수평 그리드 레이아웃 구조 (`.main-grid { display:grid; grid-template-columns: 420px 1fr 380px; gap: 14px; }`)  
> 피로 점수 추이(차트)와 이벤트 로그 카드가 중앙 2열에 배치되며, 그 우측 3열에 다중 세션 이력 카드(`.sim-log-card`)가 단독 정렬됩니다.

**시뮬레이션 로그 카드 (`.sim-log-card`)**

* **구조 및 배치**:
  - 우측 컬럼(`.right-col`)에 단독 배치되며, 내부 목록(`.sim-log-list`)은 `max-height: 580px; overflow-y: auto;`로 세로 스크롤 영역을 설정하여 중앙 차트/이벤트 영역과 최적의 대칭을 유지합니다.
* **이력 행 패턴 (`.sim-log-row`)**:
  - `border: 1px solid transparent`, `border-radius: var(--r-sm)`를 가지며 호버 시 `background: var(--bg-2)`로 피드백을 줍니다.
  - 액티브 세션(현재 로드된 이력) 상태인 경우 `.active` 클래스가 추가되며 `background: var(--accent-soft)`, `border-color: var(--accent-line)`로 하이라이트됩니다.
  - 진행 상태 뱃지: 운행 중일 때 `●` (`--ok` 색상), 중단/종료일 때 `■` (`--text-3` 색상)으로 렌더링합니다.
* **조작 피드백**:
  - 휴지통 삭제 단추(`.sim-log-delete`): hover 시 `background: rgba(181, 84, 74, 0.15)`, `color: var(--danger)`로 시각적 피드백을 제공하며 클릭 이벤트 버블링 차단을 위해 `.stop` 수식어를 바인딩합니다.

---

---

## 🔑 핵심 설계 원칙

1. **Surface 계층**: bg-0(어두움)이 앱 배경 → bg-1(밝음)이 카드/콘텐츠. 보통의 라이트 테마와 반대 방향
2. **Sans / Mono 역할 분리**: 한글 UI와 타이틀은 LINE Seed Sans KR, 숫자/상태/코드/라벨은 IBM Plex Mono
3. **상태 색상 쌍**: solid(강조 텍스트·도트) + soft(배경) 항상 쌍으로 사용
4. **Accent 파생 토큰**: accent-soft(10% opacity), accent-line(28% opacity)으로 활성 상태 표현
5. **피로도 기준**: NORMAL(0~39점, ok), CAUTION(40~69점, warn), DANGER(70점 이상, danger)
6. **최소 가독성 기준**: 스타일 가이드와 실제 앱 UI의 상세 항목 최소 크기는 14px이며, 항목 제목과 핵심 텍스트는 16px 이상을 사용한다.
7. **데이터 UI 우선 원칙**: 표, 차트, KPI, 리스트는 장식보다 판독성을 우선하며 `--text-3` 이상 대비를 기본으로 한다.

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
