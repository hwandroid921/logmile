-- ============================================================
-- logmile 대시보드 더미 데이터
-- 생성일: 2026-05-24
-- 목적: 대시보드 실데이터 연동 테스트
-- 번호판 이미지 기반: 01누5936 / 01다0673 / 판독불가(수동) / 01도4663 / 01스4684
-- ============================================================

BEGIN;

-- ===== 0. 기존 테스트 run 정리 =====
-- drive_log id=1 (김철수, vehicle 1): 오늘 날짜로 재설정
UPDATE drive_log SET
    started_at         = '2026-05-24 09:00:00',
    scenario_type      = 'C',
    recognized_plate_no = '01누 5936',
    ocr_confidence     = 0.94,
    is_manual_input    = FALSE
WHERE id = 1;

-- drive_log id=8 (테스트용 가나다/998보3270): COMPLETED 처리
UPDATE drive_log SET
    status    = 'COMPLETED',
    ended_at  = '2026-05-21 16:16:00'
WHERE id = 8;

-- ===== 1. 오늘(2026-05-24) RUNNING drive_log 4개 추가 (vehicles 2~5, company 1) =====
-- 다음 ID: 10, 11, 12, 13  (nextval로 9 이미 소진됨)
INSERT INTO drive_log
    (company_id, vehicle_id, driver_id, started_at, scenario_type, status,
     recognized_plate_no, ocr_confidence, is_manual_input)
VALUES
    -- vehicle 2, 이영희, 시나리오 B (주의), 01다 0673
    (1, 2, 2, '2026-05-24 10:30:00', 'B', 'RUNNING', '01다 0673', 0.91, FALSE),
    -- vehicle 3, 박민준, 시나리오 A (정상), 판독불가 → 수동입력
    (1, 3, 3, '2026-05-24 11:30:00', 'A', 'RUNNING', NULL, NULL, TRUE),
    -- vehicle 4, 최수진, 시나리오 C (위험), 01도 4663
    (1, 4, 4, '2026-05-24 07:30:00', 'C', 'RUNNING', '01도 4663', 0.87, FALSE),
    -- vehicle 5, 정우성, 시나리오 B (주의), 01스 4684
    (1, 5, 5, '2026-05-24 12:00:00', 'B', 'RUNNING', '01스 4684', 0.92, FALSE);

-- ===== 2. 오늘 RUNNING drive_log용 fatigue_event (시계열) =====

-- [vehicle 1 / 김철수] drive_log id=1 / 시나리오 C → DANGER
INSERT INTO fatigue_event
    (drive_log_id, fatigue_score, fatigue_level, continuous_driving_minutes,
     daily_total_driving_minutes, night_driving_minutes, rest_count,
     rest_violation_count, reason, occurred_at)
VALUES
    (1, 35, 'NORMAL',  90, 90,   0, 0, 0,
     '연속 90분 운행 중 — 피로도 낮음',
     '2026-05-24 10:30:00'),
    (1, 55, 'CAUTION', 150, 150, 0, 0, 1,
     '연속 150분 운행, 휴식 누락 1회',
     '2026-05-24 11:30:00'),
    (1, 72, 'DANGER',  210, 210, 0, 0, 2,
     '연속 210분 초과, 휴식 누락 2회 — 즉시 휴식 권고',
     '2026-05-24 12:30:00'),
    (1, 82, 'DANGER',  270, 270, 0, 0, 2,
     '연속 270분 초과, 일일 누적 270분 — 위험 단계 지속',
     '2026-05-24 14:00:00');

-- [vehicle 2 / 이영희] drive_log id=10 / 시나리오 B → CAUTION
INSERT INTO fatigue_event
    (drive_log_id, fatigue_score, fatigue_level, continuous_driving_minutes,
     daily_total_driving_minutes, night_driving_minutes, rest_count,
     rest_violation_count, reason, occurred_at)
VALUES
    (10, 28, 'NORMAL',  60,  60,  0, 1, 0,
     '유효 휴식 1회 완료, 피로도 낮음',
     '2026-05-24 11:30:00'),
    (10, 55, 'CAUTION', 150, 150, 0, 1, 1,
     '연속 150분 운행, 추가 휴식 필요',
     '2026-05-24 13:00:00');

-- [vehicle 3 / 박민준] drive_log id=11 / 시나리오 A → NORMAL
INSERT INTO fatigue_event
    (drive_log_id, fatigue_score, fatigue_level, continuous_driving_minutes,
     daily_total_driving_minutes, night_driving_minutes, rest_count,
     rest_violation_count, reason, occurred_at)
