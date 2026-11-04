# Bean生成ログから初期化に失敗した場所を調べる

JDK 17、Maven 3.9系、Spring Framework 6.2.19。展開先で `mvn test` を実行します。依存が手元にない場合はMaven Centralへの接続が必要です。

- `invoice.batch-size=0` では、外側のBeanが `invoiceJob`、初期化に失敗したBeanが `invoiceRules`、最深原因が `IllegalArgumentException` になることを確認します。
- 同じBean定義に `invoice.batch-size=20` を与えると、コンテナが起動し、`invoiceJob` が設定値20を使うことを確認します。

Spring BootやWebサーバーを起動する構成は含みません。設定値はテスト専用のプロパティソースから与えます。
