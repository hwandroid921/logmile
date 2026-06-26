from fastapi import APIRouter, UploadFile, File, HTTPException
from PIL import Image
import io

from app.service.plate_service import plate_service
from app.schema.ocr_response import OcrResponse

router = APIRouter(prefix="/api/ocr", tags=["OCR"])

ALLOWED_CONTENT_TYPES = {"image/jpeg", "image/jpg", "image/png", "image/bmp"}


@router.post(
    "/recognize",
    response_model=OcrResponse,
    summary="번호판 인식",
    description=(
        "업로드된 이미지에서 번호판을 탐지하고 문자를 추출합니다.\n\n"
        "- 신뢰도 ≥ 0.85: plate_no 반환, is_manual_required=false\n"
        "- 신뢰도 < 0.85: plate_no=null, is_manual_required=true"
    ),
)
async def recognize_plate(file: UploadFile = File(...)) -> OcrResponse:
    # 파일 형식 검증
    if file.content_type not in ALLOWED_CONTENT_TYPES:
        raise HTTPException(
            status_code=400,
            detail=f"지원하지 않는 파일 형식입니다. 허용 형식: {', '.join(ALLOWED_CONTENT_TYPES)}",
        )

    # 이미지 읽기
    contents = await file.read()
    try:
        image = Image.open(io.BytesIO(contents)).convert("RGB")
    except Exception:
        raise HTTPException(status_code=400, detail="이미지 파일을 읽을 수 없습니다.")

    # 번호판 인식
    result = plate_service.recognize(image)
    return result
