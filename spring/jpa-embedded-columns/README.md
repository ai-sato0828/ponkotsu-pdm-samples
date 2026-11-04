# 同じ住所を2回埋め込むときの列名

JDK 17、Maven 3.9系、Spring Boot 3.5.16。配布ZIPを展開したディレクトリで `mvn test` を実行します。実DBサーバーは不要で、テストごとに独立したH2インメモリDBを使います。

| 確認 | 期待する結果 |
|---|---|
| `PurchaseOrderWithoutOverrides` | `EntityManagerFactory` の起動時に `city` の列重複で `MappingException` |
| `PurchaseOrder` と `CustomerAccount` のDDL | 注文は請求先3列と配送先3列を持ち、両方のEntityが作成・更新日時列を持つ。検証用DBのテーブルは2つ |
| コミット後に別の `EntityManager` で取得 | 請求先「千代田区」と配送先「大阪市北区」が入れ替わらず、両Entityの監査日時が入る |
| `UsagePeriod` | 開始日と終了日の当日を含み、期間を逆転すると例外 |

`evidence/schema.sql` は、この構成でHibernateが出力したDDLです。実行すると `target/schema.sql` へ再出力し、作成されたテーブル・列はH2の情報スキーマから検証します。既存のデータベースへこのDDLを実行する例ではありません。
