#!/usr/bin/env bash
# 影片風格拆解工具:自動偵測鏡頭切點、抽關鍵畫格、產出節奏表。
#
# 用法:
#   ./analyze_video.sh source/影片.mp4 [場景敏感度 0.1-0.6,預設 0.30]
#
# 敏感度說明:數字越小抓到越多切點。快剪的 MV/短影音用 0.35-0.45,
# 平穩敘事用 0.25-0.30。抓太少就調低,抓到一堆假切點就調高。

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VIDEO="${1:?請指定影片路徑,例如 source/clip.mp4}"
THRESHOLD="${2:-0.30}"
# 第三個參數是輸出子目錄名。多支影片各自分開存,不會互相覆蓋。
SLUG="${3:-$(basename "${VIDEO%.*}" | tr -c 'A-Za-z0-9_-' '_' | sed 's/_*$//')}"

# 這台機器沒有系統 ffmpeg,改用 imageio-ffmpeg 附帶的執行檔。
FFMPEG="$(python3 -c 'import imageio_ffmpeg; print(imageio_ffmpeg.get_ffmpeg_exe())' 2>/dev/null || echo ffmpeg)"
command -v "$FFMPEG" >/dev/null 2>&1 || [ -x "$FFMPEG" ] || { echo "找不到 ffmpeg"; exit 1; }

FRAMES="$ROOT/frames/$SLUG"
REPORT="$ROOT/report/$SLUG"
rm -rf "$FRAMES"; mkdir -p "$FRAMES" "$REPORT"

echo "▶ 1/5 讀取影片規格"
# ffmpeg 純讀取資訊時會以非零狀態結束,這是正常行為,不能當成錯誤。
PROBE="$("$FFMPEG" -hide_banner -i "$VIDEO" 2>&1 || true)"
printf '%s\n' "$PROBE" | grep -E "Duration|Stream" | sed 's/^/    /' \
  | tee "$REPORT/00-metadata.txt"

DURATION="$(printf '%s\n' "$PROBE" \
  | sed -n 's/.*Duration: \([0-9:.]*\).*/\1/p' \
  | awk -F: '{print $1*3600 + $2*60 + $3}')"
[ -n "$DURATION" ] || { echo "無法讀出影片長度,檔案可能損毀或格式不支援"; exit 1; }
echo "    總長度:${DURATION} 秒"

echo "▶ 2/5 偵測鏡頭切點 (threshold=$THRESHOLD)"
"$FFMPEG" -hide_banner -nostats -i "$VIDEO" \
  -filter:v "select='gt(scene,$THRESHOLD)',showinfo" -f null - 2>&1 \
  | sed -n 's/.*pts_time:\([0-9.]*\).*/\1/p' > "$REPORT/cuts.txt" || true
CUT_COUNT="$(wc -l < "$REPORT/cuts.txt" | tr -d ' ')"
echo "    偵測到 ${CUT_COUNT} 個切點"

echo "▶ 3/5 抽出每個鏡頭的代表畫格"
INDEX=1
extract() {  # $1=時間 $2=編號
  "$FFMPEG" -hide_banner -loglevel error -ss "$1" -i "$VIDEO" \
    -frames:v 1 -q:v 2 "$FRAMES/$(printf 'shot_%03d_%07.3fs.jpg' "$2" "$1")" 2>/dev/null || true
}
extract 0.5 1
while read -r CUT; do
  [ -z "$CUT" ] && continue
  INDEX=$((INDEX + 1))
  # 切點後 0.4 秒取樣,避開轉場過渡中的混合畫面
  extract "$(awk -v c="$CUT" 'BEGIN{print c + 0.4}')" "$INDEX"
  # 同時保留切點前一格,方便比對「切換瞬間」如何銜接
  "$FFMPEG" -hide_banner -loglevel error \
    -ss "$(awk -v c="$CUT" 'BEGIN{print (c - 0.15 > 0) ? c - 0.15 : 0}')" -i "$VIDEO" \
    -frames:v 1 -q:v 2 "$FRAMES/$(printf 'cut_%03d_before.jpg' "$INDEX")" 2>/dev/null || true
done < "$REPORT/cuts.txt"
echo "    產出 $(ls -1 "$FRAMES" | wc -l | tr -d ' ') 張畫格"

echo "▶ 4/5 產生聯絡表 (contact sheet)"
"$FFMPEG" -hide_banner -loglevel error -pattern_type glob -i "$FRAMES/shot_*.jpg" \
  -filter_complex "scale=320:-1,tile=4x5" "$REPORT/contact-sheet-%02d.jpg" 2>/dev/null || \
  echo "    (畫格不足以拼貼,略過)"

echo "▶ 5/5 計算剪接節奏並產出鏡頭表"
python3 "$ROOT/tools/shotstats.py" "$REPORT/cuts.txt" "$DURATION" "$REPORT/shot-list.md"

echo
echo "✅ 完成。請查看:"
echo "   $REPORT/shot-list.md      ← 鏡頭切換節奏表"
echo "   $REPORT/contact-sheet-*.jpg ← 全片縮圖總覽"
echo "   $FRAMES/                  ← 每個鏡頭的畫格"
