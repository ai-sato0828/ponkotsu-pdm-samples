# PostgreSQLのjsonbをRepositoryから検索する

## 実行環境

- JDK 17、Maven 3.9系、起動済みのDocker
- Spring Boot 3.5.16の依存管理を使用。Testcontainers 1.21.4、PostgreSQL JDBC 42.7.11
- PostgreSQL公式イメージ16.14-alpineをdigestで固定。digestはテストクラスに記載

```sh
docker version
mvn test
```

Dockerデーモンへ接続できる環境で実行します。コンテナを使えない場合はテストが失敗します。組み込みDBへ切り替えたり、Dockerのない環境で自動スキップしたりしません。初回はDockerイメージを取得します。

## コードと期待結果

- `src/test/resources/schema.sql` が `jsonb` 列を作ります。
- `IncomingEvent.java` がJSONマッピングを指定し、`IncomingEventRepository.java` がstatusをテキストとして比較します。
- `IncomingEventRepositoryTest.java` がREADY・DONE・キー欠落を混在させて検索し、対象のIDを確認します。一致なしは空リストです。別のテストでPostgreSQLの製品名・16.14のバージョン番号・列型を確認します。

本番マイグレーション、運用中のネットワークや権限、DBの同時更新、検索性能はこの例の検証対象外です。
