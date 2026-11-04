# リファクタリング前に配送判定の既存動作を記録する

JDK 17とMaven 3.9系で `mvn test` を実行します。DBやSpringは不要です。

- `LegacyDeliveryDecision.java` は整理する前です。
- `RefactoredDeliveryDecision.java` と `ShipmentReviewPolicy.java` は既存動作を保った整理後です。
- `RefactoredDeliveryDecision.withRemoteReview()` は承認済みの仕様変更後です。C06だけがOKからHOLDに変わります。
- `DeliveryDecisionTest.java` で7ケースを3段階に適用します。合計21件です。

退行の確認にはコピーした作業フォルダを使います。RefactoredDeliveryDecision.check の禁止品判定と保留判定のブロックを入れ替え、mvn testを実行すると、保留条件を同時に満たす禁止品がNGからHOLDへ変わります。AfterのC05、仕様変更後のC05・C07で失敗します。
