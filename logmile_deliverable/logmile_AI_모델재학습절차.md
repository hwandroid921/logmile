# logmile AI 모델 재학습 절차

> 프로젝트명: logmile  
> 문서 버전: v1.0
> 문서 목적: 번호판 탐지 모델 재학습, 검증, 프로젝트 반영 절차 정리  
> 기준 모델: YOLO11n + EasyOCR  
> 작성일: 2026.05.06

---

## 1. 재학습 목적

logmile 프로젝트의 번호판 인식 기능은 다음 흐름으로 동작한다.

```text
입력 이미지/영상 프레임
  → YOLO11 번호판 영역 탐지
  → 번호판 영역 crop
  → EasyOCR 문자 인식
  → 번호판 문자열 정규화
  → Spring Boot 운행/관측 데이터와 연동
```

재학습은 YOLO11 번호판 탐지 모델의 성능을 개선하고, 실제 프로젝트에서 사용할 수 있는 모델 파일을 생성하기 위해 수행한다.

---

## 2. 재학습 대상 범위

### 2.1 포함 범위

- 번호판 영역 탐지 모델 학습
- 데이터셋 라벨 형식 검증
- polygon 라벨을 YOLO bbox 라벨로 변환
- train / val / test 기준 성능 검증
- 프로젝트 적용용 `best.pt` 모델 생성
- FastAPI 모델 로드 확인

### 2.2 제외 범위

- 번호판 문자 OCR 모델 자체 학습
- EasyOCR 내부 모델 재학습
- 실제 도로 CCTV 연동
- Spring Boot 관측 이벤트 저장 로직 구현
- 모델 가중치 Git 커밋

EasyOCR은 학습 대상이 아니라, YOLO로 crop된 번호판 이미지의 문자 판독 모듈로 사용한다.

---

## 3. 브랜치 전략

모델 재학습은 AI 브랜치 흐름에 맞춰 진행한다.

```text
feature/ai-model-dataset
  → feature/ai-model-training
  → feature/ai-model-evaluation
  → feature/ai-ocr-license-plate
  → feature/ai-ocr-observation
```

| 브랜치 | 역할 |
|---|---|
| `feature/ai-model-dataset` | 데이터셋 구조, 라벨 기준, data.yaml 관리 |
| `feature/ai-model-training` | 학습 스크립트, 학습 옵션, 모델 생성 |
| `feature/ai-model-evaluation` | mAP, Precision, Recall, 추론 속도 검증 |
| `feature/ai-ocr-license-plate` | FastAPI 모델 적용 및 OCR 연계 |
| `feature/ai-ocr-observation` | 출발/도착, 고속도로, 휴게소 관측 로직 연계 |

---

## 4. 데이터셋 준비

### 4.1 기본 구조

```text
logmile_ai/
  dataset/
    images/
      train/
      val/
      test/
    labels/
      train/
      val/
      test/
    data.yaml
    classes.txt
```

### 4.2 데이터셋 설정 파일

`data.yaml` 기본 형식은 다음과 같다.

```yaml
path: ./dataset
train: images/train
val: images/val
test: images/test

nc: 1
names:
  - license_plate
```

프로젝트의 1차 목표는 번호판 영역 탐지이므로 class는 `license_plate` 1개로 둔다.

---

## 5. 데이터셋 검증

학습 전 반드시 아래 항목을 확인한다.

| 검증 항목 | 설명 |
|---|---|
| 이미지/라벨 개수 | 이미지 파일과 라벨 파일이 1:1로 매칭되는지 확인 |
| 라벨 형식 | YOLO bbox 또는 polygon 형식 여부 확인 |
| 좌표 범위 | 모든 좌표가 0.0 이상 1.0 이하인지 확인 |
| class id | `0`만 사용하는지 확인 |
| 빈 라벨 | 번호판이 없는 negative sample인지 확인 |
| train/val/test 분리 | 같은 이미지 또는 연속 프레임 중복 여부 확인 |

### 5.1 현재 확보 데이터 기준

2026.05.06 기준 확보 데이터셋은 다음과 같다.

| split | 이미지 수 | 라벨 수 |
|---|---:|---:|
| train | 3150 | 3150 |
| val | 900 | 900 |
| test | 450 | 450 |

### 5.2 라벨 형식 이슈

확보된 데이터셋 일부 라벨은 YOLO bbox 형식이 아니라 polygon 형식으로 확인되었다.

YOLO bbox 형식:

```text
class_id x_center y_center width height
```

polygon 형식:

```text
class_id x1 y1 x2 y2 x3 y3 ...
```

번호판 탐지 모델은 bbox 기반으로 학습하므로 polygon 라벨은 bbox로 변환해야 한다.

---

## 6. Polygon 라벨을 Bbox 라벨로 변환

원본 데이터셋은 보존하고, 학습용 변환 데이터셋을 별도로 생성한다.

