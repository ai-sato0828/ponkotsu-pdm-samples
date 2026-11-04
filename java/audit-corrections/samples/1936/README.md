JDK 17以上で実行します。

```sh
javac *.java
java IntegerEqualityDemo
```

HotSpotでは `java -XX:AutoBoxCacheMax=1000 IntegerEqualityDemo` を実行すると、1000の参照比較もtrueになることを観察できます。値比較とnullのテストは同じ結果になります。
