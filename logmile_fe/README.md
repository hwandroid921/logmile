# logmile_fe

화물차 운전자 피로도 실시간 모니터링 관제 대시보드 (Vue.js 3 + Vite)

## 기술 스택

| 구분 | 버전 |
|---|---|
| Vue.js | 3.x |
| Vite | 8.x |
| Pinia | 3.x |
| Vue Router | 5.x |
| Axios | 1.x |
| Chart.js | 4.x |

## 실행 방법

```sh
cd logmile_fe
npm install
npm run dev
```

기본 포트: `http://localhost:5173`  
백엔드 API: `http://localhost:8080` (환경변수 `VITE_API_BASE_URL`로 변경 가능)

## 환경변수

`.env` 파일을 생성하여 API 주소를 변경할 수 있습니다.

```env
VITE_API_BASE_URL=http://localhost:8080
```

설정하지 않으면 axios 기본값 `http://localhost:8080`으로 동작합니다.

## 화면 구성

### 공개 페이지 (로그인 불필요)

| 경로 | 화면 |
|---|---|
| `/` | 서비스 소개 메인 |
| `/intro` | 서비스 소개 |
| `/features` | 기능 소개 |
| `/board` | 공지/게시판 |
| `/contact` | 문의 |

### 데모 페이지 (로그인 불필요, mock 데이터)

| 경로 | 화면 |
|---|---|
| `/demo/dashboard` | 대시보드 데모 |
| `/demo/simulation` | 시뮬레이션 데모 |
| `/demo/thresholds` | 임계값 설정 데모 |

### 인증

| 경로 | 화면 |
|---|---|
| `/login` | 로그인 |
| `/signup` | 회원가입 |
| `/pending` | 승인 대기 안내 |

### Admin 관제 화면 (로그인 필요, ROLE_ADMIN)

| 경로 | 화면 |
|---|---|
| `/dashboard` | 실시간 대시보드 |
| `/simulation` | 시뮬레이션 제어 |
| `/vehicles` | 차량 관리 |
| `/drivers` | 운전자 관리 |
| `/thresholds` | 피로도 임계값 설정 |
| `/drive-history` | 운행 이력 목록 |
| `/drive-history/:id` | 운행 이력 상세 |
| `/stats` | 피로도 통계 |

### Super Admin 화면 (ROLE_SUPER_ADMIN)

| 경로 | 화면 |
|---|---|
| `/super` | Super Admin 홈 |
| `/super/approval` | 관리자 승인 관리 |
| `/super/company` | 업체 관리 |

## 디렉터리 구조

```
src/
├── api/            # Axios 기반 API 모듈
│   ├── axios.js         # baseURL, JWT 인터셉터, 401 처리
│   ├── authApi.js       # 로그인, 회원가입
│   ├── vehicleApi.js    # 차량 CRUD
│   ├── driverApi.js     # 운전자 CRUD + 차량 배정/해제
│   ├── driveHistoryApi.js # 운행 이력 조회
│   ├── thresholdApi.js  # 피로도 임계값 조회/수정
│   ├── fatigueStatsApi.js # 피로도 통계
│   ├── simulationApi.js # 시뮬레이션 시작/중지
│   ├── dashboardApi.js  # 대시보드 실시간 데이터
│   ├── approvalApi.js   # 관리자 승인 (Super Admin)
│   └── companyApi.js    # 업체 관리 (Super Admin)
├── stores/         # Pinia 상태 관리
│   ├── authStore.js     # JWT 토큰, 사용자 정보, 권한
│   ├── dashboardStore.js
│   └── simulationStore.js
├── views/          # 페이지 컴포넌트
├── router/
│   └── index.js    # Vue Router, 인증 가드
└── main.js
```

## 인증 흐름

1. `POST /api/auth/login` 응답에서 `accessToken`, `name`, `role`, `status`, `companyId` 추출
2. `authStore`에 저장 (localStorage 동기화)
3. `status === PENDING` 시 `/pending` 페이지로 이동
4. 모든 API 요청에 `Authorization: Bearer {token}` 헤더 자동 첨부
5. 401 응답 시 `clearAuth()` 후 `/login` 리다이렉트
