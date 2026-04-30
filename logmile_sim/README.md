# logmile GPS Simulator

`logmile_sim`은 산출물 기준 `FR-B01`, `FR-B02`, `FR-B04`를 검증하기 위한 Python GPS 시뮬레이터입니다.

## 역할

- 시나리오 A/B/C GPS 운행 데이터 생성
- 휴식 패턴과 야간 운행 패턴 포함
- Spring Boot 백엔드 `POST /api/gps` 형식으로 데이터 전송

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
