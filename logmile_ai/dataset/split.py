"""
logmile 번호판 데이터셋 분할 스크립트

car_plate_data1 (Darknet 형식) + car_plate_data2 (Roboflow 형식) 을 합쳐
train 70% / val 20% / test 10% 로 분배합니다.

소스 구조:
    car_plate_data1/obj_train_data/*.jpg  + *.txt  (Darknet, class: car_plate)
    car_plate_data2/train/images/*.jpg    +         (Roboflow)
    car_plate_data2/train/labels/*.txt

출력 구조:
    dataset/images/{train|val|test}/
    dataset/labels/{train|val|test}/

실행 방법:
    python dataset/split.py                    # 기본 실행 (70/20/10)
    python dataset/split.py --dry_run          # 분배 현황만 확인 (파일 복사 없음)
    python dataset/split.py --train 0.8 --val 0.1 --test 0.1
    python dataset/split.py --seed 0           # 다른 랜덤 시드
"""

import argparse
import random
import shutil
from pathlib import Path


BASE_DIR = Path(__file__).resolve().parent   # logmile_ai/dataset/
IMG_EXTS = {".jpg", ".jpeg", ".png", ".bmp"}

# 원본 소스 정의 (이미지 디렉터리, 라벨 디렉터리)
SOURCES = [
    {
        "name":   "car_plate_data1",
        "images": BASE_DIR / "car_plate_data1" / "obj_train_data",
        "labels": BASE_DIR / "car_plate_data1" / "obj_train_data",
    },
    {
        "name":   "car_plate_data2",
        "images": BASE_DIR / "car_plate_data2" / "train" / "images",
        "labels": BASE_DIR / "car_plate_data2" / "train" / "labels",
    },
]

SPLIT_DIRS = {
    "train": BASE_DIR / "images" / "train",
    "val":   BASE_DIR / "images" / "val",
    "test":  BASE_DIR / "images" / "test",
}
LABEL_DIRS = {
    "train": BASE_DIR / "labels" / "train",
    "val":   BASE_DIR / "labels" / "val",
    "test":  BASE_DIR / "labels" / "test",
}


def parse_args():
    parser = argparse.ArgumentParser(description="번호판 데이터셋 train/val/test 분할")
    parser.add_argument("--train",   type=float, default=0.7, help="학습 비율 (기본: 0.7)")
    parser.add_argument("--val",     type=float, default=0.2, help="검증 비율 (기본: 0.2)")
    parser.add_argument("--test",    type=float, default=0.1, help="테스트 비율 (기본: 0.1)")
    parser.add_argument("--seed",    type=int,   default=42,  help="랜덤 시드 (기본: 42)")
    parser.add_argument("--dry_run", action="store_true",
                        help="실제 복사 없이 분배 현황만 출력")
    return parser.parse_args()


def validate_ratios(train: float, val: float, test: float):
    if abs(train + val + test - 1.0) > 1e-4:
        raise ValueError(f"비율 합계가 1.0 이 아닙니다: {train + val + test:.4f}")


def collect_pairs() -> list[tuple[Path, Path | None, str]]:
    """두 소스에서 (이미지, 라벨, 소스명) 쌍을 수집"""
    pairs = []
    for src in SOURCES:
        img_dir = src["images"]
        lbl_dir = src["labels"]

        if not img_dir.exists():
            print(f"  [건너뜀] {src['name']} 이미지 디렉터리 없음: {img_dir}")
            continue

        images = sorted([p for p in img_dir.iterdir() if p.suffix.lower() in IMG_EXTS])
        missing = 0
        for img in images:
            lbl = lbl_dir / (img.stem + ".txt")
            if lbl.exists():
                pairs.append((img, lbl, src["name"]))
            else:
                pairs.append((img, None, src["name"]))
                missing += 1

        print(f"  [{src['name']}] 이미지 {len(images)}개  라벨 없음 {missing}개")

    return pairs


def split_pairs(pairs: list, train_r: float, val_r: float) -> dict[str, list]:
    n       = len(pairs)
    n_train = round(n * train_r)
    n_val   = round(n * val_r)
    n_test  = n - n_train - n_val

    # 합산 보정
    diff = n - (n_train + n_val + n_test)
    n_test += diff

    return {
        "train": pairs[:n_train],
        "val":   pairs[n_train:n_train + n_val],
        "test":  pairs[n_train + n_val:],
    }


def copy_file(src: Path, dst_dir: Path):
    dst_dir.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst_dir / src.name)


def distribute(split_map: dict, dry_run: bool) -> dict[str, int]:
    counts = {}
    for split_name, pairs in split_map.items():
        img_dir = SPLIT_DIRS[split_name]
        lbl_dir = LABEL_DIRS[split_name]

        if not dry_run:
            img_dir.mkdir(parents=True, exist_ok=True)
            lbl_dir.mkdir(parents=True, exist_ok=True)

        for img, lbl, _ in pairs:
            if not dry_run:
                copy_file(img, img_dir)
                if lbl:
                    copy_file(lbl, lbl_dir)

        counts[split_name] = len(pairs)
    return counts


def main():
    args = parse_args()
    validate_ratios(args.train, args.val, args.test)

    print("=" * 60)
    print("  logmile 번호판 데이터셋 분할")
    print(f"  비율  : train {args.train*100:.0f}% / val {args.val*100:.0f}% / test {args.test*100:.0f}%")
    print(f"  시드  : {args.seed}")
    if args.dry_run:
        print("  [DRY RUN] 실제 파일 복사 없이 현황만 출력")
    print("─" * 60)

    pairs = collect_pairs()
    if not pairs:
        print("\n[오류] 처리할 이미지가 없습니다.")
        return

    print(f"  전체 이미지: {len(pairs)}개")
    print("─" * 60)

    random.seed(args.seed)
    random.shuffle(pairs)

    split_map = split_pairs(pairs, args.train, args.val)
    counts    = distribute(split_map, args.dry_run)

    print(f"\n  {'예정' if args.dry_run else '완료'} 분배 현황:")
    for split_name, count in counts.items():
        print(f"    {split_name:6s} : {count:5d}개  → dataset/images/{split_name}/")
    print(f"\n  총 {sum(counts.values())}개 {'(복사 예정)' if args.dry_run else '복사 완료'}")

    if not args.dry_run:
        print("\n  다음 단계: python train/train.py")
    print("=" * 60)


if __name__ == "__main__":
    main()
