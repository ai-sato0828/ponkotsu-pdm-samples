# Mapの年齢を整数に変換する境界

JDK 17とMaven 3を使います。ZIPを展開し、`pom.xml` のあるフォルダで `mvn test` を実行します。

この例は年齢を0〜130の整数と定め、解析済みのMapで受け取る数値型を `Integer`、`Long`、`BigDecimal` に絞ります。文字列の数値や `Double` は型違いです。整数として値を保てる `BigDecimal("20.0")` は許可します。

`UserMapper.java` の `Math.toIntExact(number)` と `number.intValueExact()` をそれぞれ `number.intValue()` に戻して実行すると、`BigDecimal("20.5")` と `4_294_967_316L` がともに20になり、拒否するべき2件が失敗します。

JSONの解析、HTTPステータス、DB保存は含みません。利用するパーサーが数値をどのJava型で返すかはアプリケーション側で確認します。
