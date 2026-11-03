# 登録フォームの仕様と期待結果

- `name`は空欄・空白だけを拒否し、最大50文字（この例の `@Size` はJavaの文字列長）。
- `email`は任意。値があるときメール形式を検証する。比較では `test@example.com` を有効な値として使用する。
- `GET /users/new` は空の `userForm` をモデルへ渡し、`users/input` を200で返す。
- `POST /users` で入力エラーがある場合、同じForm・`BindingResult`を用いて `users/input` を200で返す。対象項目に対応する日本語文言と、再入力に必要な値をHTMLへ表示する。`UserService.create` は呼ばない。
- 正常なPOSTは `UserService.create` を一度呼び、`/users` へ302でリダイレクトする。`GET /users` はメモリにある登録内容を一覧表示するだけで追加登録しない。

| ケース | 固定入力・条件 | 期待結果 | UserFormHtmlTestのメソッド |
| --- | --- | --- | --- |
| V01 | 初期GET | 200、名前欄空、エラー要素なし、登録0 | `initialGetDisplaysFormWithoutErrors` |
| V02 | 名前空文字、メール正常 | 名前エラー1件、日本語文言とメール保持、登録0 | `blankNameShowsJapaneseMessageAndKeepsEmailWithoutSaving` |
| V03 | 名前半角空白3つ、メール正常 | 名前必須エラー、登録0 | `whitespaceOnlyNameAlsoFailsNotBlank` |
| V04 | 名前aを51文字、メール正常 | 名前の長さエラー、必須エラーなし、登録0 | `tooLongNameShowsLengthMessageInsteadOfRequiredMessage` |
| V05 | 名前「山田太郎」、メール`invalid` | メールエラー1件、名前保持、登録0 | `invalidEmailShowsItsOwnErrorAndKeepsName` |
| V06 | 名前「山田太郎」、メール正常 | 302、Service呼出し1回、登録1件、GETで増えない | `validPostCallsServiceOnceAndRedirectsToList` |
| V07 | V02と同じPOSTを故障・修正テンプレートへ | 両方名前エラー1件、故障は文言なし、修正はあり、両方メール保持、登録0 | `brokenAndFixedTemplatesHaveSameFieldErrorButDifferentHtml` |
| V08 | V02のエラーをテスト専用Controllerでリダイレクト | POSTに名前エラー1件、302の次GETは文言なし・メール空欄、登録0 | `redirectWithoutFlashLosesFieldErrorAndInputOnNextGet` |
