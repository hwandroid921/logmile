# ERD

## logmile - 화물차 운전자 피로도 실시간 모니터링 플랫폼

- 프로젝트명: `logmile`
- 문서 버전: `v1.5`
- 기준 문서: `logmile_DB설계서.md`
- 참조 원본: `docx/logmile_ERD.docx`
- 작성 기준일: `2026.04`
- 버전 관리 기준: `md` 우선 관리, `docx`는 제출 및 보관용
- 비고: 이 문서는 ERD 내용을 Markdown 기준으로 정리한 문서이며, 다이어그램 이미지는 보관용 docx를 참조한다.

## 1. 문서 개요

| 항목 | 내용 |
|---|---|
| 문서명 | ERD |
| 프로젝트 | logmile |
| 설명 | 화물차 운전자 피로도 실시간 모니터링 플랫폼의 논리/물리 ERD 정리 |
| 원본 표기 | 에스트래픽 / 스마트 모빌리티 DX Academy |

## 2. 목차

1. 논리 ERD
2. 물리 ERD

## 3. 논리 ERD

### 3.1 엔티티 목록

| 엔티티 | 테이블명 | 설명 |
|---|---|---|
| 관리자 | `admin` | 시스템 관리자 계정 및 권한 |
| 운수 업체 | `company` | 업체 정보 (`v5.0 신규` 표기) |
| 차량 | `vehicle` | 가상 물류센터 화물차 정보 |
| 운전자 | `driver` | 화물차 운전자 기본 정보 및 차량 배정 |
| 운행 기록 | `drive_log` | 운행 시작/종료, 시나리오, OCR 인식 결과, 운행 요약 |
| 번호판 관측 이벤트 | `plate_recognition_event` | 출발/도착, 고속도로 관측, 휴게소 진입/진출 번호판 인식 결과 |
| GPS 데이터 | `gps_data` | 운행 중 수집된 GPS 좌표 및 속도 |
| 휴식 이벤트 | `rest_event` | 운행 중 감지된 휴식 시작/종료 및 유형 |
| 피로도 이벤트 | `fatigue_event` | GPS 수신 시마다 산정된 피로도 점수 및 판단 근거 |
| 피로도 임계값 | `fatigue_threshold` | 피로도 점수 모델의 기준값 (`key/value`) |

### 3.2 엔티티 관계도

- 원본 docx의 `[그림 1] logmile 논리 ERD - 엔티티 관계도` 참조

### 3.3 관계 상세 설명

| 관계 | 유형 | 설명 |
|---|---|---|
| `vehicle ↔ driver` | 1:1 (양방향) | `driver.vehicle_id(FK/UQ)` + `vehicle.driver_id(FK/UQ)`, `ALTER TABLE` 후행 추가 |
| `company → admin` | 1:N | 업체 1개에 관리자 여러 명, `SUPER_ADMIN`은 `company_id = NULL` |
| `company → vehicle` | 1:N | 업체 1개에 차량 여러 대 |
| `company → driver` | 1:N | 업체 1개에 운전자 여러 명 |
| `company → drive_log` | 1:N | 운행 기록에 업체 정보 포함 |
| `vehicle → drive_log` | 1:N | 차량 1대가 여러 번 운행 가능 |
| `driver → drive_log` | 1:N | 운전자 1명이 여러 운행 수행 가능 |
| `vehicle → plate_recognition_event` | 1:N | 차량 1대가 여러 번호판 관측 이벤트를 가질 수 있음 |
| `drive_log → plate_recognition_event` | 1:N | 운행 1건에 출발/도착, 고속도로, 휴게소 진입/진출 관측 이벤트 포함 |
| `drive_log → gps_data` | 1:N | 운행 1건에 다수의 GPS 데이터 포함 |
| `drive_log → rest_event` | 1:N | 운행 1건에 다수의 휴식 이벤트 발생 |
| `drive_log → fatigue_event` | 1:N | 운행 중 GPS 수신마다 피로도 재산정 기록 |
| `fatigue_threshold` | 독립 | 다른 테이블과 FK 없음, 피로도 계산 로직에서 key로 조회 |
| `admin` | 독립 | 관리자 인증 전용, 운행/차량 데이터와 FK 없음 |

