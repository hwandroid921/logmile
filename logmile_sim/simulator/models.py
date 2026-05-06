from dataclasses import asdict, dataclass
from datetime import datetime
from typing import Any


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


@dataclass(frozen=True)
class PlateRecognitionResult:
    plate_no: str | None
    confidence: float
    is_manual_required: bool
    detection_confidence: float | None = None
    is_detected: bool | None = None
    fallback_reason: str | None = None

    @classmethod
    def from_response(cls, data: dict[str, Any]) -> "PlateRecognitionResult":
        return cls(
            plate_no=data.get("plate_no"),
            confidence=float(data.get("confidence") or 0.0),
            is_manual_required=bool(data.get("is_manual_required")),
            detection_confidence=(
                float(data["detection_confidence"])
                if data.get("detection_confidence") is not None
                else None
            ),
            is_detected=data.get("is_detected"),
            fallback_reason=data.get("fallback_reason"),
        )

    def to_debug_dict(self) -> dict:
        return asdict(self)


@dataclass(frozen=True)
class PlateEvent:
    plate_no: str
    event_type: str
    location_type: str
    source_type: str
    observed_at: datetime
    latitude: float | None = None
    longitude: float | None = None
    confidence: float | None = None
    detection_confidence: float | None = None
    is_manual_required: bool = False
    image_path: str | None = None
    memo: str | None = None

    def to_request(self) -> dict:
        payload = {
            "plateNo": self.plate_no,
            "eventType": self.event_type,
            "locationType": self.location_type,
            "sourceType": self.source_type,
            "observedAt": self.observed_at.isoformat(timespec="seconds"),
            "latitude": self.latitude,
            "longitude": self.longitude,
            "confidence": self.confidence,
            "detectionConfidence": self.detection_confidence,
            "isManualRequired": self.is_manual_required,
            "imagePath": self.image_path,
            "memo": self.memo,
        }
        return {key: value for key, value in payload.items() if value is not None}

    def to_debug_dict(self) -> dict:
        return self.to_request()
