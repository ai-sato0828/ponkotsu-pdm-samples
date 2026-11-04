# 数量の変換と入力エラーをJUnitで検証する

半角数字と正しい3桁カンマ区切りを受け付ける数量欄を題材にしています。
JDK 17とMaven 3.9系を用意し、展開先の `pom.xml` があるディレクトリで実行します。

```sh
mvn test
```

- `QuantityParser.java` は必須、形式、intの範囲を分けて判定します。
- `QuantityParserTest.java` は0・最大値の受理と、未入力・形式違反・範囲外のエラーを合わせて23件確認します。
- 範囲外ではアプリが返すメッセージと、原因として保持した `NumberFormatException` を確かめます。JDK内部の例外メッセージには依存しません。
- 実装のtry-catchを外して `return Integer.parseInt(input.replace(",", ""));` へ戻すと、範囲外の3件が失敗します。
- この入力規則は整数の数量欄に限ります。通貨、負数、別ロケールの数値を対象とする場合はその項目の仕様を定めます。

[記事](https://syachiku-uz.info/java-numberformatexception-causes/)
