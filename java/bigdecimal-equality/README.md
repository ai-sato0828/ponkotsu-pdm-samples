# BigDecimalの同額判定と集合のキー

架空の金額で、数値比較とscaleを含む等価性が別であることを確認します。DB・実取引・通貨計算は扱いません。

必要環境: Java 17、Maven 3.9系。JUnit 5.12.2はpom.xmlで固定しています。

    mvn -B test
    java -cp target/classes info.syachiku.samples.value.MoneyComparisonDemo

- MoneyRules.java: 数値の同額判定、丸めなし2桁化、上限を含む判定。
- MoneyComparisonDemo.java: 1200.0と1200.00の比較、HashSet／HashMap／TreeSet、正規化前後と丸めの違い。
- MoneyRulesTest.java: 24回の独立テスト実行。B01〜06は比較とnull、N01〜08は2桁化、C01〜04は集合とMap、R01は比較前の丸め、D01はdouble由来の値、L01〜04は上限とnull。

2桁化は金額が変わらない場合だけ許可します。1200.000は1200.00にでき、1200.001は例外です。nullは0へ変えません。Setの例は金額の種類を集めるためのもので、同額の請求書を重複と判断する仕様ではありません。

Java 17／Maven 3.9.5と既存の依存キャッシュで確認。初回依存取得、他OS・JDK、DBのDECIMAL列は未確認です。
