# Collectors.toMapのnull値と、IDを残す変換

Java 17用の独立サンプルです。Spring、DB、外部APIには接続しません。

## この例の仕様

- 一覧と各Userはnullではなく、IDもnullではありません。
- User.nameだけは未設定を表すnullを許します。
- **未設定のIDも結果に残し、同じIDが2回現れたら例外にします。**
- Mapの反復順序は仕様にしません。Mainは表示だけTreeMapでキー順に揃えます。
- 返すMapは可変です。null値を保つため、Map.copyOfやCollectors.toMapへ再変換しません。

## 実行

JDK 17とMavenが必要です。最初のMaven実行では、POMで固定したJUnitとプラグインの取得が必要です。

```sh
mvn clean test
mvn exec:java
```

期待する標準出力はexpected-output.txtです。Maven自身のINFO行は含めません。

```text
twoArguments=NullPointerException
hashMapSupplier=NullPointerException
filter={U001=田中}
default={U001=田中, U002=未設定}
preserve={U001=田中, U002=null}
U002 present=true, value=null
U999 present=false, value=null
duplicateFirstNull=IllegalArgumentException
```

JUnitを使わずMainだけ実行する場合は、同じプロジェクト直下で次のようにします。

```sh
mkdir -p out
javac -encoding UTF-8 -d out src/main/java/info/syachiku/tomapnull/User.java src/main/java/info/syachiku/tomapnull/UserMaps.java src/main/java/info/syachiku/tomapnull/Main.java
java -cp out info.syachiku.tomapnull.Main
```

## 読むファイル

- `User.java`: IDは必須、nameはnullableという型。
- `UserMaps.java`: toMapの2引数/4引数での再現と、3つの変換方針。
- `Main.java`: 同じ入力で例外と結果を比較。
- `UserMapsTest.java`: 再現と仕様を確認する12テスト。

本文の短い抜粋は`BEGIN ...`/`END ...`コメントの範囲に対応します。

## なぜHashMapを指定しても例外になるか

`toMapTwoArguments`は、キーが一意でもvalueMapperがnullを返すとNullPointerExceptionになります。
`toMapWithHashMapSupplier`は生成先をHashMapに指定しますが、格納処理はMap.mergeを使い、渡す値のnullを許しません。
HashMap.putがnullを許すことと、Collectors.toMapでnull値を集約できることは別です。

## 3つの方針は同じ意味ではない

- `withoutUnsetNames`: nullの行を除きます。U002というID自体が結果から消えます。
- `withDefaultName`: nullを文字列「未設定」に置換します。データとしてのnullは失われます。
- `preservingUnsetNames`: IDを残してnull値を保持します。今回の仕様を満たす修正です。

filter/defaultの2つは**比較用**です。ID保持と重複ID拒否の全仕様を満たす代替実装として扱いません。
filterでは、null名の行を除いたために重複の存在まで消える場合があります。defaultでは、実際の名前が「未設定」である場合との区別もなくなります。
表示時の見せ方だけを変えたい場合、元データのnullを保ち、表示層で文字列にする判断もできます。

## 重複判定にcontainsKeyを使う理由

値がnullでも登録済みIDは存在します。getがnullかどうかだけでは未登録と区別できません。
putIfAbsentも、既存nullの場合は新しい値へ置換してnullを返すため、今回の重複検出には使えません。
`putIfAbsentCannotDetectAnExistingNullValue`テストでこの挙動を単独確認できます。

## 12テストと確認する仕様

1. nameが全て非nullなら2引数toMapが成功。
2. 一意IDでもnull値の2引数toMapはNPE。
3. HashMap supplier付き4引数toMapもNPE。
4. filterではU002が消える。
5. defaultではnullが文字列になる。
6. 修正後はU002の登録済みnullとU999の未登録を区別。
7. 通常の名前同士の重複IDを拒否。
8. 先の名前がnullでも重複IDを拒否。
9. 両方の名前がnullでも重複IDを拒否。
10. putIfAbsentによる誤った重複判定の反例。
11. 結果Mapの変更で入力User一覧が変わらない。
12. 空一覧は空Mapになる。

## 検証環境・範囲

2026-11-17にJDK 17.0.8 / Maven 3.9.16 / JUnit Jupiter 5.12.2で検証しました。
元フォルダーと配布ZIP展開先をそれぞれ`mvn -o -B clean test exec:java`で実行し、12件成功とMainの同じ8行を確認しています。
オフライン実行には既存依存キャッシュを使用しました。依存キャッシュが空の端末からの初回取得は未検証です。

nullの一覧・nullの行・nullキー・並列Stream・性能比較は対象外です。Map一般の全null制約をこの例に一般化しません。

## 一次資料

- Java 17 Collectors.toMap: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/stream/Collectors.html#toMap(java.util.function.Function,java.util.function.Function)
- Java 17 Map.merge: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Map.html#merge(K,V,java.util.function.BiFunction)
- Java 17 Map.putIfAbsent: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Map.html#putIfAbsent(K,V)
