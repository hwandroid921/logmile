# logmile 프로젝트 전체 구조도

- 프로젝트명: `logmile`
- 버전: v4.0
- 작성 기준일: 2026.04.29

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
│  │              │                │  │ Auth (JWT)               │   │  │
│  │ YOLOv8n      │                │  │ Vehicle/Driver CRUD      │   │  │
│  │ EasyOCR      │                │  │ Dashboard/History/Stats  │   │  │
│  └──────────────┘                │  └──────────────────────────┘   │  │
│                                  │              │                   │  │
│  ┌──────────────┐   REST API     │              │ JPA               │  │
│  │  Vue.js (FE) │ <─────────────>│              ▼                   │  │
│  │    :5173     │   (Axios/JWT)  │  ┌──────────────────────────┐   │  │
│  │              │                │  │     PostgreSQL 16         │   │  │
│  │ 관제 대시보드  │                │  │        :5432             │   │  │
│  │ 차량/운전자   │                │  │                          │   │  │
│  │ 이력/통계     │                │  │ admin / vehicle / driver │   │  │
│  │ 임계값 설정   │                │  │ drive_log / gps_data     │   │  │
│  └──────────────┘                │  │ rest_event / fatigue_event│  │  │
│                                  │  │ fatigue_threshold        │   │  │
│                                  │  └──────────────────────────┘   │  │
│                                  └──────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 백엔드 (BE) 상세 구조

### 계층 구조 (MVC Layered Architecture)

