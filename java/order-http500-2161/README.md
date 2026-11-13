# HTTP 500・同じrequestIdのログ・失敗後の注文を照合する

Spring Boot 3.5.16 / Java 17 / H2のローカル教材です。検証環境はJDK17.0.8、Maven3.9.16、H2 2.3.232、JUnit5.12.2。

通知はローカルスタブです。failNotification=trueでSocketTimeoutExceptionを原因に持つIllegalStateExceptionを**意図的に投げるモデル**であり、通知先への外部送信や、実通信タイムアウトを起こしたものではありません。通知成功側も外部へ送信しません。

## 初期状態と起動

1. ZIPを展開し、pom.xmlがあるフォルダーへ移動します。
2. JDK17とMaven3.9系を準備します。次で10件の実HTTPテスト・2件のFilter単体テストとjar作成を行います。

```sh
java -version
mvn clean verify
```

初回の依存取得はネット接続が必要です。確認時は既存キャッシュをofflineで使いました。空キャッシュの初回取得・Windows・他JDKは未確認です。テストは127.0.0.1の一時ポートで実サーバーを起動します。MockMvcではありません。

3. ターミナルAで起動します。ログはapplication.logへ保存します。このコマンドはフォアグラウンドで動き続けます。

```sh
java -jar target/order-http500-2161-1.0.0.jar > application.log 2>&1
```

既定の待受けは**127.0.0.1:18081**です。18081が使用中ならそのプロセスを停止するか、`--server.port=18082`をjarの後ろに付け、以下の完全URLも全て18082へ揃えてください。H2はJVM内だけにあり、起動時のordersは0件です。H2コンソールは有効にしません。新しいJVMでアプリを再起動すれば初期状態へ戻ります。

4. ターミナルBで初期状態を読みます。

```sh
curl -i http://127.0.0.1:18081/api/orders/by-request/demo-order-ok
```

HTTP200、`{"sourceRequestId":"demo-order-ok","count":0,"orders":[]}`です。

## 3つの入力・応答・保存状態

3例は別のrequestIdを使います。下表の件数は**対象requestIdの注文数**です。DB全件数ではありません。同じ起動中に成功例と個別コミット失敗例を順に試すと、DB全体には合計2件残ります。

| 入力 | POSTのURL末尾 | HTTP | 同じrequestIdの注文数 | テスト |
| --- | --- | --- | --- | --- |
| BOOK、通知成功 | /individual | 201 | 1 | O01 |
| BOOK、通知失敗 | /individual | 500 | 1 | O02 |
| BOOK、同じ通知失敗 | /transactional | 500 | 0 | O03 |

requestIdは照合用で、重複排除キーではありません。同じIDでPOSTを再送しても二重登録を防ぎません。繰り返す時はアプリを再起動してH2を初期化するか、新しいrequestIdを使い、GETのURLも同じIDへ揃えてください。

### 1. 通知成功：201、注文1件

```sh
curl -i -X POST http://127.0.0.1:18081/api/orders/individual \
  -H 'Content-Type: application/json' \
  -H 'X-Request-Id: demo-order-ok' \
  -d '{"item":"BOOK","failNotification":false}'

curl -i http://127.0.0.1:18081/api/orders/by-request/demo-order-ok
```

POSTはHTTP201、JSONに`orderId`（実行ごとのUUID）、`item:"BOOK"`、`requestId:"demo-order-ok"`が返ります。GETはHTTP200、count=1、ordersの1件がPOSTのJSONと一致します。

### 2. 個別保存がコミットした後の通知失敗：500、注文1件

```sh
curl -i -X POST http://127.0.0.1:18081/api/orders/individual \
  -H 'Content-Type: application/json' \
  -H 'X-Request-Id: demo-order-individual-fail' \
  -d '{"item":"BOOK","failNotification":true}'

curl -i http://127.0.0.1:18081/api/orders/by-request/demo-order-individual-fail
```

POSTはHTTP500で、次の4フィールドだけを返します。応答ヘッダーX-Request-Idも一致します。

```json
{"status":500,"code":"INTERNAL_ERROR","message":"処理を完了できませんでした","requestId":"demo-order-individual-fail"}
```

GETはcount=1です。IndividualCommitOrderServiceにはトランザクションがなく、別BeanのOrderRepository.saveの@Transactionalが、メソッドから戻る時点でコミットします。その後に通知スタブが失敗しても、この保存は戻りません。

### 3. Service全体のトランザクション内で同じ通知失敗：500、注文0件

```sh
curl -i -X POST http://127.0.0.1:18081/api/orders/transactional \
  -H 'Content-Type: application/json' \
  -H 'X-Request-Id: demo-order-transactional-fail' \
  -d '{"item":"BOOK","failNotification":true}'

curl -i http://127.0.0.1:18081/api/orders/by-request/demo-order-transactional-fail
```

