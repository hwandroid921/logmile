# logmile v0.1.1 구현 범위

> 버전: v0.1.1  
> 목표: 인증 / 회원가입 / 승인 구조 통합  
> 기간: 2026.05.05 ~ 2026.05.08  
> 기준 산출물: `logmile_릴리즈전략.md`, `logmile_팀원별_기능구현_브랜치순서.md`, `logmile_요구사항정의서.md`

---

## 1. v0.1.1 착수 전 처리 사항

v0.1.1 브랜치 작업 시작 전에 아래 항목을 먼저 처리한다.

### 미커밋 (로컬)

| 파일 | 내용 | 커밋 브랜치 |
|------|------|------------|
| `.gitignore` | IDE(.idea/ 등) + 데이터셋 경로 추가 | `feature/be-company-entity` |
| `logmile_ai/dataset/.gitignore` | test split + 원본 경로 추가 | `feature/be-company-entity` |
| `logmile_ui_prototype/page_flow.html` | 페이지 흐름도 신규 파일 | `feature/docs-deliverables` |

### PR 미생성 브랜치 (dev 머지 필요)

| 브랜치 | 커밋 내용 |
|--------|---------|
| `feature/be-company-entity` | CompanyService (register / findById / findAll / getEntityById) |
| `feature/be-auth-jwt` | JWT 인프라, SecurityConfig, ErrorCode, BusinessException |
| `feature/be-swagger` | Springdoc Swagger 설정, AuthController 스텁, LoginRequest/Response DTO |
| `feature/docs-deliverables` | admin status CHECK 오류 수정, ROLE_ADMIN 명칭 통일 |

---

## 2. v0.1.1 브랜치 구성

| 브랜치 | 담당 | 선행 조건 |
|--------|------|---------|
| `feature/be-auth-signup` | 환희 | be-company-entity, be-auth-jwt dev 머지 후 |
| `feature/be-admin-approval` | 환희 | be-auth-signup 머지 후 |
| `feature/fe-auth-login` | 경서 | be-auth-signup dev 머지 후 |
| `feature/fe-signup` | 경서 | be-auth-signup dev 머지 후 |
| `feature/fe-approval-pending` | 경서 | fe-auth-login 머지 후 |
| `feature/fe-router-guard` | 경서 | fe-approval-pending 머지 후 |

---

## 3. BE — feature/be-auth-signup

### 3.1 구현 목표

- `POST /api/auth/signup` — 업체 생성 + 일반 관리자 계정 PENDING 등록 (FR-AUTH02)
- `POST /api/auth/login` 실제 구현 — AuthController 스텁 교체 (FR-AUTH01)

### 3.2 Admin 엔티티 보완

`Admin.java`에 팩토리 메서드와 상태 변경 메서드 추가

```java
// 회원가입용 팩토리
public static Admin create(String email, String encodedPassword,
                           String name, String phone, Company company) {
    Admin admin = new Admin();
    admin.email    = email;
    admin.password = encodedPassword;
    admin.name     = name;
    admin.phone    = phone;
    admin.company  = company;
    admin.role     = AdminRole.ROLE_ADMIN;
    admin.status   = AdminStatus.PENDING;   // 신규 가입은 반드시 PENDING
    return admin;
}

// 승인 처리용 (be-admin-approval에서 사용)
public void approve()  { this.status = AdminStatus.ACTIVE;    }
public void reject()   { this.status = AdminStatus.REJECTED;  }
public void suspend()  { this.status = AdminStatus.SUSPENDED; }
public void unsuspend(){ this.status = AdminStatus.ACTIVE;    }
```

현재 엔티티 상태 기본값이 `ACTIVE`로 설정되어 있으므로 `create()` 팩토리에서 반드시 `PENDING`으로 명시한다.

### 3.3 CompanyService 예외 교체

be-auth-jwt 머지 후 `BusinessException` 사용 가능 → be-auth-signup에서 수정

```java
// 변경 전 (be-company-entity 현재 상태)
throw new IllegalArgumentException("이미 등록된 업체명입니다: " + request.name());
throw new IllegalArgumentException("업체를 찾을 수 없습니다: " + id);

// 변경 후
throw new BusinessException(ErrorCode.DUPLICATE_COMPANY_NAME);
throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
```

