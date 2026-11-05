# Javaの配列比較をJUnitで再現する

JDK 17とMaven 3.9系を用意し、展開したフォルダーで `mvn test` を実行します。初回はMavenの依存関係取得にネット接続が必要です。

`src/test/java/ArrayComparisonTest.java` に次の4件が入っています。

1. 別々に作った `{1, 2}` の比較は `assertEquals` で失敗し、`assertArrayEquals` で成功します。
2. `{1, 2}` と `{2, 1}` の順序の違いを検出します。
3. `{1, 2}` と `{1, 2, 3}` の長さの違いを検出します。
4. 同じ内容の `int[][]` を `Arrays.equals`、`Arrays.deepEquals`、`assertArrayEquals` で比べます。内側の値を1つ変えた場合も確かめます。

不一致の確認では、`assertThrows` が `AssertionFailedError` を捕まえるとテストが成功します。実際の失敗メッセージを見たいときは、`differentOrderIsDetected` の `assertThrows` の中にある `assertArrayEquals(new int[]{1, 2}, new int[]{2, 1});` だけを直接呼び、`mvn -Dtest=ArrayComparisonTest#differentOrderIsDetected test` でそのテストを実行します。結果を確認したら元のコードに戻します。

記事は[Java配列のequalsがfalseになる原因](https://syachiku-uz.info/java-array-equals-arrays-equals/)を参照してください。
