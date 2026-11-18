# コンストラクター注入でBeanが作れない原因を確かめる

Spring Framework 6.2.19、JDK 17、Maven 3.9系を使います。コンテナだけで実行し、Spring Bootやデータベースは使いません。

```sh
mvn -B -ntp test
mvn -B -q -ntp compile exec:java
```

この検証では依存が取得済みのため、上のコマンドに `-o` を付けて実行しました。

## 起動に失敗するコード

注文側の `CyclicOrderService` は請求書の発行を依頼するため、コンストラクターで `CyclicInvoiceService` を受け取ります。請求書側は注文の合計を調べるため、コンストラクターで注文側を受け取ります。

起動ログには最初に `orderService`、次に `invoiceService`、最後に生成中の `orderService` が出ます。この2クラスのコンストラクターを開くと、互いを引数にしていることを確認できます。実行結果には `UnsatisfiedDependencyException` が2件、最も深い原因に `BeanCurrentlyInCreationException` が出ます。コンテナの `allowCircularReferences` を `false` または `true` にしても、今回のコンストラクターの循環では起動時に同じ例外が出ます。

## 循環を解いたコード

`CheckoutWorkflow` が `OrderService` から注文を取得し、`InvoiceService` に取得済みの `Order` を渡します。どちらのServiceも、もう一方のServiceを必要としません。修正後はコンテナが起動し、固定の注文例 `ORDER-42` の請求額として1800円を返します。

JUnitは循環の2条件で例外の型・Bean名・停止を確認し、修正後の1条件で起動と請求額を確認します。コンテナの生成時点を確かめるためのサンプルなので、注文データの取得は固定値です。Spring Boot起動時のFailure Analyzer出力や遅延注入など、別の構成での挙動は検証対象外です。
