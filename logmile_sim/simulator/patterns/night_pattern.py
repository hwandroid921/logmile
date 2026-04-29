from datetime import datetime


def night_start_for(base_date: datetime, hour: int = 22, minute: int = 0) -> datetime:
    return base_date.replace(hour=hour, minute=minute, second=0, microsecond=0)
