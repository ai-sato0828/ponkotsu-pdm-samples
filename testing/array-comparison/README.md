# Javaの配列比較をJUnitで再現する

JDK 17とMaven 3.9系を用意し、展開したフォルダーで `mvn test` を実行します。初回はMavenの依存関係取得にネット接続が必要です。

`src/test/java/ArrayComparisonTest.java` に次の4件が入っています。

1. 別々に作った `{1, 2}` の比較は `assertEquals` で失敗し、`assertArrayEquals` で成功します。
2. `{1, 2}` と `{2, 1}` の順序の違いを検出します。
3. `{1, 2}` と `{1, 2, 3}` の長さの違いを検出します。
4. 同じ内容の `int[][]` を `Arrays.equals`、`Arrays.deepEquals`、`assertArrayEquals` で比べます。内側の値を1つ変えた場合も確かめます。

不一致の確認では、`assertThrows` が `AssertionFailedError` を捕まえるとテストが成功します。実際の失敗メッセージを見たいときは、`differentOrderIsDetected` の `assertThrows` の中にある `assertArrayEquals(new int[]{1, 2}, new int[]{2, 1});` だけを直接呼び、`mvn -Dtest=ArrayComparisonTest#differentOrderIsDetected test` でそのテストを実行します。結果を確認したら元のコードに戻します。

記事は[Java配列のequalsがfalseになる原因](https://syachiku-uz.info/java-array-equals-arrays-equals/)を参照してください。

## 配列を持つ値オブジェクトの防御的コピー

今回の追加は既存4件とPOMを変更せず、記事のFileSignature全文を `src/main/java/FileSignature.java` に収録したものです。default package、final class、getterなし。constructorはrequireNonNull(...).clone()で入力のbyte配列を所有し、Arrays.equals/hashCodeで内容を扱います。FileSignatureTestの新6件と合わせ計10件です。

```sh
mvn test
java -cp target/classes FileSignatureDemo
```

|新テスト|確認する結果|
|---|---|
|同内容の別配列|equalsがtrue、hashCodeが同じ|
|同内容の別署名で検索|Set.containsがtrue、Map.getがstored|
|構築後に入力元を1→9へ変更|署名は元の{1,2}と等しく、hashも変わらない|
|入力元の変更後に検索|元署名自身と同内容別署名の両方でHashSet/HashMapの登録を検索できる|
|null入力|null拒否（NullPointerException）|
|異内容・順序違い・長さ違い|別署名と判定|

`FileSignatureDemo` は別々の入力配列 `{1,2}` から構築し、登録後に先頭を9へ変えます。Beforeはデモ用のprivate nested class UnsafeFileSignatureだけがcloneを省略します。推奨のFileSignature本体を故障版へ変更する例ではありません。Afterは記事と同じFileSignatureを使います。

```text
BEFORE initial equalsExpected=true setContains=true mapValue=stored
BEFORE mutated equalsExpected=false hashChanged=true setContains=false mapValue=null
AFTER initial equalsExpected=true setContains=true mapValue=stored
AFTER mutated equalsExpected=true hashChanged=false setContains=true mapValue=stored
```

このBeforeの検索失敗はJDK17.0.8でこの入力・操作順を実行した具体例です。可変キーを登録した全状況で必ず同じ結果になる保証ではありません。equals/hashに使う状態を登録後に変更しないことが重要です。final fieldは配列参照の再代入を禁止するだけなので、入力元との共有をcloneで切ります。byte[]はprimitive要素の1次元配列であり、cloneだけで任意の入れ子オブジェクトを複製する話ではありません。

実行確認: JDK17.0.8 / Maven3.9.16 / JUnit5.12.2 / Surefire3.5.6。既存依存キャッシュを利用したオフライン実行です。空キャッシュからの初回依存取得、他JDK、外部DB/API/HTTPは未検証です。

配布はPOM、旧ArrayComparisonTest、FileSignature、FileSignatureTest、FileSignatureDemo、README、MIT LICENSEを含みます。故障検出用の作業コピーは含めません。
