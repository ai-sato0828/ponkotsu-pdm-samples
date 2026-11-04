# Javaのintの掛け算をlongへ代入してもあふれる理由

JDK 17以上とMavenを使います。初回に依存を取得します。展開先で実行します。

```sh
mvn clean test
```

9件のテストが成功します。壊れた例の `BrokenInventoryTotal` は負の結果が出ることを観察するためのクラスです。本体の `InventoryTotal` は、合計をlongで返す場合、int列へ保存する場合、入力自体がlongの場合を分けています。

1. `long result = boxes * itemsPerBox;` では、intで掛けてからlongへ代入します。
2. `(long) boxes * itemsPerBox` では、掛ける前に片方をlongにします。
3. `Math.multiplyExact(boxes, itemsPerBox)` は、選んだ引数の型の範囲を超える結果を例外で拒否します。

修正版の `(long) boxes` を外すと、意図した3件が失敗します。通常のZIPでは修正版を収録しています。
