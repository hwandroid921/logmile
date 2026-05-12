# logmile 코딩 컨벤션 가이드 초안

## 화물차 운전자 피로도 실시간 모니터링 플랫폼

- 프로젝트명: `logmile`
- 문서 버전: `v1.0`
- 작성 기준일: 2026.04.29
- 적용 대상:
  - Backend: Java 21, Spring Boot 3.5.14, Spring Data JPA, Spring Security + JWT
  - Frontend: Vue.js 3, Vite, Pinia, Axios, Chart.js
  - AI/Python: Python 3.11, FastAPI, YOLO11n, EasyOCR
  - Database: PostgreSQL 16
  - Infra: Docker, Docker Compose
- 기준 산출물:
  - `logmile_프로젝트구조도.md`
  - `logmile_팀원별_기능구현_브랜치순서.md`
  - `logmile_DB설계서.md`

---

## 1. 공통 원칙

### 1.1 기본 규칙

- 코드 수정 시 기존 스타일을 우선 따른다.
- 한 파일 안에서는 들여쓰기, 줄바꿈, 네이밍 스타일을 일관되게 유지한다.
- 함수와 메서드는 하나의 책임을 중심으로 작성한다.
- 중복 로직이 2회 이상 반복되고 의미가 같다면 공통 함수나 Service로 분리한다.
- 복잡한 비즈니스 판단은 Controller나 View에 두지 않고 별도 계층으로 분리한다.
- 주석은 코드만으로 의도가 드러나지 않는 경우에만 짧게 작성한다.

### 1.2 네이밍 공통 규칙

| 대상 | 규칙 | 예시 |
|---|---|---|
| 패키지/디렉터리 | 소문자 | `controller`, `service`, `repository` |
| 클래스/컴포넌트 | PascalCase | `VehicleController`, `DashboardView` |
| 메서드/함수 | camelCase 또는 snake_case | Java: `calculateScore`, Python: `calculate_score` |
| 상수 | UPPER_SNAKE_CASE | `JWT_SECRET`, `LEVEL_DANGER_MIN` |
| DB 테이블/컬럼 | snake_case | `drive_log`, `fatigue_score` |
| API 경로 | kebab-case 또는 복수 명사 | `/api/drive-logs`, `/api/fatigue-thresholds` |

---

## 2. Backend Java / Spring Boot 컨벤션

### 2.1 패키지 구조

산출물 기준 기본 구조는 아래 계층형 구조를 따른다.

```text
com.project.logmile
├── config
├── controller
├── service
├── repository
├── entity
├── dto
│   ├── request
│   └── response
└── common
    ├── enums
    └── exception
```

관리자/업체 관리자 구조가 확정되면 `company`, `approval`, `auth` 패키지 분리를 검토한다.

### 2.2 클래스 네이밍

| 역할 | 네이밍 | 예시 |
|---|---|---|
| Controller | `{Domain}Controller` | `VehicleController` |
| Service | `{Domain}Service` | `FatigueScoreService` |
| Repository | `{Entity}Repository` | `DriveLogRepository` |
| Entity | 단수 명사 | `Vehicle`, `DriveLog` |
| Request DTO | `{Action}Request` 또는 `{Domain}Request` | `LoginRequest`, `GpsDataRequest` |
| Response DTO | `{Action}Response` 또는 `{Domain}Response` | `DashboardSummaryResponse` |
| Enum | 명사형 | `DriveStatus`, `FatigueLevel` |

### 2.3 Entity 규칙

- 테이블명과 컬럼명은 DB 설계서의 snake_case를 따른다.
- Entity 필드는 Java camelCase를 사용한다.
- Entity에는 비즈니스 로직을 과도하게 넣지 않는다.
- 연관관계는 필요 최소한으로 설정하고, 조회 성능과 순환 참조를 고려한다.
- `createdAt`, `updatedAt` 등 공통 시간 컬럼은 일관되게 사용한다.

예시:

```java
@Column(name = "fatigue_score", nullable = false)
private Integer fatigueScore;
```

### 2.4 Controller 규칙

