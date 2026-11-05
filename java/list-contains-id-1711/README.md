# Javaで採番済みのIDだけをListから探す

JDK 17で、展開後のこのフォルダから実行します。

```sh
java ListContainsIdDemo.java
```

| 入力 | 旧判定 | 修正後 |
|---|---|---|
| 同じ採番済みID | true | true |
| 異なる採番済みID | false | false |
| 採番済みの一覧と未採番の検索対象 | false | false |
| 候補と検索対象の両方に未採番ID | true | false |

修正前は `Objects.equals(null, null)` が `true` になるため、候補に未採番データが混ざると誤一致します。修正後は検索対象のIDを先に検査し、採番済みのIDが一致した場合だけ `true` を返します。候補リスト・候補要素・検索対象DTOは非nullを前提にしています。

比較のためのJavaコードです。EntityとDTOはIDだけを持つ簡易モデルで、JPAやDBは使いません。
