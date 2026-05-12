-- ============================================================
-- logmile - 초기 시드 데이터
-- 버전: v5.0
-- 작성일: 2026.05.12
-- 변경 내용: company 포함 구조 전환, admin 3명/company, vehicle 5대/company, driver 5명/company
-- ============================================================

-- ============================================================
-- 1. company 초기 업체 10개
-- ============================================================
INSERT INTO company (name, address, phone, is_active) VALUES
    ('로그마일운송',   '서울특별시 중구 세종대로 110',             '02-1000-0001', TRUE),
    ('한빛물류',       '서울특별시 송파구 올림픽로 300',           '02-1000-0002', TRUE),
    ('부평카고',       '인천광역시 부평구 부평대로 168',           '032-100-0003', TRUE),
    ('서해로지스',     '인천광역시 연수구 컨벤시아대로 123',       '032-100-0004', TRUE),
    ('경기특송',       '경기도 수원시 팔달구 효원로 241',          '031-100-0005', TRUE),
    ('남부윙바디',     '경기도 성남시 분당구 판교로 242',          '031-100-0006', TRUE),
    ('동해냉동물류',   '강원특별자치도 강릉시 강릉대로 33',        '033-100-0007', TRUE),
    ('충청화물',       '대전광역시 서구 둔산로 100',               '042-100-0008', TRUE),
    ('영남트레일러',   '부산광역시 해운대구 센텀중앙로 97',        '051-100-0009', TRUE),
    ('호남종합운수',   '광주광역시 서구 상무중앙로 7',             '062-100-0010', TRUE);

-- ============================================================
-- 2. admin 계정
-- password: admin1234 (BCrypt 해시)
-- ============================================================

-- 2-1. 최상위 관리자 1명 (SUPER_ADMIN — company_id NULL)
INSERT INTO admin (email, password, name, phone, role, status)
VALUES (
    'admin@logmile.com',
    '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly',
    '최상위관리자',
    '010-0000-0000',
    'ROLE_SUPER_ADMIN',
    'ACTIVE'
);

