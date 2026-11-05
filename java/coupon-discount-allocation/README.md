# クーポン割引を明細へ按分する

注文全体の固定額クーポン1枚を、明細小計の比率に応じて整数円で配ります。注文小計は1円以上、明細小計は0円以上、クーポンは注文小計以下です。明細IDは一意で、端数が同じときはIDの昇順を使います。戻り値は明細IDで引くため、反復順は契約に含めません。

## 実行

JDK 17とMaven 3.9系で、ZIPを展開して `pom.xml` のある場所で `mvn test` を実行します。全12件が成功します。

- `OrderLine.java` は明細IDと小計を保持します。
- `DiscountAllocator.java` は注文小計とクーポン額を照合し、配分表を返します。
- `DiscountAllocatorTest.java` は総額、端数の順位、並び替え、0円、範囲外、重複を検証します。

失敗も見る場合は、展開した作業用コピーで一箇所だけ変更します。

1. `int remaining = couponYen - allocated;` を `int remaining = 0;` に変更すると、`equalLinesKeepCouponTotal` が期待100円に対して実際99円で失敗します。元へ戻します。
2. `.thenComparing(Share::lineId)` を取り除くと、`sameRemainderDoesNotDependOnInputOrder` が失敗します。同額の明細を逆順に渡すと、余った1円を受け取るIDが変わります。元へ戻します。

金額はこの記事の架空仕様です。税や複数割引、クーポンの利用資格は扱いません。
