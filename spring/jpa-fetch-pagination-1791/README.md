# 注文と明細を取得する一覧を、注文単位でページングする

JDK 17とMaven 3.9系を用意し、展開先の `pom.xml` がある場所で実行します。初回の依存取得にはMavenリポジトリへの接続が必要です。実行環境はSpring Boot 3.5.16、Hibernate ORM 6.6.53.Final、H2 2.3.232。外部DBやWebサーバーは使いません。

```sh
mvn test
```

`OrderPageTest` の4件で、同じ注文3件を使います。注文日は新しい順、同じ日は注文IDの昇順です。1ページは2注文で、`PageRequest` の先頭ページは0です。

| ケース | 期待する結果 |
| --- | --- |
| 明細への `JOIN FETCH` に直接 `Pageable` を渡す | Hibernate 6.6の厳格設定で例外 |
| page=0 | 注文3に明細A・B・C、注文1に明細なし。SELECT 2回 |
| page=1 | 注文2に明細D。SELECT 2回 |
| page=2 | 空。SELECT 1回で明細取得を省略 |

`OrderRepository.findPageIds` は最初に注文IDだけをページングします。`findWithItems` は得られたIDに対して `LEFT JOIN FETCH` を実行し、明細のない注文を保持します。`IN` で取得した結果は順序を前提とせず、`OrderPageService` が先のID順へ戻してDTOを返します。

一覧は `List` を返し、全件数を求める `COUNT` は実行しません。件数は各テストで準備データをコミットした後、Hibernateの統計を初期化し、読み取り時に発行したSQLを数えています。2回の取得間で注文が削除された場合は欠けた注文を飛ばします。競合時のページ境界、別DBでの実行計画、Hibernateの他の版はこの実習の範囲外です。
