# Spring BootでJSONの日付が400になる例を検証する

同じ `2026/11/24` をGETのクエリとPOSTのJSONで送ると、Spring MVCの入口によって日付の変換方法が変わります。Spring Boot 3.5.16のMVC設定とJacksonをそのまま使い、MockMvcでステータス、Controller内まで到達したか、受け取った `LocalDate` を比べます。

## 実行する

JDK 17とMaven 3.9系で確認しました。展開したZIPの `pom.xml` があるディレクトリで、次のコマンドを実行してください。初回はMavenの依存ファイル取得が必要です。

```sh
mvn test
```

本文の読み取り失敗だけを試すには次を実行します。

```sh
mvn -Dtest=OrderDateControllerTest#postSlashWithDateTimeFormatDoesNotEnterController test
```

## 期待する結果

POSTの `Content-Type` は `application/json`、本文のプロパティ名は `orderDate` です。GETは `orderDate` クエリパラメーターを送ります。

| リクエスト | 入力値 | HTTP | Controllerが受け取った値 |
| --- | --- | ---: | --- |
| `GET /orders` | `2026/11/24` | `200` | `LocalDate`の`2026-11-24` |
| `POST /orders/iso` | `2026/11/24` | `400` | 到達しない |
| `POST /orders/iso` | `2026-11-24` | `200` | `LocalDate`の`2026-11-24` |
| `POST /orders/slash` | `2026/11/24` | `200` | `LocalDate`の`2026-11-24` |
| `POST /orders/slash` | `2026/02/29` | `400` | 到達しない |
| `POST /orders/slash` | `2026-11-24` | `400` | 到達しない |

成功時のレスポンスは3件とも `{"orderDate":"2026-11-24"}` です。リクエスト用DTOとレスポンス用レコードは別にしています。`/orders/slash` の入力がスラッシュ形式でも、レスポンスの表現はISOのままです。

`/orders/iso` の `MvcDateBody` への `@DateTimeFormat` は、JSONを読むJacksonへ形式を指定できないことを比較するためです。形式をISOにそろえられる設計では、この注釈をJSON用DTOに残す必要はありません。スラッシュ形式がAPI契約の場合だけ、`SlashDateBody` に `@JsonFormat` を付けています。`lenient = OptBoolean.FALSE` と `uuuu` を組み合わせ、存在しない日を繰り上げたり丸めたりせず失敗させます。

400の3件は、ルートの選択後にJSONを `LocalDate` に読めず `HttpMessageNotReadableException` になったこと、Controllerのリクエスト属性に値が設定されていないことを確認します。原因の切り分けのためのサンプルで、必須項目や `null`、他のデータ型、業務上の日付範囲のバリデーションは扱いません。試験はMockMvc上で実行し、HTTPサーバーや外部サービスは起動しません。
