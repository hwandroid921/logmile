import argparse
from datetime import datetime

from simulator.config import load_config
from simulator.scenarios import scenario_a, scenario_b, scenario_c


SCENARIO_GENERATORS = {
    "A": scenario_a.generate,
    "B": scenario_b.generate,
    "C": scenario_c.generate,
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="logmile GPS scenario simulator")
    parser.add_argument("--scenario", choices=SCENARIO_GENERATORS.keys(), default="A")
    parser.add_argument("--drive-log-id", type=int)
    parser.add_argument("--send", action="store_true", help="send points to backend")
    parser.add_argument("--dry-run", action="store_true", help="print sample points only")
    parser.add_argument("--interval", type=float)
    parser.add_argument("--limit", type=int, help="limit number of generated points")
    parser.add_argument(
        "--start-at",
        help="ISO datetime. Example: 2026-04-29T09:00:00",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
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