VALUES
    (11, 22, 'NORMAL', 90, 90, 0, 1, 0,
     '유효 휴식 1회, 연속 운행 90분 — 정상 상태',
     '2026-05-24 13:00:00');

-- [vehicle 4 / 최수진] drive_log id=12 / 시나리오 C → DANGER (최고 위험)
INSERT INTO fatigue_event
    (drive_log_id, fatigue_score, fatigue_level, continuous_driving_minutes,
     daily_total_driving_minutes, night_driving_minutes, rest_count,
     rest_violation_count, reason, occurred_at)
VALUES
    (12, 42, 'CAUTION', 90,  90,  0, 0, 1,
     '연속 90분, 휴식 누락 1회',
     '2026-05-24 09:00:00'),
    (12, 65, 'CAUTION', 150, 150, 0, 0, 2,
     '연속 150분 초과, 휴식 누락 2회',
     '2026-05-24 10:00:00'),
    (12, 80, 'DANGER',  210, 210, 0, 0, 3,
     '연속 210분 초과, 휴식 3회 누락 — 위험 진입',
     '2026-05-24 11:00:00'),
    (12, 88, 'DANGER',  270, 270, 0, 0, 3,
     '연속 270분, 일일 270분 — 전화 권고 필요',
     '2026-05-24 12:00:00'),
    (12, 92, 'DANGER',  300, 300, 0, 0, 3,
     '연속 300분 초과 — 즉시 운행 중단 요청',
     '2026-05-24 13:00:00');

-- [vehicle 5 / 정우성] drive_log id=13 / 시나리오 B → CAUTION
INSERT INTO fatigue_event
    (drive_log_id, fatigue_score, fatigue_level, continuous_driving_minutes,
     daily_total_driving_minutes, night_driving_minutes, rest_count,
     rest_violation_count, reason, occurred_at)
VALUES
    (13, 40, 'CAUTION', 90,  90,  0, 0, 1,
     '연속 90분, 휴식 누락 1회 — 주의 단계',
     '2026-05-24 13:30:00'),
    (13, 48, 'CAUTION', 120, 120, 0, 0, 1,
     '연속 120분 도달, 휴식 권고',
     '2026-05-24 14:30:00');

-- ===== 3. 오늘 RUNNING drive_log용 GPS 데이터 =====

-- [vehicle 1 / 김철수] 서울 → 인천 방향 (경인고속도로, DANGER 장거리)
INSERT INTO gps_data (drive_log_id, latitude, longitude, speed_kmh, recorded_at) VALUES
    (1, 37.5520, 126.9726, 72.0, '2026-05-24 09:05:00'),
    (1, 37.5380, 126.9380, 85.5, '2026-05-24 09:20:00'),
    (1, 37.5245, 126.8920, 92.0, '2026-05-24 09:40:00'),
    (1, 37.5120, 126.8480, 88.3, '2026-05-24 10:00:00'),
    (1, 37.4980, 126.8024,  0.0, '2026-05-24 10:25:00'),  -- 정차(휴게소 진입 시도 후 이탈)
    (1, 37.4843, 126.7684, 78.6, '2026-05-24 10:45:00'),
    (1, 37.4698, 126.7312, 91.2, '2026-05-24 11:05:00'),
    (1, 37.4580, 126.7052, 86.7, '2026-05-24 11:30:00'),
    (1, 37.4430, 126.6720, 94.1, '2026-05-24 12:00:00'),
    (1, 37.4290, 126.6401, 88.9, '2026-05-24 12:30:00'),
    (1, 37.4160, 126.6124, 79.5, '2026-05-24 13:00:00'),
    (1, 37.4050, 126.5882, 82.3, '2026-05-24 13:30:00'),
    (1, 37.3945, 126.5661, 90.0, '2026-05-24 14:00:00');

-- [vehicle 2 / 이영희] 서울 → 수원 방향 (경부고속도로, CAUTION)
INSERT INTO gps_data (drive_log_id, latitude, longitude, speed_kmh, recorded_at) VALUES
    (10, 37.5520, 126.9726, 65.0, '2026-05-24 10:35:00'),
    (10, 37.5124, 127.0025, 88.4, '2026-05-24 10:55:00'),
    (10, 37.4724, 127.0163, 92.1, '2026-05-24 11:15:00'),
    (10, 37.4324, 127.0225,  0.0, '2026-05-24 11:35:00'),  -- 휴게소 정차 (유효 휴식)
    (10, 37.4324, 127.0225,  0.0, '2026-05-24 12:05:00'),  -- 휴식 중
    (10, 37.3924, 127.0269, 85.6, '2026-05-24 12:25:00'),
    (10, 37.3524, 127.0278, 90.2, '2026-05-24 12:50:00'),
    (10, 37.2636, 127.0286, 78.9, '2026-05-24 13:15:00');

