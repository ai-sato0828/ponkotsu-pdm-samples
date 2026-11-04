# Spring MVCがControllerを呼び出す条件

JDK 17、Maven 3.9系、Spring Framework 6.2.19。展開先で `mvn test` を実行します。依存が手元にない場合はMaven Centralへの接続が必要です。

| リクエスト | HTTP | 本文 | `greet()` の呼び出し |
| --- | --- | --- | --- |
| GET /greeting | 200 | Hello | 1回 |
| POST /greeting | 405 | 検証対象外 | 0回 |

`MockMvc` でSpring MVCのディスパッチを試します。実際のサーバー、Spring Bootの自動設定、データベース、Spring Securityは使用しません。呼び出し回数は検証のための目印です。
