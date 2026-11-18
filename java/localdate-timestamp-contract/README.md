# 日付だけの入力と、時刻付きの入力を変換する

`LocalDate.parse` に `2026-02-28T23:30:00` を渡すと、日付部分の直後にある `T` の位置（0始まりで10）で変換が止まります。日付だけを受け付ける項目なら、この例外は入力仕様どおりです。日時を受け取る項目で、どの日付が必要かは入力形式と表示・集計の仕様で決まります。

## 実行

JDK 17とMavenが必要です。ZIPを展開した場合は `localdate-timestamp-contract` に移動します。

```sh
mvn -B test
java -cp target/classes info.syachiku.samples.date.TimestampDateDemo
```

実行例（JDK 17.0.8）。

```text
date only -> 2026-02-28
Text '2026-02-28T23:30:00' could not be parsed, unparsed text found at index 10
local timestamp date -> 2026-02-28
date prefix of invalid time -> 2026-02-28
invalid local timestamp -> DateTimeParseException
source offset date -> 2026-02-28
Asia/Tokyo date -> 2026-03-01
```

## 何を比べるか

| 入力例 | 受け付ける形式と欲しい日付 | 呼び出し |
| --- | --- | --- |
| `2026-02-28` | 日付だけを受け付ける | `parseDateOnly` |
| `2026-02-28T23:30:00` | オフセットなしの日時を受け付け、その入力が表す日付を得る | `parseLocalTimestampDate` |
| `2026-02-28T23:30:00-05:00` | オフセット付きの日時を受け付け、入力側の日付を得る | `dateAtSourceOffset` |
| 同じオフセット付きの日時 | 同じ瞬間を集計先のタイムゾーンで見た日付を得る | `dateInZone` |

日付・日時・オフセットを8件のJUnitテストで確認します。日時を日付専用の解析へ渡した場合と、オフセット付き日時をローカル日時の解析へ渡した場合のエラー位置もテストします。`2026-02-28T25:30:00` の先頭10文字だけを読むと `2026-02-28` が得られ、時刻の誤りを見逃します。日時を受け取る項目では、日時全体を解析してから日付を取り出します。`Asia/Tokyo` は集計先の指定例です。オフセットなしの入力では、どのタイムゾーンの時刻かを別途決めてから瞬間として扱います。
