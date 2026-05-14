# logmile 프로젝트 전체 구조도

- 프로젝트명: `logmile`
- 버전: v5.1
- 작성 기준일: 2026.05.14
- 변경 내용: 실제 프로젝트 파일 기준 재검증, BE API alias 및 FE API 연동 상태 반영, Company 멀티테넌시 및 PlateEvent 구조 보정

---

## 1. 시스템 전체 아키텍처

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          Docker Compose Network                         │
│                                                                         │
│  ┌──────────────┐   HTTP POST    ┌──────────────────────────────────┐  │
│  │ GPS Simulator│ ─────────────> │        Spring Boot (BE)          │  │
│  │  (Python)    │                │           :8080                  │  │
│  │  시나리오 A/B/C│                │                                  │  │
│  └──────────────┘                │  ┌──────────────────────────┐   │  │
│                                  │  │ GPS Receiver API         │   │  │
│  ┌──────────────┐   HTTP POST    │  │ Fatigue Calculator       │   │  │
│  │  FastAPI(AI) │ <─────────────>│  │ Rest Event Detector      │   │  │
│  │    :8000     │                │  │ Drive Log Manager        │   │  │
│  │              │                │  │ Auth (JWT + Signup)       │   │  │
│  │ YOLO11n      │                │  │ Vehicle/Driver CRUD      │   │  │
│  │ EasyOCR      │                │  │ Dashboard/History/Stats  │   │  │
│  └──────────────┘                │  └──────────────────────────┘   │  │
│                                  │              │                   │  │
│  ┌──────────────┐   REST API     │              │ JPA               │  │
│  │  Vue.js (FE) │ <─────────────>│              ▼                   │  │
│  │    :5173     │   (Axios/JWT)  │  ┌──────────────────────────┐   │  │
│  │              │                │  │     PostgreSQL 16         │   │  │
│  │ 관제 대시보드  │                │  │        :5432             │   │  │
│  │ 차량/운전자   │                │  │                          │   │  │
│  │ 이력/통계     │                │  │ admin / company          │   │  │
│  │ 임계값 설정   │                │  │ vehicle / driver         │   │  │
│  └──────────────┘                │  │ drive_log / gps_data     │   │  │
│                                  │  │ rest_event / fatigue_event│  │  │
│                                  │  │ fatigue_threshold        │   │  │
│                                  │  │ plate_event              │   │  │
│                                  │  └──────────────────────────┘   │  │
│                                  └──────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 백엔드 (BE) 상세 구조

### 패키지 구조 (Domain-based Architecture)

