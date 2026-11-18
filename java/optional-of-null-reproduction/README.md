# Optionalを作る前に失敗する場合と、代替表示へ進む場合

JDK 17とMaven 3.9系で実行する、Javaだけの独立サンプルです。Spring、DB、HTTPは使いません。READMEとpom.xmlのあるフォルダーで実行します。従来のコマンドは変わりません。

```sh
mvn clean test
```

JUnitは7件です。既存の登録3件と、追加の表示名比較4件を別のテストクラスで確認します。追加比較だけを読む場合は次を実行します。

```sh
mvn -Dtest=OptionalDisplayNameTest test
```

## 同じ表示名からOptionalを作る

`OptionalDisplayName`の2メソッドは、`Optional.of`と`Optional.ofNullable`の1箇所だけが違います。同じ入力と、`guest`を返す同じ役割のSupplierを渡します。

|入力|作り方|結果|代替表示のSupplier呼出し|
|---|---|---|---:|
|null|of|NullPointerException|0回|
|null|ofNullable|guest|1回|
|佐藤|of|佐藤|0回|
|佐藤|ofNullable|佐藤|0回|

`of(null)`はOptionalを作る時点で失敗します。後ろに`orElseGet`があっても代替表示へ進めません。`ofNullable(null)`はemptyを作るため、そこで初めて`orElseGet`のSupplierが1回呼ばれます。値がある場合は、どちらも元の表示名を返し、Supplierを呼びません。

`OptionalDisplayNameTest`は戻り値・例外だけでなく、各ケースの新しいカウンターでSupplierの呼出し回数を比較します。NullPointerExceptionのメッセージ全文は固定しません。`AtomicInteger`は観測用で、並行処理の例ではありません。

4件がそれぞれ通ると、次の観測行が出ます。JUnitの実行順により行の順番は変わります。

```text
of input=null result=NullPointerException fallbackCalls=0
ofNullable input=null result=guest fallbackCalls=1
of input=佐藤 result=佐藤 fallbackCalls=0
ofNullable input=佐藤 result=佐藤 fallbackCalls=0
```

これは任意の表示名がnullなら`guest`を表示してよいと決めた例です。すべての必須値をofNullableへ変える修正ではありません。空文字や空白はnullではなく、Optionalが値を持つ状態のままです。このサンプルはtrimや空白判定を追加しません。

## 既存の登録3件は別の契約

`CustomerRegistration.java`と`CustomerRegistrationTest.java`は従来の配布から変更していません。

|登録への入力|結果|保存|
|---|---|---:|
|必須externalIdがnull|IllegalArgumentException|0回|
|externalIdあり・任意displayNameがnull|CustomerのdisplayNameはOptional.empty|1回|
|externalIdあり・displayNameが佐藤|その表示名を持つCustomer|1回|

登録例は任意表示名をOptional.emptyとして保存担当へ渡します。追加した表示用Stringの`guest`への置換とは、戻り型も役割も異なります。追加比較が保存処理を行ったとは扱わないでください。ここでのRepositoryは記録先のListへ追加する関数で、実DBではありません。

## 収録ファイルと範囲

- `src/main/java/OptionalDisplayName.java`：of/ofNullableの比較対象。
- `src/test/java/OptionalDisplayNameTest.java`：追加4件と観測行。
- `src/main/java/CustomerRegistration.java`：従来の必須値・任意値の登録例。
- `src/test/java/CustomerRegistrationTest.java`：従来の3件。
- `pom.xml`：従来と同じJava 17、JUnit Jupiter 5.12.2、compiler-plugin 3.14.1、Surefire 3.5.3。
- `LICENSE`：リポジトリのMIT LICENSEと同じ内容。

サンプルはdefault packageのままです。新規の外部依存は追加していません。依存が未取得なら最初のMaven実行で取得できる環境が必要です。別JDK、完全に空の依存キャッシュ、GUI、HTTP、DBの動作は今回の範囲ではありません。