### 3.4 주요 도메인 규칙

| 규칙 | 내용 |
|---|---|
| 휴식 판단 | `gps_data.speed_kmh ≤ 3` 상태가 15분 이상 지속 시 `rest_event` 생성, `rest_type = VALID` |
| 충분 휴식 | 휴식 지속 30분 이상 시 `rest_type = SUFFICIENT` |
| 휴식 미달 | 15분 미만 정차 종료 시 `rest_type = INVALID` |
| 야간 운행 | `22:00 ~ 06:00` 구간의 GPS 기록 시각 기준으로 누적 계산 |
| 피로도 등급 | `NORMAL (0~39) / CAUTION (40~69) / DANGER (70 이상)` |
| OCR 신뢰도 | `ocr_confidence < 0.85` 시 `is_manual_input = TRUE` |
| 출발/도착 번호판 검증 | 출발 시 인식 번호판과 도착 시 인식 번호판을 비교하여 운행 차량 일치 여부 확인 |
| 고속도로 관측 | 고속도로 관측 이벤트를 연속 운행 시간 판단의 보조 근거로 활용 |
| 휴게소 진입/진출 | 휴게소 진입/진출 인식 시각 차이를 휴식 여부와 휴식 시간 보조 검증에 활용 |
| 운행 상태 | `RUNNING / COMPLETED / STOPPED` |
| 관리자 계층 | `ROLE_SUPER_ADMIN`(최상위, `company_id=NULL`) / `ROLE_ADMIN`(업체 관리자) |
| 1:1 배정 | `vehicle.driver_id UNIQUE` + `driver.vehicle_id UNIQUE` |

## 4. 물리 ERD

### 4.1 테이블 목록 및 데이터 규모 예상

| 테이블 | 예상 행 수 (시연 기준) | 증가 유형 | 보존 정책 |
|---|---|---|---|
| `company` | 1~3건 | 거의 없음 | 영구 |
| `admin` | 1~5건 | 거의 없음 | 영구 |
| `vehicle` | 10건 | 거의 없음 | 영구 |
| `driver` | 10건 | 거의 없음 | 영구 |
| `drive_log` | 30~100건 | 시나리오 실행 시 | 1년 |
| `plate_recognition_event` | 100~1,000건 | 번호판 관측 지점마다 | 1년 |
| `gps_data` | 10,000~100,000건 | 매 GPS 수신마다 | 30일 |
| `rest_event` | 100~500건 | 운행당 수 건 | 1년 |
| `fatigue_event` | 1,000~10,000건 | GPS 수신 주기 | 1년 |
| `fatigue_threshold` | 21건 (고정) | 거의 없음 | 영구 |

### 4.2 인덱스 설계

