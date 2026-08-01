#!/usr/bin/env bash
# 截圖拆解:把 source/ 裡的圖片排序編號、拼成聯絡表,並產出可填寫的判讀工作表。
# 用於「只有截圖、沒有影片」的情況。

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SOURCE="$ROOT/source"
FRAMES="$ROOT/frames"
REPORT="$ROOT/report"
FFMPEG="$(python3 -c 'import imageio_ffmpeg; print(imageio_ffmpeg.get_ffmpeg_exe())' 2>/dev/null || echo ffmpeg)"

mkdir -p "$FRAMES" "$REPORT"

mapfile -t IMAGES < <(find "$SOURCE" -maxdepth 1 -type f \
  -iregex '.*\.\(png\|jpg\|jpeg\|webp\)$' | sort)

[ "${#IMAGES[@]}" -eq 0 ] && { echo "    source/ 裡沒有圖片"; exit 0; }
echo "    找到 ${#IMAGES[@]} 張圖片"

# 統一轉成等高 jpg,方便並排比對與拼貼
INDEX=1
for IMG in "${IMAGES[@]}"; do
  OUT="$FRAMES/$(printf 'still_%03d.jpg' "$INDEX")"
  "$FFMPEG" -hide_banner -loglevel error -i "$IMG" \
    -vf "scale=-2:720" -q:v 2 "$OUT" -y 2>/dev/null || continue
  INDEX=$((INDEX + 1))
done

"$FFMPEG" -hide_banner -loglevel error -pattern_type glob -i "$FRAMES/still_*.jpg" \
  -filter_complex "scale=360:-1,tile=3x3" "$REPORT/contact-sheet-%02d.jpg" -y 2>/dev/null || true

{
  echo "# 截圖判讀工作表"
  echo
  echo "共 $((INDEX - 1)) 張。對照 METHODOLOGY.md 第一節的四個問題逐張填寫。"
  echo
  echo "## 人物一致性:五錨點比對"
  echo
  echo "挑兩張**不同鏡頭**的同一角色特寫並排比對:"
  echo
  echo "| 錨點 | 是否一致 | 判讀 |"
  echo "|------|---------|------|"
  echo "| 瞳色 / 眼距 / 鼻樑高度 |  | 一致 → 有角色鎖定機制 |"
  echo "| 髮際線 / 瀏海分線 |  | 一致 → 參考圖或 LoRA |"
  echo "| 服裝鈕扣數 / 口袋 / 配件 |  | 一致 → 首尾幀控制 |"
  echo "| 痣 / 疤 / 刺青等小特徵 |  | 一致 → LoRA 自訓或真人 |"
  echo "| 身形比例 / 肩寬 |  | 一致 → 3D 或真人來源 |"
  echo
  echo "## 逐張紀錄"
  echo
  echo "| # | 檔案 | 景別 | 運鏡 | 光線方向 | 生成破綻(手/文字/物理) | 備註 |"
  echo "|---|------|------|------|---------|----------------------|------|"
  for N in $(seq 1 $((INDEX - 1))); do
    printf '| %d | `still_%03d.jpg` |  |  |  |  |  |\n' "$N" "$N"
  done
} > "$REPORT/image-worksheet.md"

echo "    產出 $((INDEX - 1)) 張標準化畫格 + report/image-worksheet.md"
