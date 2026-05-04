-- ============================================================
-- logmile - 초기 시드 데이터
-- 버전: v4.0
-- 작성일: 2026.04.29
-- ============================================================

-- ============================================================
-- 1. company 초기 업체 10개
-- ============================================================
INSERT INTO company (name, address, phone, is_active) VALUES
    ('로그마일운송', '서울특별시 중구 세종대로 110', '02-1000-0001', TRUE),
    ('한빛물류', '서울특별시 송파구 올림픽로 300', '02-1000-0002', TRUE),
    ('부평카고', '인천광역시 부평구 부평대로 168', '032-100-0003', TRUE),
    ('서해로지스', '인천광역시 연수구 컨벤시아대로 123', '032-100-0004', TRUE),
    ('경기특송', '경기도 수원시 팔달구 효원로 241', '031-100-0005', TRUE),
    ('남부윙바디', '경기도 성남시 분당구 판교로 242', '031-100-0006', TRUE),
    ('동해냉동물류', '강원특별자치도 강릉시 강릉대로 33', '033-100-0007', TRUE),
    ('충청화물', '대전광역시 서구 둔산로 100', '042-100-0008', TRUE),
    ('영남트레일러', '부산광역시 해운대구 센텀중앙로 97', '051-100-0009', TRUE),
    ('호남종합운수', '광주광역시 서구 상무중앙로 7', '062-100-0010', TRUE);

-- ============================================================
-- 2. admin 초기 계정
-- password: admin1234 (BCrypt 해시)
-- ============================================================
INSERT INTO admin (email, password, name, phone, role, status)
VALUES (
    'admin@logmile.com',
    '$2a$10$VgBMTWqr3oBJBOvRl3sVXuCR6PjGLxUoB7C7sNH3FAJH4VK5kDkRi',
    '최상위관리자',
    '010-0000-0000',
    'ROLE_SUPER_ADMIN',
    'ACTIVE'
);

-- ============================================================
-- 3. 일반 관리자 더미 계정 10건 (company별 1명)
-- password: admin1234 (BCrypt 해시)
-- ============================================================
INSERT INTO admin (company_id, email, password, name, phone, role, status) VALUES
    (1,  'admin01@logmile.com', '$2a$10$VgBMTWqr3oBJBOvRl3sVXuCR6PjGLxUoB7C7sNH3FAJH4VK5kDkRi', '로그마일운송 관리자', '010-2000-0001', 'ROLE_ADMIN', 'ACTIVE'),
    (2,  'admin02@logmile.com', '$2a$10$VgBMTWqr3oBJBOvRl3sVXuCR6PjGLxUoB7C7sNH3FAJH4VK5kDkRi', '한빛물류 관리자',     '010-2000-0002', 'ROLE_ADMIN', 'ACTIVE'),
    (3,  'admin03@logmile.com', '$2a$10$VgBMTWqr3oBJBOvRl3sVXuCR6PjGLxUoB7C7sNH3FAJH4VK5kDkRi', '부평카고 관리자',     '010-2000-0003', 'ROLE_ADMIN', 'ACTIVE'),
    (4,  'admin04@logmile.com', '$2a$10$VgBMTWqr3oBJBOvRl3sVXuCR6PjGLxUoB7C7sNH3FAJH4VK5kDkRi', '서해로지스 관리자',   '010-2000-0004', 'ROLE_ADMIN', 'ACTIVE'),
    (5,  'admin05@logmile.com', '$2a$10$VgBMTWqr3oBJBOvRl3sVXuCR6PjGLxUoB7C7sNH3FAJH4VK5kDkRi', '경기특송 관리자',     '010-2000-0005', 'ROLE_ADMIN', 'ACTIVE'),
    (6,  'admin06@logmile.com', '$2a$10$VgBMTWqr3oBJBOvRl3sVXuCR6PjGLxUoB7C7sNH3FAJH4VK5kDkRi', '남부윙바디 관리자',   '010-2000-0006', 'ROLE_ADMIN', 'ACTIVE'),
    (7,  'admin07@logmile.com', '$2a$10$VgBMTWqr3oBJBOvRl3sVXuCR6PjGLxUoB7C7sNH3FAJH4VK5kDkRi', '동해냉동물류 관리자', '010-2000-0007', 'ROLE_ADMIN', 'ACTIVE'),
    (8,  'admin08@logmile.com', '$2a$10$VgBMTWqr3oBJBOvRl3sVXuCR6PjGLxUoB7C7sNH3FAJH4VK5kDkRi', '충청화물 관리자',     '010-2000-0008', 'ROLE_ADMIN', 'ACTIVE'),
    (9,  'admin09@logmile.com', '$2a$10$VgBMTWqr3oBJBOvRl3sVXuCR6PjGLxUoB7C7sNH3FAJH4VK5kDkRi', '영남트레일러 관리자', '010-2000-0009', 'ROLE_ADMIN', 'ACTIVE'),
    (10, 'admin10@logmile.com', '$2a$10$VgBMTWqr3oBJBOvRl3sVXuCR6PjGLxUoB7C7sNH3FAJH4VK5kDkRi', '호남종합운수 관리자', '010-2000-0010', 'ROLE_ADMIN', 'ACTIVE');

-- ============================================================
-- 4. vehicle 초기 차량 10대 (가상 물류센터)
-- ============================================================
INSERT INTO vehicle (company_id, plate_no, type, is_active) VALUES
    (1,  '12가 3456', '5톤 카고',      TRUE),
    (2,  '23나 4567', '5톤 카고',      TRUE),
    (3,  '34다 5678', '11톤 윙바디',   TRUE),
    (4,  '45라 6789', '11톤 윙바디',   TRUE),
    (5,  '56마 7890', '25톤 트레일러', TRUE),
    (6,  '67바 8901', '25톤 트레일러', TRUE),
    (7,  '78사 9012', '5톤 냉동',      TRUE),
    (8,  '89아 0123', '5톤 냉동',      TRUE),
    (9,  '90자 1234', '11톤 카고',     TRUE),
    (10, '01차 2345', '11톤 카고',     TRUE);

-- ============================================================
-- 5. driver 초기 운전자 10명 + 차량 배정
-- ============================================================
INSERT INTO driver (company_id, name, phone, license_type, vehicle_id) VALUES
    (1,  '김철수', '010-1111-0001', '1종 대형', 1),
    (2,  '이영희', '010-1111-0002', '1종 대형', 2),
    (3,  '박민준', '010-1111-0003', '1종 대형', 3),
    (4,  '최수진', '010-1111-0004', '1종 대형', 4),
    (5,  '정우성', '010-1111-0005', '1종 대형', 5),
    (6,  '강지훈', '010-1111-0006', '1종 대형', 6),
    (7,  '윤서연', '010-1111-0007', '1종 보통', 7),
    (8,  '임현우', '010-1111-0008', '1종 보통', 8),
    (9,  '한지민', '010-1111-0009', '1종 보통', 9),
    (10, '오태양', '010-1111-0010', '1종 대형', 10);

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
-- 6. fatigue_threshold 피로도 임계값 기본 설정
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
