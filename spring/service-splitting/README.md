# Service分割の前後で契約を比較する

対応記事: https://syachiku-uz.info/java-service-class-splitting/

JDK17、Maven3.9.5。Spring6.2.19、JUnit5.12.2、H2 2.3.232。pom.xmlのある場所で `mvn test`。初回は依存取得にネット接続が必要です。検証環境はJava17.0.8、既存キャッシュで `mvn -o test` を実行しました。

通常プレミアム/一般会員、2商品目在庫不足、注文保存失敗、送信前失敗、送信後監査失敗をBefore/After各6件=12件と、料金内訳2件、計14件。14 passed/0 failed/0 errors/0 skipped。

顧客→商品→計算→各商品の在庫判定と更新→注文保存→メール→監査の順を維持。RuntimeException時DB変更は戻りますが、成功済みの偽メール送信記録は戻りません。これはBeforeの動作を保持する例で、望ましい通知保証を設計する例ではありません。メール失敗は送信記録前の失敗だけです。

入力は存在する顧客、重複しない商品ID、1種類1個、指定順、非負価格、int範囲内の合計。注文ID100は実験用固定値です。未存在/重複/並行在庫/実メール/リトライ/実DB依存の隔離水準は未検証。Springの実トランザクションとH2は使用し、テスト外側に@Transactionalは付けていません。

独自コードはMITライセンス。テスト内LabPortsがH2操作と偽送信を担当します。実運用のRepositoryやMailerの完成実装ではありません。
