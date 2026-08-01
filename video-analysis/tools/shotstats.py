#!/usr/bin/env python3
"""Turn a list of cut timecodes into shot-length statistics and a shot list."""
import sys
import os


def load_cuts(path):
    with open(path) as fh:
        return sorted({round(float(line), 3) for line in fh if line.strip()})


def classify(duration):
    if duration < 1.0:
        return "極快切 (flash cut)"
    if duration < 2.5:
        return "快切"
    if duration < 5.0:
        return "標準"
    if duration < 10.0:
        return "長鏡頭"
    return "極長鏡頭"


def timecode(seconds):
    minutes, secs = divmod(seconds, 60)
    return f"{int(minutes):02d}:{secs:06.3f}"


def main():
    cuts_path, total_str, out_path = sys.argv[1], sys.argv[2], sys.argv[3]
    total = float(total_str)
    cuts = [c for c in load_cuts(cuts_path) if 0.0 < c < total]

    boundaries = [0.0] + cuts + [total]
    shots = []
    for index in range(len(boundaries) - 1):
        start, end = boundaries[index], boundaries[index + 1]
        shots.append((index + 1, start, end, end - start))

    lengths = sorted(shot[3] for shot in shots)
    count = len(lengths)
    mean = sum(lengths) / count
    median = lengths[count // 2] if count % 2 else (lengths[count // 2 - 1] + lengths[count // 2]) / 2
    cuts_per_min = len(cuts) / (total / 60.0) if total else 0.0

    lines = [
        "# 鏡頭切換節奏表 (自動產生)",
        "",
        f"- 影片總長:{total:.3f} 秒",
        f"- 偵測到切點:{len(cuts)} 個 → 共 {count} 個鏡頭",
        f"- 平均鏡頭長度:{mean:.2f} 秒 / 中位數:{median:.2f} 秒",
        f"- 最短 {lengths[0]:.2f} 秒 / 最長 {lengths[-1]:.2f} 秒",
        f"- 剪接密度:每分鐘 {cuts_per_min:.1f} 刀",
        "",
        "> 節奏判讀:每分鐘 30 刀以上屬高頻剪接(短影音/預告片語感);",
        "> 10-20 刀屬敘事節奏;10 刀以下偏沉浸長鏡頭。",
        "",
        "| # | 進點 | 出點 | 長度(秒) | 節奏分類 | 畫格檔 |",
        "|---|------|------|---------|---------|--------|",
    ]
    for number, start, end, duration in shots:
        frame = f"shot_{number:03d}_{start:07.3f}s.jpg"
        lines.append(
            f"| {number} | {timecode(start)} | {timecode(end)} | {duration:.2f} | {classify(duration)} | `{frame}` |"
        )

    lines += [
        "",
        "## 待人工填寫(看畫格後補)",
        "",
        "| # | 景別 | 運鏡 | 轉場方式 | 角色是否一致 | 備註 |",
        "|---|------|------|---------|------------|------|",
    ]
    for number, _, _, _ in shots:
        lines.append(f"| {number} |  |  |  |  |  |")

    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    with open(out_path, "w") as fh:
        fh.write("\n".join(lines) + "\n")

    print(f"shots={count} cuts={len(cuts)} mean={mean:.2f}s median={median:.2f}s cpm={cuts_per_min:.1f}")


if __name__ == "__main__":
    main()