-- [vehicle 3 / 박민준] 서울 → 성남 방향 (단거리, NORMAL)
INSERT INTO gps_data (drive_log_id, latitude, longitude, speed_kmh, recorded_at) VALUES
    (11, 37.5520, 126.9726, 55.0, '2026-05-24 11:35:00'),
    (11, 37.5080, 127.0180, 72.3, '2026-05-24 11:55:00'),
    (11, 37.4780, 127.0420,  0.0, '2026-05-24 12:10:00'),  -- 휴게소 정차
    (11, 37.4780, 127.0420,  0.0, '2026-05-24 12:35:00'),  -- 충분 휴식
    (11, 37.4450, 127.1050, 68.7, '2026-05-24 12:55:00'),
    (11, 37.4150, 127.1200, 61.2, '2026-05-24 13:10:00');

-- [vehicle 4 / 최수진] 서울 → 인천 → 아산 방향 (DANGER 최장거리)
INSERT INTO gps_data (drive_log_id, latitude, longitude, speed_kmh, recorded_at) VALUES
    (12, 37.5520, 126.9726, 78.0, '2026-05-24 07:35:00'),
    (12, 37.5380, 126.9380, 90.5, '2026-05-24 07:55:00'),
    (12, 37.5120, 126.8480, 94.8, '2026-05-24 08:25:00'),
    (12, 37.4843, 126.7684, 88.2, '2026-05-24 08:55:00'),
    (12, 37.4580, 126.7052, 92.6, '2026-05-24 09:25:00'),
    (12, 37.4290, 126.6401, 89.1, '2026-05-24 09:55:00'),
    (12, 37.4050, 126.5882, 93.4, '2026-05-24 10:25:00'),
    (12, 37.3820, 126.5480, 87.7, '2026-05-24 10:55:00'),
    (12, 37.3580, 126.5180, 91.3, '2026-05-24 11:25:00'),
    (12, 37.3350, 126.4890, 88.6, '2026-05-24 11:55:00'),
    (12, 37.3100, 126.4620, 90.8, '2026-05-24 12:25:00'),
    (12, 37.2880, 126.4380, 86.4, '2026-05-24 12:55:00'),
    (12, 37.2650, 126.4140, 92.1, '2026-05-24 13:25:00');

-- [vehicle 5 / 정우성] 서울 → 안양 방향 (CAUTION)
INSERT INTO gps_data (drive_log_id, latitude, longitude, speed_kmh, recorded_at) VALUES
    (13, 37.5520, 126.9726, 60.0, '2026-05-24 12:05:00'),
    (13, 37.5180, 126.9502, 78.4, '2026-05-24 12:25:00'),
    (13, 37.4840, 126.9276, 85.2, '2026-05-24 12:45:00'),
    (13, 37.4500, 126.9156, 82.7, '2026-05-24 13:05:00'),
    (13, 37.4200, 126.9064, 79.3, '2026-05-24 13:25:00'),
    (13, 37.3939, 126.9568, 88.1, '2026-05-24 13:50:00'),
    (13, 37.3640, 126.9642, 84.6, '2026-05-24 14:15:00');

-- ===== 4. 오늘 RUNNING drive_log용 rest_event =====

-- [vehicle 2 / 이영희] 유효 휴식 1회 (30분 이상 → SUFFICIENT)
INSERT INTO rest_event (drive_log_id, rest_started_at, rest_ended_at, rest_minutes, rest_type)
VALUES (10, '2026-05-24 11:35:00', '2026-05-24 12:07:00', 32, 'SUFFICIENT');

-- [vehicle 3 / 박민준] 충분 휴식 1회 (SUFFICIENT)
INSERT INTO rest_event (drive_log_id, rest_started_at, rest_ended_at, rest_minutes, rest_type)
VALUES (11, '2026-05-24 12:10:00', '2026-05-24 12:45:00', 35, 'SUFFICIENT');

-- [vehicle 1 / 김철수] 정차 10분 (INVALID — 15분 미달)
INSERT INTO rest_event (drive_log_id, rest_started_at, rest_ended_at, rest_minutes, rest_type)
VALUES (1, '2026-05-24 10:25:00', '2026-05-24 10:37:00', 12, 'INVALID');

