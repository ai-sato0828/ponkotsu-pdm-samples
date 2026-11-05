# Spring BootのRestClientで404だけを「見つからない」にする

JDK 17、Maven 3.9系、Spring Boot 3.5.16のサンプルです。展開後、`pom.xml` のあるディレクトリで実行します。

```sh
mvn test
```

JUnitは5件です。200のJSONを見積として返し、404だけを `Optional.empty()` に変えます。503と401はHTTP例外を保持し、200で本文がなければ異常として扱います。

- `src/main/java/info/syachiku/shipping/ShippingQuoteClient.java` はHTTPステータスを呼び出し元の結果へ対応させます。
- `src/main/java/info/syachiku/shipping/ShippingQuote.java` は配送サービスと送料を受け取ります。
- `src/test/java/info/syachiku/shipping/ShippingQuoteClientTest.java` はURL・GET・Acceptと5種類の結果を確かめます。

`MockRestServiceServer` で応答を用意するため、外部APIやネットワーク接続は不要です。依存ライブラリを初めて取得する場合はMavenの接続が必要です。このプロジェクトは起動用のWebアプリを含みません。

503を見落とす不具合を再現する場合は、`ShippingQuoteClient` のcatchの型 `HttpClientErrorException.NotFound` を `org.springframework.web.client.RestClientResponseException` へ置き換えて実行します。正常、該当なし、200で本文なしの3件は通過し、503と401を確認する2件が失敗します。再現後はcatchの型を元に戻してください。
