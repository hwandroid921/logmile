from datetime import datetime, timedelta
from itertools import cycle
from pathlib import Path

from simulator.models import PlateEvent, PlateRecognitionResult


DUMMY_PLATE_NUMBERS = (
    "12가3456",
    "23나4567",
    "34다5678",
    "45라6789",
    "56마7890",
    "67바8901",
    "78사9012",
    "89아0123",
    "90자1234",
    "11차2345",
)


def build_manual_plate_event(
    plate_no: str,
    event_type: str,
    location_type: str,
    observed_at: datetime,
    latitude: float | None,
    longitude: float | None,
) -> PlateEvent:
    return PlateEvent(
        plate_no=plate_no,
        event_type=event_type,
        location_type=location_type,
        source_type="MANUAL",
        observed_at=observed_at,
        latitude=latitude,
        longitude=longitude,
        confidence=1.0,
        is_manual_required=False,
        memo="시뮬레이터 직접 입력 번호판 이벤트",
    )


def build_dummy_plate_events(
    count: int,
    event_type: str,
    location_type: str,
    observed_at: datetime,
    latitude: float | None,
    longitude: float | None,
) -> list[PlateEvent]:
    events: list[PlateEvent] = []
    plates = cycle(DUMMY_PLATE_NUMBERS)

    for index in range(count):
        events.append(
            PlateEvent(
                plate_no=next(plates),
                event_type=event_type,
                location_type=location_type,
                source_type="DUMMY",
                observed_at=observed_at + timedelta(minutes=index),
                latitude=latitude,
                longitude=longitude,
                confidence=1.0,
                is_manual_required=False,
                memo=f"시뮬레이터 더미 번호판 이벤트 #{index + 1}",
            )
        )

    return events


def expand_event_pairs(events: list[PlateEvent], exit_after_minutes: int) -> list[PlateEvent]:
    paired_events: list[PlateEvent] = []

    for event in events:
        entry = PlateEvent(
            plate_no=event.plate_no,
            event_type="ENTRY",
            location_type=event.location_type,
            source_type=event.source_type,
            observed_at=event.observed_at,
            latitude=event.latitude,
            longitude=event.longitude,
            confidence=event.confidence,
            detection_confidence=event.detection_confidence,
            is_manual_required=event.is_manual_required,
            image_path=event.image_path,
            memo=event.memo,
        )
        exit_event = PlateEvent(
            plate_no=event.plate_no,
            event_type="EXIT",
            location_type=event.location_type,
            source_type=event.source_type,
            observed_at=event.observed_at + timedelta(minutes=exit_after_minutes),
            latitude=event.latitude,
            longitude=event.longitude,
            confidence=event.confidence,
            detection_confidence=event.detection_confidence,
            is_manual_required=event.is_manual_required,
            image_path=event.image_path,
            memo=f"{event.memo or '시뮬레이터 번호판 이벤트'} / 입출차 쌍 종료",
        )
        paired_events.extend([entry, exit_event])

    return paired_events


def build_ocr_plate_event(
    result: PlateRecognitionResult,
    image_path: Path,
    event_type: str,
    location_type: str,
    observed_at: datetime,
    latitude: float | None,
    longitude: float | None,
    fallback_plate_no: str | None,
) -> PlateEvent:
    plate_no = result.plate_no or fallback_plate_no
    if not plate_no:
        raise ValueError("OCR 결과 plate_no가 없어 이벤트 생성이 불가합니다. --fallback-plate-no 또는 --dummy-plate를 사용하세요.")

    source_type = "OCR" if result.plate_no else "MANUAL"
    memo = result.fallback_reason if result.is_manual_required else "FastAPI OCR 번호판 인식 이벤트"
    return PlateEvent(
        plate_no=plate_no,
        event_type=event_type,
        location_type=location_type,
        source_type=source_type,
        observed_at=observed_at,
        latitude=latitude,
        longitude=longitude,
        confidence=result.confidence,
        detection_confidence=result.detection_confidence,
        is_manual_required=result.is_manual_required,
        image_path=str(image_path),
        memo=memo,
    )
