from datetime import datetime, timedelta

from simulator.models import GpsPoint


def generate_drive_segment(
    drive_log_id: int,
    start_at: datetime,
    minutes: int,
    start_latitude: float,
    start_longitude: float,
    speed_kmh: float = 72.0,
) -> list[GpsPoint]:
    points: list[GpsPoint] = []
    for minute in range(minutes):
        points.append(
            GpsPoint(
                drive_log_id=drive_log_id,
                latitude=start_latitude + minute * 0.001,
                longitude=start_longitude + minute * 0.001,
                speed_kmh=speed_kmh,
                recorded_at=start_at + timedelta(minutes=minute),
            )
        )
    return points
