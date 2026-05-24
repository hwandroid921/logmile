이제 전체 파악됐습니다. 정리해드릴게요.

---

## :clipboard: FE 현황 (dev 기준)

### :white_check_mark: 완료된 것

| 항목 | 파일 | 상태 |
|------|------|------|
| 프로젝트 셋업 | `package.json`, `vite.config.js` | :white_check_mark: Vue3 + Pinia + Axios + Chart.js |
| Axios 인스턴스 | `src/api/axios.js` | :white_check_mark: JWT 인터셉터 + 401 자동 로그아웃 |
| API 함수 전체 | `src/api/*.js` | :white_check_mark: auth/dashboard/vehicle/driver/simulation/history/threshold/stats |
| Pinia 스토어 | `src/stores/authStore.js` 외 2개 | :white_check_mark: 인증 + 대시보드 + 시뮬레이션 |
| 라우터 + 인증가드 | `src/router/index.js` | :white_check_mark: 9개 라우트 + beforeEach 가드 |

### :x: 미구현 (View 전체 placeholder)

모든 Vue 파일이 아래 수준입니다:
```vue
<template>
  <div>DashboardView (FR-A01~A06)</div>  <!-- 빈 placeholder -->
</template>
```

**구현해야 할 화면:**

| 우선순위 | View 파일 | 기능 | FR 번호 |
|---------|-----------|------|---------|
| :red_circle: 1순위 | `LoginView.vue` | 로그인 | FR-AUTH01 |
| :red_circle: 1순위 | `DashboardView.vue` | 메인 대시보드 + OCR + 차트 | FR-A01~A06 |
| :yellow_circle: 2순위 | `SimulationView.vue` | 시뮬레이션 제어 | FR-B01, B04 |
| :yellow_circle: 2순위 | `VehicleView.vue` | 차량 관리 | FR-C01 |
| :yellow_circle: 2순위 | `DriverView.vue` | 운전자 관리 | FR-C02 |
| :yellow_circle: 2순위 | `DriveHistoryView.vue` | 운행 이력 목록 | FR-D01 |
| :green_circle: 3순위 | `DriveHistoryDetailView.vue` | 운행 이력 상세 | FR-D02 |
| :green_circle: 3순위 | `ThresholdView.vue` | 임계값 설정 | FR-C03 |
| :green_circle: 3순위 | `FatigueStatsView.vue` | 피로도 통계 | FR-E01 |

**추가로 없는 것:**
- **레이아웃 컴포넌트 없음** — 사이드바, 상단바 컴포넌트 (`src/components/` 폴더 자체 없음)
- **SUPER_ADMIN 전용 화면 없음** — 회원가입 승인, 업체 관리가 라우터에 미등록

---

## :whale: Docker DB 설정 방법

`docker-compose.yml`과 SQL은 이미 잘 작성돼 있습니다. **DB만 먼저 띄우는 방법** 알려드립니다.

### 1단계 — `.env` 파일 생성

```bash
cd logmile_infra
cp .env.example .env
```

`.env` 열어서 아래 값들 채우세요:

```env
# PostgreSQL
POSTGRES_DB=logmile
POSTGRES_USER=logmile_user
POSTGRES_PASSWORD=원하는_비밀번호   # ← 변경

# Spring Boot
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/logmile
SPRING_DATASOURCE_USERNAME=logmile_user
SPRING_DATASOURCE_PASSWORD=위와_동일한_비밀번호   # ← 변경
JWT_SECRET=최소32자_이상_랜덤문자열   # ← 변경 (예: openssl rand -hex 32)
JWT_EXPIRATION_MS=86400000

# 나머지는 기본값 사용 가능
```

### 2단계 — DB만 먼저 실행

```bash
# logmile_infra 디렉토리에서
docker compose up -d postgres
```

### 3단계 — 정상 확인

```bash
# 헬스체크 확인
docker ps

# 컨테이너 로그 확인
docker logs logmile-postgres

# DB 직접 접속 테스트
docker exec -it logmile-postgres psql -U logmile_user -d logmile -c "\dt"
```

### 자동으로 실행되는 것

Docker가 올라오면 자동으로 아래 순서 실행됩니다:

```
01_init.sql → 테이블 9개 생성 (company, admin, vehicle, driver, drive_log, gps_data, rest_event, fatigue_event, fatigue_threshold)
02_seed.sql → 초기 데이터 투입
              ├─ 업체 10개
              ├─ SUPER_ADMIN 계정 (admin@logmile.com / admin1234)
              ├─ 일반 관리자 10명 (admin01~10@logmile.com / admin1234)
              ├─ 차량 10대
              ├─ 운전자 10명 (차량 배정 포함)
              └─ 피로도 임계값 21개
```

### 전체 서비스 한번에 올릴 때

BE Dockerfile, FE Dockerfile 준비되면:
```bash
docker compose up -d          # 전체
docker compose up -d postgres backend   # DB + BE만
```

> **참고:** `simulator`는 `profiles: [simulator]`로 분리돼 있어서 기본 `up`에 포함 안 됩니다. 필요할 때만 `docker compose run --rm simulator python -m simulator.main` 으로 실행하면 됩니다.