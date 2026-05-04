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
        plate_region = yolo_service.detect_plate_region(image)

        # 2단계: EasyOCR 문자 추출
        plate_no, confidence = ocr_service.extract_text(plate_region)

        # 3단계: 신뢰도 판정
        threshold = settings.OCR_CONFIDENCE_THRESHOLD
        is_manual_required = confidence < threshold

        if is_manual_required:
            plate_no = None

        return OcrResponse(
            plate_no=plate_no,
            confidence=confidence,
            is_manual_required=is_manual_required,
        )


plate_service = PlateService()
