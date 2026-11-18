# HashSet.addは既存の社員を更新しない

同じ社員ID `E001` の、名前が「田中」の元データと「佐藤」の新データを比較するローカル検証です。この例では社員IDだけを同一性に使い、名前を比較に含めません。IDと名前のフィールドは変更しません。

JDK 17だけで実行できます。Mavenや外部ライブラリは不要です。

```sh
javac -encoding UTF-8 -d classes HashSetDuplicateProbe.java
java -cp classes HashSetDuplicateProbe
```

```text
HashSet.add: id=E001, result=false, size=1, keptName=田中, sameAsOriginal=true
Map.put: id=E001, previousName=田中, size=1, currentName=佐藤, sameAsIncoming=true
```

`HashSet.add`は新しい名前を取り込む更新処理ではありません。同一IDの別オブジェクトを渡しても、この例では元のオブジェクトが残ります。`equals`で同じと確認するだけでは名前の取り違えを見逃すため、元の参照が残ったかと、残った名前の両方を検証します。

同じIDの最新データへ常に置き換える要件なら、IDをキーにした`HashMap.put`が対照になります。これは既存値を返し、新しい値を保存します。重複をエラーにするCSV取込などへ、置換をそのまま適用してはいけません。拒否する要件なら`add`の`false`を受けて呼び出し側で拒否する判断が必要です。

各条件は通常のメソッドでチェックし、不一致なら`AssertionError`を投げます。`java -ea`の有無には依存しません。JUnitの2件という数え方ではなく、Java mainの独立した2シナリオです。
