# HashMapでキーを調べる

JDK 17以上。ZIPの展開先で下の5行を一つずつ実行します。

```sh
java equals-only/Lookup.java
java fixed-key/Lookup.java
java mutable-key/MutableLookup.java
java orders/Aggregate.java
java collisions/Collision.java
```

- equals-onlyは値で比較する一方で、hashCodeはObjectから継承します。別インスタンスを使うため、同じ文字列でもハッシュ値が異なるとnullです。Objectが二つに同じハッシュ値を返す場合は取得できることがあります。
- fixed-keyはequalsとhashCodeの両方がユーザーIDを使うので「田中」です。
- mutable-keyは登録後にIDを書き換えるため、記事で使うU001とU002ではnullです。
- ordersは注文番号Aの明細が一つのグループにまとまり、件数2を返します。
- collisionsはハッシュ値が2112で同じ「Aa」「BB」に対して、それぞれの値を取得します。
