# XMLと@Serviceで候補0件・1件・2件を切り分ける

既存記事2185用の独立したサンプル。Java 17、Maven、Spring Framework 6.2.19、JUnit 5.12.2。Spring Boot、Webサーバー、DBは不要です。

```sh
mvn -B test
mvn -B org.codehaus.mojo:exec-maven-plugin:3.6.3:java -Dexec.mainClass=info.syachiku.bean.BeanLookupDemo
```

`pom.xml` がある場所で実行します。依存が未取得の場合はMaven Centralへ接続できる環境が必要です。1行目が全6件のJUnit、2行目は5状態の比較出力です。B06はJUnitだけで実行します。

| ID | 設定・操作 | 仕様上の期待結果 |
| --- | --- | --- |
| B01 | `scan.xml`で`info.syachiku.inquiry`を探索 | `someService` 1件、呼出し可能 |
| B02 | `wrong-scan.xml`で`info.syachiku.missing`を探索 | 0件、厳密に`NoSuchBeanDefinitionException` |
| B03 | XML未読込の`GenericApplicationContext` | 0件、厳密に`NoSuchBeanDefinitionException` |
| B04 | `duplicate.xml`で同じ型を別名登録、型だけで取得 | `serviceA`・`serviceB`の2件、`NoUniqueBeanDefinitionException` |
| B05 | `primary.xml`で`serviceA`を既定に指定、型で取得 | 候補2件のまま`serviceA`を取得 |
| B06 | `duplicate.xml`のまま、名前と型を指定 | `serviceA`を取得、二つのインスタンスは別 |

- `src/main/java/info/syachiku/inquiry/SomeService.java`：全ケース共通の要求型。
- `src/main/java/info/syachiku/bean/BeanContexts.java`：XMLを読む入口と読まない入口。
- `src/main/java/info/syachiku/bean/BeanLookupDemo.java`：mainで比較する場合の入口。
- `src/main/resources/`：名前空間を含むXML4枚。
- `src/test/java/info/syachiku/bean/BeanLookupTest.java`：B01〜B06の期待結果。

このサンプルは親を持たないコンテナのBean取得を比較します。Webアプリ全体の依存注入、Profile、親子コンテナ、`@Qualifier`を使うコンストラクタ注入は対象外です。`primary`は登録済みの候補から選ぶための設定です。0件を登録する仕組みではありません。
