# Spring Bootの入力位置とJSON受信

JDK 17、Maven 3.9系、Spring Boot 3.5.16。展開したディレクトリで実行します。

```sh
mvn test
mvn spring-boot:run
```

別のターミナルから同じディレクトリで実行します。

```sh
bash requests/locations.sh
bash requests/body-errors.sh
```

- `locations.sh` はパスのID、検索クエリ、JSONによる登録、URLエンコードしたHTMLフォームの順に送信します。結果は200、200、201、302です。ブラウザーでは `http://localhost:8080/users/new` が登録フォームです。
- `body-errors.sh` は不完全なJSON、数値に変換できない値、本文なし、DTOの制約違反を順に送ります。どれも400ですが、MockMvcテストでは例外とServiceの呼び出し回数も確かめます。
- JSONの未知プロパティをエラーにする設定を、このサンプルの `application.properties` に明記しています。公開APIの方針に応じて使い分けてください。
- 利用者はメモリー上のサンプルデータです。再起動すると初期データに戻ります。