### 3.4 SignupRequest / SignupResponse DTO

```
SignupRequest
  - companyName  : String  (업체명, @NotBlank)
  - address      : String  (업체 주소)
  - phone        : String  (업체 연락처)
  - email        : String  (관리자 이메일, @Email @NotBlank)
  - password     : String  (패스워드, @NotBlank)
  - name         : String  (관리자 이름, @NotBlank)
  - adminPhone   : String  (관리자 연락처)

SignupResponse
  - adminId    : Long
  - email      : String
  - name       : String
  - status     : AdminStatus   (PENDING)
  - companyId  : Long
  - companyName: String
```

### 3.5 AuthService 구현

```
signup(SignupRequest):
  1. companyRepository.existsByName() → DUPLICATE_COMPANY_NAME 예외
  2. adminRepository.existsByEmail() → DUPLICATE_EMAIL 예외
  3. Company.create() → save
  4. Admin.create(email, BCrypt(password), name, phone, company) → save
  5. SignupResponse 반환

login(LoginRequest):
  1. adminRepository.findByEmail() → ADMIN_NOT_FOUND 예외
  2. passwordEncoder.matches() → UNAUTHORIZED 예외
  3. switch(admin.status):
       PENDING    → ACCOUNT_PENDING 예외
       REJECTED   → ACCOUNT_REJECTED 예외
       SUSPENDED  → ACCOUNT_SUSPENDED 예외
       INACTIVE   → ACCOUNT_INACTIVE 예외
  4. JwtTokenProvider.createToken(adminId, email, role)
  5. LoginResponse.of(token, adminId, name, role, status, companyId)
```

### 3.6 API 명세

| 메서드 | 경로 | 인증 | 요청 | 응답 |
|--------|------|------|------|------|
| POST | `/api/auth/signup` | 불필요 | `SignupRequest` | `201 SignupResponse` |
| POST | `/api/auth/login` | 불필요 | `LoginRequest` | `200 LoginResponse` |

**오류 응답**

| 상황 | HTTP | ErrorCode |
|------|------|-----------|
| 이메일 중복 | 409 | `DUPLICATE_EMAIL` |
| 업체명 중복 | 409 | `DUPLICATE_COMPANY_NAME` |
| 이메일/패스워드 불일치 | 401 | `UNAUTHORIZED` |
| PENDING 계정 | 403 | `ACCOUNT_PENDING` |
| REJECTED 계정 | 403 | `ACCOUNT_REJECTED` |
| SUSPENDED 계정 | 403 | `ACCOUNT_SUSPENDED` |

---

## 4. BE — feature/be-admin-approval

### 4.1 구현 목표

최상위 관리자(ROLE_SUPER_ADMIN)의 일반 관리자 승인/거절/정지/해제 API (FR-SUPER01~04)

### 4.2 API 명세

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| GET | `/api/admin/approvals/pending` | SUPER_ADMIN | PENDING 상태 관리자 목록 조회 (FR-SUPER01) |
| PATCH | `/api/admin/approvals/{id}/approve` | SUPER_ADMIN | 승인 → ACTIVE (FR-SUPER02) |
| PATCH | `/api/admin/approvals/{id}/reject` | SUPER_ADMIN | 거절 → REJECTED (FR-SUPER03) |
| PATCH | `/api/admin/approvals/{id}/suspend` | SUPER_ADMIN | 정지 → SUSPENDED (FR-SUPER04) |
| PATCH | `/api/admin/approvals/{id}/unsuspend` | SUPER_ADMIN | 정지 해제 → ACTIVE (FR-SUPER04) |

### 4.3 AdminApprovalService 구현

```
getPendingAdmins():
  adminRepository.findByStatus(PENDING) → List<AdminSummaryResponse>

approve(id):
  admin = findById → ADMIN_NOT_FOUND 예외
  admin.getStatus() != PENDING → INVALID_INPUT 예외
  admin.approve()

reject(id):
  admin = findById → ADMIN_NOT_FOUND 예외
  admin.reject()

suspend(id):
  admin = findById → ADMIN_NOT_FOUND 예외
  admin.isSuperAdmin() → ACCESS_DENIED 예외 (최상위 관리자 정지 불가)
  admin.suspend()

unsuspend(id):
  admin = findById → ADMIN_NOT_FOUND 예외
  admin.unsuspend()
```

