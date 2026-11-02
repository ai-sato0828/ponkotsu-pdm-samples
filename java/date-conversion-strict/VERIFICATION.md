# 実行確認

2026-11-02、Java17.0.8、Maven3.9.5、JUnit5.12.2。既存ローカル依存キャッシュを利用した`mvn -o -B test`で29件成功、失敗0・エラー0・スキップ0。

DateParsingDemoはREADME記載の5行と一致。STRICTをSMARTへ変えた検証用コピーでは29件中5件が失敗し、存在しない日付の補正を検出した。検証用コピーは配布物へ含めない。

空の依存キャッシュからの初回取得、別のJDK/OS、Spring Form/Jackson経由の変換、DB保存は未検証。
