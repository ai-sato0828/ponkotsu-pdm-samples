# Stream.toList()の結果にaddできない理由を確かめる

JDK 17、Maven 3.9系、JUnit Jupiter 5.12.2、Surefire 3.5.3を使用します。ZIPを展開し、`pom.xml` のある場所で `mvn test` を実行します。初回はMavenが依存を取得できる環境が必要です。

注文のうち表示対象だけを表示行にし、最後に合計を1行出します。A-101（1,200円）とA-103（800円）が対象で、A-102（5,000円）は対象外です。結果は2明細と合計2,000円の行です。対象が0件なら合計0円の行だけになります。

- `editableRows` は `Collectors.toCollection(ArrayList::new)` で変更可能な一覧を作ります。合計を追加し、呼び出し側でさらに行を追加できます。
- `completedRows` は明細と合計を `Stream.concat` で結合してから `.toList()` で一覧を完成させます。完成後の追加は `UnsupportedOperationException` になります。
- どちらも入力の注文Listを変更しません。

通常はJUnit 6件成功します。別の展開先で `editableRows` の `collect(Collectors.toCollection(ArrayList::new))` を `toList()` に置き換えると、合計を `add` する行で4件がエラーになります。別の展開先で `completedRows` の戻り値を `return details;` に変えると、合計が不足して3件失敗します。配布するコードは修正版です。
