from datetime import datetime, timedelta

from simulator.models import GpsPoint
from simulator.patterns.movement_pattern import generate_drive_segment
from simulator.patterns.night_pattern import night_start_for
from simulator.patterns.rest_pattern import generate_rest_segment


def generate(
    drive_log_id: int,
    start_at: datetime,
    start_latitude: float,
    start_longitude: float,
) -> list[GpsPoint]:
    scenario_start = night_start_for(start_at, 22, 0)
    long_drive = generate_drive_segment(
        drive_log_id, scenario_start, 260, start_latitude, start_longitude, 78.0
    )
    invalid_rest_start = long_drive[-1].recorded_at + timedelta(minutes=1)
    invalid_rest = generate_rest_segment(
        drive_log_id,
        invalid_rest_start,
        5,
        long_drive[-1].latitude,
        long_drive[-1].longitude,
    )
    second_start = invalid_rest[-1].recorded_at + timedelta(minutes=1)
    second_drive = generate_drive_segment(
        drive_log_id,
        second_start,
        120,
        invalid_rest[-1].latitude,
        invalid_rest[-1].longitude,
        76.0,
    )
    return long_drive + invalid_rest + second_drive
