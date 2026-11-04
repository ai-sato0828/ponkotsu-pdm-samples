# catchしてもUnexpectedRollbackExceptionが起きるケースをH2で確かめる

JDK 17とMaven 3.9系で `mvn test` を実行します。Spring Boot 3.5.16のJDBCトランザクションとH2 2.3.232を使用します。Webサーバー、実業務DB、外部システムは不要です。

| 条件 | 呼び出し元に見える結果 | 注文 | 審査 |
|---|---|---|---|
| 内側REQUIRED失敗を外側でcatch | UnexpectedRollbackException | なし | なし |
| 必須審査の失敗を呼び出し元へ伝える | ReviewFailedException | なし | なし |
| 必須審査成功 | 正常終了 | REVIEWED | CHECKED |
| 独立した審査が失敗しても注文を保留できる契約 | 正常終了 | PENDING | なし |
| 独立した審査は成功、外側注文が後から失敗 | IllegalStateException | なし | CHECKED |

トランザクション境界は別Spring Beanの公開メソッドを呼ぶ位置です。テスト自身に `@Transactional` を付けず、サービス呼び出し終了後に別の問い合わせでテーブルの内容を読みます。審査レコードは、DBに未確定の注文行がなくても成立する入力IDを使う独立記録です。外部キーや他プロセスとの同時実行、別DB、接続プールの運用サイズはこのサンプルの検証対象外です。
