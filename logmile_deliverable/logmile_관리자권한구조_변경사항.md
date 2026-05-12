# 관리자 권한 구조 변경 사항

## logmile - 최상위 관리자 / 업체 관리자 구조 반영

- 프로젝트명: `logmile`
- 문서 버전: `v1.0`
- 작성 기준일: 2026.04.29
- 기준 산출물:
  - `logmile_요구사항정의서.md`
  - `logmile_DB설계서.md`
  - `logmile_프로젝트구조도.md`
  - `logmile_infra/db/init.sql`
  - `logmile_infra/db/seed.sql`
- 변경 목적: 기존 단일 `ROLE_ADMIN` 구조를 최상위 관리자와 업체 관리자 구조로 확장

---

## 1. 현재 산출물 기준 구조

현재 산출물은 단일 관리자 권한 구조를 기준으로 작성되어 있다.

| 항목 | 현재 내용 |
|---|---|
| 관리자 권한 | `ROLE_ADMIN` |
| 관리자 테이블 | `admin` 단일 테이블 |
| 업체 테이블 | 없음 |
| 회원가입 | 없음 |
| 가입 승인 | 없음 |
| 데이터 소유 범위 | 전체 차량/운전자/운행 데이터를 하나의 관리 범위로 사용 |
| 차량/운전자/운행 데이터 분리 | 업체 기준 분리 없음 |

현재 구조는 학습용 단일 관제 관리자 시스템에는 적합하지만, 여러 업체가 가입하여 각자 자신의 차량과 운전자를 관리하는 구조에는 부족하다.

---

## 2. 변경 후 목표 구조

변경 후 구조는 다음 두 권한을 기준으로 한다.

| 사용자 유형 | 권한 | 설명 |
|---|---|---|
| 최상위 관리자 | `ROLE_SUPER_ADMIN` | logmile 서비스 운영자. 전체 업체와 업체 관리자 계정을 승인/관리 |
| 업체 관리자 | `ROLE_COMPANY_ADMIN` | 서비스를 직접 사용하는 업체 담당자. 자기 업체의 차량, 운전자, 운행 데이터만 관리 |

업체 관리자는 회원가입 후 바로 서비스를 이용하지 않고, 최상위 관리자 승인 후 이용 가능하다.

```text
업체 관리자 회원가입
→ PENDING 상태 저장
→ 최상위 관리자 가입 요청 확인
→ 승인 시 ACTIVE
→ 업체 관리자 로그인 가능
```

---

## 3. 요구사항 정의서 수정 사항

### 3.1 사용자 및 권한 수정

기존:

| 사용자 | 권한 | 주요 기능 |
|---|---|---|
| 관제 관리자 | `ROLE_ADMIN` | 대시보드 조회, 차량/운전자 관리, 운행 이력 조회, 피로도 임계값 관리 |
| 시스템 관리자 | `ROLE_ADMIN` | 관리자 인증, 시스템 설정, API 문서 확인 |

변경:

| 사용자 | 권한 | 주요 기능 |
|---|---|---|
| 최상위 관리자 | `ROLE_SUPER_ADMIN` | 업체 가입 요청 승인/거절, 업체 목록 조회, 업체 관리자 정지/해제, 전체 운영 현황 조회 |
| 업체 관리자 | `ROLE_COMPANY_ADMIN` | 자기 업체 차량/운전자 관리, 시뮬레이션 실행, 대시보드 조회, 운행 이력/통계 조회, 피로도 임계값 관리 |

### 3.2 추가 기능 요구사항

| ID | 기능 | 요구사항 | 권한 |
|---|---|---|---|
| FR-AUTH01 | 로그인 | 이메일/비밀번호 기반 JWT 로그인을 제공한다. | 공통 |
| FR-AUTH02 | 업체 관리자 회원가입 | 업체 정보와 담당자 정보를 입력하여 가입 신청할 수 있다. | 비인증 |
| FR-AUTH03 | 가입 승인 대기 | 회원가입 직후 계정 상태는 `PENDING`이며 로그인할 수 없다. | 비인증 |
| FR-SUPER01 | 가입 요청 목록 조회 | 최상위 관리자는 승인 대기 중인 업체 관리자 목록을 조회할 수 있다. | 최상위 관리자 |
| FR-SUPER02 | 가입 승인 | 최상위 관리자는 업체 관리자 가입 요청을 승인할 수 있다. | 최상위 관리자 |
| FR-SUPER03 | 가입 거절 | 최상위 관리자는 업체 관리자 가입 요청을 거절할 수 있다. | 최상위 관리자 |
| FR-SUPER04 | 업체 관리자 정지/해제 | 최상위 관리자는 업체 관리자 계정을 정지하거나 해제할 수 있다. | 최상위 관리자 |
| FR-COMPANY01 | 내 업체 정보 조회 | 업체 관리자는 자기 업체 정보를 조회할 수 있다. | 업체 관리자 |
| FR-COMPANY02 | 업체별 데이터 접근 제한 | 업체 관리자는 자기 업체의 차량, 운전자, 운행 데이터만 조회/수정할 수 있다. | 업체 관리자 |

