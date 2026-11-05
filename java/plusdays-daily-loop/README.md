# LocalDate.plusDaysで日次処理を進める

JDK 17とMaven 3を使います。ZIPを展開し、`pom.xml` のあるフォルダで `mvn test` を実行します。

- 同じ開始日と終了日は1回だけ処理します。
- 2024年2月28日から3月1日までは2月29日を含め、3日を順に1回ずつ処理します。
- 開始日が終了日より後なら、1件も処理せずに `IllegalArgumentException` を送出します。
- `LocalDate.MAX` の1日分は翌日を計算せず、1回だけ処理します。

`src/main/java/example/dates/DailyDateRunner.java` の `current = current.plusDays(1);` を `current.plusDays(1);` に変え、再度 `mvn test` を実行すると、うるう日をまたぐテストが同じ2月28日の再処理を検出します。重複した時点で検証が止まるため、ループが続くのを待つ必要はありません。