```
logmile_be/
├── build.gradle
├── settings.gradle
└── src/
    └── main/
        ├── java/com/project/logmile/
        │   ├── LogmileApplication.java
        │   │
        │   ├── common/                                      # 공통 계층
        │   │   ├── config/
        │   │   │   └── SwaggerConfig.java                   # Springdoc OpenAPI 설정
        │   │   ├── enums/
        │   │   │   ├── ScenarioType.java                    # A, B, C
        │   │   │   ├── DriveLogStatus.java                  # RUNNING, COMPLETED, STOPPED
        │   │   │   ├── RestType.java                        # PENDING, VALID, SUFFICIENT, INVALID
        │   │   │   ├── FatigueLevel.java                    # NORMAL, CAUTION, DANGER
        │   │   │   ├── AdminRole.java                       # ROLE_ADMIN, ROLE_SUPER_ADMIN
        │   │   │   ├── AdminStatus.java                     # PENDING, ACTIVE, INACTIVE, REJECTED, SUSPENDED
        │   │   │   ├── PlateEventType.java                  # ENTRY, EXIT
        │   │   │   ├── PlateLocationType.java               # HIGHWAY_GATE, REST_AREA, CCTV
        │   │   │   └── PlateSourceType.java                 # OCR, SIMULATOR, MANUAL, DUMMY
        │   │   ├── exception/
        │   │   │   ├── BusinessException.java               # 비즈니스 예외 (ErrorCode 기반)
        │   │   │   ├── ErrorCode.java                       # 에러 코드 열거형
        │   │   │   ├── ErrorResponse.java                   # 에러 응답 DTO
        │   │   │   └── GlobalExceptionHandler.java          # @RestControllerAdvice
        │   │   └── security/
        │   │       ├── SecurityConfig.java                  # Spring Security + JWT 필터 체인
        │   │       ├── TenantAccessService.java             # 회사 기반 데이터 격리
        │   │       └── jwt/
        │   │           ├── JwtTokenProvider.java            # JWT 생성/검증/파싱
        │   │           ├── JwtAuthenticationFilter.java     # 요청별 JWT 인증 필터
        │   │           └── CustomUserDetailsService.java    # Admin 기반 UserDetails 로드
        │   │
        │   └── domain/                                      # 도메인 계층
        │       │
        │       ├── admin/                                   # 관리자 승인
        │       │   ├── controller/AdminApprovalController.java
        │       │   ├── dto/AdminSummaryResponse.java
        │       │   ├── entity/Admin.java
        │       │   ├── repository/AdminRepository.java
        │       │   └── service/AdminApprovalService.java
        │       │
        │       ├── auth/                                    # 인증 (로그인/회원가입)
        │       │   ├── controller/AuthController.java       # POST /api/auth/login, /signup
        │       │   ├── dto/LoginRequest.java
        │       │   ├── dto/LoginResponse.java
        │       │   ├── dto/SignupRequest.java
        │       │   ├── dto/SignupResponse.java
        │       │   └── service/AuthService.java
        │       │
        │       ├── company/                                 # 회사 (멀티테넌시)
        │       │   ├── controller/CompanyController.java    # /api/companies
        │       │   ├── dto/CompanyRegisterRequest.java
        │       │   ├── dto/CompanyResponse.java
        │       │   ├── entity/Company.java
        │       │   ├── repository/CompanyRepository.java
        │       │   └── service/CompanyService.java
        │       │
        │       ├── dashboard/                               # 관제 대시보드
        │       │   ├── controller/DashboardController.java  # GET /api/dashboard/summary|vehicles
        │       │   ├── dto/DashboardSummaryResponse.java
        │       │   ├── dto/VehicleStatusResponse.java
        │       │   └── service/DashboardService.java
        │       │
        │       ├── drivelog/                                # 운행 기록
        │       │   ├── controller/DriveHistoryController.java # GET /api/drive-history, /api/drive-logs
        │       │   ├── controller/SimulationController.java  # POST /api/simulation/start
        │       │   │                                          # PATCH /api/simulation/{id}/stop
        │       │   ├── dto/DriveHistoryDetailResponse.java
        │       │   ├── dto/DriveHistoryListResponse.java
        │       │   ├── dto/SimulationStartRequest.java
        │       │   ├── dto/SimulationStartResponse.java
        │       │   ├── entity/DriveLog.java
        │       │   ├── repository/DriveLogRepository.java
        │       │   ├── service/DriveHistoryService.java
        │       │   └── service/SimulationService.java
        │       │
        │       ├── driver/                                  # 운전자 관리
        │       │   ├── controller/DriverController.java     # /api/drivers CRUD
        │       │   ├── dto/DriverRequest.java
        │       │   ├── dto/DriverResponse.java
        │       │   ├── entity/Driver.java
        │       │   ├── repository/DriverRepository.java
        │       │   └── service/DriverService.java
        │       │
        │       ├── fatigue/                                 # 피로도 판단
        │       │   ├── controller/FatigueThresholdController.java # /api/thresholds, /api/fatigue/thresholds
        │       │   ├── dto/ThresholdResponse.java
        │       │   ├── dto/ThresholdUpdateRequest.java
        │       │   ├── entity/FatigueEvent.java
        │       │   ├── entity/FatigueThreshold.java
        │       │   ├── repository/FatigueEventRepository.java
        │       │   ├── repository/FatigueThresholdRepository.java
        │       │   ├── service/FatigueScoreService.java     # 점수 합산, 등급 결정, reason 생성
        │       │   └── service/FatigueThresholdService.java
        │       │
        │       ├── gps/                                     # GPS 수신
        │       │   ├── controller/GpsController.java        # POST /api/gps
        │       │   ├── dto/GpsDataRequest.java
        │       │   ├── entity/GpsData.java
        │       │   ├── repository/GpsDataRepository.java
        │       │   └── service/GpsReceiverService.java      # GPS 저장 → RestEvent → FatigueScore
        │       │
        │       ├── plateevent/                              # 번호판 이벤트 기록
        │       │   ├── controller/PlateEventController.java # POST /api/simulation/plate-events
        │       │   ├── dto/PlateEventCreateRequest.java
        │       │   ├── dto/PlateEventResponse.java
        │       │   ├── entity/PlateEvent.java
        │       │   ├── repository/PlateEventRepository.java
        │       │   └── service/PlateEventService.java
        │       │
        │       ├── rest/                                    # 휴식 판단
        │       │   ├── entity/RestEvent.java
        │       │   ├── repository/RestEventRepository.java
        │       │   └── service/RestEventService.java        # speed≤3 판정, VALID/SUFFICIENT 분류
        │       │
        │       └── vehicle/                                 # 차량 관리
        │           ├── controller/VehicleController.java    # /api/vehicles CRUD
        │           ├── dto/VehicleRequest.java
        │           ├── dto/VehicleResponse.java
        │           ├── entity/Vehicle.java
        │           ├── repository/VehicleRepository.java
        │           └── service/VehicleService.java
        │
        └── resources/
            └── application.yml                              # server, spring.datasource, jpa, jwt 설정
```

