# logmile

운행 데이터 기반 화물차 운전자 피로도 실시간 모니터링 플랫폼입니다.

## 프로젝트 목적

`logmile`은 화물차 운전자의 장시간 운행, 휴식 부족, 일일 누적 운행 시간, 야간 운행 시간을 기반으로 피로도 점수를 산정하고, 관제 대시보드에서 정상·주의·위험 상태를 실시간으로 확인할 수 있도록 하는 웹 기반 모빌리티 서비스입니다.

본 프로젝트는 학습 연구용 프로젝트이며, 실제 GPS 단말기와 외부 지도 API 없이 Python GPS 시뮬레이터와 자체 데이터를 활용해 시연 가능한 구조를 목표로 합니다.

## 주요 기능

- YOLO11n + EasyOCR 기반 화물차 번호판 인식
- Python GPS 시뮬레이터 기반 운행 시나리오 A/B/C 생성
- 연속 운행 시간, 휴식 여부, 일일 총 운행 시간, 야간 운행 시간 기반 피로도 점수 산정
- 정상·주의·위험 피로도 등급 표시
- Vue.js 관제 대시보드 및 Chart.js 통계 시각화
- 차량·운전자 관리
- 운행 이력 및 피로도 이벤트 조회
- JWT 기반 관리자 인증

## 팀원 역할

| 이름 | 담당 분야 | 주요 역할 |
|---|---|---|
| 유환희 | 백엔드 + AI 번호판 인식 | Spring Boot API, JPA Entity, 피로도 점수 계산, 휴식 판단, JWT/Security, FastAPI, YOLO11n + EasyOCR |
| 백경서 | 프론트엔드 + GPS 시뮬레이터 + 산출물 | Vue.js 대시보드, Pinia/Axios, Chart.js, Python GPS 시나리오 설계, ERD/DB 설계, 요구사항/화면/발표 문서 |

## 기술 스택

| 구분 | 기술 |
|---|---|
| AI / Python | Python 3.11, FastAPI, YOLO11n, EasyOCR |
| Backend | Java 21, Spring Boot 3.5, Spring Data JPA, Spring Security + JWT, Springdoc |
| Frontend | Vue.js 3, Vite, Pinia, Axios, Chart.js |
| Database | PostgreSQL 16 |
| Infra | Docker, Docker Compose |
| Collaboration | GitHub, Notion, Discord |

## 피로도 판단 기준

피로도 점수는 다음 항목을 합산해 산정합니다.

```text
피로도 점수 =
연속 운행 시간 점수
+ 일일 총 운행 시간 점수
+ 야간 운행 시간 점수
+ 휴식 부족 점수
- 휴식 보정 점수
```

등급 기준:

| 등급 | 점수 |
|---|---:|
| 정상 | 0~39점 |
| 주의 | 40~69점 |
| 위험 | 70점 이상 |

## 개발 범위

### 포함

- 번호판 인식
- GPS 시뮬레이터
- 피로도 점수 계산
- 관제 대시보드
- 차량·운전자 관리
- 운행 이력 조회

### 제외

- 실제 GPS 단말기 연동
- 카카오맵 실시간 경로 반영
- SMS 실연동
- 운전자 얼굴/눈 감김 인식
- 외부 보험사/운송사 API 연동

