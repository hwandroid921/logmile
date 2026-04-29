# AGENTS.md

## 프로젝트 개요

이 프로젝트는 `logmile`이며, 화물차 운전자의 피로도를 실시간으로 모니터링하는 웹 기반 모빌리티 관제 플랫폼입니다.

시스템은 다음 구성 요소를 결합합니다.

- FastAPI + YOLOv8 + EasyOCR 기반 번호판 인식
- 운행 시나리오 생성을 위한 Python GPS 시뮬레이터
- REST API, 피로도 점수 산정, 인증, 데이터 저장을 담당하는 Spring Boot
- 관제 대시보드를 담당하는 Vue.js
- 운행 데이터 저장을 위한 PostgreSQL

이 프로젝트는 학습 및 연구용 프로젝트이며, 실제 운영 배포를 목적으로 하지 않습니다.

## 팀원 역할

### 유환희: 백엔드 + AI

- Spring Boot REST API
- JPA Entity / Repository / Service
- 피로도 점수 계산
- 휴식 판단 로직
- JWT / Spring Security
- FastAPI 번호판 인식
- YOLOv8 + EasyOCR

### 백경서: 프론트엔드 + GPS 시뮬레이터 + 문서

- Vue.js 대시보드
- Pinia / Axios
- Chart.js 통계
- Python GPS 시나리오 설계
- ERD / DB 설계
- 요구사항 정의서, 화면 설계서, 발표 문서

## 기술 스택

- Backend: Java 21, Spring Boot 3.5.14, Spring Data JPA, Spring Security + JWT
- Frontend: Vue.js 3, Vite, Pinia, Axios, Chart.js
- AI/Python: Python 3.11, FastAPI, YOLOv8n, EasyOCR
- Database: PostgreSQL 16
- Infra: Docker, Docker Compose

## 지침 사항

- 모든 작업 시 실제 폴더 내에서 작업한다.
- 모든 코드 수정은 기존 스타일을 따르며 코드 수정 시에 수정 한 내용 정리하여 보고한다.
- 코드 작성 시 반드시 산출물을 기반으로 작성해야하며 수정이 필요할 시 산출물 작업이 우선되어야 하며 반드시 보고한다. 
- 한글이 포함된 문서, SQL, 설정 파일을 확인할 때는 반드시 UTF-8 인코딩을 명시하여 읽고, 콘솔 출력이 깨져 보이는 경우 원문이 깨졌다고 단정하지 않는다.
- 한글 깨짐 또는 SQL 문법 오류를 판단하기 전에는 `Get-Content -Encoding UTF8` 등으로 재확인하고, 깨진 출력만 근거로 오류를 보고하지 않는다.
- git 커밋 시 Co-Authored-By 등 Agent(Claude)를 contributor로 추가하지 않는다.

## Git 작업 규칙

- Agent(Claude)는 커밋(`git commit`)을 직접 실행하지 않는다. 작업 완료 후 커밋할 내용을 정리하여 보고하고, 사용자가 직접 커밋한다.
- Agent(Claude)는 `git push` 실행 전 반드시 사용자에게 확인을 받는다.
- 확인 없이 원격 저장소에 push하지 않는다.
- force push(`--force`, `--force-with-lease`)는 반드시 사전 설명 및 확인 후 실행한다.

## 환경변수 규칙

- `.env` 파일은 절대 git에 커밋하지 않는다.
- 모든 모듈에 `.env.example` 파일을 함께 제공하여 필요한 환경변수 항목을 명시한다.
- 민감 정보(DB 패스워드, JWT 시크릿, API 토큰 등)는 반드시 환경변수로 관리한다.
- Spring Boot의 경우 `application.yml`에 민감 정보를 직접 작성하지 않으며, `application-local.yml`로 분리하여 관리한다. (`application-local.yml`은 gitignore 대상)
- `.vscode/`, `.claude/`, `__pycache__/`, `node_modules/`, `dist/`, YOLO 모델 가중치(`*.pt`, `*.pth`)는 git에 커밋하지 않는다.
