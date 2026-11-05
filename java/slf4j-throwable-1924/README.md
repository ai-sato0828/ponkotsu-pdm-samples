# SLF4JでメッセージとThrowableの渡し方を比べる

JDK 17とMaven 3.9系を使用します。ZIPを展開し、`pom.xml` があるフォルダで `mvn test` を実行します。SLF4J APIとバインディングは `slf4j-api` 1.7.30と `slf4j-simple` 1.7.30に固定しています。`src/main/java/Slf4jThrowableDemo.java` に比較するログの呼び出し、`src/test/java/Slf4jThrowableDemoTest.java` にテスト全文があります。

7ケースの出力は `target/slf4j-logs` に保存されます。それぞれ1回の `ERROR` 呼び出しです。

| ログ名 | 先頭のメッセージ | スタックトレース |
|---|---|---|
| `message-only.log` | `Save failed: Connection refused` | 0回 |
| `throwable-only.log` | `保存に失敗` | 1回 |
| `id-then-throwable.log` | `userId=U001 の保存に失敗` | 1回 |
| `extra-placeholder.log` | `userId=U001 の保存に失敗: {}` | 1回 |
| `throwable-not-last.log` | `保存に失敗: java.lang.IllegalStateException: Connection refused userId=U001` | 0回 |
| `last-is-string.log` | `userId=U001 の保存に失敗: Connection refused` | 0回 |
| `lone-throwable-placeholder.log` | `保存に失敗: {}` | 1回 |

例外はテスト用に組み立てたもので、データベースや外部サービスには接続しません。ログの接頭辞や出力先はバインディングと設定によって変わります。この実行ではSimple以外の出力実装は扱いません。
