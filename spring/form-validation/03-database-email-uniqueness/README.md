# 事前チェック後に競合してもメール登録を1件に保つ

旧例 `02-controller-service` の7件とは別の、実H2/JdbcTemplateによる独立moduleです。旧module/固定リンクは変更しません。フォームとJSON APIは同じUserServiceを使い、通常重複とDBで競合した重複の両方をDuplicateEmailExceptionとして扱います。事前existsだけでは二重登録を防げない場面を再現します。

## 実行

JDK17とMaven3.9系で、READMEとpom.xmlのあるディレクトリから実行します。

```sh
mvn test
```

JUnitはunit2件＋実H2/MockMvc統合11件の計13件です。JUnitのMockMvcは実Controller/Service/JDBC/DB/Thymeleafを通しますが、TCPサーバを起動した実HTTPクライアント試験ではありません。

手動で触る場合は次を実行し、localhostへアクセスします。

```sh
mvn package
java -jar target/database-email-uniqueness-1.0.0.jar --server.address=127.0.0.1
```

この手動TCP起動・ブラウザ操作は本検証では実施していません。GET /users/new、POST /users、POST /api/usersを用意しています。H2はプロセス内メモリDBで、外部DB/APIは使いません。

## このINSERTで重複と判断できる範囲

schema.sqlの `pk_registration_email PRIMARY KEY(email)` が、registration_usersに対するこのINSERTの唯一のPK/UNIQUEルールです。idは返却用identity値ですが、PK/UNIQUEではありません。`name NOT NULL` 等の整合性制約は別です。

UserService.registerは事前existsがtrueならcauseなしのDuplicateEmailExceptionを出します。falseでもINSERTがDuplicateKeyExceptionになれば、同じ業務例外へ元例外をcauseとして保持して変換します。DataIntegrityViolationException全体やRuntimeException全体はcatchしません。実NOT NULL違反とunitの別整合性例外を使い、重複へ変換しないことを確認します。

これは他の一意列もある任意のテーブルへそのまま使える判定器ではありません。別のUNIQUE制約を追加した場合は、どの制約に違反したのかをDB/ドライバごとに判別する実装へ見直す必要があります。例外メッセージの部分一致で制約を推測するコードは入れていません。メールの大文字小文字統一や空白除去は実装せず、同じ完全一致文字列same@example.comで比較します。

## 通常重複と競合を分ける

通常重複は、先に1行保存してから再登録します。exists=trueなのでINSERTを試みず、DB由来のcauseはありません。

競合は、実DBへのexists問い合わせを2スレッドとも終え、両falseを記録したところでテスト専用CyclicBarrierへ待機します。2件が揃ってからINSERTへ進めるため、片方の事前チェックだけが既登録を見つける通常経路と混ざりません。製品コードにテスト用barrier/latch/sleepはありません。wrapperと制御はsrc/testだけです。

各レースは専用executor2thread、barrier待機5秒、Future取得10秒、finallyでreset/shutdownNow/終了待機5秒。Thread.sleepは使用しません。勝者の名前や採番順はassertせず、成功1・業務重複1・DB1行と敗者の入力保持を検証します。レースごとにDBを空にして同じメールを使います。

## トランザクション境界

Serviceにもテストにも@Transactionalを付けません。Hikari auto-commit=trueを明示し、実接続でも確認します。existsとINSERTはそれぞれのJdbcTemplate呼出しで接続を返却し、INSERT成功時はその呼出し中にcommitされます。barrierはSELECTが戻って接続を返した後だけで待機します。1つの接続やTXを待機中に保持する再現ではありません。

ORMの遅延flush/commit時の例外、外側の@Transactional、別DBの分離レベル・ロック挙動は同じとは限りません。本例が保証するのはこのH2構成の同一メール行の一意性であり、メール送信等の副作用までexactly-onceにする実装ではありません。

## 検証する13件

|種類|内容|
|---|---|
|unit2|DuplicateKeyのみ業務例外へcause保持、別DataIntegrity例外はそのまま|
|統合1|通常重複：exists=true、INSERT0、causeなし、DB1行|
|統合2|Service競合：false2→INSERT2試行→成功1/DB競合1、DB1行|
|統合3/4|HTML通常重複200/メール欄エラー・全入力保持、API通常重複409/JSON|
|統合5|HTML競合：302と200、敗者メール欄エラーと名前/メール/部署保持、DB1行|
|統合6|API競合：201と409、DUPLICATE_EMAIL、DB1行|
|統合7|実NOT NULL障害は重複へ変換せずDB0行|
|統合8/9|HTML空欄/API不正メールはDBチェック前に止める|
|統合10/11|API正常201/保存値、HTML正常302/保存|

HTMLは重複ならusers/newをHTTP200で再描画し、BindingResultのemailへduplicateを付けます。入力値を失うredirectにはしません。正常時だけ302で/users/new?registeredへ移動します。APIは同じServiceの重複を409、code DUPLICATE_EMAILへ変換し、正常は201と生成idを返します。

## 環境と制限

Java17.0.8 / Maven3.9.16 / Spring Boot3.5.16 / JUnit5.12.2。既存依存キャッシュでオフライン検証します。空依存キャッシュの初回取得、外部DB、他JDK、実TCP HTTPは未確認です。

収録は対象Controller/DTO/Form/Service/例外/実JdbcRepository/起動クラス、schemaと設定、HTML、unitと統合test、POM、README、MIT LICENSE。旧7件や他moduleを新13件へ混ぜていません。