- API 경로는 `/api/{resource}` 형식을 사용한다.
- Controller는 요청 검증, DTO 변환, 응답 반환에 집중한다.
- 비즈니스 로직은 Service로 위임한다.
- 응답은 가능하면 공통 응답 형식을 사용한다.

예시:

```text
GET    /api/vehicles
POST   /api/vehicles
PUT    /api/vehicles/{id}
DELETE /api/vehicles/{id}
```

### 2.5 Service 규칙

- 트랜잭션 경계는 Service 계층에서 관리한다.
- 피로도 계산처럼 복잡한 로직은 목적별 Service로 분리한다.
- 예외는 의미 있는 커스텀 예외 또는 표준 예외로 명확하게 던진다.

피로도 관련 Service 예시:

```text
RestEventService
ContinuousDrivingService
DailyDrivingService
NightDrivingService
FatigueScoreService
FatigueReasonService
```

### 2.6 DTO 규칙

- Entity를 API 응답으로 직접 반환하지 않는다.
- Request/Response DTO를 분리한다.
- 프론트엔드와 주고받는 JSON 필드는 camelCase를 사용한다.

예시:

```json
{
  "driveLogId": 1,
  "speedKmh": 72.5,
  "recordedAt": "2026-04-29T22:30:00"
}
```

---

## 3. Frontend Vue 컨벤션

### 3.1 디렉터리 구조

```text
src/
├── api
├── components
│   ├── common
│   ├── dashboard
│   └── charts
├── router
├── stores
└── views
```

### 3.2 파일 네이밍

| 대상 | 규칙 | 예시 |
|---|---|---|
| View | PascalCase + `View.vue` | `DashboardView.vue` |
| Component | PascalCase | `VehicleTable.vue` |
| Store | camelCase + `Store.js` | `authStore.js` |
| API 모듈 | camelCase + `Api.js` | `vehicleApi.js` |

### 3.3 Vue 작성 규칙

- Vue 3 Composition API 사용을 기본으로 한다.
- 화면 단위는 `views`, 재사용 UI는 `components`에 둔다.
- API 호출은 컴포넌트에 직접 작성하지 않고 `api` 모듈 또는 Pinia store를 통해 처리한다.
- 로그인/JWT 상태는 `authStore`에서 관리한다.
- 대시보드 polling은 store에서 시작/중지 흐름을 명확히 관리한다.

### 3.4 Pinia Store 규칙

- Store는 도메인 단위로 분리한다.
- 상태 이름은 camelCase를 사용한다.
- API 호출 action은 동사로 시작한다.

예시:

```text
authStore
vehicleStore
dashboardStore
simulationStore
```

### 3.5 Axios 규칙

- 공통 Axios 인스턴스를 사용한다.
- JWT는 request interceptor에서 `Authorization` 헤더에 추가한다.
- API URL은 `.env`의 `VITE_*` 환경변수로 관리한다.

예시:

```text
Authorization: Bearer {accessToken}
```

---

## 4. Python / FastAPI / Simulator 컨벤션

### 4.1 공통 Python 규칙

- Python 3.11 기준으로 작성한다.
- 파일명과 함수명은 snake_case를 사용한다.
- 클래스명은 PascalCase를 사용한다.
- 설정값은 `.env`와 config 모듈을 통해 읽는다.
- 실행 진입점은 명확하게 둔다.

### 4.2 FastAPI AI 서버 구조

```text
logmile_ai/
└── app/
    ├── main.py
    ├── core
    ├── router
    ├── service
    ├── schema
    └── model
```

### 4.3 AI 서버 규칙

- 라우터는 요청/응답 처리에 집중한다.
- YOLO 탐지, OCR 인식, 번호판 후처리는 Service로 분리한다.
- OCR 신뢰도 기준은 환경변수 또는 config에서 관리한다.
- 인식 신뢰도 0.85 미만이면 수동 입력 필요 응답을 반환한다.

응답 예시:

```json
{
  "plateNo": "12가3456",
  "confidence": 0.92,
  "isManualRequired": false
}
```

### 4.4 GPS 시뮬레이터 구조