### 계층 흐름

```
HTTP 요청
    ↓
Controller  (요청 파라미터 수신, DTO 변환, 응답 반환)
    ↓
Service     (비즈니스 로직, 트랜잭션, 계산)
    ↓
Repository  (JPA 쿼리, DB 접근)
    ↓
Entity      (DB 테이블 매핑)
    ↓
PostgreSQL
```

---

## 3. AI 서버 (FastAPI) 상세 구조

```
logmile_ai/
├── requirements.txt                  # fastapi, ultralytics, easyocr, python-multipart
├── Dockerfile
├── .env
└── app/
    ├── main.py                       # FastAPI 앱 진입점, 라우터 등록
    ├── core/
    │   └── config.py                 # 환경변수, 신뢰도 임계값 설정 (0.85)
    ├── router/
    │   └── ocr_router.py             # POST /api/ocr/recognize
    ├── service/
    │   ├── yolo_service.py           # YOLO11n 번호판 영역 탐지
    │   ├── ocr_service.py            # EasyOCR 문자 추출
    │   └── plate_service.py          # 탐지 + OCR 통합, 신뢰도 판정
    ├── schema/
    │   ├── ocr_request.py            # 이미지 업로드 요청 스키마
    │   └── ocr_response.py           # { plate_no, confidence, is_manual_required }
    └── model/
        └── yolov8n.pt                # 사전학습 모델 가중치
```

**OCR API 흐름:**
```
이미지 업로드
    ↓
YOLO11n → 번호판 영역 bbox 탐지
    ↓
EasyOCR → 번호판 문자 추출 + confidence
    ↓
confidence ≥ 0.85 ?
  ├── YES → { plate_no, confidence, is_manual_required: false }
  └── NO  → { plate_no: null, confidence, is_manual_required: true }
```

---

## 4. GPS 시뮬레이터 (SIM) 상세 구조

```
logmile_sim/
├── requirements.txt                  # requests, python-dotenv
├── .env                              # BE API URL, 시나리오 설정
└── simulator/
    ├── main.py                       # 시뮬레이터 실행 진입점 (시나리오 선택)
    ├── config.py                     # API URL, 시나리오 파라미터
    ├── sender.py                     # HTTP POST → /api/gps (GpsDataRequest 형식)
    ├── patterns/
    │   ├── rest_pattern.py           # speed ≤ 3, 15분/30분 휴식 데이터 삽입
    │   └── night_pattern.py          # 22:00~06:00 구간 시각 보정
    └── scenarios/
        ├── scenario_a.py             # 정상: 적절한 휴식, 야간 없음
        ├── scenario_b.py             # 주의: 휴식 부족, 야간 일부
        └── scenario_c.py             # 위험: 장시간 연속 + 야간 + 휴식 누락
```

**시나리오별 특성:**

