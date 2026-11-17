# 実行確認

2026-11-18、Java17.0.8、Maven3.9.16、JUnit5.12.2。既存依存キャッシュを使用したオフライン実行。DateInputsと旧DateInputsTest（29件）は固定65589fe8ee34e669431b8ef5ebc632037a11bd86からbyte保持。

旧29件＋新WeekYearParsingExampleTest3件＝32件。通常日2026/06/15・年末2026/12/31・年始2027/01/01の同じ入力で、YYYY/MM/ddの例外とuuuu/MM/ddのLocalDateを比較。両formatterへLocale.JAPAN/STRICTを明示し、正しい結果と既存DateInputs.parseSlashDateとの一致もassert。fullエラーメッセージはassertしない。

Mainの6行と旧DateParsingDemoの5行を実行。誤patternをuuuuへ変える対照故障で新3件が失敗し、復元32件成功。配布には故障コピーを含めない。

原本とZIP展開で32件と両Mainを照合。空依存キャッシュからの初回取得、他JDK/Locale、HTTP/DB/APIは未検証。
