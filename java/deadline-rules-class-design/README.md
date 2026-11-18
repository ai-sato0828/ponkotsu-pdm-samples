# 返品と商品不足の期限を分けるJavaのサンプル

架空の通販を題材に、納品日を起点とした返品と商品不足の連絡期限を比較します。変更前は両方とも納品日から7日後まで受け付けます。今回の変更で返品のみ14日後までにします。納品日を0日目とし、納品日とそれぞれの最終日を範囲に含めます。日付は暦日で、時刻・タイムゾーンや商品ごとの条件はこのサンプルの範囲外です。

| 納品日 | 連絡日 | 返品 | 商品不足 |
| --- | --- | --- | --- |
| 2026-11-01 | 2026-10-31 | 期限外 | 期限外 |
| 2026-11-01 | 2026-11-01 | 受付可 | 受付可 |
| 2026-11-01 | 2026-11-08 | 受付可 | 受付可 |
| 2026-11-01 | 2026-11-09 | 受付可 | 期限外 |
| 2026-11-01 | 2026-11-15 | 受付可 | 期限外 |
| 2026-11-01 | 2026-11-16 | 期限外 | 期限外 |

## 実行する

JDK 17とPOSIX互換シェル（macOSやLinuxなど）を使います。

```sh
sh run.sh
```

外部ライブラリを使わずに、共有7日、共有14日、分離後を同じ納品日と受付日で比較します。

```text
legacy shared 7 days: return 2026-11-09=false, missing 2026-11-09=false
wrong shared 14 days: return 2026-11-09=true, missing 2026-11-09=true
separate: return 2026-11-09=true, missing 2026-11-09=false
return: 2026-11-15=true, 2026-11-16=false
missing: 2026-11-08=true, 2026-11-09=false
before delivery 2026-10-31: return=false, missing=false
```

## JUnitで確認する

JDK 17とMavenを追加で使います。Mavenで初回にテストする場合は、JUnitと実行用プラグインの取得にネット接続が必要です。

```sh
mvn test
```

分離後の納品日前、納品日、期限当日、翌日、月またぎなどを10件で確認します。変更前と共通の日数を14日に変えたときの再現を2件で確認します。

- `src/main/java/example/deadline/DeadlineDemo.java` は比較を実行する入口です。
- `src/main/java/example/deadline/ReturnDeadline.java` は返品の14日を定義します。
- `src/main/java/example/deadline/MissingItemDeadline.java` は商品不足の連絡の7日を定義します。
- `src/main/java/example/deadline/DeliveryWindow.java` は、受け取った期間を使って納品日から最終日までの日付範囲を判定します。
- `src/test/java/example/deadline/DeadlineTest.java` に、再現と分離後の期待値があります。

返品を21日に延長する練習では、まず返品の期待値に最終日の11月22日と翌日の11月23日を追加し、商品不足の11月8日と9日は既存値を期待します。失敗を確認してから、返品側の日数を更新してください。
