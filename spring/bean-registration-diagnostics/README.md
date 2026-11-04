# SpringのBean登録を切り分ける

JDK 17、Maven 3.9系で `mvn test` を実行します。Spring Boot 3.5.16。実データベースや決済APIは不要です。

## 2029。Bean候補が0件になる原因

- 普通の実装クラスをスキャン範囲に置いても、登録の目印がなければ候補は0件です。
- `@Component` の付いた実装も、起動構成がそのパッケージをスキャンしない場合は0件です。
- アノテーションを編集できない外部SDKを模したクラスを `@Bean` で生成し、依存の型を満たすアダプターを別に登録します。
- Profile限定のBeanは、有効なProfileを設定した場合だけ登録されます。
- MVCスライスでは、テスト対象のControllerに必要なServiceをテスト設定で明示します。

## 1787。ステレオタイプとSpring Dataの登録

- `OrderCsvParser`、`OrderService`、`OrderQueryRepository` はスキャンで登録します。
- `OrderRepository` はSpring Data JPAのRepositoryスキャンとFactoryで登録されるプロキシです。
- 注文入力を変換し、JPAで保存してから別の呼び出しでJdbcTemplateによるSQL検索を実行します。H2を使用します。
- 注文入力のパーサーは、引用符を扱わない固定2列のデモです。一般のCSV形式を処理するライブラリではありません。
