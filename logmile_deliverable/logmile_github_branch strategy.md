# logmile GitHub 브랜치 전략

> 프로젝트명: logmile | 버전: v1.0 | 작성일: 2026.04.27  
> 개발 기간: 2026.04.28 ~ 2026.05.27 제출 / 2026.06.02 발표

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

## 2. Feature 브랜치 목록

### 브랜치 네이밍 컨벤션
```
feature/{layer}-{기능명}
```

---

### 🤖 AI 레이어 (담당: 환희)

| 브랜치명 | 기능 | 요구사항 ID |
|---|---|---|
| `feature/ai-ocr-license-plate` | YOLOv8 + EasyOCR 번호판 인식 | FR-OCR01, FR-OCR02 |
| `feature/ai-ocr-fallback` | 신뢰도 0.85 미만 수동 입력 fallback 처리 | FR-OCR03 |
| `feature/ai-fastapi-server` | FastAPI 서버 설정, 라우터, 환경 구성 | - |

---

### 🛠️ Backend 레이어 (담당: 환희)

| 브랜치명 | 기능 | 요구사항 ID |
|---|---|---|
| `feature/be-entity-db` | JPA Entity 전체 설계 (admin, driver, vehicle, drive_log, gps_data, rest_event, fatigue_event, fatigue_threshold) | - |
| `feature/be-auth-jwt` | Spring Security + JWT 인증/인가, ROLE_ADMIN | FR-AUTH01 |
| `feature/be-gps-receiver` | GPS 데이터 수신 API (gps_data 저장) | FR-B02 |
| `feature/be-simulation-control` | 시뮬레이션 시작/중지 API (drive_log 상태 관리) | FR-B03 |
| `feature/be-rest-event` | 휴식 판단 로직 (speed_kmh ≤ 3, 15분 이상 유효 휴식 기록) | FR-FAT02 |
| `feature/be-fatigue-continuous-driving` | 연속 운행 시간 계산 (유효 휴식 전까지 누적) | FR-FAT01 |
| `feature/be-fatigue-daily-driving` | 일일 총 운행 시간 누적 계산 | FR-FAT03 |
| `feature/be-fatigue-night-driving` | 야간 운행 시간 계산 (22:00~06:00 구간) | FR-FAT04 |
| `feature/be-fatigue-score` | 피로도 점수 산정 및 등급 결정 (정상/주의/위험) | FR-FAT05 |
| `feature/be-fatigue-reason` | fatigue_event.reason 판단 근거 저장 | FR-FAT06 |
| `feature/be-vehicle-crud` | 차량 등록/조회/수정/삭제 API | FR-C01 |
| `feature/be-driver-crud` | 운전자 등록/조회/수정/삭제, 차량 배정 API | FR-C02 |
| `feature/be-fatigue-threshold` | 피로도 임계값 key/value 조회/수정 API | FR-C03 |
| `feature/be-drive-history` | 운행 이력 목록/상세 조회 API | FR-D01, FR-D02 |
| `feature/be-fatigue-stats` | 피로도 통계 API (일별 운행/야간/휴식 누락/평균 점수) | FR-E01 |
| `feature/be-dashboard-summary` | 통계 요약 카드 API (운행 중 차량 수, 주의/위험 차량 수, 평균 피로 점수) | FR-A01 |
| `feature/be-swagger` | Springdoc Swagger API 문서 자동 생성 설정 | - |

---

### 🖥️ Frontend 레이어 (담당: 경서)

| 브랜치명 | 기능 | 요구사항 ID |
|---|---|---|
| `feature/fe-project-setup` | Vue.js 3 + Vite + Pinia + Axios + Chart.js 프로젝트 초기 세팅 | - |
| `feature/fe-auth-login` | 로그인 화면, JWT 토큰 저장 및 인증 처리 | FR-AUTH01 |
| `feature/fe-dashboard-summary-card` | 통계 요약 카드 (운행 중/주의/위험/완료/평균 피로 점수) | FR-A01 |
| `feature/fe-dashboard-vehicle-table` | 차량 목록 테이블 (차량번호, 운전자, 속도, 운행시간, 피로점수 등) | FR-A02 |
| `feature/fe-dashboard-fatigue-badge` | 피로도 배지 (정상/주의/위험 등급 색상 표시) | FR-A03 |
| `feature/fe-dashboard-detail-panel` | 차량 상세 패널 (점수 근거, 휴식 타임라인) | FR-A04 |
| `feature/fe-dashboard-call-link` | 위험 차량 운전자 tel 링크 연결 | FR-A05 |
| `feature/fe-dashboard-polling` | Axios 5초 Polling (차량 상태·피로 점수 갱신) | FR-A06 |
| `feature/fe-simulation-panel` | 시뮬레이션 화면 (시나리오 선택, 실행/중지, 진행 상태 표시) | FR-B01, FR-B04 |
| `feature/fe-vehicle-management` | 차량 등록/조회/수정/삭제 페이지 | FR-C01 |
| `feature/fe-driver-management` | 운전자 등록/조회/수정/삭제, 차량 배정 페이지 | FR-C02 |
| `feature/fe-fatigue-threshold-settings` | 피로도 임계값 설정 UI (연속운행/일일운행/야간/휴식보정/등급 범위) | FR-C03 |
| `feature/fe-drive-history-list` | 운행 이력 목록 (총 운행시간, 야간시간, 휴식 횟수, 최고 피로점수) | FR-D01 |
| `feature/fe-drive-history-detail` | 운행 상세 (속도 변화 차트, 피로점수 변화, 이벤트 타임라인) | FR-D02 |
| `feature/fe-fatigue-stats-chart` | 피로도 통계 Chart.js (일별 운행시간, 야간운행, 휴식 누락, 평균 피로) | FR-E01 |
| `feature/fe-router-guard` | Vue Router 인증 가드 (미로그인 redirect 처리) | - |

