# JSONの quantity に整数だけを受け付ける

個数を表す `Integer quantity` が、JSONの数値 `2`、数字の文字列 `"2"`、文字列 `"二個"`、小数 `2.9` をどう読み取るかを比較します。元の数量必須サンプルに、`quantity` だけJSON整数に限定する比較用エンドポイントを追加しました。

## 実行

JDK 17とMavenを使います。展開した `jackson-integer-quantity-coercion` ディレクトリで次を実行します。

```sh
mvn test
```

既存の `QuantityBoundaryTest` 10件と、今回追加した `JacksonQuantityCoercionTest` 12件を実行します。`COERCION` で始まる行に、HTTPステータス、Serviceの呼び出し回数と受け取った数量、解決済み例外が出ます。

## 同じ注文データを送る

送信先は `POST /api/required/orders`（既定のJackson）と `POST /api/strict/orders`（quantityに整数を要求）の2つです。どちらにも `Content-Type: application/json` を指定します。

```json
{"customerId":10,"quantity":2}
{"customerId":10,"quantity":"2"}
{"customerId":10,"quantity":"二個"}
{"customerId":10,"quantity":2.9}
```

各行が1件のJSON本文です。

| quantity | 既定のJackson | quantityにJSON整数を要求 |
|---|---|---|
| `2` | HTTP 200、Service 1回、数量2 | HTTP 200、Service 1回、数量2 |
| `"2"` | HTTP 200、Service 1回、数量2 | HTTP 400、Service 0回 |
| `"二個"` | HTTP 400、Service 0回 | HTTP 400、Service 0回 |
| `2.9` | HTTP 200、Service 1回、数量2 | HTTP 400、Service 0回 |

既定の `"二個"` は `HttpMessageNotReadableException` で、原因は `InvalidFormatException` です。厳格側の文字列2件と小数は同じSpring例外になり、原因は `MismatchedInputException` です。JSONの構文は4件とも正しく、ここで比較しているのは数量プロパティを `Integer` として読み取る段階です。

## JSON整数の判定と値の制約

`OrderController.StrictOrder` の数量に `@JsonDeserialize` を付け、`JsonIntegerQuantityDeserializer` が `VALUE_NUMBER_INT` を要求します。この指定は `quantity` だけに効きます。厳格側でも `{"customerId":"10","quantity":2}` はHTTP 200で、Serviceは顧客ID 10、数量2を受け取ります。

`0` はJSON整数として読み取れます。DTOへの変換後に `@Min(1)` が検証に失敗し、HTTP 400と `MethodArgumentNotValidException` になります。省略と明示 `null` は同じSpring例外になり、失敗する制約は `@NotNull` です。3ケースともServiceの呼び出しは0回です。

## 旧サンプルと実行範囲

元のエンドポイント `/api/min-only/orders` と `/api/required/orders` は、数量の省略・明示 `null`・`0`・`1` を比較します。既存の2 DTO、Service、テストは維持しています。追加した `/api/strict/orders` のServiceも注文データを返すだけです。HTTP 200はサンプルの戻り値が返ったことを示します。

Spring Boot 3.5.16、Jackson Databind 2.21.4、JDK 17.0.8で22件を確認しました。MockMvcを使い、HTTPステータス、サーバー側例外、実際のService引数を検証します。実TCPやDBへの接続は含みません。
