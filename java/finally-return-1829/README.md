# finallyのreturnで消える戻り値・例外を実行する

Java17の小さな実行一式です。Spring・DB・HTTPサーバー・デバッガは使いません。RecordingOrderRepositoryは保存回数のモデル、RecordingAuditは失敗しないメモリ上の記録器です。

## 最初の実行

ZIPを展開しpom.xmlがあるフォルダーで実行します。

```sh
mvn clean test
java -cp target/classes info.syachiku.finallyreturn.Main
```

JDK17.0.8 / Maven3.9.16 / JUnit5.12.2で確認しています。全30件が成功します。意図的な不良例の危険な挙動も、教材として期待値を固定したテストを含みます。実行時は既存Mavenキャッシュのofflineモードでした。初回依存取得にはネット接続が必要です。空キャッシュの初回取得、別JDK、Windowsは未確認です。

## まず見る比較

| 対象 | 入力 | 不良例 | 修正版 | テスト |
| --- | --- | --- | --- | --- |
| return | tryでAを返す | getValueはB | getValueAfterCleanupはcleanupを記録してA | F01/F02 |
| 注文保存 | 保存成功 | falseなのに保存1回/1件 | trueで1件 | F03/F04 |
| 注文保存 | saveが例外 | falseで例外が隠れる | 同じ例外が呼び出し元へ届く | F05/F06 |
| 名前取得 | DB connection failedをthrow | guestで失敗が隠れる | 同じIllegalStateExceptionが届く | F07/F08 |
| 名前取得 | Alice/null | finallyが値を決めてしまう | Aliceを返す/nullは契約違反としてNPE | F09/F10 |
| catch | operation正常/例外 | 両方9、例外側はcatch到達1回 | 正常0/例外1、例外側のcatch到達1回 | F11〜F14 |
| ロック | 更新正常/例外 | — | どちらもunlock、失敗時は同じ例外 | F15/F16 |

CatchExamples.operation(true)は実際にIllegalStateExceptionを投げます。単にtryでreturn0を書いただけの、catchへ入ることのない例ではありません。caughtCalls()とfinallyのcalls()を別に確認しています。

OrderServiceのRepositoryはメモリ上のモデルです。falseを見て再実行してよいとは言えない理由を示しますが、DBコミットやトランザクションを実行した教材ではありません。

## 名前と戻り値nullの契約

NameRepository.loadNameは非null文字列かRuntimeExceptionを返す契約です。取得失敗をguestやnullへ変換しません。NameService.loadNameは違反したnullをNullPointerException("repository returned null")として拒否し、そのときもauditは1回実行します。空文字禁止という追加仕様は設けていません。

getValueAfterCleanupのeventsには変更可能なArrayListを渡します。この教材のcleanup記録が正常に終わる条件で、呼び出し元がAを受け取る前にcleanupが記録されることを確認します。変更不能リストへのaddが成功する契約ではありません。

## readとcloseの優先順位は別の表で確認する

ResourceExamples.readLines(Path)はUTF8ファイルを開き、readLines(BufferedReader)へ渡します。後者は渡されたreaderの所有権を受け取り、try-with-resourcesで閉じます。共有されているreaderを渡してよいAPIではありません。

| 読取 | close | 呼び出し元の結果 | suppressed | close呼出し | テスト |
| --- | --- | --- | --- | --- | --- |
| 正常 | 正常 | 読んだ行のList | なし | 1回 | F17 |
| IOException | 正常 | 同じ読取IOException | 0件 | 1回 | F18 |
| 正常 | IOException | 同じclose IOException（Listは返らない） | 0件 | 1回 | F19 |
| IOException | 別のIOException | 読取IOExceptionが主例外 | close IOExceptionが1件 | 1回 | F20 |

closeを**呼び出すこと**と、closeが**成功すること**は別です。ProbeReaderはStringReader上にread/close失敗を注入し、closeCallsを数えるモデルです。実OSのclose障害を起こしていません。F22では一時ディレクトリへ実UTF8ファイルを書いてPath版も読みます。

readLineはEOFでnullを返しますが、このメソッドの戻り値Listはnullにしません。空ファイルは空List（F21）、null path/readerはNPE拒否（F30）です。開く時点で存在しないパスならNoSuchFileException（IOException）を返します（F23）。取得に成功していないreaderのcloseを検証したとは扱いません。

旧断片のreader.lines()は、読取中のIOExceptionをUncheckedIOExceptionへ包む仕様があります。この版はreadLineループへ揃えたため、上表の読取例外はchecked IOExceptionで統一しています。どちらも同じ例外型と説明してはいけません。

## importとdeleteが両方失敗する手書きfinally

ImportExamplesの2つのRunnableは、メモリ上で取込処理・後処理の呼出しと失敗をモデル化します。実ファイルの取込/削除は行いません。

| 取込 | 削除 | 素朴版importWithoutSuppression | 保持版importPreservingFailure | テスト |
| --- | --- | --- | --- | --- |
| RuntimeException | 別のRuntimeException | 削除例外が表に出て取込例外を隠す | 取込が主、削除をsuppressedへ保持 | F24/F25 |
| 正常 | RuntimeException | — | 削除例外が表に出る | F26 |
| RuntimeException | 正常 | — | 同じ取込例外、削除呼出し1回 | F27 |
| 正常 | 正常 | — | import→deleteの順で各1回 | F28 |
| 同じ例外インスタンスを両方がthrow | 同じインスタンス | — | 自分自身をsuppressedへ追加せず元を維持 | F29 |

保持版はRuntimeException同士の優先方針を説明する範囲です。checked例外やErrorまで吸収する汎用ライブラリとはしていません。F29の自己抑制ガードは手書き版の方針であり、try-with-resourcesのすべての組合せを再実装したものではありません。AutoCloseableにできるリソースはtry-with-resourcesを優先します。

## テストが誤変更を検出するか

元プロジェクトをコピーして試してください。

1. NameService.loadNameのfinallyのaudit.record()直後だけに`return "guest";`を追加します。

```sh
mvn test '-Dtest=FinallyExamplesTest#F08*'
```

DB例外を期待しているのにguestで正常終了するため失敗します。追加したreturnを戻し、同じテストを成功へ戻してください。

2. 別コピーのCatchExamples.executeBrokenの`operation(fail);`だけを消します。

```sh
mvn test '-Dtest=FinallyExamplesTest#F12*'
```

戻り値9は同じでも、catch到達expected1/actual0で失敗します。操作呼出しを戻し、同じテストを成功へ戻してください。配布ZIPは正常版です。

3. 別コピーのImportExamples.importPreservingFailureで`failure.addSuppressed(cleanupError);`だけを消します。

```sh
mvn test '-Dtest=FinallyExamplesTest#F25*'
```

主例外は同じでも、suppressedの削除例外1件が失われて失敗します。元のaddSuppressedを戻し、同じテストを成功へ戻してください。

## 配布範囲と資料

src/main/javaには全型・Main、src/test/javaには全30テスト、ルートにPOM・README・仕様とテスト対応表expected-results.jsonがあります。targetやMaven依存キャッシュは含めません。サーバーもデバッガも起動しません。

確認した一次資料:

- [JLS17 try/finally](https://docs.oracle.com/javase/specs/jls/se17/html/jls-14.html#jls-14.20.2)
- [JLS17 try-with-resources](https://docs.oracle.com/javase/specs/jls/se17/html/jls-14.html#jls-14.20.3.1)
- [BufferedReader Java17](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/io/BufferedReader.html)
- [Throwable.addSuppressed Java17](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Throwable.html#addSuppressed(java.lang.Throwable))
