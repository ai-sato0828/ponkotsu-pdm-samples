# Javaの整数除算とdoubleへのキャスト

JDK 17とMaven 3.9系で確認したサンプルです。ZIPを展開したフォルダーから実行します。Javaの実行例はJDKだけで動きます。Mavenを初めて使う環境でJUnitを実行する場合は、依存取得にネット接続が必要です。

```sh
java src/main/java/ImportSuccessRate.java
mvn test
```

`ImportSuccessRate.java` は、成功1件・全4件で、doubleへの代入と割った後のキャストがどちらも0.0、割る前のキャストが0.25になることを表示します。表示は順に25.0%、66.7%、0.0%、未集計です。

`ImportSuccessRateTest.java` は有効な入力6件と範囲外4件を確かめます。結果の表示形式だけを対象にし、CSVの読み込みや金額計算は行いません。

不具合を再現するときは、本体の `double ratio = (double) succeeded / total;` を `double ratio = (double) (succeeded / total);` へ一時的に変更します。10件のうち、25.0%・66.7%・50.0%を期待する3件が失敗します。確認後は元の1行に戻してください。
