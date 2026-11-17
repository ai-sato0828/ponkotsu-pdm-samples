# Javaの日付入力を厳密に変換する

対応記事: https://syachiku-uz.info/java-localdate-parse-datetimeparseexception/

架空の登録項目を想定した、Java標準APIだけの変換処理とJUnitテストです。Springや画面、CSVファイルの読み込みは実装しません。

## 実行

JDK 17とMaven 3.9系を使用します。このREADMEとpom.xmlがあるフォルダーで実行します。

```sh
mvn -B test
java -cp target/classes info.syachiku.samples.date.DateParsingDemo
```

従来のDateInputsTestは29件のまま保持し、WeekYearParsingExampleTestの3件を加えた合計32件を実行します。Demoの出力は次のとおりです。

```text
既定の解析方針: SMART
SMART: 2026/02/30 -> 2026-02-28
STRICT: 2026/02/30 -> DateTimeParseException
必須・正常: 2024-02-29
任意・未入力: Optional.empty
```

初回はMavenとテストライブラリを取得するネット接続が必要です。検証時は既存の依存キャッシュを使ったため、空キャッシュからの初回取得は未検証です。

## 入力契約

- `DateInputs.parseSlashDate` は必須入力。null、空文字、String.isBlankがtrueの文字列はIllegalArgumentExceptionです。
- 半角数字で年4桁/月2桁/日2桁、区切りは`/`です。前後の空白を取り除きません。形が違う入力はDateTimeParseExceptionです。
- 年はJavaのuuuuに従い0000も扱います。西暦1年以降だけ、未来日不可などの業務上の範囲制約は、このサンプルの後段で別に設けるものです。
- 形が合っていても存在しない日付はResolverStyle.STRICTによってDateTimeParseExceptionです。
- `parseOptionalDate` はnull/空文字/空白だけならOptional.empty。それ以外は同じ厳密変換を行い、不正入力をemptyへ隠しません。
- エラーメッセージを画面向け文言へ変換するハンドラー、ロギング、保存処理は含めません。

## ファイル

- `src/main/java/info/syachiku/samples/date/DateInputs.java`: 変換対象。
- `src/main/java/info/syachiku/samples/date/DateParsingDemo.java`: SMARTとSTRICTを同じ入力で比較する実行例。
- `src/test/java/info/syachiku/samples/date/DateInputsTest.java`: 29件のテスト。
- `pom.xml`: Java17/JUnit5.12.2とビルドプラグインの固定設定。

## テストの内訳

SMARTとの比較1、必須の正常日3、形式・存在日の不正10、必須の未入力5、任意の未入力5、任意の正常日1、任意の不正3、yyyyとuuuuの比較1で計29件です。null/空文字はそれぞれ別ケースとして数えています。

正常版の成功だけでなく、STRICTをSMARTへ変える誤実装で5件が失敗することを作成時に確認しました。誤実装は配布元コードへ混ぜていません。

確認環境: Java17.0.8、Maven3.9.5。詳細はVERIFICATION.md。独自コードはMITライセンスです。

## YYYY は暦年の uuuu とは違う

WeekYearParsingExample は `YYYY/MM/dd`（Locale.JAPAN、STRICT）の誤った組合せと、同じLocale.JAPAN・STRICTの `uuuu/MM/dd` を同じ有効日付で比較します。実行は次のコマンドです。

```sh
mvn -B test
java -cp target/classes info.syachiku.samples.date.WeekYearParsingExample
```

`2026/06/15`、`2026/12/31`、`2027/01/01` はすべて有効な暦日です。しかし Y は週基準年、M/d は暦の月日なので、このフィールドの組合せではLocalDateを解決できません。年末だけの不具合ではありません。週日付が必要なら週基準年・週番号・曜日で表す別の入力契約が必要です。この例は暦日入力を扱います。

小文字 y は year-of-era、大文字 Y は week-based-year、u は proleptic-yearです。従来29件に含む yyyy/uuuu とERAの問題とは別に確認します。Localeによって週の定義や診断文が変わり得るため、例外メッセージ全文は固定assertしません。新3件は例外型・元入力と修正後LocalDateを検証し、既存DateInputs.parseSlashDateとの一致も確認します。Mainも同じ2メソッドを呼びます。

確認環境はJava17.0.8 / Maven3.9.16 / JUnit5.12.2、既存キャッシュ利用です。空キャッシュからの依存取得、他JDK/Locale、HTTP/DBは未検証です。
