"""
logmile OCR 최종 성공률 측정 스크립트

YOLO 탐지 + EasyOCR 추출 파이프라인의 end-to-end 정확도를 측정합니다.
검증 이미지와 정답 번호판 텍스트(ground_truth.json)를 기반으로
OCR 성공률, 평균 신뢰도, 신뢰도 임계값(0.85) 미달 비율을 계산합니다.

실행 방법:
    python evaluate/ocr_accuracy.py --weight runs/train/.../best.pt
    python evaluate/ocr_accuracy.py --weight best.pt --gt evaluate/ground_truth.json
"""

import argparse
import json
from pathlib import Path

from app.service.plate_service import recognize_plate_from_path


BASE_DIR   = Path(__file__).resolve().parent.parent
RESULT_DIR = BASE_DIR / "evaluate" / "results"
DEFAULT_GT = BASE_DIR / "evaluate" / "ground_truth.json"
VAL_IMAGES = BASE_DIR / "dataset" / "images" / "val"


def parse_args():
    parser = argparse.ArgumentParser(description="logmile OCR end-to-end 정확도 측정")
    parser.add_argument("--weight",  type=str, required=True,
                        help="평가할 best.pt 경로")
    parser.add_argument("--gt",      type=str, default=str(DEFAULT_GT),
                        help="정답 파일 경로 (기본: evaluate/ground_truth.json)")
    parser.add_argument("--val_dir", type=str, default=str(VAL_IMAGES),
                        help="검증 이미지 디렉터리")
    parser.add_argument("--threshold", type=float, default=0.85,
                        help="OCR 신뢰도 임계값 (기본: 0.85)")
    return parser.parse_args()


def load_ground_truth(gt_path: str) -> dict:
    """
    정답 파일 형식 (ground_truth.json):
    {
      "img_001.jpg": "12가3456",
      "img_002.jpg": "서울34나5678",
      ...
    }
    """
    with open(gt_path, "r", encoding="utf-8") as f:
        return json.load(f)


def normalize(text: str) -> str:
    """공백 제거 후 비교"""
    return text.replace(" ", "").strip()


def run_ocr_accuracy(args):
    gt = load_ground_truth(args.gt)
    val_dir = Path(args.val_dir)

    total       = 0
    correct     = 0
    manual_req  = 0
    confidences = []

    print(f"\n  이미지 수: {len(gt)}  |  임계값: {args.threshold}")
    print("─" * 60)

    for filename, answer in gt.items():
        img_path = val_dir / filename
        if not img_path.exists():
            print(f"  [건너뜀] {filename} 없음")
            continue

        total += 1
        result = recognize_plate_from_path(str(img_path))

        confidence   = result.get("confidence", 0.0)
        plate_no     = result.get("plate_no") or ""
        is_manual    = result.get("is_manual_required", True)

        confidences.append(confidence)
        if is_manual:
            manual_req += 1

        if normalize(plate_no) == normalize(answer):
            correct += 1
        else:
            print(f"  [오류] {filename}: 예측={plate_no!r}  정답={answer!r}  신뢰도={confidence:.3f}")

    if total == 0:
        print("  평가할 이미지가 없습니다.")
        return

    accuracy    = correct / total
    avg_conf    = sum(confidences) / total
    manual_rate = manual_req / total

    print(f"\n  총 이미지    : {total}")
    print(f"  정답 수      : {correct}")
    print(f"  OCR 성공률   : {accuracy * 100:.2f} %")
    print(f"  평균 신뢰도  : {avg_conf:.4f}")
    print(f"  수동입력 비율: {manual_rate * 100:.2f} %  (임계값 {args.threshold} 미달)")

    # 결과 저장
    RESULT_DIR.mkdir(parents=True, exist_ok=True)
    out = {
        "total": total, "correct": correct,
        "accuracy": round(accuracy, 4),
        "avg_confidence": round(avg_conf, 4),
        "manual_required_rate": round(manual_rate, 4),
        "threshold": args.threshold,
    }
    out_path = RESULT_DIR / "ocr_accuracy.json"
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(out, f, ensure_ascii=False, indent=2)
    print(f"\n  결과 저장: {out_path}")


def main():
    args = parse_args()
    print("=" * 60)
    print("  logmile OCR end-to-end 정확도 측정")
    print("=" * 60)
    run_ocr_accuracy(args)
    print("\n" + "=" * 60)


if __name__ == "__main__":
    main()
