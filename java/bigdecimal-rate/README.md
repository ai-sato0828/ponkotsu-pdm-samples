# Java BigDecimalの割り算と達成率

JDK 17とMaven 3.9系。展開したフォルダで実行します。

```sh
mvn clean test
```

- `RateCalculator.rate` は完了件数を総件数で割り、小数4桁を `HALF_UP` で返します。返す値は百分率に変換する前の比率です。
- `2 / 3` は `0.6667`、`0 / 3` は `0.0000` です。
- 総件数が0なら未計算とし、`IllegalArgumentException("total must not be zero")` を投げます。
- 記事の丸め方の例として、`1 / 6` を小数2桁へ丸めた `0.17` の確認も収録しています。