```
logmile_be/
├── build.gradle
├── settings.gradle
└── src/
    └── main/
        ├── java/com/project/logmile/
        │   ├── LogmileApplication.java
        │   │
        │   ├── config/                                      # 설정 계층
        │   │   ├── SecurityConfig.java                      # Spring Security + JWT 필터 체인
        │   │   ├── JwtTokenProvider.java                    # JWT 생성/검증/파싱
        │   │   ├── JwtAuthenticationFilter.java             # 요청별 JWT 인증 필터
        │   │   └── SwaggerConfig.java                       # Springdoc OpenAPI 설정
        │   │
        │   ├── controller/                                  # Controller 계층 (요청/응답)
        │   │   ├── AuthController.java                      # POST /api/auth/login
        │   │   ├── VehicleController.java                   # /api/vehicles CRUD
        │   │   ├── DriverController.java                    # /api/drivers CRUD + 차량 배정
        │   │   ├── SimulationController.java                # POST /api/simulation/start|stop
        │   │   ├── GpsController.java                       # POST /api/gps
        │   │   ├── DriveLogController.java                  # GET /api/drive-logs (이력 목록/상세)
        │   │   ├── DashboardController.java                 # GET /api/dashboard/summary|vehicles
        │   │   ├── FatigueThresholdController.java          # GET|PUT /api/fatigue/thresholds
        │   │   └── FatigueStatsController.java              # GET /api/fatigue/stats
        │   │
        │   ├── service/                                     # Service 계층 (비즈니스 로직)
        │   │   ├── AdminService.java                        # 관리자 조회, 인증 처리
        │   │   ├── VehicleService.java                      # 차량 등록/조회/수정/삭제
        │   │   ├── DriverService.java                       # 운전자 등록/조회/수정/삭제/배정
        │   │   ├── SimulationService.java                   # drive_log 시작/중지 상태 관리
        │   │   ├── GpsReceiverService.java                  # GPS 저장 후 피로도 계산 트리거
        │   │   ├── RestEventService.java                    # speed≤3 판정, 휴식 시작/종료/분류
        │   │   ├── ContinuousDrivingService.java            # 마지막 유효 휴식 이후 운행 시간 계산
        │   │   ├── DailyDrivingService.java                 # 당일 drive_log 합산 운행 시간 계산
        │   │   ├── NightDrivingService.java                 # 22:00~06:00 야간 운행 시간 계산
        │   │   ├── FatigueScoreService.java                 # 항목 합산 → 등급 결정 (NORMAL/CAUTION/DANGER)
        │   │   ├── FatigueReasonService.java                # fatigue_event.reason 텍스트 생성
        │   │   ├── FatigueThresholdService.java             # 임계값 key/value 조회/수정
        │   │   ├── DriveLogService.java                     # 운행 이력 목록/상세 조회
        │   │   ├── DashboardService.java                    # 운행 중/주의/위험 차량 수, 평균 피로 점수
        │   │   └── FatigueStatsService.java                 # 일별 운행/야간/휴식 누락/평균 점수 통계
        │   │
        │   ├── repository/                                  # Repository 계층 (DB 접근)
        │   │   ├── AdminRepository.java
        │   │   ├── VehicleRepository.java
        │   │   ├── DriverRepository.java
        │   │   ├── DriveLogRepository.java
        │   │   ├── GpsDataRepository.java
        │   │   ├── RestEventRepository.java
        │   │   ├── FatigueEventRepository.java
        │   │   └── FatigueThresholdRepository.java
        │   │
        │   ├── entity/                                      # Entity 계층 (JPA 매핑)
        │   │   ├── Admin.java                               # admin 테이블
        │   │   ├── Vehicle.java                             # vehicle 테이블
        │   │   ├── Driver.java                              # driver 테이블
        │   │   ├── DriveLog.java                            # drive_log 테이블
        │   │   ├── GpsData.java                             # gps_data 테이블
        │   │   ├── RestEvent.java                           # rest_event 테이블
        │   │   ├── FatigueEvent.java                        # fatigue_event 테이블
        │   │   └── FatigueThreshold.java                    # fatigue_threshold 테이블
        │   │
        │   ├── dto/                                         # DTO 계층 (요청/응답 객체)
        │   │   ├── request/
        │   │   │   ├── LoginRequest.java                    # { email, password }
        │   │   │   ├── VehicleRequest.java                  # { plateNo, type }
        │   │   │   ├── DriverRequest.java                   # { name, phone, licenseType, vehicleId }
        │   │   │   ├── SimulationStartRequest.java          # { vehicleId, driverId, scenarioType, recognizedPlateNo, ocrConfidence, isManualInput }
        │   │   │   ├── GpsDataRequest.java                  # { driveLogId, latitude, longitude, speedKmh, recordedAt }
        │   │   │   └── ThresholdUpdateRequest.java          # { thresholdValue }
        │   │   └── response/
        │   │       ├── LoginResponse.java                   # { token, name, role }
        │   │       ├── VehicleResponse.java                 # { id, plateNo, type, driverName, isActive }
        │   │       ├── DriverResponse.java                  # { id, name, phone, licenseType, vehicleId, plateNo }
        │   │       ├── SimulationResponse.java              # { driveLogId, status, startedAt }
        │   │       ├── DriveLogResponse.java                # { id, vehicleId, driverId, 운행 요약 }
        │   │       ├── DriveLogDetailResponse.java          # { 운행 요약 + gpsData + restEvents + fatigueEvents }
        │   │       ├── DashboardSummaryResponse.java        # { runningCount, cautionCount, dangerCount, completedCount, avgFatigueScore }
        │   │       ├── VehicleFatigueResponse.java          # { vehicleId, plateNo, driverName, speedKmh, continuousDriving, fatigueScore, fatigueLevel, reason }
        │   │       ├── FatigueThresholdResponse.java        # { id, thresholdKey, thresholdValue, description }
        │   │       ├── FatigueStatsResponse.java            # { date, totalDrivingMinutes, nightDrivingMinutes, restViolationCount, avgFatigueScore }
        │   │       └── ApiResponse.java                     # 공통 응답 래퍼 { success, message, data }
        │   │
        │   └── common/                                      # 공통 계층
        │       ├── enums/
        │       │   ├── ScenarioType.java                    # A, B, C
        │       │   ├── DriveStatus.java                     # RUNNING, COMPLETED, STOPPED
        │       │   ├── RestType.java                        # PENDING, VALID, SUFFICIENT, INVALID
        │       │   └── FatigueLevel.java                    # NORMAL, CAUTION, DANGER
        │       └── exception/
        │           ├── GlobalExceptionHandler.java          # @RestControllerAdvice
        │           └── CustomException.java
        │
        └── resources/
            └── application.yml                              # server, spring.datasource, jpa, jwt 설정
```

### MVC 계층 흐름

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
    │   ├── yolo_service.py           # YOLOv8n 번호판 영역 탐지
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
YOLOv8n → 번호판 영역 bbox 탐지
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
    │   └── index.js                  # 라우트 정의 + 인증 가드
    │
    ├── stores/
    │   ├── authStore.js              # JWT 토큰, 로그인 상태
    │   ├── vehicleStore.js           # 차량 목록, 상태
    │   ├── dashboardStore.js         # 대시보드 요약, polling 관리
    │   └── simulationStore.js        # 시뮬레이션 진행 상태
    │
    ├── api/
    │   ├── axios.js                  # Axios 인스턴스, JWT 인터셉터
    │   ├── authApi.js                # POST /api/auth/login
    │   ├── vehicleApi.js             # /api/vehicles CRUD
    │   ├── driverApi.js              # /api/drivers CRUD
    │   ├── dashboardApi.js           # /api/dashboard/summary
    │   ├── simulationApi.js          # /api/simulation/start|stop
    │   ├── driveHistoryApi.js        # /api/drive-logs
    │   ├── fatigueStatsApi.js        # /api/fatigue/stats
    │   └── thresholdApi.js           # /api/fatigue/thresholds
    │
    ├── views/
    │   ├── LoginView.vue             # FR-AUTH01
    │   ├── DashboardView.vue         # FR-A01~A06
    │   ├── SimulationView.vue        # FR-B01, B04
    │   ├── VehicleView.vue           # FR-C01
    │   ├── DriverView.vue            # FR-C02
    │   ├── ThresholdView.vue         # FR-C03
    │   ├── DriveHistoryView.vue      # FR-D01
    │   ├── DriveHistoryDetailView.vue # FR-D02
    │   └── FatigueStatsView.vue      # FR-E01
    │
    └── components/
        ├── dashboard/
        │   ├── SummaryCard.vue       # 운행중/주의/위험/완료/평균 피로
        │   ├── VehicleTable.vue      # 차량 목록 테이블
        │   ├── FatigueBadge.vue      # 정상/주의/위험 배지
        │   └── VehicleDetailPanel.vue # 상세 패널, 타임라인
        ├── charts/
        │   ├── SpeedChart.vue        # 속도 변화 Chart.js
        │   ├── FatigueScoreChart.vue # 피로 점수 변화 Chart.js
        │   └── StatsChart.vue        # 일별 통계 Chart.js
        └── common/
            ├── NavBar.vue
            └── LoadingSpinner.vue
