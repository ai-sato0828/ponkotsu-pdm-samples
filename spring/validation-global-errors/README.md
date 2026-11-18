# BindingResultのグローバルエラーをThymeleafに表示する

JDK17とMaven3.9系を使い、pom.xmlのあるフォルダーで実行します。

```sh
mvn clean test
```

Java17.0.8、Spring Boot3.5.16、Spring Framework6.2.19、Thymeleaf3.1.5.RELEASE、Hibernate Validator8.0.3.Final、JUnit5.12.2で確認します。今回の実行はMaven3.9.16と既存依存キャッシュの`-o`を使います。初回の空キャッシュからのダウンロードは未確認です。

## 何を比較するか

`PeriodForm`はstartDate/endDateをLocalDateとして受け、@NotNullと@DateTimeFormat(iso=ISO.DATE)を指定します。`POST /periods`で両方の個別入力が正常でも、開始日が終了日より後なら`PeriodController`の`BindingResult.reject("period.order", "開始日は終了日以前にしてください")`でグローバルエラーを1件追加します。

`rejectValue("startDate", ...)`ではなく`reject(...)`を使うため、field errorは0件です。`th:errors="*{startDate}"`と`*{endDate}`だけではこのメッセージを表示しません。本番テンプレートsrc/main/resources/templates/periods/input.htmlには、同じth:objectを持つform内に以下を置きます。

```html
<p id="global-error" th:if="${#fields.hasGlobalErrors()}" th:errors="*{global}"></p>
```

## 4件のテスト

| テスト | 入力 | 期待値 |
|---|---|---|
| fieldOnlyMarkupMissesGlobalErrorButFixedMarkupShowsIt | start=2026-11-20 / end=2026-11-19 | HTTP200、hasErrors=true、field=0/global=1、period.order、固定HTMLに本文あり、両入力値保持 |
| missingStartIsFieldErrorAndSkipsCrossFieldComparison | start空 / end=2026-11-19 | HTTP200、field=1/global=0、startDateのNotNull本文を表示、期間比較は行わない |
| validPeriodRedirectsWithoutErrors | 2026-11-19〜2026-11-20 | HTTP302、/periods/completeへリダイレクト |
| sameDayIsValid | 2026-11-19〜2026-11-19 | 同日を許容しHTTP302 |

1件目は固定テンプレートへ返る実際のMockMvc POSTに加え、その同じModel/BindingResultをテスト専用src/test/resources/templates/periods/field-only.htmlへThymeleafで再描画します。比較テンプレートの差はglobal表示の1行だけです。比較側ではメッセージがなく、固定側では表示されること、どちらも日付の値が残ることをassertします。比較用のHTTPルートやクエリ切替は製品コードへ追加していません。Beforeを別のHTTPリクエストとして測定しているわけではありません。

テストは次の4行を出力します（実行順は固定しません）。

```text
GLOBAL status=200 hasErrors=true field=0 global=1 beforeText=false afterText=true retained=true
FIELD status=200 field=1 global=0 startMessage=true globalText=false
VALID status=302 location=/periods/complete saved=false
SAME_DAY status=302 location=/periods/complete saved=false
```

## 範囲と注意

MockMvcとThymeleaf実描画を使い、外部HTTPサーバー・DB・外部サービスは起動しません。テスト環境はwebEnvironment=MOCKです。正常パスは遷移だけを示し保存処理はありません。@NotNullの項目エラーがあれば相関比較をせず、nullを比較して例外になることも避けます。日付の型変換エラー・Spring Security・複数言語メッセージ・クラスレベル独自制約は今回の検証範囲外です。既存validation-message-displayの8件やmessage-key追試とは別サンプルで、合算しません。

故障確認として、本番input.htmlのglobal表示1行を除くと1件目のafterText検証が失敗します。元へ戻すと4件成功します。コードや期待値を故障版へ合わせて変更しないでください。