```text
logmile_sim/
└── simulator/
    ├── main.py
    ├── config.py
    ├── sender.py
    ├── patterns
    └── scenarios
```

### 4.5 GPS 시뮬레이터 규칙

- 시나리오는 A/B/C로 분리한다.
- 휴식 패턴과 야간 패턴은 재사용 가능한 pattern 모듈로 분리한다.
- 백엔드 전송 JSON은 `GpsDataRequest` 형식과 일치시킨다.
- dry-run 모드를 제공해 백엔드 없이 데이터 생성 확인이 가능하게 한다.

---

## 5. Database / SQL 컨벤션

### 5.1 네이밍

| 대상 | 규칙 | 예시 |
|---|---|---|
| 테이블 | snake_case, 단수 또는 도메인 명사 | `drive_log`, `gps_data` |
| 컬럼 | snake_case | `recorded_at`, `fatigue_score` |
| PK | `id` | `id BIGSERIAL PRIMARY KEY` |
| FK | `{table}_id` | `drive_log_id` |
| 인덱스 | `idx_{table}_{column}` | `idx_gps_data_recorded_at` |
| 제약조건 | `fk_`, `uk_`, `chk_` 접두사 | `fk_drive_log_vehicle` |

### 5.2 SQL 작성 규칙

- PostgreSQL 16 기준으로 작성한다.
- DDL과 seed는 `logmile_infra/db`에 둔다.
- 날짜/시간 컬럼은 프로젝트 기준에 맞춰 일관되게 사용한다.
- 상태값은 가능하면 `CHECK` 제약 또는 enum으로 관리한다.

### 5.3 기본 상태값

| 도메인 | 값 |
|---|---|
| 운행 상태 | `RUNNING`, `COMPLETED`, `STOPPED` |
| 휴식 유형 | `PENDING`, `VALID`, `SUFFICIENT`, `INVALID` |
| 피로 등급 | `NORMAL`, `CAUTION`, `DANGER` |
| 시나리오 | `A`, `B`, `C` |

---

## 6. API 컨벤션

### 6.1 경로 규칙

- API 경로는 `/api`로 시작한다.
- 리소스는 복수형을 기본으로 한다.
- 동작보다 자원을 우선 표현한다.

예시:

```text
GET /api/vehicles
GET /api/drive-logs/{id}
POST /api/gps
GET /api/fatigue/thresholds
```

### 6.2 요청/응답 규칙

- JSON 필드는 camelCase를 사용한다.
- 시간 값은 ISO-8601 문자열을 사용한다.
- 실패 응답은 원인을 알 수 있는 메시지를 포함한다.
- 인증이 필요한 API는 JWT를 사용한다.

---

## 7. 테스트 컨벤션

### 7.1 Backend

- 피로도 계산, 휴식 판단, 인증/인가, GPS 수신은 우선 테스트 대상으로 둔다.
- Service 단위 테스트와 Controller API 테스트를 분리한다.
- 테스트명은 기대 동작을 드러내게 작성한다.

예시:

```java
@Test
void 장시간_연속운행이면_위험등급을_반환한다() {
}
```

### 7.2 Frontend

- 로그인, 라우터 가드, 대시보드 polling, 시뮬레이션 실행 흐름을 우선 검증한다.
- API 모듈과 store action은 모킹 가능한 구조로 작성한다.

### 7.3 Python

- 시나리오별 GPS 데이터 생성 개수와 시간 범위를 검증한다.
- OCR 후처리 함수는 정규식/공백/신뢰도 기준을 테스트한다.

---

## 8. 추후 확정 필요 사항

- Java formatter 또는 Checkstyle 적용 여부
- Frontend ESLint/Prettier 적용 여부
- Python formatter로 Black/Ruff를 사용할지 여부
- 패키지명을 `com.project.logmile`로 유지할지 `com.logmile`로 변경할지 여부
- 최상위 관리자/업체 관리자 구조 확정 후 패키지와 DB 컨벤션 보완
- 업체별 피로도 임계값을 공통으로 둘지, `company_id` 기준으로 분리할지 여부
