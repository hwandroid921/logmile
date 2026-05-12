from dataclasses import dataclass
from os import getenv

try:
    from dotenv import load_dotenv
except ImportError:
    load_dotenv = None


if load_dotenv:
    load_dotenv()


@dataclass(frozen=True)
class SimulatorConfig:
    backend_base_url: str
    gps_endpoint: str
    plate_event_endpoint: str
    ai_base_url: str
    ocr_endpoint: str
    api_token: str | None
    default_drive_log_id: int
    default_start_latitude: float
    default_start_longitude: float
    default_interval_seconds: float

    @property
    def gps_url(self) -> str:
        return f"{self.backend_base_url.rstrip('/')}/{self.gps_endpoint.lstrip('/')}"

    @property
    def plate_event_url(self) -> str:
        return f"{self.backend_base_url.rstrip('/')}/{self.plate_event_endpoint.lstrip('/')}"

    @property
    def ocr_url(self) -> str:
        return f"{self.ai_base_url.rstrip('/')}/{self.ocr_endpoint.lstrip('/')}"


def load_config() -> SimulatorConfig:
    token = getenv("API_TOKEN", "").strip()
    return SimulatorConfig(
        backend_base_url=getenv("BE_API_URL", "http://localhost:8080"),
        gps_endpoint=getenv("GPS_ENDPOINT", "/api/gps"),
        plate_event_endpoint=getenv("PLATE_EVENT_ENDPOINT", "/api/simulation/plate-events"),
        ai_base_url=getenv("AI_BASE_URL", "http://localhost:8000"),
        ocr_endpoint=getenv("OCR_ENDPOINT", "/api/ocr/recognize"),
        api_token=token or None,
        default_drive_log_id=int(getenv("DEFAULT_DRIVE_LOG_ID", "1")),
        default_start_latitude=float(getenv("DEFAULT_START_LATITUDE", "37.5665")),
        default_start_longitude=float(getenv("DEFAULT_START_LONGITUDE", "126.9780")),
        default_interval_seconds=float(getenv("DEFAULT_INTERVAL_SECONDS", "0.1")),
    )
