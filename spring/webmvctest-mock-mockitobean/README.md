# `@WebMvcTest`でControllerの依存を用意する

JDK 17、Maven 3.9系を使います。Spring Boot 3.5.16のサンプルです。このフォルダーで実行します。

```sh
mvn test
```

`GET /rooms/R-100`に対し、通常は次の3テストが成功します。

| クラス | 使うService | 確認すること |
|---|---|---|
| `RoomAvailabilityWebMvcTest` | `@MockitoBean`で登録したモック | 同じURLでHTTP 200、`roomId`は`R-100`。モックの戻り値を変えると`available`は`true`と`false`に変わる。 |
| `RoomAvailabilityBootTest` | アプリケーションコンテキストが登録した実Service | HTTP 200、`roomId`は`R-100`、`available`は`true`。Serviceはモックではない。 |

`RoomAvailabilityService`の空き判定は、`R-100`なら`true`を返す固定例です。実際の在庫、予約、DB、ネットワーク通信は扱いません。テストはMockMvcで実行します。

## `@Mock`だけで失敗する例

次のコマンドは、意図的に失敗する例だけを実行します。通常の`mvn test`には含まれません。

```sh
mvn -Dtest=MockFieldOnlyReproduction test
```

`MockFieldOnlyReproduction`はコンストラクターで`@Mock`フィールドを初期化し、`REPRO_FIELD_MOCK_CREATED`を出力します。それでも`RoomAvailabilityService`はSpringのBeanにならず、`RoomAvailabilityController`のコンストラクター引数を解決できません。最深原因は`NoSuchBeanDefinitionException`です。`REPRO_MOCKMVC_REQUEST_REACHED`は出ず、HTTPステータスを確認する前に止まります。

この例では、Javaエージェントを動的に読み込まなくても非finalのServiceをモックできるよう、テストの`MockMaker`を`mock-maker-subclass`に指定しています。この設定がSpringへBeanを登録するわけではありません。

例外ハンドラーを登録して予約エラーをHTTP 409へ変換する別の例とは異なり、ここで比較するのは、Controllerを組み立てるときにSpringから依存を渡せるかどうかです。
