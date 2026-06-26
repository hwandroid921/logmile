# logmile_be

화물차 운전자 피로도 모니터링 시스템의 Spring Boot REST API 서버

## 기술 스택

| 구분 | 버전 |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.14 |
| Spring Data JPA | - |
| Spring Security + JWT (jjwt) | 0.12.6 |
| springdoc-openapi | 2.7.0 |
| PostgreSQL Driver | - |
| Lombok | - |

## 실행 방법

### 로컬 실행

PostgreSQL이 먼저 실행되어 있어야 합니다.

```powershell
cd logmile_be
./gradlew bootRun
```

환경변수를 직접 지정할 경우:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/logmile"
$env:SPRING_DATASOURCE_USERNAME="logmile_user"
$env:SPRING_DATASOURCE_PASSWORD="your_password"
$env:JWT_SECRET="your_jwt_secret_min_32_chars"
./gradlew bootRun
```

### Docker 빌드

```powershell
docker build -t logmile-backend .
```

Docker Compose를 통한 실행은 `logmile_infra/` 참고.

## 환경변수

| 변수 | 기본값 | 설명 |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/logmile` | DB 접속 URL |
| `SPRING_DATASOURCE_USERNAME` | `logmile_user` | DB 사용자 |
| `SPRING_DATASOURCE_PASSWORD` | _(필수)_ | DB 비밀번호 |
| `JWT_SECRET` | _(필수)_ | JWT 서명 키 (32자 이상) |
| `JWT_EXPIRATION_MS` | `86400000` | 토큰 만료 시간 (ms, 기본 24시간) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | CORS 허용 Origin |

## API 문서

서버 실행 후 Swagger UI 접속:

```
http://localhost:8080/swagger-ui/index.html
```

## 도메인 구조

```
com.project.logmile/
├── common/
│   ├── config/          # SwaggerConfig
│   ├── enums/           # AdminRole, AdminStatus, FatigueLevel, DriveLogStatus 등
│   ├── exception/       # BusinessException, ErrorCode, GlobalExceptionHandler
│   └── security/        # SecurityConfig, JwtTokenProvider, JwtAuthenticationFilter, TenantAccessService
└── domain/
    ├── auth/            # 로그인, 회원가입 (POST /api/auth/login, /api/auth/signup)
    ├── admin/           # 관리자 승인 (Super Admin 전용)
    ├── company/         # 업체 관리 (Super Admin 전용)
    ├── vehicle/         # 차량 CRUD (GET/POST/PATCH/DELETE /api/vehicles)
    ├── driver/          # 운전자 CRUD + 차량 배정 (GET/POST/PATCH/DELETE /api/drivers)
    ├── drivelog/        # 운행 이력 조회, 시뮬레이션 시작/중지
    ├── gps/             # GPS 데이터 수신 (POST /api/gps)
    ├── fatigue/         # 피로도 점수 산정, 임계값 관리, 일별 통계
    ├── plateevent/      # 번호판 입출차 이벤트 기록
    ├── rest/            # 휴식 이벤트 판정 및 저장
    └── dashboard/       # 실시간 대시보드 요약 데이터
```

## 주요 API

| 경로 | 메서드 | 인증 | 설명 |
|---|---|---|---|
| `/api/auth/login` | POST | 없음 | 로그인, JWT 발급 |
| `/api/auth/signup` | POST | 없음 | 관리자 회원가입 |
| `/api/vehicles` | GET / POST | Admin | 차량 목록 / 등록 |
| `/api/vehicles/{id}` | PATCH / DELETE | Admin | 차량 수정 / 삭제 |
| `/api/drivers` | GET / POST | Admin | 운전자 목록 / 등록 |
| `/api/drivers/{id}` | PATCH / DELETE | Admin | 운전자 수정 / 삭제 |
| `/api/drivers/{id}/assign/{vehicleId}` | PATCH | Admin | 운전자-차량 배정 |
| `/api/drivers/{id}/unassign` | PATCH | Admin | 차량 배정 해제 |
| `/api/drive-history` | GET | Admin | 운행 이력 목록 |
| `/api/drive-history/{id}` | GET | Admin | 운행 이력 상세 |
| `/api/thresholds` | GET | Admin | 피로도 임계값 조회 |
| `/api/thresholds/{id}` | PATCH | Admin | 피로도 임계값 수정 |
| `/api/fatigue/stats` | GET | Admin | 피로도 일별 통계 |
| `/api/gps` | POST | Admin | GPS 데이터 수신 |
| `/api/simulation/start` | POST | Admin | 시뮬레이션 시작 |
| `/api/simulation/{id}/stop` | PATCH | Admin | 시뮬레이션 중지 |
| `/api/simulation/plate-events` | POST | Admin | 번호판 이벤트 수신 |
| `/api/dashboard` | GET | Admin | 대시보드 요약 |
| `/api/admin/approvals` | GET / PATCH | Super Admin | 관리자 승인 목록 / 처리 |
| `/api/companies` | GET / POST | Super Admin | 업체 관리 |

## 인증 방식

- 로그인 후 발급된 JWT를 `Authorization: Bearer {token}` 헤더로 전송
- `ROLE_ADMIN` — 소속 업체 데이터만 접근 (TenantAccessService로 격리)
- `ROLE_SUPER_ADMIN` — 전체 업체 데이터 접근, 관리자 승인·업체 관리 가능

## 피로도 점수 산정

GPS 데이터 수신 시 `FatigueScoreService`가 실시간으로 점수를 계산합니다.

| 등급 | 점수 |
|---|---:|
| 정상 | 0 ~ 39점 |
| 주의 | 40 ~ 69점 |
| 위험 | 70점 이상 |

산정 항목: 연속 운행 시간 + 일일 총 운행 시간 + 야간 운행 시간 + 휴식 부족 − 휴식 보정
