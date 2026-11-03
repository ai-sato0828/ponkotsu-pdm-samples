# Spring Data JPAのRepositoryで0件・1件・2件を比較する

既存記事1559の実行サンプル。Java 17、Maven、Spring Boot 3.5.16、Spring Data JPA 3.5.13、Hibernate ORM 6.6.53.Final、H2 2.3.232、JUnit 5.12.2。外部DBやWebサーバーは不要です。

```sh
mvn -B test
```

`pom.xml`のある場所で実行。依存が未取得の場合はMaven Centralへ接続できる環境が必要です。`@DataJpaTest`で本物のRepositoryとメモリ内H2を起動します。モックを使わず、Mockitoもテスト用スターターから除外しています。`create-drop`の学習用構成を本番DBへ接続しないでください。

すべてのメール条件は架空の`dummy@example.invalid`。期待値は先に決め、現在の戻り値から生成していません。

| ID | 準備・操作 | 期待結果 |
| --- | --- | --- |
| R01 | 0行・Optional検索 | `Optional.empty()` |
| R02 | 0行・単一Entity検索 | `null`（非nullのパッケージ既定なし） |
| R03 | 0行・List検索 | 空リスト |
| R04 | 1行・3種類で同じメール検索 | 保存したIDに一致 |
| R05 | 同じメール2行・Optional検索 | `IncorrectResultSizeDataAccessException` |
| R06 | 同じメール2行・単一Entity検索 | 同じ件数不一致例外 |
| R07 | 同じメール2行・List検索 | 明示したID昇順で2件 |
| R08 | 不在IDの`findById(...).get()` | 空Optionalで、直接getは`NoSuchElementException` |
| R09 | `AccountDirectory.searchEmail`で0件 | 正常な空Optional |
| R10 | `AccountDirectory.require`で不在ID | IDを含む`AccountNotFoundException` |
| R11 | 同じメール2行・存在/件数確認 | `exists`はtrue、`count`は2 |
| R12 | 一意制約の表で同じメールを二度保存 | 二行目の`saveAndFlush`で`DataIntegrityViolationException` |
| R13 | 一意制約の表で1行保存 | IDが付与され、存在確認はtrue |
| R14 | 制約なしの表で存在チェック後に同じメールを二度保存 | チェックがfalseでも、その後は2行を保存できる |

`Account`は重複を許す表、`UniqueAccount`はメールの一意性をDBで守る表。両者を同じスキーマだと読み替えないでください。R12でSQLState 23505がログへ出るのは意図した制約違反の確認で、JUnit全体が失敗したことを意味しません。テストごとにトランザクションをロールバックします。

`RepositorySampleApplication`はテストの起点となるBoot構成です。HTTPへの変換、PostgreSQL/MySQLの比較、同時実行の再現、データ移行は含めていません。