### 3.3 인증 요구사항 수정

기존:

```text
JWT 기반 ROLE_ADMIN 인증을 제공한다.
```

변경:

```text
JWT 기반 인증을 제공하며, 권한은 ROLE_SUPER_ADMIN과 ROLE_COMPANY_ADMIN으로 구분한다.
계정 상태가 ACTIVE인 관리자만 로그인할 수 있다.
```

---

## 4. DB 설계서 수정 사항

### 4.1 신규 테이블 추가

#### company

업체 정보를 저장한다.

| 컬럼명 | 타입 | NULL | 설명 |
|---|---|---|---|
| `id` | `BIGSERIAL` | NOT NULL | 업체 식별자 |
| `name` | `VARCHAR(100)` | NOT NULL | 업체명 |
| `business_no` | `VARCHAR(30)` | NULL | 사업자등록번호 |
| `phone` | `VARCHAR(20)` | NULL | 업체 연락처 |
| `address` | `VARCHAR(255)` | NULL | 업체 주소 |
| `status` | `VARCHAR(20)` | NOT NULL | 업체 상태 |
| `created_at` | `TIMESTAMP` | NOT NULL | 생성 시각 |
| `updated_at` | `TIMESTAMP` | NULL | 수정 시각 |

권장 상태값:

```text
ACTIVE
SUSPENDED
```

### 4.2 admin 테이블 변경

기존 `admin`은 단일 관리자 계정만 표현한다. 업체 관리자 가입/승인 구조를 위해 아래 컬럼을 추가한다.

| 컬럼명 | 타입 | NULL | 설명 |
|---|---|---|---|
| `company_id` | `BIGINT` | NULL | 업체 관리자 소속 업체 ID. 최상위 관리자는 NULL 가능 |
| `password_hash` | `VARCHAR(255)` | NOT NULL | 기존 `password` 컬럼명 변경 권장 |
| `role` | `VARCHAR(30)` | NOT NULL | `ROLE_SUPER_ADMIN`, `ROLE_COMPANY_ADMIN` |
| `status` | `VARCHAR(20)` | NOT NULL | 계정 상태 |
| `approved_at` | `TIMESTAMP` | NULL | 승인 시각 |
| `approved_by` | `BIGINT` | NULL | 승인한 최상위 관리자 ID |
| `updated_at` | `TIMESTAMP` | NULL | 수정 시각 |

관리자 상태값:

```text
PENDING
ACTIVE
REJECTED
SUSPENDED
```

### 4.3 기존 업무 테이블 변경

업체별 데이터 분리를 위해 주요 업무 테이블에 `company_id`를 추가한다.

| 테이블 | 추가 컬럼 | 이유 |
|---|---|---|
| `vehicle` | `company_id BIGINT NOT NULL` | 업체별 차량 분리 |
| `driver` | `company_id BIGINT NOT NULL` | 업체별 운전자 분리 |
| `drive_log` | `company_id BIGINT NOT NULL` | 업체별 운행 이력 분리 |

선택 검토:

| 테이블 | 추가 여부 | 설명 |
|---|---|---|
| `fatigue_threshold` | `company_id` 선택 | 업체별 피로도 임계값을 다르게 운영하려면 추가 |
| `gps_data` | 추가 비권장 | `drive_log`를 통해 업체 추적 가능 |
| `rest_event` | 추가 비권장 | `drive_log`를 통해 업체 추적 가능 |
| `fatigue_event` | 추가 비권장 | `drive_log`를 통해 업체 추적 가능 |

### 4.4 관계 변경

```text
company 1:N admin
company 1:N vehicle
company 1:N driver
company 1:N drive_log
admin.approved_by N:1 admin.id
```

업체 관리자의 모든 차량/운전자/운행 조회는 JWT의 `companyId`를 기준으로 필터링한다.

---

## 5. DDL 수정 방향

### 5.1 company 테이블 생성

```sql
CREATE TABLE IF NOT EXISTS company (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    business_no  VARCHAR(30),
    phone        VARCHAR(20),
    address      VARCHAR(255),
    status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP,
    CONSTRAINT chk_company_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED'))
);
```

### 5.2 admin 테이블 수정 방향

