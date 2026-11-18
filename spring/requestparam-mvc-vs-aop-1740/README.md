# RequestParamの検証がMVCとAOPで異なる例外になる

JDK17、Maven3.9系でpom.xmlのある場所から実行します。

```sh
mvn clean test
```

Boot3.5.16、Spring Framework6.2.19、Hibernate Validator8.0.3.Final、JUnit5.12.2、JDK17.0.8/Maven3.9.16の既存cacheを使って確認します。初回の空cache依存取得は未確認です。MockMvcのMOCK環境で実行し、実HTTPサーバー・DB・外部サービスは起動しません。

## 同じ入力とController処理で比較する

`ItemsController`と`ValidatedItemsController`は、同じ`GET /items`、同じ`@RequestParam("keyword") @NotBlank String keyword`、同じJSON返却を持ちます。意味上の差はクラスの`@Validated`です。クラス名を分けるのは2つの例を配布するためです。テストは2つのBootコンテキストにControllerを1つずつ明示Importし、同じパスが競合しないようにします。推奨アプリにquery切替を追加する例ではありません。

`InputValidationAdvice`は入力の`HandlerMethodValidationException`だけを400/`{"code":"INVALID_PARAMETER"}`へ変換します。返却値の検証エラーは再送出し、無差別に400へ変えません。`ConstraintViolationException`や`Exception`全体を捕まえるhandlerは追加しません。

| 経路 | keyword | MockMvcでの結果 |
|---|---|---|
| class @Validatedなし | 半角空白3個 | MVCがHandlerMethodValidationException、Adviceが400へ変換 |
| class @Validatedなし | java | 200、keyword=java |
| class @Validatedあり | 半角空白3個 | default method validation AOPがConstraintViolationException、狭いAdviceでは扱わずServletExceptionに包まれて送出 |
| class @Validatedあり | java | 200、keyword=java |

JUnit4件は上の4ケースを検証します。空白の検証では、実効beanがAOP proxyかもassertします。MVC側はresolvedExceptionの型と入力値・400本文、AOP側は送出の最深例外型・違反数1・NotBlank制約・入力値を確認します。

```text
AOP keyword=java status=200 bodyKeyword=java
AOP keyword=spaces outcome=ServletException root=ConstraintViolationException httpStatus=not-observed
MVC keyword=java status=200 bodyKeyword=java
MVC keyword=spaces status=400 exception=HandlerMethodValidationException code=INVALID_PARAMETER
```

上の4行はログの該当行を比較用に順を揃えたものです。テストの実行順は保証しません。AOPケースはMockMvcが例外を投げたことを確認したもので、実HTTP500応答を測定していません。MockMvcの途中レスポンス値を実際の応答statusとして解釈しません。

## 直す場所を限定する

この採用版のControllerでMVC組込のメソッド検証を使うなら、`@NotBlank`は維持し、クラスの`@Validated`を外して`ItemsController`と同じ構成にします。すると狭いMVC Adviceが扱える400の経路になります。`@Validated`が常に不要という意味ではなく、Serviceのメソッド検証や別の設計まで無条件に外しません。入力制約を削除して200にする修正でもありません。

この比較はBootの既定のmethod validation構成です。MethodValidationPostProcessorの設定や別の例外ハンドラーを持つアプリでは、実効proxyと例外型を先に確認します。request bodyの検証、返却値エラー、Spring Security、独自の広いAdvice、実Servletコンテナのエラーレスポンスは試験対象外です。POMと対象・Testの全文を配布し、追加依存JAR・故障コピー・target・ログはZIPに含めません。MIT LICENSE付き。
