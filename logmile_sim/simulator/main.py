import argparse
from datetime import datetime
from pathlib import Path

from simulator.config import load_config
from simulator.scenarios import scenario_a, scenario_b, scenario_c


SCENARIO_GENERATORS = {
    "A": scenario_a.generate,
    "B": scenario_b.generate,
    "C": scenario_c.generate,
}

PLATE_EVENT_TYPES = ("ENTRY", "EXIT")
PLATE_LOCATION_TYPES = ("HIGHWAY_GATE", "REST_AREA", "CCTV")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="logmile GPS scenario simulator")
    parser.add_argument("--scenario", choices=SCENARIO_GENERATORS.keys(), default="A")
    parser.add_argument("--drive-log-id", type=int)
    parser.add_argument("--send", action="store_true", help="send points to backend")
    parser.add_argument("--dry-run", action="store_true", help="print sample points only")
    parser.add_argument("--ocr-image", help="send one plate image to FastAPI OCR model")
    parser.add_argument("--ocr-dir", help="send plate images in a directory to FastAPI OCR model")
    parser.add_argument("--ocr-limit", type=int, help="limit number of OCR images")
    parser.add_argument("--send-event", action="store_true", help="send plate events to backend")
    parser.add_argument("--plate-no", help="manual plate number for plate event")
    parser.add_argument("--fallback-plate-no", help="manual plate number when OCR text is empty")
    parser.add_argument("--dummy-plate", action="store_true", help="generate dummy plate events")
    parser.add_argument("--dummy-count", type=int, default=1, help="number of dummy plate events")
    parser.add_argument("--event-type", choices=PLATE_EVENT_TYPES, default="ENTRY")
    parser.add_argument("--event-pair", action="store_true", help="generate ENTRY and EXIT events")
    parser.add_argument("--exit-after-minutes", type=int, default=30)
    parser.add_argument("--location-type", choices=PLATE_LOCATION_TYPES, default="REST_AREA")
    parser.add_argument("--event-at", help="ISO datetime for plate event")
    parser.add_argument("--event-latitude", type=float)
    parser.add_argument("--event-longitude", type=float)
    parser.add_argument("--interval", type=float)
    parser.add_argument("--limit", type=int, help="limit number of generated points")
    parser.add_argument(
        "--start-at",
        help="ISO datetime. Example: 2026-04-29T09:00:00",
    )
    return parser.parse_args()


def parse_event_datetime(args: argparse.Namespace) -> datetime:
    if args.event_at:
        return datetime.fromisoformat(args.event_at)
    if args.start_at:
        return datetime.fromisoformat(args.start_at)
    return datetime.now()


def collect_ocr_image_paths(args: argparse.Namespace) -> list[Path]:
    image_paths: list[Path] = []

    if args.ocr_image:
        image_paths.append(Path(args.ocr_image))

    if args.ocr_dir:
        directory = Path(args.ocr_dir)
        patterns = ("*.jpg", "*.jpeg", "*.png", "*.bmp")
        for pattern in patterns:
            image_paths.extend(sorted(directory.glob(pattern)))

    if args.ocr_limit:
        image_paths = image_paths[: args.ocr_limit]

    return image_paths


def run_ocr(args: argparse.Namespace) -> list:
    if not args.ocr_image and not args.ocr_dir:
        return []

    from simulator.plate_events import build_ocr_plate_event
    from simulator.sender import AiOcrClient

    config = load_config()
    client = AiOcrClient(config)
    image_paths = collect_ocr_image_paths(args)
    events = []

    for image_path in image_paths:
        result = client.recognize(image_path)
        print(
            {
                "image": str(image_path),
                "ocrUrl": config.ocr_url,
                "result": result.to_debug_dict(),
            }
        )
        if args.send_event or args.fallback_plate_no:
            events.append(
                build_ocr_plate_event(
                    result=result,
                    image_path=image_path,
                    event_type=args.event_type,
                    location_type=args.location_type,
                    observed_at=parse_event_datetime(args),
                    latitude=args.event_latitude,
                    longitude=args.event_longitude,
                    fallback_plate_no=args.fallback_plate_no,
                )
            )

    return events


def run_plate_events(args: argparse.Namespace, ocr_events: list) -> bool:
    if not (args.plate_no or args.dummy_plate or ocr_events):
        return False

    from simulator.plate_events import build_dummy_plate_events, build_manual_plate_event, expand_event_pairs

    config = load_config()
    observed_at = parse_event_datetime(args)
    events = list(ocr_events)

    if args.plate_no:
        events.append(
            build_manual_plate_event(
                plate_no=args.plate_no,
                event_type=args.event_type,
                location_type=args.location_type,
                observed_at=observed_at,
                latitude=args.event_latitude,
                longitude=args.event_longitude,
            )
        )

    if args.dummy_plate:
        events.extend(
            build_dummy_plate_events(
                count=args.dummy_count,
                event_type=args.event_type,
                location_type=args.location_type,
                observed_at=observed_at,
                latitude=args.event_latitude,
                longitude=args.event_longitude,
            )
        )

    if args.event_pair:
        events = expand_event_pairs(events, args.exit_after_minutes)

    for event in events:
        print({"plateEventUrl": config.plate_event_url, "event": event.to_debug_dict()})

    if not args.send_event:
        return True

    from simulator.sender import PlateEventSender

    sent_count = PlateEventSender(config).send(events)
    print(f"plateEventsSent={sent_count} url={config.plate_event_url}")
    return True


def main() -> None:
    args = parse_args()
    ocr_events = run_ocr(args)

    if run_plate_events(args, ocr_events):
        return

    if args.ocr_image or args.ocr_dir:
        return

    config = load_config()
    drive_log_id = args.drive_log_id or config.default_drive_log_id
    start_at = datetime.fromisoformat(args.start_at) if args.start_at else datetime.now()

    generator = SCENARIO_GENERATORS[args.scenario]
    points = generator(
        drive_log_id,
        start_at,
        config.default_start_latitude,
        config.default_start_longitude,
    )
    if args.limit:
        points = points[: args.limit]

    print(f"scenario={args.scenario} driveLogId={drive_log_id} points={len(points)}")
    print(f"first={points[0].to_request()}")
    print(f"last={points[-1].to_request()}")

    if args.dry_run or not args.send:
        return

    from simulator.sender import GpsSender

    interval = args.interval
    if interval is None:
        interval = config.default_interval_seconds

    sent_count = GpsSender(config).send(points, interval)
    print(f"sent={sent_count} url={config.gps_url}")


if __name__ == "__main__":
    main()