-- ===== 5. 지난 7일 COMPLETED drive_log + fatigue_event (주간 통계용) =====

-- 2026-05-18 (DANGER 1건)
INSERT INTO drive_log
    (company_id, vehicle_id, driver_id, started_at, ended_at, scenario_type, status,
     total_driving_minutes, night_driving_minutes, total_rest_count,
     max_fatigue_score, max_fatigue_level)
VALUES
    (1, 1, 1, '2026-05-18 08:00:00', '2026-05-18 16:30:00', 'C', 'COMPLETED',
     480, 0, 1, 82, 'DANGER');       -- id=14

INSERT INTO fatigue_event
    (drive_log_id, fatigue_score, fatigue_level, continuous_driving_minutes,
     daily_total_driving_minutes, night_driving_minutes, rest_count,
     rest_violation_count, reason, occurred_at)
VALUES
    (14, 82, 'DANGER', 270, 480, 0, 1, 2,
     '연속 270분 초과, 휴식 누락 2회',
     '2026-05-18 14:00:00');

-- 2026-05-19 (CAUTION만, DANGER 없음 → 주간 통계 0)
INSERT INTO drive_log
    (company_id, vehicle_id, driver_id, started_at, ended_at, scenario_type, status,
     total_driving_minutes, night_driving_minutes, total_rest_count,
     max_fatigue_score, max_fatigue_level)
VALUES
    (1, 2, 2, '2026-05-19 09:00:00', '2026-05-19 15:00:00', 'A', 'COMPLETED',
     360, 0, 2, 30, 'NORMAL');       -- id=15

INSERT INTO fatigue_event
    (drive_log_id, fatigue_score, fatigue_level, continuous_driving_minutes,
     daily_total_driving_minutes, night_driving_minutes, rest_count,
     rest_violation_count, reason, occurred_at)
VALUES
    (15, 30, 'NORMAL', 90, 360, 0, 2, 0,
     '충분한 휴식, 정상 상태',
     '2026-05-19 12:00:00');

-- 2026-05-20 (DANGER 2건)
INSERT INTO drive_log
    (company_id, vehicle_id, driver_id, started_at, ended_at, scenario_type, status,
     total_driving_minutes, night_driving_minutes, total_rest_count,
     max_fatigue_score, max_fatigue_level)
VALUES
    (1, 1, 1, '2026-05-20 07:00:00', '2026-05-20 18:00:00', 'C', 'COMPLETED',
     600, 0, 0, 95, 'DANGER'),       -- id=16
    (1, 3, 3, '2026-05-20 10:00:00', '2026-05-20 16:00:00', 'C', 'COMPLETED',
     360, 0, 0, 75, 'DANGER');       -- id=17

INSERT INTO fatigue_event
    (drive_log_id, fatigue_score, fatigue_level, continuous_driving_minutes,
     daily_total_driving_minutes, night_driving_minutes, rest_count,
     rest_violation_count, reason, occurred_at)
VALUES
    (16, 95, 'DANGER', 330, 600, 0, 0, 3,
     '연속 330분, 휴식 누락 3회 — 극도 위험',
     '2026-05-20 14:00:00'),
    (17, 75, 'DANGER', 210, 360, 0, 0, 2,
     '연속 210분 초과, 휴식 누락 2회',
     '2026-05-20 15:00:00');

-- 2026-05-21 (DANGER 1건)
INSERT INTO drive_log
    (company_id, vehicle_id, driver_id, started_at, ended_at, scenario_type, status,
     total_driving_minutes, night_driving_minutes, total_rest_count,
     max_fatigue_score, max_fatigue_level)
VALUES
    (1, 4, 4, '2026-05-21 08:00:00', '2026-05-21 19:00:00', 'C', 'COMPLETED',
     600, 60, 0, 88, 'DANGER');      -- id=18

INSERT INTO fatigue_event
    (drive_log_id, fatigue_score, fatigue_level, continuous_driving_minutes,
     daily_total_driving_minutes, night_driving_minutes, rest_count,
     rest_violation_count, reason, occurred_at)
VALUES
    (18, 88, 'DANGER', 300, 600, 60, 0, 3,
     '야간 운행 60분 포함, 연속 300분 초과',
     '2026-05-21 16:00:00');

-- 2026-05-22 (DANGER 3건)
INSERT INTO drive_log
    (company_id, vehicle_id, driver_id, started_at, ended_at, scenario_type, status,
     total_driving_minutes, night_driving_minutes, total_rest_count,
     max_fatigue_score, max_fatigue_level)
