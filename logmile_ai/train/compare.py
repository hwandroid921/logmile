"""
logmile YOLO11n vs YOLOv8n 비교 학습 스크립트

동일 데이터셋·하이퍼파라미터로 두 모델을 순차 학습하여
성능을 비교합니다. 결과는 runs/compare/ 에 저장됩니다.

실행 방법:
    python train/compare.py
    python train/compare.py --epochs 50 --batch 16
"""

import argparse
from pathlib import Path

import yaml
from ultralytics import YOLO


BASE_DIR    = Path(__file__).resolve().parent.parent
DEFAULT_CONFIG = BASE_DIR / "train" / "train_config.yaml"

# 비교 대상 모델
COMPARE_MODELS = [
    {"key": "yolo11n", "weight": "yolo11n.pt"},
    {"key": "yolov8n", "weight": "yolov8n.pt"},
]


def load_config(config_path: Path) -> dict:
    with open(config_path, "r", encoding="utf-8") as f:
        return yaml.safe_load(f)


def parse_args():
    parser = argparse.ArgumentParser(description="YOLO11n vs YOLOv8n 비교 학습")
    parser.add_argument("--config", type=str, default=str(DEFAULT_CONFIG))
    parser.add_argument("--epochs", type=int, default=None)
    parser.add_argument("--batch",  type=int, default=None)
    parser.add_argument("--device", type=str, default=None)
    return parser.parse_args()


def run_training(weight: str, name: str, cfg: dict, args) -> dict:
    """단일 모델 학습 실행 후 결과 반환"""
    model = YOLO(weight)

    # data 절대경로
    data_path = Path(cfg["data"])
    if not data_path.is_absolute():
        data_path = (BASE_DIR / "train" / data_path).resolve()

    train_kwargs = {k: v for k, v in cfg.items() if k not in ("model", "name")}
    train_kwargs["data"]    = str(data_path)
    train_kwargs["project"] = "runs/compare"
    train_kwargs["name"]    = name

    if args.epochs is not None:
        train_kwargs["epochs"] = args.epochs
    if args.batch is not None:
        train_kwargs["batch"] = args.batch
    if args.device is not None:
        train_kwargs["device"] = args.device

    print(f"\n{'='*60}")
    print(f"  [{name}] 학습 시작")
    print(f"{'='*60}")
    results = model.train(**train_kwargs)

    best = Path(results.save_dir) / "weights" / "best.pt"
    return {
        "model":    name,
        "save_dir": str(results.save_dir),
        "best_pt":  str(best) if best.exists() else None,
    }


def main():
    args = parse_args()
    cfg  = load_config(Path(args.config))

    print("=" * 60)
    print("  logmile YOLO11n vs YOLOv8n 비교 학습")
    print("=" * 60)

    summary = []
    for m in COMPARE_MODELS:
        result = run_training(
            weight=m["weight"],
            name=f"logmile_plate_{m['key']}",
            cfg=cfg,
            args=args,
        )
        summary.append(result)

    print("\n" + "=" * 60)
    print("  비교 학습 완료 요약")
    print("=" * 60)
    for r in summary:
        print(f"  [{r['model']}]")
        print(f"    저장 경로 : {r['save_dir']}")
        print(f"    best.pt   : {r['best_pt'] or '없음'}")
    print("-" * 60)
    print("  runs/compare/ 에서 결과 비교 후")
    print("  feature/ai-model-evaluation 에서 최종 모델 선정")
    print("=" * 60)


if __name__ == "__main__":
    main()
