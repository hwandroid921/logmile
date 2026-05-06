from typing import Optional
from PIL import Image

from app.core.config import settings
from app.service.yolo_service import yolo_service
from app.service.ocr_service import ocr_service
from app.schema.ocr_response import OcrResponse


class PlateService:
    """
    번호판 인식 통합 서비스

    흐름:
    1. YoloService — 번호판 영역 탐지 및 크롭
    2. OcrService  — 크롭 이미지에서 문자 추출 + 신뢰도
    3. 신뢰도 판정 — OCR_CONFIDENCE_THRESHOLD(0.85) 기준
       - 이상: plate_no 반환, is_manual_required=false
       - 미만: plate_no=null, is_manual_required=true
    """

    def recognize(self, image: Image.Image) -> OcrResponse:
        """
        번호판 이미지를 입력받아 인식 결과를 반환한다.

        :param image: PIL Image 객체
        :return: OcrResponse
        """
        # 1단계: YOLO 번호판 영역 탐지
        detection = yolo_service.detect_plate(image)

        # 2단계: EasyOCR 문자 추출
        plate_no, confidence = ocr_service.extract_text(detection.image)

        # 3단계: 신뢰도 판정
        threshold = settings.OCR_CONFIDENCE_THRESHOLD
        is_manual_required = confidence < threshold
        fallback_reason = None

        if is_manual_required:
            fallback_reason = self._get_fallback_reason(detection.is_detected, plate_no, confidence)
            plate_no = None

        return OcrResponse(
            plate_no=plate_no,
            confidence=confidence,
            is_manual_required=is_manual_required,
            detection_confidence=detection.confidence,
            is_detected=detection.is_detected,
            fallback_reason=fallback_reason,
        )

    def _get_fallback_reason(self, is_detected: bool, plate_no: Optional[str], confidence: float) -> str:
        if not is_detected:
            return "번호판 영역을 탐지하지 못했습니다."
        if plate_no is None:
            return "OCR 문자 판독 결과가 없습니다. 비식별/모자이크 데이터에서는 정상 fallback일 수 있습니다."
        return f"OCR 신뢰도가 기준값보다 낮습니다. confidence={confidence}"


plate_service = PlateService()
