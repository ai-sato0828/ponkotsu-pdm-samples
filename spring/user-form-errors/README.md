# Spring Bootのフォーム再表示とBindingResultの位置

JDK 17、Maven 3.9系、Spring Boot 3.5.16。展開したディレクトリで実行します。

```sh
mvn test
mvn spring-boot:run
```

- ブラウザーで `http://localhost:8080/users/new` を開きます。部署は開発、利用枠は1が初期値です。利用枠に `abc` を入れて送ると、入力と選んだ部署が残り、整数を求めるエラーが出ます。
- `http://localhost:8080/demo/users/wrong-form-name` は意図的な失敗例です。Modelに入れる名前が `form` のため、`userRegisterForm` を使うテンプレートの描画に失敗します。
- 別のターミナルから同じディレクトリで `bash requests/compare.sh` を実行すると、正しい順番で200、間に `Model` を挟む失敗例で400を返します。
- `/demo/users/wrong-order` と `/demo/users/implicit` は比較用です。テストでは、誤った引数の順番でも入力が正常なら登録できること、明示アノテーションを省略したときのモデル名を確認します。
- 登録先はサンプル内のメモリーです。再起動すると消えます。
