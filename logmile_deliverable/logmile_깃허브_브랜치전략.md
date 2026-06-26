# logmile 깃허브 브랜치 전략

> 프로젝트명: logmile | 버전: v1.6 | 작성일: 2026.05.26  
> 개발 기간: 2026.04.28 ~ 2026.05.27 제출 / 2026.06.02 발표  
> *v1.6 수정사항: AI 에이전트 원격 업로드(Commit/Push) 및 PR Description 코드블록 제공 지침 추가*

---

## 1. 브랜치 구조 개요

```
main
 └── dev
      ├── feature/ai-*        (AI 서버 - 환희)
      ├── feature/be-*        (Backend - 환희)
      ├── feature/fe-*        (Frontend - 경서)
      ├── feature/sim-*       (GPS 시뮬레이터 - 경서)
      └── feature/infra-*     (Infra 공통)
```

- **main**: 최종 릴리즈 전용 (직접 커밋 금지)
- **dev**: 통합 브랜치 (feature 머지 대상)
- **feature/\***: 기능 단위 개발 브랜치 → 완료 시 dev로 PR 머지
- **release/\***: 제출 전 QA 브랜치 → QA 완료 후 main으로 머지 후 Tag

---

## 2. 기능 브랜치 목록

### 브랜치 네이밍 컨벤션
```
feature/{layer}-{기능명}
```

---

### 🤖 AI 레이어 (담당: 환희)

| 브랜치명 | 기능 | 요구사항 ID |
|---|---|---|
| `feature/ai-fastapi-server` | FastAPI 서버 설정, 라우터, 환경 구성 | - |
| `feature/ai-model-dataset` | 번호판 학습 데이터 수집, Roboflow 라벨링/오토 라벨링, 데이터셋 버전 관리 | FR-OCR01 |
| `feature/ai-model-training` | YOLO11n 번호판 탐지 모델 학습, YOLOv8n 비교 학습, 학습 로그/가중치 산출 | FR-OCR01 |
| `feature/ai-model-retraining-artifacts` | GPU 재학습 전략, 학습 결과물 제외 기준, 성능 비교 산출물 관리 | FR-OCR01 |
| `feature/ai-model-evaluation` | mAP50, Precision, Recall, OCR 최종 성공률, 추론 속도 비교 및 모델 선정 | FR-OCR01 |
| `feature/ai-ocr-license-plate` | YOLO11 + EasyOCR 번호판 인식 API 적용 | FR-OCR01, FR-OCR02 |
| `feature/ai-ocr-observation` | 출발/도착, 고속도로 관측, 휴게소 진입/진출 관측 유형별 OCR 처리 | FR-OCR04, FR-OCR05, FR-OCR06 |
| `feature/ai-ocr-fallback` | 신뢰도 0.85 미만 수동 입력 fallback 처리 | FR-OCR03 |

**모델 훈련 작업 흐름:**

```
feature/ai-model-dataset
  → feature/ai-model-training
  → feature/ai-model-retraining-artifacts
  → feature/ai-model-evaluation
  → feature/ai-ocr-license-plate
  → feature/ai-ocr-observation
```

**모델 산출물 관리 기준:**

- 학습 데이터 원본, 라벨링 데이터, 모델 가중치(`*.pt`, `*.pth`)는 Git에 직접 커밋하지 않는다.
- 데이터셋 버전, 라벨링 기준, 학습 설정, 성능 지표 요약은 문서 또는 설정 파일로 남긴다.
- `runs/`, `wandb/`, `mlruns/`, `evaluate/results/*.json`, GPU 재학습 결과물, 모델 가중치는 로컬 또는 외부 저장소에 보관하고 Git에는 제외한다.
- Git에는 재현 가능한 학습 설정(`train_config.yaml`), 평가 스크립트, 결과 요약 문서만 포함한다.
- 기본 후보 모델은 `YOLO11n`으로 두고, `YOLOv8n`은 비교 기준 모델로 학습한다.
- 최종 모델은 검출 성능뿐 아니라 OCR 최종 성공률과 추론 속도를 함께 비교해 선정한다.

---

### 🛠️ Backend 레이어 (담당: 환희)

