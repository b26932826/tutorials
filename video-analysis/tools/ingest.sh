#!/usr/bin/env bash
# 自動偵測新素材:掃描所有可能的上傳落點,把影片/圖片收進 source/。
# 以內容雜湊去重,同一個檔案重複掃到不會重複收錄。

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SOURCE="$ROOT/source"
LEDGER="$ROOT/.ingested"
mkdir -p "$SOURCE"
touch "$LEDGER"

# 上傳檔案可能落在哪裡,環境不同位置不同,全部掃一遍。
CANDIDATES=(
  "$SOURCE"
  "$ROOT/.."
  "$HOME"
  "$HOME/uploads" "$HOME/Downloads" "$HOME/upload"
  "/mnt/user-data" "/mnt/uploads" "/mnt/data"
  "/workspace" "/uploads" "/data"
  "${CLAUDE_SCRATCHPAD:-/nonexistent}"
)

MEDIA_RE='.*\.\(mp4\|mov\|mkv\|webm\|avi\|m4v\|png\|jpg\|jpeg\|webp\|heic\)$'
FOUND=0

for DIR in "${CANDIDATES[@]}"; do
  [ -d "$DIR" ] || continue
  while IFS= read -r FILE; do
    [ -f "$FILE" ] || continue
    # 略過工具自身產出與版控/相依目錄,避免把自己的輸出當成新素材
    case "$FILE" in
      */video-analysis/frames/*|*/video-analysis/report/*) continue ;;
      */.git/*|*/node_modules/*|*/dist-packages/*|*/site-packages/*) continue ;;
      */maven/*|*/JGit/*|*/Twitter4J/*) continue ;;
    esac
    HASH="$(md5sum "$FILE" | cut -d' ' -f1)"
    grep -q "^$HASH " "$LEDGER" 2>/dev/null && continue

    BASE="$(basename "$FILE")"
    DEST="$SOURCE/$BASE"
    # 檔名撞號就加雜湊前綴,不覆蓋既有素材
    [ -e "$DEST" ] && [ "$FILE" != "$DEST" ] && DEST="$SOURCE/${HASH:0:6}_$BASE"
    [ "$FILE" != "$DEST" ] && cp "$FILE" "$DEST"

    echo "$HASH $(basename "$DEST")" >> "$LEDGER"
    echo "    + 收錄 $(basename "$DEST")"
    FOUND=$((FOUND + 1))
  done < <(find "$DIR" -maxdepth 3 -type f -iregex "$MEDIA_RE" 2>/dev/null)
done

echo "$FOUND"  > "$ROOT/.last-ingest-count"
[ "$FOUND" -eq 0 ] && echo "    (沒有發現新素材)"
exit 0
