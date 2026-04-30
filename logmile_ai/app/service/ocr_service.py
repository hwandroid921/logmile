from typing import Tuple, Optional
from PIL import Image
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
            image_np = np.array(image)
            results = reader.readtext(image_np)

            if not results:
                return None, 0.0

            # 신뢰도 가장 높은 결과 선택
            best = max(results, key=lambda r: r[2])
            _, text, confidence = best

            plate_no = text.strip().replace(" ", "")
            return plate_no, round(float(confidence), 4)

        except Exception as e:
            print(f"[OcrService] OCR 추출 오류: {e}")
            return None, 0.0


ocr_service = OcrService()
