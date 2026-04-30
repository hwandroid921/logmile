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
    scenario_start = night_start_for(start_at, 21, 30)
    first_drive = generate_drive_segment(
        drive_log_id, scenario_start, 150, start_latitude, start_longitude, 70.0
    )
    rest_start = first_drive[-1].recorded_at + timedelta(minutes=1)
    short_rest = generate_rest_segment(
        drive_log_id,
        rest_start,
        10,
        first_drive[-1].latitude,
        first_drive[-1].longitude,
    )
    second_start = short_rest[-1].recorded_at + timedelta(minutes=1)
    second_drive = generate_drive_segment(
        drive_log_id,
        second_start,
        70,
        short_rest[-1].latitude,
        short_rest[-1].longitude,
        74.0,
    )
    return first_drive + short_rest + second_drive
