# Spring BootのDB更新とHTTP動作の再現

JDK 17、Maven 3.9系で `mvn test` を実行します。Spring Boot 3.5.16・Hibernate 6.6.53.Final・H2 2.3.232のサンプルです。外部DBは不要です。

| 記事ID | テストクラス | 確認すること |
|---|---|---|
| 1567 | RegistrationServiceTest | 一意制約違反の後、ユーザーだけ増える場合と、Serviceの境界でどちらも増えない場合 |
| 2287 | RequestPageControllerTest | GETの更新、同じPOSTの再送、一意制約で同時POSTを止めた結果、キー再利用で内容が異なる場合の409 |
| 1779 | OrderServiceIntegrationTest | Java上の値のみの変更、コミット後のDB更新、flush後のロールバック |
| 1791 | OrderQueryServiceTest | 3注文の関連取得回数。別顧客で4、同一顧客で2、取得方法を変更すると1、一覧は同じ |
| 2289 | InquiryControllerTest | 同時刻におけるID降順、最終ページ・範囲外ページ、件数上限違反 |

再現対象とテストの対応は表を参照してください。H2とHibernateで観測した結果であり、運用するDBとプロバイダでも確認してください。二重送信の例は固定キーをテストから渡します。本番では、フォームごとにキーを発行し、認証した利用者と操作にスコープを限定し、認可・入力検証を適用してください。PRGのみのルートはテストの比較用です。
