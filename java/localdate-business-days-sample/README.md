# LocalDateで回答期限に営業日を加算するサンプル

JDK 17とMaven 3.9系を使います。JUnit Jupiter 5.12.2、Maven Surefire 3.5.3です。初回はMavenが依存を取得できる環境で実行します。

```sh
mvn test
```

ZIPを展開し、`pom.xml` のある場所で実行すると、`BusinessDaysTest` が10件成功します。

## 仕様

- 入力は受付日、0〜60の営業日数、休業日の集合です。
- 1以上の場合は翌日から数えます。土日と、集合にある休業日では残りを減らしません。
- 0なら受付日を返します。受付日が休業日でも繰り延べません。
- 負数・61以上は `IllegalArgumentException`。日付の上限を超えると `DateTimeException` です。
- 受付日、休業日の集合、集合の要素に `null` は指定できません。休業日を設定しないときは `Set.of()` を渡します。
- 休日カレンダーの取得、日付が足りているかの確認、時刻の締切はこのメソッドの外で扱います。

`BusinessDays.java` の `while` ループを外して `return receivedOn.plusDays(businessDays);` に変え、同じテストを実行すると5件失敗します。失敗の再現は別の展開先で行ってください。サンプル本体は修正版です。
