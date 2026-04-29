from datetime import datetime, timedelta

from simulator.models import GpsPoint


def generate_rest_segment(
    drive_log_id: int,
    start_at: datetime,
    minutes: int,
    latitude: float,
    longitude: float,
    speed_kmh: float = 2.0,
) -> list[GpsPoint]:
    points: list[GpsPoint] = []
    for minute in range(minutes):
        points.append(
            GpsPoint(
                drive_log_id=drive_log_id,
                latitude=latitude,
                longitude=longitude,
                speed_kmh=speed_kmh,
                recorded_at=start_at + timedelta(minutes=minute),
            )
        )
    return points
