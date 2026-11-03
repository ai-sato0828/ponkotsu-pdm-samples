# 実行確認（2026-11-04）

- Java 17.0.8 / Maven 3.9.5 / Spring Boot 3.5.16 / Spring Data JPA 3.5.13 / Hibernate ORM 6.6.53.Final / H2 2.3.232 / JUnit 5.12.2。
- 公式Maven Centralから不足したJPA依存を取得してテストを実行。完全に空のキャッシュでの初回取得は未確認。
- 最終版の`mvn -o -B test`：14件成功、失敗・エラー・スキップなし。
- JPAと実H2を使用。不要なMockito依存を除外し、動的なエージェントのattachを必要としない構成にした。
- R12でH2のSQLState 23505（UNIQUE違反）とSpringの`DataIntegrityViolationException`を確認。
- 意図的な変更の確認：別コピーで`UniqueAccount`の一意制約だけを除くと、二行目の保存が成功してR12が失敗した。配布元にその変更は入れていない。
- 別JDK/OS、本番DB、HTTP、同時実行、マイグレーションは未確認。
