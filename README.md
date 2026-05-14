# logmile

운행 데이터 기반 화물차 운전자 피로도 실시간 모니터링 플랫폼

> K-디지털 트레이닝 스마트 모빌리티 DX 프로젝트 (26.04.27 ~ 26.06.01)  
> 인천 3기 3조 · 팀장 유환희

## 프로젝트 목적

`logmile`은 화물차 운전자의 장시간 운행, 휴식 부족, 일일 누적 운행 시간, 야간 운행 시간을 기반으로 피로도 점수를 산정하고, 관제 대시보드에서 정상·주의·위험 상태를 실시간으로 확인할 수 있도록 하는 웹 기반 모빌리티 서비스입니다.

본 프로젝트는 학습 연구용 프로젝트이며, 실제 GPS 단말기와 외부 지도 API 없이 Python GPS 시뮬레이터와 자체 데이터를 활용해 시연 가능한 구조를 목표로 합니다.

## 주요 기능

- YOLO11n + EasyOCR 기반 화물차 번호판 인식
- Python GPS 시뮬레이터 기반 운행 시나리오 A/B/C 생성
- 연속 운행 시간, 휴식 여부, 일일 총 운행 시간, 야간 운행 시간 기반 피로도 점수 산정
- 정상·주의·위험 피로도 등급 표시
- Vue.js 관제 대시보드 및 Chart.js 통계 시각화
- 차량·운전자 관리 및 운행 이력·피로도 이벤트 조회
- JWT 기반 관리자 인증 (ROLE_ADMIN / ROLE_SUPER_ADMIN)

## 팀원 역할

| 이름 | 담당 분야 | 주요 역할 |
|---|---|---|
| 유환희 | 백엔드 + AI 번호판 인식 | Spring Boot API, JPA Entity, 피로도 점수 계산, 휴식 판단, JWT/Security, FastAPI, YOLO11n + EasyOCR |
| 백경서 | 프론트엔드 + GPS 시뮬레이터 + 산출물 | Vue.js 대시보드, Pinia/Axios, Chart.js, Python GPS 시나리오 설계, ERD/DB 설계, 요구사항/화면/발표 문서 |

## 기술 스택

| 구분 | 기술 |
|---|---|
| AI / Python | Python 3.11, FastAPI, YOLO11n, EasyOCR |
| Backend | Java 21, Spring Boot 3.5, Spring Data JPA, Spring Security + JWT, springdoc-openapi |
| Frontend | Vue.js 3, Vite, Pinia, Axios, Chart.js |
| Database | PostgreSQL 16 |
| Infra | Docker, Docker Compose |

## 시스템 구성

```
logmile/
├── logmile_be/          # Spring Boot REST API 서버
├── logmile_fe/          # Vue.js 3 관제 대시보드
├── logmile_ai/          # FastAPI 번호판 인식 (YOLO11n + EasyOCR)
├── logmile_sim/         # Python GPS 시뮬레이터
├── logmile_infra/       # Docker Compose 인프라 구성
├── logmile_ui_prototype/ # UI 프로토타입
└── logmile_deliverable/ # 산출물 (요구사항정의서, ERD, DB 설계서 등)
```

## 포트 구성

| 서비스 | 기본 포트 |
|---|---|
| Spring Boot BE | 8080 |
| FastAPI AI | 8000 |
| Vue.js FE (Nginx) | 80 |
| PostgreSQL | 5432 |

---

## 로컬 개발 환경 실행

### 사전 요구사항

- Java 21
- Node.js 20 이상
- Python 3.11
- Docker Desktop

### 1. PostgreSQL 실행 (Docker)

```powershell
cd logmile_infra
Copy-Item .env.example .env   # 비밀번호, JWT_SECRET 값 설정
docker compose up postgres -d
```

### 2. Spring Boot BE 실행

```powershell
cd logmile_be
./gradlew bootRun
```

API 문서: `http://localhost:8080/swagger-ui/index.html`

### 3. FastAPI AI 실행

```powershell
cd logmile_ai
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

### 4. Vue.js FE 실행

```powershell
cd logmile_fe
npm install
npm run dev
```

대시보드: `http://localhost:5173`

### 5. GPS 시뮬레이터 실행 (선택)

```powershell
cd logmile_sim
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
Copy-Item .env.example .env
python -m simulator.main --scenario A --drive-log-id 1 --send
```

---

## Docker Compose 전체 실행

```powershell
cd logmile_infra
Copy-Item .env.example .env   # 값 설정 후 진행
docker compose up -d
```

시뮬레이터 포함 실행:

```powershell
docker compose --profile simulator run --rm simulator python -m simulator.main --scenario A --drive-log-id 1 --send
```

### 주요 환경변수 (`logmile_infra/.env`)

| 변수 | 설명 |
|---|---|
| `POSTGRES_PASSWORD` | PostgreSQL 비밀번호 |
| `JWT_SECRET` | JWT 서명 키 (32자 이상) |
| `CORS_ALLOWED_ORIGINS` | FE 접근 허용 Origin |

---

## 주요 API 목록

| 경로 | 메서드 | 설명 |
|---|---|---|
| `/api/auth/login` | POST | 로그인 |
| `/api/auth/signup` | POST | 회원가입 |
| `/api/vehicles` | GET / POST | 차량 목록 조회 / 등록 |
| `/api/vehicles/{id}` | PATCH / DELETE | 차량 수정 / 삭제 |
| `/api/drivers` | GET / POST | 운전자 목록 조회 / 등록 |
| `/api/drivers/{id}/assign/{vehicleId}` | PATCH | 운전자-차량 배정 |
| `/api/drive-history` | GET | 운행 이력 목록 |
| `/api/drive-history/{id}` | GET | 운행 이력 상세 |
| `/api/thresholds` | GET | 피로도 임계값 조회 |
| `/api/thresholds/{id}` | PATCH | 피로도 임계값 수정 |
| `/api/fatigue/stats` | GET | 피로도 일별 통계 |
| `/api/gps` | POST | GPS 데이터 수신 |
| `/api/simulation/start` | POST | 시뮬레이션 시작 |
| `/api/simulation/{id}/stop` | PATCH | 시뮬레이션 중지 |
| `/api/ocr/recognize` | POST | 번호판 이미지 인식 (AI) |

전체 명세: `http://localhost:8080/swagger-ui/index.html`

---

## 피로도 판단 기준

```text
피로도 점수 =
  연속 운행 시간 점수
  + 일일 총 운행 시간 점수
  + 야간 운행 시간 점수
  + 휴식 부족 점수
  - 휴식 보정 점수
```

| 등급 | 점수 |
|---|---:|
| 정상 | 0 ~ 39점 |
| 주의 | 40 ~ 69점 |
| 위험 | 70점 이상 |

---

## 개발 범위

### 포함

- 번호판 인식 (YOLO11n + EasyOCR)
- GPS 시뮬레이터 (시나리오 A/B/C)
- 피로도 점수 계산
- 관제 대시보드 및 통계
- 차량·운전자·업체 관리
- 운행 이력 및 피로도 이벤트 조회

### 제외

- 실제 GPS 단말기 연동
- 외부 지도 API 실시간 경로 반영
- SMS 실연동
- 운전자 얼굴/눈 감김 인식
- 외부 보험사/운송사 API 연동

---

## 모듈별 README

- [logmile_fe/README.md](logmile_fe/README.md) — Vue.js 화면 구성, 라우트, API 모듈
- [logmile_sim/README.md](logmile_sim/README.md) — GPS 시뮬레이터 시나리오 및 실행 예시
