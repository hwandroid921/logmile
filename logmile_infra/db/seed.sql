-- ============================================================
-- logmile - 초기 시드 데이터
-- 버전: v4.0
-- 작성일: 2026.04.29
-- ============================================================

-- ============================================================
-- 1. admin 초기 계정
-- password: admin1234 (BCrypt 해시)
-- ============================================================
INSERT INTO admin (email, password, name, phone, role)
VALUES (
    'admin@logmile.com',
    '$2a$10$VgBMTWqr3oBJBOvRl3sVXuCR6PjGLxUoB7C7sNH3FAJH4VK5kDkRi',
    '시스템관리자',
    '010-0000-0000',
    'ROLE_ADMIN'
);

-- ============================================================
-- 2. vehicle 초기 차량 10대 (가상 물류센터)
-- ============================================================
INSERT INTO vehicle (plate_no, type, is_active) VALUES
    ('12가 3456', '5톤 카고',      TRUE),
    ('23나 4567', '5톤 카고',      TRUE),
    ('34다 5678', '11톤 윙바디',   TRUE),
    ('45라 6789', '11톤 윙바디',   TRUE),
    ('56마 7890', '25톤 트레일러', TRUE),
    ('67바 8901', '25톤 트레일러', TRUE),
    ('78사 9012', '5톤 냉동',      TRUE),
    ('89아 0123', '5톤 냉동',      TRUE),
    ('90자 1234', '11톤 카고',     TRUE),
    ('01차 2345', '11톤 카고',     TRUE);

-- ============================================================
-- 3. driver 초기 운전자 10명 + 차량 배정
-- ============================================================
INSERT INTO driver (name, phone, license_type, vehicle_id) VALUES
    ('김철수', '010-1111-0001', '1종 대형', 1),
    ('이영희', '010-1111-0002', '1종 대형', 2),
    ('박민준', '010-1111-0003', '1종 대형', 3),
    ('최수진', '010-1111-0004', '1종 대형', 4),
    ('정우성', '010-1111-0005', '1종 대형', 5),
    ('강지훈', '010-1111-0006', '1종 대형', 6),
    ('윤서연', '010-1111-0007', '1종 보통', 7),
    ('임현우', '010-1111-0008', '1종 보통', 8),
    ('한지민', '010-1111-0009', '1종 보통', 9),
    ('오태양', '010-1111-0010', '1종 대형', 10);

-- vehicle.driver_id 역방향 업데이트
UPDATE vehicle SET driver_id = 1  WHERE id = 1;
UPDATE vehicle SET driver_id = 2  WHERE id = 2;
UPDATE vehicle SET driver_id = 3  WHERE id = 3;
UPDATE vehicle SET driver_id = 4  WHERE id = 4;
UPDATE vehicle SET driver_id = 5  WHERE id = 5;
UPDATE vehicle SET driver_id = 6  WHERE id = 6;
UPDATE vehicle SET driver_id = 7  WHERE id = 7;
UPDATE vehicle SET driver_id = 8  WHERE id = 8;
UPDATE vehicle SET driver_id = 9  WHERE id = 9;
UPDATE vehicle SET driver_id = 10 WHERE id = 10;

-- ============================================================
-- 4. fatigue_threshold 피로도 임계값 기본 설정
-- 프로젝트개요서 v4 기준
-- ============================================================

-- 연속 운행 시간 임계값 (단위: 분)
INSERT INTO fatigue_threshold (threshold_key, threshold_value, description) VALUES
    ('CONTINUOUS_DRIVING_90',   90,  '연속 운행 90분 이상 → +10점'),
    ('CONTINUOUS_DRIVING_120', 120,  '연속 운행 120분 이상 → +25점'),
    ('CONTINUOUS_DRIVING_180', 180,  '연속 운행 180분 이상 → +45점'),
    ('CONTINUOUS_DRIVING_240', 240,  '연속 운행 240분 이상 → +65점');

-- 일일 총 운행 시간 임계값 (단위: 분)
INSERT INTO fatigue_threshold (threshold_key, threshold_value, description) VALUES
    ('DAILY_DRIVING_360',  360,  '일일 총 운행 6시간(360분) 이상 → +15점'),
    ('DAILY_DRIVING_480',  480,  '일일 총 운행 8시간(480분) 이상 → +30점'),
    ('DAILY_DRIVING_600',  600,  '일일 총 운행 10시간(600분) 이상 → +45점');

-- 야간 운행 시간 임계값 (단위: 분, 22:00~06:00 기준)
INSERT INTO fatigue_threshold (threshold_key, threshold_value, description) VALUES
    ('NIGHT_DRIVING_30',    30,  '야간 운행 누적 30분 이상 → +10점'),
    ('NIGHT_DRIVING_60',    60,  '야간 운행 누적 1시간 이상 → +20점'),
    ('NIGHT_DRIVING_120',  120,  '야간 운행 누적 2시간 이상 → +35점');

-- 휴식 관련 임계값 (단위: 분)
INSERT INTO fatigue_threshold (threshold_key, threshold_value, description) VALUES
    ('REST_VALID_MINUTES',      15,  '유효 휴식 기준 15분 이상 (speed ≤ 3 km/h)'),
    ('REST_SUFFICIENT_MINUTES', 30,  '충분 휴식 기준 30분 이상'),
    ('REST_REQUIRED_AFTER',    120,  '2시간(120분) 운행 후 휴식 필요');

-- 휴식 부족 점수 임계값
INSERT INTO fatigue_threshold (threshold_key, threshold_value, description) VALUES
    ('REST_VIOLATION_ONCE_SCORE',  10,  '휴식 누락 1회 → +10점'),
    ('REST_VIOLATION_TWICE_SCORE', 25,  '휴식 누락 2회 이상 → +25점');

-- 휴식 보정 점수 임계값
INSERT INTO fatigue_threshold (threshold_key, threshold_value, description) VALUES
    ('REST_CORRECTION_VALID_SCORE',      10,  '유효 휴식(15분 이상) 보정 → -10점'),
    ('REST_CORRECTION_SUFFICIENT_SCORE', 20,  '충분 휴식(30분 이상) 보정 → -20점');

-- 피로 등급 점수 기준
INSERT INTO fatigue_threshold (threshold_key, threshold_value, description) VALUES
    ('LEVEL_NORMAL_MAX',  39,  '정상 등급 최대 점수 (0~39)'),
    ('LEVEL_CAUTION_MIN', 40,  '주의 등급 최소 점수 (40~69)'),
    ('LEVEL_CAUTION_MAX', 69,  '주의 등급 최대 점수 (40~69)'),
    ('LEVEL_DANGER_MIN',  70,  '위험 등급 최소 점수 (70 이상)');
