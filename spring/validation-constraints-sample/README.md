# @NotNull・@NotEmpty・@NotBlankを同じPOSTで比べる

JDK 17、Maven 3.9系。Spring Boot 3.5.16／Hibernate Validator **8.0.3.Final**／JUnit 5.12.2に固定したサンプルです。DB、認証、外部API、画面テンプレートは使いません。

## 試す仕様

同じJSON項目 `name` を4つのRESTエンドポイントへ送ります。

| POST先 | nameの制約 |
|---|---|
| `/validation/not-null` | `@NotNull` |
| `/validation/not-empty` | `@NotEmpty` |
| `/validation/not-blank` | `@NotBlank` |
| `/validation/required-name` | この例の氏名仕様を表した独自の `@RequiredName` |

入力と期待値は `src/test/resources/input-cases.json` にあります。欠落、null、空文字、半角空白、全角空白、NBSP、文字ありの7入力です。標準3制約の比較21ケースと、修正後7ケースを同じ仕様表から検証します。400では `errors[0].field == "name"` と制約名も確認し、200では入力値を変えずに返したことを確認します。

この固定版の`@NotBlank`では、全角空白(U+3000)とNBSP(U+00A0)だけの入力も200になります。すべてのProvider版で同じとは扱わないでください。独自制約の業務仕様は「null、空文字、Javaの`Character.isWhitespace`または`Character.isSpaceChar`がtrueの文字だけなら拒否」です。値をtrimして上書きする処理ではありません。

## テストと起動

```sh
mvn clean test
mvn package
java -jar target/validation-constraints-sample-1.0.0.jar --server.address=127.0.0.1 --server.port=18087
```

テストは28比較ケース＋Provider版の確認＋前後・途中の空白を保持する確認の計30件です。初回はMaven依存を取得するネットワークが必要です。起動後、別の端末から次を送ると、`@NotEmpty`は半角空白だけの値を200で受け付けます。

```sh
curl -i -X POST http://127.0.0.1:18087/validation/not-empty \
  -H 'Content-Type: application/json' --data '{"name":" "}'
```

同じ入力のままURLを`/validation/not-blank`へ変えると400、`/validation/required-name`でも400です。全角空白とNBSPは`input-cases.json`にUnicodeエスケープで区別してあります。

Python 3を使える場合、起動済みJARへ28ケース全部を実POSTして照合できます。

```sh
python3 scripts/verify-http.py http://127.0.0.1:18087
```

スクリプトは標準ライブラリだけを使用し、送信先を`127.0.0.1`に限定しています。終了するときはJARを起動した端末でCtrl+Cを押します。

## 仕様を弱めた変更を捕まえる

作業用コピーで、`RequiredNameValidator.isValid`の最後のreturnを`return !value.isEmpty();`へ変え、`mvn test`を実行します。半角空白・全角空白・NBSPで期待400に対して200となり、3ケースが失敗します。元の`codePoints().anyMatch(...)`へ戻して30件が通ることを確認します。配布版は修正済みの実装です。

## 範囲

`@Valid @RequestBody`を使ったREST APIの例です。入力エラーを画面へ表示するMVCの`BindingResult`と、再表示のHTTP 200を同じものとして扱いません。HTMLフォームの空欄・nullへの変換、DB保存、認証、独自のJackson設定、長さ上限や禁止文字は別の仕様です。
