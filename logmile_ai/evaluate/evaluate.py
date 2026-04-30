"""
logmile 번호판 탐지 모델 성능 평가 스크립트

학습된 best.pt 가중치를 검증 데이터셋에서 평가하여
mAP50, Precision, Recall, 추론 속도를 측정합니다.

실행 방법:
    # 단일 모델 평가
    python evaluate/evaluate.py --weight runs/train/logmile_plate_yolo11n/weights/best.pt

    # YOLO11n vs YOLOv8n 비교 평가
    python evaluate/evaluate.py --compare
"""

import argparse
import json
import time
from pathlib import Path

import yaml
from ultralytics import YOLO


BASE_DIR    = Path(__file__).resolve().parent.parent
DATA_YAML   = BASE_DIR / "dataset" / "data.yaml"
RESULT_DIR  = BASE_DIR / "evaluate" / "results"

# 비교 평가 대상 (compare.py 기본 출력 경로)
COMPARE_WEIGHTS = [
    {
        "name":   "YOLO11n",
        "weight": "runs/compare/logmile_plate_yolo11n/weights/best.pt",
    },
    {
        "name":   "YOLOv8n",
        "weight": "runs/compare/logmile_plate_yolov8n/weights/best.pt",
    },
]


def parse_args():
    parser = argparse.ArgumentParser(description="logmile 번호판 탐지 모델 평가")
    parser.add_argument("--weight",  type=str, default=None,
                        help="평가할 best.pt 경로")
    parser.add_argument("--data",    type=str, default=str(DATA_YAML),
                        help="data.yaml 경로 (기본: dataset/data.yaml)")
    parser.add_argument("--imgsz",   type=int, default=640)
    parser.add_argument("--batch",   type=int, default=16)
    parser.add_argument("--device",  type=str, default=None)
    parser.add_argument("--compare", action="store_true",
                        help="YOLO11n vs YOLOv8n 비교 평가 모드")
    return parser.parse_args()


def evaluate_model(weight_path: str, data_yaml: str, imgsz: int,
                   batch: int, device: str | None) -> dict:
    """단일 모델 평가 실행, 결과 dict 반환"""
    weight = Path(weight_path)
    if not weight.exists():
        # BASE_DIR 기준 상대경로도 시도
        weight = BASE_DIR / weight_path
    if not weight.exists():
        raise FileNotFoundError(f"가중치 파일 없음: {weight_path}")

    model = YOLO(str(weight))

    val_kwargs = dict(
        data=data_yaml,
        imgsz=imgsz,
        batch=batch,
        verbose=False,
    )
    if device:
        val_kwargs["device"] = device

    # 추론 속도 측정
    start = time.perf_counter()
    metrics = model.val(**val_kwargs)
    elapsed = time.perf_counter() - start

    # 지표 추출
    map50      = float(metrics.box.map50)
    map50_95   = float(metrics.box.map)
    precision  = float(metrics.box.mp)
    recall     = float(metrics.box.mr)
    speed_ms   = round(elapsed / max(metrics.box.nc, 1) * 1000, 2)  # 근사치

    return {
        "weight":    str(weight),
        "mAP50":     round(map50, 4),
        "mAP50-95":  round(map50_95, 4),
        "Precision": round(precision, 4),
        "Recall":    round(recall, 4),
        "speed_ms":  speed_ms,
    }


def print_result(name: str, r: dict):
    print(f"\n  [{name}]")
    print(f"    mAP50      : {r['mAP50']}")
    print(f"    mAP50-95   : {r['mAP50-95']}")
    print(f"    Precision  : {r['Precision']}")
    print(f"    Recall     : {r['Recall']}")
    print(f"    Speed(ms)  : {r['speed_ms']}")


def save_results(results: list[dict], label: str):
    RESULT_DIR.mkdir(parents=True, exist_ok=True)
    out_path = RESULT_DIR / f"{label}.json"
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    print(f"\n  결과 저장: {out_path}")


def main():
    args = parse_args()

    print("=" * 60)
    print("  logmile 번호판 탐지 모델 성능 평가")
    print("=" * 60)

    if args.compare:
        # ── 비교 평가 모드 ─────────────────────────
        results = []
        for m in COMPARE_WEIGHTS:
            print(f"\n  [{m['name']}] 평가 중...")
            try:
                r = evaluate_model(m["weight"], args.data, args.imgsz, args.batch, args.device)
                r["model"] = m["name"]
                results.append(r)
                print_result(m["name"], r)
            except FileNotFoundError as e:
                print(f"  [건너뜀] {e}")

        if results:
            save_results(results, "compare_result")
            _print_winner(results)

    else:
        # ── 단일 모델 평가 ─────────────────────────
        if not args.weight:
            print("  --weight 또는 --compare 옵션을 지정하세요.")
            return
        r = evaluate_model(args.weight, args.data, args.imgsz, args.batch, args.device)
        r["model"] = Path(args.weight).parent.parent.name
        print_result(r["model"], r)
        save_results([r], r["model"])

    print("\n" + "=" * 60)


def _print_winner(results: list[dict]):
    """mAP50 기준 최우수 모델 출력"""
    best = max(results, key=lambda x: x["mAP50"])
    print("\n" + "─" * 60)
    print(f"  ✅ 최우수 모델 (mAP50 기준): {best['model']}")
    print(f"     mAP50 {best['mAP50']}  |  Precision {best['Precision']}  |  Recall {best['Recall']}")
    print("─" * 60)
    print("  → feature/ai-ocr-license-plate 에서 해당 best.pt 적용 예정")


if __name__ == "__main__":
    main()
