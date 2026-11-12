# Collectors.toMapの重複キーを同じ入力で比較する

## 実行環境と最初の操作

- Java 17以上、Maven 3.9系。検証はJDK 17.0.8、Maven 3.9.16、JUnit 5.12.2。
- ZIPを展開し、pom.xmlがあるディレクトリで実行します。外部DBやSpringは不要です。
- 初回はMavenの依存取得にネット接続が必要です。確認時は既存キャッシュを使うofflineモードでした。空キャッシュからの初回ダウンロード、Windows、他JDK版は未確認です。

```sh
mvn clean test
java -cp target/classes info.syachiku.tomap.Main
```

JUnitは20件成功します。Mainは実装クラスだけを使い、JUnitのクラスパスを追加する必要はありません。

```text
withoutMerge=IllegalStateException
first A=2
last A=3
sum A=5
group A=[2, 3]
label keys=[B, A, C]
```

## 比較する仕様

全ての数量比較に、同じ`A:2 / B:1 / A:3`の3行を使います。先勝ちや後勝ちを採用するかどうかは業務要件で決めてください。合計してよい数量、複数の明細を全て残す場合、本来一意で重複なら停止する場合を区別します。

| 方針 | MapExamplesのメソッド | Aの結果 | 主なテスト |
| --- | --- | --- | --- |
| マージしない | quantitiesWithoutMerge | IllegalStateException | T02 |
| 先勝ち | firstQuantities | 2 | T03/T07 |
| 後勝ち | lastQuantities | 3 | T04/T07 |
| 数量を合計 | totalQuantities | 5 | T05/T07 |
| 明細を全件保持 | groupLines | 2と3の2行 | T06/T07 |

`List.stream()`による逐次・遭遇順のある処理で確認しています。入力を逆順へ変えると先勝ちと後勝ちの結果が入れ替わります。DBの取得順が未指定、順序を持たないデータ源、並列実行へ同じ説明を無条件に適用する例ではありません。

## 型と役割

- `User`は数値IDと表示名を持つgetter型。`namesWithoutMerge`と`firstNamesById`だけで使います。
- `Employee`は社員番号と名前を持つrecord。`toEmployeeMap`は社員番号重複で明示的な例外を投げます。UserのgetIdと同じフィールドへ社員番号を詰め替える例ではありません。
- `OrderLine`は商品コードと数量、`Order`は注文ID・顧客ID・状態、`Item`はコード・表示名です。
- `labelsInEncounterOrder`と`groupLines`は`LinkedHashMap`でキーが初めて現れた順を保持します。通常のtoMapの返すMap実装・反復順には依存しません。

空の一覧は空Mapを返します。一覧そのもののnullと一覧内のnull行は許可しません。各型の文字列フィールドやUserのIDもnullを拒否します。nullを業務上の「未確定」へ読み替える実装ではありません。数量はintの小さい架空値を使い、整数overflow、金額の丸め、データベース制約、同時更新は扱いません。

## 20件の確認範囲

| テスト | 確認すること |
| --- | --- |
| T01 | キーも値も同じAが2回来ても標準toMapは例外 |
| T02〜T06 | 同一3行で例外・先勝ち・後勝ち・合計・全明細保持を比較 |
| T07 | 入力逆順で先/後が入れ替わり、合計は5のまま |
| T08 | 5方式の空入力が空Map |
| T09〜T11 | getter型Userの数値ID、重複停止、名前の先勝ち |
| T12〜T13 | Employeeの社員番号一意/重複と明示的な例外 |
| T14〜T15 | 顧客ごとの全注文保持と状態別件数 |
| T16〜T17 | LinkedHashMapのB/A/C順と重複キーの位置保持 |
| T18〜T19 | 一覧null、一覧内null行の拒否 |
| T20 | 同名でもIDが異なるなら別キーとして保持 |

## テストが誤変更を検出するか試す

検証用のコピーを作り、`MapExamples.firstQuantities`にある第3引数だけを、`(existing, incoming) -> existing`から`(existing, incoming) -> incoming`へ変更します。他のメソッドの同じ式まで一括置換しないでください。

```sh
mvn test '-Dtest=MapExamplesTest#T03*'
```

T03はAが2という期待に対して3が返り失敗します。最後に変更を戻し、`mvn clean test`で全20件を成功へ戻します。配布ZIPには修正版を入れてあります。

## 配布範囲

src/main/javaに全型・変換処理・Main、src/test/javaに全テスト、ルートにPOMと本READMEがあります。targetやローカル依存キャッシュは含めません。記事のクイズ画像は元の短いレビュー断片であり、この実行例の型定義と必要な設定は本ZIPにまとめています。
