# Booleanを返す三項演算子：未設定を保つ選択

Java17、Maven3.9系、JUnit5.12.2。展開後、pom.xmlがあるフォルダーで実行します。アプリ本体の外部ライブラリ、Spring、DB、JSON、HTTPサーバーは不要です。

```sh
mvn test
mvn -q exec:java
```

初回はMavenプラグイン/JUnit取得のネットワークが必要です。検証はJava17.0.8/Maven3.9.16、既存キャッシュでのoffline実行です。空キャッシュからの全依存取得は未確認です。

## このサンプルの契約

既存設定を採用する(useExisting=true)場合は、configuredのtrue/false/nullをそのまま返す。採用しない場合はBoolean.FALSEを返す。nullは「未設定」でありfalseと同じ意味にしません。

`broken`は意図的な不具合例です。左辺をBooleanにしても、`useExisting ? configured : false`は三項演算子自体がboolean型となり、選ばれたconfiguredがnullならアンボクシング時にNPEです。`preserveNullable`では両枝をBooleanにし、第三状態を保ちます。

| useExisting | configured | broken | preserveNullable |
|---|---|---|---|
| true | null | NullPointerException | null |
| true | true | true | true |
| true | false | false | false |
| false | null | false | false |
| false | true | false | false |
| false | false | false | false |

useExisting=false/configured=nullでは、nullの枝が選ばれず、brokenもNPEになりません。二つの枝を常に評価するわけではない点も比較できます。条件useExisting自体はprimitive booleanなので、条件式がnullの場合をこの例へ混ぜていません。

Mainは上表の6行と、後続ifの境界1行を出します。意図したNPEは捕捉して比較用の名前を表示し、Main全体は正常終了します。

## nullを保てても後のifは別の変換

`preserveNullable(true, null)`はnullを返すため、その結果をそのまま`if (selected)`へ入れると依然NPEです。この修正は全てのNPEを消すことではなく、選択処理の「未設定を保つ」仕様を満たすものです。

`Boolean.TRUE.equals(selected)`で二値化するとnullとfalseは両方falseになります。trueの場合だけ実行する仕様なら選べますが、nullを未設定として引き渡す仕様の代替ではありません。

## JUnit10ケース

- useExisting true/false × configured null/true/false の6ケース。故障のNPE/値と修正後の値を同じ入力で照合。
- 修正後のnullを後からifへ使うとNPEになる1ケース。
- null/true/falseをBoolean.TRUE.equalsで二値化する別契約3ケース。

既存記事の同意登録6テスト版とJSON PATCH38ケース版とは別moduleです。それらのコード・配布リンク・期待値を置き換えません。

## 収録範囲

TernarySelection.java（故障/修正）、Main.java、TernarySelectionTest.java、pom.xml、README、LICENSE。依存JAR、class、個人設定はZIPに含めません。

仕様の根拠：
- JLS17 §15.25.1 Boolean Conditional Expressions：両枝がBooleanならBoolean、それ以外のboolean条件式はboolean。
- JLS17 §15.25：選ばれない枝は評価しない。
- JLS17 §5.1.8：null参照をアンボクシングするとNullPointerException。

https://docs.oracle.com/javase/specs/jls/se17/html/jls-15.html#jls-15.25.1
https://docs.oracle.com/javase/specs/jls/se17/html/jls-5.html#jls-5.1.8
