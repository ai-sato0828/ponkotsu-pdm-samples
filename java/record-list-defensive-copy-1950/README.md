# recordのListを、作成時の状態で保持する

JDK17とMaven3.9系で、pom.xmlのあるフォルダーから実行します。

```sh
mvn clean test
java -cp target/classes example.recordcopy.CatalogLabelDemo
```

架空のSKU `SKU-001` のカタログタグを、`[java]`で受け取ります。作成後に呼出側が同じ入力Listへ`sale`を追加したとき、作成時のラベルのタグは変わらない、という用途です。CSVや外部サービスを実際に呼ぶ例ではありません。

`CatalogLabelDemo.java`には次の2つのrecordがあります。

- `SharedLabel`はList参照をそのまま保持します。recordの参照フィールドがfinalでも、入力Listや`tags()`が返すListから中身を変更できます。
- `SnapshotLabel`はcompact constructorの`tags = List.copyOf(tags)`で、入力Listから作成時の内容を受け取る変更不可Listを保持します。この代入値はrecordコンポーネントへ設定されます。入力元に後から追加してもタグは変わらず、アクセサー経由のaddも拒否します。

```text
SHARED sku=SKU-001 tags=[java, sale]
SNAPSHOT sku=SKU-001 tags=[java]
ACCESSOR add=UnsupportedOperationException tags=[java]
NULL_ELEMENT sharedTags=[java, null] snapshot=NullPointerException
```

`CatalogLabelDemoTest.java`の4件は、共有版の入力元/アクセサー双方からの変更、コピー版の入力元変更との隔離、アクセサーadd拒否、null要素の扱いを検証します。SKUが保持されることとタグ内容もassertします。`List.copyOf`はnull要素（およびnullのList）を拒否します。null要素の許容が必要な業務では、その要件を変えずにこの方式をそのまま採用しないでください。この例はnull要素を許可しないSnapshotLabelの契約です。

要素型は変更不可のStringです。Listの構造と可変要素は別問題で、可変Userや入れ子Listを持つrecord全体を深く複製したと主張する例ではありません。`List.copyOf`は入力が既に変更不可なら再利用する場合もあるため、常に新しい参照になるとは検証しません。SKUのnull/形式検証も今回の対象外です。

確認環境はJDK17.0.8/Maven3.9.16/JUnit5.12.2/compiler3.14.1/Surefire3.5.3。既存依存cacheでオフライン実行し、空cache初回依存取得は未確認です。独立コピーでcompact constructorのList.copyOfを除くと、入力隔離・add拒否・null要素拒否のテストが失敗し、復元後は4件成功することを確認します。故障コピー、ログ、target、依存jarは配布に含めません。MIT LICENSE付き。
