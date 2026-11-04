# Spring MVCとBootの保守サンプル

JDK 17とMaven 3.9系で `mvn test` を実行します。Spring Boot 3.5.16／Spring Framework 6.2.19です。DBや実サービスは不要です。

| 記事ID | 検証箇所 |
|---|---|
| 229 | MaintenanceMvcTestで、ヘッダー欠落・空白・フォーム項目不足を別の例外と400で確認 |
| 1736 | 下記シェルで、同じjarの設定を変えて起動結果を比較 |
| 502 | ChannelSettingsLoaderTestが実際にjarファイルを作り、ストリーム読み込みと同一経路名の重複拒否を確認 |
| 282 | MaintenanceMvcTestで外部テンプレートとクラスパス同梱テンプレートの優先順位を確認 |
| 1075 | MaintenanceMvcTestで循環forward、Thymeleafのテンプレート欠落、文字列レスポンスを区別 |
| 1080 | MaintenanceMvcTestで個別指定3項目、同名の2値、BindingResultによる400を確認 |

1736の検証を行う場合

```sh
mvn package
sh scripts/verify-config.sh target/spring-maintenance-diagnostics-1.0.0.jar
```

7つの起動条件を一時ディレクトリで検証し、jar内にしかない識別値がlocationの置換で消えることも確認します。データベースの接続先や秘密情報は使いません。282はSpring BootのMVCテストであり、旧外部Tomcatの共有クラスローダーやWAR展開は検証対象に含めていません。