| 테이블 | 인덱스명 | 컬럼 | 유형 | 목적 |
|---|---|---|---|---|
| `admin` | `(PK)` | `id` | B-Tree | 기본키 |
| `admin` | `(UK)` | `email` | B-Tree | 로그인 조회 |
| `company` | `(PK)` | `id` | B-Tree | 기본키 |
| `company` | `(UK)` | `name` | B-Tree | 업체명 중복 방지 |
| `vehicle` | `(PK)` | `id` | B-Tree | 기본키 |
| `vehicle` | `(UK)` | `plate_no` | B-Tree | 번호판 중복 방지 및 OCR 조회 |
| `driver` | `(PK)` | `id` | B-Tree | 기본키 |
| `drive_log` | `(PK)` | `id` | B-Tree | 기본키 |
| `drive_log` | `idx_drive_log_company_id` | `company_id` | B-Tree | 업체별 운행 이력 조회 |
| `drive_log` | `idx_drive_log_vehicle_id` | `vehicle_id` | B-Tree | 차량별 운행 이력 조회 |
| `drive_log` | `idx_drive_log_driver_id` | `driver_id` | B-Tree | 운전자별 운행 이력 조회 |
| `drive_log` | `idx_drive_log_started_at` | `started_at` | B-Tree | 날짜 기준 이력/통계 조회 |
| `plate_recognition_event` | `idx_plate_event_drive_log_id` | `drive_log_id` | B-Tree | 운행별 번호판 관측 타임라인 조회 |
| `plate_recognition_event` | `idx_plate_event_vehicle_id` | `vehicle_id` | B-Tree | 차량별 번호판 관측 이력 조회 |
| `plate_recognition_event` | `idx_plate_event_captured_at` | `captured_at` | B-Tree | 관측 시각 기준 조회 |
| `plate_recognition_event` | `idx_plate_event_location_type` | `location_type` | B-Tree | 고속도로/휴게소 관측 구분 조회 |
| `gps_data` | `idx_gps_data_drive_log_id` | `drive_log_id` | B-Tree | 운행별 GPS 조회 |
| `gps_data` | `idx_gps_data_recorded_at` | `recorded_at` | B-Tree | 시간대별 야간 운행 계산 |
| `rest_event` | `idx_rest_event_drive_log_id` | `drive_log_id` | B-Tree | 운행별 휴식 이벤트 조회 |
| `fatigue_event` | `idx_fatigue_event_drive_log_id` | `drive_log_id` | B-Tree | 운행별 피로도 이벤트 조회 |
| `fatigue_event` | `idx_fatigue_event_occurred_at` | `occurred_at` | B-Tree | 통계 집계용 시간 조회 |
| `fatigue_threshold` | `(UK)` | `threshold_key` | B-Tree | key 기반 임계값 조회 |

### 4.3 FK 및 CASCADE 정책

| FK | 참조 방향 | ON DELETE | 비고 |
|---|---|---|---|
| `driver.vehicle_id → vehicle.id` | `driver → vehicle` | `SET NULL` | 차량 삭제 시 운전자 `vehicle_id` 초기화 |
| `vehicle.driver_id → driver.id` | `vehicle → driver` | `SET NULL` | 운전자 삭제 시 차량 `driver_id` 초기화 |
| `admin.company_id → company.id` | `admin → company` | `SET NULL` | 업체 삭제 시 관리자 `company_id` 초기화 |
| `vehicle.company_id → company.id` | `vehicle → company` | `SET NULL` | 업체 삭제 시 차량 `company_id` 초기화 |
| `driver.company_id → company.id` | `driver → company` | `SET NULL` | 업체 삭제 시 운전자 `company_id` 초기화 |
| `drive_log.vehicle_id → vehicle.id` | `drive_log → vehicle` | `RESTRICT` | 운행 중 차량 삭제 방지 |
| `drive_log.driver_id → driver.id` | `drive_log → driver` | `RESTRICT` | 운행 중 운전자 삭제 방지 |
| `drive_log.company_id → company.id` | `drive_log → company` | `SET NULL` | 업체 삭제 시 운행 기록 초기화 |
| `plate_recognition_event.drive_log_id → drive_log.id` | `plate_recognition_event → drive_log` | `CASCADE` | 운행 삭제 시 번호판 관측 이벤트 함께 삭제 |
| `plate_recognition_event.vehicle_id → vehicle.id` | `plate_recognition_event → vehicle` | `SET NULL` | 차량 삭제 시 관측 이력은 보존 |
| `gps_data.drive_log_id → drive_log.id` | `gps_data → drive_log` | `CASCADE` | 운행 삭제 시 GPS 데이터 함께 삭제 |
| `rest_event.drive_log_id → drive_log.id` | `rest_event → drive_log` | `CASCADE` | 운행 삭제 시 휴식 이벤트 함께 삭제 |
| `fatigue_event.drive_log_id → drive_log.id` | `fatigue_event → drive_log` | `CASCADE` | 운행 삭제 시 피로도 이벤트 함께 삭제 |

### 4.4 물리 ERD 다이어그램

- 원본 docx의 `[그림 2] logmile 물리 ERD - 테이블 명세` 참조
