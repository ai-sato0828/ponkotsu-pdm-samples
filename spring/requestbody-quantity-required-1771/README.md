# quantity の省略・null・0・1を同じJSONで比較する

旧配布 `01-validation-basics` の17件は変更せず、新しい独立moduleで数量の必須要件を比較します。旧DTOも新しいMinOnlyOrderも `@Min(1) Integer quantity` です。新しいRequiredOrderだけに `@NotNull` を追加します。quantityを任意にしたい仕様ならMinだけでも誤りではありません。

## 実行

JDK17、Maven3.9系でこのREADMEとpom.xmlのあるディレクトリから実行します。

```sh
mvn test
```

新規テストはHTTP比較8件＋Jackson単体比較2件の計10件です。旧17件とは別の件数です。MockMvcで実際のJackson/Bean Validation/Controller/Service境界を検証し、TCPサーバやDBは使いません。

## 同じ入力と期待結果

customerIdは全ケースで10を送り、数量以外の制約違反を混ぜません。

|quantity|MinだけのHTTP/Service|NotNull＋MinのHTTP/Service|fieldエラー|
|---|---|---|---|
|項目を省略|200、nullで1回到達|400、到達0回|修正版quantity:NotNull|
|明示null|200、nullで1回到達|400、到達0回|修正版quantity:NotNull|
|0|400、到達0回|400、到達0回|両方quantity:Min|
|1|200、1で1回到達|200、1で1回到達|なし|

```json
{"customerId":10}
{"customerId":10,"quantity":null}
{"customerId":10,"quantity":0}
{"customerId":10,"quantity":1}
```

各行がそれぞれ独立したリクエスト本文です。4つをまとめて1個のJSONとして送信しません。

- POST /api/min-only/orders は @Minだけの比較用Controllerです。
- POST /api/required/orders は数量を必須にした比較用Controllerです。
- 両方が同じOrderService.acceptへOrderCommandを渡します。

200はこのサンプルでDTOをechoできたという結果です。Serviceは値を返すだけで、注文登録・保存・計算・nullを0にする補正は実装していません。nullを受け取っても例外を出さない観測境界なので、検証前後の違いだけを確認できます。

## JSONを読めることと入力制約は別

Jackson単体の2件では、項目省略と明示nullが両DTOのInteger quantityへnullとして読まれることをassertします。readValueだけで@NotNullのBean Validationは実行しません。

HTTP8件では両endpointに@Valid @RequestBodyを付けます。Minはnullを有効として扱うため、省略/nullはMinだけでは拒否されず、ControllerからServiceへnullが届きます。数量が必須の仕様なら@NotNullでnullを拒否し、0はMinで下限違反として拒否します。

拒否時はMethodArgumentNotValidException、quantityのfieldエラーcodeとrejectedValue、バインドされたDTOのquantityを確認します。Serviceへ到達していないことも確認します。ここでfield/codeはサーバ側の例外内BindingResultを観測した値です。クライアント向けエラーJSONやAdviceは実装していません。読めないJSONや数値への型変換失敗とは別であり、このmoduleへそれらの再現は追加していません。

## Service到達の観測

製品側のOrderServiceはOrderCommandを返す小さな実装です。テストだけが@PrimaryのRecordingServiceで差し替え、acceptの引数を記録した後に実OrderServiceへdelegateします。Mockitoや製品側のカウンタ、DBなどは追加しません。各ケース開始前に記録をリセットします。

## 旧配布との分担

旧固定d81b3a96433d8ea1b7996566cb48a87d26fe2e00の01-validation-basicsは6ファイル/17ケースで、Group検証9・HTML3・400分類4・正常リクエスト1を含みます。旧ControllerはServiceを持たず、読み取ったOrderRequestを返します。旧テストのquantity=0はMinのfieldエラーを確認し、正常quantity=2を確認していますが、省略/null/1やService到達は検証していません。

旧記事にはすでにMinがnullを通す注意があります。このサンプルはその注意を省略/null/0/1の実行結果へ接続する補強であり、旧17件がすでにService到達を測ったと扱いません。旧6ファイルと配布ZIPは変更しません。

## 環境と範囲

Java17.0.8 / Maven3.9.16 / Spring Boot3.5.16 / JUnit5.12.2、既存依存キャッシュでオフライン実行。新moduleはBootのvalidation starterとHibernate Validator8.0.3.Finalを使用します。旧配布のHibernate Validator8.0.1.Final固定設定は変更しません。新module内の両DTOは同じprovider/設定で比較します。

空の依存キャッシュからの初回取得、別JDK/provider、実TCP HTTP、DB/外部APIは未確認です。収録は起動クラス、Controllerと2DTO、共通Command/Service、テスト、POM、README、MIT LICENSEです。
