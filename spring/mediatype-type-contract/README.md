# Spring MediaType：受け側APIの型からJSON定数を選ぶ

Java17、Maven3.9系。Spring Boot3.5.16 / Spring Framework6.2.19 / JUnit5.12.2固定です。初回は依存取得のネットワークが必要。空のMavenキャッシュからの全依存取得は未検証です。

```sh
mvn clean test
mvn package
java -jar target/mediatype-type-contract-1.0.0.jar
```

POST `/order-previews` は `{"itemCode":"BOOK-001","quantity":1}` を受け、HTTP200・Content-Type application/json・`{"itemCode":"BOOK-001","quantity":1,"status":"preview"}` を返します。保存・料金計算・在庫引当は行いません。GET `/order-previews/example` は同じ見本を返します。DB・外部API・認証は含みません。

## 型の使い分け

- `@PostMapping`のconsumes/produces、`@GetMapping`のproduces：String定数`MediaType.APPLICATION_JSON_VALUE`。
- `ResponseEntity.BodyBuilder.contentType(MediaType)`：`MediaType.APPLICATION_JSON`。
- `HttpHeaders.setContentType(MediaType)`：`MediaType.APPLICATION_JSON`。

同名メソッドがすべて同じ型を受けるわけではありません。MockMvcのcontentTypeにはStringのオーバーロードもあります。このサンプルは上記の受け側の型を対象にします。文字列の低水準ヘッダー設定APIとの一般化もしません。

## 7テストの内訳

`CompilerContractTest`の3ケースは、各々で次の全てを確認します。

1. broken/fixedの差が指定した1箇所だけ。
2. brokenをJDK17のJavaCompilerでコンパイルすると、対象のエラーコード1件を得る。
3. fixedはコンパイル成功。

| ファイル | 故障 | 修正 |
|---|---|---|
| AnnotationObject.java | producesへMediaTypeオブジェクト | String定数_VALUEへ |
| BuilderString.java | ResponseEntity.contentTypeへString | MediaTypeオブジェクトへ |
| AnnotationToString.java | 注釈値でtoString()を呼ぶ | 定数式のString定数_VALUEへ |

故障例は`examples/broken`、修正版は`examples/fixed`に分離し、通常のmain/testコンパイル対象へ入れません。テストから意図的にコンパイルします。JREだけでは動かず、JavaCompilerを含むJDKが必要です。テストが成功するのは、故障版が実際に失敗することも確認できたという意味です。

残る`OrderPreviewHttpTest`の4ケースは、POST3入力（BOOK-001/1、PEN-002/3、用紙-003/2）とGET見本1入力です。全てHTTP200、正確なContent-Type、JSON本文の入力値・statusを検証します。

## 実HTTPによる確認

JDK17、Python3で、ビルド後に次を実行できます。

```sh
python3 scripts/verify_http.py target/mediatype-type-contract-1.0.0.jar
```

127.0.0.1の空きポートへJARを起動し、同じ4リクエストを確認して必ず停止します。外部DB/APIへ接続しません。常駐させる必要はありません。

## 収録範囲

POM、起動クラス、Controller、JsonHeaders、テスト2クラス、broken/fixed各3ファイル、HTTP確認スクリプト、README、LICENSE。JAR・依存JAR・class・個人設定はZIPに含めません。

注釈は受け付ける/返せる形式の条件、レスポンスヘッダーはこの応答本文の形式を表します。定数を取り替えるだけでJavaオブジェクトがJSONへ変換されるわけではなく、このWeb Starterに含まれる変換器と本文型でJSON応答を生成します。415/406の詳しい調査は別記事へ分担します。

[ブログ記事（公開後）](https://syachiku-uz.info/spring-mediatype-application-json-value-difference/)