| 시나리오 | 연속 운행 | 휴식 | 야간 운행 | 예상 피로 등급 |
|---|---|---|---|---|
| A (정상) | 90분 미만 | 30분 이상 충분 | 없음 | 정상 (0~39) |
| B (주의) | 120~180분 | 15분 유효 1~2회 | 30분 내외 | 주의 (40~69) |
| C (위험) | 240분 이상 | 누락 2회 이상 | 2시간 이상 | 위험 (70+) |

**GPS 데이터 전송 형식:**
```json
{
  "driveLogId": 1,
  "latitude": 37.5665,
  "longitude": 126.9780,
  "speedKmh": 72.5,
  "recordedAt": "2026-04-29T22:30:00"
}
```

---

## 5. 프론트엔드 (FE) 상세 구조

```
logmile_fe/
├── package.json                      # vue, vite, pinia, axios, chart.js
├── vite.config.js
├── index.html
└── src/
    ├── main.js                       # Vue 앱 생성, Pinia, Router 등록
    ├── router/
    │   └── index.js                  # 라우트 정의 + 인증 가드 (requiresAuth, requiresSuper)
    │
    ├── stores/
    │   ├── authStore.js              # JWT 토큰, 로그인 상태, 역할(role) 관리
    │   ├── dashboardStore.js         # 대시보드 요약, polling 관리
    │   └── simulationStore.js        # 시뮬레이션 진행 상태
    │
    ├── api/
    │   ├── axios.js                  # Axios 인스턴스, JWT 인터셉터
    │   ├── authApi.js                # POST /api/auth/login, /api/auth/signup
    │   ├── approvalApi.js            # 관리자 승인 API (SUPER_ADMIN 전용)
    │   ├── vehicleApi.js             # /api/vehicles CRUD
    │   ├── driverApi.js              # /api/drivers CRUD
    │   ├── dashboardApi.js           # GET /api/dashboard/summary|vehicles
    │   ├── simulationApi.js          # POST /api/simulation/start
    │   │                             # PATCH /api/simulation/{id}/stop
    │   ├── driveHistoryApi.js        # GET /api/drive-logs
    │   ├── fatigueStatsApi.js        # GET /api/fatigue/stats
    │   └── thresholdApi.js           # GET /api/fatigue/thresholds, PUT /api/fatigue/thresholds/{key}
    │
    ├── views/
    │   │   ── 인증 (레이아웃 없음) ──────────────────────
    │   ├── LoginView.vue             # FR-AUTH01 — 로그인
    │   ├── SignupView.vue            # FR-AUTH01 — 회원가입
    │   ├── PendingView.vue           # 승인 대기 안내 화면
    │   │   ── Super Admin 전용 (/super) ─────────────────
    │   ├── SuperHomeView.vue         # Super Admin 홈
    │   ├── SuperApprovalView.vue     # 관리자 계정 승인 관리
    │   ├── SuperCompanyView.vue      # 회사 목록 관리
    │   │   ── Admin 전용 (/) ──────────────────────────────
    │   ├── DashboardView.vue         # FR-A01~A06
    │   ├── SimulationView.vue        # FR-B01, B04
    │   ├── VehicleView.vue           # FR-C01
    │   ├── DriverView.vue            # FR-C02
    │   ├── ThresholdView.vue         # FR-C03
    │   ├── DriveHistoryView.vue      # FR-D01
    │   ├── DriveHistoryDetailView.vue # FR-D02
    │   └── FatigueStatsView.vue      # FR-E01
    │
    ├── components/
    │   ├── layout/
    │   │   ├── AppLayout.vue         # 사이드바 + 상단바 레이아웃 래퍼
    │   │   ├── AppSidebar.vue        # 네비게이션 사이드바
    │   │   ├── AppTopbar.vue         # 상단 바
    │   │   └── AppLogo.vue           # 로고 컴포넌트
    │   └── common/
    │       └── AppIcon.vue           # 공통 아이콘 컴포넌트
    │
    └── data/
        └── mockData.js               # 개발/시연용 목 데이터
```

**라우트 가드 규칙:**

