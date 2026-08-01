#!/usr/bin/env bash
# 自動處理機制:偵測新素材 → 判斷類型 → 跑對應分析 → 存檔 → 可選擇自動 commit。
#
# 用法:
#   ./auto.sh                  跑一次(素材已在時用這個)
#   ./auto.sh --watch          持續監看,素材一出現就自動處理
#   ./auto.sh --commit         處理完自動 commit + push,結果永久保存供審閱
#   ./auto.sh --watch --commit 兩者併用
#
# 設計原則:可重複執行。已處理過的素材不會重跑,沒有素材時安靜結束。

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SENSITIVITY="${SENSITIVITY:-0.35}"
WATCH=0; COMMIT=0; INTERVAL="${INTERVAL:-20}"

for ARG in "$@"; do
  case "$ARG" in
    --watch) WATCH=1 ;;
    --commit) COMMIT=1 ;;
    --sensitivity=*) SENSITIVITY="${ARG#*=}" ;;
    -h|--help) sed -n '2,15p' "$0"; exit 0 ;;
    *) echo "未知參數:$ARG"; exit 1 ;;
  esac
done

run_once() {
  echo "▶ 掃描新素材"
  bash "$ROOT/tools/ingest.sh"
  local COUNT; COUNT="$(cat "$ROOT/.last-ingest-count" 2>/dev/null || echo 0)"
  [ "$COUNT" -eq 0 ] && return 1

  # 影片與圖片都處理:每支影片各自輸出到獨立子目錄,不會互相覆蓋。
  local VIDEOS=()
  while IFS= read -r V; do [ -n "$V" ] && VIDEOS+=("$V"); done < <(find "$ROOT/source" \
    -maxdepth 1 -type f -iregex '.*\.\(mp4\|mov\|mkv\|webm\|avi\|m4v\)$' | sort)

  if [ "${#VIDEOS[@]}" -gt 0 ]; then
    echo "▶ 偵測到 ${#VIDEOS[@]} 支影片 → 逐支跑鏡頭切點分析"
    for V in "${VIDEOS[@]}"; do
      echo "  ── $(basename "$V")"
      bash "$ROOT/analyze_video.sh" "$V" "$SENSITIVITY" || echo "    ⚠ 這支處理失敗,跳過繼續"
    done
  fi

  if find "$ROOT/source" -maxdepth 1 -type f \
      -iregex '.*\.\(png\|jpg\|jpeg\|webp\)$' | grep -q .; then
    echo "▶ 偵測到圖片 → 跑截圖判讀流程"
    bash "$ROOT/analyze_images.sh"
  fi

  if [ "$COMMIT" -eq 1 ]; then
    echo "▶ 保存結果到 git"
    cd "$ROOT/.."
    git add -A video-analysis 2>/dev/null || true
    if ! git diff --cached --quiet 2>/dev/null; then
      git -c user.name="Claude" -c user.email="noreply@anthropic.com" \
        commit -q -m "Update video analysis output from auto pipeline"
      git push -q origin HEAD 2>/dev/null && echo "    已 push" || echo "    push 失敗(稍後重試)"
    else
      echo "    沒有變更需要提交"
    fi
  fi

  echo "✅ 處理完成"
  return 0
}

if [ "$WATCH" -eq 1 ]; then
  echo "👁  監看模式啟動(每 ${INTERVAL} 秒掃描一次,Ctrl-C 結束)"
  while true; do
    run_once && echo "--- 等待下一批素材 ---"
    sleep "$INTERVAL"
  done
else
  run_once || { echo "沒有素材可處理。把影片或截圖放進 source/,或直接在對話中上傳。"; exit 0; }
fi