```sql
CREATE TABLE IF NOT EXISTS admin (
    id             BIGSERIAL PRIMARY KEY,
    company_id     BIGINT,
    email          VARCHAR(100) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    name           VARCHAR(50) NOT NULL,
    phone          VARCHAR(20),
    role           VARCHAR(30) NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_at    TIMESTAMP,
    approved_by    BIGINT,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP,
    CONSTRAINT fk_admin_company
        FOREIGN KEY (company_id) REFERENCES company (id),
    CONSTRAINT fk_admin_approved_by
        FOREIGN KEY (approved_by) REFERENCES admin (id),
    CONSTRAINT chk_admin_role
        CHECK (role IN ('ROLE_SUPER_ADMIN', 'ROLE_COMPANY_ADMIN')),
    CONSTRAINT chk_admin_status
        CHECK (status IN ('PENDING', 'ACTIVE', 'REJECTED', 'SUSPENDED'))
);
```

### 5.3 업무 테이블 company_id 추가

```sql
ALTER TABLE vehicle
    ADD COLUMN company_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_vehicle_company
        FOREIGN KEY (company_id) REFERENCES company (id);

ALTER TABLE driver
    ADD COLUMN company_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_driver_company
        FOREIGN KEY (company_id) REFERENCES company (id);

ALTER TABLE drive_log
    ADD COLUMN company_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_drive_log_company
        FOREIGN KEY (company_id) REFERENCES company (id);
```

### 5.4 인덱스 추가

```sql
CREATE INDEX idx_admin_company_id ON admin (company_id);
CREATE INDEX idx_admin_status ON admin (status);
CREATE INDEX idx_vehicle_company_id ON vehicle (company_id);
CREATE INDEX idx_driver_company_id ON driver (company_id);
CREATE INDEX idx_drive_log_company_status_started
    ON drive_log (company_id, status, started_at);
```

---

## 6. seed.sql 수정 사항

기존:

```text
admin 1건
vehicle 10건
driver 10건
fatigue_threshold 21건
```

변경:

```text
company 1~2건
ROLE_SUPER_ADMIN 1건
ROLE_COMPANY_ADMIN 1~2건
vehicle 10건에 company_id 포함
driver 10건에 company_id 포함
fatigue_threshold 21건
```

예시:

```sql
INSERT INTO company (id, name, business_no, phone, address, status)
VALUES (1, '대한물류', '123-45-67890', '02-1234-5678', '서울시 강남구', 'ACTIVE');

INSERT INTO admin (
    email, password_hash, name, phone, role, status
) VALUES (
    'super@logmile.com',
    '{BCrypt_HASH}',
    '최상위관리자',
    '010-0000-0000',
    'ROLE_SUPER_ADMIN',
    'ACTIVE'
);

INSERT INTO admin (
    company_id, email, password_hash, name, phone, role, status, approved_at, approved_by
) VALUES (
    1,
    'admin@daehanlogistics.com',
    '{BCrypt_HASH}',
    '업체관리자',
    '010-1111-1111',
    'ROLE_COMPANY_ADMIN',
    'ACTIVE',
    NOW(),
    1
);
```

---

## 7. API 수정 사항

### 7.1 인증 API

| 메서드 | 경로 | 기능 | 인증 |
|---|---|---|---|
| POST | `/api/auth/signup` | 업체 관리자 회원가입 신청 | 불필요 |
| POST | `/api/auth/login` | 로그인 및 JWT 발급 | 불필요 |

로그인 정책:

```text
PENDING   → 로그인 불가, 승인 대기 메시지
ACTIVE    → 로그인 가능
REJECTED  → 로그인 불가, 거절 메시지
SUSPENDED → 로그인 불가, 정지 메시지
```

### 7.2 최상위 관리자 API

| 메서드 | 경로 | 기능 | 권한 |
|---|---|---|---|
| GET | `/api/super/admins/pending` | 가입 승인 대기 목록 조회 | `ROLE_SUPER_ADMIN` |
| PATCH | `/api/super/admins/{adminId}/approve` | 가입 승인 | `ROLE_SUPER_ADMIN` |
| PATCH | `/api/super/admins/{adminId}/reject` | 가입 거절 | `ROLE_SUPER_ADMIN` |
| PATCH | `/api/super/admins/{adminId}/suspend` | 업체 관리자 정지 | `ROLE_SUPER_ADMIN` |
| PATCH | `/api/super/admins/{adminId}/activate` | 업체 관리자 정지 해제 | `ROLE_SUPER_ADMIN` |
| GET | `/api/super/companies` | 업체 목록 조회 | `ROLE_SUPER_ADMIN` |

### 7.3 업체 관리자 API

