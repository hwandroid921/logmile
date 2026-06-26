import re
from typing import Tuple, Optional
from PIL import Image, ImageFilter, ImageOps
import numpy as np


class OcrService:
    """
    EasyOCR 기반 번호판 문자 추출 서비스

    - EasyOCR Reader는 최초 호출 시 lazy 초기화한다.
    - GPU 없는 환경을 고려해 gpu=False로 기본 설정한다.
    """

    def __init__(self):
        self._reader = None

    def _get_reader(self):
        if self._reader is None:
            try:
                import easyocr
                self._reader = easyocr.Reader(["ko", "en"], gpu=False)
                print("[OcrService] EasyOCR Reader 초기화 완료")
            except Exception as e:
                print(f"[OcrService] EasyOCR 초기화 실패: {e}")
        return self._reader

    def extract_text(self, image: Image.Image) -> Tuple[Optional[str], float]:
        """
        이미지에서 번호판 문자열과 신뢰도를 추출한다.

        :param image: PIL Image 객체 (번호판 크롭 또는 원본)
        :return: (plate_no, confidence) — 추출 실패 시 (None, 0.0)
        """
        reader = self._get_reader()
        if reader is None:
            return None, 0.0

        try:
            image_np = np.array(self._preprocess(image))
            results = reader.readtext(image_np)

            if not results:
                return None, 0.0

            # 신뢰도 가장 높은 결과 선택
            best = max(results, key=lambda r: r[2])
            _, text, confidence = best

            plate_no = self._normalize_plate_text(text)
            if not plate_no:
                return None, round(float(confidence), 4)
            return plate_no, round(float(confidence), 4)

        except Exception as e:
            print(f"[OcrService] OCR 추출 오류: {e}")
            return None, 0.0

    def _preprocess(self, image: Image.Image) -> Image.Image:
        """
        번호판 crop이 작을 때 OCR 입력 최소 크기를 확보한다.
        모자이크 데이터에서는 문자열 판독이 실패할 수 있으며, 이는 fallback으로 처리한다.
        """
        target_width = 240
        scale = max(1, int(target_width / max(1, image.width)))
        if scale > 1:
            image = image.resize(
                (image.width * scale, image.height * scale),
                Image.Resampling.LANCZOS,
            )

        gray = ImageOps.grayscale(image)
        return ImageOps.autocontrast(gray).filter(ImageFilter.SHARPEN).convert("RGB")

    def _normalize_plate_text(self, text: str) -> str:
        normalized = re.sub(r"[^0-9가-힣A-Za-z]", "", text)
        return normalized.upper()


ocr_service = OcrService()
