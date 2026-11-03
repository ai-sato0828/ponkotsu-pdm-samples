# Spring Bootでバリデーションメッセージが表示されない場合の比較

同じ `POST /users` で、`BindingResult` に名前のエラーが1件あっても、Thymeleafの `th:errors` が正常な `email` を参照すると名前の文言が表示されないことを確認する独立プロジェクトです。仕様と期待結果は [docs/spec.md](docs/spec.md) にあります。

JDK 17とMavenが必要です。確認環境はJava 17.0.8、Maven 3.9.5、Spring Boot 3.5.16、Spring Framework 6.2.19、Thymeleaf 3.1.5.RELEASE、Hibernate Validator 8.0.3.Final、JUnit 5.12.2です。Maven依存を初めて取得する際はネットワーク接続が必要です。開発環境には既存の依存キャッシュがあり、未取得だったValidation starter等はMaven Centralから取得しました。空の依存キャッシュからの取得は検証していません。

## テストと起動

ZIPを展開し、このREADMEと `pom.xml` があるフォルダで実行します。

```sh
mvn -B test
mvn -B package
java -jar target/validation-message-display-1.0.0.jar
```

`http://127.0.0.1:8080/users/new` を開き、名前を空欄、メールを `test@example.com` にして登録します。名前の下に「名前を入力してください。」が表示され、メールが残ります。次に名前へ「山田太郎」を入れて登録すると、`/users` の一覧に1件が表示されます。再確認する場合は、停止して起動し直すと登録内容が消えます。ポート8080が使用中なら、`java -jar ... --server.port=18775` のように変更します。起動先は127.0.0.1に限定しています。

## ファイルの読み順

1. `src/main/java/info/syachiku/validation/UserController.java`：`@Valid`の結果を受け取り、エラー時は画面を返す。正常時はServiceを呼んでリダイレクトする。
2. `src/main/java/info/syachiku/validation/UserForm.java`：名前の必須・長さと、メールの形式。
3. `src/main/resources/templates/users/input.html` と `messages.properties`：項目とエラー表示、解決する日本語文言。
4. `src/test/resources/comparison/`：本番用テンプレートと同じfixed、`th:errors`の1箇所だけ変えたbroken。
5. `src/test/java/info/syachiku/validation/UserFormHtmlTest.java`：Model、レンダリング済みHTML、Service呼出しの8ケース。

故障テンプレートとリダイレクト比較Controllerはテスト専用です。普段起動するアプリに故障比較用URLはありません。Serviceはメモリ上に登録し、実DB、認証、CSRF、ブラウザのCSSやJavaScriptは含めません。起動用JARの再現では、ローカルHTTPで初期GET・空欄POST・正常POST・一覧GETを確認しました。

このサンプルはMockitoを使わないため、テスト用starterからMockito依存2件を除いています。既存プロジェクトへ移す際に、そのプロジェクトで利用しているMockito依存まで外す必要はありません。
