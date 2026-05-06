# 테이블 정의서

## logmile - 화물차 운전자 피로도 실시간 모니터링 플랫폼

- 프로젝트명: `logmile`
- 기준 원본: `docx/logmile_테이블정의서.docx`
- 버전 표기: `원본 docx에 별도 버전 표기 없음`
- 작성 기준일: `2026.04`
- 비고: 이 문서는 `docx/logmile_테이블정의서.docx` 내용을 Markdown 기준으로 정리한 문서다.

## 1. 목차

1. `company`
2. `admin`
3. `vehicle`
4. `driver`
5. `drive_log`
6. `plate_recognition_event`
7. `gps_data`
8. `rest_event`
9. `fatigue_event`
10. `fatigue_threshold`

## 2. company (운수 업체)

- 설명: 운수 업체 정보. `v5.0 신규 추가` 표기. `admin / vehicle / driver / drive_log` 에서 FK 참조
- 인덱스: `PK(id)`, `UK(name)`

| # | 컬럼명 | 데이터 타입 | NULL | 기본값 | 제약조건 | 설명 |
|---|---|---|---|---|---|---|
| 1 | `id` | `BIGSERIAL` | NOT NULL | `auto` | PK | 업체 식별자 |
| 2 | `name` | `VARCHAR(100)` | NOT NULL | - | UK | 업체명 |
| 3 | `address` | `VARCHAR(255)` | NULL | - | - | 업체 주소 |
| 4 | `phone` | `VARCHAR(20)` | NULL | - | - | 업체 연락처 |
| 5 | `is_active` | `BOOLEAN` | NOT NULL | `TRUE` | - | 업체 활성 여부 |
| 6 | `created_at` | `TIMESTAMP` | NOT NULL | `NOW()` | - | 등록 시각 |

## 3. admin (관리자 계정)

- 설명: 시스템 관리자 계정 및 권한. JWT 인증에 사용. `ROLE_SUPER_ADMIN` / `ROLE_ADMIN` 구조
- 인덱스: `PK(id)`, `UK(email)`
- CHECK: `role IN (ROLE_SUPER_ADMIN, ROLE_ADMIN)` / `status IN (PENDING, ACTIVE, INACTIVE, REJECTED, SUSPENDED)`

| # | 컬럼명 | 데이터 타입 | NULL | 기본값 | 제약조건 | 설명 |
|---|---|---|---|---|---|---|
| 1 | `id` | `BIGSERIAL` | NOT NULL | `auto` | PK | 관리자 식별자 |
| 2 | `company_id` | `BIGINT` | NULL | - | `FK → company.id` | 소속 업체 ID |
| 3 | `email` | `VARCHAR(100)` | NOT NULL | - | UK | 로그인 이메일 |
| 4 | `password` | `VARCHAR(255)` | NOT NULL | - | - | BCrypt 암호화 패스워드 |
| 5 | `name` | `VARCHAR(50)` | NOT NULL | - | - | 관리자 이름 |
| 6 | `phone` | `VARCHAR(20)` | NULL | - | - | 관리자 연락처 |
| 7 | `role` | `VARCHAR(20)` | NOT NULL | `ROLE_ADMIN` | CHK | 권한 |
| 8 | `status` | `VARCHAR(20)` | NOT NULL | `ACTIVE` | CHK | 계정 상태 |
| 9 | `created_at` | `TIMESTAMP` | NOT NULL | `NOW()` | - | 계정 생성 시각 |

## 4. vehicle (차량)

- 설명: 가상 물류센터 화물차 정보. 운전자와 1:1 배정 관계
- 인덱스: `PK(id)`, `UK(plate_no)`
- 비고: `driver_id` FK는 `ALTER TABLE`로 후행 추가, `UNIQUE`로 1:1 보장

