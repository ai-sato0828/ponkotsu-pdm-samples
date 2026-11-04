# Spring MVCのキャッシュとThymeleafのURL

JDK 17とMaven 3.9系で `mvn test` を実行します。Spring Boot 3.5.16のMockMvcとThymeleafで確認します。

- 1156。個人データのレスポンスは `Cache-Control: no-store`、公開ヘルプは `max-age=300, public` を返すことを確認します。
- 1058。コンテキストパスを含む詳細URLをリンクとdata属性に生成します。引用符・アンパサンドを含む元の検索語が、遷移先のGETに渡ることを確認します。
- 1106。HTMLのCSS参照と、JavaScriptが扱う文字列をそれぞれ適切な文脈でレンダリングします。外部URLは説明用の固定値で、外部アクセスは行いません。

任意で `mvn spring-boot:run` を実行し、ブラウザから `http://localhost:8080/navigation?q=sample` を開けます。ブラウザでのボタン操作と公開記事の描画は、MockMvcによるレンダリング確認とは別です。
