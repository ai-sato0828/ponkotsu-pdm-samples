# Mavenでソース・ターゲットの5が拒否される条件を比べる

JDKを変えずに、Maven Compiler Plugin 3.1の既定設定で起きるコンパイルエラーと、Java 8向けに設定した結果を比べます。

## 準備

JDK 17とMaven 3.9系を用意してください。確認に使ったのはJDK 17.0.8とMaven 3.9.16です。ZIPを展開し、`pom.xml` のあるディレクトリで次を実行します。`mvn -v` のJava versionが17であることを確認し、4条件で同じJDKを使います。

```sh
mvn -v
```

最初の2つのビルドは、エラーを確認するために個別に実行します。各コマンドの `clean` でこのサンプルの `target` を消してからコンパイルします。初回はMavenのプラグインと依存ライブラリの取得が必要です。必要な依存がすべてキャッシュ済みなら、各 `mvn` コマンドに `-o` を付けてオフラインで実行できます。

## 1. 旧プラグインの既定設定で失敗する

```sh
mvn clean compile
```

`pom.xml` に `<java-version>1.8</java-version>` があっても、このPOMにはその値をコンパイラに渡す設定がありません。Compiler Plugin 3.1の既定値はソース・ターゲットともに1.5です。JDK 17のログには、ソース・オプション5とターゲット・オプション5がサポートされていない旨が出ます。英語環境でもエラーに出るオプションの値は5です。

## 2. プラグイン3.1のままreleaseを指定しても失敗する

```sh
mvn -Dmaven.compiler.release=8 clean compile
```

3.1では `maven.compiler.release` をコンパイル設定として読み取らないため、1と同じエラーになります。

## 3. プラグイン3.1が読むsource・targetを明示する

```sh
mvn -Pexplicit-source-target clean compile
javap -verbose target/classes/example/App.class
```

このプロファイルは `maven.compiler.source` と `maven.compiler.target` をともに8に設定します。`BUILD SUCCESS` が出て、`javap` で `major version: 52` を確認できます。この設定でJava 8のクラスファイル形式を選べますが、より新しいJDKのAPIを誤って呼び出しても検出できるとは限りません。

## 4. 対応プラグインでreleaseを指定する

```sh
mvn -Prelease-8 clean compile
javap -verbose target/classes/example/App.class
java -cp target/classes example.App
```

このプロファイルはCompiler Plugin 3.14.0と `maven.compiler.release=8` を使います。`BUILD SUCCESS`、`release 8`、`major version: 52` を確認できます。JDK 17で起動すると `Compiled for Java 8` が表示されます。JDK 17の `--release 8` はJava 8の言語仕様、クラスファイル形式、公開APIを対象にします。このサンプルはJava 8実機での起動や実案件の依存ライブラリの互換性まで検証していません。

実案件でコンパイラの設定値を探す場合は、`mvn help:effective-pom` で親POMやプロファイル適用後の構成を確認し、ビルドログに出たCompiler Pluginの版と照合します。プロファイルを使う案件では、調査のときも同じプロファイルを指定します。

ZIPに含まれる `settings.xml` は空のMaven設定です。個人や組織のMaven設定から切り離して再現したい場合は、上記の `mvn` コマンドに `-gs settings.xml -s settings.xml` を追加できます。

- [Compiler Plugin 3.1のsourceとtarget](https://maven.apache.org/plugins-archives/maven-compiler-plugin-3.1/compile-mojo.html)
- [Compiler Plugin 3.14.0のrelease設定](https://maven.apache.org/plugins-archives/maven-compiler-plugin-3.14.0/examples/set-compiler-release.html)
- [Maven Help Pluginのeffective-pom](https://maven.apache.org/plugins/maven-help-plugin/effective-pom-mojo.html)
