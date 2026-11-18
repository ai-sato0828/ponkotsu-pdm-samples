# 外側readOnlyへREQUIREDで参加した更新処理を比較する

JDK17とMaven3.9系を使い、pom.xmlのあるフォルダーで実行します。

```sh
mvn clean test
```

Spring Boot3.5.16 / Spring Framework6.2.19 / Spring Data JPA3.5.13 / Hibernate6.6.53.Final / H2 2.3.232 / JUnit5.12.2。今回はJDK17.0.8/Maven3.9.16の既存cacheで`mvn -B -o clean test`を実行しています。空cacheからの初回依存取得は未確認です。外部DB・API・Webサーバーは使いません。

## 比較する3ケース

同じID1、状態WAITINGの注文を用意し、同じOrderWriter.shipでSHIPPEDへ変更します。各テストに@Transactionalは付けません。

| 呼出経路 | Writer内readOnly | flush mode | entity readOnly | メモリ状態 | 外側終了後DB |
|---|---|---|---|---|---|
| 外部呼出し→OrderFacade.readOnlyShip→別Bean Writer | true | MANUAL | true | SHIPPED | WAITING |
| 外部呼出し→OrderFacade.writeShip→同じWriter | false | AUTO | false | SHIPPED | SHIPPED |
| 外部呼出し→Writerのみ | false | AUTO | false | SHIPPED | SHIPPED |

3ケースともentityはmanagedです。Writerの`@Transactional`は既定のREQUIREDなので、外側にトランザクションがあれば参加します。Writerへ`@Transactional`を付けただけでは外側readOnlyを通常の更新トランザクションへ切り替えません。対照は同じWriterを呼ぶ外側の境界を通常の更新用トランザクションにしたものです。REQUIRES_NEWへの一律置換を勧める例ではありません。

## 観測点とファイル

- `OrderFacade.java`は、外部から呼ばれる2つのトランザクション境界です。別BeanのWriterを呼び、自己呼出しと混同しません。
- `OrderWriter.java`はEntityManager.findで注文を取得し、setter相当のshipで状態を変えます。tx readOnly、Hibernate flush mode、HibernateがentityをreadOnlyとして扱うか、managedか、変更前後をObservationで返します。save/flushで挙動を変える比較は行いません。
- `PurchaseOrder.java`は注文のEntity、`ReadOnlyApplication.java`はテスト用Boot設定です。
- `ReadOnlyRequiredTest.java`は各ケースの前にJDBC autocommitで同じ初期値を保存します。Facade/Writerから戻った後、トランザクションが存在しないことをassertしてDataSourceからconnectionを再取得し、JPA一次キャッシュを通さずDBstatusを読みます。コネクションpoolは物理connectionを再利用できるため、「物理的に新しい接続」とは主張しません。

実行ログの`TX`行は次の3行です。テスト実行順は保証せず、下は比較用に順を揃えています。

```text
TX path=outer-readOnly readOnly=true flush=MANUAL entityReadOnly=true managed=true memory=WAITING->SHIPPED database=WAITING
TX path=outer-write readOnly=false flush=AUTO entityReadOnly=false managed=true memory=WAITING->SHIPPED database=SHIPPED
TX path=standalone-writer readOnly=false flush=AUTO entityReadOnly=false managed=true memory=WAITING->SHIPPED database=SHIPPED
```

## この結果の範囲

readOnlyはすべてのDBへの書込みを必ず拒否する仕組みではありません。この版と設定ではHibernateがreadOnly取得したentityの変更を通常のdirty checking/flushで保存せず、メモリでSHIPPEDになってもDBはWAITINGでした。明示SQLや他providerの書込み禁止を実証したものではありません。処理が更新用途なら、外部から入るトランザクション境界も含めて設計・確認します。

テストはDB結果とトランザクション実効値の両方をassertし、メモリのsetter成功だけで保存済みとは判定しません。独立コピーでreadOnlyShipのreadOnly=trueをfalseへ変えるとreadOnlyケースが失敗し、復元後は3件成功します。故障コピー・ログ・target・依存jarはZIPに含めません。MIT LICENSE付き。HTTP・本番DB設定・大量データでの性能は未検証です。