| 메서드 | 경로 | 기능 | 권한 |
|---|---|---|---|
| GET | `/api/company/me` | 내 업체 정보 조회 | `ROLE_COMPANY_ADMIN` |
| GET | `/api/vehicles` | 자기 업체 차량 목록 조회 | `ROLE_COMPANY_ADMIN` |
| GET | `/api/drivers` | 자기 업체 운전자 목록 조회 | `ROLE_COMPANY_ADMIN` |
| GET | `/api/drive-logs` | 자기 업체 운행 이력 조회 | `ROLE_COMPANY_ADMIN` |

---

## 8. 백엔드 구조 수정 사항

기존 구조에 아래 모듈을 추가한다.

```text
controller/
├── AuthController.java
├── SignupController.java
├── SuperAdminController.java
└── CompanyController.java

service/
├── AdminService.java
├── CompanyService.java
├── ApprovalService.java
└── TenantAccessService.java

repository/
├── AdminRepository.java
└── CompanyRepository.java

entity/
├── Admin.java
└── Company.java

common/enums/
├── AdminRole.java
├── AdminStatus.java
└── CompanyStatus.java
```

JWT에는 아래 정보를 포함하는 것을 권장한다.

```json
{
  "adminId": 1,
  "companyId": 1,
  "role": "ROLE_COMPANY_ADMIN"
}
```

최상위 관리자는 `companyId`가 `null`일 수 있다.

---

## 9. 프론트엔드 수정 사항

### 9.1 추가 화면

| 화면 | 설명 |
|---|---|
| 회원가입 화면 | 업체 정보와 담당자 정보 입력 |
| 승인 대기 안내 화면 | 가입 신청 후 승인 대기 상태 안내 |
| 최상위 관리자 대시보드 | 업체 가입 요청, 업체 목록, 관리자 계정 관리 |
| 업체 정보 화면 | 업체 관리자의 자기 업체 정보 확인 |

### 9.2 라우팅 권한

```text
/signup                  → 비인증 접근 가능
/login                   → 비인증 접근 가능
/super/**                → ROLE_SUPER_ADMIN
/dashboard               → ROLE_COMPANY_ADMIN
/vehicles                → ROLE_COMPANY_ADMIN
/drivers                 → ROLE_COMPANY_ADMIN
/simulation              → ROLE_COMPANY_ADMIN
/drive-logs              → ROLE_COMPANY_ADMIN
/stats                   → ROLE_COMPANY_ADMIN
/thresholds              → ROLE_COMPANY_ADMIN
```

### 9.3 Pinia authStore 변경

```text
accessToken
adminId
companyId
role
status
adminName
isSuperAdmin
isCompanyAdmin
```

---

## 10. 산출물별 반영 우선순위

| 순서 | 산출물 | 수정 내용 |
|---:|---|---|
| 1 | 요구사항 정의서 | 사용자/권한, 회원가입, 승인 기능 추가 |
| 2 | DB 설계서 | `company`, `admin` 확장, `company_id` 반영 |
| 3 | 프로젝트 구조도 | 백엔드/프론트엔드 모듈 및 API 구조 수정 |
| 4 | init.sql | 신규 테이블/컬럼/FK/인덱스 반영 |
| 5 | seed.sql | 최상위 관리자, 업체, 업체 관리자 seed 반영 |
| 6 | 브랜치 전략/기능 순서 | `be-company`, `be-admin-approval`, `fe-signup`, `fe-super-admin` 작업 추가 |

---

## 11. 구현 우선순위

```text
1. 산출물 수정
2. DB DDL 수정
3. Company/Admin Entity 수정
4. 회원가입 API 구현
5. 최상위 관리자 승인/거절 API 구현
6. JWT에 role/companyId 포함
7. 차량/운전자/운행 조회 company_id 필터링
8. 프론트 회원가입/승인대기/최상위 관리자 화면 구현
9. 기존 대시보드와 시뮬레이션 화면에 업체 범위 적용
```

---

## 12. 검토 포인트

- 최상위 관리자가 업체별 운행 데이터를 직접 조회할 수 있게 할지 여부
- 피로도 임계값을 전체 공통으로 둘지, 업체별로 따로 둘지 여부
- 사업자등록번호를 필수 입력으로 둘지 여부
- 가입 승인 시 알림 기능을 구현할지 여부
- 업체 관리자 추가 초대 기능을 1차 범위에 포함할지 여부

---

## 13. 결론

현재 산출물은 단일 `ROLE_ADMIN` 구조이므로, 최상위 관리자와 업체 관리자 구조를 적용하려면 요구사항, DB, API, 백엔드 구조, 프론트 화면을 함께 수정해야 한다.

가장 중요한 변경점은 `company` 테이블 추가와 `admin.role/status/company_id` 확장, 그리고 차량/운전자/운행 데이터의 `company_id` 기반 접근 제한이다.
