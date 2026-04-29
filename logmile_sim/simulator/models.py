from dataclasses import asdict, dataclass
from datetime import datetime


@dataclass(frozen=True)
class GpsPoint:
    drive_log_id: int
    latitude: float
    longitude: float
    speed_kmh: float
    recorded_at: datetime

    def to_request(self) -> dict:
        return {
            "driveLogId": self.drive_log_id,
            "latitude": round(self.latitude, 6),
            "longitude": round(self.longitude, 6),
            "speedKmh": round(self.speed_kmh, 1),
            "recordedAt": self.recorded_at.isoformat(timespec="seconds"),
        }

    def to_debug_dict(self) -> dict:
        data = asdict(self)
        data["recorded_at"] = self.recorded_at.isoformat(timespec="seconds")
        return data
