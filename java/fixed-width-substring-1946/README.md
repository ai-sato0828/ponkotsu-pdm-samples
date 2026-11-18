# 固定位置のsubstringで短い取込入力を切らない

先頭2文字に続く4文字を取り出す、ASCIIの取込コード例です。`AB1234` の `substring(2,6)` は `1234` になります。入力が短いときに範囲外例外へ任せるBeforeと、必要な長さを理由付きで拒否するAfterを同じ入力で比べます。

## 実行

JDK17とMaven3.9系で、READMEとpom.xmlのあるフォルダーから実行します。

```sh
mvn test
java -cp target/classes example.importcode.FixedWidthDemo
```

JUnitは12件です。実行対象はFixedWidthCode、MainはFixedWidthDemo、テスト全文はFixedWidthCodeTestです。旧java/index-boundariesの5ファイル・10テストは別サンプルのまま保持しています。

## この入力契約

- nullは両方でObjects.requireNonNullによりNullPointerException。空文字と同じ扱いにしません。
- BeforeのextractUncheckedはsubstring(2,6)を直接呼び、6文字未満では範囲外例外です。
- AfterのextractPayloadは6文字以上を要求し、足りなければIllegalArgumentExceptionへ必要長6と実際の長さを含めて拒否します。空文字や短い入力を空の正常値へ補正しません。
- 6文字ちょうどは受け付けます。長いAB1234TAILでも位置2〜5だけを取り、後ろのTAILは返しません。
- ABwxyzもwxyzとして受け付けます。これは位置と必要長だけの例で、先頭文字、数字4桁、記号やコード全体の書式は検証しません。

ASCIIを使う例に限定しています。String.length/substringの位置はUTF-16のchar単位です。このコードが任意Unicodeの見た目の文字数やUTF-8のバイト幅を検証するとは扱いません。ASCIIであるか自体を検査する機能も実装していません。

## 実行結果

```text
input="AB1234" before=1234 after=1234
input="AB123" before=StringIndexOutOfBoundsException after=IllegalArgumentException: 取込コードは6文字以上が必要です: 実際=5
input="" before=StringIndexOutOfBoundsException after=IllegalArgumentException: 取込コードは6文字以上が必要です: 実際=0
input="AB1234TAIL" before=1234 after=1234
```

BeforeでStringIndexOutOfBoundsExceptionとなったのはJDK17.0.8のこの実測です。Java17のString.substring(begin,end) APIが宣言する例外契約はIndexOutOfBoundsExceptionなので、テストもその親型で確認します。JDK由来の例外メッセージ全文は固定assertせず、Afterが自分で設定した拒否理由だけをassertします。

## 12件の内訳

|範囲|ケース数|確認|
|---|---|---|
|正常比較|3|AB1234、AB1234TAIL、ABwxyzを両版で同じ4文字へ|
|Beforeの短い入力|2|AB123/空がsubstring内で範囲外|
|Afterの短い入力|2|AB123/空を必要6文字・実際長付きで明示拒否|
|null|1|両版ともNPE|
|JDKの半開区間|4|substring(length,length)は空、end>length/逆順/負beginは範囲外|

終了位置は含まない境目であり、要素の添字ではありません。substring(length,length)の空区間は有効ですが、固定のsubstring(2,6)には少なくともlength=6が必要です。この区別だけを扱い、別記事で実証済みのsubListのビュー/コピー/境界は再実装していません。

## 確認環境と範囲

Java17.0.8 / Maven3.9.16 / JUnit5.12.2 / Surefire3.5.6。Maven pluginを明示固定し、既存の依存キャッシュでオフライン実行しました。旧index-boundariesのPOMは変更していません。

空キャッシュからの初回取得、他JDK、任意Unicode/バイト幅、ファイル入出力、Spring/DB/外部APIは未確認です。収録はJava対象・Main・JUnit・POM・README・MIT LICENSE、故障検出copyは含みません。

参考: [Java17 String.substring(int,int)](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html#substring(int,int))。beginを含みendを含まず、負begin・end>length・begin>endが範囲外である契約を確認しています。