| # | 컬럼명 | 데이터 타입 | NULL | 기본값 | 제약조건 | 설명 |
|---|---|---|---|---|---|---|
| 1 | `id` | `BIGSERIAL` | NOT NULL | `auto` | PK | 차량 식별자 |
| 2 | `company_id` | `BIGINT` | NULL | - | `FK → company.id` | 소속 업체 ID |
| 3 | `plate_no` | `VARCHAR(20)` | NOT NULL | - | UK | 차량 번호판 |
| 4 | `type` | `VARCHAR(50)` | NOT NULL | - | - | 차종 |
| 5 | `driver_id` | `BIGINT` | NULL | - | `FK/UQ → driver.id` | 현재 배정 운전자 ID |
| 6 | `is_active` | `BOOLEAN` | NOT NULL | `TRUE` | - | 차량 활성 여부 |
| 7 | `created_at` | `TIMESTAMP` | NOT NULL | `NOW()` | - | 차량 등록 시각 |

## 5. driver (운전자)

- 설명: 화물차 운전자 기본 정보 및 차량 배정 상태
- 인덱스: `PK(id)`
- 비고: `vehicle_id UNIQUE`로 1:1 배정 보장

| # | 컬럼명 | 데이터 타입 | NULL | 기본값 | 제약조건 | 설명 |
|---|---|---|---|---|---|---|
| 1 | `id` | `BIGSERIAL` | NOT NULL | `auto` | PK | 운전자 식별자 |
| 2 | `company_id` | `BIGINT` | NULL | - | `FK → company.id` | 소속 업체 ID |
| 3 | `name` | `VARCHAR(50)` | NOT NULL | - | - | 운전자 이름 |
| 4 | `phone` | `VARCHAR(20)` | NOT NULL | - | - | 연락처 |
| 5 | `license_type` | `VARCHAR(30)` | NOT NULL | - | - | 면허 종류 |
| 6 | `vehicle_id` | `BIGINT` | NULL | - | `FK/UQ → vehicle.id` | 배정 차량 ID |
| 7 | `created_at` | `TIMESTAMP` | NOT NULL | `NOW()` | - | 운전자 등록 시각 |

## 6. drive_log (운행 기록)

- 설명: 운행 시작/종료, 시나리오 유형, OCR 번호판 인식 결과, 운행 완료 요약 저장
- 인덱스: `PK(id)`, `idx_drive_log_company_id`, `idx_drive_log_vehicle_id`, `idx_drive_log_driver_id`, `idx_drive_log_started_at`
- CHECK: `scenario_type IN (A,B,C)` / `status IN (RUNNING,COMPLETED,STOPPED)` / `ocr_confidence 0.0~1.0` / `max_fatigue_level IN (NORMAL,CAUTION,DANGER)`

| # | 컬럼명 | 데이터 타입 | NULL | 기본값 | 제약조건 | 설명 |
|---|---|---|---|---|---|---|
| 1 | `id` | `BIGSERIAL` | NOT NULL | `auto` | PK | 운행 기록 식별자 |
| 2 | `company_id` | `BIGINT` | NULL | - | `FK → company.id` | 소속 업체 ID |
| 3 | `vehicle_id` | `BIGINT` | NOT NULL | - | `FK → vehicle.id` | 운행 차량 ID |
| 4 | `driver_id` | `BIGINT` | NOT NULL | - | `FK → driver.id` | 운행 운전자 ID |
| 5 | `started_at` | `TIMESTAMP` | NOT NULL | - | - | 운행 시작 시각 |
| 6 | `ended_at` | `TIMESTAMP` | NULL | - | - | 운행 종료 시각 |
| 7 | `scenario_type` | `VARCHAR(10)` | NOT NULL | - | `CHK(A,B,C)` | 시나리오 |
| 8 | `status` | `VARCHAR(20)` | NOT NULL | `RUNNING` | CHK | 운행 상태 |
| 9 | `recognized_plate_no` | `VARCHAR(20)` | NULL | - | - | OCR 인식 번호판 |
| 10 | `ocr_confidence` | `DOUBLE PRECISION` | NULL | - | `CHK(0.0~1.0)` | OCR 신뢰도 |
| 11 | `is_manual_input` | `BOOLEAN` | NOT NULL | `FALSE` | - | 수동 입력 여부 |
| 12 | `total_driving_minutes` | `INTEGER` | NULL | - | - | 총 운행 시간(분) |
| 13 | `night_driving_minutes` | `INTEGER` | NULL | - | - | 야간 운행 시간(분) |
| 14 | `total_rest_count` | `INTEGER` | NULL | - | - | 유효 휴식 총 횟수 |
| 15 | `max_fatigue_score` | `INTEGER` | NULL | - | - | 최고 피로 점수 |
| 16 | `max_fatigue_level` | `VARCHAR(20)` | NULL | - | CHK | 최고 피로 등급 |
| 17 | `created_at` | `TIMESTAMP` | NOT NULL | `NOW()` | - | 레코드 생성 시각 |