| 브랜치명 | 기능 | 요구사항 ID |
|---|---|---|
| `feature/be-entity-db` | JPA Entity 전체 (Company, Admin, Vehicle, Driver, DriveLog 등) | - |
| `feature/be-company-entity` | Company Entity, Repository, CompanyService 분리 | FR-COMPANY01 |
| `feature/be-auth-jwt` | Spring Security + JWT, ACTIVE 상태만 로그인 | FR-AUTH01 |
| `feature/be-auth-signup` | 일반 관리자 회원가입 API (PENDING 상태 생성) | FR-AUTH02, FR-AUTH03 |
| `feature/be-admin-approval` | 최상위 관리자 승인/거절/정지/해제 API | FR-SUPER01~04 |
| `feature/be-company-api` | 업체 목록/상세 조회 API | FR-SUPER05, FR-COMPANY01 |
| `feature/be-tenant-access` | TenantAccessService - 업체별 데이터 접근 제한 | FR-COMPANY02 |
| `feature/be-vehicle-crud` | 차량 CRUD API (TenantAccess 적용) | FR-C01 |
| `feature/be-driver-crud` | 운전자 CRUD + 차량 배정 API (TenantAccess 적용) | FR-C02 |
| `feature/be-gps-receiver` | GPS 수신 API (gps_data 저장, TenantAccess 검증) | FR-B02 |
| `feature/be-simulation-control` | 시뮬레이션 시작/중지 API (drive_log + company_id) | FR-B03 |
| `feature/be-rest-event` | 휴식 판단 (speed_kmh ≤ 3, 15분 이상 유효 휴식) | FR-FAT02 |
| `feature/be-fatigue-continuous-driving` | 연속 운행 시간 계산 | FR-FAT01 |
| `feature/be-fatigue-daily-driving` | 일일 총 운행 시간 계산 | FR-FAT03 |
| `feature/be-fatigue-night-driving` | 야간 운행 시간 계산 (22:00~06:00) | FR-FAT04 |
| `feature/be-fatigue-score` | 피로도 점수 산정 및 등급 결정 | FR-FAT05 |
| `feature/be-fatigue-reason` | fatigue_event.reason 판단 근거 저장 | FR-FAT06 |
| `feature/be-fatigue-threshold` | 피로도 임계값 조회/수정 API | FR-C03 |
| `feature/be-drive-history` | 운행 이력 목록/상세 조회 API (업체별) | FR-D01, FR-D02 |
| `feature/be-fatigue-stats` | 피로도 통계 API (일별) | FR-E01 |
| `feature/be-dashboard-summary` | 대시보드 요약 카드 API (업체별) | FR-A01 |
| `feature/be-swagger` | Springdoc Swagger API 문서 자동 생성 | - |

---

### 🖥️ Frontend 레이어 (담당: 경서)

| 브랜치명 | 기능 | 요구사항 ID |
|---|---|---|
| `feature/fe-project-setup` | Vue.js 3 + Vite + Pinia + Axios + Chart.js 초기 세팅 | - |
| `feature/fe-auth-login` | 로그인 화면, JWT 저장, role/status 기반 redirect | FR-AUTH01 |
| `feature/fe-signup` | 일반 관리자 회원가입 화면 | FR-AUTH02 |
| `feature/fe-approval-pending` | PENDING 상태 승인 대기 안내 화면 | FR-AUTH03 |
| `feature/fe-router-guard` | Vue Router 가드 (role + status 기반 접근 제어) | - |
| `feature/fe-super-admin-dashboard` | 최상위 관리자 전용 홈 화면 | - |
| `feature/fe-super-admin-approval` | 가입 승인/거절/정지/해제 UI | FR-SUPER01~04 |
| `feature/fe-super-admin-company` | 업체 목록 및 상태 조회 화면 | FR-SUPER05 |
| `feature/fe-dashboard-summary-card` | 통계 요약 카드 | FR-A01 |
| `feature/fe-dashboard-vehicle-table` | 차량 목록 테이블 | FR-A02 |
| `feature/fe-dashboard-fatigue-badge` | 피로도 배지 | FR-A03 |
| `feature/fe-dashboard-detail-panel` | 차량 상세 패널 | FR-A04 |
| `feature/fe-dashboard-call-link` | 위험 차량 운전자 tel 링크 | FR-A05 |
| `feature/fe-dashboard-polling` | Axios 5초 Polling | FR-A06 |
| `feature/fe-simulation-panel` | 시뮬레이션 화면 | FR-B01, FR-B04 |
| `feature/fe-vehicle-management` | 차량 CRUD 페이지 | FR-C01 |
| `feature/fe-driver-management` | 운전자 CRUD + 차량 배정 페이지 | FR-C02 |
| `feature/fe-fatigue-threshold-settings` | 피로도 임계값 설정 UI | FR-C03 |
| `feature/fe-drive-history-list` | 운행 이력 목록 | FR-D01 |
| `feature/fe-drive-history-detail` | 운행 상세 | FR-D02 |
| `feature/fe-fatigue-stats-chart` | 피로도 통계 Chart.js | FR-E01 |

---

### 🛰️ GPS Simulator (담당: 경서)

| 브랜치명 | 기능 | 요구사항 ID |
|---|---|---|
| `feature/sim-project-setup` | Python GPS 시뮬레이터 초기 구조 | - |
| `feature/sim-scenario-a-normal` | 시나리오 A - 정상 운행 패턴 | FR-B01 |
| `feature/sim-scenario-b-caution` | 시나리오 B - 주의 운행 패턴 | FR-B01 |
| `feature/sim-scenario-c-danger` | 시나리오 C - 위험 운행 패턴 | FR-B01 |
| `feature/sim-gps-data-sender` | GPS 데이터 BE API 전송 | FR-B02 |
| `feature/sim-rest-pattern` | 휴식 패턴 삽입 | FR-B02 |
| `feature/sim-night-pattern` | 야간 운행 패턴 삽입 | FR-B02 |

---