```

---

## 6. 인프라 (INFRA) 상세 구조

```
logmile_infra/
├── docker-compose.yml                # 전체 서비스 오케스트레이션
├── .env                              # 공통 환경변수
└── db/
    ├── init.sql                      # 테이블 DDL (8개 테이블)
    └── seed.sql                      # 초기 데이터 (관리자, 차량 10대, 운전자 10명, 임계값)
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
| GET | `/api/vehicles` | 차량 목록 조회 | JWT |
| POST | `/api/vehicles` | 차량 등록 | JWT |
| PUT | `/api/vehicles/{id}` | 차량 수정 | JWT |
| DELETE | `/api/vehicles/{id}` | 차량 삭제 | JWT |
| GET | `/api/drivers` | 운전자 목록 조회 | JWT |
| POST | `/api/drivers` | 운전자 등록 | JWT |
| PUT | `/api/drivers/{id}` | 운전자 수정/차량 배정 | JWT |
| DELETE | `/api/drivers/{id}` | 운전자 삭제 | JWT |
| POST | `/api/simulation/start` | 시뮬레이션 시작 (drive_log 생성) | JWT |
| POST | `/api/simulation/stop` | 시뮬레이션 중지 | JWT |
| POST | `/api/gps` | GPS 데이터 수신 + 피로도 재계산 | JWT |
| GET | `/api/dashboard/summary` | 통계 요약 카드 데이터 | JWT |
| GET | `/api/dashboard/vehicles` | 차량별 현재 피로도 상태 | JWT |
| GET | `/api/drive-logs` | 운행 이력 목록 | JWT |
| GET | `/api/drive-logs/{id}` | 운행 이력 상세 | JWT |
| GET | `/api/fatigue/stats` | 일별 피로도 통계 | JWT |
| GET | `/api/fatigue/thresholds` | 피로도 임계값 전체 조회 | JWT |
| PUT | `/api/fatigue/thresholds/{key}` | 임계값 수정 | JWT |
| POST | `/api/ocr/recognize` (AI) | 번호판 이미지 인식 | 없음(내부) |

---

## 8. 피로도 계산 흐름

```
GPS 데이터 수신 (POST /api/gps)
    ↓
GpsData 저장
    ↓
speed ≤ 3 km/h 판정
  ├── YES → 휴식 시작/진행 중 업데이트 (RestEvent)
  │         15분 이상 → VALID / 30분 이상 → SUFFICIENT
  └── NO  → 운행 중 처리
    ↓
피로도 재계산 (GPS 수신마다 또는 주기적)
  ├── 연속 운행 시간 계산 (마지막 유효 휴식 이후)
  ├── 일일 총 운행 시간 계산 (당일 drive_log 합산)
  ├── 야간 운행 시간 계산 (22:00~06:00 구간)
  └── 점수 합산 → 등급 결정 → reason 생성
    ↓
FatigueEvent 저장
    ↓
대시보드 polling (5초) → 최신 FatigueEvent 반환
```

---

## 9. DB 테이블 관계도 (ERD 요약)

```
admin
  (독립 테이블 — 관리자 인증용)

vehicle ──< drive_log >── driver
   │                         │
   └── driver_id (nullable)  └── vehicle_id (nullable)
   (상호 참조 — 배정 관계)

drive_log ──< gps_data       (CASCADE DELETE)
drive_log ──< rest_event     (CASCADE DELETE)
drive_log ──< fatigue_event  (CASCADE DELETE)

fatigue_threshold
  (독립 테이블 — key/value 설정값)
```