POSTはHTTP500、JSONのrequestIdはdemo-order-transactional-fail、GETはcount=0・orders=[]です。TransactionalOrderService.createの@TransactionalにRepository.saveも参加し、通知スタブのRuntimeExceptionがServiceの外へ出るとDB変更をロールバックします。

このモデルが確かめるのはH2の注文保存です。**実際の外部通知はDBの@Transactionalではロールバックできません。** 相手が受信したか、再送で重複するか、外部システムとの原子性は、このサンプルでは検証していません。

## 応答のrequestIdから実ログと自作コードへ

まずログを開き、応答と同じrequestIdの`result=failed`を検索してください。次のコマンドなら、そのエラー以降を表示できます。

```sh
sed -n '/result=failed.*requestId=demo-order-individual-fail/,$p' application.log
```

該当エラーのスタック内で、`Caused by: java.net.SocketTimeoutException: simulated notification timeout`、`NotificationStub.send(NotificationStub.java:実行時の行)`、`IndividualCommitOrderService.create(...)`を確認します。ServiceTx版はTransactionalOrderService.createへ変わります。**固定の時刻や架空の行番号をこのREADMEには書きません。** 配布evidence/request-matching.logは実採取の時刻と行番号を持ち、evidence/source-locations.jsonが同じソースの該当行とSHAを対応させています。

saveReturned=trueは、保存メソッドから戻った記録です。ServiceTx版ではcommitNotYet=trueと記録し、その時点でコミット済みとは扱いません。エラーログだけで保存結果を断定せず、POST完了後の別HTTP GETで照合してください。GETは別の読取り用トランザクションです。テスト全体へ@Transactionalは付けていません。

エラー応答へ内部例外名・SQL・コードパスを出しません。ログは原因スタックを保持します。Springの標準エラーはResponseEntityExceptionHandlerで400/405/415と必要ヘッダーを保ち、JSONだけ安全な形に揃えます。未知例外は500です。

## 実HTTP10件・Filter2件と誤変更の検出

| テスト | 検査 |
| --- | --- |
| O00 | 未投入requestIdは0件 |
| O01 | 正常201、同じrequestIdの注文1件、JSON/GET/成功ログ一致 |
| O02 | 個別コミット後の失敗500、注文1件、原因と自作行、安全JSON |
| O03 | 同じ通知失敗500、ServiceTxで注文0件、原因と自作行 |
| O04 | ServiceTx正常201、注文1件 |
| O05/O06 | 壊れたJSON/必須item省略は400、注文0件 |
| O07 | 不正な入力requestIdをUUIDへ置換し、応答/保存へ同じ値 |
| O08 | GETでPOST用URLを呼ぶと405、Allow=POSTを保持、保存0件 |
| O09 | text/plainは415、安全JSON、保存0件 |

Filter単体のF01は同一スレッドで2要求を通し、指定IDと新生成UUIDの相違・処理中のMDC・各終了後のMDC除去を検査します。F02はFilterChainがIOExceptionを投げてもfinallyでMDCを除去することを検査します。これはMockHttpServletRequest/Responseを使う2件で、上の実HTTP10件とは分けて数えます。

故障注入は元プロジェクトをコピーして別々に試します。

1. ApiExceptionHandler.handleUnexpectedの`ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)`だけを`ResponseEntity.ok()`へ変更します。

```sh
mvn test '-Dtest=OrderHttpTest#O02*'
```

期待HTTP500に対し200で失敗します。例外を200へ握りつぶす修正を検出します。元へ戻し同じテストを成功へ戻してください。

2. **別のコピー**のTransactionalOrderService.createの@Transactionalだけを外します（Repositoryのものは残します）。

```sh
mvn test '-Dtest=OrderHttpTest#O03*'
```

HTTP500は同じでも、注文0件の期待に対して1件が残り失敗します。@Transactionalを戻し、同じテストを成功へ戻してください。配布ZIPは両方とも正常版です。

## 証跡と配布範囲

- evidence/http-evidence.json: 起動したjarへの3入力、実際の完全URL/ポート・HTTPヘッダー/JSON・別HTTP読取。採取時の一時ポートなので、手元の18081とは異なります。
- evidence/request-matching.log: 同じrequestIdの採取ログ。架空ログではありません。
- evidence/source-locations.json: 実スタックの自作コード行と原本SHA。
- src/main/javaの全型/main、src/main/resourcesのH2設定・表定義、src/test/javaの全テスト、POM、README。targetやjar本体・依存キャッシュはZIPに含めません。

## 停止と初期化

ターミナルAでCtrl+Cを押し、停止を確認します。H2はメモリ内DBなのでアプリを終了すると破棄されます。次の起動は注文0件から始まります。テストと採取用アプリは検証完了時に停止済みです。
