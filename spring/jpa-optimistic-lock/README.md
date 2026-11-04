# Spring Boot JPAの楽観ロックと古い編集画面

JDK 17とMaven 3.9系で、展開したフォルダから次を実行します。

```sh
mvn clean test
```

Spring Boot 3.5.16、組み込みH2、MockMvcで検証します。テスト実行時に
作られる一時DBを使います。起動中のHTTPサーバーや外部DBは不要です。

- `ShippingNote` はJPAの `@Version` 付きの配送メモ。
- `ShippingNotesController` のGETは現在の版とメモを返します。PUTはその版を受け取り、
  保存時点の版と一致する場合にメモを変更します。
- `BeforeShippingNotesController` はテスト専用の失敗例。受信した版を使わず、
  保存時に取得したEntityの版で更新します。
- `ShippingNoteUpdateTest` は修正前後、版欠落・null・負数、同じ値の保存、
  トランザクションが競合するケースを実行します。

テストの初期値は「指定なし」と版0。Aが「午前に配達」を保存した後、
版0のままのBが「置き配」を送ります。失敗例では200で版2に上書きされます。
修正版は409と `STALE_SHIPPING_NOTE` を返し、DBにはAのメモと版1が残ります。
別のテストは、二つのトランザクションが版0を読み、Aのコミット後に
Bがflushすると競合として拒否されることを確認します。

このフォルダはテストで再現するための例です。認証、配送画面、DB移行は含めていません。