### 4.4 AdminSummaryResponse DTO

```
AdminSummaryResponse
  - adminId    : Long
  - email      : String
  - name       : String
  - status     : AdminStatus
  - companyId  : Long
  - companyName: String
  - createdAt  : LocalDateTime
```

---

## 5. FE — feature/fe-auth-login

### 5.1 구현 목표

`LoginView.vue` 실제 구현 (현재 스텁), authStore role/status 확장, 로그인 후 권한별 redirect

### 5.2 authStore 확장

현재 `token`, `name`만 저장 → role, status, adminId, companyId 추가 필요

```js
// 추가할 상태
const adminId   = ref(localStorage.getItem('adminId') || null)
const role      = ref(localStorage.getItem('role') || null)
const status    = ref(localStorage.getItem('status') || null)
const companyId = ref(localStorage.getItem('companyId') || null)

// computed
const isSuperAdmin = computed(() => role.value === 'ROLE_SUPER_ADMIN')
const isActive     = computed(() => status.value === 'ACTIVE')

// setAuth 확장
function setAuth(res) {
  token.value     = res.accessToken
  name.value      = res.name
  adminId.value   = res.adminId
  role.value      = res.role
  status.value    = res.status
  companyId.value = res.companyId
  // localStorage 저장
}
```

### 5.3 LoginView.vue 구현 내용

```
- 이메일 / 패스워드 입력 폼
- POST /api/auth/login 호출
- 성공 시:
    ROLE_SUPER_ADMIN → /super-admin (최상위 관리자 홈)
    ROLE_ADMIN + ACTIVE → / (대시보드)
- 오류 처리:
    401 UNAUTHORIZED     → "이메일 또는 패스워드가 올바르지 않습니다."
    403 ACCOUNT_PENDING  → /pending (승인 대기 안내)
    403 ACCOUNT_REJECTED → "가입이 거절된 계정입니다."
    403 ACCOUNT_SUSPENDED → "정지된 계정입니다. 관리자에게 문의하세요."
```

### 5.4 api/auth.js

```js
export const login  = (data) => axios.post('/api/auth/login', data)
export const signup = (data) => axios.post('/api/auth/signup', data)
```

---

## 6. FE — feature/fe-signup

### 6.1 구현 목표

일반 관리자 회원가입 화면 (FR-AUTH02)

### 6.2 SignupView.vue 구현 내용

```
신규 파일: src/views/SignupView.vue

입력 필드:
  - 업체명 (companyName)
  - 업체 주소 (address)
  - 업체 연락처 (phone)
  - 관리자 이름 (name)
  - 관리자 연락처 (adminPhone)
  - 이메일 (email)
  - 패스워드 (password)

성공 시: /pending 이동 (승인 대기 안내)
오류 처리:
  409 DUPLICATE_EMAIL        → "이미 사용 중인 이메일입니다."
  409 DUPLICATE_COMPANY_NAME → "이미 등록된 업체명입니다."
```

### 6.3 라우터 등록

```js
{
  path: '/signup',
  name: 'signup',
  component: () => import('@/views/SignupView.vue'),
}
```

---

## 7. FE — feature/fe-approval-pending

### 7.1 구현 목표

PENDING 상태 계정의 로그인 후 안내 화면 (FR-AUTH03)

### 7.2 PendingView.vue 구현 내용

```
신규 파일: src/views/PendingView.vue

표시 내용:
  - 회원가입 완료 안내 메시지
  - "최상위 관리자의 승인을 기다리는 중입니다."
  - 로그아웃 버튼 (authStore.clearAuth() → /login)

라우터 등록:
  path: '/pending'
  name: 'pending'
  인증 불필요 (PENDING 상태 접근 가능해야 함)
```

---

## 8. FE — feature/fe-router-guard

### 8.1 구현 목표

현재 `requiresAuth`(로그인 여부)만 체크하는 라우터 가드에 role + status 분기 추가

### 8.2 router/index.js 보완

**추가할 라우트**