권장 생성 위치:

```text
logmile_ai/runs/datasets/license_plate_detect/
```

변환 방식:

```text
polygon x 좌표 중 최소값 → x_min
polygon x 좌표 중 최대값 → x_max
polygon y 좌표 중 최소값 → y_min
polygon y 좌표 중 최대값 → y_max

x_center = (x_min + x_max) / 2
y_center = (y_min + y_max) / 2
width    = x_max - x_min
height   = y_max - y_min
```

변환 후 구조:

```text
runs/
  datasets/
    license_plate_detect/
      images/
        train/
        val/
        test/
      labels/
        train/
        val/
        test/
      data.yaml
```

변환 후에는 bbox 라벨 검증을 다시 수행한다.

---

## 7. 학습 환경 구성

### 7.1 가상환경 생성

```bash
cd logmile_ai
python -m venv .venv
.\.venv\Scripts\Activate.ps1
```

### 7.2 패키지 설치

```bash
pip install --upgrade pip
pip install -r requirements.txt
```

핵심 패키지:

```text
ultralytics
torch
opencv-python
easyocr
fastapi
uvicorn
```

### 7.3 GPU 확인

```bash
python -c "import torch; print(torch.cuda.is_available())"
```

| 결과 | 의미 |
|---|---|
| `True` | GPU 학습 가능 |
| `False` | CPU 학습만 가능 |

CPU 학습은 매우 느리므로 최종 모델 학습은 GPU 환경을 권장한다.

---

## 8. 1차 학습

### 8.1 CPU 확인용 짧은 학습

데이터 경로와 라벨 형식이 정상인지 확인하기 위해 짧은 학습을 먼저 수행한다.

```bash
yolo detect train ^
  model=yolo11n.pt ^
  data=runs/datasets/license_plate_detect/data.yaml ^
  epochs=3 ^
  imgsz=416 ^
  batch=4 ^
  device=cpu ^
  workers=0 ^
  project=logmile_license_plate ^
  name=yolo11n_cpu_e3 ^
  exist_ok=True
```

### 8.2 GPU 권장 학습

최종 후보 모델은 GPU 환경에서 아래 기준으로 재학습한다.

```bash
yolo detect train ^
  model=yolo11n.pt ^
  data=runs/datasets/license_plate_detect/data.yaml ^
  epochs=100 ^
  imgsz=640 ^
  batch=16 ^
  device=0 ^
  workers=4 ^
  project=logmile_license_plate ^
  name=yolo11n_gpu_e100 ^
  exist_ok=True
```

GPU 메모리가 부족하면 `batch=8` 또는 `imgsz=512`로 낮춘다.

---

## 9. 학습 결과 확인

학습 완료 후 결과는 아래 경로에 생성된다.

```text
logmile_ai/
  runs/
    detect/
      logmile_license_plate/
        yolo11n_cpu_e3/
          weights/
            best.pt
            last.pt
          results.csv
          results.png
          confusion_matrix.png
```

| 파일 | 설명 |
|---|---|
| `best.pt` | 검증 성능이 가장 좋은 모델 |
| `last.pt` | 마지막 epoch 기준 모델 |
| `results.csv` | epoch별 학습 지표 |
| `results.png` | 학습 곡선 |
| `confusion_matrix.png` | 검증 혼동 행렬 |

---

## 10. 검증 절차

### 10.1 Validation 성능 확인

학습 과정에서 val split 기준 성능이 자동으로 계산된다.

확인 지표:

```text
Precision
Recall
mAP50
mAP50-95
box_loss
cls_loss
dfl_loss
```

### 10.2 Test split 검증

학습에 직접 사용하지 않은 test split으로 최종 검증한다.

```bash
yolo detect val ^
  model=runs/detect/logmile_license_plate/yolo11n_cpu_e3/weights/best.pt ^
  data=runs/datasets/license_plate_detect/data.yaml ^
  split=test ^
  imgsz=416 ^
  batch=4 ^
  device=cpu ^
  workers=0 ^
  project=logmile_license_plate ^
  name=yolo11n_cpu_e3_test ^
  exist_ok=True
```

### 10.3 2026.05.06 1차 학습 결과

CPU 환경에서 3 epoch 학습한 1차 모델 결과는 다음과 같다.

| 구분 | Precision | Recall | mAP50 | mAP50-95 |
|---|---:|---:|---:|---:|
| val | 0.98767 | 0.96827 | 0.98201 | 0.67095 |
| test | 0.992 | 0.953 | 0.968 | 0.645 |

CPU 기준 test 추론 속도:

```text
preprocess: 3.1ms/image
inference: 280.3ms/image
postprocess: 3.8ms/image
```

해당 수치는 CPU 기준이므로 실제 서버/GPU 환경에서는 달라질 수 있다.

---

## 11. 프로젝트 모델 파일 생성

