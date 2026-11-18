# Loggerの実効レベルとAppenderの出力条件を分ける

JDK17とMaven3.9系で、pom.xmlのある場所から実行します。

```sh
mvn clean test
mvn -q compile exec:java
```

既存1803の公開POMと同じLogback Classic1.5.34/SLF4J API2.0.17を採用します。JUnit5.12.2、JDK17.0.8/Maven3.9.16の既存cacheを使ってオフライン実行しています。空cacheからの初回依存取得は未確認です。

## 同じ架空ログを3条件で比較する

各ケースは独立したLoggerContextを作り、rootにINFOを設定します。架空のクラスLogger名は`example.shipping.PriceLookup`、メッセージはSKU-001の開始DEBUGと完了INFOだけです。子はadditive=true、ListAppenderはrootだけに付けます。外部APIやファイルは使いません。

| ケース | 子Logger指定 | 実効レベル | isDebugEnabled | rootの対象Appenderが受け取るイベント |
|---|---|---|---|---|
| INHERITED | なし（rootを継承） | INFO | false | INFOのみ |
| CHILD_DEBUG | DEBUG | DEBUG | true | DEBUG、INFO |
| THRESHOLD_INFO | DEBUG | DEBUG | true | INFOのみ（AppenderにThresholdFilter INFO） |

root INFOは、明示DEBUGの子から伝播したイベントを止める共通の最終しきい値ではありません。子がレベルを継承するときはroot INFOが実効値になりますが、子でDEBUGが有効になった後に親へ転送する場面では親のLoggerレベルを再評価しません。出力先にさらにINFO以上という条件が必要なら、そのAppenderのFilterも調べます。

反対に、isDebugEnabledがtrueでも、全ての出力先へDEBUGが表示されるとは限りません。3ケース目では同じイベントをAppenderのThresholdFilterが拒否します。Loggerの有効判定と、対象Appenderの受信結果を分けて確認してください。

## コードと実行結果

`src/main/java/example/logging/LoggerAppenderDemo.java`に3つの比較メソッドとmain、Result recordがあります。メソッドはroot/子の指定・実効レベル・isDebugEnabled・受信したイベント全文を返します。`src/test/java/example/logging/LoggerAppenderDemoTest.java`の3件はそれぞれの値とイベントの順序・文面までassertします。

```text
INHERITED root=INFO child=INHERITED effective=INFO debugEnabled=false events=[INFO:lookup complete sku=SKU-001]
CHILD_DEBUG root=INFO child=DEBUG effective=DEBUG debugEnabled=true events=[DEBUG:lookup started sku=SKU-001, INFO:lookup complete sku=SKU-001]
THRESHOLD_INFO root=INFO child=DEBUG effective=DEBUG debugEnabled=true events=[INFO:lookup complete sku=SKU-001]
```

独立LoggerContextは各ケースのfinallyでstopし、グローバルなLoggerFactoryや本番のログ設定を変更しません。ListAppenderでメモリに捕捉するだけで、監視通知・セキュリティ監視・ログ転送・実ファイル出力・実際の価格照会は試験対象ではありません。既存1803の処理結果からWARN/ERRORを選ぶ例とは別の比較です。

独立故障コピーでThresholdFilterの追加を止めると、3ケース目のDEBUG拒否テストが失敗し、復元後は3件成功します。入力ログは全て架空の固定値です。故障コピー/target/依存jar/実行ログは配布ZIPに含めず、MIT LICENSE/POM/README/対象/Testの5ファイルだけを収録します。
