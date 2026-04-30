from collections.abc import Iterable
from time import sleep

import requests

from simulator.config import SimulatorConfig
from simulator.models import GpsPoint


class GpsSender:
    def __init__(self, config: SimulatorConfig):
        self.config = config

    def send(self, points: Iterable[GpsPoint], interval_seconds: float) -> int:
        sent_count = 0
        headers = {"Content-Type": "application/json"}
        if self.config.api_token:
            headers["Authorization"] = f"Bearer {self.config.api_token}"

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
