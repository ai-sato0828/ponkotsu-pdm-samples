# Spring Bootの注文検索を@RequestParamからFormクラスへ移す

JDK 17、Maven 3.9系、Spring Boot 3.5.16。

```sh
mvn test
mvn spring-boot:run
```

- `/demo/orders/simple?keyword=Coffee&status=CLOSED` は2条件だけを引数で受け、注文3をJSONで返します。
- `/demo/orders/before` は6項目を個別に受ける比較用のGETです。日付を変換できないと400を返します。
- `/orders` は6項目を `OrderSearchForm` にまとめ、Thymeleafで検索画面を表示します。日付の型変換、日付範囲、ページ番号のエラー時は入力を残して検索画面に戻ります。
- 変更前と変更後のGETは比較のため別のパスです。既存の画面をリファクタリングするときは、公開中のパスと既存のクエリ名を引き継ぎます。

正常時は次のクエリを両方に付けると注文1を返します。

```text
?keyword=Coffee&status=OPEN&fromDate=2026-10-01&toDate=2026-10-02&staffId=S01&page=1
```

注文はサンプル内の固定データです。検索1ページの件数は2件で、ページ番号を省略するか空で送ると1ページ目を返します。テストはMockMvcを使い、Spring MVCの実際のバインディングとThymeleafの出力を確認します。
