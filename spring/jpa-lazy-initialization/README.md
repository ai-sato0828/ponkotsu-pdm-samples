# Spring Bootで注文の明細を読むトランザクションを比較する

Spring Boot 3.5.16、JDK 17、Maven 3.9系で動くローカル教材です。H2のインメモリーDBに、注文42と明細 `BOOK × 2`、`PEN × 1` を起動時に登録します。外部DBや実データは使いません。

## テストを実行する

展開して `pom.xml` のあるフォルダーで実行します。

```sh
mvn clean test
```

| `LazyInitializationTest` | テストが確かめること |
| --- | --- |
| `testTransactionKeepsTheLazyLinesReadable` | テスト自身のトランザクション内では、未ロードだった明細を変換できる。`Hibernate.isInitialized` は `false` から `true` になる。 |
| `repositoryReturnDoesNotInitializeTheLinesWithoutAnOuterTransaction` | 外側のトランザクションがなく、Repositoryから戻った時点で明細は未ロード。変換すると `LazyInitializationException` が出る。 |
| `outsideEndpointExposesTheSameFailureWithoutATestTransaction` | テストのトランザクションがないMockMvcで `GET /orders/42/outside` を呼ぶと、`ServletException` の原因が `LazyInitializationException` になる。 |
| `insideEndpointReturnsACompleteDtoWithoutATestTransaction` | 同じ注文を `GET /orders/42/inside` で読み、HTTP 200と明細2件を含むJSONを照合できる。 |

合計4件です。失敗経路も、期待どおりの例外が出ることをアサーションするのでテストとしては通ります。MockMvcはServletコンテナのエラーページへの再ディスパッチを伴わないため、失敗経路では500の応答本文を比較せず原因例外を検査します。

## 実HTTPで500と200を比べる

ターミナルAでアプリを起動します。

```sh
mvn clean package
java -jar target/jpa-lazy-initialization-1.0.0.jar
```

ターミナルBで、同じ注文番号42を2つの経路で読みます。

```sh
curl -i http://127.0.0.1:18083/orders/42/outside
curl -i http://127.0.0.1:18083/orders/42/inside
```

`outside` はHTTP 500です。ターミナルAのログで `org.hibernate.LazyInitializationException` を探します。`inside` はHTTP 200で、次のJSONを取得できます。

```json
{"orderId":42,"lines":[{"sku":"BOOK","quantity":2},{"sku":"PEN","quantity":1}]}
```

サンプルは、HTTP応答に例外の詳細を含めません。アプリを終了するとH2のデータは消えます。ポート18083が使用中なら、起動コマンドの末尾に `--server.port=18084` を付け、2つのURLも同じ値へ揃えます。

## 変更箇所を読む順番

1. `PurchaseOrder.java` の `lines` は `@OneToMany` で関連付け、遅延ロードにしています。
2. `OrderController.java` の `outside` は `OrderService.load` から注文を受け取り、Controllerで `OrderDetails.from` を呼びます。`spring.jpa.open-in-view=false` のため、この時点ではRepositoryで注文を読んだ永続化コンテキストから外れています。
3. `OrderService.java` の `detailsInsideTransaction` は、Controllerから呼び出される `@Transactional(readOnly = true)` のメソッドです。同じ注文を読み、明細の商品コードと数量までDTOにコピーしてから返します。

`@Transactional` を付けたテストが通るときは、テストのトランザクションがRepositoryの処理より後まで続いていないか確認します。`readOnly = true` は読み取りを意図する指定です。この例で明細を読めるのは、Serviceメソッドでトランザクションを開始し、その範囲内でDTOの作成まで済ませるためです。

- [Spring BootのOpen EntityManager in View設定](https://docs.spring.io/spring-boot/3.5/reference/data/sql.html#data.sql.jpa-and-spring-data.open-entity-manager-in-view)
- [Spring TestContextのテスト管理トランザクション](https://docs.spring.io/spring-framework/reference/6.2/testing/testcontext-framework/tx.html)
- [Hibernate 6.6のLazyInitializationException](https://docs.hibernate.org/orm/6.6/javadocs/org/hibernate/LazyInitializationException.html)
