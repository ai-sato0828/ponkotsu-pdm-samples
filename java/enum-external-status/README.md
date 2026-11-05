# JavaのEnum.valueOf、Spring MVC、外部値の変換を比較する

JDK 17、Maven 3.9系、Spring Boot 3.5.16で実行します。ZIPを展開し、`pom.xml` のあるフォルダで `mvn test` を実行してください。Spring MVCのリクエストはMockMvcで確認します。

- Javaの `Status.valueOf("ACTIVE ")` は `IllegalArgumentException` になります。
- Spring MVC 6.2.19の標準変換を使う `/orders` は `ACTIVE ` を受け取って200、本文は `ACTIVE` です。小文字 `active` は400です。
- 小文字の `active` を外部値として受け取る `/orders/external` は200を返します。大文字や前後空白のある値は400です。

`StatusController.java` の `Status.fromValue(status)` を `Status.fromValue(status.trim().toLowerCase(java.util.Locale.ROOT))` に変更すると、外部契約にない大文字や空白付きの入力まで通り、対応する3テストが失敗します。

HTTPサーバーの起動、JSONボディの変換、DB保存は対象に含みません。
