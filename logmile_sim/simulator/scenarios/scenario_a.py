from datetime import datetime, timedelta

from simulator.models import GpsPoint
from simulator.patterns.movement_pattern import generate_drive_segment
from simulator.patterns.rest_pattern import generate_rest_segment


def generate(
    drive_log_id: int,
    start_at: datetime,
    start_latitude: float,
    start_longitude: float,
) -> list[GpsPoint]:
    first_drive = generate_drive_segment(
        drive_log_id, start_at, 80, start_latitude, start_longitude, 68.0
    )
    rest_start = first_drive[-1].recorded_at + timedelta(minutes=1)
    rest = generate_rest_segment(
        drive_log_id,
        rest_start,
        30,
        first_drive[-1].latitude,
        first_drive[-1].longitude,
    )
    second_start = rest[-1].recorded_at + timedelta(minutes=1)
    second_drive = generate_drive_segment(
        drive_log_id,
        second_start,
        60,
        rest[-1].latitude,
        rest[-1].longitude,
        65.0,
    )
    return first_drive + rest + second_drive
