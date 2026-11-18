# CsvSourceの空欄でint引数へ変換できないとき

JDK 17、JUnit Jupiter 5.12.2、Maven 3.9系の例です。ZIPを展開して `pom.xml` がある場所で実行します。初回は依存関係の取得にネット接続が必要です。

注文数量が未入力なら0として扱う、という仕様を、同じCSVの2行で確認します。

| ケース | CSV | 受け取りたい数量 | 期待値 |
| --- | --- | --- | ---: |
| Q01 | `Q01,3,3` | `3` | 3 |
| Q02 | `Q02,,0` | `null`（引用符のない空欄） | 0 |

まず `int` を使う再現用クラスを明示して実行します。このコマンドは意図したエラーで終了します。

```sh
mvn -Dtest=CsvSourcePrimitiveProbe test
```

Q01は `ENTER Q01 quantity=3` を出します。Q02ではテストの本体に入る前に変換できず、`ENTER Q02` が出ません。ログと `target/surefire-reports/TEST-example.csvsource.CsvSourcePrimitiveProbe.xml` では、メソッド名の末尾 `[2]` が2回目の呼び出し、`parameter at index 1` が0始まりで2番目の引数 `quantity` を指します。2回のうちエラーは1件です。例外の型は `ParameterResolutionException`、その原因は `ArgumentConversionException` です。

数量が未入力でも扱う仕様なので、テストの数量引数を `Integer` に変えて `null` を渡せるようにします。入力のCSVを保ったまま修正側を実行します。

```sh
mvn clean test
```

今度は `ENTER Q01 quantity=3` と `ENTER Q02 quantity=null` が出ます。どちらもテストの本体まで進み、Q02は `QuantityRule.normalize(null)` の結果が0であることを確認します。通常のテスト実行では、クラス名が `Probe` の故障再現用クラスは実行されません。

## ファイルの役割

- `QuantityRule.java` は未入力の数量を0へ変換する処理です。
- `QuantityCases.java` は両方のテストが使う同じ2行です。
- `CsvSourcePrimitiveProbe.java` は `int` で受け取る再現用です。
- `CsvSourceNullableTest.java` は `Integer` で受け取り、同じ期待値を検証します。

[JUnit 5.12.2公式ガイドのCsvSource](https://docs.junit.org/5.12.2/user-guide/index.html#writing-tests-parameterized-tests-sources-CsvSource)
