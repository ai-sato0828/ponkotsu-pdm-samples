# Optionalの代替処理で不要な顧客保存を防ぐ

JDK 17とMaven 3を使います。ZIPを展開し、`pom.xml` のあるフォルダで `mvn test` を実行します。

- 既存の顧客が見つかれば、その顧客を返し、保存は0回です。
- 保存を呼ぶと重複を拒否するRepositoryでも、既存の顧客をそのまま返せます。
- 未登録なら新規顧客を1回だけ保存し、保存結果を返します。

`CustomerRegistration.java` の `.orElseGet(() -> customerRepository.save(new Customer(email)))` を `.orElse(customerRepository.save(new Customer(email)))` に変更し、再度 `mvn test` を実行します。既存顧客を返せても保存が1回になるケースと、重複保存で例外になるケースが再現します。未登録のケースは通過します。

Repositoryは保存回数を数えるテスト用の実装です。重複拒否はテスト用の例外で再現しており、実際のDBや同時登録は扱いません。
