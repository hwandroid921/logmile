# AGENTS.md

## 프로젝트 개요

이 프로젝트는 `logmile`이며, 화물차 운전자의 피로도를 실시간으로 모니터링하는 웹 기반 모빌리티 관제 플랫폼입니다.

시스템은 다음 구성 요소를 결합합니다.

- FastAPI + YOLO11n + EasyOCR 기반 번호판 인식
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
- YOLO11n + EasyOCR

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
- AI/Python: Python 3.11, FastAPI, YOLO11n, EasyOCR
- Database: PostgreSQL 16
- Infra: Docker, Docker Compose

## 지침 사항

- 모든 작업 시 실제 폴더 내에서 작업한다.
- 모든 코드 수정은 기존 스타일을 따르며 코드 수정 시에 수정 한 내용 정리하여 보고한다.
- 코드 작성 시 반드시 산출물을 기반으로 작성해야하며 수정이 필요할 시 산출물 작업이 우선되어야 하며 반드시 보고한다. 
- 산출물 작업 및 변경점 반영 시 수정 사항은 버전 단위로 관리한다.
- 산출물 수정 작업은 `md` 문서를 우선 기준으로 진행하고, `docx` 문서는 사용자가 요청한 경우에만 생성 또는 최신화한다.
- `docx` 문서는 제출 및 보관용 문서로 관리하며, 우선 별도 폴더에 보관하고 추후 정리 대상으로 둔다.
- 버전 표기가 없는 산출물은 다음 수정 시점부터 새 버전을 작성하여 관리한다.
- 이후 산출물 작업 시 문서 상단의 버전, 작성일, 변경 기준을 확인하고 변경 내용이 어느 버전에 반영되었는지 보고한다.
- 한글이 포함된 문서, SQL, 설정 파일을 확인할 때는 반드시 UTF-8 인코딩을 명시하여 읽고, 콘솔 출력이 깨져 보이는 경우 원문이 깨졌다고 단정하지 않는다.
- 한글 깨짐 또는 SQL 문법 오류를 판단하기 전에는 `Get-Content -Encoding UTF8` 등으로 재확인하고, 깨진 출력만 근거로 오류를 보고하지 않는다.
- git 커밋은 사용자가 명시적으로 커밋을 지시한 경우에만 수행하며, 작업 완료 후 자동으로 커밋하지 않는다.
- git 커밋 메시지는 기존 커밋 이력(`git log`)의 형식과 스타일을 기반으로 작성한다.
- Co-Authored-By 등 Agent(Claude)를 contributor로 추가하지 않는다.
