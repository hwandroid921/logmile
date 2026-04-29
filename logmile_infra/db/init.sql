-- ============================================================
-- logmile - 화물차 운전자 피로도 실시간 모니터링 플랫폼
-- DB 초기 스키마 DDL
-- 버전: v4.0
-- 작성일: 2026.04.29
-- ============================================================

-- 확장 모듈
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- 1. admin (관리자 계정)
-- ============================================================
CREATE TABLE IF NOT EXISTS admin (
    id          BIGSERIAL       PRIMARY KEY,
    email       VARCHAR(100)    NOT NULL UNIQUE,
    password    VARCHAR(255)    NOT NULL,
    name        VARCHAR(50)     NOT NULL,
    phone       VARCHAR(20),
    role        VARCHAR(20)     NOT NULL DEFAULT 'ROLE_ADMIN',
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  admin               IS '관리자 계정 및 권한 정보';
COMMENT ON COLUMN admin.email         IS '관리자 로그인 이메일 (unique)';
COMMENT ON COLUMN admin.password      IS 'BCrypt 암호화 패스워드';
COMMENT ON COLUMN admin.role          IS '권한 (ROLE_ADMIN)';

-- ============================================================
-- 2. vehicle (차량) — driver FK는 아래 ALTER TABLE로 추가
-- ============================================================
CREATE TABLE IF NOT EXISTS vehicle (
    id          BIGSERIAL       PRIMARY KEY,
    plate_no    VARCHAR(20)     NOT NULL UNIQUE,
    type        VARCHAR(50)     NOT NULL,
    driver_id   BIGINT,
    is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  vehicle             IS '차량 번호, 차종, 활성 상태';
COMMENT ON COLUMN vehicle.plate_no    IS '차량 번호판 (unique)';
COMMENT ON COLUMN vehicle.type        IS '차종 (예: 5톤 카고, 11톤 윙바디 등)';
COMMENT ON COLUMN vehicle.driver_id   IS '현재 배정된 운전자 ID (nullable)';
COMMENT ON COLUMN vehicle.is_active   IS '차량 활성 여부';

-- ============================================================
-- 3. driver (운전자) — vehicle FK 참조
-- ============================================================
CREATE TABLE IF NOT EXISTS driver (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(50)     NOT NULL,
    phone           VARCHAR(20)     NOT NULL,
    license_type    VARCHAR(30)     NOT NULL,
    vehicle_id      BIGINT,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_driver_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES vehicle (id)
        ON DELETE SET NULL
);

COMMENT ON TABLE  driver                 IS '운전자 기본 정보 및 차량 배정 정보';
COMMENT ON COLUMN driver.license_type    IS '면허 종류 (예: 1종 대형, 1종 보통 등)';
COMMENT ON COLUMN driver.vehicle_id      IS '배정된 차량 ID (nullable)';

-- vehicle.driver_id → driver FK 후행 추가 (순환 참조 방지)
ALTER TABLE vehicle
    ADD CONSTRAINT fk_vehicle_driver
    FOREIGN KEY (driver_id) REFERENCES driver (id)
    ON DELETE SET NULL;

-- ============================================================
-- 4. drive_log (운행 기록)
-- ============================================================
CREATE TABLE IF NOT EXISTS drive_log (
    id                      BIGSERIAL       PRIMARY KEY,
    vehicle_id              BIGINT          NOT NULL,
    driver_id               BIGINT          NOT NULL,
    started_at              TIMESTAMP       NOT NULL,
    ended_at                TIMESTAMP,
    scenario_type           VARCHAR(10)     NOT NULL,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'RUNNING',
    -- OCR 번호판 인식 결과 (FR-OCR01~03)
    recognized_plate_no     VARCHAR(20),
    ocr_confidence          DOUBLE PRECISION,
    is_manual_input         BOOLEAN         NOT NULL DEFAULT FALSE,
    -- 운행 완료 요약 (FR-D01: 운행 이력 목록 조회)
    total_driving_minutes   INTEGER,
    night_driving_minutes   INTEGER,
    total_rest_count        INTEGER,
    max_fatigue_score       INTEGER,
    max_fatigue_level       VARCHAR(20),
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_drive_log_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES vehicle (id),
    CONSTRAINT fk_drive_log_driver
        FOREIGN KEY (driver_id) REFERENCES driver (id)
);

COMMENT ON TABLE  drive_log                        IS '운행 시작/종료, 시나리오, 운행 상태';
COMMENT ON COLUMN drive_log.scenario_type          IS '시나리오 종류 (A: 정상, B: 주의, C: 위험)';
COMMENT ON COLUMN drive_log.status                 IS '운행 상태 (RUNNING, COMPLETED, STOPPED)';
COMMENT ON COLUMN drive_log.recognized_plate_no    IS 'OCR이 인식한 번호판 텍스트 (FR-OCR01, FR-OCR02)';
COMMENT ON COLUMN drive_log.ocr_confidence         IS '번호판 인식 신뢰도 (0.0~1.0), 0.85 미만 시 수동 입력 (FR-OCR03)';
COMMENT ON COLUMN drive_log.is_manual_input        IS '수동 입력 여부 (TRUE: fallback 수동입력, FALSE: OCR 자동인식)';
COMMENT ON COLUMN drive_log.total_driving_minutes  IS '총 운행 시간 (분), 운행 종료 시 기록 (FR-D01)';
COMMENT ON COLUMN drive_log.night_driving_minutes  IS '야간 운행 시간 (분), 22:00~06:00 구간, 운행 종료 시 기록 (FR-D01)';
COMMENT ON COLUMN drive_log.total_rest_count       IS '유효 휴식 횟수, 운행 종료 시 기록 (FR-D01)';
COMMENT ON COLUMN drive_log.max_fatigue_score      IS '운행 중 최고 피로 점수, 운행 종료 시 기록 (FR-D01)';
COMMENT ON COLUMN drive_log.max_fatigue_level      IS '운행 중 최고 피로 등급 (NORMAL/CAUTION/DANGER), 운행 종료 시 기록 (FR-D01)';

CREATE INDEX idx_drive_log_vehicle_id ON drive_log (vehicle_id);
CREATE INDEX idx_drive_log_driver_id  ON drive_log (driver_id);
CREATE INDEX idx_drive_log_started_at ON drive_log (started_at);

-- ============================================================
-- 5. gps_data (GPS 좌표 및 속도)
-- ============================================================
CREATE TABLE IF NOT EXISTS gps_data (
    id              BIGSERIAL       PRIMARY KEY,
    drive_log_id    BIGINT          NOT NULL,
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    speed_kmh       DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    recorded_at     TIMESTAMP       NOT NULL,
    CONSTRAINT fk_gps_data_drive_log
        FOREIGN KEY (drive_log_id) REFERENCES drive_log (id)
        ON DELETE CASCADE
);

COMMENT ON TABLE  gps_data               IS 'GPS 좌표, 속도, 기록 시각';
COMMENT ON COLUMN gps_data.speed_kmh     IS '차량 속도 (km/h), 3 이하는 정차로 판단';
COMMENT ON COLUMN gps_data.recorded_at   IS 'GPS 데이터 실제 기록 시각 (시뮬레이터 기준)';

CREATE INDEX idx_gps_data_drive_log_id   ON gps_data (drive_log_id);
CREATE INDEX idx_gps_data_recorded_at    ON gps_data (recorded_at);

-- ============================================================
-- 6. rest_event (휴식 이벤트)
-- ============================================================
CREATE TABLE IF NOT EXISTS rest_event (
    id                  BIGSERIAL       PRIMARY KEY,
    drive_log_id        BIGINT          NOT NULL,
    rest_started_at     TIMESTAMP       NOT NULL,
    rest_ended_at       TIMESTAMP,
    rest_minutes        INTEGER,
    rest_type           VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_rest_event_drive_log
        FOREIGN KEY (drive_log_id) REFERENCES drive_log (id)
        ON DELETE CASCADE
);

COMMENT ON TABLE  rest_event                IS '휴식 시작/종료 시각, 휴식 시간, 휴식 유형';
COMMENT ON COLUMN rest_event.rest_minutes   IS '실제 휴식 시간 (분), 종료 후 계산';
COMMENT ON COLUMN rest_event.rest_type      IS '휴식 유형 (VALID: 유효 15분 이상, SUFFICIENT: 충분 30분 이상, INVALID: 미달, PENDING: 진행 중)';

CREATE INDEX idx_rest_event_drive_log_id ON rest_event (drive_log_id);

-- ============================================================
-- 7. fatigue_event (피로도 이벤트)
-- ============================================================
CREATE TABLE IF NOT EXISTS fatigue_event (
    id                              BIGSERIAL       PRIMARY KEY,
    drive_log_id                    BIGINT          NOT NULL,
    fatigue_score                   INTEGER         NOT NULL DEFAULT 0,
    fatigue_level                   VARCHAR(20)     NOT NULL DEFAULT 'NORMAL',
    continuous_driving_minutes      INTEGER         NOT NULL DEFAULT 0,
    daily_total_driving_minutes     INTEGER         NOT NULL DEFAULT 0,
    night_driving_minutes           INTEGER         NOT NULL DEFAULT 0,
    rest_count                      INTEGER         NOT NULL DEFAULT 0,
    rest_violation_count            INTEGER         NOT NULL DEFAULT 0,
    reason                          TEXT,
    occurred_at                     TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_fatigue_event_drive_log
        FOREIGN KEY (drive_log_id) REFERENCES drive_log (id)
        ON DELETE CASCADE
);

COMMENT ON TABLE  fatigue_event                              IS '피로 점수, 등급, 판단 항목, 판단 근거';
COMMENT ON COLUMN fatigue_event.fatigue_score                IS '피로도 총점';
COMMENT ON COLUMN fatigue_event.fatigue_level                IS '피로 등급 (NORMAL: 0~39, CAUTION: 40~69, DANGER: 70 이상)';
COMMENT ON COLUMN fatigue_event.continuous_driving_minutes   IS '연속 운행 시간 (분)';
COMMENT ON COLUMN fatigue_event.daily_total_driving_minutes  IS '일일 총 운행 시간 (분)';
COMMENT ON COLUMN fatigue_event.night_driving_minutes        IS '야간 운행 누적 시간 (분), 22:00~06:00 기준';
COMMENT ON COLUMN fatigue_event.rest_count                   IS '유효 휴식 횟수';
COMMENT ON COLUMN fatigue_event.rest_violation_count         IS '휴식 누락 횟수';
COMMENT ON COLUMN fatigue_event.reason                       IS '피로도 점수 산정 근거 (텍스트)';

CREATE INDEX idx_fatigue_event_drive_log_id ON fatigue_event (drive_log_id);
CREATE INDEX idx_fatigue_event_occurred_at  ON fatigue_event (occurred_at);

-- ============================================================
-- 8. fatigue_threshold (피로도 임계값 설정)
-- ============================================================
CREATE TABLE IF NOT EXISTS fatigue_threshold (
    id                  BIGSERIAL       PRIMARY KEY,
    threshold_key       VARCHAR(100)    NOT NULL UNIQUE,
    threshold_value     DOUBLE PRECISION NOT NULL,
    description         VARCHAR(255),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  fatigue_threshold                  IS '피로도 점수 모델 임계값 key/value 관리';
COMMENT ON COLUMN fatigue_threshold.threshold_key    IS '임계값 식별 키 (예: CONTINUOUS_90, DAILY_6H)';
COMMENT ON COLUMN fatigue_threshold.threshold_value  IS '임계값 (분 또는 점수)';
