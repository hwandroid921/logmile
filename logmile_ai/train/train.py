"""
logmile YOLO11n 번호판 탐지 모델 학습 스크립트

실행 방법:
    python train/train.py
    python train/train.py --config train/train_config.yaml
    python train/train.py --epochs 150 --batch 32

데이터셋:
    dataset/images/train/, dataset/images/val/
    dataset/labels/train/, dataset/labels/val/
    (실제 데이터는 추후 추가)
"""

import argparse
from pathlib import Path

import yaml
from ultralytics import YOLO


# ── 경로 기준 ─────────────────────────────────
BASE_DIR = Path(__file__).resolve().parent.parent   # logmile_ai/
DEFAULT_CONFIG = BASE_DIR / "train" / "train_config.yaml"


def load_config(config_path: Path) -> dict:
    with open(config_path, "r", encoding="utf-8") as f:
        return yaml.safe_load(f)


def parse_args():
    parser = argparse.ArgumentParser(description="logmile YOLO11n 번호판 탐지 모델 학습")
    parser.add_argument("--config", type=str, default=str(DEFAULT_CONFIG),
                        help="학습 설정 파일 경로 (기본: train/train_config.yaml)")
    parser.add_argument("--epochs",     type=int,   default=None, help="학습 epoch 수 오버라이드")
    parser.add_argument("--batch",      type=int,   default=None, help="배치 크기 오버라이드")
    parser.add_argument("--imgsz",      type=int,   default=None, help="입력 이미지 크기 오버라이드")
    parser.add_argument("--device",     type=str,   default=None,
                        help="학습 디바이스 (예: 0, 0,1, cpu) — 미지정 시 자동 선택")
    parser.add_argument("--resume",     action="store_true",       help="마지막 체크포인트에서 재개")
    return parser.parse_args()


def build_train_kwargs(cfg: dict, args) -> dict:
    """config 기반 학습 파라미터 구성, CLI 인자로 오버라이드"""
    kwargs = {k: v for k, v in cfg.items() if k not in ("model",)}

    # data 경로를 절대 경로로 변환
    data_path = Path(cfg["data"])
    if not data_path.is_absolute():
        data_path = (BASE_DIR / "train" / data_path).resolve()
    kwargs["data"] = str(data_path)

    # CLI 오버라이드
    if args.epochs is not None:
        kwargs["epochs"] = args.epochs
    if args.batch is not None:
        kwargs["batch"] = args.batch
    if args.imgsz is not None:
        kwargs["imgsz"] = args.imgsz
    if args.device is not None:
        kwargs["device"] = args.device
    if args.resume:
        kwargs["resume"] = True

    return kwargs


def main():
    args = parse_args()
    cfg  = load_config(Path(args.config))

    print("=" * 60)
    print("  logmile YOLO11n 번호판 탐지 모델 학습 시작")
    print("=" * 60)

    # 모델 로드 (사전학습 가중치)
    model_name = cfg.get("model", "yolo11n.pt")
    print(f"[모델] {model_name}")
    model = YOLO(model_name)

    # 학습 파라미터 구성
    train_kwargs = build_train_kwargs(cfg, args)
    print(f"[데이터셋] {train_kwargs['data']}")
    print(f"[Epochs] {train_kwargs.get('epochs')}  "
          f"[Batch] {train_kwargs.get('batch')}  "
          f"[Imgsz] {train_kwargs.get('imgsz')}")
    print("-" * 60)

    # 학습 실행
    results = model.train(**train_kwargs)

    print("=" * 60)
    print("  학습 완료")
    print(f"  저장 경로: {results.save_dir}")
    print("=" * 60)

    # 최적 가중치 경로 출력
    best_weight = Path(results.save_dir) / "weights" / "best.pt"
    if best_weight.exists():
        print(f"[best.pt] {best_weight}")
    else:
        print("[경고] best.pt 가 존재하지 않습니다. 학습 결과를 확인하세요.")


if __name__ == "__main__":
    main()