## 7. plate_recognition_event (번호판 관측 이벤트)

- 설명: 출발/도착 번호판 검증, 고속도로 관측, 휴게소 진입/진출 번호판 인식 결과를 운행 기록과 연결하여 저장
- 인덱스: `PK(id)`, `idx_plate_event_drive_log_id`, `idx_plate_event_vehicle_id`, `idx_plate_event_captured_at`, `idx_plate_event_location_type`
- CHECK: `confidence 0.0~1.0` / `source_type` / `location_type`
- 비고: 실제 CCTV/RTSP 실시간 스트리밍은 제외하고 샘플 이미지/영상 프레임 기반 이벤트 저장을 우선 범위로 한다.

| # | 컬럼명 | 데이터 타입 | NULL | 기본값 | 제약조건 | 설명 |
|---|---|---|---|---|---|---|
| 1 | `id` | `BIGSERIAL` | NOT NULL | `auto` | PK | 번호판 관측 이벤트 식별자 |
| 2 | `drive_log_id` | `BIGINT` | NOT NULL | - | `FK CASCADE → drive_log.id` | 연결된 운행 기록 ID |
| 3 | `vehicle_id` | `BIGINT` | NULL | - | `FK SET NULL → vehicle.id` | 매칭된 차량 ID |
| 4 | `recognized_plate_no` | `VARCHAR(20)` | NOT NULL | - | - | OCR 인식 번호판 |
| 5 | `expected_plate_no` | `VARCHAR(20)` | NULL | - | - | 운행/차량 기준 기대 번호판 |
| 6 | `confidence` | `DOUBLE PRECISION` | NULL | - | `CHK(0.0~1.0)` | 번호판 인식 신뢰도 |
| 7 | `source_type` | `VARCHAR(30)` | NOT NULL | - | CHK | 입력 소스 (`DEPARTURE`, `ARRIVAL`, `HIGHWAY_CCTV`, `REST_AREA_CCTV`) |
| 8 | `location_type` | `VARCHAR(30)` | NOT NULL | - | CHK | 관측 위치 유형 (`DEPOT`, `HIGHWAY`, `REST_AREA_ENTRY`, `REST_AREA_EXIT`) |
| 9 | `location_name` | `VARCHAR(100)` | NULL | - | - | 관측 지점명 |
| 10 | `latitude` | `DOUBLE PRECISION` | NULL | - | - | 관측 지점 위도 |
| 11 | `longitude` | `DOUBLE PRECISION` | NULL | - | - | 관측 지점 경도 |
| 12 | `captured_at` | `TIMESTAMP` | NOT NULL | - | - | 번호판 이미지/프레임 관측 시각 |
| 13 | `matched` | `BOOLEAN` | NOT NULL | `FALSE` | - | 기대 번호판과 일치 여부 |
| 14 | `image_path` | `VARCHAR(255)` | NULL | - | - | 샘플 이미지 또는 저장 이미지 경로 |
| 15 | `created_at` | `TIMESTAMP` | NOT NULL | `NOW()` | - | 이벤트 저장 시각 |

### 7.1 source_type 값 정의

| 값 | 설명 |
|---|---|
| `DEPARTURE` | 운행 시작 시 번호판 인식 |
| `ARRIVAL` | 운행 종료 시 번호판 인식 |
| `HIGHWAY_CCTV` | 고속도로 관측 지점 번호판 인식 |
| `REST_AREA_CCTV` | 휴게소 진입/진출 지점 번호판 인식 |

### 7.2 location_type 값 정의

| 값 | 설명 |
|---|---|
| `DEPOT` | 출발지 또는 도착지 |
| `HIGHWAY` | 고속도로 관측 지점 |
| `REST_AREA_ENTRY` | 휴게소 진입 지점 |
| `REST_AREA_EXIT` | 휴게소 진출 지점 |

## 8. gps_data (GPS 데이터)