VALUES
    (1, 1, 1, '2026-05-22 06:00:00', '2026-05-22 20:00:00', 'C', 'COMPLETED',
     780, 0, 0, 98, 'DANGER'),       -- id=19
    (1, 2, 2, '2026-05-22 09:00:00', '2026-05-22 17:00:00', 'B', 'COMPLETED',
     480, 0, 1, 60, 'CAUTION'),      -- id=20
    (1, 5, 5, '2026-05-22 07:00:00', '2026-05-22 22:00:00', 'C', 'COMPLETED',
     840, 120, 0, 92, 'DANGER');     -- id=21

INSERT INTO fatigue_event
    (drive_log_id, fatigue_score, fatigue_level, continuous_driving_minutes,
     daily_total_driving_minutes, night_driving_minutes, rest_count,
     rest_violation_count, reason, occurred_at)
VALUES
    (19, 98, 'DANGER', 360, 780, 0, 0, 4,
     '연속 360분, 휴식 4회 누락 — 최고위험',
     '2026-05-22 15:00:00'),
    (20, 60, 'CAUTION', 180, 480, 0, 1, 1,
     '연속 180분, 유효휴식 1회 — 주의 단계',
     '2026-05-22 13:00:00'),
    (21, 92, 'DANGER', 300, 840, 120, 0, 3,
     '야간 120분 포함, 연속 300분 — 위험',
     '2026-05-22 18:00:00');

-- 2026-05-23 (DANGER 2건)
INSERT INTO drive_log
    (company_id, vehicle_id, driver_id, started_at, ended_at, scenario_type, status,
     total_driving_minutes, night_driving_minutes, total_rest_count,
     max_fatigue_score, max_fatigue_level)
VALUES
    (1, 3, 3, '2026-05-23 08:00:00', '2026-05-23 16:00:00', 'C', 'COMPLETED',
     480, 0, 1, 72, 'DANGER'),       -- id=22
    (1, 4, 4, '2026-05-23 10:00:00', '2026-05-23 18:30:00', 'B', 'COMPLETED',
     510, 0, 2, 58, 'CAUTION');      -- id=23

INSERT INTO fatigue_event
    (drive_log_id, fatigue_score, fatigue_level, continuous_driving_minutes,
     daily_total_driving_minutes, night_driving_minutes, rest_count,
     rest_violation_count, reason, occurred_at)
VALUES
    (22, 72, 'DANGER', 210, 480, 0, 1, 2,
     '연속 210분, 휴식 누락 2회',
     '2026-05-23 13:00:00'),
    (23, 58, 'CAUTION', 150, 510, 0, 2, 1,
     '연속 150분, 유효휴식 2회 — 주의 유지',
     '2026-05-23 15:30:00');

-- ===== 6. plate_event (OCR 번호판 인식 이벤트) =====
INSERT INTO plate_event
    (vehicle_id, plate_no, event_type, location_type, source_type,
     observed_at, latitude, longitude, confidence, detection_confidence, is_manual_required)
VALUES
    -- 01누 5936 (현대 쏘나타 흰색) → 차량 1 '11가 1001' 불일치, 수동 확인 불필요 (외부 차량)
    (NULL, '01누 5936', 'ENTRY', 'HIGHWAY_GATE', 'OCR',
     '2026-05-24 09:00:00', 37.5520, 126.9726, 0.94, 0.97, FALSE),
    -- 01다 0673 (아우디 A8 은색)
    (NULL, '01다 0673', 'ENTRY', 'HIGHWAY_GATE', 'OCR',
     '2026-05-24 10:30:00', 37.5520, 126.9726, 0.91, 0.95, FALSE),
    -- 판독불가 (키아 EV6) → 수동 확인 필요
    (NULL, '???', 'ENTRY', 'CCTV', 'MANUAL',
     '2026-05-24 11:30:00', 37.5520, 126.9726, NULL, 0.62, TRUE),
    -- 01도 4663 (현대 캐스퍼 야간)
    (NULL, '01도 4663', 'ENTRY', 'HIGHWAY_GATE', 'OCR',
     '2026-05-24 07:30:00', 37.5520, 126.9726, 0.87, 0.91, FALSE),
    -- 01스 4684 (현대 아이오닉6)
    (NULL, '01스 4684', 'ENTRY', 'HIGHWAY_GATE', 'OCR',
     '2026-05-24 12:00:00', 37.5520, 126.9726, 0.92, 0.96, FALSE);

COMMIT;
