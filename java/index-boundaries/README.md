# Javaの添字とページ番号の境界

[IndexOutOfBoundsExceptionの記事](https://syachiku-uz.info/java-indexoutofboundsexception-causes/)の実行サンプルです。リストと配列の添字、1始まりのページ番号の変換をJDK 17とJUnit 5.12.2で確かめます。

JDK 17とMaven 3.9系を用意し、pom.xmlのある場所で実行します。

```sh
mvn test
```

- `PageIndex.java` はページ番号を1以上と確認してから1を引きます。例えば1は0に、2は1に変換します。
- `IndexBoundariesTest.java` は空List・1件・3件・配列の4件と、変換の正常・異常各3件を実行します。ページ番号の検証を減算の後に移すと `Integer.MIN_VALUE` の1件が失敗します。
- 変換は整数が1以上かを検証します。実際の最終ページやページサイズは、このサンプルの外で決まります。DBやSpring Bootの起動は不要です。
- 記事の検索RepositoryやCSVパーサーの例は説明用の抜粋です。このZIPには掲載したページ番号変換と境界値テストの全文、実行用POMが入っています。
