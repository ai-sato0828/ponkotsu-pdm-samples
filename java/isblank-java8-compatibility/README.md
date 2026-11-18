# Java 8で空白だけの入力を判定するサンプル

Java 8を対象にすると `String.isBlank()` の呼び出しがコンパイルで止まることと、Java 8で使えるAPIによる空白判定を確認します。依存ライブラリは不要です。

JDK 17の `javac` と `java` を使います。ZIPを展開した場合は、展開後の `java8-blank-sample` に移動してから実行してください。

## Java 8向けにコンパイルする

```sh
mkdir -p build/java8 build/rejected build/java11
javac --release 8 -d build/java8 \
  src/main/java/example/BlankInput8.java \
  src/test/java/example/BlankInputCheck.java
javac -J-Duser.language=en -J-Duser.country=US --release 8 \
  -cp build/java8 -d build/rejected \
  src/jdk11/java/example/BlankInput11.java
```

1つ目のコンパイルは成功します。2つ目では `error: cannot find symbol` と `symbol:   method isBlank()` が表示されます。

## Java 8向けの判定と isBlank() を比べる

```sh
java -cp build/java8 example.BlankInputCheck
javac --release 11 -cp build/java8 -d build/java11 \
  src/jdk11/java/example/BlankInput11.java
java -cp build/java8:build/java11 example.BlankInput11
```

クラスパスの区切りは、macOS・Linuxでは `:`、Windowsでは `;` です。Java 8向けのクラスだけで実行すると、`String.isBlank` 列は `-` になります。Java 11向けのクラスも使うと、同じ入力で3つの結果を比較できます。

```text
input | Java 8-compatible | String.isBlank | trim().isEmpty
null | true | true | true
empty | true | true | true
ASCII spaces | true | true | true
tab and newline | true | true | true
full-width U+3000 | true | true | false
NBSP U+00A0 | false | false | false
ordinary text | false | false | false
Verified 7 cases.
```

このサンプルでは `null` も未入力として扱います。3つとも先に `null` を判定しているため、`null` の行では文字列のメソッドを呼びません。全角空白（U+3000）を1文字だけ含む入力は、`trim().isEmpty()` では未入力になりません。

JDK 17.0.8で、Java 8向けのコンパイルと表の7ケースを確認しました。
