# Spring MVCの@RequestMappingをMockMvcで再現する

JDK 17とMaven 3.9系を用意し、ZIPを展開したフォルダーで実行します。

```sh
mvn clean test
```

Spring Boot 3.5.16の依存管理とSpring Framework 6.2.19を使います。初回はMavenリポジトリからの依存取得が必要です。このサンプルに起動用の`main`はなく、HTTPサーバーも起動しません。記事中の`curl http://localhost:8080/...`はControllerをSpring Webアプリへ配置した場合の説明用です。ZIPだけで確認するときは、以下のMockMvcテストを実行してください。

## 既存の14件：HTTPメソッドと送受信形式

```sh
mvn -Dtest=RequestMappingTest test
```

`SampleController`はPOST専用のJSON・フォーム受信用、`GetOnlyController`はGET専用の別例です。各例を別のMockMvcへ登録します。POST専用の方へGETを送ると405になり、レスポンスの`Allow`は`POST`になります。複数の例を同じアプリへ同時登録した場合は、この記事の405の前提が変わります。

14件ではJSONとフォームのハンドラ選択、405・415・406・404、Content-Typeなし、Acceptなしとワイルドカード、壊れたJSONの400、指定を省略した場合のJSON変換、GET専用の2パスを検証します。元のController2本とテスト14件は変更していません。

405だけを確かめる場合：

```sh
mvn -Dtest='RequestMappingTest#unsupportedMethodIs405WithAllowPost' test
```

## 追加の9件：paramsとheadersによる呼出先の選択

```sh
mvn -Dtest=ConditionSelectionTest test
```

`ConditionalController`だけを別のMockMvcへ登録します。先ほどのPOST例・GET例とは同時登録しません。同じ`GET /sample`でも、`mode=preview`と`mode=confirm`でメソッドが変わります。`@RequestParam`の`name`は、メソッドが選ばれた後に値を引数へ渡すために使います。

| リクエスト | status | handler | body／解決された例外 |
|---|---:|---|---|
| `GET /sample?mode=preview&name=Alice` | 200 | `preview` | `preview:Alice` |
| `GET /sample?mode=confirm&name=Bob` | 200 | `confirm` | `confirm:Bob` |
| `GET /sample?mode=preview` | 200 | `preview` | `preview:guest` |
| `GET /sample?name=Alice`（modeなし） | 400 | なし | `UnsatisfiedServletRequestParameterException` |
| `GET /sample?mode=unknown` | 400 | なし | `UnsatisfiedServletRequestParameterException` |
| `GET /sample/headers`＋`X-Client: internal` | 200 | `internal` | `internal` |
| `GET /sample/headers`＋`X-Client: external` | 200 | `external` | `external` |
| `GET /sample/headers`（X-Clientなし） | 404 | なし | `NoHandlerFoundException` |
| `GET /sample/headers`＋`X-Client: partner` | 404 | なし | `NoHandlerFoundException` |

各テストは`INPUT=... STATUS=... HANDLER=... EXCEPTION=... BODY=...`を表示します。実行順は固定していません。成功行だけでなく、欠落・不一致でメソッドに入らないことも検証します。

これはSpring Security・独自例外処理・他の候補メソッドを組み込まない、このControllerと採用版の結果です。すべてのヘッダー不一致が404になるという一般則ではありません。`X-Client`は分岐条件を練習する値であり、クライアントが送れるため認証・権限の証明には使いません。本文の送受信形式は独自ヘッダーへまとめず、`consumes`／`produces`で指定します。

[ブログ記事](https://syachiku-uz.info/spring-mvcrequestmapping/)


## 追加の4件：クラスのproducesをメソッドで上書きする

```sh
mvn -Dtest=ProducesOverrideTest test
```

`ProducesOverrideController`だけを独立したMockMvcへ登録します。クラスの`produces = application/json`を`GET /reports/default`は引き継ぎ、`GET /reports/export`はメソッドの`produces = text/plain`で上書きします。クラスとメソッドの値を足して2種類許可する例ではありません。

| パス | Accept | 期待status | 応答 |
|---|---|---:|---|
| `/reports/default` | `application/json` | 200 | JSON `{"status":"ready"}` |
| `/reports/default` | `text/plain` | 406 | `HttpMediaTypeNotAcceptableException` |
| `/reports/export` | `text/plain` | 200 | `status=ready` |
| `/reports/export` | `application/json` | 406 | `HttpMediaTypeNotAcceptableException` |

成功時はContent-Typeと本文、406時はハンドラ未選択と例外型も検証します。テストは`PRODUCES path=... accept=... status=... contentType=... exception=...`の4行を出します（順序は固定しません）。`export`は固定文字列を返す観測用で、ファイル作成やDB保存はしません。Acceptを付けるHTTPクライアント側の条件と、応答のContent-Typeを混同しないでください。

合計は既存14件＋既存params/headers9件＋今回4件＝27件です。既存の5Javaファイル・POM・LICENSEは固定版とbyte同一、READMEだけを追記しています。JDK17.0.8、Maven3.9.16、既存依存キャッシュを使ったオフライン実行を確認しています。空キャッシュからの初回依存取得は今回未確認です。サーバー・外部DB・外部APIは使いません。
