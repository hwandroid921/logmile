# logmile API 명세서

## logmile - 화물차 운전자 피로도 실시간 모니터링 플랫폼

- 프로젝트명: `logmile`
- 버전: v1.5
- 작성 기준일: 2026.05.08
- 기준 산출물: `logmile_요구사항정의서.md`, `logmile_프로젝트구조도.md`, `logmile_WBS_초안.md`

## 1. 문서 목적

이 문서는 애자일 방식의 현재 구현 기준 API 명세서다.

- 1차 시연 가능한 API와 이후 반복에서 보완할 API를 함께 관리한다.
- `완료 / 진행 / 계획` 상태를 표시해 중간 점검 시 현재 범위를 명확히 설명한다.
- 상세 DTO 구조는 백엔드 구현과 함께 계속 갱신한다.

## 2. 상태 기준

| 상태 | 의미 |
|---|---|
| `완료` | 구현 브랜치가 병합되었거나 현재 기준으로 동작 흐름이 확보된 API |
| `진행` | 화면/서비스/통합 중 일부만 완료되었고 추가 연결이 필요한 API |
| `계획` | 산출물 기준으로 필요하지만 아직 구현되지 않은 API |

## 3. 1차 시연 범위(MVP)

- JWT 로그인 / 회원가입 / 승인 API
- 업체 조회, 차량 CRUD, 운전자 CRUD
- 시뮬레이션 시작/중지, GPS 수신
- 대시보드 요약, 운행 이력 조회
- OCR `recognize` API와 fallback 구조

## 4. API 목록

| 영역 | 메서드 | 경로 | 설명 | 권한 | 상태 | 시연 우선순위 |
|---|---|---|---|---|---|---|
| 인증 | `POST` | `/api/auth/login` | 관리자 로그인, JWT 발급 (`ACTIVE`만 허용) | 없음 | `완료` | `상` |
| 인증 | `POST` | `/api/auth/signup` | 일반 관리자 회원가입 (`PENDING`) | 없음 | `완료` | `상` |
| 인증 | `GET` | `/api/auth/me` | 현재 로그인 사용자 정보 조회 | JWT | `진행` | `중` |
| 최상위 관리자 | `GET` | `/api/super/admins/pending` | 승인 대기 관리자 목록 조회 | SUPER | `완료` | `상` |
| 최상위 관리자 | `PATCH` | `/api/super/admins/{adminId}/approve` | 일반 관리자 승인 | SUPER | `완료` | `상` |
| 최상위 관리자 | `PATCH` | `/api/super/admins/{adminId}/reject` | 일반 관리자 거절 | SUPER | `완료` | `중` |
| 최상위 관리자 | `PATCH` | `/api/super/admins/{adminId}/suspend` | 일반 관리자 정지 | SUPER | `완료` | `중` |
| 최상위 관리자 | `PATCH` | `/api/super/admins/{adminId}/activate` | 일반 관리자 정지 해제 | SUPER | `완료` | `중` |
| 업체 | `GET` | `/api/super/companies` | 업체 목록 조회 | SUPER | `완료` | `중` |
| 업체 | `GET` | `/api/super/companies/{companyId}` | 업체 상세 조회 | SUPER | `진행` | `하` |
| 업체 | `GET` | `/api/company/me` | 내 업체 정보 조회 | JWT | `완료` | `중` |
| 차량 | `GET` | `/api/vehicles` | 소속 업체 차량 목록 조회 | JWT | `완료` | `상` |
| 차량 | `POST` | `/api/vehicles` | 차량 등록 | JWT | `완료` | `상` |
| 차량 | `PUT` | `/api/vehicles/{id}` | 차량 수정 | JWT | `완료` | `중` |
| 차량 | `DELETE` | `/api/vehicles/{id}` | 차량 삭제 | JWT | `완료` | `하` |
| 운전자 | `GET` | `/api/drivers` | 소속 업체 운전자 목록 조회 | JWT | `완료` | `상` |
| 운전자 | `POST` | `/api/drivers` | 운전자 등록 | JWT | `완료` | `중` |
| 운전자 | `PUT` | `/api/drivers/{id}` | 운전자 수정/차량 배정 | JWT | `완료` | `중` |
| 운전자 | `DELETE` | `/api/drivers/{id}` | 운전자 삭제 | JWT | `완료` | `하` |
| 시뮬레이션 | `POST` | `/api/simulation/start` | 시뮬레이션 시작 및 `drive_log` 생성 | JWT | `완료` | `상` |
| 시뮬레이션 | `POST` | `/api/simulation/stop` | 시뮬레이션 종료 및 운행 상태 갱신 | JWT | `완료` | `상` |
| GPS | `POST` | `/api/gps` | GPS 데이터 수신 및 피로도 계산 트리거 | JWT | `완료` | `상` |
| 번호판 관측 | `POST` | `/api/plate-events` | 출발/도착/관측/휴게소 이벤트 저장 | JWT | `완료` | `중` |
| 번호판 관측 | `GET` | `/api/drive-logs/{id}/plate-events` | 운행별 번호판 타임라인 조회 | JWT | `진행` | `하` |
| 대시보드 | `GET` | `/api/dashboard/summary` | 업체별 요약 카드 조회 | JWT | `완료` | `상` |
| 대시보드 | `GET` | `/api/dashboard/vehicles` | 차량별 현재 피로도 상태 조회 | JWT | `진행` | `상` |
| 운행 이력 | `GET` | `/api/drive-logs` | 운행 이력 목록 조회 | JWT | `완료` | `상` |
| 운행 이력 | `GET` | `/api/drive-logs/{id}` | 운행 이력 상세 조회 | JWT | `진행` | `중` |
| 피로도 | `GET` | `/api/fatigue/stats` | 일별 피로도 통계 | JWT | `계획` | `하` |
| 피로도 | `GET` | `/api/fatigue/thresholds` | 임계값 조회 | JWT | `계획` | `하` |
| 피로도 | `PUT` | `/api/fatigue/thresholds/{key}` | 임계값 수정 | JWT | `계획` | `하` |
| AI OCR | `POST` | `/api/ocr/recognize` | 번호판 인식 및 fallback 판정 | 내부 | `완료` | `상` |
| AI OCR | `POST` | `/api/ocr/observe` | 관측 유형을 포함한 OCR 처리 | 내부 | `계획` | `하` |

## 5. 애자일 점검 메모

- 현재 API 명세는 최종 완성본이 아니라 `내일 시연 가능한 범위` 중심으로 우선순위를 부여한 상태다.
- `완료` API는 시연 및 설명 가능한 범위, `진행` API는 구조는 잡혔으나 화면/통합이 남은 범위, `계획` API는 다음 반복에서 구현할 범위다.
- 중간 점검 이후 고객/내부 피드백을 반영해 우선순위를 조정한다.
