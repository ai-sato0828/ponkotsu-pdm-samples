# Spring Bootの設定値を起動時に検証する

夜間のファイル連携で1回に処理する件数を `export.batch-size` に指定します。件数の必須条件は整数の1〜500です。サンプルは設定の読込と検証を行い、起動できたときに値を表示します。

## 実行

JDK 17とMaven 3.9系を使います。ZIPを展開し、`pom.xml` のあるディレクトリで実行します。

```sh
mvn package
java -jar target/export-config-validation-1.0.0.jar --export.batch-size=250
java -jar target/export-config-validation-1.0.0.jar
java -jar target/export-config-validation-1.0.0.jar --export.batch-size=0
java -jar target/export-config-validation-1.0.0.jar --export.batch-size=abc
```

最初のJavaコマンドは `EXPORT_READY batchSize=250` を表示して終了コード0です。続く3つのJavaコマンドは意図した失敗で、終了コード1です。実行用ディレクトリの設定ファイル、環境変数、JVMオプションでこの設定を別途渡している場合は、欠落の再現前に取り除きます。

- 指定なしでは `NotNull`、0では `Min` の検証で止まります。
- `abc` は整数へ変換できないため、制約を確かめる段階まで進みません。
- エラー診断の `Property:` には、制約違反で `export.batchSize`、型変換エラーで `export.batch-size` と表示される場合があります。入力キーは `export.batch-size` です。制約の文言はロケールにより変わります。

## テスト

```sh
mvn test
```

`ExportApplication.java` で登録する `ExportProperties.java` を、`ExportPropertiesTest.java` がSpringの設定読込経由で確かめます。1と500は受理し、欠落・0・501・`abc` は起動を拒みます。各入力で新しいSpringコンテキストを作り、起動失敗とその原因を調べます。

検出力を確かめる場合は、別の展開先で `ExportProperties.java` の `@Validated` を1行外し、もう一度テストします。欠落・0・501を拒む3件が失敗し、`abc` の変換失敗と境界2件は成功します。
