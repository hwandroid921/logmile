# logmile 팀원별 기능 구현 및 브랜치 순서

> 프로젝트명: logmile  
> 기준 브랜치: `dev`  
> 원칙: feature 브랜치는 미리 전부 만들지 않고, 해당 기능 개발을 시작할 때 `dev`에서 생성 후 원격 push한다.

---

## 1. 공통 브랜치 운영 원칙

### 기본 흐름

```bash
git switch dev
git pull origin dev
git switch -c feature/{layer}-{feature-name}
git push -u origin feature/{layer}-{feature-name}
```

개발 완료 후에는 GitHub에서 `feature/*` 브랜치를 `dev`로 Pull Request 한다.

### 브랜치 역할

| 브랜치 | 역할 |
|---|---|
| `main` | 최종 릴리즈 브랜치 |
| `dev` | 팀 통합 개발 브랜치 |
| `feature/*` | 기능 단위 개발 브랜치 |
| `release/*` | 제출 전 QA 및 릴리즈 준비 브랜치 |

### 커밋 메시지 예시

```text
feat: 피로도 점수 계산 로직 구현
fix: GPS 수신 시간 변환 오류 수정
refactor: 운행 이력 조회 서비스 분리
docs: 브랜치 전략 문서 업데이트
chore: Docker Compose 환경 변수 정리
test: 피로도 계산 단위 테스트 추가
```

---

## 2. 유환희 - 백엔드 + AI 구현 순서

### Phase 1. 백엔드 기반 구조

| 순서 | 브랜치 | 구현 내용 | 선행 조건 |
|---:|---|---|---|
| 1 | `feature/be-entity-db` | JPA Entity, Repository, 기본 DB 매핑 구현 | DB 초기 설계 |
| 2 | `feature/be-auth-jwt` | Spring Security, JWT 로그인, ROLE_ADMIN 인증/인가 | Entity 구조 |
| 3 | `feature/be-swagger` | Springdoc Swagger 설정 및 API 문서 기반 구성 | 기본 Controller 구조 |

### Phase 2. 차량/운전자 관리 API

| 순서 | 브랜치 | 구현 내용 | 선행 조건 |
|---:|---|---|---|
| 4 | `feature/be-vehicle-crud` | 차량 등록/조회/수정/삭제 API | Entity 구현 |
| 5 | `feature/be-driver-crud` | 운전자 등록/조회/수정/삭제, 차량 배정 API | 차량 API |

### Phase 3. 운행 및 GPS 데이터 수신

| 순서 | 브랜치 | 구현 내용 | 선행 조건 |
|---:|---|---|---|
| 6 | `feature/be-gps-receiver` | GPS 데이터 수신 API, `gps_data` 저장 | Entity 구현 |
| 7 | `feature/be-simulation-control` | 시뮬레이션 시작/중지 API, `drive_log` 상태 관리 | GPS 수신 API |

### Phase 4. 피로도 핵심 로직

| 순서 | 브랜치 | 구현 내용 | 선행 조건 |
|---:|---|---|---|
| 8 | `feature/be-rest-event` | `speed_kmh <= 3`, 15분 이상 유효 휴식 판단 및 저장 | GPS 데이터 |
| 9 | `feature/be-fatigue-continuous-driving` | 유효 휴식 전까지 연속 운행 시간 계산 | 휴식 이벤트 |
| 10 | `feature/be-fatigue-daily-driving` | 운전자 기준 일일 총 운행 시간 누적 계산 | 운행 로그 |
| 11 | `feature/be-fatigue-night-driving` | 22:00~06:00 야간 운행 시간 계산 | GPS 데이터 |
| 12 | `feature/be-fatigue-score` | 피로도 점수 합산 및 정상/주의/위험 등급 결정 | 피로도 항목 계산 |
| 13 | `feature/be-fatigue-reason` | `fatigue_event.reason`에 판단 근거 저장 | 점수 계산 |
| 14 | `feature/be-fatigue-threshold` | 피로도 임계값 key/value 조회 및 수정 API | 점수 계산 |

