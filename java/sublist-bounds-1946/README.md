# JavaのArrayList.subListで終点と逆順を確認する

注文ID `A01` と `B02` の2件を使い、注文一覧のプレビューで切り出せる範囲を確かめます。JDK 17とMaven 3.9系を使います。

```sh
mvn -B -ntp test
mvn -B -q -ntp compile exec:java
```

依存ライブラリを取得済みの環境では、両方に `-o` を付けて実行できます。Java 17と既存のMavenキャッシュで実行を確認しました。

| 範囲 `[from, to)` | `ArrayList.subList` の結果 | `preview` の結果 |
| --- | --- | --- |
| `[0, 2)` | `[A01, B02]` | `[A01, B02]` |
| `[2, 2)` | `[]` | `[]` |
| `[-1, 1)` | `IndexOutOfBoundsException` | 引数と件数を明示して拒否 |
| `[0, 3)` | `IndexOutOfBoundsException` | 引数と件数を明示して拒否 |
| `[2, 1)` | `IllegalArgumentException` | 引数と件数を明示して拒否 |

`to` は含まないので、`to == size` で最後の要素まで取れます。`from == to` は0件を表す有効な範囲です。2件の一覧を使う場合、`subList(2, 2)` は空で、単一要素を読む `get(2)` は範囲外です。

`preview` は画面から受け取った範囲を `0 <= from <= to <= size` の条件で検証します。エラーに両端と件数を入れると、入力を調べるときに負数、上限超過、逆順を区別できます。結果をその場で表示する想定なので、返り値は元の一覧にひもづくビューです。元の一覧を後で変更しても切り出した内容を保持する用途では、`new ArrayList<>(orders.subList(from, to))` で別の一覧にします。

JUnitは直接呼び出す5件と、`preview` の5件を確認します。出力の例外名はJava 17の `ArrayList` に対するものです。[Java SE 17のArrayList.subList](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ArrayList.html#subList(int,int))
