# 帳票のプレビューと確定をメソッドで分けるサンプル

JDK 17とMaven 3.9系を使用します。JUnit Jupiter 5.12.2、Maven Surefire 3.5.3です。初回はMavenが依存を取得できる環境で実行します。

ZIPを展開し、`pom.xml` のある場所で実行します。

```sh
mvn test
```

JUnitは6件成功します。旧コードでプレビューが1回保存する症状は、再現用テストがその状態を確かめます。修正版では `preview` が0回、`publish` が1回保存します。帳票日2026-11-05、購入明細1,200円と800円を両方に渡すと、どちらも合計2,000円の帳票を返します。保存先はテストごとのメモリ上のリストです。

`ReportService.java` の `preview` 内で `return publish(reportDate, amountsYen);` と呼ぶと、保存0回または全体で1回の期待が壊れて3件失敗します。別の展開先で `publish` の `store.save(report);` を外すと、確定の保存を確認する2件が失敗します。両方の変更は別々に試してください。配布するコードは修正版です。

- `ReportService.java` はプレビュー・確定・共通の計算。
- `Report.java` は帳票日と合計金額を保持する値。
- `ReportStore.java` は保存先の境界。
- `LegacyReportScreen.java` は旧コードの症状を再現するテスト専用の画面呼び出しと旧サービス。
- `ReportServiceTest.java` は旧コードと修正版を実行するテスト。
