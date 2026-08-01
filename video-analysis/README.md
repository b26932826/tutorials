# 影片風格拆解工具組

針對「人物一致性高 + 鏡頭切換有設計感」的影片,做逆向拆解與複製生產。

## 自動處理機制(推薦)

素材放進 `source/`(或直接在對話中上傳),然後一行搞定:

```bash
./auto.sh                    # 跑一次
./auto.sh --watch            # 持續監看,素材一出現就自動處理
./auto.sh --commit           # 處理完自動 commit + push,結果永久保存
./auto.sh --watch --commit   # 兩者併用:全自動
```

`auto.sh` 會自動:

1. **掃描**所有可能的上傳落點,把新素材收進 `source/`(以內容雜湊去重,不會重複處理)
2. **判斷類型** — 影片走切點偵測流程,截圖走判讀流程,兩者都有就都跑
3. **逐支處理**多支影片,各自輸出到獨立子目錄,不互相覆蓋
4. **保存結果**(加 `--commit`)

調整靈敏度:`./auto.sh --sensitivity=0.45`(快剪片用 0.35–0.45,平穩敘事用 0.25–0.30)。

## 單獨執行

```bash
./analyze_video.sh source/影片.mp4 0.35    # 只跑影片
./analyze_images.sh                        # 只跑截圖
```

## 產出

| 路徑 | 內容 |
|------|------|
| `report/<影片名>/shot-list.md` | 鏡頭切換節奏表:每一刀的時間碼、鏡頭長度、剪接密度 |
| `report/image-worksheet.md` | 截圖判讀工作表,含人物一致性五錨點比對表 |
| `report/**/contact-sheet-*.jpg` | 縮圖總覽 |
| `frames/<影片名>/shot_*.jpg` | 每個鏡頭的代表畫格 |
| `frames/<影片名>/cut_*_before.jpg` | 每個切點的**前一格**,用來比對切換如何銜接 |

`report/EXAMPLE-shot-list.md` 是範例輸出,可先看格式長什麼樣。

## 目錄

- `METHODOLOGY.md` — **核心文件**。如何從畫格判斷製作方式、人物一致性的四種技術、
  鏡頭切換機制與剪接節奏量化標準、可複製的生產管線與 prompt 模板。
- `auto.sh` — 自動處理主程式。
- `analyze_video.sh` / `analyze_images.sh` — 影片 / 截圖分析。
- `tools/ingest.sh` — 素材自動偵測與收錄。
- `tools/shotstats.py` — 切點時間碼 → 節奏統計與鏡頭表。

## 環境備註

這台機器沒有系統 ffmpeg,腳本會自動改用 `imageio-ffmpeg` 附帶的執行檔。
在自己的電腦執行時,安裝 ffmpeg 後即可直接使用。

原始素材(`source/`)與畫格(`frames/`)不進版控;分析報告會保留供審閱。
