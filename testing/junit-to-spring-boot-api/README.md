# JUnitで検証した送料をSpring Boot APIへつなぐ

Java 17以上、Maven、初回の依存取得にネット接続が必要です。Spring Bootは3.5.16に固定しています。

```sh
mvn -Dtest=ShippingFeeTest test
mvn -Dtest=ShippingQuoteHttpTest test
mvn clean test
```

最初は送料の単体テスト3件、次はHTTPのテスト4件です。全件では7件が成功します。

```sh
mvn spring-boot:run
```

アプリの起動後、別ターミナルで次のURLを確認します。

```sh
curl -i 'http://localhost:8080/shipping-quote?subtotal=8000'
curl -i 'http://localhost:8080/shipping-quote?subtotal=abc'
curl -i 'http://localhost:8080/shipping-quote?subtotal=-1'
```

8000円では200と `{"subtotal":8000,"shippingFee":500}` を返し、abcと-1は400です。

## 仕様を変える練習

送料無料の境界を10,000円から12,000円へ変更するとき、送料のテストの9,999円と10,000円を11,999円と12,000円に更新します。テストだけ先に変えると11,999円のテストが失敗します。本体の閾値を変えると成功します。HTTP契約の4件はそのままです。別の練習として、応答のJSON項目だけを `shippingFee` から `fee` に変えると、送料の3件が成功したままHTTPのJSON検証が失敗します。