### ⚙️ Infra (공통)

| 브랜치명 | 기능 |
|---|---|
| `feature/infra-docker-compose` | Docker Compose 전체 구성 (BE + AI + FE + PostgreSQL) |
| `feature/infra-db-init` | PostgreSQL 초기 스키마 DDL (10테이블), 시드 데이터 (최상위관리자, 업체, 차량, 운전자, 임계값) |
| `feature/infra-env-config` | 환경변수 설정 (.env, application.yml, CORS 설정) |

---

## 3. Releases 관리

| Release 브랜치 | 목표 | 예상 시점 |
|---|---|---|
| `release/v0.1.0` | 프로젝트 기반 구조 통합 (FE/BE/SIM 기본 구조, DB init, 문서) | 5월 1주차 |
| `release/v0.1.1` | 인증/회원가입/승인 구조 안정화 | 5월 1주차 |
| `release/v0.2.0` | 백엔드/AI 핵심 완성 (차량/운전자, GPS, 피로도 계산, OCR) | 5월 2~3주차 |
| `release/v0.3.0` | 프론트엔드 + 시뮬레이터 1차 통합 | 5월 4주차 |
| `release/v1.0.0` | 전체 기능 연동 및 최종 제출 | 2026.05.27 (제출) |
| `release/v1.0.1` | 발표용 마무리 수정 | 2026.06.02 (발표) |

**릴리즈 흐름:**
```
feature/* → dev → release/vX.X.X → main
```

---

## 4. Tags 관리

Semantic Versioning 규칙: `vMAJOR.MINOR.PATCH`

| Tag | 시점 | 내용 |
|---|---|---|
| `v0.1.0` | 기반 구조 완료 | FE 초기 세팅, BE Entity, DB init, SIM 기본 구조, 문서 |
| `v0.1.1` | 인증/회원가입/승인 완료 | JWT 로그인, 회원가입, 최상위 관리자 승인 흐름 |
| `v0.2.0` | 백엔드 1차 완료 | 차량/운전자, GPS 수신, 피로도 계산 모델, TenantAccess |
| `v0.2.1` | AI 모듈 완료 | FastAPI + YOLO11 + EasyOCR 번호판 인식, 모델 학습/평가 기준 정리 |
| `v0.2.2` | GPS 시뮬레이터 완료 | 시나리오 A/B/C 생성 및 전송 |
| `v0.3.0` | 프론트엔드 1차 완료 | 회원가입/승인/로그인 화면, 최상위 관리자 화면, 대시보드 |
| `v0.3.1` | 프론트엔드 2차 완료 | 차량/운전자 관리, 운행 이력/통계 |
| `v1.0.0` | **최종 제출 버전** | 전체 통합 완료 (2026.05.27) |
| `v1.0.1` | **발표 버전** | 발표용 마무리 수정 (2026.06.02) |

**Tag 생성 명령어:**
```bash
git tag -a v1.0.0 -m "release: 최종 제출 버전"
git push origin v1.0.0
git push origin --tags
```

---

## 5. 커밋 메시지 컨벤션

### 기본 형식
```
{type}: {내용}
```

| type | 설명 |
|---|---|
| `feat` | 새 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 코드 리팩토링 |
| `docs` | 문서 수정 |
| `chore` | 빌드/설정 변경 |
| `test` | 테스트 추가 |

---

### 🤖 AI 에이전트 원격 업로드 및 커밋/PR 지침 (v1.6 추가)

에이전트(AI)가 사용자의 명시적인 원격 업로드 요청을 받아 `commit` 및 `push`를 진행할 때는 다음 지침을 엄격히 준수한다.

1. **커밋 메시지 형식**
   * 커밋 메시지는 포괄적인 작업 나열이 아니라, **실제 구현 및 수정한 기능들을 간단하고 명확하게 한글로 요약하여 작성**한다.
   * 형식: `[YYYY.MM.DD 파트담당이름 (ex. hwan, seo)] 구현한 기능`
   * *예시*: `[2026.05.26 seo] feat: JWT 리프레시 토큰 및 자동 로그아웃 기능 추가`

2. **PR(Pull Request) 및 Description 지침**
   * AI 에이전트는 **원격 레포지토리에 PR을 직접 생성하여 진행하지 않는다.**
   * 대신, 사용자가 바로 카피해서 PR 작성에 활용할 수 있도록 AI 응답 본문에 **마크다운 코드 블록(```)으로 PR Description을 작성하여 제공**한다.
   * **PR Description 구성 필수 내용**:
     * **구현한 기능의 세부 목록 및 내용**
     * **이번 PR의 핵심 설계 요약**
     * **이전 사항(기존 구조/로직)과 비교하여 달라진 점 및 개선점**

---

## 6. 팀원별 담당 브랜치 요약

### 유환희 (Backend + AI)
`feature/ai-*` 8개 + `feature/be-*` 22개 = **총 30개**

### 백경서 (Frontend + GPS Simulator)
`feature/fe-*` 21개 + `feature/sim-*` 7개 = **총 28개**

### 공통 (Infra)
`feature/infra-*` 3개
