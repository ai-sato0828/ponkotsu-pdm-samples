# Spring MVCのconsumesでvendor +jsonを受け付ける

注文プレビューのAPIに同じJSONを送って、`Content-Type` と `consumes` の組み合わせで応答が変わることを確かめます。`consumes` はどのControllerに渡せるかを絞ります。Jacksonは、その入口を通った本文をJavaの型へ変換します。

## 実行する

JDK 17とMaven 3.9系を使います。このサンプルの依存関係はSpring Boot 3.5.16で管理し、Spring MVC 6.2.19を使います。初回はMavenの依存関係をダウンロードできる環境が必要です。

ZIPを展開し、`spring-requestmapping-vendor-json-609` の `pom.xml` と同じ場所で実行します。

```sh
mvn test
```

MockMvcのテストなので、サーバーの起動やDB、外部APIは不要です。Controllerは比較ごとに一方だけ登録します。

## 同じ本文で4通りを確認する

すべて `POST /orders/preview` に `{"orderId":"ORD-42","quantity":2}` を送り、`Accept: application/json` を固定します。Serviceの呼び出しが1回なら、Controllerが受け取った注文番号と個数も照合します。

| `consumes` の指定 | リクエストの `Content-Type` | 応答 | Serviceの呼び出し |
| --- | --- | --- | ---: |
| `application/json` | `application/json` | 200、注文番号と個数のJSON | 1回 |
| `application/json` | `application/vnd.example.order+json` | 415、`HttpMediaTypeNotSupportedException` | 0回 |
| 通常のJSONと注文用vendor型 | `application/vnd.example.order+json` | 200、注文番号と個数のJSON | 1回 |
| 通常のJSONと注文用vendor型 | `application/vnd.example.invoice+json` | 415、`HttpMediaTypeNotSupportedException` | 0回 |

200の応答本文は `{"orderId":"ORD-42","quantity":2}` です。415ではControllerのハンドラーが選ばれていないことも確かめます。さらに1件のテストで、既定の `MappingJackson2HttpMessageConverter` が両方のvendor型を読み取り対象にできることを確認します。この5件で、変換器の対応形式とAPIが受け付ける形式を切り分けられます。

`src/main/java/info/syachiku/orders/OrderPreviewControllers.java` に変更前後の入口、`src/test/java/info/syachiku/orders/OrderPreviewMediaTypeTest.java` に比較手順があります。注文用vendor型を受け付ける契約なら、その型を明示します。契約が通常のJSONだけなら、送信側の `Content-Type` を `application/json` に揃えます。
