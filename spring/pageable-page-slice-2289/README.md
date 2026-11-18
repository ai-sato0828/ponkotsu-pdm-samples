# Spring Data JPAでPageとSliceのSQLを比べる

JDK 17とMaven 3.9系を用意し、ZIPを展開して `pom.xml` のある場所で実行します。初回は依存関係の取得にネット接続が必要です。

```sh
mvn clean test
```

Spring Boot 3.5.16とH2のインメモリDBで実行するJUnit 2件です。Webサーバーの起動やDBの準備は不要です。

## 同じ問い合わせをPageとSliceで取得する

OPENのID 1、2、3とCLOSEDのID 4を、同じ作成時刻で保存します。両方のメソッドへ同じ `PageRequest.of(0, 2, ORDER)` を渡し、OPENだけを作成日時の降順、同日時ならIDの降順で取得します。

| 取得方法 | ページ | 画面に返すID | hasNext | totalElements | SELECT総数 | うちCOUNT |
| --- | ---: | --- | --- | ---: | ---: | ---: |
| Page | 0 | [3, 2] | true | 3 | 2 | 1 |
| Slice | 0 | [3, 2] | true | 取得しない | 1 | 0 |
| Page | 1 | [1] | false | 3 | 1 | 0 |

最初のPageはページサイズと同じ2件を取得し、総件数を求めるためにCOUNTを実行します。Sliceは次ページの有無を見るために、指定した2件より1件多く取得し、返す一覧を2件に絞ります。

末尾のPageは2件を要求して1件だけ戻るので、このSpring Data JPAではoffset 2と取得件数1から総数3を判断できます。Pageでも総数を推定できる場面ではCOUNTを省くため、検索条件とページ番号を揃えてSQLを確認します。

テストは表の値とSQLの発行件数を検証します。Mavenの実行ログで `PAGE first`、`SLICE first`、`PAGE last` と、その直後の `SQL` 行を探すと発行SQLを確認できます。SELECT総数にはCOUNTのSELECTも含まれます。

## ファイルの役割

- `Inquiry.java` はID、問い合わせ状態、作成日時を定義します。`PageSliceApplication.java` はテスト用のSpring Boot設定です。
- `InquiryRepository.java` に、同じ条件でPageとSliceを返す2つのメソッドを置いています。
- `PageSliceComparisonTest.java` が初期データを保存し、戻り値とSQLを確認します。
- `SqlCapture.java` は、Hibernateが準備するSQLをリポジトリー呼び出し中だけ記録します。同じスレッドの対象呼び出しだけを測るため、その前の保存SQLは集計に入りません。

既存の入力境界サンプルでは、入力拒否と空ページのレスポンスを確認できます。ここではPageとSliceのSQLの違いを確認します。

[Spring Data JPAのページングとSlice](https://docs.spring.io/spring-data/jpa/reference/3.5/repositories/query-methods-details.html#repositories.special-parameters)
