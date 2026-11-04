# JavaのplusMonthsで毎月の予定日がずれる

設備点検の練習用の仕様です。基準日が当月に存在すればその日、存在しなければ当月末。翌月以降も基準日は変えません。休日・時刻・タイムゾーンはこのサンプルの対象外です。

JDK 17とMaven 3.9系を用意し、展開先で実行します。

```sh
mvn clean test
java -cp target/classes example.inspection.InspectionScheduleDemo
```

- 修正前の2026年1〜3月は `[2026-01-31, 2026-02-28, 2026-03-28]`。
- 修正後は `[2026-01-31, 2026-02-28, 2026-03-31]`。
- 2026年3月では基準日30日の契約は30日、常に月末の契約は31日です。

`InspectionScheduleDemo` に修正前と表示用の実行コード、`MonthlyInspectionSchedule` に修正版、`MonthlyInspectionScheduleTest` にJUnit 9件を入れています。
