# 表示用Listをsortしたら取込元の配列まで変わる

JDK 17とMaven 3.9系を使います。ZIPを展開し、`pom.xml` がある場所から実行します。初回は依存関係の取得にネット接続が必要です。

```sh
mvn clean test
java -cp target/classes example.listsort.ListSortDemo
```

CSVから読み込んだ注文IDは `[ORD-103, ORD-101, ORD-102]` の順です。取込結果を照合する処理にはこの順序を渡し、画面のプレビューだけをIDの昇順にします。確認するのは、同じ3件を順序の違う2つの用途で扱う境界です。

| 作り方 | sortの結果 | 呼び出し後の元データ |
| --- | --- | --- |
| `Arrays.asList(array)` | `[ORD-101, ORD-102, ORD-103]` | 元の配列も `[ORD-101, ORD-102, ORD-103]` |
| `List.of(...)` | `UnsupportedOperationException` | Listは `[ORD-103, ORD-101, ORD-102]` のまま |
| `new ArrayList<>(Arrays.asList(array))` | `[ORD-101, ORD-102, ORD-103]` | 元の配列は `[ORD-103, ORD-101, ORD-102]` のまま |

固定長の `Arrays.asList(array)` は、同じ配列の要素を置き換えられるListです。長さを変えずに `sort` でき、その結果は配列にも反映されます。`List.of(...)` は変更不可で `sort` を拒否します。プレビューだけの順序を作るなら、`new ArrayList<>(orderIds)` で一覧をコピーしてから並べ替えます。

`ListSortDemo.java` が並べ替え前・結果・並べ替え後の元データを3行で表示します。`ListSortDemoTest.java` は同じ3つのケースをJUnit 5.12.2で確かめます。

[Java 17のArrays.asList仕様](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Arrays.html#asList(T...)) · [Java 17のList.sort仕様](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/List.html#sort(java.util.Comparator))
