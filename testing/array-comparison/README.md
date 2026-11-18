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


## double[]の測定計算は、許容する絶対差を決めて比べる

```sh
mvn -Dtest=DoubleMeasurementArrayTest test
```

測定した区間の長さをメートル（m）で合計する例です。`double`の`0.1 + 0.2`は、このJDK17実行で`0.30000000000000004`となり、リテラル`0.3`とdeltaなしで配列を比較すると不一致になります。測定・計算上許容できる絶対差を`1e-9`mと**この教材で定めた場合**、`assertArrayEquals(expectedMetres, actualMetres, DELTA_METRES)`はこの丸め差を許容します。

`1e-9`はすべての測定に適用する推奨値ではありません。実際は単位・計測精度・要求から許容幅を決めます。この例は金額・会計の比較に使いません。NaN/Infinity・相対誤差・丸め方式は検証範囲外です。deltaなし比較がいつも失敗するという意味でもありません。

追加の`src/test/java/DoubleMeasurementArrayTest.java`は4件です。

| テスト | 入力と期待値 |
|---|---|
| exactComparisonRejectsAdditionRoundingDifference | expected0.3m / actual0.1+0.2m、deltaなしの不一致をassertThrowsで検証 |
| chosenToleranceAcceptsAdditionRoundingDifference | 同じ入力を許容幅1e-9mで比較し成功 |
| differenceBeyondChosenToleranceIsStillRejected | expected0.3m / actual0.31mは同じ許容幅では拒否 |
| lengthMismatchIsRejectedEvenWithTolerance | expected1要素 / actual2要素は、先頭要素の丸め差を許しても拒否 |

不一致のassertionをassertThrowsが捕まえるケースも、JUnitのテスト結果は成功です。値の差と配列の長さの差を同じ原因にしないでください。長さ不一致の例は先頭要素の差が許容幅内なので、要素数の違いを分離しています。

合計は既存10件＋新4件＝14件。既存の全JavaとPOM/LICENSEはbyte保持、READMEへの追記とこのテスト追加だけです。元のFileSignatureDemoの4行出力もそのままです。実行はJDK17.0.8/Maven3.9.16/JUnit5.12.2、依存cacheを使いオフラインで確認します。空cache初回取得は未確認です。独立コピーでDELTA_METRESを0.02へ広げると0.31mの拒否テストが失敗し、復元で成功することも確認します。故障版を配布へ混ぜません。
