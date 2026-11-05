# Spring MVCで400になったときに自作コードの行を追う

JDK 17とMaven 3.9系を使います。ZIPを展開し、`pom.xml` のあるフォルダで実行します。

```sh
mvn -Dtest=StacktraceFlowTest test
```

MockMvcを使う4件のテストです。Spring Boot 3.5.16とSpring Framework 6.2.19を使用し、HTTPサーバーやDBは起動しません。

| `target/stacktrace-evidence` のログ | 入力 | HTTP | Controller呼び出し | 見る箇所 |
|---|---|---:|---:|---|
| `builtin-invalid.log` | `/quantity/int` に `three` | 400 | 0 | `NumberFormatException` に自作処理のフレームがない |
| `builtin-corrected.log` | `/quantity/int` に `2` | 200 | 1 | レスポンスが `{"quantity":2}` |
| `custom-broken.log` | `/quantity/custom` に前後空白付きの `" 2 "` | 400 | 0 | `BrokenQuantityConverter.java:8` にたどり着く |
| `custom-fixed.log` | `/quantity/custom` に前後空白付きの `" 2 "` | 200 | 1 | `strip()` を使うと `{"quantity":2}` |
| `custom-still-invalid.log` | `/quantity/custom` に `three` | 400 | 0 | 修正版でも文字列 `three` は拒否する |

各ログと同じ名前のJSONにHTTPステータス、例外チェーンと自作処理のフレームがあります。ログはMockMvcで取得した例外を検証用に出力したものです。`src/main/java` にControllerとConverterの全文、`src/test/java` にMockMvcテストの全文があります。記事冒頭の注文CSVログは説明例で、このプロジェクトはSpring MVCの比較を扱います。
