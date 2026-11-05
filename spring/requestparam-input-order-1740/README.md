# @RequestParamの欠落・型変換・制約違反を分ける

JDK 17とMaven 3.9系を用意し、ZIPを展開して `pom.xml` があるフォルダで実行します。Spring Boot 3.5.16を使用します。初回の依存取得ではMavenリポジトリへの接続が必要です。

```sh
mvn test
```

`RequestParamOrderTest` の10ケースは、すべて同じ `EmployeeSearchController` の社員検索に送ります。MockMvcのため、このコマンドではHTTPサーバーを起動しません。HTTPステータスに加え、解決された例外の型と対象引数をテスト結果で確認できます。

| クエリ | 観測する結果 |
| --- | --- |
| なし、`?page=abc`、`?page=-1` | 400。先に解決する必須引数 `keyword` が欠落 |
| `?keyword=A&page=abc`、`?keyword=%20&page=abc` | 400。引数 `page` の型変換エラー |
| `?keyword=A&page=-1` | 400。変換後の `page` が `@Min(0)` に違反 |
| `?keyword=%20&page=0` | 400。空白の `keyword` が `@NotBlank` に違反 |
| `?keyword=A`、`?keyword=A&page=`、`?keyword=A&page=0` | 200、本文は `A:0` |

実HTTPの結果を見る場合は、同じフォルダから起動します。

```sh
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8174
```

別の端末から、URLを引用符で囲んで実行します。

```sh
curl -i 'http://localhost:8174/api/employees?page=abc'
curl -i 'http://localhost:8174/api/employees?keyword=A&page=abc'
curl -i 'http://localhost:8174/api/employees?keyword=A&page=-1'
curl -i 'http://localhost:8174/api/employees?keyword=A&page='
```

終了するときは起動した端末で Ctrl+C を押します。既定の400レスポンスは必ずしも例外クラス名を表示しないため、欠落・型変換・制約の区別はテストで確認してください。

追試のControllerは既存のGitHub固定版からそのまま収録しています。関連する別のJSON・パス検証を含む固定版ZIPと、この追試のテストは別です。 [固定版のController](https://github.com/ai-sato0828/ponkotsu-pdm-samples/blob/0589da101283fdfd7f41119da50262d394b962f2/spring/audit-validation/src/main/java/info/syachiku/auditvalidation/EmployeeSearchController.java)
