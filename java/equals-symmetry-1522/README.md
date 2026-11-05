# equalsの比較方向とfinalを確かめる

JDK 17で、展開したフォルダから実行します。

```sh
java ProductCodeEqualityDemo.java
```

`OpenProductCode` は継承できる修正前の値クラスを表します。派生クラス `RegionalProductCode` は、コードに加えて地域も比較します。同じコードでも、親から子を比較すると `true`、子から親を比較すると `false` です。

修正後の `ProductCode` は、コードだけで同じものと判断し、`final` で継承を防ぎます。同じコードの比較を両方向から行い、ハッシュ値も同じことを確認します。別コードは両方向で不一致、`null` との比較は `false` です。失敗すると `AssertionError` が出ます。

地域も識別に必要なら、この例のようなコード専用の値型とは別に、コードと地域を組み合わせる値型を設ける設計が考えられます。このサンプルはJPA Entityや継承を公開している型の設計を扱いません。
