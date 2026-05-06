# logmile GPS Simulator

`logmile_sim`은 산출물 기준 `FR-B01`, `FR-B02`, `FR-B04`를 검증하기 위한 Python GPS 시뮬레이터입니다.

## 역할

- 시나리오 A/B/C GPS 운행 데이터 생성
- 휴식 패턴과 야간 운행 패턴 포함
- Spring Boot 백엔드 `POST /api/gps` 형식으로 데이터 전송
- FastAPI 번호판 인식 모델 `POST /api/ocr/recognize` 호출 검증

## 실행 준비

```powershell
cd logmile_sim
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
Copy-Item .env.example .env
```

## 실행 예시

데이터 생성만 확인:

```powershell
python -m simulator.main --scenario A --drive-log-id 1 --dry-run
```

백엔드로 전송:

```powershell
python -m simulator.main --scenario C --drive-log-id 1 --send
```

FastAPI OCR 모델로 번호판 이미지 1건 전송:

```powershell
cd ..\logmile_ai
Start-Process -WindowStyle Hidden -FilePath ".\.venv\Scripts\python.exe" -ArgumentList "-m","uvicorn","app.main:app","--host","127.0.0.1","--port","8000"

cd ..\logmile_sim
$env:AI_BASE_URL="http://localhost:8000"
python -m simulator.main --ocr-image ..\logmile_ai\dataset\images\test\sample.jpg
```

FastAPI OCR 모델로 폴더 내 이미지 전송:

```powershell
$env:AI_BASE_URL="http://localhost:8000"
python -m simulator.main --ocr-dir ..\logmile_ai\dataset\images\test --ocr-limit 5
```

현재 학습 데이터가 개인정보 보호를 위해 모자이크 처리된 경우 `plate_no`가 `null`이어도 정상 fallback일 수 있습니다. 이때 `is_detected`, `detection_confidence`, `fallback_reason`으로 번호판 영역 탐지 여부와 수동 확인 필요 여부를 판단합니다.

직접 입력 번호판으로 입출차 이벤트 생성:

```powershell
python -m simulator.main --plate-no 12가3456 --event-type ENTRY --location-type REST_AREA --event-latitude 37.5665 --event-longitude 126.9780
```

더미 번호판으로 입출차 이벤트 생성:

```powershell
python -m simulator.main --dummy-plate --dummy-count 5 --event-type EXIT --location-type HIGHWAY_GATE
```

휴게소 진입/진출 쌍 생성:

```powershell
python -m simulator.main --plate-no 12가3456 --event-pair --exit-after-minutes 30 --location-type REST_AREA
```

OCR 결과를 입출차 이벤트로 변환:

```powershell
python -m simulator.main --ocr-image ..\logmile_ai\dataset\images\test\sample.jpg --fallback-plate-no 12가3456 --event-type ENTRY
```

백엔드 시뮬레이션 이벤트 API로 전송:

```powershell
python -m simulator.main --plate-no 12가3456 --event-type ENTRY --send-event
```

## 시나리오

| 시나리오 | 목적 | 예상 등급 |
|---|---|---|
| A | 90분 미만 운행 + 충분 휴식 | 정상 |
| B | 120~180분 운행 + 휴식 부족 + 야간 일부 | 주의 |
| C | 240분 이상 장시간 운행 + 휴식 누락 + 야간 2시간 이상 | 위험 |

## GPS 전송 형식

```json
{
  "driveLogId": 1,
  "latitude": 37.5665,
  "longitude": 126.9780,
  "speedKmh": 72.5,
  "recordedAt": "2026-04-29T22:30:00"
}
```
