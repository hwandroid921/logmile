from pydantic import BaseModel
from typing import Optional


class OcrResponse(BaseModel):
    """
    번호판 인식 응답 스키마

    - plate_no: 인식된 번호판 문자열 (신뢰도 미달 시 null)
    - confidence: OCR 신뢰도 점수 (0.0 ~ 1.0)
    - is_manual_required: 수동 입력 필요 여부 (confidence < 0.85 이면 true)
    """
    plate_no: Optional[str] = None
    confidence: float
    is_manual_required: bool


class OcrErrorResponse(BaseModel):
    detail: str
