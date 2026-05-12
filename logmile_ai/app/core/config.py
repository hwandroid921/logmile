import os
from dotenv import load_dotenv

load_dotenv()


class Settings:
    AI_HOST: str = os.getenv("AI_HOST", "0.0.0.0")
    AI_PORT: int = int(os.getenv("AI_PORT", "8000"))

    # OCR 신뢰도 임계값 — 이 값 미만이면 수동 입력 필요
    OCR_CONFIDENCE_THRESHOLD: float = float(os.getenv("OCR_CONFIDENCE_THRESHOLD", "0.85"))

    # YOLO11 번호판 탐지 모델 경로
    YOLO_MODEL_PATH: str = os.getenv("YOLO_MODEL_PATH", "app/model/yolo11n.pt")

    DEBUG: bool = os.getenv("DEBUG", "false").lower() == "true"


settings = Settings()