| 조건 | 동작 |
|---|---|
| 비로그인 → requiresAuth 페이지 | `/login` 리다이렉트 |
| ROLE_ADMIN → requiresSuper 페이지 | `/` 리다이렉트 |
| 로그인 상태 → login/signup/pending | 역할별 홈으로 리다이렉트 |

**2026.05.14 FE 데이터 소스 검증 결과:**

| 화면/API 모듈 | 현재 기준 | 비고 |
|---|---|---|
| LoginView / SignupView | API 연동 | `/api/auth/login`, `/api/auth/signup` |
| SuperApprovalView / SuperHomeView | API 연동 | `/api/admin/approvals/*` |
| SuperCompanyView | API 연동 | `/api/companies`, activate/deactivate |
| FatigueStatsView | API 연동 | `/api/fatigue/stats` |
| driveHistoryApi / thresholdApi | API 모듈 경로 정합 | BE alias 기준 `/api/drive-logs`, `/api/fatigue/thresholds` 사용 가능 |
| DashboardView / DriveHistoryView / DriveHistoryDetailView / VehicleView / DriverView / ThresholdView | mock 기반 화면 유지 | 실제 API 모듈은 존재하나 화면 단위 연결은 후속 작업 |
| SimulationView | mock/API 혼재 | start/stop API 모듈은 정합, 화면 상태와 GPS 시뮬레이터 통합 검증 필요 |

---

## 6. 인프라 (INFRA) 상세 구조

```
logmile_infra/
├── docker-compose.yml                # 전체 서비스 오케스트레이션
├── .env                              # 공통 환경변수
└── db/
    ├── init.sql                      # 테이블 DDL (10개 테이블)
    └── seed.sql                      # 초기 데이터 (회사 10개, 관리자 31명, 차량 50대, 운전자 50명, 임계값 21건)
```

**Docker Compose 서비스 구성:**
```yaml
services:
  postgres:   # PostgreSQL 16 (:5432)
  backend:    # Spring Boot   (:8080) — postgres 의존
  ai:         # FastAPI        (:8000)
  frontend:   # Vue.js/Nginx   (:80)
  simulator:  # Python SIM     (필요 시 수동 실행)
```

---

## 7. API 엔드포인트 전체 목록

