# JPA saveの元参照と戻り値を分けて確認する（1779）

JDK17、Maven3.9系、Spring Boot3.5.16 / Framework6.2.19 / Spring Data JPA3.5.13 / Hibernate6.6.53.Final / H2 2.3.232 / JUnit5.12.2。

```sh
mvn test
```

外部DB/API、HTTPサーバーは使いません。Mainの代わりに結合テストが4行の比較結果を出します。検証はJava17.0.8/Maven3.9.16の既存キャッシュでoffline実行。空Mavenキャッシュから初回に全依存を取得する検証は未実施です。

## 同じOrderの契約

Orderは手動のLong @Idで、@Version、@GeneratedValue、Persistableはありません。Spring Dataのこのモデルの既定new判定はIDを使い、非null IDの1はisNew=falseとなりsaveはmergeを選びます。

isNew=falseは「DBに行がある」と同じ意味ではありません。追加テストのfixtureはdeleteAll後に行1が未存在であることと、新しいOrder(id1)のisNew=falseを別々にassertしてからsaveAndFlushします。非null手動IDの初回seedでもmerge経由でINSERTできます。これはpersistを実測した対照ではありません。

旧OrderService、OrderServiceIntegrationTestの3件、Order/OrderStatus/Customerは元の共有サンプルとbyte不変です。専用Repositoryは今回必要なJpaRepositoryへ限定し、別記事のJPQL/OrderRow等を持ち込みません。

## トランザクション境界

テスト自体へ@Transactionalを付けません。seedをrepository呼出しのTXで完了後、別のrepository.findByIdから既存Orderを取得します。この呼出しのread-only TXが終わると対象はdetachedです。テストは外側にTXがないことを前後でassertします。

次にSpringのproxy経由でSaveReturnServiceの@Transactionalメソッドを呼びます。Service内のsaveはこのTXへ参加します。EntityManager.containsの値は**このService TX内**で、save前/直後に採取したスナップショットです。TX完了後もmanagedのままだという主張ではありません。

Service呼出しが正常に戻ってcommitした後、さらに別のrepository.findByIdでDB状態を読みます。変更したJavaオブジェクトをそのまま見て、DB更新と取り違えないようにします。

## 追加4ケース

| 正式Serviceメソッド | 順序 | 元managed前/後 | 戻りmanaged | same | 元/戻りstatus | commit後DB |
|---|---|---|---|---|---|---|
| changeDetachedOriginalAfterSave | save(WAITING)→元だけship→commit | false/false | true | false | SHIPPED/WAITING | WAITING |
| changeReturnedManagedAfterSave | save(WAITING)→戻りship→commit | false/false | true | false | WAITING/SHIPPED | SHIPPED |
| changeOriginalBeforeSave | 元ship→save(SHIPPED)→commit | false/false | true | false | SHIPPED/SHIPPED | SHIPPED |
| saveAlreadyManagedAndShip | 同じTX取得→save→戻りship→commit | true/true | true | true | SHIPPED/SHIPPED | SHIPPED |

mergeはsaveを呼ぶ時点の状態をmanagedな参照へコピーします。save後にdetachedな元だけを変えても、この例ではDBへ反映されません。先に元を変えてからsaveすれば、その状態はコピーされます。通常の同じTXで取得したOrderは、元のOrderService.shipのようにsaveを呼ばず変更検知へ任せる実装も使えます。

この対照で既存managed Orderをsaveしたときはsame=trueでした。「saveの戻り値は常に別」「新規persistでも常に別」へ一般化しません。new判定はモデルやVersion/Persistableの設定で変わります。今回の配布で新規persist専用モデルを作って試したわけではありません。

## JUnit総7件

元のOrderServiceIntegrationTest3件を維持：TX外ではJavaだけSHIPPEDでDB WAITING、同じService TXの変更でDB SHIPPED、flush後の例外でUPDATE1回を観測してもDB WAITINGへrollback。

SaveReturnIntegrationTest4件を追加：上表の4つ。元/戻りのmanagedと参照同一性、変更の順序、Service完了後のDB状態を組み合わせてassertします。テスト順を固定し、CASEの比較4行は実行ごとに同じ値となります。日時や起動ログまで同じ出力になるという意味ではありません。

## ファイルと抜粋

SaveReturnService.javaのBEGIN/ENDはdetachedOriginal、saveReturn、beforeSave、alreadyManaged。本文で抜粋する場合は同じ名前の領域を使えます。全文、旧3テスト、新4テスト、POM、設定、Application、Order/Customer/Status/Repositoryを一式で取得できます。

H2だけの確認です。外部DB、楽観ロック、同時更新、関連オブジェクトのmerge/cascadeまで保証するサンプルではありません。Customerは元Orderの関連を保つために必要で、fixtureはcustomer=nullです。

一次資料：
https://github.com/spring-projects/spring-data-jpa/blob/3.5.13/spring-data-jpa/src/main/java/org/springframework/data/jpa/repository/support/SimpleJpaRepository.java
https://jakarta.ee/specifications/persistence/3.1/apidocs/jakarta.persistence/jakarta/persistence/EntityManager.html
