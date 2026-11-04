# JavaからSpring Bootへ 注文明細の見積API

JDK 17とMaven 3.9系を使います。Spring Bootの版はpom.xmlで3.5.16に固定しています。

## 1. Javaだけで2,750円を表示する

数量と単価は、両配列の添字が対応している前提です。

```sh
javac -d out src/main/java/example/orderquote/QuoteFromArrays.java
java -cp out example.orderquote.QuoteFromArrays
```

## 2. 明細と合計の責務を分ける

- `LineItem` は数量1〜100、単価0〜1,000,000円を受け付け、1行分の金額を返します。
- `QuoteCalculator` は1〜50件の明細を合計します。50件の上限例で合計は5,000,000,000円です。

## 3. 業務ルールをJUnitで確認する

```sh
mvn -Dtest=QuoteCalculatorTest test
```

## 4. JSONで見積を計算する

```sh
mvn -Dtest=OrderQuoteHttpTest test
mvn clean test
mvn spring-boot:run
```

起動後、別ターミナルで同じディレクトリに移動します。

```sh
curl -i http://localhost:8080/order-quotes -H 'Content-Type: application/json' --data @requests/valid.json
curl -i http://localhost:8080/order-quotes -H 'Content-Type: application/json' --data @requests/missing-price.json
curl -i http://localhost:8080/order-quotes -H 'Content-Type: application/json' --data @requests/invalid-quantity.json
```

順にHTTP 200と `{"total":2750}`、HTTP 400と `INVALID_BODY`、HTTP 400と `INVALID_QUOTE` を返します。数字を文字列や小数で送った場合は `INVALID_JSON` です。学習用の見積であり、単価は送信値、金額は整数の円、永続化はありません。実際の注文では商品IDからサーバーの価格を取得します。

記事は [JavaからSpring Bootまでの学習順](https://syachiku-uz.info/java-spring-boot-order-quote-api/) です。
