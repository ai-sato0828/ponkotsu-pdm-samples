# 例外を渡したのにLogbackでスタックトレースが出ない場合

JDK 17とMaven 3.9系を使います。ZIPを展開して、`pom.xml` のあるディレクトリーで実行します。

```sh
mvn test
mvn -q compile exec:java
```

初回は依存の取得にネット接続が必要です。Logback Classic 1.5.34はSLF4J API 2.0.17を解決します。このサンプルのログ出力は独立したLoggerContext内で実行します。

1. `logger.error("在庫の保存に失敗 id={}", "I-17", exception)` で、同じログイベントに文面と例外を持たせます。
2. `getThrowableProxy()` の型を表示して、フォーマット前に `java.lang.IllegalStateException` が保持されていることを確認します。
3. 同じイベントを3つのパターンで整形します。実行例は各パターンの出力の先頭3行まで表示します。

| パターン | 実行すると表示される情報 |
| --- | --- |
| `%msg%n%nopex` | 文面のみ |
| `%msg%n%ex` | 同じ文面、例外の型とメッセージ、`at` 行 |
| `%msg%n` | 同じ文面、例外の型とメッセージ、`at` 行。LogbackのPatternLayoutが変換指定を補います |

Logbackで文面だけ届く場合は、まず末尾引数でThrowableを渡しているかを確かめます。イベントに例外があるなら、次に出力先のappenderに適用されるパターンを確認します。`%nopex` が指定されていれば、修正例のように `%ex` に替えます。

`mvn test` の4件は、イベントが持つ例外と、3通りの出力を確かめます。記事のSLF4J Simple 1.7.30のサンプルでは、呼び出し時にThrowableを渡すかで出力が変わることを確かめられます。こちらのサンプルでは、同じイベントを出力パターンでどう表示するかを比較します。

[Logback公式マニュアルのPatternLayout](https://logback.qos.ch/manual/layouts.html#conversionWord)