-- 2-2. 업체별 관리자 3명씩 (총 30명)
-- company 1 : 로그마일운송
INSERT INTO admin (company_id, email, password, name, phone, role, status) VALUES
    (1, 'c1_admin1@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '로그마일 총괄관리자', '010-2001-0001', 'ROLE_ADMIN', 'ACTIVE'),
    (1, 'c1_admin2@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '로그마일 운행관리자', '010-2001-0002', 'ROLE_ADMIN', 'ACTIVE'),
    (1, 'c1_admin3@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '로그마일 안전관리자', '010-2001-0003', 'ROLE_ADMIN', 'ACTIVE');

-- company 2 : 한빛물류
INSERT INTO admin (company_id, email, password, name, phone, role, status) VALUES
    (2, 'c2_admin1@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '한빛물류 총괄관리자', '010-2002-0001', 'ROLE_ADMIN', 'ACTIVE'),
    (2, 'c2_admin2@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '한빛물류 운행관리자', '010-2002-0002', 'ROLE_ADMIN', 'ACTIVE'),
    (2, 'c2_admin3@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '한빛물류 안전관리자', '010-2002-0003', 'ROLE_ADMIN', 'ACTIVE');

-- company 3 : 부평카고
INSERT INTO admin (company_id, email, password, name, phone, role, status) VALUES
    (3, 'c3_admin1@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '부평카고 총괄관리자', '010-2003-0001', 'ROLE_ADMIN', 'ACTIVE'),
    (3, 'c3_admin2@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '부평카고 운행관리자', '010-2003-0002', 'ROLE_ADMIN', 'ACTIVE'),
    (3, 'c3_admin3@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '부평카고 안전관리자', '010-2003-0003', 'ROLE_ADMIN', 'ACTIVE');

-- company 4 : 서해로지스
INSERT INTO admin (company_id, email, password, name, phone, role, status) VALUES
    (4, 'c4_admin1@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '서해로지스 총괄관리자', '010-2004-0001', 'ROLE_ADMIN', 'ACTIVE'),
    (4, 'c4_admin2@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '서해로지스 운행관리자', '010-2004-0002', 'ROLE_ADMIN', 'ACTIVE'),
    (4, 'c4_admin3@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '서해로지스 안전관리자', '010-2004-0003', 'ROLE_ADMIN', 'ACTIVE');

-- company 5 : 경기특송
INSERT INTO admin (company_id, email, password, name, phone, role, status) VALUES
    (5, 'c5_admin1@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '경기특송 총괄관리자', '010-2005-0001', 'ROLE_ADMIN', 'ACTIVE'),
    (5, 'c5_admin2@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '경기특송 운행관리자', '010-2005-0002', 'ROLE_ADMIN', 'ACTIVE'),
    (5, 'c5_admin3@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '경기특송 안전관리자', '010-2005-0003', 'ROLE_ADMIN', 'ACTIVE');

-- company 6 : 남부윙바디
INSERT INTO admin (company_id, email, password, name, phone, role, status) VALUES
    (6, 'c6_admin1@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '남부윙바디 총괄관리자', '010-2006-0001', 'ROLE_ADMIN', 'ACTIVE'),
    (6, 'c6_admin2@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '남부윙바디 운행관리자', '010-2006-0002', 'ROLE_ADMIN', 'ACTIVE'),
    (6, 'c6_admin3@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '남부윙바디 안전관리자', '010-2006-0003', 'ROLE_ADMIN', 'ACTIVE');

-- company 7 : 동해냉동물류
INSERT INTO admin (company_id, email, password, name, phone, role, status) VALUES
    (7, 'c7_admin1@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '동해냉동 총괄관리자', '010-2007-0001', 'ROLE_ADMIN', 'ACTIVE'),
    (7, 'c7_admin2@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '동해냉동 운행관리자', '010-2007-0002', 'ROLE_ADMIN', 'ACTIVE'),
    (7, 'c7_admin3@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '동해냉동 안전관리자', '010-2007-0003', 'ROLE_ADMIN', 'ACTIVE');

-- company 8 : 충청화물
INSERT INTO admin (company_id, email, password, name, phone, role, status) VALUES
    (8, 'c8_admin1@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '충청화물 총괄관리자', '010-2008-0001', 'ROLE_ADMIN', 'ACTIVE'),
    (8, 'c8_admin2@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '충청화물 운행관리자', '010-2008-0002', 'ROLE_ADMIN', 'ACTIVE'),
    (8, 'c8_admin3@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '충청화물 안전관리자', '010-2008-0003', 'ROLE_ADMIN', 'ACTIVE');

-- company 9 : 영남트레일러
INSERT INTO admin (company_id, email, password, name, phone, role, status) VALUES
    (9, 'c9_admin1@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '영남트레일러 총괄관리자', '010-2009-0001', 'ROLE_ADMIN', 'ACTIVE'),
    (9, 'c9_admin2@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '영남트레일러 운행관리자', '010-2009-0002', 'ROLE_ADMIN', 'ACTIVE'),
    (9, 'c9_admin3@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '영남트레일러 안전관리자', '010-2009-0003', 'ROLE_ADMIN', 'ACTIVE');

-- company 10 : 호남종합운수
INSERT INTO admin (company_id, email, password, name, phone, role, status) VALUES
    (10, 'c10_admin1@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '호남종합 총괄관리자', '010-2010-0001', 'ROLE_ADMIN', 'ACTIVE'),
    (10, 'c10_admin2@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '호남종합 운행관리자', '010-2010-0002', 'ROLE_ADMIN', 'ACTIVE'),
    (10, 'c10_admin3@logmile.com', '$2a$10$ZPWzjI5qMP3FOZSLriOE8.AHCt8zdmXYlwhLFTNjFQB0skhb.Ttly', '호남종합 안전관리자', '010-2010-0003', 'ROLE_ADMIN', 'ACTIVE');

-- ============================================================
-- 3. vehicle 초기 차량 50대 (업체당 5대, driver_id 없이 먼저 INSERT)
-- 차종 구성: 5톤 카고, 5톤 냉동, 11톤 윙바디, 11톤 카고, 25톤 트레일러
-- ============================================================

-- company 1 : 로그마일운송 (id 1~5)
INSERT INTO vehicle (company_id, plate_no, type, is_active) VALUES
    (1, '11가 1001', '5톤 카고',       TRUE),
    (1, '11가 1002', '5톤 냉동',       TRUE),
    (1, '11가 1003', '11톤 윙바디',    TRUE),
    (1, '11가 1004', '11톤 카고',      TRUE),
    (1, '11가 1005', '25톤 트레일러',  TRUE);

-- company 2 : 한빛물류 (id 6~10)
INSERT INTO vehicle (company_id, plate_no, type, is_active) VALUES
    (2, '22나 2001', '5톤 카고',       TRUE),
    (2, '22나 2002', '5톤 냉동',       TRUE),
    (2, '22나 2003', '11톤 윙바디',    TRUE),
    (2, '22나 2004', '11톤 카고',      TRUE),
    (2, '22나 2005', '25톤 트레일러',  TRUE);

-- company 3 : 부평카고 (id 11~15)
INSERT INTO vehicle (company_id, plate_no, type, is_active) VALUES
    (3, '33다 3001', '5톤 카고',       TRUE),
    (3, '33다 3002', '5톤 냉동',       TRUE),
    (3, '33다 3003', '11톤 윙바디',    TRUE),
    (3, '33다 3004', '11톤 카고',      TRUE),
    (3, '33다 3005', '25톤 트레일러',  TRUE);

-- company 4 : 서해로지스 (id 16~20)
INSERT INTO vehicle (company_id, plate_no, type, is_active) VALUES
    (4, '44라 4001', '5톤 카고',       TRUE),
    (4, '44라 4002', '5톤 냉동',       TRUE),
    (4, '44라 4003', '11톤 윙바디',    TRUE),
    (4, '44라 4004', '11톤 카고',      TRUE),
    (4, '44라 4005', '25톤 트레일러',  TRUE);

-- company 5 : 경기특송 (id 21~25)
INSERT INTO vehicle (company_id, plate_no, type, is_active) VALUES
    (5, '55마 5001', '5톤 카고',       TRUE),
    (5, '55마 5002', '5톤 냉동',       TRUE),
    (5, '55마 5003', '11톤 윙바디',    TRUE),
    (5, '55마 5004', '11톤 카고',      TRUE),
    (5, '55마 5005', '25톤 트레일러',  TRUE);

-- company 6 : 남부윙바디 (id 26~30)
INSERT INTO vehicle (company_id, plate_no, type, is_active) VALUES
    (6, '66바 6001', '5톤 카고',       TRUE),
    (6, '66바 6002', '5톤 냉동',       TRUE),
    (6, '66바 6003', '11톤 윙바디',    TRUE),
    (6, '66바 6004', '11톤 카고',      TRUE),
    (6, '66바 6005', '25톤 트레일러',  TRUE);

-- company 7 : 동해냉동물류 (id 31~35)
INSERT INTO vehicle (company_id, plate_no, type, is_active) VALUES
    (7, '77사 7001', '5톤 카고',       TRUE),
    (7, '77사 7002', '5톤 냉동',       TRUE),
    (7, '77사 7003', '11톤 윙바디',    TRUE),
    (7, '77사 7004', '11톤 카고',      TRUE),
    (7, '77사 7005', '25톤 트레일러',  TRUE);

-- company 8 : 충청화물 (id 36~40)
INSERT INTO vehicle (company_id, plate_no, type, is_active) VALUES
    (8, '88아 8001', '5톤 카고',       TRUE),
    (8, '88아 8002', '5톤 냉동',       TRUE),
    (8, '88아 8003', '11톤 윙바디',    TRUE),
    (8, '88아 8004', '11톤 카고',      TRUE),
    (8, '88아 8005', '25톤 트레일러',  TRUE);

-- company 9 : 영남트레일러 (id 41~45)
INSERT INTO vehicle (company_id, plate_no, type, is_active) VALUES
    (9, '99자 9001', '5톤 카고',       TRUE),
    (9, '99자 9002', '5톤 냉동',       TRUE),
    (9, '99자 9003', '11톤 윙바디',    TRUE),
    (9, '99자 9004', '11톤 카고',      TRUE),
    (9, '99자 9005', '25톤 트레일러',  TRUE);

-- company 10 : 호남종합운수 (id 46~50)
INSERT INTO vehicle (company_id, plate_no, type, is_active) VALUES
    (10, '00차 0001', '5톤 카고',       TRUE),
    (10, '00차 0002', '5톤 냉동',       TRUE),
    (10, '00차 0003', '11톤 윙바디',    TRUE),
    (10, '00차 0004', '11톤 카고',      TRUE),
    (10, '00차 0005', '25톤 트레일러',  TRUE);

-- ============================================================
-- 4. driver 초기 운전자 50명 + 차량 배정 (업체당 5명)
-- 25톤 트레일러는 1종 대형 필수, 나머지는 혼합
-- ============================================================

-- company 1 : 로그마일운송 (driver id 1~5, vehicle id 1~5)
INSERT INTO driver (company_id, name, phone, license_type, vehicle_id) VALUES
    (1, '김철수', '010-1001-0001', '1종 대형', 1),
    (1, '이영희', '010-1001-0002', '1종 보통', 2),
    (1, '박민준', '010-1001-0003', '1종 대형', 3),
    (1, '최수진', '010-1001-0004', '1종 보통', 4),
    (1, '정우성', '010-1001-0005', '1종 대형', 5);

-- company 2 : 한빛물류 (driver id 6~10, vehicle id 6~10)
INSERT INTO driver (company_id, name, phone, license_type, vehicle_id) VALUES
    (2, '강지훈', '010-1002-0001', '1종 대형', 6),
    (2, '윤서연', '010-1002-0002', '1종 보통', 7),
    (2, '임현우', '010-1002-0003', '1종 대형', 8),
    (2, '한지민', '010-1002-0004', '1종 보통', 9),
    (2, '오태양', '010-1002-0005', '1종 대형', 10);

-- company 3 : 부평카고 (driver id 11~15, vehicle id 11~15)
INSERT INTO driver (company_id, name, phone, license_type, vehicle_id) VALUES
    (3, '류현수', '010-1003-0001', '1종 대형', 11),
    (3, '장미래', '010-1003-0002', '1종 보통', 12),
    (3, '권도현', '010-1003-0003', '1종 대형', 13),
    (3, '문수아', '010-1003-0004', '1종 보통', 14),
    (3, '남기혁', '010-1003-0005', '1종 대형', 15);

-- company 4 : 서해로지스 (driver id 16~20, vehicle id 16~20)
INSERT INTO driver (company_id, name, phone, license_type, vehicle_id) VALUES
    (4, '신유리', '010-1004-0001', '1종 대형', 16),
    (4, '홍길동', '010-1004-0002', '1종 보통', 17),
    (4, '조명수', '010-1004-0003', '1종 대형', 18),
    (4, '배미영', '010-1004-0004', '1종 보통', 19),
    (4, '채준혁', '010-1004-0005', '1종 대형', 20);

-- company 5 : 경기특송 (driver id 21~25, vehicle id 21~25)
INSERT INTO driver (company_id, name, phone, license_type, vehicle_id) VALUES
    (5, '황선우', '010-1005-0001', '1종 대형', 21),
    (5, '서미현', '010-1005-0002', '1종 보통', 22),
    (5, '노진우', '010-1005-0003', '1종 대형', 23),
    (5, '여승표', '010-1005-0004', '1종 보통', 24),
    (5, '마현아', '010-1005-0005', '1종 대형', 25);

-- company 6 : 남부윙바디 (driver id 26~30, vehicle id 26~30)
INSERT INTO driver (company_id, name, phone, license_type, vehicle_id) VALUES
    (6, '구성진', '010-1006-0001', '1종 대형', 26),
    (6, '지민석', '010-1006-0002', '1종 보통', 27),
    (6, '탁성은', '010-1006-0003', '1종 대형', 28),
    (6, '함재원', '010-1006-0004', '1종 보통', 29),
    (6, '엄기훈', '010-1006-0005', '1종 대형', 30);

-- company 7 : 동해냉동물류 (driver id 31~35, vehicle id 31~35)
INSERT INTO driver (company_id, name, phone, license_type, vehicle_id) VALUES
    (7, '심재영', '010-1007-0001', '1종 대형', 31),
    (7, '우상혁', '010-1007-0002', '1종 보통', 32),
    (7, '선우영', '010-1007-0003', '1종 대형', 33),
    (7, '방준식', '010-1007-0004', '1종 보통', 34),
    (7, '제갈민', '010-1007-0005', '1종 대형', 35);

-- company 8 : 충청화물 (driver id 36~40, vehicle id 36~40)
INSERT INTO driver (company_id, name, phone, license_type, vehicle_id) VALUES
    (8, '독고진', '010-1008-0001', '1종 대형', 36),
    (8, '사공민', '010-1008-0002', '1종 보통', 37),
    (8, '어준혁', '010-1008-0003', '1종 대형', 38),
    (8, '모상윤', '010-1008-0004', '1종 보통', 39),
    (8, '갈성윤', '010-1008-0005', '1종 대형', 40);

-- company 9 : 영남트레일러 (driver id 41~45, vehicle id 41~45)
INSERT INTO driver (company_id, name, phone, license_type, vehicle_id) VALUES
    (9, '안성현', '010-1009-0001', '1종 대형', 41),
    (9, '경수진', '010-1009-0002', '1종 보통', 42),
    (9, '피태영', '010-1009-0003', '1종 대형', 43),
    (9, '금민서', '010-1009-0004', '1종 보통', 44),
    (9, '류진수', '010-1009-0005', '1종 대형', 45);

-- company 10 : 호남종합운수 (driver id 46~50, vehicle id 46~50)
INSERT INTO driver (company_id, name, phone, license_type, vehicle_id) VALUES
    (10, '봉준호', '010-1010-0001', '1종 대형', 46),
    (10, '하율미', '010-1010-0002', '1종 보통', 47),
    (10, '조성현', '010-1010-0003', '1종 대형', 48),
    (10, '차민영', '010-1010-0004', '1종 보통', 49),
    (10, '염세준', '010-1010-0005', '1종 대형', 50);

-- vehicle.driver_id 역방향 업데이트 (순환 FK 완성)
UPDATE vehicle SET driver_id =  1 WHERE id =  1;
UPDATE vehicle SET driver_id =  2 WHERE id =  2;
UPDATE vehicle SET driver_id =  3 WHERE id =  3;
UPDATE vehicle SET driver_id =  4 WHERE id =  4;
UPDATE vehicle SET driver_id =  5 WHERE id =  5;
UPDATE vehicle SET driver_id =  6 WHERE id =  6;
UPDATE vehicle SET driver_id =  7 WHERE id =  7;
UPDATE vehicle SET driver_id =  8 WHERE id =  8;
UPDATE vehicle SET driver_id =  9 WHERE id =  9;
UPDATE vehicle SET driver_id = 10 WHERE id = 10;
UPDATE vehicle SET driver_id = 11 WHERE id = 11;
UPDATE vehicle SET driver_id = 12 WHERE id = 12;
UPDATE vehicle SET driver_id = 13 WHERE id = 13;
UPDATE vehicle SET driver_id = 14 WHERE id = 14;
UPDATE vehicle SET driver_id = 15 WHERE id = 15;
UPDATE vehicle SET driver_id = 16 WHERE id = 16;
UPDATE vehicle SET driver_id = 17 WHERE id = 17;
UPDATE vehicle SET driver_id = 18 WHERE id = 18;
UPDATE vehicle SET driver_id = 19 WHERE id = 19;
UPDATE vehicle SET driver_id = 20 WHERE id = 20;
UPDATE vehicle SET driver_id = 21 WHERE id = 21;
UPDATE vehicle SET driver_id = 22 WHERE id = 22;
UPDATE vehicle SET driver_id = 23 WHERE id = 23;
UPDATE vehicle SET driver_id = 24 WHERE id = 24;
UPDATE vehicle SET driver_id = 25 WHERE id = 25;
UPDATE vehicle SET driver_id = 26 WHERE id = 26;
UPDATE vehicle SET driver_id = 27 WHERE id = 27;
UPDATE vehicle SET driver_id = 28 WHERE id = 28;
UPDATE vehicle SET driver_id = 29 WHERE id = 29;
UPDATE vehicle SET driver_id = 30 WHERE id = 30;
UPDATE vehicle SET driver_id = 31 WHERE id = 31;
UPDATE vehicle SET driver_id = 32 WHERE id = 32;
UPDATE vehicle SET driver_id = 33 WHERE id = 33;
UPDATE vehicle SET driver_id = 34 WHERE id = 34;
UPDATE vehicle SET driver_id = 35 WHERE id = 35;
UPDATE vehicle SET driver_id = 36 WHERE id = 36;
UPDATE vehicle SET driver_id = 37 WHERE id = 37;
UPDATE vehicle SET driver_id = 38 WHERE id = 38;
UPDATE vehicle SET driver_id = 39 WHERE id = 39;
UPDATE vehicle SET driver_id = 40 WHERE id = 40;
UPDATE vehicle SET driver_id = 41 WHERE id = 41;
UPDATE vehicle SET driver_id = 42 WHERE id = 42;
UPDATE vehicle SET driver_id = 43 WHERE id = 43;
UPDATE vehicle SET driver_id = 44 WHERE id = 44;
UPDATE vehicle SET driver_id = 45 WHERE id = 45;
UPDATE vehicle SET driver_id = 46 WHERE id = 46;
UPDATE vehicle SET driver_id = 47 WHERE id = 47;
UPDATE vehicle SET driver_id = 48 WHERE id = 48;
UPDATE vehicle SET driver_id = 49 WHERE id = 49;
UPDATE vehicle SET driver_id = 50 WHERE id = 50;

-- ============================================================
-- 5. fatigue_threshold 피로도 임계값 기본 설정 (21건 고정)
-- FatigueScoreService 하드코딩 값과 동일 — 관리자 화면에서 수정 가능
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

-- 휴식 부족 점수
INSERT INTO fatigue_threshold (threshold_key, threshold_value, description) VALUES
    ('REST_VIOLATION_ONCE_SCORE',  10,  '휴식 누락 1회 → +10점'),
    ('REST_VIOLATION_TWICE_SCORE', 25,  '휴식 누락 2회 이상 → +25점');

-- 휴식 보정 점수
INSERT INTO fatigue_threshold (threshold_key, threshold_value, description) VALUES
    ('REST_CORRECTION_VALID_SCORE',      10,  '유효 휴식(15분 이상) 보정 → -10점'),
    ('REST_CORRECTION_SUFFICIENT_SCORE', 20,  '충분 휴식(30분 이상) 보정 → -20점');

-- 피로 등급 점수 기준
INSERT INTO fatigue_threshold (threshold_key, threshold_value, description) VALUES
    ('LEVEL_NORMAL_MAX',  39,  '정상 등급 최대 점수 (0~39)'),
    ('LEVEL_CAUTION_MIN', 40,  '주의 등급 최소 점수 (40~69)'),
    ('LEVEL_CAUTION_MAX', 69,  '주의 등급 최대 점수 (40~69)'),
    ('LEVEL_DANGER_MIN',  70,  '위험 등급 최소 점수 (70 이상)');
