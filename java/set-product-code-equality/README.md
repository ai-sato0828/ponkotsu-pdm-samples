# 商品コードをHashSetとStream.distinctで重複除去する

JDK 17とMaven 3を使います。ZIPを展開し、`pom.xml` のあるフォルダで `mvn test` を実行します。

この例では、商品コードが一致する行を重複として扱います。表示順は保持しますが、同一性を判定する項目から外します。

- `A001`、表示順10と、`A001`、表示順20は同一の商品コードです。`HashSet` と `Stream.distinct()` の件数はともに1です。
- `List` から作った順序付きのストリームでは、先に出た表示順10のオブジェクトが残ります。
- `A001` と `B001` は表示順が同じでも、どちらも残ります。

`ProductCode.java` の `hashCode()` だけを `return Objects.hash(value, displayOrder);` に変え、`mvn test` を再実行します。JDK 17では、ハッシュ値の一致、HashSetの件数、distinctの件数を確かめる3件が失敗します。
