# logmile GPS Simulator

`logmile_sim`은 산출물 기준 `FR-B01`, `FR-B02`, `FR-B04`를 검증하기 위한 Python GPS 시뮬레이터입니다.

## 역할

- 시나리오 A/B/C GPS 운행 데이터 생성 및 Spring Boot 백엔드로 전송
- 이동/야간/휴식 패턴을 조합하여 피로도 등급 검증
- FastAPI 번호판 인식 모델 `POST /api/ocr/recognize` 호출 검증
- 직접 입력 번호판 또는 더미 번호판으로 입출차 이벤트(`plate_event`) 생성

## 디렉터리 구조

```
logmile_sim/
├── simulator/
│   ├── main.py          # CLI 진입점 (argparse)
│   ├── config.py        # .env 로드 및 설정 관리
│   ├── models.py        # GPS 포인트, 번호판 이벤트 데이터 모델
│   ├── sender.py        # 백엔드/FastAPI HTTP 전송 로직
│   ├── plate_events.py  # 번호판 이벤트 생성 및 전송
│   ├── scenarios/
│   │   ├── scenario_a.py    # 정상 (90분 미만, 충분 휴식)
│   │   ├── scenario_b.py    # 주의 (120~180분, 휴식 부족, 야간 일부)
│   │   └── scenario_c.py    # 위험 (240분 이상, 휴식 누락, 야간 2시간+)
│   └── patterns/
│       ├── movement_pattern.py  # 기본 이동 패턴
│       ├── rest_pattern.py      # 휴식 패턴
│       └── night_pattern.py     # 야간 운행 패턴
├── requirements.txt
└── .env.example
```

## 실행 준비

```powershell
cd logmile_sim
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
Copy-Item .env.example .env
```

`.env`에서 백엔드/AI URL과 인증 토큰을 설정합니다.

```env
BE_API_URL=http://localhost:8080
AI_BASE_URL=http://localhost:8000
API_TOKEN=          # JWT Bearer 토큰
```

## 실행 예시

### GPS 시나리오 실행

데이터 생성만 확인 (전송 없음):

```powershell
python -m simulator.main --scenario A --drive-log-id 1 --dry-run
```

백엔드로 GPS 데이터 전송:

```powershell
python -m simulator.main --scenario C --drive-log-id 1 --send
```

시작 시각 및 전송 간격 지정:

```powershell
python -m simulator.main --scenario B --drive-log-id 2 --send --start-at 2026-05-14T08:00:00 --interval 3
```

### 번호판 OCR 테스트

FastAPI OCR 모델로 이미지 1건 전송:

```powershell
cd ..\logmile_ai
Start-Process -WindowStyle Hidden -FilePath ".\.venv\Scripts\python.exe" -ArgumentList "-m","uvicorn","app.main:app","--host","127.0.0.1","--port","8000"

cd ..\logmile_sim
$env:AI_BASE_URL="http://localhost:8000"
python -m simulator.main --ocr-image ..\logmile_ai\dataset\images\test\sample.jpg
```

폴더 내 이미지 일괄 전송:

```powershell
python -m simulator.main --ocr-dir ..\logmile_ai\dataset\images\test --ocr-limit 5
```

OCR 결과를 입출차 이벤트로 변환:

```powershell
python -m simulator.main --ocr-image ..\logmile_ai\dataset\images\test\sample.jpg --fallback-plate-no 12가3456 --event-type ENTRY --send-event
```

### 번호판 입출차 이벤트 직접 생성

직접 입력 번호판:

```powershell
python -m simulator.main --plate-no 12가3456 --event-type ENTRY --location-type REST_AREA --event-latitude 37.5665 --event-longitude 126.9780 --send-event
```

더미 번호판 다수 생성:

```powershell
python -m simulator.main --dummy-plate --dummy-count 5 --event-type EXIT --location-type HIGHWAY_GATE --send-event
```

휴게소 진입/진출 쌍 생성 (30분 후 자동 EXIT):

```powershell
python -m simulator.main --plate-no 12가3456 --event-pair --exit-after-minutes 30 --location-type REST_AREA --send-event
```

## 시나리오

| 시나리오 | 목적 | 예상 피로도 등급 |
|---|---|---|
| A | 90분 미만 운행 + 충분 휴식 | 정상 |
| B | 120~180분 운행 + 휴식 부족 + 야간 일부 | 주의 |
| C | 240분 이상 장시간 운행 + 휴식 누락 + 야간 2시간 이상 | 위험 |

## GPS 전송 형식

`POST /api/gps`

```json
{
  "driveLogId": 1,
  "latitude": 37.5665,
  "longitude": 126.9780,
  "speedKmh": 72.5,
  "recordedAt": "2026-04-29T22:30:00"
}
```

## 번호판 이벤트 전송 형식

`POST /api/simulation/plate-events`

```json
{
  "plateNo": "12가3456",
  "eventType": "ENTRY",
  "locationType": "REST_AREA",
  "latitude": 37.5665,
  "longitude": 126.9780,
  "detectedAt": "2026-05-14T10:00:00"
}
```

## 참고

- `plate_no`가 `null`인 경우는 OCR 학습 데이터 모자이크 처리로 인한 정상 fallback일 수 있습니다. `is_detected`, `detection_confidence`, `fallback_reason` 필드로 번호판 영역 탐지 여부를 확인하세요.
- Docker 환경에서는 `.env`의 `BE_API_URL`을 `http://backend:8080`으로, `AI_BASE_URL`을 `http://ai:8000`으로 변경합니다.
