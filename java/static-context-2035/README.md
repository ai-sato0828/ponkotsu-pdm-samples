# static contextエラーを再現し、状態と依存の持ち主から直す

対象記事: https://syachiku-uz.info/java-non-static-method-static-context/

JDK 17以上、Maven 3.9系を使います。検証はOracle JDK 17.0.8・Maven 3.9.16です。
Java側はJUnit 5.12.2、Spring側はSpring Boot 3.5.16を使います。最初のMaven実行には依存関係を取得する通信が必要です。空キャッシュからの初回取得は未検証です。

## 構成

- `broken/method/MessageApp.java`: static mainからインスタンスメソッドを直接呼ぶ失敗例。
- `broken/field/Counter.java`: staticメソッドからインスタンスフィールドを直接読む失敗例。
- `java/`: 修正版MessageApp、Counter、TextNormalizer、対応JUnitとPOM。
- `anti-pattern/OrderFinderDemo.java`: staticフィールドへ依存を入れたとき、後から渡した依存に入れ替わる比較用の単独Java例。
- `spring/`: OrderService、Controller、Runner、Repository等を揃えた別のSpringサンプル。外部DB不要。
- `excerpts/OrderFinder.java.txt`: 記事内のSpring向けNG抜粋の原文。単体コンパイル用ではありません。共有の再現には上記anti-patternを使います。

失敗例はMavenのsrc/main/javaへ入れていません。同名のMessageApp/Counterがあるので、`javac`へ全Javaファイルをまとめて渡さないでください。

## 1. 2種類のコンパイルエラー

ZIPを展開した直下から、1本ずつ実行します。両方とも終了コード1になるのが期待結果です。

```sh
mkdir -p out/method out/field
javac -J-Duser.language=en -encoding UTF-8 --release 17 -d out/method broken/method/MessageApp.java
javac -J-Duser.language=en -encoding UTF-8 --release 17 -d out/field broken/field/Counter.java
```

1本目はMessageApp.javaの4行目で `non-static method printMessage() cannot be referenced from a static context`、2本目はCounter.javaの6行目で `non-static variable count cannot be referenced from a static context` が出ます。診断文の前のパスや日本語/英語は環境により異なります。このコマンドでは英語表示を指定しています。JDK17.0.8で確認しました。

## 2. 呼び出し先のインスタンスを指定する

```sh
mvn -f java/pom.xml clean test
java -cp java/target/classes MessageApp
```

JUnit3件成功、mainは `Hello` を1行表示します。CounterTestの `increment()` と `getCount()` は同じ配布のCounter.javaに実装しています。TextNormalizerには `java.util.Locale` のimportを含めています。

CounterTestはfirstを2回、secondを1回増やし、first=2/second=1を確認します。検証用のコピーだけでCounterの `private int count;` を `private static int count;` に変え、`mvn -f java/pom.xml test -Dtest=CounterTest` を実行すると、最初のassertEqualsでexpected=2、actual=3となります。共有した値がfirstに返るためです。変更を元へ戻し、もう一度3件を通してください。

## 3. staticフィールドに依存を入れた場合を比較する

```sh
java anti-pattern/OrderFinderDemo.java
```

```text
first=A-1
afterSecond=B-1
```

同じstaticメソッドを呼んでも、2つ目のOrderFinderを作ると参照先がBへ変わります。OrderFinderDemoは記事のSpring抜粋とは別の単独実行例です。Springを起動しなくても共有の問題だけを観測できます。

## 4. Spring管理のBeanを使う

```sh
mvn -f spring/pom.xml clean test
```

JUnit3件です。

1. 実ApplicationContextをWebサーバーなしで起動。CommandLineRunnerから渡されたServiceを呼び、注文1・2をPENDINGからPROCESSEDへ変え、未処理0件になることを確認。
2. 別Repositoryをコンストラクタに渡した2つのServiceで、片方の処理がもう片方へ漏れないことを確認。
3. MockMvcでController→Service→Repositoryを通し、既存ID1は200・PENDING、存在しない999は404を確認。

MockMvcの3番目は独立した未処理Repositoryを使うのでPENDING、ApplicationContextを起動する1番目ではRunnerが先に処理するのでPROCESSEDです。期待値の違いは入力状態の違いです。

手元でAPIを起動する場合:

```sh
mvn -f spring/pom.xml spring-boot:run
```

起動後、別の端末で:

```sh
curl -i http://127.0.0.1:18035/api/orders/1
curl -i http://127.0.0.1:18035/api/orders/999
```

起動時にRunnerが動くので1は200・`{"id":1,"status":"PROCESSED"}`、999は404が期待値です。終了は起動した端末でCtrl+Cです。127.0.0.1だけで待ち受けます。作成時に127.0.0.1で起動し、実HTTPでもこの2応答を確認しました。通常の権限制限内ではローカル待受が拒否されたため、許可された一時起動で検証し、確認後に終了しています。自動テストはApplicationContextとMockMvcの3件です。

Springの各publicクラスは同名.javaの別ファイルです。BatchApplicationとOrderBatchを1つの.javaへ貼り付けないでください。データはインメモリで、外部DBの永続化、トランザクション、同時リクエストの安全性、実運用の認証は対象外です。

## 根拠

- Java17言語仕様 8.4.3.2: https://docs.oracle.com/javase/specs/jls/se17/html/jls-8.html#jls-8.4.3.2
- String.toUpperCase(Locale): https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html#toUpperCase(java.util.Locale)
- CommandLineRunner: https://docs.spring.io/spring-boot/3.5/api/java/org/springframework/boot/CommandLineRunner.html