- 설명: 시뮬레이터가 전송하는 GPS 좌표 및 속도. 피로도 계산의 원천 데이터
- 인덱스: `PK(id)`, `idx_gps_data_drive_log_id`, `idx_gps_data_recorded_at`
- 보존 정책: `30일 후 삭제`

| # | 컬럼명 | 데이터 타입 | NULL | 기본값 | 제약조건 | 설명 |
|---|---|---|---|---|---|---|
| 1 | `id` | `BIGSERIAL` | NOT NULL | `auto` | PK | GPS 데이터 식별자 |
| 2 | `drive_log_id` | `BIGINT` | NOT NULL | - | `FK CASCADE` | 소속 운행 기록 ID |
| 3 | `latitude` | `DOUBLE PRECISION` | NOT NULL | - | - | 위도 |
| 4 | `longitude` | `DOUBLE PRECISION` | NOT NULL | - | - | 경도 |
| 5 | `speed_kmh` | `DOUBLE PRECISION` | NOT NULL | `0.0` | - | 차량 속도 |
| 6 | `recorded_at` | `TIMESTAMP` | NOT NULL | - | - | GPS 기록 시각 |

## 9. rest_event (휴식 이벤트)

- 설명: `speed_kmh ≤ 3` 조건 감지 시 생성. 종료 후 `rest_type`, `rest_minutes` 확정
- 인덱스: `PK(id)`, `idx_rest_event_drive_log_id`
- CHECK: `rest_type IN (VALID, SUFFICIENT, INVALID, PENDING)`

| # | 컬럼명 | 데이터 타입 | NULL | 기본값 | 제약조건 | 설명 |
|---|---|---|---|---|---|---|
| 1 | `id` | `BIGSERIAL` | NOT NULL | `auto` | PK | 휴식 이벤트 식별자 |
| 2 | `drive_log_id` | `BIGINT` | NOT NULL | - | `FK CASCADE` | 소속 운행 기록 ID |
| 3 | `rest_started_at` | `TIMESTAMP` | NOT NULL | - | - | 휴식 시작 시각 |
| 4 | `rest_ended_at` | `TIMESTAMP` | NULL | - | - | 휴식 종료 시각 |
| 5 | `rest_minutes` | `INTEGER` | NULL | - | - | 실제 휴식 시간(분) |
| 6 | `rest_type` | `VARCHAR(20)` | NOT NULL | `PENDING` | CHK | 휴식 유형 |

### 9.1 rest_type 값 정의

| 값 | 조건 | 설명 |
|---|---|---|
| `PENDING` | 진행 중 | 휴식 중인 상태 |
| `VALID` | 15분 이상 | 유효 휴식, 피로도 보정 `-10점` |
| `SUFFICIENT` | 30분 이상 | 충분 휴식, 피로도 보정 `-20점` |
| `INVALID` | 15분 미만 | 휴식 미달 |

## 10. fatigue_event (피로도 이벤트)

- 설명: GPS 데이터 수신 시마다 피로도를 재산정하여 기록. 대시보드 실시간 표시 및 운행 이력 조회에 사용
- 인덱스: `PK(id)`, `idx_fatigue_event_drive_log_id`, `idx_fatigue_event_occurred_at`
- CHECK: `fatigue_level IN (NORMAL, CAUTION, DANGER)` / `fatigue_score ≥ 0`

| # | 컬럼명 | 데이터 타입 | NULL | 기본값 | 제약조건 | 설명 |
|---|---|---|---|---|---|---|
| 1 | `id` | `BIGSERIAL` | NOT NULL | `auto` | PK | 피로도 이벤트 식별자 |
| 2 | `drive_log_id` | `BIGINT` | NOT NULL | - | `FK CASCADE` | 소속 운행 기록 ID |
| 3 | `fatigue_score` | `INTEGER` | NOT NULL | `0` | `CHK(≥0)` | 피로도 총점 |
| 4 | `fatigue_level` | `VARCHAR(20)` | NOT NULL | `NORMAL` | CHK | 피로 등급 |
| 5 | `continuous_driving_minutes` | `INTEGER` | NOT NULL | `0` | - | 연속 운행 시간 |
| 6 | `daily_total_driving_minutes` | `INTEGER` | NOT NULL | `0` | - | 당일 총 운행 시간 |
| 7 | `night_driving_minutes` | `INTEGER` | NOT NULL | `0` | - | 야간 운행 누적 시간 |
| 8 | `rest_count` | `INTEGER` | NOT NULL | `0` | - | 유효 휴식 횟수 |
| 9 | `rest_violation_count` | `INTEGER` | NOT NULL | `0` | - | 휴식 누락 횟수 |
| 10 | `reason` | `TEXT` | NULL | - | - | 점수 산정 근거 텍스트 |
| 11 | `occurred_at` | `TIMESTAMP` | NOT NULL | `NOW()` | - | 피로도 산정 시각 |

