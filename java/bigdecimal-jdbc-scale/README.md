# BigDecimalをJDBCで読み取ったときのscale

入力の `1200.0` と、H2の `DECIMAL(12, 2)` 列から読み取った `1200.00` を使い、SQLの数値比較とJavaの `equals`／`HashMap` 検索を比べます。

## 実行する

JDK 17とMaven 3.9系を用意し、`pom.xml` のあるフォルダーで実行します。初回はMavenが依存ライブラリを取得します。H2 2.3.232のメモリーDBを接続ごとに作り、接続を閉じると消えます。

```sh
mvn clean test
mvn -q compile exec:java
```

JUnit Jupiter 5.12.2で5件、実行例で次の6行を確認できます。

```text
SQL matched 1 row: R001
input=1200.0 (scale 1)
stored=1200.00 (scale 2)
equals=false, compareTo=0
HashMap raw=null
HashMap normalized=R001
```

## どこで結果が分かれるか

1. `R001` に `1200.0`、`R002` に `1300.00` を `PreparedStatement#setBigDecimal` で保存します。
2. `WHERE amount = ?` に `1200.0` を指定します。`R001` が1件見つかり、`ResultSet#getBigDecimal` は `1200.00` を返します。
3. `equals` はscaleも比較するので `false`、`compareTo` は `0` です。`HashMap` に取得値をキーとして入れ、入力値のまま探すと `null` になります。
4. 金額キーを小数2桁に揃える仕様なら、登録と検索の両方で `setScale(2, RoundingMode.UNNECESSARY)` を使います。`1200.000` は数値を変えずに2桁へ揃い、`1200.001` はDBへ渡す前の確認で `ArithmeticException` になります。

`1200.00` とscale 2は、指定版のH2とこの列定義で得た値です。運用先では列やSQL式の型と、使っているJDBCドライバーで確認します。金額を注文の識別子にする用途は想定していません。

- `ReceiptAmountDemo.java` にDDL・登録・検索と、同じ値を使う比較があります。
- `ReceiptAmountDemoTest.java` はSQLでの取得、未正規化キーの検索失敗、正規化したキーの検索成功、末尾の0と丸めが必要な入力を確認します。

[H2のNUMERIC／DECIMAL](https://h2database.com/html/datatypes.html#numeric) · [PreparedStatement#setBigDecimal](https://docs.oracle.com/en/java/javase/17/docs/api/java.sql/java/sql/PreparedStatement.html#setBigDecimal(int,java.math.BigDecimal)) · [ResultSet#getBigDecimal](https://docs.oracle.com/en/java/javase/17/docs/api/java.sql/java/sql/ResultSet.html#getBigDecimal(java.lang.String))
