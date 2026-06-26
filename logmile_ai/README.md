# logmile_ai

화물차 번호판 인식 AI 서버 (FastAPI + YOLO11n + EasyOCR)

## 기술 스택

| 구분 | 버전 |
|---|---|
| Python | 3.11 |
| FastAPI | 0.111.0 |
| Uvicorn | 0.29.0 |
| Ultralytics (YOLO11n) | 8.4.45 |
| EasyOCR | 1.7.1 |
| Pillow | 10.3.0 |
| NumPy | 1.26.4 |

## 실행 방법

### 로컬 실행

```powershell
cd logmile_ai
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

### Docker 빌드

```powershell
docker build -t logmile-ai .
```

Docker Compose를 통한 실행은 `logmile_infra/` 참고.

## 환경변수

| 변수 | 기본값 | 설명 |
|---|---|---|
| `AI_HOST` | `0.0.0.0` | 서버 바인드 주소 |
| `AI_PORT` | `8000` | 서버 포트 |
| `YOLO_MODEL_PATH` | `app/model/yolo11n.pt` | YOLO11n 모델 파일 경로 |
| `OCR_CONFIDENCE_THRESHOLD` | `0.85` | OCR 신뢰도 임계값 |
| `DEBUG` | `false` | 디버그 모드 |

## API 문서

서버 실행 후 Swagger UI 접속:

```
http://localhost:8000/docs
```

## 디렉터리 구조

```
logmile_ai/
├── app/
│   ├── main.py              # FastAPI 앱 생성, CORS, 라우터 등록
│   ├── core/
│   │   └── config.py        # 환경변수 로드 (Settings)
│   ├── router/
│   │   └── ocr_router.py    # POST /api/ocr/recognize 엔드포인트
│   ├── schema/
│   │   ├── ocr_request.py   # 요청 스키마
│   │   └── ocr_response.py  # 응답 스키마 (OcrResponse)
│   ├── service/
│   │   ├── plate_service.py # 번호판 인식 통합 서비스
│   │   ├── yolo_service.py  # YOLO11n 번호판 영역 탐지
│   │   └── ocr_service.py   # EasyOCR 문자 추출
│   └── model/
│       └── yolo11n.pt       # YOLO11n 학습 모델 (git 미포함)
├── dataset/                 # 학습/테스트 이미지 (git 미포함)
├── Dockerfile
└── requirements.txt
```

## API

### `POST /api/ocr/recognize`

번호판 이미지를 업로드하면 탐지 및 문자 인식 결과를 반환합니다.

**요청:** `multipart/form-data`

| 필드 | 타입 | 설명 |
|---|---|---|
| `file` | image | JPEG / PNG / BMP 이미지 |

**응답:**

```json
{
  "plate_no": "12가3456",
  "confidence": 0.92,
  "is_manual_required": false,
  "detection_confidence": 0.88,
  "is_detected": true,
  "fallback_reason": null
}
```

| 필드 | 설명 |
|---|---|
| `plate_no` | 인식된 번호판 문자열 (신뢰도 미달 시 `null`) |
| `confidence` | EasyOCR 문자 인식 신뢰도 (0.0 ~ 1.0) |
| `is_manual_required` | `confidence < 0.85`이면 `true` (수동 입력 필요) |
| `detection_confidence` | YOLO 번호판 영역 탐지 신뢰도 |
| `is_detected` | YOLO 번호판 탐지 성공 여부 |
| `fallback_reason` | 수동 입력이 필요한 사유 |

### `GET /health`

서버 상태 확인.

```json
{ "status": "ok", "service": "logmile-ai" }
```

## 인식 흐름

1. YOLO11n으로 이미지에서 번호판 영역(Bounding Box) 탐지
2. 탐지된 영역을 크롭하여 EasyOCR로 문자 추출
3. OCR 신뢰도 ≥ `OCR_CONFIDENCE_THRESHOLD(0.85)` → `plate_no` 반환
4. OCR 신뢰도 미달 → `plate_no=null`, `is_manual_required=true` 반환

## 참고

- YOLO11n 모델 파일(`app/model/yolo11n.pt`)은 git에 포함되지 않습니다. 학습 완료 후 해당 경로에 배치하세요.
- 학습 데이터가 개인정보 보호를 위해 모자이크 처리된 경우 `plate_no`가 `null`이어도 정상 fallback입니다.
