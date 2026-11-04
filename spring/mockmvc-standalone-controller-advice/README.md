# MockMvc standaloneSetupと@RestControllerAdvice

会議室予約APIの「予約済みのR-100は拒否する」という固定の仕様を用います。空きのR-200はHTTP 201です。会議室の検索や予約の永続化は扱いません。

JDK 17、Maven 3.9系、Spring Boot 3.5.16。展開先で実行します。

```sh
mvn clean test
```

- `ReservationServiceTest` は、予約済みの部屋がJavaの例外になることと、空きの部屋を通せることを確認します。
- `ReservationStandaloneTest` は、Advice未登録で `perform()` が `ServletException` を投げ、同じControllerへAdviceを登録すると409とJSONになることを確認します。空き部屋の201も確認します。
- `ReservationMvcSliceTest` は、同じAdviceをSpring BootのMVCスライスが拾うことを確認します。`@Import` は簡単な実Serviceだけに使います。

修正版の期待値は、HTTP 409と `{"code":"RESERVATION_CONFLICT","roomId":"R-100"}` です。JSONのキー順序は契約に含めません。サンプルの `@RestControllerAdvice` に依存オブジェクトが増える場合は、対象の依存とBean構成をテストの目的に合わせて用意してください。
