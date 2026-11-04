# JUnitのタイムアウトとSpringテストのロールバック

JDK 17、Maven 3.9系、Spring Boot 3.5.16。テスト用の組み込みH2を使います。ZIPを展開して実行します。

```sh
mvn clean test
```

通知履歴を1行追加する処理を、同じJDBC設定で比較します。

| テスト | 実行スレッド | INSERT直後 | テストトランザクション終了後 |
| --- | --- | --- | --- |
| id 100、assertTimeoutPreemptively | 別スレッド | 1行 | 1行 |
| id 200、assertTimeout | 同じスレッド | 1行 | 0行 |

テストは意図して時間内に処理を終え、タイムアウト自体は発生させません。違いはINSERTがどのスレッドで実行されたかです。
`@AfterTransaction` で件数を確認してから、そのテストのIDだけを削除します。
