# 同じGETの重複でSpring Bootが起動できないケースを確認する

JDK 17、Maven 3.9系、Spring Boot 3.5.16、JUnit Jupiter 5.12.2、Surefire 3.5.6を使用します。ZIPを展開し、`pom.xml` のある場所で実行します。初回はMavenが依存を取得できる環境が必要です。

```sh
mvn test
```

通常は7件成功します。 `BrokenMappingTest` は別のテスト用コンテキストで同じ `GET /orders` を二つ登録し、起動失敗を期待するテストです。正常なテスト実行でも、意図した `Ambiguous mapping` のログが出ます。

修正案は同じURLを使う別々の選択肢です。設定 `orders.mapping=combined` は1ハンドラーへ統合（既定）、`orders.mapping=params` は `!status` と `status` の排他的な条件で分けます。両方の実装を一度に有効にしません。両案は `GET /orders` が2件、`GET /orders?status=OPEN` が1件、`GET /orders?status=` が0件を、HTTP 200のJSONで返します。

重複をもう一度作るには、別の展開先で `ParameterOrdersController.java` の二つの `params` 指定を外して `mvn test` を実行します。条件付き版のコンテキストが起動できず、HTTPテスト3件がエラーになります。配布コードは修正版です。