### Phase 5. 대시보드/이력/통계 API

| 순서 | 브랜치 | 구현 내용 | 선행 조건 |
|---:|---|---|---|
| 15 | `feature/be-dashboard-summary` | 운행 중 차량 수, 주의/위험 차량 수, 평균 피로 점수 API | 피로도 이벤트 |
| 16 | `feature/be-drive-history` | 운행 이력 목록/상세 조회 API | 운행 로그, 피로도 이벤트 |
| 17 | `feature/be-fatigue-stats` | 일별 운행/야간/휴식 누락/평균 점수 통계 API | 이력 데이터 |

### Phase 6. AI 번호판 인식

| 순서 | 브랜치 | 구현 내용 | 선행 조건 |
|---:|---|---|---|
| 18 | `feature/ai-fastapi-server` | FastAPI 서버 구조, 라우터, 환경 설정 | AI 프로젝트 구조 |
| 19 | `feature/ai-ocr-license-plate` | YOLOv8 + EasyOCR 번호판 인식 API | FastAPI 서버 |
| 20 | `feature/ai-ocr-fallback` | 신뢰도 0.85 미만 수동 입력 fallback 처리 | OCR API |

---

## 3. 백경서 - 프론트엔드 + GPS 시뮬레이터 + 문서 구현 순서

### Phase 1. 프론트엔드/GPS 기반 구조

| 순서 | 브랜치 | 구현 내용 | 선행 조건 |
|---:|---|---|---|
| 1 | `feature/fe-project-setup` | Vue 3, Vite, Pinia, Axios, Chart.js 초기 세팅 | 없음 |
| 2 | `feature/sim-project-setup` | Python GPS 시뮬레이터 프로젝트 구조 및 설정 | 없음 |

### Phase 2. 인증 화면 및 라우팅

| 순서 | 브랜치 | 구현 내용 | 선행 조건 |
|---:|---|---|---|
| 3 | `feature/fe-auth-login` | 로그인 화면, JWT 토큰 저장 및 인증 처리 | 백엔드 인증 API |
| 4 | `feature/fe-router-guard` | Vue Router 인증 가드, 미로그인 redirect | 로그인 화면 |

### Phase 3. 차량/운전자 관리 화면

| 순서 | 브랜치 | 구현 내용 | 선행 조건 |
|---:|---|---|---|
| 5 | `feature/fe-vehicle-management` | 차량 등록/조회/수정/삭제 페이지 | 차량 API |
| 6 | `feature/fe-driver-management` | 운전자 등록/조회/수정/삭제 및 차량 배정 페이지 | 운전자 API |

### Phase 4. GPS 시뮬레이터

| 순서 | 브랜치 | 구현 내용 | 선행 조건 |
|---:|---|---|---|
| 7 | `feature/sim-scenario-a-normal` | 시나리오 A - 정상 운행 패턴 생성 | 시뮬레이터 구조 |
| 8 | `feature/sim-rest-pattern` | 휴식 패턴 삽입, 15분/30분 휴식 데이터 생성 | 시나리오 A |
| 9 | `feature/sim-night-pattern` | 22:00~06:00 야간 운행 패턴 생성 | 시나리오 A |
| 10 | `feature/sim-scenario-b-caution` | 시나리오 B - 주의 운행 패턴 생성 | 휴식/야간 패턴 |
| 11 | `feature/sim-scenario-c-danger` | 시나리오 C - 위험 운행 패턴 생성 | 휴식/야간 패턴 |
| 12 | `feature/sim-gps-data-sender` | 생성 GPS 데이터를 백엔드 API로 HTTP POST 전송 | GPS 수신 API |

### Phase 5. 시뮬레이션 UI

| 순서 | 브랜치 | 구현 내용 | 선행 조건 |
|---:|---|---|---|
| 13 | `feature/fe-simulation-panel` | 시나리오 선택, 실행/중지, 진행 상태 표시 | 시뮬레이션 API, GPS sender |