### 10.1 피로 등급 기준

| 등급 | 점수 범위 | 조치 |
|---|---|---|
| `NORMAL` | 0 ~ 39 | 정상 배지 표시 |
| `CAUTION` | 40 ~ 69 | 주의 배지 표시 |
| `DANGER` | 70 이상 | 위험 배지 + 전화 권고 |

## 11. fatigue_threshold (피로도 임계값)

- 설명: 피로도 점수 모델의 기준값을 `key/value`로 관리. 관리자 화면에서 동적 수정 가능
- 인덱스: `PK(id)`, `UK(threshold_key)`

| # | 컬럼명 | 데이터 타입 | NULL | 기본값 | 제약조건 | 설명 |
|---|---|---|---|---|---|---|
| 1 | `id` | `BIGSERIAL` | NOT NULL | `auto` | PK | 임계값 식별자 |
| 2 | `threshold_key` | `VARCHAR(100)` | NOT NULL | - | UK | 임계값 식별 키 |
| 3 | `threshold_value` | `DOUBLE PRECISION` | NOT NULL | - | - | 임계값 |
| 4 | `description` | `VARCHAR(255)` | NULL | - | - | 임계값 설명 |
| 5 | `updated_at` | `TIMESTAMP` | NOT NULL | `NOW()` | - | 최종 수정 시각 |

### 11.1 기본 시드 데이터

| threshold_key | 값 | 설명 |
|---|---:|---|
| `CONTINUOUS_DRIVING_90` | 90 | 연속 운행 90분 이상 → +10점 |
| `CONTINUOUS_DRIVING_120` | 120 | 연속 운행 120분 이상 → +25점 |
| `CONTINUOUS_DRIVING_180` | 180 | 연속 운행 180분 이상 → +45점 |
| `CONTINUOUS_DRIVING_240` | 240 | 연속 운행 240분 이상 → +65점 |
| `DAILY_DRIVING_360` | 360 | 일일 총 운행 6시간 이상 → +15점 |
| `DAILY_DRIVING_480` | 480 | 일일 총 운행 8시간 이상 → +30점 |
| `DAILY_DRIVING_600` | 600 | 일일 총 운행 10시간 이상 → +45점 |
| `NIGHT_DRIVING_30` | 30 | 야간 운행 누적 30분 이상 → +10점 |
| `NIGHT_DRIVING_60` | 60 | 야간 운행 누적 1시간 이상 → +20점 |
| `NIGHT_DRIVING_120` | 120 | 야간 운행 누적 2시간 이상 → +35점 |
| `REST_VALID_MINUTES` | 15 | 유효 휴식 기준(분) |
| `REST_SUFFICIENT_MINUTES` | 30 | 충분 휴식 기준(분) |
| `REST_REQUIRED_AFTER` | 120 | 2시간 운행 후 휴식 필요 |
| `REST_VIOLATION_ONCE_SCORE` | 10 | 휴식 누락 1회 → +10점 |
| `REST_VIOLATION_TWICE_SCORE` | 25 | 휴식 누락 2회 이상 → +25점 |
| `REST_CORRECTION_VALID_SCORE` | 10 | 유효 휴식 보정 → -10점 |
| `REST_CORRECTION_SUFFICIENT_SCORE` | 20 | 충분 휴식 보정 → -20점 |
| `LEVEL_NORMAL_MAX` | 39 | 정상 등급 최대 점수 |
| `LEVEL_CAUTION_MIN` | 40 | 주의 등급 최소 점수 |
| `LEVEL_CAUTION_MAX` | 69 | 주의 등급 최대 점수 |
| `LEVEL_DANGER_MIN` | 70 | 위험 등급 최소 점수 |
