# JavaのisEmptyとisBlankで入力の違いを確認する

JDK 17とMaven 3.9系を使います。展開先の `pom.xml` があるディレクトリから、次を実行します。

```sh
java src/main/java/BlankStringDemo.java
mvn test
```

1行目のコマンドはnull、空文字、空白、文字あり、NBSPを含む8行の判定結果を表示します。`NPE` は直接呼んだときに `NullPointerException` になることを示す表示で、実際の例外はJUnitで検証します。`U+3000` の `isBlank` と `isMissing` は `true`、`U+00A0` はどちらも `false` です。

2行目のコマンドは11件のJUnitテストを実行します。内訳は未入力6件、文字ありとNBSPの各1件、nullへの直接呼び出し1件、`trim` と `strip` の比較2件です。

`BlankStringDemo.isMissing` の `value.isBlank()` を `value.isEmpty()` に置き換えると、半角空白・全角空白・タブ・改行の4件が失敗します。元に戻して再度実行できます。

Hibernate Validatorはこの記事の補足で扱い、このサンプルの依存関係には含めていません。
