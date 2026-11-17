# try-with-resources の close順・主例外・suppressed を再現する

Java 17標準APIとJUnit5.12.2で、架空resource A/Bの取得・body・closeを記録します。外部ファイル/DB/API/HTTPへアクセスしません。例外は実際のtry-with-resources構文に処理させ、主例外の手動選択やaddSuppressedは行いません。

## 実行

JDK17とMaven3.9系で、このREADMEとpom.xmlのあるフォルダーから実行します。

```sh
mvn test
java -cp target/classes ResourceScenarios
java -cp target/classes ReaderFailureDemo
```

JUnitは新しいResourceScenariosTestのparameterized6件です。ReaderFailureDemoは従来の単独Mainをbyte保持した別実行であり、この6件には数えません。従来のjavac/java案内はlegacy/README-original.mdに保存しています。旧raw source/POMなしの形式も通常の実行方法で、欠陥として扱いません。

## 期待値

|シナリオ|主例外message|suppressed message順|close順|
|---|---|---|---|
|NORMAL|none|なし|B,A|
|BODY_FAIL|body|なし|B,A|
|BODY_CLOSE_FAIL|body|close:B,close:A|B,A|
|CLOSE_FAIL|close:B|close:A|B,A|
|ACQUIRE_CLOSE_FAIL|open:B|close:A|A|
|ACQUIRE_FAIL|open:B|なし|A|

Aを先に、Bを次に取得します。両方取得した場合は逆順B→Aでcloseします。Bの取得時に失敗した場合はbodyへ入らず、まだ取得できていないBをcloseせず、取得済みAだけをcloseします。traceのopen:Bは取得を試みた記録であり、取得成功の意味ではありません。

body失敗時にはその例外が主例外として維持され、closeの失敗はB→A順でsuppressedになります。bodyが成功してcloseだけ両方失敗した場合は、先にcloseしたBの例外が主例外となり、後続Aの失敗がsuppressedです。B取得失敗時にはopen:Bが主例外で、Aのclose失敗がsuppressedになります。

Mainは各case1行、計6行です。例外messageはこのサンプルが自分で投げる短いラベルです。Java/JDK由来の診断全文を固定assertするものではありません。

```text
NORMAL main=none suppressed=[] close=[B, A] trace=[open:A, open:B, body, close:B, close:A]
BODY_FAIL main=body suppressed=[] close=[B, A] trace=[open:A, open:B, body, close:B, close:A]
BODY_CLOSE_FAIL main=body suppressed=[close:B, close:A] close=[B, A] trace=[open:A, open:B, body, close:B, close:A]
CLOSE_FAIL main=close:B suppressed=[close:A] close=[B, A] trace=[open:A, open:B, body, close:B, close:A]
ACQUIRE_CLOSE_FAIL main=open:B suppressed=[close:A] close=[A] trace=[open:A, open:B, close:A]
ACQUIRE_FAIL main=open:B suppressed=[] close=[A] trace=[open:A, open:B, close:A]
```

## 実装とテストを読む

- RecordingResourceはopenを試みた時・closeされた時にtraceへ追記し、指定された段階だけIOExceptionを投げる再現用実装です。
- ResourceScenariosはtry-with-resourcesの外側で最終例外をcatchし、Outcomeへそのまま格納します。getSuppressedを読み取るだけで、例外を追加・並べ替えしません。
- ResourceScenariosTestは取得/body/closeの全trace、主例外messageとIOException型、suppressed順を同じ6caseで確認します。
- ReaderFailureDemoはread時のIOExceptionがUncheckedIOExceptionに包まれ、close成功が記録される従来の別例です。新しい多重例外caseとは分けてください。

closeが失敗する実装では、その例外を握りつぶさず原因追跡できるように主例外とgetSuppressedの両方を確認してください。ここでは再現のためのラベルを使い、本番のログ方針やリトライは実装していません。返却Streamの生存期間の例もこのmoduleには追加していません。

## 確認環境と収録範囲

JDK17.0.8 / Maven3.9.16 / JUnit5.12.2 / Surefire3.5.6。既存依存キャッシュでオフライン確認。空キャッシュからの初回依存取得、他JDK、外部I/O、実DB/API/HTTPは未確認です。

POM、RecordingResource、ResourceScenarios、ResourceScenariosTest、既存ReaderFailureDemo、README、旧README、MIT LICENSEを収録します。故障検出用コピーは含めません。
