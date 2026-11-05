# Mockitoで使われないstubを見つけるサンプル

JDK 17とMaven 3.9系を使用します。Springや外部サーバーは不要です。依存関係はJUnit Jupiter 5.12.2とMockito 5.17.0、テスト実行はMaven Surefire 3.5.3です。初回はMavenが依存を取得できる環境で実行します。

## 在庫照会の仕様

- 商品コード `SKU-100` なら倉庫へそのコードで1回問い合わせ、モックが返した8個を返します。
- 空白だけなら「商品コードを入力してください」という `IllegalArgumentException` にします。倉庫には問い合わせません。

## 実行

展開して `pom.xml` のあるフォルダに移動します。

```sh
mvn test
```

修正後の `StockInquiryServiceTest` が2件成功します。

```sh
mvn -Dtest=SharedStockStubbingSpec test
```

再現用のクラスが2件実行され、正常ケースは成功、空白ケースは `UnnecessaryStubbingException` でエラーになります。意図した終了コードは1です。クラス名の末尾を `Spec` にしてあり、このPOMの通常実行では実行されません。IDEから再現用クラスを選んだ場合はエラーを観察できます。

## ファイル

- `StockGateway.java` は倉庫への問い合わせを表すインターフェースです。
- `StockInquiryService.java` は商品コードを検証し、正常なら在庫を問い合わせます。
- `SharedStockStubbingSpec.java` は毎回共通のstubを作る再現版です。
- `StockInquiryServiceTest.java` は正常ケースでだけstubを作る修正版です。
- `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` は、このサンプルで使うインターフェースのモックをJavaのプロキシで生成する指定です。テストのstrictnessは変えていません。
