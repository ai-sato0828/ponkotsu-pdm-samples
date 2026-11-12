# String.splitとピリオドを同じ入力で比べる

Java 17以上・Maven 3.9系。外部DB、Webサーバー、Springは不要です。
JUnit 5.12.2、Maven Compiler Plugin 3.14.0、Surefire 3.5.3を固定しています。

## 実行

ZIPを展開したら、`pom.xml`がある場所で実行します。

```sh
mvn clean test
java -cp target/classes info.syachiku.split.Quiz
java -cp target/classes info.syachiku.split.SplitDemo
```

JUnitは **27件**。S01〜S11が通常テスト11件、S12が10種類の入力、S13が6種類の区切り文字です。
実装ではなく、期待する全配列を`assertArrayEquals`で確認します。
`Quiz`は元の2行で`0`を表示し、`SplitDemo`は誤った正規表現・文字のピリオド・Pattern.quoteを比較します。

初回はMaven本体、プラグインとJUnitの取得にネット接続が必要です。
作成環境ではJDK 17.0.8 / Maven 3.9.16、既存依存キャッシュを使ったオフライン実行で確認しました。
空キャッシュからの初回取得、別JDK、Windowsでは未実行です。コマンドはmacOS/Linuxの例です。

## 収録ファイル

- `src/main/java/info/syachiku/split/Quiz.java`: 元のクイズの2行を実行する入口。
- `src/main/java/info/syachiku/split/SplitFields.java`: 意図的な誤例、ピリオドのリテラル分割、変数の区切り文字、出力表示。
- `src/main/java/info/syachiku/split/SplitDemo.java`: 同じ入力を比較する実行入口。
- `src/test/java/info/syachiku/split/SplitFieldsTest.java`: 27件の期待配列・例外を検証。
- `pom.xml`: Java17とJUnit、ビルド用プラグインの固定版。

`wrongDot`は比較専用です。文字のピリオドを区切る実装として業務へ採用しないでください。

## 何を確認するか

| テスト | 入力・条件 | 期待する結果 |
| --- | --- | --- |
| S01 | `a.b`を`.`、limit=0で分割 | 要素数0、`[]` |
| S02 | 同じ誤った`.`、limit=-1 | `['', '', '', '']`に相当する4個の空文字 |
| S03 | `a.b`を`\\.`で分割 | `["a", "b"]` |
| S04〜S06 | limit=2/1/10 | 最後の要素へ残りを格納／分割なし／末尾空を保持 |
| S07 | 空配列と空文字1個を表示 | `length=0 values=[]`と`length=1 values=[""]`を区別 |
| S08〜S10 | null入力・null区切り・空区切り | 前2つはNullPointerException、空区切りは本ヘルパーの設定エラー |
| S11 | `A+B`を直接`+`で分割 | PatternSyntaxException。quoteした修正版なら`["A", "B"]` |
| S12 | 下表の10入力 | escapedとPattern.quoteの両方式で全配列が一致 |
| S13 | `|`、`+`、`(`、`)`、`::`、`\E` | 区切り全体を文字として扱い、末尾空の保持/削除も一致 |

S12はそれぞれlimit=0（第2引数省略時と同じ）とlimit=-1で確認します。

| 入力 | limit=0 | limit=-1 |
| --- | --- | --- |
| `A.B` | `["A", "B"]` | `["A", "B"]` |
| `A.B.` | `["A", "B"]` | `["A", "B", ""]` |
| `.A` | `["", "A"]` | `["", "A"]` |
| `A..B` | `["A", "", "B"]` | `["A", "", "B"]` |
| `A` | `["A"]` | `["A"]` |
| 空文字`""` | `[""]` | `[""]` |
| `.` | `[]` | `["", ""]` |
| `A..` | `["A"]` | `["A", "", ""]` |
| `..A` | `["", "", "A"]` | `["", "", "A"]` |
| `...` | `[]` | `["", "", "", ""]` |

`Arrays.toString(new String[0])`も`Arrays.toString(new String[]{""})`も見た目は`[]`です。
このサンプルでは`describe`が各要素を引用符で囲み、要素数も付けて区別します。

## 入力契約と対象範囲

- `literalDot`の`limit`はString.splitへそのまま渡します。0/-1だけを許可するAPIではありません。
- `literalDelimiter`は固定の文字列を区切り文字として受け取ります。正規表現を入力する機能の例ではありません。
- null入力・null区切りは、空文字へ置換せず拒否します。
- 空の区切り文字は、このヘルパーでは設定ミスとしてIllegalArgumentExceptionにします。**String.splitが空の正規表現を禁止するという意味ではありません。**
- `.`が通常の設定で一致しない行終端文字などは本例の入力に含めません。記事の`a.b`の3文字が全て区切りになる話を、任意の全入力へ一般化しないでください。
- CSVの引用符内カンマ・改行は対象外です。CSVをこのヘルパーで解析しないでください。

## テストが誤変更を検出するか試す

検証用のコピーでだけ試し、最後は復元します。

1. `SplitFields.literalDot`の`split("\\.", limit)`を`split(".", limit)`へ変える。
2. `mvn test '-Dtest=SplitFieldsTest#S03*'`を実行する。`a,b`が取れず配列の期待値で失敗する。
3. 戻してから、同じ箇所を第2引数なしの`split("\\.")`へ変える。
4. `mvn test '-Dtest=SplitFieldsTest#S12*'`を実行する。`A.B.`等の末尾空が消え、全配列比較で失敗する。
5. `split("\\.", limit)`へ戻し、`mvn clean test`で27件成功を確認する。

## 一次仕様

- [JDK17 String.split(regex, limit)](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html#split(java.lang.String,int))
- [JDK17 Pattern.quote](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/regex/Pattern.html#quote(java.lang.String))

このコードは記事の実行用サンプルです。記事のX画像は元のクイズを示し、全文のファイル名と抜粋範囲は本文に記載します。
