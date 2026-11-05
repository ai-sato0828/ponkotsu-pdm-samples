# Spring BootのテストでClockを固定して受付締切を確かめる

JDK 17、Maven 3.9系、Spring Boot 3.5.16、JUnit Jupiter 5.12.2、Surefire 3.5.6を使います。ZIPを展開し、`pom.xml` のある場所で実行します。初回はMavenが依存を取得できる環境が必要です。

```sh
mvn test
```

`GET /application-window` は受付可否 `accepting` と締切 `closesAt` を返します。締切は `2026-11-20T08:00:00Z` で、締切の1ナノ秒前まで受付可能、同時刻と以降は受付終了です。状態照会は、受付終了時もHTTP 200で返します。

通常のアプリは `applicationClock` から `Clock.systemUTC()` を使います。テストは `FixedClockConfiguration` を明示的に読み込み、別名の `fixedTestClock` を `@Primary` で選びます。締切は固定し、各コンテキストの `test.clock.instant` だけを変えます。

通常は5件成功します。別の展開先で `ApplicationWindow.java` の `clock.instant().isBefore(closesAt)` を `!clock.instant().isAfter(closesAt)` に置き換えると、締切ちょうどの1件だけ失敗します。配布するコードは修正版です。

- `ApplicationWindowApp.java` は本番のClock Bean、`ApplicationWindow.java` は受付可否の判定、`ApplicationWindowController.java` はHTTP入口。
- `application.properties` はUTCの締切。 `FixedClockConfiguration.java` はテスト専用設定。
- `BeforeDeadlineTest.java`、`AtDeadlineTest.java`、`AfterDeadlineTest.java` はHTTP応答とテストのDI、`ProductionClockTest.java` は固定時計の設定を読み込まない構成を確認。
