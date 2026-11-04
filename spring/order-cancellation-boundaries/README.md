# 注文取消でController・Service・応答DTOの境界を確認する

JDK 17、Maven 3.9系、Spring Boot 3.5.16で確認しました。展開先で `mvn test` を実行します。初回は依存取得にMaven Centralへの接続が必要です。

| 入力 | HTTP | 結果 |
| --- | --- | --- |
| GET /orders/100 | 200 | id 100、WAITING、cancelable true。保存なし |
| POST /orders/abc/cancel | 400 | IDをlongへ変換できず、読み込み・保存とも0回 |
| POST /orders/999/cancel | 404 | ORDER_NOT_FOUND。保存0回 |
| POST /orders/101/cancel | 409 | ORDER_CANNOT_BE_CANCELED。SHIPPEDのまま、保存0回 |
| POST /orders/102/cancel | 409 | ORDER_CANNOT_BE_CANCELED。CANCELEDのまま、保存0回 |
| POST /orders/100/cancel | 200 | id 100、CANCELED、cancelable false。保存1回 |

各ケースは状態を初期化してから実行します。成功時のJSONは3項目を厳密比較し、保存用の内部参照を含まないことを検証します。HTTPなしのJUnit 4件と、BootコンテキストでMockMvcを使う6件があります。

Repositoryはテスト用のメモリ実装です。データベース、同時更新、認可、外部通信、トランザクションは検証対象外です。DTOの `cancelable` はこの例の状態だけを表し、利用者の権限を表すものではありません。
