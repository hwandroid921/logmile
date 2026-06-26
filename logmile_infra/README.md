# logmile_infra

logmile 프로젝트의 Docker Compose 기반 인프라 구성

## 구성 서비스

| 서비스 | 이미지 / 소스 | 기본 포트 | 설명 |
|---|---|---|---|
| `postgres` | postgres:16-alpine | 5432 | PostgreSQL 데이터베이스 |
| `backend` | logmile_be/Dockerfile | 8080 | Spring Boot REST API |
| `ai` | logmile_ai/Dockerfile | 8000 | FastAPI 번호판 인식 |
| `frontend` | logmile_fe/Dockerfile | 80 | Vue.js (Nginx) |
| `simulator` | logmile_sim/Dockerfile | _(수동 실행)_ | GPS 시뮬레이터 (profile: simulator) |

## 디렉터리 구조

```
logmile_infra/
├── docker-compose.yml   # 전체 서비스 구성
├── .env.example         # 환경변수 템플릿
└── db/
    ├── init.sql         # 테이블 생성 DDL
    └── seed.sql         # 초기 테스트 데이터
```

## 실행 방법

### 1. 환경변수 파일 준비

```powershell
Copy-Item .env.example .env
```

`.env`를 열어 아래 항목을 반드시 설정합니다.

| 변수 | 설명 |
|---|---|
| `POSTGRES_PASSWORD` | PostgreSQL 비밀번호 |
| `SPRING_DATASOURCE_PASSWORD` | POSTGRES_PASSWORD와 동일하게 설정 |
| `JWT_SECRET` | JWT 서명 키 (32자 이상 임의 문자열) |
| `CORS_ALLOWED_ORIGINS` | FE 접근 허용 Origin (기본: `http://localhost:5173,http://localhost:80`) |

### 2. 전체 서비스 실행

```powershell
docker compose up -d
```

서비스별 상태 확인:

```powershell
docker compose ps
docker compose logs -f backend
```

### 3. PostgreSQL만 실행 (로컬 개발)

로컬에서 BE/FE를 직접 실행할 때 DB만 Docker로 띄울 수 있습니다.

```powershell
docker compose up postgres -d
```

### 4. GPS 시뮬레이터 실행

시뮬레이터는 기본 실행에 포함되지 않으며 `simulator` 프로파일로 수동 실행합니다.

```powershell
docker compose --profile simulator run --rm simulator \
  python -m simulator.main --scenario A --drive-log-id 1 --send
```

### 5. 전체 종료

```powershell
docker compose down
```

데이터 볼륨까지 삭제:

```powershell
docker compose down -v
```

## 환경변수 전체 목록

```env
# PostgreSQL
POSTGRES_DB=logmile
POSTGRES_USER=logmile_user
POSTGRES_PASSWORD=your_password_here
POSTGRES_PORT=5432

# Spring Boot
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/logmile
SPRING_DATASOURCE_USERNAME=logmile_user
SPRING_DATASOURCE_PASSWORD=your_password_here
JWT_SECRET=your_jwt_secret_here_min_32_chars
JWT_EXPIRATION_MS=86400000
BE_PORT=8080

# FastAPI AI
AI_PORT=8000
OCR_CONFIDENCE_THRESHOLD=0.85
YOLO_MODEL_PATH=app/model/yolo11n.pt

# Vue.js FE
FE_PORT=80
VITE_API_BASE_URL=http://localhost:8080

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:80
```

## DB 초기화

`postgres` 서비스 첫 실행 시 자동으로 실행됩니다.

| 파일 | 설명 |
|---|---|
| `db/init.sql` | 테이블 생성 DDL (순서: company → admin → vehicle → driver → drive_log → ...) |
| `db/seed.sql` | 테스트용 시드 데이터 (업체, 관리자, 차량, 운전자, 피로도 임계값) |

데이터를 초기화하려면 볼륨을 삭제 후 재실행합니다.

```powershell
docker compose down -v
docker compose up postgres -d
```

## 서비스 기동 순서

```
postgres (healthcheck 통과)
    └── backend (postgres healthy 확인 후 기동)
            └── frontend (backend 기동 후 Nginx 서빙)
```

`ai`와 `simulator`는 독립 기동됩니다.
