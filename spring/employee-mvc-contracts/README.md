# 社員管理で画面、API、HTTPメソッドの役割を分ける

JDK 17とMaven 3.9系で `mvn test` を実行します。Spring Boot 3.5.16。MockMvcはSpring MVCとThymeleafを通し、外部DB・サーバーポートは使いません。社員データはメモリに置きます。

| 記事 | テスト | 確認内容 |
|---|---|---|
| 1783 | ReturnValueTest | 同じ社員の画面とJSON、RestControllerのString本文、MapのModelとJSON、API登録の201・Location・新規ID |
| 1732 | HttpMethodTest | GETの状態不変、POSTで一件登録、詳細GETで追加登録なし、無効POSTのフォーム復帰、GET削除405、POST削除とAPI DELETE |

HTMLの入力項目は氏名1項目の最小例です。永続DB、認証・認可、CSRF、POST再送の重複防止は別の要件です。
