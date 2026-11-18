# LocalDate.parseで英語の月名を読む

JDK 17とMaven 3.9系で、ZIPを展開した先の `pom.xml` があるディレクトリーから実行します。初回は依存関係の取得にネット接続が必要です。

```sh
mvn clean test
java -cp target/classes example.date.MonthNameDateDemo
```

外部システムから受け取ったCSVの日付列に `18 Nov 2026` と入っている例です。`dd MMM uuuu` と `ResolverStyle.STRICT` を固定して、ロケールだけを変えます。

| 入力 | ロケール | 結果 |
| --- | --- | --- |
| `18 Nov 2026` | `Locale.JAPAN` | `DateTimeParseException`。位置3は月名 `Nov` の先頭 |
| `18 Nov 2026` | `Locale.ENGLISH` | `2026-11-18` |
| `30 Feb 2026` | `Locale.ENGLISH` | `DateTimeParseException`。2月30日は存在しない |

`MMM` は月の短い名前を読みます。`DateTimeFormatter.ofPattern(pattern)` でロケールを省くと、フォーマッターを作った環境の既定のFORMATロケールに依存します。環境によって結果が異なるときは、使用中の `formatter.getLocale()` と入力仕様を確認します。英語の月名が入力契約なら、`ofPattern(pattern, Locale.ENGLISH)` を使います。

STRICTは日付として実在するかを確認します。`30 Feb 2026` では英語の月名を読めても、日付は作れません。例外メッセージの全文や位置は失敗の種類やJDKで変わるため、位置だけで原因を決めず、対象の入力とメッセージを確認します。

- `MonthNameDateDemo.java` は解析処理と、3通りの結果を表示するmainを収録します。
- `MonthNameDateDemoTest.java` は英語の月名の失敗・成功と、実在しない日付の拒否を3件のJUnitで確かめます。

[Java 17のDateTimeFormatter公式API](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html#ofPattern(java.lang.String,java.util.Locale))