| 메서드 | 경로 | 기능 | 인증 |
|---|---|---|---|
| POST | `/api/auth/login` | 관리자 로그인, JWT 발급 | 불필요 |
| POST | `/api/auth/signup` | 관리자 회원가입 (승인 대기) | 불필요 |
| GET | `/api/admin/approvals/pending` | 승인 대기 관리자 목록 | JWT (SUPER_ADMIN) |
| PATCH | `/api/admin/approvals/{adminId}/approve` | 관리자 승인 | JWT (SUPER_ADMIN) |
| PATCH | `/api/admin/approvals/{adminId}/reject` | 관리자 거절 | JWT (SUPER_ADMIN) |
| PATCH | `/api/admin/approvals/{adminId}/suspend` | 관리자 정지 | JWT (SUPER_ADMIN) |
| PATCH | `/api/admin/approvals/{adminId}/unsuspend` | 관리자 정지 해제 | JWT (SUPER_ADMIN) |
| GET | `/api/companies` | 업체 목록 조회 | JWT (SUPER_ADMIN) |
| GET | `/api/companies/{companyId}` | 업체 상세 조회 | JWT (SUPER_ADMIN) |
| PATCH | `/api/companies/{companyId}/activate` | 업체 활성화 | JWT (SUPER_ADMIN) |
| PATCH | `/api/companies/{companyId}/deactivate` | 업체 비활성화 | JWT (SUPER_ADMIN) |
| GET | `/api/companies/me` | 내 업체 정보 조회 | JWT (ADMIN) |
| GET | `/api/vehicles` | 차량 목록 조회 | JWT (ADMIN) |
| POST | `/api/vehicles` | 차량 등록 | JWT (ADMIN) |
| GET | `/api/vehicles/{id}` | 차량 상세 조회 | JWT (ADMIN) |
| PATCH | `/api/vehicles/{id}` | 차량 수정 | JWT (ADMIN) |
| DELETE | `/api/vehicles/{id}` | 차량 삭제 | JWT (ADMIN) |
| GET | `/api/drivers` | 운전자 목록 조회 | JWT (ADMIN) |
| POST | `/api/drivers` | 운전자 등록 | JWT (ADMIN) |
| GET | `/api/drivers/{id}` | 운전자 상세 조회 | JWT (ADMIN) |
| PATCH | `/api/drivers/{id}` | 운전자 수정 | JWT (ADMIN) |
| PATCH | `/api/drivers/{id}/assign/{vehicleId}` | 차량 배정 | JWT (ADMIN) |
| PATCH | `/api/drivers/{id}/unassign` | 차량 배정 해제 | JWT (ADMIN) |
| DELETE | `/api/drivers/{id}` | 운전자 삭제 | JWT (ADMIN) |
| POST | `/api/simulation/start` | 시뮬레이션 시작 (DriveLog 생성) | JWT (ADMIN) |
| PATCH | `/api/simulation/{driveLogId}/stop` | 시뮬레이션 중지 | JWT (ADMIN) |
| POST | `/api/gps` | GPS 데이터 수신 + 피로도 재계산 | JWT |
| POST | `/api/simulation/plate-events` | 번호판 관측 이벤트 저장 | JWT |
| GET | `/api/dashboard/summary` | 통계 요약 카드 데이터 | JWT (ADMIN/SUPER_ADMIN) |
| GET | `/api/dashboard/vehicles` | 차량별 현재 피로도 상태 | JWT (ADMIN/SUPER_ADMIN) |
| GET | `/api/drive-history`, `/api/drive-logs` | 운행 이력 목록 | JWT (ADMIN/SUPER_ADMIN) |
| GET | `/api/drive-history/{driveLogId}`, `/api/drive-logs/{driveLogId}` | 운행 이력 상세 | JWT (ADMIN/SUPER_ADMIN) |
| GET | `/api/fatigue/stats` | 일별 피로도 통계 | JWT (ADMIN) |
| GET | `/api/thresholds`, `/api/fatigue/thresholds` | 피로도 임계값 전체 조회 | JWT (ADMIN/SUPER_ADMIN) |
| PATCH | `/api/thresholds/{id}` | 임계값 수정 | JWT (SUPER_ADMIN) |
| PUT | `/api/fatigue/thresholds/{key}` | 임계값 수정 (key 기준) | JWT (SUPER_ADMIN) |
| POST | `/api/ocr/recognize` (AI) | 번호판 이미지 인식 | 없음(내부) |

---

## 8. 피로도 계산 흐름

```
GPS 데이터 수신 (POST /api/gps)
    ↓
GpsData 저장
    ↓
RestEventService.process()
  speed ≤ 3 km/h ?
  ├── YES → 휴식 시작/진행 중 업데이트 (RestEvent)
  │         15분 이상 → VALID / 30분 이상 → SUFFICIENT
  └── NO  → 운행 중 처리
    ↓
FatigueScoreService.calculate()
  ├── 연속 운행 시간 계산 (마지막 유효 휴식 이후)
  ├── 일일 총 운행 시간 계산 (당일 DriveLog 합산)
  ├── 야간 운행 시간 계산 (22:00~06:00 구간)
  ├── 휴식 부족/보정 점수 적용
  └── 점수 합산 → 등급 결정 → reason 텍스트 생성
    ↓
FatigueEvent 저장
    ↓
대시보드 polling (5초) → 최신 FatigueEvent 반환
```

---

## 9. DB 테이블 관계도 (ERD 요약)

```
company
  │
  └──< admin           (company_id FK — 회사별 관리자)

admin
  (admin 인증 + 승인 상태 관리)

company ──< vehicle    (company_id FK)
company ──< driver     (company_id FK)

vehicle ──< drive_log >── driver
   │                         │
   └── driver_id (nullable)  └── vehicle_id (nullable)
   (상호 참조 — 배정 관계)

drive_log ──< gps_data        (CASCADE DELETE)
drive_log ──< rest_event      (CASCADE DELETE)
drive_log ──< fatigue_event   (CASCADE DELETE)
vehicle ──< plate_event       (번호판 관측 이벤트, vehicle_id nullable)

fatigue_threshold
  (독립 테이블 — key/value 설정값)
```