### Phase 6. 대시보드 UI

| 순서 | 브랜치 | 구현 내용 | 선행 조건 |
|---:|---|---|---|
| 14 | `feature/fe-dashboard-summary-card` | 운행 중/주의/위험/완료/평균 피로 점수 요약 카드 | 대시보드 요약 API |
| 15 | `feature/fe-dashboard-vehicle-table` | 차량 목록 테이블, 속도/운행시간/피로점수 표시 | 차량 상태 API |
| 16 | `feature/fe-dashboard-fatigue-badge` | 정상/주의/위험 등급 배지 색상 표시 | 피로도 점수 데이터 |
| 17 | `feature/fe-dashboard-detail-panel` | 차량 상세 패널, 점수 근거, 휴식 타임라인 | 피로도 상세 API |
| 18 | `feature/fe-dashboard-call-link` | 위험 차량 운전자 `tel:` 링크 연결 | 운전자 연락처 데이터 |
| 19 | `feature/fe-dashboard-polling` | Axios 5초 polling으로 차량 상태 갱신 | 대시보드 UI |

### Phase 7. 이력/통계/설정 화면

| 순서 | 브랜치 | 구현 내용 | 선행 조건 |
|---:|---|---|---|
| 20 | `feature/fe-fatigue-threshold-settings` | 피로도 임계값 설정 UI | 임계값 API |
| 21 | `feature/fe-drive-history-list` | 운행 이력 목록 페이지 | 운행 이력 API |
| 22 | `feature/fe-drive-history-detail` | 속도/피로점수 변화, 이벤트 타임라인 상세 | 운행 상세 API |
| 23 | `feature/fe-fatigue-stats-chart` | Chart.js 기반 일별 운행/야간/휴식/평균 피로 차트 | 통계 API |

---

## 4. 공통 Infra 구현 순서

공통 작업은 두 팀원이 협의해 먼저 진행한다. 가능하면 프로젝트 초기에 완료해 백엔드, 프론트엔드, AI, 시뮬레이터가 같은 실행 환경을 바라보도록 한다.

| 순서 | 브랜치 | 구현 내용 | 담당 |
|---:|---|---|---|
| 1 | `feature/infra-docker-compose` | Backend, Frontend, AI, PostgreSQL Docker Compose 구성 | 공통 |
| 2 | `feature/infra-env-config` | `.env`, CORS, DB 접속, API URL 환경변수 정리 | 공통 |
| 3 | `feature/infra-db-init` | PostgreSQL 초기 스키마 DDL 및 시드 데이터 | 공통 |

---

## 5. 권장 통합 순서

기능은 팀원별로 병렬 개발하되, `dev` 병합 순서는 다음 흐름을 권장한다.

| 단계 | 병합 대상 |
|---:|---|
| 1 | Infra 초기 구성 |
| 2 | Backend Entity/DB |
| 3 | Auth + Frontend Login |
| 4 | Vehicle/Driver API + 화면 |
| 5 | GPS Receiver + Simulator |
| 6 | Rest Event + Fatigue 계산 |
| 7 | Dashboard API + Dashboard UI |
| 8 | Drive History + Stats |
| 9 | AI OCR |
| 10 | Swagger, 문서, 발표용 QA |

---

## 6. Release 브랜치 생성 시점

`release/*` 브랜치는 미리 만들지 않는다. 각 마일스톤 기능이 `dev`에 충분히 통합된 뒤 생성한다.

| 브랜치 | 생성 시점 |
|---|---|
| `release/v0.1.0` | 프로젝트 구조, Docker, DB Entity, 인증이 안정화된 뒤 |
| `release/v0.2.0` | 피로도 계산, GPS 수신, OCR 1차 구현 완료 뒤 |
| `release/v1.0.0` | 프론트엔드, 시뮬레이터, 백엔드 전체 통합 완료 뒤 |
| `release/v1.0.1` | 발표 전 수정 및 QA 완료 뒤 |

