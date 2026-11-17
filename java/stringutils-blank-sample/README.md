# JavaのStringUtils：未入力の条件からAPIを選ぶ

Java 17、Maven 3.9系で、展開したこのフォルダーから実行します。

```sh
mvn clean test
mvn -q exec:java
```

Apache Commons Lang 3.22.0を固定しています。JUnit 5.12.2とSpring Core 6.2.19は比較テスト専用のtest scopeです。MainやInputPolicyを動かすためにSpringを組み込む必要はありません。初回はMaven Centralから依存を取得するためネットワークが必要です。

## 要件と実装

このサンプルはWebサーバーやDBを起動せず、2つの入力仕様だけを比較します。

- 表示名：null・空文字・Javaの空白文字だけの値を「未入力」と分類する。`InputPolicy.isDisplayNameMissing`はこの分類だけを担当し、文字種・文字数まで検証する最終的な入力許可判定ではない。
- 備考：null・空文字だけを`(未設定)`に替える。空白だけでも、入力されている文字列は削らずそのまま残す。

既にCommons Langを使うプロジェクトを想定した実装が`InputPolicy`です。ライブラリを増やさない別案が`JdkOnlyInputPolicy`で、このクラスはJava標準だけでコンパイルできます。両方を同時に使う必要はなく、比較のため同じZIPへ入れています。

## 7入力の期待値

| CASE | 入力 | Apache isEmpty | Apache isBlank | 表示名が未入力 | 備考の結果 |
|---|---|---|---|---|---|
| NULL | null | true | true | true | `(未設定)` |
| EMPTY | `""` | true | true | true | `(未設定)` |
| HALF_SPACE | `" "` | false | true | true | 半角空白を保持 |
| FULL_SPACE | `"\u3000"` | false | true | true | 全角空白を保持 |
| NBSP | `"\u00A0"` | false | false | false | NBSPを保持 |
| TAB_LF | `"\t\n"` | false | true | true | タブ・改行を保持 |
| TEXT | `"Alice"` | false | false | false | `Alice` |

NBSPはこのAPIのblank判定ではtrueになりません。画面上で空白に見える文字をすべて拒否する仕様を満たすとは限らず、NBSPも拒否したい場合は要件と判定の追加が必要です。

Mainは7行を表示します。`JDK_EMPTY`と`JDK_BLANK`のnull時の`NPE`は比較表示のラベルで、Main自体は落ちません。`BlankSelectionTest`では実際にnullへ両メソッドを呼び、NullPointerExceptionを検証します。`MEMO`欄の空白はUnicodeエスケープ等で表示し、消したか残したかを見える形にしています。

## テスト43件の内訳

7入力を6つの観点で検証する42件と、Springの公開API確認1件です。

1. ApacheのisEmpty/isBlank：7件。
2. 標準StringのisEmpty/isBlankとnull時の例外：7件。
3. 表示名の未入力分類：7件。
4. 備考の既定値と入力値保持：7件。
5. JDKだけで書いた別案が同じ要件を満たす：7件。
6. SpringのhasText/hasLengthが逆向きの意味であること：7件。
7. 固定したSpring Core 6.2.19のStringUtilsにisBlankという公開メソッドがないこと：1件。

Springの同名クラスは`org.springframework.util.StringUtils`、この記事の主題は`org.apache.commons.lang3.StringUtils`です。名前だけでimportを補完しないでください。Springでは`hasText`は空白以外の文字があればtrue、`hasLength`は長さがあればtrueです。

## 収録ファイル

- `src/main/java/info/syachiku/samples/stringutils/InputPolicy.java`：Commonsを使う2つの要件。
- `src/main/java/info/syachiku/samples/stringutils/JdkOnlyInputPolicy.java`：Java標準だけの別案。
- `src/main/java/info/syachiku/samples/stringutils/Main.java`：7入力の比較表示。
- `src/test/java/info/syachiku/samples/stringutils/BlankSelectionTest.java`：期待値を固定した43件。
- `pom.xml`、`README.md`、`LICENSE`。

依存JAR、コンパイル済みclass、個人設定はZIPに含めません。

[ブログ記事（公開後）](https://syachiku-uz.info/java-stringutils-isblank-isempty-difference/)
