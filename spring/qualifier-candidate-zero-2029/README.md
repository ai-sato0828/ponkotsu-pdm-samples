# Beanは登録済みでもQualifierが合わず候補が0件になる

JDK 17とMaven 3.9系で、ZIPを展開した先の `pom.xml` と同じ場所から実行します。初回は依存関係の取得にネット接続が必要です。

```sh
mvn clean test
```

Spring Boot 3.5.16、Spring Framework 6.2.19、JUnit 5.12.2を使います。カードと銀行振込を模した2つのスタブだけを使用します。決済API、Webサーバー、DBは不要です。

| Bean名 | 定義側のQualifier | スタブに注文ID 42を渡した結果 |
| --- | --- | --- |
| `cardGateway` | `card-gateway` | `CARD:42` |
| `bankGateway` | `bank-gateway` | `BANK:42` |

## ログに出た注入条件から修正箇所を決める

1. コンポーネントスキャンの範囲を広げる前に、型と注入条件を確認します。最深の `NoSuchBeanDefinitionException` の `Dependency annotations` に `card-gateawy` が出ます。
2. 注入側の値を確認し、定義側の `@Qualifier("card-gateway")` と照合します。この例の `cardGateway` はBean名、`card-gateway` は明示したQualifierです。
3. 注入側を `card-gateway` に直します。同じ2つの登録で起動し、カード用のスタブが呼び出されます。

JUnit 3件は、登録だけの構成に2つのBeanがあること、不一致で `checkoutService` の作成が失敗すること、修正後は登録2件のまま `CARD:42` を返すことを確認します。ログの `REGISTERED`、`WRONG`、`Caused by`、`FIXED` 行を見れば結果を確認できます。テスト同士は別のコンテナを使います。

## ファイルの役割

- `PaymentGateway.java` はサービスが要求する型、`GatewayConfiguration.java` は2つのスタブを登録する定義です。
- `CheckoutConfigurations.java` は同じ `CheckoutService.java` を、誤ったQualifierと正しいQualifierでそれぞれ構成します。
- `QualifierCandidateTest.java` は `ApplicationContextRunner` で登録状態、失敗、修正後を別々に確認します。

[Spring Framework 6.2公式マニュアルのQualifier](https://docs.spring.io/spring-framework/reference/6.2/core/beans/annotation-config/autowired-qualifiers.html)
