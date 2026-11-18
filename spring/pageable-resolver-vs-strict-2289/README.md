# Spring BootでPageableの自動補正と400の入力検証を比べる

同じ `GET /inquiries` を3通りの設定に送ります。Controllerがページ入力を自分で検証する実装と、引数の `Pageable` をSpring Dataが組み立てる実装の差を、HTTP応答と後続処理への到達回数で確かめます。

## 準備とテスト

JDK 17、Maven 3.9系を使います。サンプルはSpring Boot 3.5.16、Spring Data Commons 3.5.13に対応しています。ZIPを展開し、`pom.xml` があるディレクトリで実行します。初回はMavenの依存取得が必要です。外部DB・API・アカウントは使いません。

```sh
mvn -v
mvn clean test
```

7種類の入力を、各設定でMockMvcから合計21件実行します。成功時は採用した `page` と `size` がJSONに入り、後続処理が1回呼ばれます。400の場合は後続処理が呼ばれません。後続処理はページ条件を値として返すだけなので、DBの検索結果や並び順は比較の範囲に含みません。

## 同じリクエストで結果を比べる

各行は `GET /inquiries` にクエリを付けて送った結果です。200の後ろの括弧は、後続処理へ渡った `(page, size)` を表します。

| クエリ | コントローラで検証 | Pageable・既定設定 | Pageable・上限50 |
|---|---|---|---|
| 省略 | 200 (0, 20) | 200 (0, 20) | 200 (0, 20) |
| `?page=1&size=2` | 200 (1, 2) | 200 (1, 2) | 200 (1, 2) |
| `?page=-1&size=2` | 400 | 200 (0, 2) | 200 (0, 2) |
| `?page=1&size=0` | 400 | 200 (1, 20) | 200 (1, 20) |
| `?page=1&size=-2` | 400 | 200 (1, 20) | 200 (1, 20) |
| `?page=1&size=51` | 400 | 200 (1, 51) | 200 (1, 50) |
| `?page=1&size=2500` | 400 | 200 (1, 2000) | 200 (1, 50) |

このサンプルでは、設定しなければ `Pageable` の既定のページサイズは20、上限は2000です。上限だけ50に設定すると、大きな値は最大50になります。サイズを省略した場合や0を送った場合の20は変わりません。マイナスのページ番号は0になります。範囲外を400にするAPI契約では、この例のように元の値を受け取って検証し、適合した値で `PageRequest` を作成します。

## 手元で応答を見る

次のうち一つを選んで起動します。それぞれの比較が終わったら `Ctrl+C` で止めてから、次の設定を起動してください。macOS・Linux向けの例で、アプリは `127.0.0.1` のみにバインドします。

```sh
# コントローラで検証
mvn "-Dspring-boot.run.arguments=--server.address=127.0.0.1 --spring.profiles.active=strict" spring-boot:run

# Pageable・既定設定
mvn "-Dspring-boot.run.arguments=--server.address=127.0.0.1" spring-boot:run

# Pageable・上限50
mvn "-Dspring-boot.run.arguments=--server.address=127.0.0.1 --spring.data.web.pageable.max-page-size=50" spring-boot:run
```

起動したら別のターミナルで実行します。

```sh
curl -i 'http://127.0.0.1:8080/inquiries?page=1&size=51'
```

コードを読むときは、[Controller](src/main/java/info/syachiku/paging/InquiryControllers.java)の引数と検証処理、[テスト](src/test/java/info/syachiku/paging/InquiryPagingTest.java)の期待値を照合してください。 [Spring Data Commons 3.5.13の入力解決処理](https://github.com/spring-projects/spring-data-commons/blob/3.5.13/src/main/java/org/springframework/data/web/PageableHandlerMethodArgumentResolverSupport.java)
