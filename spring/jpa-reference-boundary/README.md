# findById と getReferenceById の SQL・例外境界を確かめる

Java 17 / Spring Boot 3.5.16 / Spring Data JPA 3.5.13 / Hibernate ORM 6.6.53.Final / H2 2.3.232 の独立サンプルです。外部DB/API、Web Controller、HTTP 404変換はありません。Mainでアプリを起動する必要はありません。

```sh
mvn test
```

JDK 17 と Maven 3.9.16、既存の依存キャッシュで実行確認しています。依存がなければ初回はMaven Centralへの接続が必要です。空のキャッシュからの依存取得は未確認です。

## 比較する入力と期待値

各テストの開始前に、ID 1・name「田中」の1行を保存してコミットします。ID 999 は存在しません。テスト自体には @Transactional を付けず、外部のService proxyを呼びます。Serviceの各測定メソッドは REQUIRES_NEW の読取トランザクションを開き、Persistence Contextをclearし、キャッシュをevictしてから測定を開始します。seedのDELETE/INSERTは測定対象外です。

|テスト|期待値|
|---|---|
|findById(1)|find直後1・name読取後1 SELECT、Optionalに田中|
|findById(999)|find直後1・空Optionalのmap後1 SELECT、Optional.empty|
|getReferenceById(1) → getId → getName|累積SELECT 0 → 0 → 1、初期化 false → false → true|
|getReferenceById(999) → getId → getName|累積SELECT 0 → 0 → 1、getNameでEntityNotFoundException|
|不存在参照のIDだけをTX外で読む|999を返す、SELECT 0、存在確認にはならない|
|既存参照のnameをTX終了後に読む|LazyInitializationException、SELECT 0|
|不存在find結果のOptional.get|NoSuchElementException、SELECT 1|
|同一Contextで先にfindしてから参照取得|同じmanaged参照、累積SELECT 1 → 1 → 1|
|業務メソッドで既存ID 1|田中を返す|
|業務メソッドで不存在ID 999|CustomerNotFoundException|
|既存ID 2・name=null|entityPresent=true / name Optional.empty、業務メソッドはnullを返す|
|別Service TXを2回呼ぶ|どちらも参照取得0・name取得後1、初回結果を使い回さない|

JUnitは12件です。SQLカウンタは厳密にはHibernate StatisticsのprepareStatementCountです。この測定窓では書き込みがなく、StatementInspectorで実際のSQLを収集し、全てSELECTであることも検証します。SQL_WINDOW行には実SQLが出ます。SqlCaptureはこの単一スレッドの再現用で、業務用のSQL監視基盤ではありません。

## 観測用コードと業務判断を分ける

ReferenceService.findSnapshot は FindResult の entityPresent と name（Optional<String>）を分けて返します。nameがnullの既存行もmap後はOptional.emptyになるため、name.emptyで行不存在を判定しません。計測用Serviceと分けた通常業務用CustomerQueryService.requireCustomerNameはfindByIdのEntity段階でorElseThrow(CustomerNotFoundException)を適用し、それからnameを返します。存在するname=nullはnullを返し、行不存在とは区別します。これはHTTP 404変換ではありません。findThenUnsafeOptionalGet は .get() の別例外を示す意図的な悪い例です。referenceForOutsideAccess が返すentityも、TX外の遅延初期化を再現するためだけの対照です。その他の観測結果は値のsnapshotであり、未初期化entityを返しません。

ReferenceTraceはgetReferenceById / getId / getNameのどこまで進んだかを残します。例外は同じRuntimeExceptionを再throwし、EntityNotFoundExceptionをcatchして正常commitする実装にはしていません。

## 採用版・設定に限った実測

2nd level/query cache、OSIV、enable_lazy_load_no_transは無効、hibernate.jpa.compliance.proxy=falseを明示しています。このHibernate設定ではIDアクセス時にロードされませんが、JPA一般の保証ではありません。providerは参照取得の時点で不存在例外を出すこともできます。getReferenceByIdの成功だけで存在を確認したと扱わないでください。

存在の有無をこの境界で判断したい場合はfindByIdのOptionalを処理します。ただし、その後の並行削除や更新の競合をなくす保証にはなりません。すでに存在を別の仕様で扱っている関連付け用途までgetReferenceByIdを禁止するサンプルではありません。LazyInitializationExceptionはTX/Context終了後のアクセス、NoSuchElementExceptionはOptionalの扱いであり、不存在の初期化時例外と分けてください。

## ファイル

- Customer / CustomerRepository: 固定入力となるJPA entityとrepository。
- ReferenceService: find、参照の段階測定、TX外対照、Optional対照、同Context対照。
- CustomerQueryService: 通常のreadOnly/REQUIREDの業務例。計測用clear・cache操作・REQUIRES_NEWは持ちません。
- CustomerNotFoundException: Entity段階で行不存在を表す業務例外（HTTP変換なし）。
- ReferenceTrace: 例外時にも段階と値だけを記録。
- SqlCapture: 実SELECTの検査。
- ReferenceBoundaryTest: seedと12ケース、SQLと初期化状態・例外型を検証。
- application.properties / pom.xml: 採用設定と固定版。

参考: Spring Data JPA JpaRepository#getReferenceById、Jakarta Persistence EntityManager#getReference の契約と、採用Hibernateでの実測を区別しています。
