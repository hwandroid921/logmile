from collections.abc import Iterable
from pathlib import Path
from time import sleep
from mimetypes import guess_type

import requests

from simulator.config import SimulatorConfig
from simulator.models import GpsPoint, PlateEvent, PlateRecognitionResult


def build_headers(config: SimulatorConfig, content_type: str | None = "application/json") -> dict[str, str]:
    headers = {}
    if content_type:
        headers["Content-Type"] = content_type
    if config.api_token:
        headers["Authorization"] = f"Bearer {config.api_token}"
    return headers


class GpsSender:
    def __init__(self, config: SimulatorConfig):
        self.config = config

    def send(self, points: Iterable[GpsPoint], interval_seconds: float) -> int:
        sent_count = 0
        headers = build_headers(self.config)

        for point in points:
            response = requests.post(
                self.config.gps_url,
                json=point.to_request(),
                headers=headers,
                timeout=5,
            )
            response.raise_for_status()
            sent_count += 1
            if interval_seconds > 0:
                sleep(interval_seconds)

        return sent_count


class AiOcrClient:
    def __init__(self, config: SimulatorConfig):
        self.config = config

    def recognize(self, image_path: str | Path) -> PlateRecognitionResult:
        path = Path(image_path)
        if not path.is_file():
            raise FileNotFoundError(f"번호판 이미지 파일을 찾을 수 없습니다: {path}")

        content_type = guess_type(path.name)[0] or "application/octet-stream"
        headers = build_headers(self.config, content_type=None)

        with path.open("rb") as image_file:
            response = requests.post(
                self.config.ocr_url,
                files={"file": (path.name, image_file, content_type)},
                headers=headers,
                timeout=15,
            )

        response.raise_for_status()
        return PlateRecognitionResult.from_response(response.json())


class PlateEventSender:
    def __init__(self, config: SimulatorConfig):
        self.config = config

    def send(self, events: Iterable[PlateEvent]) -> int:
        sent_count = 0
        headers = build_headers(self.config)

        for event in events:
            response = requests.post(
                self.config.plate_event_url,
                json=event.to_request(),
                headers=headers,
                timeout=5,
            )
            response.raise_for_status()
            sent_count += 1

        return sent_count
