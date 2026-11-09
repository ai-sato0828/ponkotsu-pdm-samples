# Controllerの404・405・400を同じアプリで分ける

対象記事: https://syachiku-uz.info/spring-boot-404-not-found-controller/

Java 17、Maven 3.9系、Spring Boot 3.5.16。DBとログインは不要です。架空の利用者ID10だけを返します。依存関係の初回取得にはネット接続が必要です。検証時はOracle JDK17.0.8と既存Mavenキャッシュを使いました。空キャッシュからの取得、実プロキシ、認証フィルター、テンプレート描画は未確認です。

## 収録するもの

- pom.xml: Maven・Java・Spring Bootの設定。
- src/main/java/info/syachiku/controller404/Application.java: 起動クラス。
- 同UserController.java: /api/users/{id}のGETとコンストラクタ注入。
- 同UserService.java: ID10を返す処理、未存在の404、到達回数の教材用観測。
- 同UserResponse.java: JSONのid/name。
- src/main/resources/application.properties: 127.0.0.1、18027、context-path=/sales。
- src/test/java/info/syachiku/controller404/UserControllerTest.java: 実HTTP7件＋実コンテキストのMockMvc2件。

記事のOrderController/ReportController/パッケージ図は、Mapping連結・Bean登録を説明する別の抜粋です。このZIPにそれらの画面・テンプレートは含めません。実行する題材はUserControllerへ統一しています。

## まずテストする

展開したフォルダーで実行します。

```sh
mvn clean test
```

9件成功します。7件は実Tomcatのランダムポートへjava.net.httpでアクセスします。MockMvc2件も同じSpring Bootアプリと実UserServiceを使います。Mockitoによる擬似ServiceやstandaloneSetupは使いません。

| ケース | HTTP | URLのパス | 結果 | Service呼び出し |
|---|---|---|---|---|
| H01 | GET | /sales/api/users/10 | 200、id=10/name=Tanaka | 1 |
| H02 | GET | /sales/api/user/10 | 404、usersのsが欠ける | 0 |
| H03 | GET | /sales/api/users | 404、必須パスIDがない | 0 |
| H04 | POST | /sales/api/users/10 | 405、AllowにGETを含む | 0 |
| H05 | GET | /sales/api/users/abc | 400、Longへ変換できない | 0 |
| H06 | GET | /api/users/10 | 404、/salesが欠ける | 0 |
| H07 | GET | /sales/api/users/999 | 404、Serviceが未存在を返す | 1 |

各テスト前に到達回数をリセットします。到達回数はこの教材の観測用で、実業務へ置くAPIではありません。同じ404でも、H02/H03/H06は利用者検索を呼ばず、H07は呼んだ後に未存在を返します。ステータスだけで到達有無を断定しないでください。

## 起動して別端末から確認する

端末A:

```sh
mvn spring-boot:run
```

18027番が使用中なら、他のアプリを止めず、`mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=18028` で起動し、以下のURLも18028へ揃えます。

端末B（各コマンドのレスポンス先頭でステータスを確認）:

```sh
curl -i http://127.0.0.1:18027/sales/api/users/10
curl -i http://127.0.0.1:18027/sales/api/user/10
curl -i http://127.0.0.1:18027/sales/api/users
curl -i -X POST http://127.0.0.1:18027/sales/api/users/10
curl -i http://127.0.0.1:18027/sales/api/users/abc
curl -i http://127.0.0.1:18027/api/users/10
curl -i http://127.0.0.1:18027/sales/api/users/999
```

順に200/404/404/405/400/404/404。最初のJSONは `{"id":10,"name":"Tanaka"}`、POSTのAllowにはGETが含まれます。エラー本文はSpring/TomcatとAcceptによって形式が異なるため、文面の一致を判定条件にしません。curlだけでは内部の到達回数は見えず、その部分はJUnitで照合します。終了は端末AでCtrl+Cです。

## 変更でテストが検出すること

検証用コピーだけでUserControllerの `@RequestMapping("/api/users")` を `/api/user` に変え、`mvn test` を実行します。H01/H02/H04/H05/H07とMockMvc2件の計7件が失敗します。意図したURL契約とずれたためです。元へ戻して全9件が成功することを確かめます。Mappingを変えるのと、呼び出し側URLを直すのとでは意味が違います。実際にどちらを直すかは正とするAPI仕様から決めます。

## ControllerがBeanでなくなった場合も同じURLで試す

別の検証用コピーで、UserController.javaの `@RestController` という1行だけを外します。`@RequestMapping` と `@GetMapping` は残します。次のコマンドは実HTTP7件だけを実行します。

```sh
mvn test '-Dtest=UserControllerTest#actualHttpDistinguishesRouteMethodInputAndMissingData'
```

H01の観測行は `H01 GET /sales/api/users/10 status=404 service=0` になります。URLを変えていなくても、Controllerとして登録されず、利用者検索へ進みません。H01/H04/H05/H07の4件が失敗し、H02/H03/H06は元から404・未到達のため成功のままです。判定前にstatus/callsを出すので、期待200に対する実際404だけでなく、Service0も読めます。これはこの構成での実測であり、すべてのスキャン不備が必ず同じ症状になるという意味ではありません。

`@RestController`を戻し、`mvn clean test`で全9件を通して終了します。Mappingのタイプミスを直す作業と、Bean登録を直す作業を混ぜないでください。

MockMvcでは `get("/sales/api/users/10").contextPath("/sales")` として、/salesをアプリの文脈パスとして明示します。擬似リクエストの設定だけでは実Tomcatの配備先を検証できないため、H06を実HTTPで確認します。
