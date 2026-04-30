from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.core.config import settings
from app.router import ocr_router

app = FastAPI(
    title="logmile AI Server",
    description="화물차 번호판 인식 API (YOLO11 + EasyOCR)",
    version="0.1.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

# CORS — 백엔드(Spring Boot)에서만 호출하므로 내부 통신 허용
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 라우터 등록
app.include_router(ocr_router.router)


@app.get("/health", tags=["Health"])
def health_check():
    return {"status": "ok", "service": "logmile-ai"}