---

### 🛰️ GPS Simulator (담당: 경서)

| 브랜치명 | 기능 | 요구사항 ID |
|---|---|---|
| `feature/sim-project-setup` | Python GPS 시뮬레이터 프로젝트 초기 구조 및 설정 | - |
| `feature/sim-scenario-a-normal` | 시나리오 A - 정상 운행 패턴 생성 | FR-B01 |
| `feature/sim-scenario-b-caution` | 시나리오 B - 주의 운행 패턴 생성 (휴식 부족/야간 포함) | FR-B01 |
| `feature/sim-scenario-c-danger` | 시나리오 C - 위험 운행 패턴 생성 (장시간 연속 + 야간) | FR-B01 |
| `feature/sim-gps-data-sender` | 생성된 GPS 데이터 BE API로 전송 (HTTP POST) | FR-B02 |
| `feature/sim-rest-pattern` | 휴식 패턴 삽입 (speed ≤ 3, 15분/30분 휴식) | FR-B02 |
| `feature/sim-night-pattern` | 야간 운행 패턴 삽입 (22:00~06:00 구간 시뮬레이션) | FR-B02 |

---

### ⚙️ Infra (공통)

| 브랜치명 | 기능 |
|---|---|
| `feature/infra-docker-compose` | Docker Compose 전체 구성 (BE + AI + FE + PostgreSQL) |
| `feature/infra-db-init` | PostgreSQL 초기 스키마 DDL, 시드 데이터 |
| `feature/infra-env-config` | 환경변수 설정 (.env, application.yml, CORS 설정) |

---

## 3. Releases 관리

| Release 브랜치 | 목표 | 예상 시점 |
|---|---|---|
| `release/v0.1.0` | 프로젝트 초기 세팅 완료 (DB 스키마, Docker, JWT 인증) | 5월 1주차 |
| `release/v0.2.0` | 백엔드 핵심 완성 (피로도 계산 전체, GPS 수신, OCR) | 5월 2~3주차 |
| `release/v1.0.0` | 프론트엔드 + 시뮬레이터 통합, 전체 기능 연동 완료 | 2026.05.27 (제출) |
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
| `v0.1.0` | 초기 세팅 완료 | 프로젝트 구조, Docker, DB Entity |
| `v0.1.1` | 인증 완료 | JWT 로그인/인가 |
| `v0.2.0` | 백엔드 1차 완료 | 피로도 계산 모델 전체, GPS 수신 API |
| `v0.2.1` | AI 모듈 완료 | FastAPI + YOLOv8 + EasyOCR 번호판 인식 |
| `v0.2.2` | GPS 시뮬레이터 완료 | 시나리오 A/B/C 생성 및 전송 |
| `v0.3.0` | 프론트엔드 1차 완료 | 대시보드, 시뮬레이션 화면 |
| `v0.3.1` | 프론트엔드 2차 완료 | 차량·운전자 관리, 운행 이력·통계 |
| `v1.0.0` | **최종 제출 버전** | 전체 통합 완료 (2026.05.27) |
| `v1.0.1` | **발표 버전** | 발표용 마무리 수정 (2026.06.02) |

**Tag 생성 명령어:**
```bash
# 어노테이션 태그 생성
git tag -a v1.0.0 -m "release: 최종 제출 버전"

# 원격 저장소에 태그 푸시
git push origin v1.0.0

# 모든 태그 한 번에 푸시
git push origin --tags
```

---

## 5. 커밋 메시지 컨벤션

```
{type}: {내용}

예시)
feat: 피로도 점수 계산 로직 구현
fix: GPS polling 5초 간격 오류 수정
refactor: fatigue_event 저장 로직 분리
docs: 요구사항 정의서 업데이트
chore: Docker Compose 포트 설정 수정
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

## 6. 팀원별 담당 브랜치 요약

### 유환희 (Backend + AI)
`feature/ai-*` 3개 + `feature/be-*` 17개 = **총 20개**

### 백경서 (Frontend + GPS Simulator)
`feature/fe-*` 16개 + `feature/sim-*` 7개 = **총 23개**

### 공통 (Infra)
`feature/infra-*` 3개
