# MavenでJUnitのテストが0件になる例

JDK 17、Maven 3.9系、JUnit Jupiter 5.12.2、Maven Surefire Plugin 3.5.3で検証しました。ZIPを展開し、pom.xmlのあるディレクトリで実行します。

- `mvn clean test` は成功しますが、既定のクラス名パターンに合わないため、テストは0件です。
- `mvn -Dtest=GiftWrapFeeSpec clean test` は同じテストクラスを2件実行します。
- `mvn -Dtest=MissingTest clean test` は意図どおり失敗します。
- `mvn -DfailIfNoTests=true clean test` は0件を失敗として扱います。

通常の実行で拾わせるには、`src/test/java/info/syachiku/giftwrap/GiftWrapFeeSpec.java` を `GiftWrapFeeTest.java` に改名し、ファイル内のクラス宣言も `class GiftWrapFeeTest` に変更します。`mvn clean test` で2件成功します。

未修正版を別の場所へ展開し、`GiftWrapFee.java` の `subtotalYen >= 3000` を `subtotalYen > 3000` に変えると、通常実行は0件のまま成功します。明示実行の `mvn -Dtest=GiftWrapFeeSpec clean test` では2件が実行され、3000円で200円になった1件が失敗します。確認後は条件を元に戻してください。