검증이 완료된 `best.pt`를 프로젝트 모델 경로로 복사한다.

```bash
Copy-Item ^
  -LiteralPath "runs\detect\logmile_license_plate\yolo11n_cpu_e3\weights\best.pt" ^
  -Destination "app\model\license_plate_yolo11n_best.pt" ^
  -Force
```

FastAPI 설정 파일의 기본 모델 경로는 다음 값을 사용한다.

```python
YOLO_MODEL_PATH = "app/model/license_plate_yolo11n_best.pt"
```

`.env.example`에도 동일하게 반영한다.

```env
YOLO_MODEL_PATH=app/model/license_plate_yolo11n_best.pt
```

---

## 12. 모델 로드 검증

모델 파일이 정상적으로 로드되는지 확인한다.

```bash
python -c "from ultralytics import YOLO; m=YOLO('app/model/license_plate_yolo11n_best.pt'); print(m.model.names)"
```

정상 출력:

```text
{0: 'license_plate'}
```

FastAPI 앱 import도 확인한다.

```bash
$env:PYTHONPATH='.'
python -c "from app.main import app; print(app.title); print(len(app.routes))"
```

정상 로그:

```text
[YoloService] 모델 로드 완료: app/model/license_plate_yolo11n_best.pt
logmile AI Server
```

---

## 13. 샘플 추론 검증

학습된 모델로 샘플 이미지를 추론한다.

```bash
yolo detect predict ^
  model=app/model/license_plate_yolo11n_best.pt ^
  source="runs/datasets/license_plate_detect/images/test/샘플이미지.jpg" ^
  imgsz=416 ^
  conf=0.25 ^
  device=cpu ^
  project=logmile_license_plate ^
  name=yolo11n_cpu_e3_sample ^
  exist_ok=True
```

정상 예시:

```text
1 license_plate
Results saved to runs/detect/logmile_license_plate/yolo11n_cpu_e3_sample
```

---

## 14. Git 관리 기준

다음 파일은 Git에 커밋하지 않는다.

```text
*.pt
*.pth
*.onnx
runs/
.venv/
dataset 원본 이미지 대량 파일
```

Git에 남길 수 있는 항목:

```text
data.yaml 샘플
classes.txt
학습/검증 스크립트
학습 절차 문서
성능 결과 요약
.env.example의 모델 경로
FastAPI 모델 로드 설정
```

---

## 15. 최종 모델 선정 기준

최종 모델은 YOLO 지표만으로 선정하지 않고, 프로젝트 요구사항에 맞춰 다음 기준을 함께 본다.

| 기준 | 설명 |
|---|---|
| mAP50 | 번호판 영역 탐지 성공률 |
| Recall | 번호판 누락 방지 수준 |
| Precision | 오탐지 방지 수준 |
| mAP50-95 | bbox 품질 |
| 추론 속도 | 실시간/준실시간 처리 가능성 |
| OCR 성공률 | EasyOCR까지 연결했을 때 번호판 문자열 인식 성공률 |
| 실패 케이스 | 야간, 원거리, 흐림, CCTV 각도, 휴게소 진입/진출 |

logmile에서는 최종적으로 다음 조건을 만족하는 모델을 우선 선정한다.

```text
1. 번호판 탐지 누락이 적을 것
2. EasyOCR이 읽기 좋은 crop을 만들 것
3. CPU 또는 서버 환경에서 추론 시간이 과도하지 않을 것
4. 고속도로/휴게소/출발/도착 관측 이미지에 적용 가능할 것
```

---

## 16. 향후 재학습 개선 사항

다음 재학습에서는 아래 항목을 보강한다.

- GPU 환경에서 `epochs=50~100`, `imgsz=640` 기준 학습
- YOLO11n과 YOLO11s 비교
- YOLOv8n 기준 모델과 비교
- 야간/저조도 번호판 데이터 추가
- 고속도로 CCTV 원거리 각도 데이터 추가
- 휴게소 진입/진출 카메라 각도 데이터 추가
- OCR 문자열 정답 데이터 구축
- YOLO 탐지 성능과 OCR 최종 성공률을 함께 평가

---

## 17. 결론

현재 확보 데이터셋은 번호판 탐지 모델 학습에 사용할 수 있으나, 일부 라벨이 polygon 형식이므로 bbox 변환 과정이 필요하다.

2026.05.06 기준 1차 CPU 학습에서는 test 기준 `mAP50 0.968`, `Recall 0.953`을 확보했으며, 프로젝트 적용용 모델 파일 `license_plate_yolo11n_best.pt`를 생성할 수 있음을 확인했다.

다만 최종 적용 모델로 확정하기 전에는 GPU 환경에서 더 긴 epoch와 높은 이미지 크기로 재학습하고, EasyOCR까지 포함한 번호판 문자열 인식 성공률을 추가 검증해야 한다.
