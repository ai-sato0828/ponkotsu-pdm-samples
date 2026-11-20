# Springの検査例外とrollbackForを4件で確かめる

注文の領収書発行を例に、DBの登録行を1行INSERTしたあと、出力が失敗するとその行が残るかを比べます。同じ処理を、`@Transactional` と `@Transactional(rollbackFor = IOException.class)` の2通りで呼びます。

## 実行する

JDK 17以上とMaven 3.6.3以上が必要です。Spring Boot 3.5.16、Spring JDBC、H2、JUnit Jupiterを使用します。ZIPの場合は展開し、`pom.xml` がある場所で次のコマンドを実行します。初回はMavenの依存ファイル取得が必要です。

```sh
mvn test
```

1件だけ試す場合は、たとえば次のコマンドを使います。

```sh
mvn -Dtest=ReceiptServiceTest#checkedExceptionCommitsByDefault test
```

## 観察する結果

| 呼び出し | 出力処理の結果 | 呼び出し元に届く例外 | サービスを抜けた後の登録行 |
| --- | --- | --- | ---: |
| `register` | 正常終了 | なし | 1 |
| `register` | `IllegalStateException` | 同じ `IllegalStateException` | 0 |
| `register` | `IOException` | 同じ `IOException` | 1 |
| `registerRollbackOnIo` | `IOException` | 同じ `IOException` | 0 |

`ReceiptServiceTest` はSpringから注入されたサービスのプロキシを呼びます。各ケースで、出力処理の直前にトランザクション内で該当の登録行が存在することを確認し、呼び出しを抜け、トランザクションが終わってから同じ `order_id` の行を数えます。例外の3ケースは、元の例外インスタンスがそのまま届くことも検証します。テストメソッドには `@Transactional` を付けていません。

`ReceiptService.java` が比較するサービス、`ReceiptServiceTest.java` が実行テスト、`schema.sql` がH2のテーブル定義です。ファイル出力はコールバックで模擬し、外部サービスや実ファイルとは接続しません。ロールバックで戻るのはこのDBへの書き込みです。すでに書き出したファイルや送信した通知が戻ることを示すサンプルではありません。Springの既定のロールバック設定を前提とします。