```js
{
  path: '/super-admin',
  name: 'superAdminDashboard',
  component: () => import('@/views/SuperAdminView.vue'),
  meta: { requiresAuth: true, requiresSuperAdmin: true },
},
{
  path: '/super-admin/approvals',
  name: 'approvals',
  component: () => import('@/views/ApprovalView.vue'),
  meta: { requiresAuth: true, requiresSuperAdmin: true },
},
```

**router.beforeEach 보완**

```js
router.beforeEach((to) => {
  const auth = useAuthStore()

  // 비로그인 → 로그인 페이지
  if (to.meta.requiresAuth && !auth.isLoggedIn) return { name: 'login' }

  // PENDING/REJECTED/SUSPENDED 상태 → pending 또는 login
  if (auth.isLoggedIn && auth.status === 'PENDING'
      && to.name !== 'pending' && to.name !== 'login') {
    return { name: 'pending' }
  }
  if (auth.isLoggedIn
      && (auth.status === 'REJECTED' || auth.status === 'SUSPENDED')
      && to.meta.requiresAuth) {
    return { name: 'login' }
  }

  // SUPER_ADMIN 전용 경로에 ROLE_ADMIN 접근 차단
  if (to.meta.requiresSuperAdmin && !auth.isSuperAdmin) {
    return { name: 'dashboard' }
  }

  // 로그인 상태에서 /login 접근 → role별 홈으로
  if (to.name === 'login' && auth.isLoggedIn) {
    return auth.isSuperAdmin ? { name: 'superAdminDashboard' } : { name: 'dashboard' }
  }
})
```

---

## 9. v0.1.1 완료 기준

산출물 `logmile_릴리즈전략.md` 4.2 기준

| 항목 | 확인 기준 |
|------|---------|
| 인증 | JWT 로그인 성공, accessToken 발급, Swagger에서 Authorize 후 보호 API 호출 가능 |
| 회원가입 | POST /api/auth/signup 호출 시 Company + Admin(PENDING) 생성 확인 (DB 조회) |
| PENDING 처리 | PENDING 계정 로그인 → 403 ACCOUNT_PENDING 응답 |
| 승인 흐름 | SUPER_ADMIN으로 /api/admin/approvals/pending 조회 → approve → ACTIVE 전환 확인 |
| 정지 | SUPER_ADMIN으로 suspend 후 해당 계정 로그인 시 403 ACCOUNT_SUSPENDED 확인 |
| FE 로그인 | LoginView에서 실제 로그인 성공 후 role에 따라 올바른 경로로 이동 |
| FE 가드 | PENDING 계정 로그인 후 /pending 이동, 보호 경로 직접 접근 시 /login 이동 |
| DB | seed의 `admin@logmile.com` / `admin1234` 로 로그인 성공 |

---

## 10. 파일 변경 목록 요약

### BE

```
logmile_be/src/main/java/com/project/logmile/
├── domain/
│   ├── admin/
│   │   ├── entity/Admin.java                     [수정] create(), approve(), reject(), suspend(), unsuspend() 추가
│   │   └── service/
│   │       ├── AuthService.java                  [신규] signup(), login()
│   │       └── AdminApprovalService.java         [신규] getPendingAdmins(), approve(), reject(), suspend(), unsuspend()
│   ├── auth/
│   │   ├── controller/AuthController.java        [수정] login() 스텁 → AuthService 호출로 교체, signup() 추가
│   │   └── dto/
│   │       ├── SignupRequest.java                [신규]
│   │       └── SignupResponse.java               [신규]
│   ├── company/
│   │   └── service/CompanyService.java           [수정] IllegalArgumentException → BusinessException 교체
│   └── admin/
│       ├── controller/AdminApprovalController.java [신규]
│       └── dto/AdminSummaryResponse.java           [신규]
```

### FE

```
logmile_fe/src/
├── views/
│   ├── LoginView.vue        [수정] 스텁 → 실제 구현
│   ├── SignupView.vue        [신규]
│   ├── PendingView.vue       [신규]
│   └── SuperAdminView.vue   [신규] (스텁 수준, fe-super-admin-dashboard에서 상세 구현)
├── stores/
│   └── authStore.js         [수정] role, status, adminId, companyId 추가
├── api/
│   └── auth.js              [신규 or 수정] login(), signup()
└── router/
    └── index.js             [수정] /signup, /pending, /super-admin 라우트 추가 + beforeEach 보완
```
