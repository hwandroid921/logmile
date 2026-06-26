import os
from dataclasses import dataclass
from typing import Optional
from PIL import Image
from app.core.config import settings


@dataclass(frozen=True)
class PlateDetectionResult:
    image: Image.Image
    is_detected: bool
    confidence: Optional[float] = None


class YoloService:
    """
    YOLO11n 기반 번호판 영역 탐지 서비스

    - 모델이 존재하면 실제 탐지를 수행한다.
    - 모델이 없으면 None을 반환하고 후속 단계에서 fallback 처리한다.
    """

    def __init__(self):
        self._model = None
        self._load_model()

    def _load_model(self):
        model_path = settings.YOLO_MODEL_PATH
        if not os.path.exists(model_path):
            print(f"[YoloService] 모델 파일 없음: {model_path} — OCR 직접 실행 모드로 전환")
            return
        try:
            from ultralytics import YOLO
            self._model = YOLO(model_path)
            print(f"[YoloService] 모델 로드 완료: {model_path}")
        except Exception as e:
            print(f"[YoloService] 모델 로드 실패: {e}")

    def detect_plate(self, image: Image.Image) -> PlateDetectionResult:
        """
        이미지에서 번호판 영역을 탐지하고 크롭 결과와 탐지 정보를 반환한다.
        탐지 실패 시 원본 이미지를 반환한다.

        :param image: PIL Image 객체
        :return: 번호판 탐지 결과
        """
        if self._model is None:
            # 모델 없음 — 원본 이미지를 그대로 OCR에 넘긴다
            return PlateDetectionResult(image=image, is_detected=False)

        try:
            results = self._model(image, verbose=False)
            boxes = results[0].boxes

            if boxes is None or len(boxes) == 0:
                return PlateDetectionResult(image=image, is_detected=False)

            # 신뢰도 가장 높은 bbox 선택
            best_box = max(boxes, key=lambda b: float(b.conf[0]))
            x1, y1, x2, y2 = map(int, best_box.xyxy[0].tolist())
            confidence = round(float(best_box.conf[0]), 4)
            cropped = image.crop((x1, y1, x2, y2))
            return PlateDetectionResult(
                image=cropped,
                is_detected=True,
                confidence=confidence,
            )

        except Exception as e:
            print(f"[YoloService] 탐지 오류: {e}")
            return PlateDetectionResult(image=image, is_detected=False)

    def detect_plate_region(self, image: Image.Image) -> Image.Image:
        """
        기존 호출부 호환용 메서드.
        """
        return self.detect_plate(image).image


yolo_service = YoloService()
