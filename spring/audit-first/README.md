# SpringのBean選択とJPA継承の再現サンプル

JDK 17とMaven 3.9系で `mvn test` を実行します。外部DBを使わず、テストはメモリ上で完結します。

| 記事ID | テスト | 確認すること |
|---|---|---|
| 968 | `FieldInjectionTest` | Bean候補が2つあると失敗。Qualifierで実装Bを選ぶと実行結果がBになる |
| 978 | `ConstructorInjectionTest` | 親クラスへ渡す引数の候補が2つあると失敗。子クラスの引数にQualifierを付けると親の処理がmessage:Aを返す |
| 919 | `MappingTest` | 親をEntityにした既定の単一テーブル継承と、MappedSuperclassで共通列を別の子テーブルに持たせた構成を比較する |

失敗ケースはJUnitが例外を捕捉して型を検証するため、Mavenの全体結果は成功します。Bean選択の検証にはSpring Framework 6.2.19とLombok 1.18.36を使用し、過去の記事にあるSpring Framework 4系・Java 8の実行結果とは区別しています。JPAの例は旧記事と同じjavax.persistenceを使うHibernate ORM 5.6.15.FinalとH2 2.3.232です。
